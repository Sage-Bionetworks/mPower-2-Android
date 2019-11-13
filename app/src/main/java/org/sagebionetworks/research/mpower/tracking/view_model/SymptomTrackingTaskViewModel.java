package org.sagebionetworks.research.mpower.tracking.view_model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.sagebionetworks.research.mpower.tracking.fragment.SymptomLoggingFragment;
import org.sagebionetworks.research.mpower.tracking.fragment.TrackingFragment;
import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;
import org.sagebionetworks.research.mpower.tracking.model.TrackingStepView;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.SimpleTrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.LoggingCollection;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SymptomLog;

/**
 * Subclass of TrackingTaskViewModel which uses LogTypes specific to the Symptoms task.
 */
public class SymptomTrackingTaskViewModel extends TrackingTaskViewModel<SimpleTrackingItemConfig, SymptomLog> {

    protected SymptomTrackingTaskViewModel(
            @NonNull final TrackingStepView stepView,
            @Nullable final LoggingCollection<SymptomLog> previousLoggingCollection) {
        super(stepView, previousLoggingCollection);
    }

    @Override
    protected SymptomLog instantiateLogForUnloggedItem(@NonNull final SimpleTrackingItemConfig config) {
        return SymptomLog.builder()
                .setIdentifier(config.getIdentifier())
                .setText(config.getIdentifier())
                .build();
    }

    @Override
    protected LoggingCollection<SymptomLog> instantiateLoggingCollection() {
        return LoggingCollection.<SymptomLog>builder()
                .setIdentifier(TrackingTaskViewModel.LOGGING_COLLECTION_IDENTIFIER)
                .build();
    }

    @Override
    protected SimpleTrackingItemConfig instantiateConfigFromSelection(@NonNull final TrackingItem item) {
        return SimpleTrackingItemConfig.builder().setIdentifier(item.getIdentifier()).setTrackingItem(item).build();
    }

    @Override
    protected void proceedToInitialFragmentOnSecondRun(TrackingFragment trackingFragment) {
        trackingFragment.replaceWithFragment(SymptomLoggingFragment.newInstance(this.stepView));
    }
}
