package org.sagebionetworks.research.mpower.tracking.view_model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;
import org.sagebionetworks.research.mpower.tracking.model.TrackingSection;
import org.sagebionetworks.research.mpower.tracking.model.TrackingStepView;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.TrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.TrackingItemLog;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The ViewModel for a TrackingTask. Manages: - The TrackingItems which are available to the user to log. -
 * TrackingItemConfigs for the items the user has indicated that they want to log. - TrackingItemLogs for the items
 * the user has actually logged.
 *
 * @param <ConfigType>
 *         The type of TrackingItemConfig.
 * @param <LogType>
 *         The type of TrackingItemLog.
 */
public abstract class TrackingTaskViewModel<ConfigType extends TrackingItemConfig, LogType extends TrackingItemLog>
        extends ViewModel {
    protected LiveData<Boolean> selectionMade;

    // invariant availableElements.getValue() != null
    protected MutableLiveData<Map<TrackingSection, Set<TrackingItem>>> availableElements;

    // invariant activeElements.getValue() != null
    protected MutableLiveData<Set<ConfigType>> activeElements;

    // invariant unconfiguredElements.getValue() != null
    protected LiveData<Set<ConfigType>> unconfiguredElements;

    // invariant loggedElements.getValue() != null
    protected MutableLiveData<Set<LogType>> loggedElements;

    protected TrackingStepView stepView;

    protected TrackingTaskViewModel(@NonNull final TrackingStepView stepView) {
        this.stepView = stepView;
        this.availableElements = new MutableLiveData<>();
        this.availableElements.setValue(stepView.getSelectionItems());
        this.activeElements = new MutableLiveData<>();
        this.activeElements.setValue(new HashSet<>());
        this.selectionMade = Transformations.map(this.activeElements, (elements) -> !elements.isEmpty());
        this.unconfiguredElements = Transformations.map(this.activeElements, (elements) -> {
            Set<ConfigType> result = new HashSet<>();
            for (ConfigType config : elements) {
                if (!config.isConfigured()) {
                    result.add(config);
                }
            }

            return result;
        });

        this.loggedElements = new MutableLiveData<>();
        this.loggedElements.setValue(new HashSet<>());
    }

    // region Selection

    /**
     * Returns a LiveData which is true whenever the user has selected at least on item, and false otherwise.
     *
     * @return a LiveData which is true whenever the user has selected at least on item and false otherwise.
     */
    public LiveData<Boolean> getSelectionMade() {
        return this.selectionMade;
    }

    /**
     * Returns a LiveData containing a of TrackingSections to the TrackingItems in those sections representing all of
     * the options that the user cna choose to select.
     *
     * @return a LiveData containing a of TrackingSections to the TrackingItems in those sections representing all of
     *         the options that the user cna choose to select.
     */
    public LiveData<Map<TrackingSection, Set<TrackingItem>>> getAvailableElements() {
        return this.availableElements;
    }

    /**
     * Returns a LiveData containing all of the logs which have been created.
     *
     * @return a LiveData containing all of the logs which have been created.
     */
    public LiveData<Set<LogType>> getLoggedElements() {
        return this.loggedElements;
    }

    /**
     * Called to indicate that the given tracking item ahs been selected by the user as one they would like to track.
     *
     * @param item
     *         The item the user has indicated they would like to track.
     */
    public void itemSelected(@NonNull TrackingItem item) {
        Set<ConfigType> activeElements = this.activeElements.getValue();
        if (!TrackingTaskViewModel.containsMatchingTrackingItem(activeElements, item)) {
            activeElements.add(this.instantiateConfigFromSelection(item));
        }

        this.activeElements.setValue(activeElements);
    }

    /**
     * Called to indicate that the user has removed the given tracking item from the set of items they wish to track.
     *
     * @param item
     *         The item the user no longer wishes to track.
     */
    public void itemDeselected(@NonNull TrackingItem item) {
        Set<ConfigType> result = new HashSet<>();
        // Filter the config for the given item out.
        for (ConfigType config : this.activeElements.getValue()) {
            if (!config.getTrackingItem().equals(item)) {
                result.add(config);
            }
        }

        this.activeElements.setValue(result);
    }
    // endregion

    // region Configuration

    /**
     * Returns a LiveData containing the set of Configs for the TrackingItems that the user has chosen to select.
     *
     * @return a LiveData containing the set of Configs for the TrackingItems that the user has chosen to select.
     */
    public LiveData<Set<ConfigType>> getActiveElements() {
        return this.activeElements;
    }

    /**
     * Returns a LiveData containing the set of Configs which the user has not finished adding data to. For example in
     * the medication task this set would contain the medication configs which do not yet have a time and dosage set.
     *
     * @return a LiveData containing the set of Configs which the user has not finished adding data to.
     */
    public LiveData<Set<ConfigType>> getUnconfiguredElements() {
        return this.unconfiguredElements;
    }

    /**
     * Returns the config for the given TrackingItem, or null if the user has not selected the given TrackingItem.
     *
     * @param trackingItem
     *         The Tracking item to get the config for.
     * @return the config for the given TrackingItem, or null if hte user has not selected the given TrackingItem.
     */
    public ConfigType getConfig(@NonNull TrackingItem trackingItem) {
        return getMatchingTrackingItem(this.activeElements.getValue(), trackingItem);
    }

    /**
     * Adds the given Configuration to the set of configurations.
     *
     * @param config
     *         the Configuration to add to the set of configurations.
     */
    public void addActiveElement(@NonNull ConfigType config) {
        Set<ConfigType> result = new HashSet<>();
        for (ConfigType currentConfig : this.activeElements.getValue()) {
            // Filter out the old Config if there is one.
            if (!currentConfig.getTrackingItem().equals(config.getTrackingItem())) {
                result.add(currentConfig);
            }
        }

        result.add(config);
        this.activeElements.setValue(result);
    }
    // endregion

    // region Logging

    /**
     * Returns true if the item with the given config has been logged by the user, false otherwise.
     *
     * @param config
     *         The Config of the item the user has logged.
     * @return true if the time with the given config has been logged by the user, false otherwise.
     */
    public boolean isLogged(@NonNull ConfigType config) {
        return TrackingTaskViewModel
                .containsMatchingTrackingItem(this.loggedElements.getValue(), config.getTrackingItem());
    }

    /**
     * Returns the log for the given tracking item, or null if no such log exists.
     *
     * @param trackingItem
     *         The tracking item to get the log for.
     * @return the log for the given tracking item, or null if no such log exists.
     */
    @Nullable
    public LogType getLog(@NonNull TrackingItem trackingItem) {
        return getMatchingTrackingItem(this.loggedElements.getValue(), trackingItem);
    }

    /**
     * Adds the given log to the set of logged elements removing any previous logs with the same tracking item.
     *
     * @param log
     *         the log to add to the set of logged elements.
     */
    public void addLoggedElement(@NonNull LogType log) {
        Set<LogType> result = this.removeLoggedElementHelper(log.getTrackingItem().getIdentifier());
        result.add(log);
        this.loggedElements.setValue(result);
    }

    /**
     * Removes the log with the given identifier from the set of logged elements.
     *
     * @param identifier
     *         the identifier of the log to remove from the set of logged elements.
     */
    public void removeLoggedElement(@NonNull String identifier) {
        this.loggedElements.setValue(this.removeLoggedElementHelper(identifier));
    }

    /**
     * Helper which removes all logs with the given identifier from the set of logged elements.
     *
     * @param identifier
     *         the identifier to remove logs for from the logged elements.
     * @return a set of logs representing the set difference of the logged elements and the logged elements which have
     *         the given identifier.
     */
    private Set<LogType> removeLoggedElementHelper(@NonNull String identifier) {
        Set<LogType> result = new HashSet<>();
        for (LogType currentLog : this.loggedElements.getValue()) {
            // Filter out the old log if there is one.
            if (!currentLog.getTrackingItem().getIdentifier().equals(identifier)) {
                result.add(currentLog);
            }
        }

        return result;
    }
    // endregion

    /**
     * Helper method which returns true if the given set contains an element which has the given TrackingItem.
     *
     * @param items
     *         The set of items to search for an element containing the TrackingItem in.
     * @param item
     *         The tracking item to check if any of the elements in the set contain.
     * @param <E>
     *         The type of elements in the set.
     * @return true if at least one of the items in the given set has the given TrackingItem.
     */
    private static <E extends HasTrackingItem> boolean containsMatchingTrackingItem(@NonNull Set<E> items,
            @NonNull TrackingItem item) {
        E result = getMatchingTrackingItem(items, item);
        return result != null;
    }

    /**
     * Helper method which returns the first element in the given set which has the given TrackingItem.
     *
     * @param items
     *         The set of items to get the element which has the given tracking item from.
     * @param item
     *         The tracking item to get the element for.
     * @param <E>
     *         The type of elements in the set.
     * @return The first element from the given set which has the given TrackingItem.
     */
    @Nullable
    private static <E extends HasTrackingItem> E getMatchingTrackingItem(@NonNull Set<E> items,
            @NonNull TrackingItem item) {
        for (E hasTrackingItem : items) {
            if (hasTrackingItem.getTrackingItem().equals(item)) {
                return hasTrackingItem;
            }
        }

        return null;
    }

    /**
     * Returns the TrackingStepView which this view model uses the items from.
     *
     * @return the TrackingStepView which this view model uses the items from.
     */
    public TrackingStepView getStepView() {
        return this.stepView;
    }

    /**
     * Called to instantiate an unconfigured config when the user selects a new element, this is used when the user
     * picks a new element from the selections, but hasn't necessarily added the full details for the configuration
     * yet.
     *
     * @param item
     *         the Item to create a configuration for.
     * @return an new Unconfigured config for the given tracking item.
     */
    protected abstract ConfigType instantiateConfigFromSelection(@NonNull TrackingItem item);
}
