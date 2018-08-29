package org.sagebionetworks.research.mpower.tracking.view_model;

import android.support.annotation.NonNull;

import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;
import org.sagebionetworks.research.mpower.tracking.model.TrackingStepView;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.SimpleTrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SimpleTrackingItemLog;

/**
 * Subclass of TrackingTaskViewModel which uses the most basic types of configs, and logs.
 */
public class SimpleTrackingTaskViewModel extends
        TrackingTaskViewModel<SimpleTrackingItemConfig, SimpleTrackingItemLog> {
    protected SimpleTrackingTaskViewModel(
            @NonNull final TrackingStepView stepView) {
        super(stepView);
    }

    @Override
    protected SimpleTrackingItemConfig instantiateConfigFromSelection(@NonNull final TrackingItem item) {
        return SimpleTrackingItemConfig.builder().setTrackingItem(item).build();
    }
}
