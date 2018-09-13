package org.sagebionetworks.research.mpower.tracking.view_model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;
import org.sagebionetworks.research.mpower.tracking.model.TrackingStepView;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.MedicationConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.LoggingCollection;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SimpleTrackingItemLog;

public class MedicationTrackingTaskViewModel extends TrackingTaskViewModel<MedicationConfig, SimpleTrackingItemLog> {
    protected MedicationTrackingTaskViewModel(
            @NonNull final TrackingStepView stepView,
            @Nullable final LoggingCollection<SimpleTrackingItemLog> previousLoggingCollection) {
        super(stepView, previousLoggingCollection);
    }

    @Override
    protected LoggingCollection<SimpleTrackingItemLog> instantiateLoggingCollection() {
        return LoggingCollection.<SimpleTrackingItemLog>builder()
                .setIdentifier("trackedItems")
                .build();
    }

    @Override
    protected SimpleTrackingItemLog instantiateLogForUnloggedItem(@NonNull final MedicationConfig config) {
        return SimpleTrackingItemLog.builder()
                .setIdentifier(config.getIdentifier())
                .setText(config.getIdentifier())
                .build();
    }

    @Override
    protected MedicationConfig instantiateConfigFromSelection(@NonNull final TrackingItem item) {
        return MedicationConfig.builder()
                .setIdentifier(item.getIdentifier())
                .setTrackingItem(item)
                .build();
    }
}
