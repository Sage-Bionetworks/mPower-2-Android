package org.sagebionetworks.research.mpower.tracking.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.sagebionetworks.research.mobile_ui.show_step.view.SystemWindowHelper;
import org.sagebionetworks.research.mobile_ui.show_step.view.SystemWindowHelper.Direction;
import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.tracking.recycler_view.TriggersLoggingItemAdapter;
import org.sagebionetworks.research.mpower.tracking.view_model.SimpleTrackingActiveTaskViewModel;
import org.sagebionetworks.research.mpower.tracking.view_model.SimpleTrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.SimpleTrackingItemLog;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;

import java.util.ArrayList;
import java.util.List;

public class TriggersLoggingFragment extends RecyclerViewTrackingFragment<SimpleTrackingItemConfig, SimpleTrackingItemLog,
        SimpleTrackingActiveTaskViewModel> {
    @NonNull
    public static TriggersLoggingFragment newInstance(@NonNull StepView step) {
        TriggersLoggingFragment fragment = new TriggersLoggingFragment();
        Bundle args = RecyclerViewTrackingFragment.createArguments(step);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Adapter<?> initializeAdapter() {
        List<SimpleTrackingItemConfig> activeElements = new ArrayList<>(this.viewModel.getActiveElements().getValue());
        return new TriggersLoggingItemAdapter(activeElements, this.viewModel);
    }

    @Override
    @NonNull
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = super.onCreateView(inflater, container, savedInstanceState);
        this.title.setText(this.stepView.getLoggingInfo().getTitle());
        this.detail.setText(this.stepView.getLoggingInfo().getDetail());
        if (this.addMore != null) {
            this.addMore.setOnClickListener((view) -> {
                TriggersSelectionFragment selectionFragment = TriggersSelectionFragment.newInstance(this.stepView);
                this.getFragmentManager().beginTransaction().replace(R.id.rs2_step_container, selectionFragment)
                        .commit();
            });
        }

        return result;
    }

    @Override
    @Nullable
    public ItemDecoration initializeItemDecoration() {
        DividerItemDecoration itemDecoration = new DividerItemDecoration(this.getContext(), DividerItemDecoration.VERTICAL);
        Drawable drawable = this.getResources().getDrawable(R.drawable.mpower2_triggers_logging_item_decoration);
        itemDecoration.setDrawable(drawable);
        return itemDecoration;
    }

    @Override
    public int getLayoutId() {
        return R.layout.mpower2_triggers_logging_step;
    }
}
