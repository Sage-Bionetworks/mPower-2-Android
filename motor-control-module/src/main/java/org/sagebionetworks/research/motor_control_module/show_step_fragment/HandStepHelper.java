package org.sagebionetworks.research.motor_control_module.show_step_fragment;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.sagebionetworks.research.domain.result.interfaces.AnswerResult;
import org.sagebionetworks.research.domain.result.interfaces.TaskResult;
import org.sagebionetworks.research.domain.step.interfaces.SectionStep;
import org.sagebionetworks.research.domain.step.interfaces.Step;
import org.sagebionetworks.research.domain.task.Task;
import org.sagebionetworks.research.domain.task.navigation.StepNavigator;
import org.sagebionetworks.research.motor_control_module.show_step_fragment.hand_selection.ShowHandSelectionStepFragment;

import java.util.List;

public abstract class HandStepHelper {
    public static final String PLACEHOLDER = "<>";
    // Matches any step with the PLACEHOLDER anywhere in it's identifier or parent's identifiers.
    // it is expected that PLACEHOLDER will be replaced with the desired identifier via .replaceAll()
    public static final String REGEX_FORMAT;
    static {
        String startRegexFormat = "^" + PLACEHOLDER + "(\\..*)?";
        String middleRegexFormat = ".*\\." + PLACEHOLDER + "\\..*";
        REGEX_FORMAT = "(" + startRegexFormat + ")" + "|(" + middleRegexFormat + ")";
    }

    public enum Hand {
        LEFT, RIGHT;

        public String toString() {
            return this == LEFT ? "left" : "right";
        }

        @NonNull
        public Hand getOtherHand() {
            return this == LEFT ? RIGHT : LEFT;
        }

        @Nullable
        public static Hand fromString(@NonNull String str) {
            if (str.equals("left")) {
                return LEFT;
            } else if (str.equals("right")) {
                return RIGHT;
            }

            return null;
        }
    }

    /**
     * Returns whichever hand the current step's identifier represents a hand step for, or null
     * if the given identifier doesn't represent a hand step.
     * @param identifier The identifier of the step to get the hand for.
     * @return whichever hand the current step's identifier represents a hand step for, or null
     * if the given identifier doesn't represent a hand step.
     */
    public static Hand whichHand(@NonNull String identifier) {
        String leftRegex = REGEX_FORMAT.replaceAll(PLACEHOLDER, "left");
        if (identifier.matches(leftRegex)) {
            return Hand.LEFT;
        }

        String rightRegex = REGEX_FORMAT.replaceAll(PLACEHOLDER, "right");
        if (identifier.matches(rightRegex)) {
            return Hand.RIGHT;
        }

        return null;
    }

    /**
     * Returns the next hand that should go from the given task result, or null if no next hand
     * should go.
     * @param task The task to figure out which hand should go next in.
     * @param result The task result to figure out which hand should go next from.
     * @return the next hand that should go from the given task result.
     */
    @Nullable
    public static Hand nextHand(@NonNull Task task, @NonNull TaskResult result) {
        // find that hand sections.
        SectionStep leftSection = HandStepHelper.findHandSectionStep(task.getSteps(), Hand.LEFT);
        SectionStep rightSection = HandStepHelper.findHandSectionStep(task.getSteps(), Hand.RIGHT);
        if (leftSection == null || rightSection == null) {
            // If we don't have sections for both right and left there is no reason to continue.
            return null;
        }

        AnswerResult<List<String>> handOrderResult =
                result.getAnswerResult(ShowHandSelectionStepFragment.HAND_ORDER_KEY);
        List<String> handOrder = handOrderResult.getAnswer();
        Hand firstHand = Hand.fromString(handOrder.get(0));
        SectionStep firstSection = firstHand == Hand.LEFT ? leftSection : rightSection;
        if (!HandStepHelper.hasGone(firstHand, firstSection, result)) {
            return firstHand;
        } else {
            String secondHandString = handOrder.size() == 2 ? handOrder.get(1) : null;
            if (secondHandString != null) {
                Hand secondHand = Hand.fromString(secondHandString);
                SectionStep secondSection = secondHand == Hand.LEFT ? leftSection : rightSection;
                if (!HandStepHelper.hasGone(secondHand, secondSection, result)) {
                    return secondHand;
                }
            }
        }

        return null;
    }

    /**
     * Returns `true` if the given hand has already gone, `false` otherwise.
     * @param hand The hand to test whether has already gone.
     * @param handSection The SectionStep that contains all of the hand steps for the given hand.
     * @param result The task result to figure out if the hand has already gone from.
     * @return `true` if the given hand has already gone, `false` otherwise.
     */
    public static boolean hasGone(@NonNull Hand hand, @NonNull SectionStep handSection,
                                  @NonNull TaskResult result) {
        // If the hand has gone there must be either a string that has anything.handString.anything,
        // or handString.anything.
        String handString = hand.toString();
        String handRegex = REGEX_FORMAT.replaceAll(PLACEHOLDER, handString);
        return result.getResultsMatchingRegex(handRegex).size() != handSection.getSteps().size();
    }

    /**
     * Returns a SectionStep from the given list of steps which represents the Hand section for the
     * given hand.
     * @param steps The list of steps to find the section step in.
     * @param hand The hand to find the section step for.
     * @return a SectionStep from the given list of steps which represents the Hand section for the
     * given hand.
     */
    public static SectionStep findHandSectionStep(@NonNull List<Step> steps, @NonNull Hand hand) {
        String identifier = hand.toString();
        for (Step step : steps) {
            if (step instanceof SectionStep) {
                SectionStep sectionStep = (SectionStep)steps;
                // The given section step should have an identifier of the form <anything>.identifier
                // since it could be nested in other section steps.
                if (step.getIdentifier().matches("(.*\\.)?" + identifier)) {
                    return sectionStep;
                } else {
                    SectionStep substepResult = findHandSectionStep(sectionStep.getSteps(), hand);
                    if (substepResult != null) {
                        return substepResult;
                    }
                }
            }
        }

        return null;
    }
}
