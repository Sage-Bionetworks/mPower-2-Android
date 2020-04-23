package org.sagebionetworks.research.mpower.tracking;

import static org.sagebionetworks.research.mpower.research.MpIdentifier.MOTIVATION;
import static org.sagebionetworks.research.mpower.research.MpIdentifier.STUDY_BURST_REMINDER;
import static org.sagebionetworks.research.mpower.studyburst.StudyBurstActivityKt.STUDY_BURST_EXTRA_GUID_OF_TASK_TO_RUN;
import static org.sagebionetworks.research.mpower.studyburst.StudyBurstActivityKt.STUDY_BURST_REQUEST_CODE;
import static org.sagebionetworks.researchstack.backbone.ui.fragment.ActivitiesFragment.REQUEST_TASK;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.sagebionetworks.research.mobile_ui.show_step.view.SystemWindowHelper;
import org.sagebionetworks.research.mobile_ui.show_step.view.SystemWindowHelper.Direction;
import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.TaskLauncher;
import org.sagebionetworks.research.mpower.profile.MPowerProfileViewModel;
import org.sagebionetworks.research.mpower.profile.PassiveGaitPermissionActivity;
import org.sagebionetworks.research.mpower.profile.PassiveGaitPermissionViewModel;
import org.sagebionetworks.research.mpower.reminders.StudyBurstReminderActivity;
import org.sagebionetworks.research.mpower.researchstack.framework.MpTaskFactory;
import org.sagebionetworks.research.mpower.researchstack.framework.MpViewTaskActivity;
import org.sagebionetworks.research.mpower.researchstack.framework.step.MpSmartSurveyTask;
import org.sagebionetworks.research.mpower.studyburst.StudyBurstActivity;
import org.sagebionetworks.research.mpower.viewmodel.PassiveGaitViewModel;
import org.sagebionetworks.research.mpower.viewmodel.StudyBurstItem;
import org.sagebionetworks.research.mpower.viewmodel.StudyBurstReminderState;
import org.sagebionetworks.research.mpower.viewmodel.StudyBurstReminderViewModel;
import org.sagebionetworks.research.mpower.viewmodel.StudyBurstViewModel;
import org.sagebionetworks.research.mpower.viewmodel.SurveyViewModel;
import org.sagebionetworks.research.mpower.viewmodel.TodayActionBarItem;
import org.sagebionetworks.research.mpower.viewmodel.TodayScheduleViewModel;
import org.sagebionetworks.research.sageresearch.dao.room.AppConfigRepository;
import org.sagebionetworks.research.sageresearch.dao.room.ReportRepository;
import org.sagebionetworks.research.sageresearch.dao.room.ScheduledActivityEntity;
import org.sagebionetworks.research.sageresearch.profile.ProfileDataLoader;
import org.sagebionetworks.research.sageresearch.profile.ProfileManager;
import org.sagebionetworks.research.sageresearch.viewmodel.ReportViewModel;
import org.sagebionetworks.researchstack.backbone.factory.IntentFactory;
import org.sagebionetworks.researchstack.backbone.model.TaskModel;
import org.sagebionetworks.researchstack.backbone.result.TaskResult;
import org.sagebionetworks.researchstack.backbone.ui.ViewTaskActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(TrackingTabFragment.class);

    private static final int PASSIVE_DATA_PERMISSION_REQUEST_CODE = 31231;

    @BindView(R.id.tracking_status_bar)
    TrackingStatusBar trackingStatusBar;

    @Inject
    TodayScheduleViewModel.Factory todayScheduleViewModelFactory;

    @Inject
    StudyBurstViewModel.Factory studyBurstViewModelFactory;

    @Inject
    SurveyViewModel.Factory surveyViewModelFactory;

    @Inject
    ReportViewModel.Factory reportViewModelFactory;

    @Inject
    StudyBurstReminderViewModel.Factory studyBurstReminderViewModelFactory;


    private MPowerProfileViewModel mPowerProfileViewModel = null;

    @Inject
    MPowerProfileViewModel.Factory profileViewModelFactory;


    @Inject
    ReportRepository reportRepo;

    @Inject
    AppConfigRepository appConfigRepo;

    ProfileManager profileManager = null;

    ProfileDataLoader profileDataLoader = null;


    private TodayScheduleViewModel todayScheduleViewModel;

    private SurveyViewModel surveyViewModel;

    private StudyBurstViewModel studyBurstViewModel;

    private ReportViewModel reportViewModel;

    private StudyBurstReminderViewModel studyBurstReminderViewModel;

    private Unbinder unbinder;

    private @NonNull
    TrackingTabFragmentViewModel trackingTabViewModel;

    private CompositeSubscription compositeSubscription = new CompositeSubscription();

    public static TrackingTabFragment newInstance() {
        return new TrackingTabFragment();
    }

    private PassiveGaitViewModel passiveGaitViewModel;


    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPowerProfileViewModel = new ViewModelProvider(this, profileViewModelFactory)
                .get(MPowerProfileViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tracking_fragment, container, false);
        unbinder = ButterKnife.bind(this, view);
        // Move the status bar down by the window insets.
        OnApplyWindowInsetsListener listener = SystemWindowHelper.getOnApplyWindowInsetsListener(Direction.TOP);
        ViewCompat.setOnApplyWindowInsetsListener(this.trackingStatusBar, listener);

        profileManager = new ProfileManager(reportRepo, appConfigRepo);
        profileManager.profileDataLoader().observe(this.getViewLifecycleOwner(), profileDataLoader1 -> {
            String passiveDataAllowedString = profileDataLoader1
                    .getValueString(PassiveGaitPermissionViewModel.PROFILE_KEY_PASSIVE_DATA_ALLOWED);
            if (passiveDataAllowedString != null) {
                trackingTabViewModel.passiveDataAllowed = null;//Boolean.parseBoolean(passiveDataAllowedString);
            }

        });

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

        todayScheduleViewModel = new ViewModelProvider(this, todayScheduleViewModelFactory)
                .get(TodayScheduleViewModel.class);
        todayScheduleViewModel.liveData().observe(getViewLifecycleOwner(), todayHistoryItems -> {
            // do something so compiler doesn't reuse lambda as observer across LiveDatas
            LOGGER.debug("Observed todayHistoryItems");
            // TODO: mdephillips 9/4/18 mimic what iOS does with the history items, see TodayViewController
        });

        trackingTabViewModel = new ViewModelProvider(getActivity()).get(TrackingTabFragmentViewModel.class);

        studyBurstViewModel = new ViewModelProvider(this, studyBurstViewModelFactory)
                .get(StudyBurstViewModel.class);
        studyBurstViewModel.liveData().observe(getViewLifecycleOwner(), this::setupActionBar);
        studyBurstViewModel.getScheduleErrorLiveData().observe(getViewLifecycleOwner(), this::showErrorMessage);
        // This is a single live event that will only be triggered once after a call to loadResearchStackSurvey
        studyBurstViewModel.getLoadRsSurveyLiveData().observe(getViewLifecycleOwner(), this::rsSurveyLoaded);

        surveyViewModel = new ViewModelProvider(this, surveyViewModelFactory)
                .get(SurveyViewModel.class);
        surveyViewModel.liveData().observe(getViewLifecycleOwner(), scheduledActivityEntities -> {
            // TODO: mdephillips 9/4/18 mimic On iOS, this runs any survey that managers may add
            // TODO: mdephillips 9/4/18 we may want to hold off on implementing it
            // do something so compiler doesn't reuse lambda as observer across LiveDatas
            LOGGER.debug("Observed scheduledActivityEntities");
            // TODO: mdephillips 9/4/18 because not all survey types are currently supported with UI right now
        });

        reportViewModel = new ViewModelProvider(this, reportViewModelFactory).get(ReportViewModel.class);

        // This view model is used to ensure that the study burst reminders are kept up to date
        // even if the user is using multiple devices, or has recently logged in/out
        studyBurstReminderViewModel = new ViewModelProvider(this,
                studyBurstReminderViewModelFactory).get(StudyBurstReminderViewModel.class);
        studyBurstReminderViewModel.reminderLiveData()
                .observe(getViewLifecycleOwner(), this::updateStudyBurstReminders);

        passiveGaitViewModel = new ViewModelProvider(getActivity()).get(PassiveGaitViewModel.class);
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
        if (!trackingTabViewModel.hasShownStudyBurst &&
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
     * Shows the next screen when the action bar is tapped, or when the user has not done their motivation survey
     * yet.
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
     *
     * @param surveySchedule
     *         of the survey to launch
     */
    private void launchRsSurvey(@Nullable ScheduledActivityEntity surveySchedule) {
        if (surveySchedule == null) {
            return; // NPE guard statement
        }

        LOGGER.info("Launching rs survey " + surveySchedule.activityIdentifier());

        // The study burst reminder is a special case survey that isn't an RS survey,
        // but the task result is uploaded as one, so it needs special case logic here
        if (STUDY_BURST_REMINDER.equals(surveySchedule.activityIdentifier())) {
            trackingTabViewModel.hasShownStudyBurst = true;
            trackingTabViewModel.currentSurveySchedule = surveySchedule;
            runStudyBurstReminder();
            return;
        }

        if (surveySchedule.getActivity() == null ||
                surveySchedule.getActivity().getSurvey() == null) {
            return; // More NPE guard statements
        }

        trackingTabViewModel.hasShownStudyBurst = true;
        trackingStatusBar.setEnabled(false);
        trackingStatusBar.setProgressBarVisibility(View.VISIBLE);
        trackingTabViewModel.currentSurveySchedule = surveySchedule;
        // This triggers a SingleLiveEvent which only posts a success once
        studyBurstViewModel.loadRsSurvey(surveySchedule);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LOGGER.info("onActivityResult with requestCode " + requestCode + " resultCode " + resultCode);

        // Will be set if a survey was just successfully completed and uploaded
        String successfulSurveyUploadTaskId = null;
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_TASK) {
            TaskResult taskResult = (TaskResult)
                    data.getSerializableExtra(ViewTaskActivity.EXTRA_TASK_RESULT);

            LOGGER.info("Task was successfully finished");

            if (trackingTabViewModel.currentSurveyTask != null) {
                // This will trigger any after-rule processing like adding data groups based on survey answers
                trackingTabViewModel.currentSurveyTask.processTaskResult(taskResult);
            }

            if (trackingTabViewModel.currentSurveySchedule != null) {
                LOGGER.info("currentSurveySchedule non-null, uploading results");

                surveyViewModel.getBridgeRepoManager()
                        .saveTaskResult(taskResult, trackingTabViewModel.currentSurveySchedule);

                if (MOTIVATION.equals(trackingTabViewModel.currentSurveySchedule.activityIdentifier())) {
                    // send the user straight into the study burst
                    goToStudyBurst();
                }
                successfulSurveyUploadTaskId = trackingTabViewModel.currentSurveySchedule.activityIdentifier();


            } else {
                LOGGER.info("currentSurveySchedule is null, cannot upload results");
            }
        } else if (resultCode == Activity.RESULT_OK && requestCode == PASSIVE_DATA_PERMISSION_REQUEST_CODE) {
            //CHECK here if it's the passive data permission activity result
            //mPowerProfileViewModel.currentSurveyTask?.processTaskResult(taskResult)
            TaskResult taskResult = (TaskResult) data.getSerializableExtra(ViewTaskActivity.EXTRA_TASK_RESULT);
            if (taskResult != null) {
                surveyViewModel.getBridgeRepoManager()
                        .saveTaskResult(taskResult, mPowerProfileViewModel.getCurrentScheduledActivity());
            }

        }

        trackingTabViewModel.currentSurveyTask = null;
        trackingTabViewModel.currentSurveySchedule = null;

        // Check this at the end because it may set currentSurveyTask and currentSurveySchedule
        if (resultCode == Activity.RESULT_OK && requestCode == STUDY_BURST_REQUEST_CODE) {
            ScheduledActivityEntity scheduleToRun = (ScheduledActivityEntity)
                    data.getSerializableExtra(STUDY_BURST_EXTRA_GUID_OF_TASK_TO_RUN);
            if (scheduleToRun != null) {
                LOGGER.info("Study burst finished with new activity to run " + scheduleToRun.activityIdentifier());
                launchRsSurvey(scheduleToRun);
            }
        }

        // Check this at the end because it may set currentSurveyTask and currentSurveySchedule
        // Per logic of iOS flow, demographics survey should be run after a successful study burst reminder
        if (STUDY_BURST_REMINDER.equals(successfulSurveyUploadTaskId)) {
            LOGGER.info("Study burst reminder task finished attempting to run demographics survey");
            StudyBurstItem currentItem = studyBurstViewModel.liveData().getValue();
            if (currentItem != null && currentItem.getDemographicsSurvey() != null) {
                launchRsSurvey(currentItem.getDemographicsSurvey());
            }
        }

        //special case for Walk and Balance to ask for permission
        if (resultCode == Activity.RESULT_OK && requestCode == TaskLauncher.WALK_AND_BALANCE_REQUEST_CODE && trackingTabViewModel.passiveDataAllowed == null) {
            Intent intent = new Intent(this.getContext(), PassiveGaitPermissionActivity.class);
            intent.putExtra(PassiveGaitPermissionActivity.ARG_PASSIVE_DATA_ALLOWED_VALUE, (String) null);

            this.startActivityForResult(intent, PASSIVE_DATA_PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * Called when the survey model is loaded from bridge, time to show it to the user
     *
     * @param task
     *         old research stack model object for creating surveys
     */
    private void rsSurveyLoaded(TaskModel task) {
        trackingStatusBar.setEnabled(true);
        trackingStatusBar.setProgressBarVisibility(View.GONE);
        if (task != null && getActivity() != null) {
            MpTaskFactory factory = new MpTaskFactory();
            trackingTabViewModel.currentSurveyTask = factory.createMpSmartSurveyTask(getActivity(), task);
            startActivityForResultParent(IntentFactory.INSTANCE.newTaskIntent(getActivity(),
                    MpViewTaskActivity.class, trackingTabViewModel.currentSurveyTask), REQUEST_TASK);
        }
    }

    /**
     * Called when there is a network or database error
     *
     * @param errorMessage
     *         to display to the user
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
        startActivityForResultParent(new Intent(getActivity(), StudyBurstReminderActivity.class), REQUEST_TASK);
    }

    /**
     * Transitions to the study burst screen
     */
    private void goToStudyBurst() {
        startActivityForResultParent(new Intent(getActivity(), StudyBurstActivity.class), STUDY_BURST_REQUEST_CODE);
    }

    /**
     * Due to a behavior issue in nested child fragments (like this fragment) We must call the startActivityForResult
     * on the parent fragment if we can
     *
     * @param intent
     *         to launch
     * @param requestCode
     *         to launch with
     */
    private void startActivityForResultParent(Intent intent, int requestCode) {
        passiveGaitViewModel.disableTracking();

        Fragment parent = this;
        while (parent.getParentFragment() != null) {
            parent = parent.getParentFragment();
        }
        LOGGER.info("Starting activity for result with parent fragment " + parent.getId());
        parent.startActivityForResult(intent, requestCode);
    }

    private void updateStudyBurstReminders(@Nullable StudyBurstReminderState reminderState) {
        if (reminderState == null || getActivity() == null) {
            return;
        }
        studyBurstReminderViewModel.updateRemindersOnDevice(getActivity(), reminderState);
    }

    /**
     * TrackingMenuFragmentViewModel contains data that should persist across the fragment life cycle state changes
     */
    public static class TrackingTabFragmentViewModel extends ViewModel {
        // Note: state is not stored so killing the app and restarting will redisplay the finished schedules
        // on the first day.
        boolean hasShownStudyBurst = false;

        /**
         * The current survey task being run, null if no survey is running
         */
        @Nullable
        MpSmartSurveyTask currentSurveyTask;

        /**
         * The current survey schedule for current survey task being run, null if no survey is running
         */
        @Nullable
        ScheduledActivityEntity currentSurveySchedule;

        @Nullable
        Boolean passiveDataAllowed;
    }
}

