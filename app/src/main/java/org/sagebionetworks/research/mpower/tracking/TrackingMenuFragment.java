package org.sagebionetworks.research.mpower.tracking;

import static com.google.common.base.Preconditions.checkState;

import android.content.Intent;
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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.TaskLauncher;
import org.sagebionetworks.research.mpower.studyburst.MedicationActivity;
import org.sagebionetworks.research.mpower.Tasks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

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
    private int selectedId;
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

    private Unbinder unbinder;
    private GestureDetectorCompat gestureDetector;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidSupportInjection.inject(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup group, @Nullable Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.tracking_menu_fragment, group, false);
        this.unbinder = ButterKnife.bind(this, result);
        this.updateMenuState(true, false);
        this.setIcons(TRACKING_ICONS, TRACKING_LABELS);
        this.selectedId = this.trackingButton.getId();
        this.trackingButton.setOnClickListener(view -> updateSelection(R.id.tracking_tab));
        this.measuringButton.setOnClickListener(view -> updateSelection(R.id.measuring_tab));
        this.setIconListeners();
        this.showingContent = true;
        this.gestureDetector = new GestureDetectorCompat(this.getContext(), new FlingListener());
        result.setOnTouchListener((view, event) -> {
            view.performClick();
            return this.gestureDetector.onTouchEvent(event);
        });

        //TODO: temporary
        this.imageViews.get(0).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                Intent intent = new Intent(getActivity(), MedicationActivity.class);
                getActivity().startActivity(intent);
            }
        });

        return result;
    }

    private void setIconListeners() {
        for (int i = 0; i < this.imageViews.size(); i++) {
            final int copy = i;
            this.imageViews.get(i).setOnClickListener(view -> {
                int selection = 0;
                if (this.selectedId == R.id.tracking_tab) {
                    selection = TRACKING_LABELS.get(copy);
                } else if (this.selectedId == R.id.measuring_tab) {
                    selection = MEASURING_LABELS.get(copy);
                }

                String taskIdentifier = this.getTaskIdentifierFromLabel(selection);
                if (taskIdentifier != null) {
                    launcher.launchTask(this.getContext(), taskIdentifier, null);
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
        if (this.showingContent != showingContent) {
            this.showingContent = showingContent;
            int visibility = showingContent ? View.VISIBLE : View.GONE;
            this.contentView.setVisibility(visibility);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.unbinder.unbind();
    }


    private void updateSelection(int selectionId) {
        if (selectionId != this.selectedId) {
            this.setShowingContent(true);
            this.selectedId = selectionId;
            if (selectionId == R.id.tracking_tab) {
                this.setIcons(TRACKING_ICONS, TRACKING_LABELS);
                this.updateMenuState(true, false);
            } else if (selectionId == R.id.measuring_tab) {
                this.setIcons(MEASURING_ICONS, MEASURING_LABELS);
                this.updateMenuState(false, true);
            }
        } else {
            this.setShowingContent(false);
            this.selectedId = 0;
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
    }
}
