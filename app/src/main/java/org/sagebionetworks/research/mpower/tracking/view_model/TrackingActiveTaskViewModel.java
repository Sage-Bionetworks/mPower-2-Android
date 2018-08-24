package org.sagebionetworks.research.mpower.tracking.view_model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;
import org.sagebionetworks.research.mpower.tracking.model.TrackingSection;
import org.sagebionetworks.research.mpower.tracking.model.TrackingStep;
import org.sagebionetworks.research.mpower.tracking.model.TrackingStepView;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class TrackingActiveTaskViewModel<ConfigType extends TrackingItemConfig, LogType extends TrackingItemLog>
        extends ViewModel {
    protected MutableLiveData<Map<TrackingSection, Set<TrackingItem>>> availableElements;
    protected MutableLiveData<Set<ConfigType>> activeElements;
    protected LiveData<Set<ConfigType>> unconfiguredElements;
    protected MutableLiveData<Set<LogType>> loggedElements;

    protected TrackingActiveTaskViewModel(@NonNull final TrackingStepView stepView) {
        this.availableElements = new MutableLiveData<>();
        this.availableElements.setValue(stepView.getSelectionItems());
        this.activeElements = new MutableLiveData<>();
        this.activeElements.setValue(new HashSet<>());
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

    public LiveData<Map<TrackingSection, Set<TrackingItem>>> getAvailableElements() {
        return this.availableElements;
    }

    public LiveData<Set<ConfigType>> getActiveElements() {
        return this.activeElements;
    }

    public LiveData<Set<ConfigType>> getUnconfiguredElements() {
        return this.unconfiguredElements;
    }

    public LiveData<Set<LogType>> getLoggedElements() {
        return this.loggedElements;
    }

    public void itemSelected(@NonNull TrackingItem item) {
        Set<ConfigType> activeElements = this.activeElements.getValue();
        if (activeElements == null) {
            activeElements = new HashSet<>();
        }

        if (!TrackingActiveTaskViewModel.containsMatchingTrackingItem(activeElements, item)) {
            activeElements.add(this.instantiateConfigFromSelection(item));
        }

        this.activeElements.setValue(activeElements);
    }

    public void itemDeselected(@NonNull TrackingItem item) {
        Set<ConfigType> result = new HashSet<>();
        Set<ConfigType> activeElements = this.activeElements.getValue();
        if (activeElements == null) {
            return;
        }

        // Filter the config for the given item out.
        for (ConfigType config : activeElements) {
            if (!config.getTrackingItem().equals(item)) {
                result.add(config);
            }
        }

        this.activeElements.setValue(result);
    }

    public boolean isSelected(@NonNull TrackingItem trackingItem) {
        Set<ConfigType> activeElements = this.activeElements.getValue();
        if (activeElements != null) {
            return TrackingActiveTaskViewModel
                    .containsMatchingTrackingItem(activeElements, trackingItem);
        } else {
            return false;
        }
    }

    private static <E extends HasTrackingItem> boolean containsMatchingTrackingItem(@NonNull Set<E> items,
            @NonNull TrackingItem item) { ;
        for (HasTrackingItem hasTrackingItem : items) {
            if (hasTrackingItem.getTrackingItem().equals(item)) {
                return true;
            }
        }

        return false;
    }

    public void addActiveElement(@NonNull ConfigType config) {
        Set<ConfigType> result = new HashSet<>();
        Set<ConfigType> activeElements = this.activeElements.getValue();
        if (activeElements != null) {
            for (ConfigType currentConfig : activeElements) {
                // Filter out the old Config if there is one.
                if (!currentConfig.getTrackingItem().equals(config.getTrackingItem())) {
                    result.add(currentConfig);
                }
            }
        }

        result.add(config);
        this.activeElements.setValue(result);
    }

    public void addLoggedElement(@NonNull LogType log) {
        Set<LogType> result = new HashSet<>();
        Set<LogType> loggedElements = this.loggedElements.getValue();
        if (loggedElements != null) {
            for (LogType currentLog : loggedElements) {
                // Filter out the old log if there is one.
                if (!currentLog.getTrackingItem().equals(log.getTrackingItem())) {
                    result.add(currentLog);
                }
            }
        }

        result.add(log);
        this.loggedElements.setValue(result);
    }

    protected abstract ConfigType instantiateConfigFromSelection(@NonNull TrackingItem item);
}
