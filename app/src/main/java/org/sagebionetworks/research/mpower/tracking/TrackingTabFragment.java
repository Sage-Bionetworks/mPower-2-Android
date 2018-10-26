package org.sagebionetworks.research.mpower.tracking;

import static org.researchstack.backbone.ui.fragment.ActivitiesFragment.REQUEST_TASK;
import static org.sagebionetworks.research.mpower.research.MpIdentifier.MOTIVATION;
import static org.sagebionetworks.research.mpower.research.MpIdentifier.STUDY_BURST_REMINDER;
import static org.sagebionetworks.research.mpower.studyburst.StudyBurstActivityKt.STUDY_BURST_EXTRA_GUID_OF_TASK_TO_RUN;
import static org.sagebionetworks.research.mpower.studyburst.StudyBurstActivityKt.STUDY_BURST_REQUEST_CODE;

import android.app.Activity;

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
import android.widget.Toast;

import org.researchstack.backbone.factory.IntentFactory;
import org.researchstack.backbone.model.TaskModel;
import org.researchstack.backbone.result.TaskResult;
import org.researchstack.backbone.ui.ViewTaskActivity;

import org.sagebionetworks.research.mobile_ui.show_step.view.SystemWindowHelper;
import org.sagebionetworks.research.mobile_ui.show_step.view.SystemWindowHelper.Direction;
import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.reminders.ReminderActivity;
import org.sagebionetworks.research.mpower.researchstack.framework.MpTaskFactory;
import org.sagebionetworks.research.mpower.researchstack.framework.MpViewTaskActivity;
import org.sagebionetworks.research.mpower.researchstack.framework.step.MpSmartSurveyTask;
import org.sagebionetworks.research.mpower.studyburst.StudyBurstActivity;
import org.sagebionetworks.research.mpower.viewmodel.StudyBurstItem;
import org.sagebionetworks.research.mpower.viewmodel.StudyBurstViewModel;
import org.sagebionetworks.research.mpower.viewmodel.SurveyViewModel;
import org.sagebionetworks.research.mpower.viewmodel.TodayActionBarItem;
import org.sagebionetworks.research.mpower.viewmodel.TodayScheduleViewModel;
import org.sagebionetworks.research.sageresearch.dao.room.ScheduledActivityEntity;
import org.threeten.bp.Instant;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;
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

    @Inject
    TodayScheduleViewModel.Factory todayScheduleViewModelFactory;

    @Inject
    StudyBurstViewModel.Factory studyBurstViewModelFactory;

    @Inject
    SurveyViewModel.Factory surveyViewModelFactory;

    private TodayScheduleViewModel todayScheduleViewModel;

    private SurveyViewModel surveyViewModel;

    private StudyBurstViewModel studyBurstViewModel;

    private Unbinder unbinder;

    // Note: state is not stored so killing the app and restarting will redisplay the finished schedules
    // on the first day.
    private boolean hasShownStudyBurst = false;

    /**
     * The current survey task being run, null if no survey is running
     */
    private @Nullable MpSmartSurveyTask currentSurveyTask;
    /**
     * The current survey schedule for current survey task being run, null if no survey is running
     */
    private @Nullable ScheduledActivityEntity currentSurveySchedule;

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

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ViewCompat.requestApplyInsets(view);
    }

    @Override
    public void onStop() {
        super.onStop();
        compositeSubscription.unsubscribe();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        todayScheduleViewModel = ViewModelProviders.of(this, todayScheduleViewModelFactory)
                .get(TodayScheduleViewModel.class);
        todayScheduleViewModel.liveData().observe(this, todayHistoryItems -> {
            // TODO: mdephillips 9/4/18 mimic what iOS does with the history items, see TodayViewController
        });

        studyBurstViewModel = ViewModelProviders.of(this, studyBurstViewModelFactory)
                .get(StudyBurstViewModel.class);
        studyBurstViewModel.liveData().observe(this, this::setupActionBar);
        studyBurstViewModel.getScheduleSyncErrorMessageLiveData().observe(this, this::showErrorMessage);
        // This is a single live event that will only be triggered once after a call to loadResearchStackSurvey
        studyBurstViewModel.getLoadRsSurveyLiveData().observe(this, this::rsSurveyLoaded);

        surveyViewModel = ViewModelProviders.of(this, surveyViewModelFactory)
                .get(SurveyViewModel.class);
        surveyViewModel.liveData().observe(this, scheduledActivityEntities -> {
            // TODO: mdephillips 9/4/18 mimic On iOS, this runs any survey that managers may add
            // TODO: mdephillips 9/4/18 we may want to hold off on implementing it
            // TODO: mdephillips 9/4/18 because not all survey types are currently supported with UI right now
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
    private void setupActionBar(final @Nullable StudyBurstItem item) {
        if (item == null) {
            return;
        }
        // DEBUGGING HELP:
        // To immediately run a survey you can uncomment this. You can also change to any survey
        // For example change both instances of getBackgroundSurvey() to getDemographicsSurvey()
//        if (!hasShownStudyBurst && item.getBackgroundSurvey() != null) {
//            launchRsSurvey(item.getBackgroundSurvey());
//        }
        if (!hasShownStudyBurst &&
                !item.hasCompletedMotivationSurvey() &&
                item.getMotivationSurvey() != null) {
            showActionBarFlow(item);
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
            trackingStatusBar.setEnabled(true);
            trackingStatusBar.setOnClickListener(view -> showActionBarFlow(item));
            trackingStatusBar.setTitleTextBackgroundVisibility(View.VISIBLE);
            trackingStatusBar.setTitle(actionBarItem.getTitle());
            trackingStatusBar.setText(actionBarItem.getDetail());
        } else {
            trackingStatusBar.setEnabled(false);
            trackingStatusBar.setOnClickListener(null);
            trackingStatusBar.setTitleTextBackgroundVisibility(View.INVISIBLE);
            trackingStatusBar.setTitle(null);
            trackingStatusBar.setText(null);
        }
    }

    /**
     * Shows the next screen when the action bar is tapped,
     * or when the user has not done their motivation survey yet.
     */
    private void showActionBarFlow(@Nonnull StudyBurstItem item) {
        ScheduledActivityEntity nextCompletionTask = item.getNextCompletionActivityToShow();
        if (!item.hasCompletedMotivationSurvey() && item.getMotivationSurvey() != null) {
            launchRsSurvey(item.getMotivationSurvey());
        } else if (nextCompletionTask != null) {
            launchRsSurvey(nextCompletionTask);
        } else {
            goToStudyBurst();
        }
    }

    /**
     * Launches an old style ResearchStack SmartSurveyTask
     * @param surveySchedule of the survey to launch
     */
    private void launchRsSurvey(@Nullable ScheduledActivityEntity surveySchedule) {
        if (surveySchedule == null) {
            return; // NPE guard statement
        }

        // The study burst reminder is a special case survey that isn't an RS survey,
        // but the task result is uploaded as one, so it needs special case logic here
        if (STUDY_BURST_REMINDER.equals(surveySchedule.activityIdentifier())) {
            hasShownStudyBurst = true;
            currentSurveySchedule = surveySchedule;
            runStudyBurstReminder();
            return;
        }

        if (surveySchedule.getActivity() == null ||
                surveySchedule.getActivity().getSurvey() == null) {
            return; // More NPE guard statements
        }

        hasShownStudyBurst = true;
        trackingStatusBar.setEnabled(false);
        trackingStatusBar.setProgressBarVisibility(View.VISIBLE);
        currentSurveySchedule = surveySchedule;
        // This triggers a SingleLiveEvent which only posts a success once
        studyBurstViewModel.loadRsSurvey(surveySchedule);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Will be set if a survey was just successfully completed and uploaded
        String successfulSurveyUploadTaskId = null;
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_TASK) {
            TaskResult taskResult = (TaskResult)
                    data.getSerializableExtra(ViewTaskActivity.EXTRA_TASK_RESULT);

            if (currentSurveyTask != null) {
                // This will trigger any after-rule processing like adding data groups based on survey answers
                currentSurveyTask.processTaskResult(taskResult);
            }
            if (currentSurveySchedule != null) {
                currentSurveySchedule.setStartedOn(Instant.ofEpochMilli(taskResult.getStartDate().getTime()));
                currentSurveySchedule.setFinishedOn(Instant.ofEpochMilli(taskResult.getEndDate().getTime()));
                // This function updates the schedule on bridge and in the ScheduleRepository
                studyBurstViewModel.updateScheduleToBridge(currentSurveySchedule);
                // This function uploads the result of the task to S3
                studyBurstViewModel.uploadResearchStackTaskResultToS3(currentSurveySchedule, taskResult);

                if (MOTIVATION.equals(currentSurveySchedule.activityIdentifier())) {
                    // send the user straight into the study burst
                    goToStudyBurst();
                }
                successfulSurveyUploadTaskId = currentSurveySchedule.activityIdentifier();
            }
        }
        currentSurveyTask = null;
        currentSurveySchedule = null;

        // Check this at the end because it may set currentSurveyTask and currentSurveySchedule
        if (resultCode == Activity.RESULT_OK && requestCode == STUDY_BURST_REQUEST_CODE) {
            ScheduledActivityEntity scheduleToRun = (ScheduledActivityEntity)
                    data.getSerializableExtra(STUDY_BURST_EXTRA_GUID_OF_TASK_TO_RUN);
            if (scheduleToRun != null) {
                launchRsSurvey(scheduleToRun);
            }
        }

        // Check this at the end because it may set currentSurveyTask and currentSurveySchedule
        // Per logic of iOS flow, demographics survey should be run after a successful study burst reminder
        if (STUDY_BURST_REMINDER.equals(successfulSurveyUploadTaskId)) {
            StudyBurstItem currentItem = studyBurstViewModel.liveData().getValue();
            if (currentItem != null && currentItem.getDemographicsSurvey() != null) {
                launchRsSurvey(currentItem.getDemographicsSurvey());
            }
        }
    }

    /**
     * Called when the survey model is loaded from bridge, time to show it to the user
     * @param task old research stack model object for creating surveys
     */
    private void rsSurveyLoaded(TaskModel task) {
        trackingStatusBar.setEnabled(true);
        trackingStatusBar.setProgressBarVisibility(View.GONE);
        if (task != null && getActivity() != null) {
            MpTaskFactory factory = new MpTaskFactory();
            currentSurveyTask = factory.createMpSmartSurveyTask(getActivity(), task);
            startActivityForResult(IntentFactory.INSTANCE.newTaskIntent(getActivity(),
                    MpViewTaskActivity.class, currentSurveyTask), REQUEST_TASK);
        }
    }

    /**
     * Called when there is a network or database error
     * @param errorMessage to display to the user
     */
    private void showErrorMessage(@Nullable String errorMessage) {
        if (errorMessage == null) {
            return;
        }
        trackingStatusBar.setEnabled(true);
        trackingStatusBar.setProgressBarVisibility(View.GONE);
        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
    }

    private void runStudyBurstReminder() {
        startActivityForResult(new Intent(getActivity(), ReminderActivity.class), REQUEST_TASK);
    }

    /**
     * Transitions to the study burst screen
     */
    private void goToStudyBurst() {
        startActivityForResult(new Intent(getActivity(), StudyBurstActivity.class), STUDY_BURST_REQUEST_CODE);
    }
}