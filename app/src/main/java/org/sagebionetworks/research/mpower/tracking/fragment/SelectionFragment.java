package org.sagebionetworks.research.mpower.tracking.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.sagebionetworks.research.mobile_ui.show_step.ShowStepFragment;
import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.tracking.recycler_view.SelectionItemAdapter;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.TrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.TrackingItemLog;
import org.sagebionetworks.research.mpower.tracking.view_model.TrackingTaskViewModel;

/**
 * A SelectionFragment represents the screen where the user selects which Symptoms, Triggers, or Medications, apply to
 * them to narrow down the list of choices. The SelectionFragment allows a subclass to override which fragment is navigated
 * to next by overriding getNextFragment();
 * @param <ConfigType> The type of TrackingItemConfig.
 * @param <LogType> The type of TrackingItemLog.
 * @param <ViewModelType> The type of TrackingTaskViewModel.
 */
public abstract class SelectionFragment<ConfigType extends TrackingItemConfig, LogType extends TrackingItemLog,
                ViewModelType extends TrackingTaskViewModel<ConfigType, LogType>>
        extends RecyclerViewTrackingFragment<ConfigType, LogType, ViewModelType> {
    @Override
    @NonNull
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = super.onCreateView(inflater, container, savedInstanceState);
        this.title.setText(this.stepView.getSelectionInfo().getTitle());
        this.detail.setText(this.stepView.getSelectionInfo().getDetail());
        this.navigationActionBar.setActionButtonClickListener((actionButton -> {
            if (actionButton.getId() == R.id.rs2_step_navigation_action_forward) {
                ShowStepFragment nextFragment = this.getNextFragment();
                this.getFragmentManager().beginTransaction()
                        .replace(((ViewGroup)this.getView().getParent()).getId(), nextFragment)
                        .commit();
            }
        }));

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

    /**
     * Returns the Fragment that should replace this fragment when the forward button is pressed.
     * @return the Fragment that should replace this fragment when the forward button is pressed.
     */
    public abstract TrackingFragment<?, ?, ?> getNextFragment();
}
