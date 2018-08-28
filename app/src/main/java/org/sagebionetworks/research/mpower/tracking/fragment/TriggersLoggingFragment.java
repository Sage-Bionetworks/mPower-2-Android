package org.sagebionetworks.research.mpower.tracking.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.sagebionetworks.research.mobile_ui.show_step.ShowStepFragment;
import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.tracking.recycler_view.TriggersLoggingItemAdapter;
import org.sagebionetworks.research.mpower.tracking.view_model.SimpleTrackingTaskViewModel;
import org.sagebionetworks.research.mpower.tracking.view_model.SimpleTrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.SimpleTrackingItemLog;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;

import java.util.ArrayList;
import java.util.List;

public class TriggersLoggingFragment extends LoggingFragment<SimpleTrackingItemConfig, SimpleTrackingItemLog,
        SimpleTrackingTaskViewModel> {
    @NonNull
    public static TriggersLoggingFragment newInstance(@NonNull StepView step) {
        TriggersLoggingFragment fragment = new TriggersLoggingFragment();
        Bundle args = RecyclerViewTrackingFragment.createArguments(step);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public ShowStepFragment getNextFragment() {
        return TriggersSelectionFragment.newInstance(this.stepView);
    }

    @NonNull
    @Override
    public Adapter<?> initializeAdapter() {
        List<SimpleTrackingItemConfig> activeElements = new ArrayList<>(this.viewModel.getActiveElements().getValue());
        return new TriggersLoggingItemAdapter(activeElements, this.viewModel);
    }

}
