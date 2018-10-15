package org.sagebionetworks.research.mpower.tracking;

import static org.researchstack.backbone.ui.fragment.ActivitiesFragment.REQUEST_TASK;

import android.app.Activity;
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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.joda.time.DateTime;
import org.researchstack.backbone.DataProvider;
import org.researchstack.backbone.factory.IntentFactory;
import org.researchstack.backbone.result.TaskResult;
import org.researchstack.backbone.ui.ViewTaskActivity;
import org.sagebionetworks.bridge.researchstack.BridgeDataProvider;
import org.sagebionetworks.bridge.researchstack.survey.SurveyTaskScheduleModel;
import org.sagebionetworks.bridge.rest.model.ScheduledActivity;
import org.sagebionetworks.research.mobile_ui.show_step.view.SystemWindowHelper;
import org.sagebionetworks.research.mobile_ui.show_step.view.SystemWindowHelper.Direction;
import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.TaskLauncher;
import org.sagebionetworks.research.mpower.researchstack.framework.MpDataProvider;
import org.sagebionetworks.research.mpower.researchstack.framework.MpViewTaskActivity;
import org.sagebionetworks.research.mpower.studyburst.StudyBurstActivity;
import org.sagebionetworks.research.mpower.viewmodel.StudyBurstItem;
import org.sagebionetworks.research.mpower.viewmodel.StudyBurstViewModel;
import org.sagebionetworks.research.mpower.viewmodel.SurveyViewModel;
import org.sagebionetworks.research.mpower.viewmodel.TodayActionBarItem;
import org.sagebionetworks.research.mpower.viewmodel.TodayScheduleViewModel;
import org.sagebionetworks.research.sageresearch.dao.room.EntityTypeConverters;
import org.sagebionetworks.research.sageresearch.dao.room.ScheduledActivityEntity;
import org.threeten.bp.Instant;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

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

    private TodayScheduleViewModel todayScheduleViewModel;
    private SurveyViewModel surveyViewModel;
    private StudyBurstViewModel studyBurstViewModel;

    private Unbinder unbinder;

    // Note: state is not stored so killing the app and restarting will redisplay the finished schedules
    // on the first day.
    private boolean hasShownStudyBurst = false;

    private @Nullable ScheduledActivityEntity surveyLaunched;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

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
    public void onStart() {
        super.onStart();
        ViewCompat.requestApplyInsets(this.getView());
    }

    @Override
    public void onStop() {
        super.onStop();
        compositeSubscription.unsubscribe();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            todayScheduleViewModel = TodayScheduleViewModel.create(getActivity());
            todayScheduleViewModel.liveData().observe(this, todayHistoryItems -> {
                // TODO: mdephillips 9/4/18 mimic what iOS does with the history items, see TodayViewController
            });
            surveyViewModel = SurveyViewModel.create(getActivity());
            surveyViewModel.liveData().observe(this, scheduledActivityEntities -> {
                // TODO: mdephillips 9/4/18 mimic what iOS does with these
            });
            studyBurstViewModel = StudyBurstViewModel.create(getActivity());
            studyBurstViewModel.liveData().observe(this, this::setupActionBar);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    /**
     * Sets up the action bar according to the current state of the study burst
     * @param item most recent item from the StudyBurstViewModel
     */
    private void setupActionBar(@Nullable StudyBurstItem item) {
        if (item == null) {
            return;
        }
        if (!hasShownStudyBurst && !item.hasCompletedMotivationSurvey()) {
            showActionBarFlow(false, item);
        }
        if (!hasShownStudyBurst && item.getBackgroundSurvey() != null) {
            launchSurvey(item.getBackgroundSurvey());
        }
        if (!item.getHasStudyBurst() || item.getDayCount() == null) {
            trackingStatusBar.setVisibility(View.GONE);
            return;
        }
        trackingStatusBar.setVisibility(View.VISIBLE);
        trackingStatusBar.setDayCount(item.getDayCount());
        trackingStatusBar.setMax(100);
        trackingStatusBar.setProgress(Math.round(100 * item.getProgress()));

        if (getContext() == null) return;
        TodayActionBarItem actionBarItem = item.getActionBarItem(getContext());
        if (actionBarItem != null) {
            trackingStatusBar.setTitle(actionBarItem.getTitle());
            trackingStatusBar.setText(actionBarItem.getDetail());
        } else {
            trackingStatusBar.setTitle(null);
            trackingStatusBar.setText(null);
        }
    }

    /**
     * Shows the next screen when the action bar is tapped,
     * or when the user has not done their motivation survey yet.
     * @param animated true there will be a slide animation, false there will be none
     */
    private void showActionBarFlow(boolean animated, @Nonnull StudyBurstItem item) {
        if (!item.hasCompletedMotivationSurvey() && getActivity() != null) {
            launchSurvey(item.getMotivationSurvey());
        }
    }

    /**
     * Launches an old style ResearchStack SmartSurveyTask
     * @param surveySchedule of the survey to launch
     */
    private void launchSurvey(@Nullable ScheduledActivityEntity surveySchedule) {
        if (surveySchedule == null ||
                surveySchedule.getActivity() == null ||
                surveySchedule.getActivity().getSurvey() == null) {
            return; // NPE guard statements
        }
        hasShownStudyBurst = true;
        SurveyTaskScheduleModel survey = new SurveyTaskScheduleModel();
        survey.surveyGuid = surveySchedule.getActivity().getSurvey().getGuid();
        // Load task attempts to load a survey task based, based on the data provider.
        compositeSubscription.add(DataProvider.getInstance().loadTask(getContext(), survey).subscribe(newTask -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (newTask != null) {
                        surveyLaunched = surveySchedule;
                        // This is a survey task.
                        startActivityForResult(IntentFactory.INSTANCE.newTaskIntent(getActivity(),
                                MpViewTaskActivity.class, newTask), REQUEST_TASK);
                    }
                });
            }
        }, throwable -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), throwable.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_TASK) {
            TaskResult taskResult = (TaskResult)
                    data.getSerializableExtra(ViewTaskActivity.EXTRA_TASK_RESULT);
            if (surveyLaunched != null) {
                surveyLaunched.setStartedOn(Instant.ofEpochMilli(taskResult.getStartDate().getTime()));
                surveyLaunched.setFinishedOn(Instant.ofEpochMilli(taskResult.getEndDate().getTime()));
                // This function updates the schedule on bridge and in the ScheduleRepository
                studyBurstViewModel.updateSchedule(surveyLaunched);

                // We only need to provide enough information in the ScheduledActivity
                // for the BridgeDataProvider to create the metadata JSON file
                ScheduledActivity bridgeSchedule = EntityTypeConverters.bridgeMetaDataSchedule(surveyLaunched);
                // We give it the schedule for the metadata, but we specify not to update the schedule on bridge
                // Because we do that ourselves through the ScheduleRepository
                // on the line above studyBurstViewModel.updateSchedule(surveyLaunched)
                BridgeDataProvider.getInstance().uploadTaskResult(
                        getActivity(), taskResult, bridgeSchedule, false);
            }
        }
        surveyLaunched = null;
    }
}