package org.sagebionetworks.research.mpower.logging;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.common.base.Strings;

import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.logging.LoggingViewModel.ScheduledActivityView;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;

public class LoggingFragment extends Fragment {
    @BindView(R.id.textview_error_message)
    TextView errorMessageTextView;

    @Inject
    LoggingViewModelFactory loggingViewModelFactory;

    @BindView(R.id.togglebutton_loading)
    ToggleButton scheduledActivitiesLoadingToggleButton;

    @BindView(R.id.textview_scheduled_activities)
    TextView scheduledActivitiesTextView;

    private LoggingViewModel loggingViewModel;

    private Unbinder unbinder;

    public static LoggingFragment newInstance() {
        return new LoggingFragment();
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.logging_fragment, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loggingViewModel = ViewModelProviders.of(
                this,
                loggingViewModelFactory.create()).get(LoggingViewModel.class);

        loggingViewModel.getScheduledActivitiesLiveData()
                .observe(this, this::updateScheduledActivities);
        loggingViewModel.getScheduledActivitiesLoadingErrorMessageLiveData()
                .observe(this, this::updateScheduledActivitiesErrorMessage);
        loggingViewModel.getScheduledActivitiesLoadingLiveData()
                .observe(this, this::updateScheduledActivitiesLoading);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.button_reload)
    void onReloadClicked() {
        loggingViewModel.reload();
    }

    void updateScheduledActivities(List<ScheduledActivityView> scheduledActivityViews) {
        StringBuilder sb = new StringBuilder();
        for (ScheduledActivityView v : scheduledActivityViews) {
            sb.append(v.scheduledActivityGuid + " ");
        }
    }

    void updateScheduledActivitiesErrorMessage(String errorMessage) {
        if (Strings.isNullOrEmpty(errorMessage)) {
            errorMessageTextView.setVisibility(View.GONE);
        } else {
            errorMessageTextView.setVisibility(View.VISIBLE);
            errorMessageTextView.setText(errorMessage);
        }
    }

    void updateScheduledActivitiesLoading(Boolean isLoading) {
        scheduledActivitiesLoadingToggleButton.setChecked(isLoading);
    }
}