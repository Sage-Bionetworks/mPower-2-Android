package org.sagebionetworks.research.motor_control_module.show_step_view_model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import org.sagebionetworks.research.domain.result.interfaces.TaskResult;
import org.sagebionetworks.research.domain.step.interfaces.SectionStep;
import org.sagebionetworks.research.domain.step.interfaces.Step;
import org.sagebionetworks.research.motor_control_module.R;
import org.sagebionetworks.research.motor_control_module.result.TappingResult;
import org.sagebionetworks.research.motor_control_module.step.HandStepHelper;
import org.sagebionetworks.research.motor_control_module.step.HandStepHelper.Hand;
import org.sagebionetworks.research.motor_control_module.step.TappingStep;
import org.sagebionetworks.research.motor_control_module.step_view.TappingCompletionStepView;
import org.sagebionetworks.research.presentation.perform_task.PerformTaskViewModel;
import org.sagebionetworks.research.presentation.show_step.show_step_view_models.ShowUIStepViewModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ShowTappingCompletionStepViewModel extends ShowUIStepViewModel<TappingCompletionStepView> {
    private static Logger LOGGER = LoggerFactory.getLogger(ShowTappingCompletionStepViewModel.class);

    public ShowTappingCompletionStepViewModel(
            final PerformTaskViewModel performTaskViewModel,
            final TappingCompletionStepView stepView) {
        super(performTaskViewModel, stepView);
    }

    /**
     * Returns the number of taps the user completed with the given hand.
     * @param hand the hand to get the number of taps for.
     * @return the number of taps the user completed with the given hand.
     */
    @Nullable
    public Integer getTappingCount(@NonNull HandStepHelper.Hand hand) {
        TappingResult tappingResult = this.getTappingResult(hand);
        return tappingResult != null ? tappingResult.getHitButtonCount() : null;
    }

    /**
     * Returns the resource for the string to display on the description label for the given hand. For example
     * getDescriptionText(<RIGHT>) returns a StringRes that is by default "RIGHT COUNT".
     * @param hand The hand to get the description label for.
     * @return the resource for the string to display on the description label for the given hand.
     */
    @StringRes
    public int getDescriptionText(@NonNull HandStepHelper.Hand hand) {
        return hand == Hand.LEFT ? R.string.tapping_completion_left_description : R.string.tapping_completion_right_description;
    }

    /**
     * Returns the TappingResult for the given Hand in this task, or null if the given Hand didn't perform this task.
     *
     * @param hand
     *         the Hand to get the TappingResult for.
     * @return the TappingResult for the given Hand in this task, or null if the given Hand didn't perform this task.
     */
    @Nullable
    private TappingResult getTappingResult(@NonNull HandStepHelper.Hand hand) {
        TaskResult taskResult = this.performTaskViewModel.getTaskResult();
        if (taskResult != null) {
            TappingStep step = this.getTappingStep(hand);
            if (step != null) {
                if (taskResult.getResult(step) instanceof  TappingResult) {
                    return (TappingResult) taskResult.getResult(step);
                } else {
                    LOGGER.warn("Not a tapping step result: {}", taskResult.getResult(step));
                }
            }
        }

        return null;
    }

    /**
     * Returns the TappingStep for the given Hand in this task.
     *
     * @param hand
     *         The hand to get the TappingStep for.
     * @return the TappingStep for the given Hand in this task.
     */
    @Nullable
    private TappingStep getTappingStep(@NonNull HandStepHelper.Hand hand) {
        List<Step> steps = this.performTaskViewModel.getTask().getSteps();
        return getTappingStepHelper(steps, hand);
    }

    /**
     * Returns the tapping step for the given hand from the list of tapping steps.
     * @param steps The list of steps to get the tapping step for the given hand from.
     * @param hand The hand to get the tapping step for.
     * @return the tapping step for the given hand from the list of tapping steps.
     */
    private static TappingStep getTappingStepHelper(@NonNull List<Step> steps, @NonNull HandStepHelper.Hand hand) {
        for (Step step : steps) {
            if (step instanceof TappingStep && HandStepHelper.whichHand(step.getIdentifier()) == hand) {
                return (TappingStep) step;
            } else if (step instanceof SectionStep) {
                TappingStep sectionResult = getTappingStepHelper(((SectionStep) step).getSteps(), hand);
                if (sectionResult != null) {
                    return sectionResult;
                }
            }
        }

        return null;
    }
}
