package org.sagebionetworks.research.mpower.tracking.fragment;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.sagebionetworks.researchstack.backbone.utils.ResUtils;
import org.sagebionetworks.research.mobile_ui.show_step.view.SystemWindowHelper;
import org.sagebionetworks.research.mobile_ui.show_step.view.SystemWindowHelper.Direction;
import org.sagebionetworks.research.mobile_ui.widget.ActionButton;
import org.sagebionetworks.research.mpower.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalTime;

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
    @BindViews({R.id.divider_0, R.id.divider_1, R.id.divider_2, R.id.divider_3, R.id.divider_4, R.id.divider_5})
    List<View> durationDividers;
    @BindView(R.id.rs2_title)
    TextView titleLabel;
    @BindView(R.id.rs2_detail)
    TextView detailLabel;

    private SymptomDuration[] durationChoices =
            SymptomDuration.durationChoices(LocalTime.now());
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

        for (int i = 0; i < durationSelections.size(); i++) {
            TextView selection = durationSelections.get(i);
            View divider = durationDividers.get(i);
            if (durationChoices.length > i) {
                SymptomDuration duration = durationChoices[i];
                selection.setText(duration.getLocalizedTitle(getActivity()));

                if (duration.stringValue.equals(previousSelection)) {
                    selection.setBackgroundColor(selectedColor);
                    this.selectedTextView = selection;
                } else {
                    selection.setBackgroundColor(unselectedColor);
                }

                selection.setVisibility(View.VISIBLE);
                divider.setVisibility(View.VISIBLE);
            } else {
                selection.setVisibility(View.GONE);
                divider.setVisibility(View.GONE);
            }
        }

        this.setSelectionListeners();
        this.setForwardButtonListener();
        this.setBackButtonListener();
        this.forwardButton.setText(this.getResources().getText(R.string.duration_fragment_forward_button));
        return result;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getView() == null) {
            return;
        }
        ViewCompat.requestApplyInsets(getView());
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
                int selectDurationIndex = durationSelections.indexOf(selectedTextView);
                if (selectDurationIndex >= 0 && selectDurationIndex < durationChoices.length) {
                    String selected = durationChoices[selectDurationIndex].stringValue;
                    if (this.onDurationChangeListener != null) {
                        LOGGER.info("Duration entered by user: " + selected);
                        this.onDurationChangeListener.durationChanged(selected);
                    } else {
                        LOGGER.warn("DurationFragment could not submit duration because listener was null");
                    }
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

    public enum SymptomDuration {
        NOW("DURATION_CHOICE_NOW"),
        SHORT_PERIOD("DURATION_CHOICE_SHORT_PERIOD"),
        LITTLE_WHILE("DURATION_CHOICE_A_WHILE"),
        MORNING("DURATION_CHOICE_MORNING"),
        AFTERNOON("DURATION_CHOICE_AFTERNOON"),
        EVENING("DURATION_CHOICE_EVENING"),
        HALF_DAY("DURATION_CHOICE_HALF_DAY"),
        HALF_NIGHT("DURATION_CHOICE_HALF_NIGHT"),
        ALL_DAY("DURATION_CHOICE_ALL_DAY"),
        ALL_NIGHT("DURATION_CHOICE_ALL_NIGHT");

        SymptomDuration(String value) {
            this.stringValue = value;
        }

        public final String stringValue;

        @Nullable
        public String getLocalizedTitle(Context context) {
            if (context == null) {
                return null;
            }
            @StringRes int titleRes = ResUtils.getStringResourceId(context, stringValue.toLowerCase());
            if (titleRes == 0) {
                return null;
            }
            return context.getString(titleRes);
        }

        public static SymptomDuration[] durationChoices(@NonNull LocalTime time) {
            int hour = time.getHour();
            if (hour >= 5 && hour < 12) { // morning
                return new SymptomDuration[] {
                        NOW, SHORT_PERIOD, LITTLE_WHILE, MORNING, HALF_DAY, ALL_DAY };
            } else if (hour >= 12 && hour < 17) { // afternoon
                return new SymptomDuration[] {
                        NOW, SHORT_PERIOD, LITTLE_WHILE, AFTERNOON, HALF_DAY, ALL_DAY };
            } else if (hour >= 17 && hour < 22) { // afternoon
                return new SymptomDuration[] {
                        NOW, SHORT_PERIOD, LITTLE_WHILE, EVENING, HALF_DAY, ALL_DAY };
            } else {
                return new SymptomDuration[] {
                        NOW, SHORT_PERIOD, LITTLE_WHILE, HALF_NIGHT, ALL_NIGHT };
            }
        }
    }
}
