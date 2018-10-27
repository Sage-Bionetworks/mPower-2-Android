package org.sagebionetworks.research.mpower.tracking;

import static com.google.common.base.Preconditions.checkState;

import static org.sagebionetworks.research.mpower.research.MpIdentifier.TAPPING;
import static org.sagebionetworks.research.mpower.research.MpIdentifier.TREMOR;
import static org.sagebionetworks.research.mpower.research.MpIdentifier.WALK_AND_BALANCE;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.TaskLauncher;
import org.sagebionetworks.research.mpower.Tasks;
import org.sagebionetworks.research.mpower.viewmodel.StudyBurstItem;
import org.sagebionetworks.research.mpower.viewmodel.StudyBurstTaskInfo;
import org.sagebionetworks.research.mpower.viewmodel.StudyBurstViewModel;
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

    @BindViews({R.id.centerIconCheckmark, R.id.leftIconCheckmark, R.id.rightIconCheckmark})
    protected List<ImageView> checkMarkImageViews;

    private Unbinder unbinder;
    private GestureDetectorCompat gestureDetector;

    @Inject
    StudyBurstViewModel.Factory studyBurstViewModelFactory;
    private @Nullable StudyBurstViewModel studyBurstViewModel;

    private @NonNull TrackingMenuFragmentViewModel trackingMenuViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidSupportInjection.inject(this);

        trackingMenuViewModel =
                ViewModelProviders.of(this).get(TrackingMenuFragmentViewModel.class);
        studyBurstViewModel = ViewModelProviders.of(this,
                studyBurstViewModelFactory).get(StudyBurstViewModel.class);
        studyBurstViewModel.liveData().observe(this, this::setupMeasuringTaskCompletionState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup group, @Nullable Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.tracking_menu_fragment, group, false);
        this.unbinder = ButterKnife.bind(this, result);
        updateSelection(trackingMenuViewModel.getCurrentSelectedId(), true);
        this.trackingButton.setOnClickListener(view -> updateSelection(R.id.tracking_tab, false));
        this.measuringButton.setOnClickListener(view -> updateSelection(R.id.measuring_tab, false));
        this.setIconListeners();
        this.gestureDetector = new GestureDetectorCompat(this.getContext(), new FlingListener());
        result.setOnTouchListener((view, event) -> {
            view.performClick();
            return this.gestureDetector.onTouchEvent(event);
        });
        return result;
    }

    private void setIconListeners() {
        for (int i = 0; i < this.imageViews.size(); i++) {
            final int copy = i;
            this.imageViews.get(i).setOnClickListener(view -> {

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
                    @Nullable String taskGuid = findScheduleGuidForMeasuringTask(taskIdentifier);
                    @Nullable UUID uuid = null;
                    if (studyBurstViewModel != null) {
                        uuid = studyBurstViewModel.createScheduleTaskRunUuid(taskGuid);
                    }
                    launcher.launchTask(getContext(), taskIdentifier, uuid);
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

    @Nullable
    private String getTaskIdentifierFromLabel(int label) {
        if (label == R.string.measuring_walk_and_balance_label) {
            return Tasks.WALK_AND_BALANCE;
        } else if (label == R.string.measuring_finger_tapping_label) {
            return Tasks.TAPPING;
        } else if (label == R.string.measuring_tremor_test_label) {
            return Tasks.TREMOR;
        } else if (label == R.string.tracking_medication_label) {
            return Tasks.MEDICATION;
        } else if (label == R.string.tracking_symptom_label) {
            return Tasks.SYMPTOMS;
        } else if (label == R.string.tracking_trigger_label) {
            return Tasks.TRIGGERS;
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
        int trackingButtonColor = trackingSelected ? resources.getColor(SELECTED_COLOR) : resources.getColor(UNSELECTED_COLOR);
        int measuringButtonColor = measuringSelected ? resources.getColor(SELECTED_COLOR) : resources.getColor(UNSELECTED_COLOR);
        float trackingImageViewAlpha = trackingSelected ? 1f : 0f;
        float measuringImageViewAlpha = measuringSelected ? 1f : 0f;
        this.trackingButton.setTextColor(trackingButtonColor);
        this.measuringButton.setTextColor(measuringButtonColor);
        this.trackingSelectedImageView.setAlpha(trackingImageViewAlpha);
        this.measuringSelectedImageView.setAlpha(measuringImageViewAlpha);
        refreshTabCompletionState();
    }

    /**
     * @param measuringTaskIdentifier of the schedule guid to find
     * @return the guid of the measuring task schedule if one can be found, null otheriwse
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
        int selectedId = R.id.tracking_tab;
        void setSelectedId(int selectedId) {
            this.selectedId = selectedId;
        }
        int getCurrentSelectedId() {
            return selectedId;
        }
    }
}
