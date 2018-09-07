package org.sagebionetworks.research.mpower.tracking.view_model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;
import org.sagebionetworks.research.mpower.tracking.model.TrackingSection;
import org.sagebionetworks.research.mpower.tracking.model.TrackingStepView;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.TrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.TrackingItemLog;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

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
    // invariant availableElements.getValue() != null
    protected MutableLiveData<Map<TrackingSection, Set<TrackingItem>>> availableElements;

    // invariant activeElementsById.getValue() != null
    protected MutableLiveData<Map<String, ConfigType>> activeElementsById;

    // invariant loggedElementsById.getValue() != null
    protected MutableLiveData<Map<String, LogType>> loggedElementsById;

    protected TrackingStepView stepView;

    protected TrackingTaskViewModel(@NonNull final TrackingStepView stepView) {
        this.stepView = stepView;
        this.availableElements = new MutableLiveData<>();
        this.activeElementsById = new MutableLiveData<>();
        this.availableElements.setValue(stepView.getSelectionItems());
        this.activeElementsById.setValue(new HashMap<>());
        this.loggedElementsById = new MutableLiveData<>();
        this.loggedElementsById.setValue(new HashMap<>());
    }

    // region Selection:
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
     * Called to indicate that the given tracking item ahs been selected by the user as one they would like to track.
     *
     * @param item
     *         The item the user has indicated they would like to track.
     */
    public void itemSelected(@NonNull TrackingItem item) {
        Map<String, ConfigType> activeElements = this.activeElementsById.getValue();
        if (!activeElements.containsKey(item.getIdentifier())) {
            ConfigType config = this.instantiateConfigFromSelection(item);
            activeElements.put(config.getIdentifier(), config);
        }

        this.activeElementsById.setValue(activeElements);
    }

    /**
     * Called to indicate that the user has removed the given tracking item from the set of items they wish to track.
     *
     * @param item
     *         The item the user no longer wishes to track.
     */
    public void itemDeselected(@NonNull TrackingItem item) {
        Map<String, ConfigType> activeElements = this.activeElementsById.getValue();
        activeElements.remove(item.getIdentifier());
        this.activeElementsById.setValue(activeElements);
    }
    // endregion

    // region Configuration

    /**
     * Returns the set of active elements sorted in alphabetical order based on identifier.
     * @return the set of active elements sorted in alphabetical order based on identifier.
     */
    public LiveData<Set<ConfigType>> getActiveElementsSorted() {
        return Transformations.map(this.activeElementsById, elements -> {
            Set<ConfigType> result = new TreeSet<>((o1, o2) -> o1.getIdentifier().compareTo(o2.getIdentifier()));
            result.addAll(elements.values());
            return result;
        });
    }

    /**
     * Returns a map from identifier to config for the active elements in this view model.
     * @return a map from identifier to config for the active elements in this view model.
     */
    public LiveData<Map<String, ConfigType>> getActiveElementsById() {
        return this.activeElementsById;
    }

    /**
     * Returns a LiveData containing the Config with the given identifier or null if there is no Config with the given
     * identifier.
     *
     * @param identifier
     *         the identifier of the Config to get.
     * @return a LiveData containing the Config with the given identifier or null if there is no Config with the given
     *         identifier.
     */
    public LiveData<ConfigType> getActiveElement(@NonNull String identifier) {
        return Transformations.map(this.activeElementsById, elements -> elements.get(identifier));
    }
    // endregion

    // region Logging

    /**
     * Returns a LiveData containing the log with the given identifier or null if there is no log with the given
     * identifier.
     *
     * @param identifier
     *         The identifier of the log to get.
     * @return a LiveData containing the log with the given identifier or null if there is no log with the given
     *         identifier.
     */
    public LiveData<LogType> getLoggedElement(@NonNull String identifier) {
        return Transformations
                .map(this.loggedElementsById, elements -> this.loggedElementsById.getValue().get(identifier));
    }

    /**
     * Returns a map from identifier to Logged element for the logged elements in this view model.
     * @return a map from identifier to Logged element for the logged elements in this view model.
     */
    public LiveData<Map<String, LogType>> getLoggedElementsById() {
        return this.loggedElementsById;
    }

    /**
     * Returns true if the item with the given identifier has been logged by the user, false otherwise.
     *
     * @param identifier
     *         the identifier of the log to check for.
     * @return true if the time with the given identifier has been logged by the user, false otherwise.
     */
    public boolean isLogged(@NonNull String identifier) {
        return this.loggedElementsById.getValue().containsKey(identifier);
    }

    /**
     * Returns the log for the given identifier, or null if no such log exists.
     *
     * @return the log for the given identifier, or null if no such log exists.
     */
    @Nullable
    public LogType getLog(@NonNull String identifier) {
        return this.loggedElementsById.getValue().get(identifier);
    }

    /**
     * Adds the given log to the set of logged elements removing any previous logs with the same tracking item.
     *
     * @param log
     *         the log to add to the set of logged elements.
     */
    public void addLoggedElement(@NonNull LogType log) {
        Map<String, LogType> result = this.loggedElementsById.getValue();
        result.put(log.getIdentifier(), log);
        this.loggedElementsById.setValue(result);
    }

    /**
     * Removes the log with the given identifier from the set of logged elements.
     *
     * @param identifier
     *         the identifier of the log to remove from the set of logged elements.
     */
    public void removeLoggedElement(@NonNull String identifier) {
        Map<String, LogType> result = this.loggedElementsById.getValue();
        result.remove(identifier);
        this.loggedElementsById.setValue(result);
    }
    // endregion

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
