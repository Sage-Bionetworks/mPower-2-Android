package org.sagebionetworks.research.mpower.tracking.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView.Adapter;

import org.sagebionetworks.research.mobile_ui.show_step.ShowStepFragment;
import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.tracking.recycler_view.SymptomsLoggingItemAdapter;
import org.sagebionetworks.research.mpower.tracking.view_model.SimpleTrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.SymptomLog;
import org.sagebionetworks.research.mpower.tracking.view_model.SymptomTrackingTaskViewModel;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;

public class SymptomLoggingFragment extends
        LoggingFragment<SimpleTrackingItemConfig, SymptomLog, SymptomTrackingTaskViewModel> {
    @NonNull
    public static SymptomLoggingFragment newInstance(@NonNull StepView stepView) {
        SymptomLoggingFragment fragment = new SymptomLoggingFragment();
        Bundle args = RecyclerViewTrackingFragment.createArguments(stepView);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public ShowStepFragment getNextFragment() {
        return SymptomSelectionFragment.newInstance(this.stepView);
    }

    @NonNull
    @Override
    public Adapter<?> initializeAdapter() {
        return new SymptomsLoggingItemAdapter(this.viewModel);
    }


}
