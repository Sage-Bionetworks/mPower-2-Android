package org.sagebionetworks.research.mpower.tracking.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.sagebionetworks.research.domain.step.ui.action.Action;
import org.sagebionetworks.research.mobile_ui.widget.ActionButton;
import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.TrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.TrackingItemLog;
import org.sagebionetworks.research.mpower.tracking.view_model.TrackingTaskViewModel;
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
        Map<String, Action> actions = this.stepView.getLoggingInfo().getActions();
        if (actions != null && !actions.isEmpty()) {
            Action addMore = actions.get(ActionType.ADD_MORE);
            if (this.addMore != null) {
                this.addMore.setText(addMore.getButtonTitle());
            }
        }

        this.title.setText(this.stepView.getLoggingInfo().getTitle());
        this.detail.setText(this.stepView.getLoggingInfo().getDetail());
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
        performTaskFragment.goForward();
    }

    /**
     * Sub-classes can override to provide custom submit button setup.
     */
    protected void setupSubmitButton() {
        setSubmitButtonEnabled(false);
        navigationActionBar.getForwardButton().setText(R.string.button_submit);
        navigationActionBar.getForwardButton().setOnClickListener(this::onSubmitButtonClicked);
    }

    /**
     * @param enabled when true, submit button will be enabled, when false it will be disabled.
     */
    protected void setSubmitButtonEnabled(boolean enabled) {
        navigationActionBar.setForwardButtonEnabled(enabled);
    }

    @Override
    @Nullable
    public ItemDecoration initializeItemDecoration() {
        DividerItemDecoration itemDecoration = new DividerItemDecoration(this.getContext(), DividerItemDecoration.VERTICAL);
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
