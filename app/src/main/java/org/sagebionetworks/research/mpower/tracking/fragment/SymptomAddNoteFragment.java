package org.sagebionetworks.research.mpower.tracking.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;

import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;
import org.sagebionetworks.research.mpower.tracking.view_model.SymptomTrackingTaskViewModel;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.SimpleTrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SymptomLog;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;

public class SymptomAddNoteFragment extends AddNoteFragment<SimpleTrackingItemConfig, SymptomLog, SymptomTrackingTaskViewModel> {
    @NonNull
    public static SymptomAddNoteFragment newInstance(@NonNull StepView stepView,
            @NonNull TrackingItem trackingItem) {
        SymptomAddNoteFragment addNoteFragment = new SymptomAddNoteFragment();
        Bundle args = TrackingFragment.createArguments(stepView);
        args.putParcelable(ARGUMENT_TRACKING_ITEM, trackingItem);
        addNoteFragment.setArguments(args);
        return addNoteFragment;
    }
}
