package org.sagebionetworks.research.mpower.tracking.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ItemDecoration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.sagebionetworks.research.domain.result.interfaces.Result;
import org.sagebionetworks.research.domain.step.ui.action.Action;
import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.tracking.view_model.TrackingTaskViewModel;
import org.sagebionetworks.research.mpower.tracking.model.TrackingSubstepInfo;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.TrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.TrackingItemLog;
import org.sagebionetworks.research.presentation.model.action.ActionType;

import java.util.Map;

/**
 * A LoggingFragment represents the screen where the user is asked to enter information about the specific Symptoms,
 * Triggers or Medications, that they have elected to log.
 * @param <ConfigType> The type of TrackingItemConfig.
 * @param <LogType> The type of TrackingItemLog.
 * @param <ViewModelType> The type of TrackingTaskViewModel.
 */
public abstract class LoggingFragment
        <ConfigType extends TrackingItemConfig, LogType extends TrackingItemLog,
        ViewModelType extends TrackingTaskViewModel<ConfigType, LogType>, AdapterType extends Adapter<?>>
        extends RecyclerViewTrackingFragment<ConfigType, LogType, ViewModelType, AdapterType> {

    @Override
    @NonNull
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = super.onCreateView(inflater, container, savedInstanceState);
        TrackingSubstepInfo loggingInfo = this.stepView.getLoggingInfo();
        if (loggingInfo != null) {
            Map<String, Action> actions = loggingInfo.getActions();
            if (actions != null && !actions.isEmpty()) {
                Action addMore = actions.get(ActionType.ADD_MORE);
                if (this.addMore != null) {
                    this.addMore.setText(addMore.getButtonTitle());
                }
            }

            this.title.setText(loggingInfo.getTitle());
            this.detail.setText(loggingInfo.getDetail());
        }

        if (this.addMore != null) {
            this.addMore.setOnClickListener((view) -> {
                Fragment nextFragment = this.getNextFragment();
                this.getFragmentManager().beginTransaction()
                        .replace(((ViewGroup)this.getView().getParent()).getId(), nextFragment)
                        .commit();
            });
        }

        setupSubmitButton();

        return result;
    }

    /**
     * Called when the submit button is clicked.
     * @param view that was clicked.
     */
    protected void onSubmitButtonClicked(View view) {
        Result loggingResult = viewModel.getLoggingCollection();
        performTaskViewModel.addStepResult(loggingResult);
        performTaskFragment.goForward();
    }

    /**
     * Sub-classes can override to provide custom submit button setup.
     */
    protected void setupSubmitButton() {
        if (navigationActionBar == null) {
            return;
        }
        setSubmitButtonEnabled(false);
        navigationActionBar.getForwardButton().setText(R.string.button_submit);
        navigationActionBar.getForwardButton().setOnClickListener(this::onSubmitButtonClicked);
    }

    /**
     * @param enabled when true, submit button will be enabled, when false it will be disabled.
     */
    protected void setSubmitButtonEnabled(boolean enabled) {
        if (navigationActionBar == null) {
            return;
        }
        navigationActionBar.setForwardButtonEnabled(enabled);
    }

    @Override
    @Nullable
    public ItemDecoration initializeItemDecoration() {
        if (getContext() == null) {
            return null;  // Guard NPE exceptions
        }
        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        Drawable drawable = this.getResources().getDrawable(R.drawable.mpower2_logging_item_decoration);
        itemDecoration.setDrawable(drawable);
        return itemDecoration;
    }

    @Override
    public int getLayoutId() {
        return R.layout.mpower2_logging_step;
    }

    @NonNull
    public abstract TrackingFragment<?, ?, ?> getNextFragment();
}
