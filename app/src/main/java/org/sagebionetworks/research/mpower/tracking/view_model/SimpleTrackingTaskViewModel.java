package org.sagebionetworks.research.mpower.tracking.view_model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;
import org.sagebionetworks.research.mpower.tracking.model.TrackingStepView;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.SimpleTrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.LoggingCollection;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SimpleTrackingItemLog;

/**
 * Subclass of TrackingTaskViewModel which uses the most basic types of configs, and logs.
 */
public class SimpleTrackingTaskViewModel extends
        TrackingTaskViewModel<SimpleTrackingItemConfig, SimpleTrackingItemLog> {
    protected SimpleTrackingTaskViewModel(
            @NonNull final TrackingStepView stepView,
            @Nullable final LoggingCollection<SimpleTrackingItemLog> previousLoggingCollection) {
        super(stepView, previousLoggingCollection);
    }

    @Override
    protected SimpleTrackingItemLog instantiateLogForUnloggedItem(@NonNull final SimpleTrackingItemConfig config) {
        return SimpleTrackingItemLog.builder()
                .setIdentifier(config.getIdentifier())
                .setText(config.getIdentifier())
                .build();
    }

    @Override
    protected LoggingCollection<SimpleTrackingItemLog> instantiateLoggingCollection() {
        return LoggingCollection.<SimpleTrackingItemLog>builder()
                .setIdentifier(TrackingTaskViewModel.LOGGING_COLLECTION_IDENTIFIER)
                .build();
    }

    @Override
    protected SimpleTrackingItemConfig instantiateConfigFromSelection(@NonNull final TrackingItem item) {
        return SimpleTrackingItemConfig.builder().setTrackingItem(item).build();
    }
}
