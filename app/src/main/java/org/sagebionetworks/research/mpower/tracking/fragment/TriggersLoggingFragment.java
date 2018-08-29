package org.sagebionetworks.research.mpower.tracking.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView.Adapter;

import org.sagebionetworks.research.mobile_ui.show_step.ShowStepFragment;
import org.sagebionetworks.research.mpower.tracking.recycler_view.TriggersLoggingItemAdapter;
import org.sagebionetworks.research.mpower.tracking.view_model.SimpleTrackingTaskViewModel;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.SimpleTrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SimpleTrackingItemLog;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;

import java.util.ArrayList;
import java.util.List;

/**
 * A subclass of LoggingFragment specific to the Triggers Task.
 */
public class TriggersLoggingFragment extends LoggingFragment<SimpleTrackingItemConfig, SimpleTrackingItemLog,
        SimpleTrackingTaskViewModel> {
    @NonNull
    public static TriggersLoggingFragment newInstance(@NonNull StepView step) {
        TriggersLoggingFragment fragment = new TriggersLoggingFragment();
        Bundle args = TrackingFragment.createArguments(step);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public TrackingFragment<?, ?, ?> getNextFragment() {
        return TriggersSelectionFragment.newInstance(this.stepView);
    }

    @NonNull
    @Override
    public Adapter<?> initializeAdapter() {
        List<SimpleTrackingItemConfig> activeElements = new ArrayList<>(this.viewModel.getActiveElements().getValue());
        return new TriggersLoggingItemAdapter(activeElements, this.viewModel);
    }

}
