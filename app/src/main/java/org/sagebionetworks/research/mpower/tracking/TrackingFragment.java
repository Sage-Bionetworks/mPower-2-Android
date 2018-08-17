package org.sagebionetworks.research.mpower.tracking;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.Fragment;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.common.base.Strings;

import org.sagebionetworks.research.mobile_ui.show_step.view.SystemWindowHelper;
import org.sagebionetworks.research.mobile_ui.show_step.view.SystemWindowHelper.Direction;
import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.tracking.TrackingViewModel.ScheduledActivityView;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;

/**
 * This Fragment follows the dumb/passive view pattern. Its sole responsibility is to render the UI.
 * <p>
 * https://martinfowler.com/eaaDev/PassiveScreen.html
 * <p>
 * https://medium.com/@rohitsingh14101992/lets-keep-activity-dumb-using-livedata-53468ed0dc1f
 */
public class TrackingFragment extends Fragment {
    @BindView(R.id.textview_error_message)
    TextView errorMessageTextView;

    @BindView(R.id.tracking_status_bar)
    TrackingStatusBar trackingStatusBar;

    @BindView(R.id.togglebutton_loading)
    ToggleButton scheduledActivitiesLoadingToggleButton;

    @BindView(R.id.textview_scheduled_activities)
    TextView scheduledActivitiesTextView;

    @Inject
    TrackingViewModelFactory trackingViewModelFactory;

    private TrackingViewModel trackingViewModel;

    private Unbinder unbinder;

    public static TrackingFragment newInstance() {
        return new TrackingFragment();
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tracking_fragment, container, false);
        unbinder = ButterKnife.bind(this, view);
        // Move the status bar down by the window insets.
        OnApplyWindowInsetsListener listener = SystemWindowHelper.getOnApplyWindowInsetsListener(Direction.TOP);
        ViewCompat.setOnApplyWindowInsetsListener(this.trackingStatusBar, listener);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        trackingViewModel = ViewModelProviders.of(
                this,
                trackingViewModelFactory.create()).get(TrackingViewModel.class);

        trackingViewModel.getScheduledActivitiesLiveData()
                .observe(this, this::updateScheduledActivities);
        trackingViewModel.getScheduledActivitiesLoadingErrorMessageLiveData()
                .observe(this, this::updateScheduledActivitiesErrorMessage);
        trackingViewModel.getScheduledActivitiesLoadingLiveData()
                .observe(this, this::updateScheduledActivitiesLoading);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.button_reload)
    void onReloadClicked() {
        trackingViewModel.reload();
    }

    // these methods marked with @VisibleForTesting would be the ones to UI test. Since this is a dumb/passive view,
    // the only work it should be doing is subscribing to POJOs and rendering them.
    @VisibleForTesting
    void updateScheduledActivities(List<ScheduledActivityView> scheduledActivityViews) {
        StringBuilder sb = new StringBuilder();
        for (ScheduledActivityView v : scheduledActivityViews) {
            sb.append(v.scheduledActivityGuid + " ");
        }
    }

    @VisibleForTesting
    void updateScheduledActivitiesErrorMessage(String errorMessage) {
        if (Strings.isNullOrEmpty(errorMessage)) {
            errorMessageTextView.setVisibility(View.GONE);
        } else {
            errorMessageTextView.setVisibility(View.VISIBLE);
            errorMessageTextView.setText(errorMessage);
        }
    }

    @VisibleForTesting
    void updateScheduledActivitiesLoading(Boolean isLoading) {
        scheduledActivitiesLoadingToggleButton.setChecked(isLoading);
    }
}