package org.sagebionetworks.research.mpower.tracking.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;

import org.sagebionetworks.research.mobile_ui.show_step.ShowStepFragment;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.SimpleTrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SymptomLog;
import org.sagebionetworks.research.mpower.tracking.view_model.SymptomTrackingTaskViewModel;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;

public class SymptomSelectionFragment extends SelectionFragment<SimpleTrackingItemConfig, SymptomLog, SymptomTrackingTaskViewModel> {
    @NonNull
    public static SelectionFragment newInstance(@NonNull StepView step) {
        SymptomSelectionFragment fragment = new SymptomSelectionFragment();
        Bundle args = TrackingFragment.createArguments(step);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public ShowStepFragment getNextFragment() {
        return SymptomLoggingFragment.newInstance(this.stepView);
    }
}
