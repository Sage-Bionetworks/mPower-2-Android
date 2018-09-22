package org.sagebionetworks.research.mpower.tracking.fragment;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * The DurationFragment allows the user to select from the duration options to specify how long a symptom has occurred for.
 * In order to receive updates when the user selects a new duration the creator of this fragment should set an
 * OnDurationChangeListener via setOnDurationChangeListener.
 */
public class DurationFragment extends Fragment {
    public static final String ARGUMENT_TITLE = "title";
    public static final String ARGUMENT_DETAIL = "detail";
    public static final String ARGUMENT_PREVIOUS_SELECTION = "previousSelection";

    private static final Logger LOGGER = LoggerFactory.getLogger(DurationFragment.class);

    @ColorRes
    private static final int SELECTED_COLOR = R.color.royal200;
    @ColorRes
    private static final int UNSELECTED_COLOR = R.color.transparent;

    /**
     * Interface for receiving a callback from this fragment when the user submits a new duration.
     */
    public interface OnDurationChangeListener {
        void durationChanged(String duration);
    }

    private TextView selectedTextView;
    @BindView(R.id.rs2_step_navigation_action_backward)
    ActionButton backButton;
    @BindView(R.id.rs2_step_navigation_action_forward)
    ActionButton forwardButton;
    @BindViews({R.id.duration_0, R.id.duration_1, R.id.duration_2, R.id.duration_3, R.id.duration_4, R.id.duration_5})
    List<TextView> durationSelections;
    @BindView(R.id.rs2_title)
    TextView titleLabel;
    @BindView(R.id.rs2_detail)
    TextView detailLabel;

    private String title;
    private String detail;
    private String previousSelection;
    private Unbinder unbinder;
    private OnDurationChangeListener onDurationChangeListener;

    public static DurationFragment newInstance(@Nullable String title, @Nullable String detail, @Nullable String previousSelection) {
        DurationFragment fragment = new DurationFragment();
        Bundle args = new Bundle();
        args.putString(ARGUMENT_TITLE, title);
        args.putString(ARGUMENT_DETAIL, detail);
        args.putString(ARGUMENT_PREVIOUS_SELECTION, previousSelection);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            Bundle arguments = getArguments();
            if (arguments != null) {
                this.title = arguments.getString(ARGUMENT_TITLE);
                this.detail = arguments.getString(ARGUMENT_DETAIL);
                this.previousSelection = arguments.getString(ARGUMENT_PREVIOUS_SELECTION);
            }
        } else {
            this.title = savedInstanceState.getString(ARGUMENT_TITLE);
            this.detail = savedInstanceState.getString(ARGUMENT_DETAIL);
            this.previousSelection = savedInstanceState.getString(ARGUMENT_PREVIOUS_SELECTION);
        }

        this.selectedTextView = null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.mpower2_duration_step, container, false);
        this.unbinder = ButterKnife.bind(this, result);
        OnApplyWindowInsetsListener topListener = SystemWindowHelper.getOnApplyWindowInsetsListener(Direction.TOP);
        ViewCompat.setOnApplyWindowInsetsListener(this.backButton, topListener);
        if (this.title != null) {
            this.titleLabel.setText(this.title);
        } else {
            this.titleLabel.setVisibility(View.GONE);
        }

        if (this.detail != null) {
            this.detailLabel.setText(this.detail);
        } else {
            this.detailLabel.setVisibility(View.GONE);
        }

        int unselectedColor = this.getResources().getColor(UNSELECTED_COLOR);
        int selectedColor = this.getResources().getColor(SELECTED_COLOR);
        for (TextView selection : this.durationSelections) {
            if (selection.getText().toString().equals(this.previousSelection)) {
                selection.setBackgroundColor(selectedColor);
                this.selectedTextView = selection;
            } else {
                selection.setBackgroundColor(unselectedColor);
            }
        }

        this.initializeWithPreviousDuration();
        this.setSelectionListeners();
        this.setForwardButtonListener();
        this.setBackButtonListener();
        this.forwardButton.setText(this.getResources().getText(R.string.duration_fragment_forward_button));
        return result;
    }

    @Override
    public void onStart() {
        super.onStart();
        ViewCompat.requestApplyInsets(this.getView());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.unbinder.unbind();
    }

    /**
     * Sets the OnDurationChangeListener for this fragment.
     * @param onDurationChangeListener the OnDurationChangeListener to call when the user submits a duration.
     */
    public void setOnDurationChangeListener(OnDurationChangeListener onDurationChangeListener) {
        this.onDurationChangeListener = onDurationChangeListener;
    }

    /**
     * Initializes the state of this fragment based on the previous duration selection.
     */
    private void initializeWithPreviousDuration() {
        if (this.previousSelection != null) {
            int selectedColor = this.getResources().getColor(SELECTED_COLOR);
            for (TextView selection : this.durationSelections) {
                if (selection.getText().toString().equals(this.previousSelection)) {
                    this.selectedTextView = selection;
                    selection.setBackgroundColor(selectedColor);
                }
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
            if (this.selectedTextView != null) {
                String selected = this.selectedTextView.getText().toString();
                if (this.onDurationChangeListener != null) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Duration entered by user: " + selected);
                    }

                    this.onDurationChangeListener.durationChanged(selected);
                } else {
                    LOGGER.warn("DurationFragment could not submit duration because listener was null");
                }
            }


            this.goToParentFragment();
        });
    }

    /**
     * Navigates back to the parent fragment.
     */
    private void goToParentFragment() {
        // Pop the back stack once to go back to the parent fragment.
        FragmentManager fragmentManager = this.getFragmentManager();
        if (fragmentManager != null) {
            fragmentManager.popBackStackImmediate();
        } else {
            LOGGER.warn("FragmentManager is null cannot navigate back to parent fragment.");
        }
    }
}
