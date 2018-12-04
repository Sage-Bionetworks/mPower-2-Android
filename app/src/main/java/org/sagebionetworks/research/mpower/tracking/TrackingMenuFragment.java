package org.sagebionetworks.research.mpower.tracking;

import static com.google.common.base.Preconditions.checkState;

import static org.sagebionetworks.research.mpower.research.MpIdentifier.*;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.sagebionetworks.research.domain.result.interfaces.TaskResult;
import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.TaskLauncher;
import org.sagebionetworks.research.mpower.research.MpIdentifier;
import org.sagebionetworks.research.mpower.viewmodel.StudyBurstItem;
import org.sagebionetworks.research.mpower.viewmodel.StudyBurstTaskInfo;
import org.sagebionetworks.research.mpower.viewmodel.StudyBurstViewModel;
import org.sagebionetworks.research.mpower.viewmodel.TrackingReports;
import org.sagebionetworks.research.mpower.viewmodel.TrackingScheduleViewModel;
import org.sagebionetworks.research.mpower.viewmodel.TrackingSchedules;
import org.sagebionetworks.research.sageresearch.manager.TaskInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;

public class TrackingMenuFragment extends Fragment {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrackingMenuFragment.class);

    private static final List<Integer> MEASURING_LABELS =
            Arrays.asList(R.string.measuring_walk_and_balance_label, R.string.measuring_finger_tapping_label,
                    R.string.measuring_tremor_test_label);

    private static final List<Integer> TRACKING_LABELS =
            Arrays.asList(R.string.tracking_medication_label, R.string.tracking_symptom_label, R.string.tracking_trigger_label);

    // TODO for now icons have a white background, fix this when design gets the images without the background.
    private static final List<Integer> MEASURING_ICONS =
            Arrays.asList(R.drawable.walk_and_balance_icon, R.drawable.finger_tapping_icon, R.drawable.tremor_icon);

    private static final List<Integer> TRACKING_ICONS =
            Arrays.asList(R.drawable.medication_icon, R.drawable.symptom_icon, R.drawable.trigger_icon);

    // TODO get the gesture recognizer working.
    private class FlingListener extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
            if (event1.getY() < event2.getY()) {
                // we have a down fling
                setShowingContent(false);
            } else {
                setShowingContent(true);
            }

            return true;
        }
    }


    @Inject
    TaskLauncher launcher;
    private static final int ANIMATION_DURATION = 150;
    @ColorRes
    private static final int SELECTED_COLOR = R.color.royal500;
    @ColorRes
    private static final int UNSELECTED_COLOR = R.color.royal400;
    private boolean showingContent;

    @BindView(R.id.tracking_tab)
    TextView trackingButton;

    @BindView(R.id.measuring_tab)
    TextView measuringButton;

    @BindView(R.id.tracking_selected_image_view)
    ImageView trackingSelectedImageView;

    @BindView(R.id.measuring_selected_image_view)
    ImageView measuringSelectedImageView;

    @BindView(R.id.tracking_menu_content_view)
    View contentView;

    @BindViews({R.id.centerIconLabel, R.id.leftIconLabel, R.id.rightIconLabel})
    protected List<TextView> labels;

    @BindViews({R.id.centerIconImageView, R.id.leftIconImageView, R.id.rightIconImageView})
    protected List<ImageView> imageViews;

    @BindViews({R.id.center, R.id.left, R.id.right})
    protected List<View> taskContainers;

    @BindViews({R.id.centerIconCheckmark, R.id.leftIconCheckmark, R.id.rightIconCheckmark})
    protected List<ImageView> checkMarkImageViews;

    private Unbinder unbinder;
    private GestureDetectorCompat gestureDetector;

    @Inject
    StudyBurstViewModel.Factory studyBurstViewModelFactory;
    @Nullable
    private StudyBurstViewModel studyBurstViewModel;

    @Inject
    TrackingScheduleViewModel.Factory trackingViewModelFactory;
    @Nullable
    private TrackingScheduleViewModel trackingViewModel;

    private @NonNull TrackingMenuFragmentViewModel trackingMenuViewModel;
    private boolean showTracking;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidSupportInjection.inject(this);
        showTracking = getResources().getBoolean(R.bool.show_data_tracking);
        trackingMenuViewModel =
                ViewModelProviders.of(getActivity()).get(TrackingMenuFragmentViewModel.class);
        if (!showTracking) {
            trackingMenuViewModel.setSelectedId(R.id.measuring_tab);
        }
        studyBurstViewModel = ViewModelProviders.of(getActivity(),
                studyBurstViewModelFactory).get(StudyBurstViewModel.class);
        studyBurstViewModel.liveData().observe(this, this::setupMeasuringTaskCompletionState);
        trackingViewModel = ViewModelProviders.of(getActivity(),
                trackingViewModelFactory).get(TrackingScheduleViewModel.class);
        trackingViewModel.scheduleLiveData().observe(this, trackingSchedules -> {
            // No-op needed, we use these schedules passively when the user taps a tracking task.
        });
        trackingViewModel.reportLiveData().observe(this, trackingReports -> {
            // No-op needed, we use these reports passively when the user taps a tracking task.
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup group, @Nullable Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.tracking_menu_fragment, group, false);
        this.unbinder = ButterKnife.bind(this, result);
        updateSelection(trackingMenuViewModel.getCurrentSelectedId(), true);
        this.measuringButton.setOnClickListener(view -> updateSelection(R.id.measuring_tab, false));
        if (showTracking) {
            this.trackingButton.setOnClickListener(view -> updateSelection(R.id.tracking_tab, false));
        } else {
            trackingButton.setVisibility(View.GONE);
            trackingSelectedImageView.setVisibility(View.GONE);
        }
        this.setIconListeners();
        this.gestureDetector = new GestureDetectorCompat(this.getContext(), new FlingListener());
        result.setOnTouchListener((view, event) -> {
            view.performClick();
            return this.gestureDetector.onTouchEvent(event);
        });
        return result;
    }

    private void setIconListeners() {
        for (int i = 0; i < this.taskContainers.size(); i++) {
            final int copy = i;
            taskContainers.get(i).setOnClickListener(view -> {

                if (getContext() == null) {
                    return;  // Guard NPE exceptions
                }

                int selection = 0;
                if (trackingMenuViewModel.getCurrentSelectedId() == R.id.tracking_tab) {
                    selection = TRACKING_LABELS.get(copy);
                } else if (trackingMenuViewModel.getCurrentSelectedId() == R.id.measuring_tab) {
                    selection = MEASURING_LABELS.get(copy);
                }

                String taskIdentifier = this.getTaskIdentifierFromLabel(selection);
                if (taskIdentifier != null) {

                    @Nullable String taskGuid = null;
                    if (trackingMenuViewModel.getCurrentSelectedId() == R.id.tracking_tab) {
                        taskGuid = findScheduleGuidForTrackingTask(taskIdentifier);
                    } else if (trackingMenuViewModel.getCurrentSelectedId() == R.id.measuring_tab) {
                        taskGuid = findScheduleGuidForMeasuringTask(taskIdentifier);
                    }

                    @NonNull UUID uuid = UUID.randomUUID();
                    if (studyBurstViewModel != null) {
                        uuid = studyBurstViewModel.createScheduleTaskRunUuid(taskGuid);
                    }

                    @Nullable TaskResult taskResult = null;
                    if (trackingMenuViewModel.getCurrentSelectedId() == R.id.tracking_tab) {
                        taskResult = findPreviousTaskResultForTrackingTask(taskIdentifier, uuid);
                    }

                    launcher.launchTask(getContext(), taskIdentifier, uuid, taskResult);
                } else {
                    LOGGER.warn("Selected Icon " + selection + " doesn't map to a task identifier");
                }

                if (LOGGER.isDebugEnabled()) {
                    String selectionString = this.getResources().getString(selection);
                    LOGGER.debug("Icon " + selectionString.trim() + " was selected.");
                }
            });
        }
    }

    @MpIdentifier
    @Nullable
    private String getTaskIdentifierFromLabel(@StringRes int label) {
        if (label == R.string.measuring_walk_and_balance_label) {
            return MpIdentifier.WALK_AND_BALANCE;
        } else if (label == R.string.measuring_finger_tapping_label) {
            return MpIdentifier.TAPPING;
        } else if (label == R.string.measuring_tremor_test_label) {
            return MpIdentifier.TREMOR;
        } else if (label == R.string.tracking_medication_label) {
            return MpIdentifier.MEDICATION;
        } else if (label == R.string.tracking_symptom_label) {
            return MpIdentifier.SYMPTOMS;
        } else if (label == R.string.tracking_trigger_label) {
            return MpIdentifier.TRIGGERS;
        }

        return null;
    }

    private void setIcons(List<Integer> icons, List<Integer> labels) {
        for (int i = 0; i < this.imageViews.size(); i++) {
            ImageView imageView = this.imageViews.get(i);
            TextView label = this.labels.get(i);
            imageView.setImageResource(icons.get(i));
            label.setText(labels.get(i));
        }
    }

    public void setShowingContent(boolean showingContent) {
        this.showingContent = showingContent;
        int visibility = showingContent ? View.VISIBLE : View.GONE;
        this.contentView.setVisibility(visibility);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.unbinder.unbind();
    }

    private void updateSelection(int selectionId, boolean forceUpdate) {
        if (selectionId != 0 &&
                (forceUpdate || selectionId != trackingMenuViewModel.getCurrentSelectedId())) {
            this.setShowingContent(true);
            trackingMenuViewModel.setSelectedId(selectionId);
            if (selectionId == R.id.tracking_tab) {
                this.setIcons(TRACKING_ICONS, TRACKING_LABELS);
                this.updateMenuState(true, false);
            } else if (selectionId == R.id.measuring_tab) {
                this.setIcons(MEASURING_ICONS, MEASURING_LABELS);
                this.updateMenuState(false, true);
            }
        } else {
            this.setShowingContent(false);
            trackingMenuViewModel.setSelectedId(0);
            this.updateMenuState(false, false);
        }
    }

    private void updateMenuState(boolean trackingSelected, boolean measuringSelected) {
        // It doesn't make sense for both to be selected.
        checkState(!(trackingSelected && measuringSelected));
        Resources resources = this.getResources();
        int measuringButtonColor = measuringSelected ? resources.getColor(SELECTED_COLOR) : resources.getColor(UNSELECTED_COLOR);
        float measuringImageViewAlpha = measuringSelected ? 1f : 0f;
        this.measuringButton.setTextColor(measuringButtonColor);
        this.measuringSelectedImageView.setAlpha(measuringImageViewAlpha);
        if (showTracking) {
            int trackingButtonColor = trackingSelected ? resources.getColor(SELECTED_COLOR) : resources.getColor(UNSELECTED_COLOR);
            float trackingImageViewAlpha = trackingSelected ? 1f : 0f;
            this.trackingButton.setTextColor(trackingButtonColor);
            this.trackingSelectedImageView.setAlpha(trackingImageViewAlpha);
        }
        refreshTabCompletionState();
    }

    /**
     * @param measuringTaskIdentifier of the schedule guid to find.
     * @return the guid of the measuring task schedule if one can be found, null otherwise.
     */
    private @Nullable String findScheduleGuidForMeasuringTask(@NonNull String measuringTaskIdentifier) {
        if (studyBurstViewModel == null) {
            return null; // Guard NPE exceptions
        }
        StudyBurstItem item = studyBurstViewModel.liveData().getValue();
        if (item == null) {
            LOGGER.warn("Measuring task schedule could not be found for task id " + measuringTaskIdentifier);
            return null;
        }
        for (StudyBurstTaskInfo studyBurstTaskInfo : item.getOrderedTasks()) {
            if (studyBurstTaskInfo.getSchedule() != null &&
                    measuringTaskIdentifier.equals(studyBurstTaskInfo.getTask().getIdentifier())) {
                return studyBurstTaskInfo.getSchedule().getGuid();
            }
        }
        LOGGER.warn("Measuring task schedule could not be found for task id " + measuringTaskIdentifier);
        return null;
    }

    /**
     * @param trackingTaskIdentifier of the schedule guid to find.
     * @return the guid of the tracking task schedule if one can be found, null otherwise.
     */
    private @Nullable String findScheduleGuidForTrackingTask(@NonNull String trackingTaskIdentifier) {
        if (trackingViewModel == null) {
            return null; // Guard NPE exceptions
        }
        TrackingSchedules schedules = trackingViewModel.scheduleLiveData().getValue();
        if (schedules == null) {
            LOGGER.warn("Tracking task schedule could not be found for task id " + trackingTaskIdentifier);
            return null;
        }
        switch (trackingTaskIdentifier) {
            case TRIGGERS:
                if (schedules.getTriggers() != null) {
                    return schedules.getTriggers().getGuid();
                }
                break;
            case SYMPTOMS:
                if (schedules.getSymptoms() != null) {
                    return schedules.getSymptoms().getGuid();
                }
                break;
            case MEDICATION:
                if (schedules.getMedication() != null) {
                    return schedules.getMedication().getGuid();
                }
                break;
        }
        LOGGER.warn("Tracking task schedule could not be found for task id " + trackingTaskIdentifier);
        return null;
    }

    /**
     * @param trackingTaskIdentifier of the schedule guid to find.
     * @return the guid of the tracking task schedule if one can be found, null otherwise.
     */
    private @Nullable TaskResult findPreviousTaskResultForTrackingTask(
            @NonNull String trackingTaskIdentifier, @NonNull UUID taskRunUuid) {
        if (trackingViewModel == null) {
            return null; // Guard NPE exceptions
        }
        TrackingReports reports = trackingViewModel.reportLiveData().getValue();
        if (reports == null) {
            LOGGER.warn("Tracking task report could not be found for task id " + trackingTaskIdentifier);
            return null;
        }
        switch (trackingTaskIdentifier) {
            case TRIGGERS:
                if (reports.getTriggers() != null) {
                    return trackingViewModel.createTaskResult(
                            reports.getTriggers(), TRIGGERS, taskRunUuid);
                }
                break;
            case SYMPTOMS:
                if (reports.getSymptoms() != null) {
                    return trackingViewModel.createTaskResult(
                            reports.getSymptoms(), SYMPTOMS, taskRunUuid);
                }
                break;
            case MEDICATION:
                if (reports.getMedication() != null) {
                    return trackingViewModel.createTaskResult(
                            reports.getMedication(), MEDICATION, taskRunUuid);
                }
                break;
        }
        LOGGER.warn("Previous tracking task result could not be found for task id " + trackingTaskIdentifier);
        return null;
    }

    /**
     * Refreshes the tab's completion states based on the current value of the live data
     */
    private void refreshTabCompletionState() {
        if (studyBurstViewModel == null) {
            return; // Guard NPE exceptions
        }
        setupMeasuringTaskCompletionState(studyBurstViewModel.liveData().getValue());
    }

    /**
     * Sets the completion check mark state of the measuring tasks based on their study burst state
     * @param item the study burst item that contains the study burst measuring task states
     */
    private void setupMeasuringTaskCompletionState(@Nullable StudyBurstItem item) {
        for (ImageView checkMarkImageView : checkMarkImageViews) {
            checkMarkImageView.setVisibility(View.GONE);
        }
        if (item == null ) {
            return; // Guard NPE exceptions
        }
        if (trackingMenuViewModel.getCurrentSelectedId() != R.id.measuring_tab) {
            return; // Tracking tasks don't have completion states
        }
        for (StudyBurstTaskInfo studyBurstTaskInfo : item.getOrderedTasks()) {
            if (studyBurstTaskInfo.isComplete()) {
                TaskInfo taskInfo = studyBurstTaskInfo.getTask();
                if (WALK_AND_BALANCE.equals(taskInfo.getIdentifier())) {
                    checkMarkImageViews.get(0).setVisibility(View.VISIBLE);
                } else if (TAPPING.equals(taskInfo.getIdentifier())) {
                    checkMarkImageViews.get(1).setVisibility(View.VISIBLE);
                } else if (TREMOR.equals(taskInfo.getIdentifier())) {
                    checkMarkImageViews.get(2).setVisibility(View.VISIBLE);
                }
            }
        }
    }

    /**
     * TrackingMenuFragmentViewModel contains data that should persist across the fragment life cycle state changes
     */
    public static class TrackingMenuFragmentViewModel extends ViewModel {
        private int selectedId = R.id.tracking_tab;
        void setSelectedId(int selectedId) {
            this.selectedId = selectedId;
        }
        int getCurrentSelectedId() {
            return selectedId;
        }
    }
}
