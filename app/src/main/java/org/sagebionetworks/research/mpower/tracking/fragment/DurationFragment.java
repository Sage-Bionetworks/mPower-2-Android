package org.sagebionetworks.research.mpower.tracking.fragment;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.sagebionetworks.research.mobile_ui.show_step.view.SystemWindowHelper;
import org.sagebionetworks.research.mobile_ui.show_step.view.SystemWindowHelper.Direction;
import org.sagebionetworks.research.mobile_ui.widget.ActionButton;
import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;
import org.sagebionetworks.research.mpower.tracking.view_model.SymptomTrackingTaskViewModel;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.SimpleTrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SymptomLog;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;
import org.threeten.bp.Instant;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;

/**
 * The DurationFragment allows the user to select from the duration options to specify how long a symptom has occurred for.
 * It is an invariant that there is a log in the view model for the TrackingItem provided to this fragment as an argument,
 * and the client of this fragment is responsible for ensuring this is correct.
 */
public class DurationFragment extends TrackingFragment<SimpleTrackingItemConfig, SymptomLog, SymptomTrackingTaskViewModel> {
    public static final String ARGUMENT_TRACKING_ITEM = "trackingItem";
    public static final String FORWARD_BUTTON_TEXT = "Save";

    private TrackingItem trackingItem;
    @ColorRes
    private static final int SELECTED_COLOR = R.color.royal200;
    @ColorRes
    private static final int UNSELECTED_COLOR = R.color.transparent;
    private TextView selectedTextView;

    @BindView(R.id.rs2_step_navigation_action_backward)
    ActionButton backButton;
    @BindView(R.id.rs2_step_navigation_action_forward)
    ActionButton forwardButton;
    @BindViews({R.id.duration_0, R.id.duration_1, R.id.duration_2, R.id.duration_3, R.id.duration_4, R.id.duration_5})
    List<TextView> durationSelections;

    /**
     * Creates and returns a new DurationFragment from the given StepView and TrackingItem.
     * @param stepView The TrackingStepView to create the fragment with.
     * @param trackingItem The TrackingItem that the DurationFragment is for.
     * @return a new DurationFragment from the given StepView and TrackingItem.
     */
    public static DurationFragment newInstance(@NonNull StepView stepView, @NonNull TrackingItem trackingItem) {
        DurationFragment fragment = new DurationFragment();
        Bundle args = TrackingFragment.createArguments(stepView);
        args.putParcelable(ARGUMENT_TRACKING_ITEM, trackingItem);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            Bundle arguments = getArguments();
            if (arguments != null) {
                // noinspection unchecked
                this.trackingItem = arguments.getParcelable(ARGUMENT_TRACKING_ITEM);
            }
        } else {
            // noinspection unchecked
            this.trackingItem = savedInstanceState.getParcelable(ARGUMENT_STEP_VIEW);
        }

        this.selectedTextView = null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = super.onCreateView(inflater, container, savedInstanceState);
        OnApplyWindowInsetsListener topListener = SystemWindowHelper.getOnApplyWindowInsetsListener(Direction.TOP);
        ViewCompat.setOnApplyWindowInsetsListener(this.backButton, topListener);
        int unselectedColor = this.getResources().getColor(UNSELECTED_COLOR);
        for (TextView seleciton : this.durationSelections) {
            seleciton.setBackgroundColor(unselectedColor);
        }

        SymptomLog previousLog = this.viewModel.getLog(this.trackingItem);
        if (previousLog != null) {
            String previousDuration = previousLog.getDuration();
            if (previousDuration != null) {
                this.initializeFromPreviousLog(previousDuration);
            }
        }

        this.setSelectionListeners();
        this.setForwardButtonListener();
        this.forwardButton.setText(FORWARD_BUTTON_TEXT);
        this.setBackButtonListener();
        return result;
    }

    /**
     * Initializes the state of this fragment based on the previous duration selection already made in the log.
     * @param previousDuration The previous duration selection from the log.
     */
    private void initializeFromPreviousLog(@NonNull String previousDuration) {
        int selectedColor = this.getResources().getColor(SELECTED_COLOR);
        for (TextView selection : this.durationSelections) {
            if (selection.getText().toString().equals(previousDuration)) {
                this.selectedTextView = selection;
                selection.setBackgroundColor(selectedColor);
            }
        }
    }

    /**
     * Sets the OnClickListeners for all the selection options. This method does not write logs to the view model, and
     * only changes which selection is currently considered selected.
     */
    private void setSelectionListeners() {
        int selectedColor = this.getResources().getColor(SELECTED_COLOR);
        int unselectedColor = this.getResources().getColor(UNSELECTED_COLOR);
        for (TextView selection : this.durationSelections) {
            selection.setOnClickListener(view -> {
                if (this.selectedTextView != null) {
                    this.selectedTextView.setBackgroundColor(unselectedColor);
                }

                selection.setBackgroundColor(selectedColor);
                this.selectedTextView = selection;
            });
        }
    }

    /**
     * Sets the OnClickListener for the back button. The back button navigates back to it's parent fragment.
     */
    private void setBackButtonListener() {
        this.backButton.setOnClickListener(view -> this.goToParentFragment());
    }

    /**
     * Sets the OnClickListener for the forward button. The forward button writes a log to the view model with the
     * duration corresponding to the current selected duration, and then navigates back to it's parent fragment.
     */
    private void setForwardButtonListener() {
        this.forwardButton.setOnClickListener(view -> {
            String selected = null;
            if (this.selectedTextView != null) {
                selected = this.selectedTextView.getText().toString();
            }

            SymptomLog log = this.viewModel.getLog(this.trackingItem);
            log = log.toBuilder()
                    .setDuration(selected)
                    .setTimestamp(Instant.now())
                    .build();
            this.viewModel.addLoggedElement(log);
            this.goToParentFragment();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        ViewCompat.requestApplyInsets(this.getView());
    }

    @Override
    public int getLayoutId() {
        return R.layout.mpower2_duration_step;
    }

    /**
     * Navigates back to the parent fragment.
     */
    private void goToParentFragment() {
        // Pop the back stack once to go back to the parent fragment.
        this.getFragmentManager().popBackStackImmediate();
    }
}
