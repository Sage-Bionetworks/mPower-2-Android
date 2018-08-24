package org.sagebionetworks.research.mpower.tracking.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.sagebionetworks.research.mobile_ui.widget.ActionButton;
import org.sagebionetworks.research.mobile_ui.widget.NavigationActionBar.ActionButtonClickListener;
import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.tracking.recycler_view.SelectionItemAdapter;
import org.sagebionetworks.research.mpower.tracking.view_model.SimpleTrackingActiveTaskViewModel;
import org.sagebionetworks.research.mpower.tracking.view_model.SimpleTrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.SimpleTrackingItemLog;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;

public class TriggersSelectionFragment extends RecyclerViewTrackingFragment<SimpleTrackingItemConfig, SimpleTrackingItemLog,
        SimpleTrackingActiveTaskViewModel> {
    @NonNull
    public static TriggersSelectionFragment newInstance(@NonNull StepView step) {
        TriggersSelectionFragment fragment = new TriggersSelectionFragment();
        Bundle args = RecyclerViewTrackingFragment.createArguments(step);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @NonNull
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = super.onCreateView(inflater, container, savedInstanceState);
        this.title.setText(this.stepView.getSelectionInfo().getTitle());
        this.detail.setText(this.stepView.getSelectionInfo().getDetail());
        this.navigationActionBar.setActionButtonClickListener((actionButton -> {
            if (actionButton.getId() == R.id.rs2_step_navigation_action_forward) {
                TriggersLoggingFragment loggingFragment = TriggersLoggingFragment.newInstance(this.stepView);
                this.getFragmentManager().beginTransaction().replace(R.id.rs2_step_container, loggingFragment).commit();
            }
        }));

        this.navigationActionBar.setEnabled(false);
        this.viewModel.getSelectionMade().observe(this, (selectionMade) -> {
            if (selectionMade != null && selectionMade) {
                this.navigationActionBar.setEnabled(true);
            } else {
                this.navigationActionBar.setEnabled(false);
            }
        });
        return result;
    }

    @Override
    @NonNull
    public Adapter<?> initializeAdapter() {
        return new SelectionItemAdapter(this.viewModel.getAvailableElements().getValue(), this.viewModel);
    }

    @Override
    public int getLayoutId() {
        return R.layout.mpower2_selection_step;
    }

}
