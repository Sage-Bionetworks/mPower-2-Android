package org.sagebionetworks.research.mpower.tracking.view_model;

import android.support.annotation.NonNull;

import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;
import org.sagebionetworks.research.mpower.tracking.model.TrackingStep;

public class SimpleTrackingActiveTaskViewModel extends TrackingActiveTaskViewModel<SimpleTrackingItemConfig, SimpleTrackingItemLog> {
    protected SimpleTrackingActiveTaskViewModel(
            @NonNull final TrackingStep step) {
        super(step);
    }

    @Override
    protected SimpleTrackingItemConfig instantiateConfigFromSelection(@NonNull final TrackingItem item) {
        return SimpleTrackingItemConfig.builder().setTrackingItem(item).build();
    }
}
