package org.sagebionetworks.research.motor_control_module.show_step_fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import org.sagebionetworks.research.domain.result.interfaces.TaskResult;
import org.sagebionetworks.research.domain.step.interfaces.SectionStep;
import org.sagebionetworks.research.domain.step.interfaces.Step;
import org.sagebionetworks.research.mobile_ui.show_step.view.ShowStepFragmentBase;
import org.sagebionetworks.research.mobile_ui.show_step.view.ShowUIStepFragmentBase;
import org.sagebionetworks.research.mobile_ui.show_step.view.SystemWindowHelper;
import org.sagebionetworks.research.mobile_ui.show_step.view.SystemWindowHelper.Direction;
import org.sagebionetworks.research.motor_control_module.R;
import org.sagebionetworks.research.motor_control_module.result.TappingResult;
import org.sagebionetworks.research.motor_control_module.show_step_fragment.tapping.TappingButtonIdentifier;
import org.sagebionetworks.research.motor_control_module.show_step_fragment.tapping.TappingSample;
import org.sagebionetworks.research.motor_control_module.step.HandStepHelper;
import org.sagebionetworks.research.motor_control_module.step.TappingStep;
import org.sagebionetworks.research.motor_control_module.step_view.TappingCompletionStepView;
import org.sagebionetworks.research.motor_control_module.widget.TapCountResultView;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;
import org.sagebionetworks.research.presentation.show_step.show_step_view_models.ShowUIStepViewModel;

import java.util.List;

public class ShowTappingCompletionStepFragment extends
        ShowUIStepFragmentBase<TappingCompletionStepView, ShowUIStepViewModel<TappingCompletionStepView>,
                TappingCompletionStepViewBinding> {
    public static ShowTappingCompletionStepFragment newInstance(@NonNull StepView stepView) {
        ShowTappingCompletionStepFragment fragment = new ShowTappingCompletionStepFragment();
        Bundle args = ShowStepFragmentBase.createArguments(stepView);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View result = super.onCreateView(inflater, container, savedInstanceState);
        ImageView imageView = this.stepViewBinding.getImageView();
        if (imageView != null) {
            OnApplyWindowInsetsListener listener = SystemWindowHelper.getOnApplyWindowInsetsListener(Direction.TOP);
            ViewCompat.setOnApplyWindowInsetsListener(imageView, listener);
        }

        return result;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.mpower2_tapping_completion_step;
    }

    @NonNull
    @Override
    protected TappingCompletionStepViewBinding instantiateAndBindBinding(View view) {
        return new TappingCompletionStepViewBinding(view);
    }

    @Override
    public void update(TappingCompletionStepView stepView) {
        super.update(stepView);
        TapCountResultView leftResultView = this.stepViewBinding.getLeftResult();
        TappingResult leftResult = this.getTappingResult(HandStepHelper.Hand.LEFT);
        this.updateTapCountResultView(leftResultView, leftResult, R.string.tapping_completion_left_description);
        TapCountResultView rightResultView = this.stepViewBinding.getRightResult();
        TappingResult rightResult = this.getTappingResult(HandStepHelper.Hand.RIGHT);
        this.updateTapCountResultView(rightResultView,  rightResult, R.string.tapping_completion_right_description);
        // Update the text view discussing the amount of time
        TextView timeLabel = this.stepViewBinding.getTimeLabel();
        timeLabel.setText(R.string.tapping_completion_time_label_text);
    }

    /**
     * Updates the given result view with the information from the given TappingResult.
     * @param view the TapCountResultView to update.
     * @param result the TappingResult to get the data to display from.
     * @param descriptionStringRes the String resource to display on the description label.
     */
    private void updateTapCountResultView(@NonNull TapCountResultView view, @Nullable TappingResult result,
                                          @StringRes int descriptionStringRes) {
        if (result != null) {
            // If we have a result we update the view.
            view.setVisibility(View.VISIBLE);
            int count = getHitButtonCount(result);
            view.setCount(count);
            String description = this.getResources().getString(descriptionStringRes);
            view.setDescription(description);
        } else {
            // If we don't have a result we hide the view.
            view.setVisibility(View.GONE);
        }
    }

    /**
     * Returns the TappingResult for the given Hand in this task, or null if the given Hand didn't perform this task.
     * @param hand the Hand to get the TappingResult for.
     * @return the TappingResult for the given Hand in this task, or null if the given Hand didn't perform this task.
     */
    @Nullable
    private TappingResult getTappingResult(@NonNull HandStepHelper.Hand hand) {
        TaskResult taskResult = this.performTaskViewModel.getTaskResult().getValue();
        if (taskResult != null) {
            TappingStep step = this.getTappingStep(hand);
            if (step != null) {
                return (TappingResult) taskResult.getResult(step);
            }
        }

        return null;
    }

    /**
     * Returns the TappingStep for the given Hand in this task.
     * @param hand The hand to get the TappingStep for.
     * @return the TappingStep for the given Hand in this task.
     */
    @Nullable
    private TappingStep getTappingStep(@NonNull HandStepHelper.Hand hand) {
        List<Step> steps = this.performTaskViewModel.getTask().getSteps();
        return getTappingStepHelper(steps, hand);
    }

    private static TappingStep getTappingStepHelper(@NonNull List<Step> steps, @NonNull HandStepHelper.Hand hand) {
        for (Step step : steps) {
            if (step instanceof TappingStep && HandStepHelper.whichHand(step.getIdentifier()) == hand) {
                return (TappingStep)step;
            } else if (step instanceof SectionStep) {
                TappingStep sectionResult = getTappingStepHelper(((SectionStep) step).getSteps(), hand);
                if (sectionResult != null) {
                    return sectionResult;
                }
            }
        }

        return null;
    }

    /**
     * Returns the number of hit buttons, in the given TappingResult. A hit button corresponds to any sample that
     * has an identifier that isn't NONE.
     * @param tappingResult The TappingResult to get the HitButton count from.
     * @return the number of hit buttons, in the given TappingResult.
     */
    private static int getHitButtonCount(@Nullable TappingResult tappingResult) {
        int count = 0;
        if (tappingResult != null) {
            for (TappingSample sample : tappingResult.getSamples()) {
                if (!sample.getButtonIdentifier().equals(TappingButtonIdentifier.NONE)) {
                    count++;
                }
            }
        }

        return count;
    }
}
