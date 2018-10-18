package org.sagebionetworks.research.mpower.tracking;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.sagebionetworks.research.mobile_ui.show_step.view.SystemWindowHelper;
import org.sagebionetworks.research.mobile_ui.show_step.view.SystemWindowHelper.Direction;
import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.studyburst.StudyBurstActivity;
import org.sagebionetworks.research.mpower.viewmodel.StudyBurstItem;
import org.sagebionetworks.research.mpower.viewmodel.StudyBurstViewModel;
import org.sagebionetworks.research.mpower.viewmodel.SurveyViewModel;
import org.sagebionetworks.research.mpower.viewmodel.TodayActionBarItem;
import org.sagebionetworks.research.mpower.viewmodel.TodayScheduleViewModel;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;

/**
 * This Fragment follows the dumb/passive view pattern. Its sole responsibility is to render the UI.
 * <p>
 * https://martinfowler.com/eaaDev/PassiveScreen.html
 * <p>
 * https://medium.com/@rohitsingh14101992/lets-keep-activity-dumb-using-livedata-53468ed0dc1f
 */
public class TrackingTabFragment extends Fragment {
    @BindView(R.id.tracking_status_bar)
    TrackingStatusBar trackingStatusBar;

    @Inject
    TrackingViewModelFactory trackingViewModelFactory;

    @Inject
    TodayScheduleViewModel.Factory todayScheduleViewModelFactory;

    @Inject
    StudyBurstViewModel.Factory studyBurstViewModelFactory;

    @Inject
    SurveyViewModel.Factory surveyViewModelFactory;

    private TrackingViewModel trackingViewModel;

    private TodayScheduleViewModel todayScheduleViewModel;

    private SurveyViewModel surveyViewModel;

    private StudyBurstViewModel studyBurstViewModel;

    private Unbinder unbinder;

    public static TrackingTabFragment newInstance() {
        return new TrackingTabFragment();
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

        this.trackingStatusBar.setOnClickListener((View v) -> {
            startActivity(new Intent(getActivity(), StudyBurstActivity.class));
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ViewCompat.requestApplyInsets(view);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        trackingViewModel = ViewModelProviders.of(this, trackingViewModelFactory.create())
                .get(TrackingViewModel.class);

        todayScheduleViewModel = ViewModelProviders.of(this, todayScheduleViewModelFactory)
                .get(TodayScheduleViewModel.class);
        todayScheduleViewModel.liveData().observe(this, todayHistoryItems -> {
            // TODO: mdephillips 9/4/18 mimic what iOS does with the history items, see TodayViewController
        });

        studyBurstViewModel = ViewModelProviders.of(this, studyBurstViewModelFactory)
                .get(StudyBurstViewModel.class);
        studyBurstViewModel.liveData().observe(this, this::setupActionBar);

        surveyViewModel = ViewModelProviders.of(this, surveyViewModelFactory)
                .get(SurveyViewModel.class);
        surveyViewModel.liveData().observe(this, scheduledActivityEntities -> {
            // TODO: mdephillips 9/4/18 mimic what iOS does with these
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    /**
     * Sets up the action bar according to the current state of the study burst
     *
     * @param item
     *         most recent item from the StudyBurstViewModel
     */
    private void setupActionBar(@Nullable StudyBurstItem item) {
        if (item == null) {
            return;
        }
        if (!item.getHasStudyBurst() || item.getDayCount() == null) {
            trackingStatusBar.setVisibility(View.GONE);
            return;
        }
        trackingStatusBar.setVisibility(View.VISIBLE);
        trackingStatusBar.setDayCount(item.getDayCount());
        trackingStatusBar.setMax(100);
        trackingStatusBar.setProgress(Math.round(100 * item.getProgress()));

        if (getContext() == null) {
            return;
        }
        TodayActionBarItem actionBarItem = item.getActionBarItem(getContext());
        if (actionBarItem != null) {
            trackingStatusBar.setTitle(actionBarItem.getTitle());
            trackingStatusBar.setText(actionBarItem.getDetail());
        } else {
            trackingStatusBar.setTitle(null);
            trackingStatusBar.setText(null);
        }
    }
}