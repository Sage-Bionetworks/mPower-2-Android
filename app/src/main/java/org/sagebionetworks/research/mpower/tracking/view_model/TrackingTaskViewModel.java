package org.sagebionetworks.research.mpower.tracking.view_model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;
import org.sagebionetworks.research.mpower.tracking.model.TrackingSection;
import org.sagebionetworks.research.mpower.tracking.model.TrackingStepView;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.TrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.LoggingCollection;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.TrackingItemLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import java.util.HashMap;
import java.util.Map;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(TrackingTaskViewModel.class);

    public static final String LOGGING_COLLECTION_IDENTIFIER = "trackedItems";

    // invariant availableElements.getValue() != null
    @NonNull
    protected MutableLiveData<Map<TrackingSection, Set<TrackingItem>>> availableElements;

    @NonNull
    protected Map<String, TrackingItem> trackingItemsById;

    @NonNull
    // invariant activeElementsById.getValue() != null
    protected MutableLiveData<Map<String, ConfigType>> activeElementsById;

    @NonNull
    // invariant loggedElementsById.getValue() != null
    protected MutableLiveData<Map<String, LogType>> loggedElementsById;

    protected Instant startDate;

    protected Instant endDate;

    @NonNull
    protected TrackingStepView stepView;

    protected TrackingTaskViewModel(@NonNull final TrackingStepView stepView,
            @Nullable final LoggingCollection<LogType> previousLoggingCollection) {
        this.stepView = stepView;
        availableElements = new MutableLiveData<>();
        activeElementsById = new MutableLiveData<>();
        loggedElementsById = new MutableLiveData<>();
        availableElements.setValue(stepView.getSelectionItems());
        trackingItemsById = getTrackingItemsById(availableElements.getValue());
        loggedElementsById.setValue(new HashMap<>());
        // initialize the active elements to contain either the user's previous selection or nothing depending on if
        // we have a previous logging collection.
        if (previousLoggingCollection == null) {
            activeElementsById.setValue(new HashMap<>());
        } else {
            Map<String, ConfigType> activeElements = new HashMap<>();
            for (LogType log : previousLoggingCollection.getItems()) {
                String identifier = log.getIdentifier();
                activeElements.put(identifier, instantiateConfigFromSelection(trackingItemsById.get(identifier)));
            }

            activeElementsById.setValue(activeElements);
        }
    }

    protected static Map<String, TrackingItem> getTrackingItemsById(
            Map<TrackingSection, Set<TrackingItem>> availableElements) {
        Map<String, TrackingItem> result = new HashMap<>();
        for (Set<TrackingItem> itemSet : availableElements.values()) {
            for (TrackingItem item : itemSet) {
                result.put(item.getIdentifier(), item);
            }
        }

        return result;
    }

    /**
     * Sets the timestamp the task started at to the given instant.
     *
     * @param startDate
     *         The instant to use as the start date for task.
     */
    public void setTaskStartDate(@Nullable Instant startDate) {
        this.startDate = startDate;
    }

    /**
     * Sets the timestamp the task ended at to the given instant.
     *
     * @param endDate
     *         The instant to use as the end date for task.
     */
    public void setTaskEndDate(@Nullable Instant endDate) {
        this.endDate = endDate;
    }

    /**
     * Returns the final LoggingCollection for the task. The LoggingCollection will contain: - the user entered logs
     * for every item the user has created a log for - a basic log indicating the user selected an item but didn't log
     * it for every item the user indicated is active but didn't create a log for.
     *
     * @return The LoggingCollection for task.
     */
    public LoggingCollection<LogType> getLoggingCollection() {
        Instant now = Instant.now();
        if (startDate == null) {
            startDate = now;
            LOGGER.warn("getLoggingCollection() called with null startDate using {} instead", now);
        }

        if (endDate == null) {
            endDate = now;
            LOGGER.warn("getLoggingCollection() called with null endDate using {} instead", now);
        }

        // Add logs for the items the user didn't log on task run.
        ImmutableList.Builder<LogType> itemsBuilder = ImmutableList.builder();
        for (String identifier : activeElementsById.getValue().keySet()) {
            if (loggedElementsById.getValue().containsKey(identifier)) {
                itemsBuilder.add(loggedElementsById.getValue().get(identifier));
            } else {
                itemsBuilder.add(instantiateLogForUnloggedItem(activeElementsById.getValue().get(identifier)));
            }
        }

        return instantiateLoggingCollection().toBuilder()
                .setIdentifier(LOGGING_COLLECTION_IDENTIFIER)
                .setStartDate(startDate)
                .setEndDate(endDate)
                .setItems(itemsBuilder.build())
                .build();
    }

    /**
     * Instantiates a new LoggingCollection with the correct log type for view model.
     *
     * @return a new LoggingCollection with the correct log type for view model.
     */
    protected abstract LoggingCollection<LogType> instantiateLoggingCollection();

    /**
     * Instantiates a new log which represents that the user selected a tracking item, but chose not to log it.
     *
     * @param config
     *         the config to log instantiate the log from. (this should be the config corresponding to the
     *         TrackingItem the user selected)
     * @return a new log which represents that the user selected a tracking item, but chose not to log it.
     */
    protected abstract LogType instantiateLogForUnloggedItem(@NonNull ConfigType config);

    // region Selection:

    /**
     * Returns a LiveData containing a of TrackingSections to the TrackingItems in those sections representing all of
     * the options that the user cna choose to select.
     *
     * @return a LiveData containing a of TrackingSections to the TrackingItems in those sections representing all of
     *         the options that the user cna choose to select.
     */
    @NonNull
    public LiveData<Map<TrackingSection, Set<TrackingItem>>> getAvailableElements() {
        return availableElements;
    }

    /**
     * Called to indicate that the given tracking item ahs been selected by the user as one they would like to track.
     *
     * @param item
     *         The item the user has indicated they would like to track.
     */
    public void itemSelected(@NonNull TrackingItem item) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("itemSelected() called with: {}", item);
        }

        Map<String, ConfigType> activeElements = activeElementsById.getValue();
        if (activeElements.containsKey(item.getIdentifier())) {
            LOGGER.warn("itemSelected() called on item that is already active, item was: {}", item);
        } else {
            ConfigType config = instantiateConfigFromSelection(item);
            activeElements.put(config.getIdentifier(), config);
        }

        activeElementsById.setValue(activeElements);
    }

    /**
     * Called to indicate that the user has removed the given tracking item from the set of items they wish to track.
     *
     * @param item
     *         The item the user no longer wishes to track.
     */
    public void itemDeselected(@NonNull TrackingItem item) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("itemDeselected() called with: {}", item);
        }

        Map<String, ConfigType> activeElements = activeElementsById.getValue();
        if (!activeElements.containsKey(item.getIdentifier())) {
            LOGGER.warn("itemDeselected() called on item that is not active, item was: {}", item);
        } else {
            activeElements.remove(item.getIdentifier());
            activeElementsById.setValue(activeElements);
        }
    }
    // endregion

    // region Configuration

    /**
     * Returns the set of active elements sorted in alphabetical order based on identifier.
     *
     * @return the set of active elements sorted in alphabetical order based on identifier.
     */
    public LiveData<Set<ConfigType>> getActiveElementsSorted() {
        return Transformations.map(activeElementsById, elements -> {
            Set<ConfigType> result = new TreeSet<>((o1, o2) -> o1.getIdentifier().compareTo(o2.getIdentifier()));
            result.addAll(activeElementsById.getValue().values());
            return result;
        });
    }

    /**
     * Returns a map from identifier to config for the active elements in view model.
     *
     * @return a map from identifier to config for the active elements in view model.
     */
    @NonNull
    public LiveData<Map<String, ConfigType>> getActiveElementsById() {
        return activeElementsById;
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
                .map(loggedElementsById, elements -> loggedElementsById.getValue().get(identifier));
    }

    /**
     * Returns a map from identifier to Logged element for the logged elements in view model.
     *
     * @return a map from identifier to Logged element for the logged elements in view model.
     */
    @NonNull
    public LiveData<Map<String, LogType>> getLoggedElementsById() {
        return loggedElementsById;
    }

    /**
     * Returns true if the item with the given identifier has been logged by the user, false otherwise.
     *
     * @param identifier
     *         the identifier of the log to check for.
     * @return true if the time with the given identifier has been logged by the user, false otherwise.
     */
    public boolean isLogged(@NonNull String identifier) {
        return loggedElementsById.getValue().containsKey(identifier);
    }

    /**
     * Adds the given log to the set of logged elements removing any previous logs with the same tracking item.
     *
     * @param log
     *         the log to add to the set of logged elements.
     */
    public void addLoggedElement(@NonNull LogType log) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("addLoggedElement() called with: {}", log);
        }

        if (!activeElementsById.getValue().containsKey(log.getIdentifier())) {
            LOGGER.warn("Attempt to add log for an item which is not currently active, identifier: {}",
                    log.getIdentifier());
        } else {
            Map<String, LogType> result = loggedElementsById.getValue();
            result.put(log.getIdentifier(), log);
            loggedElementsById.setValue(result);
        }
    }

    /**
     * Removes the log with the given identifier from the set of logged elements.
     *
     * @param identifier
     *         the identifier of the log to remove from the set of logged elements.
     */
    public void removeLoggedElement(@NonNull String identifier) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("removeLoggedElement() called with: {}", identifier);
        }

        Map<String, LogType> result = loggedElementsById.getValue();
        if (!result.containsKey(identifier)) {
            LOGGER.warn("removeLoggedElement() called with identifier that is not logged, identifier was: {}", identifier);
        } else {
            result.remove(identifier);
            loggedElementsById.setValue(result);
        }
    }
    // endregion

    /**
     * Returns the TrackingStepView which view model uses the items from.
     *
     * @return the TrackingStepView which view model uses the items from.
     */
    @NonNull
    public TrackingStepView getStepView() {
        return stepView;
    }

    /**
     * Called to instantiate an unconfigured config when the user selects a new element, is used when the user picks a
     * new element from the selections, but hasn't necessarily added the full details for the configuration yet.
     *
     * @param item
     *         the Item to create a configuration for.
     * @return an new Unconfigured config for the given tracking item.
     */
    protected abstract ConfigType instantiateConfigFromSelection(@NonNull TrackingItem item);
}
