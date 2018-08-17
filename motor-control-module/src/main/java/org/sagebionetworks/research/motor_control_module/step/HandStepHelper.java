package org.sagebionetworks.research.motor_control_module.step;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.sagebionetworks.research.domain.result.interfaces.AnswerResult;
import org.sagebionetworks.research.domain.result.interfaces.Result;
import org.sagebionetworks.research.domain.result.interfaces.TaskResult;
import org.sagebionetworks.research.domain.step.interfaces.SectionStep;
import org.sagebionetworks.research.domain.step.interfaces.Step;
import org.sagebionetworks.research.domain.task.Task;
import org.sagebionetworks.research.motor_control_module.show_step_fragment.hand_selection.ShowHandSelectionStepFragment;
import org.sagebionetworks.research.presentation.DisplayString;

import java.util.List;

public class HandStepHelper {
    // private constructor to prevent instantiation
    private HandStepHelper() {}

    public static final String REGEX_PLACEHOLDER = "<>";
    public static final String JSON_PLACEHOLDER = "%@";
    // Matches any step with the REGEX_PLACEHOLDER anywhere in it's identifier or parent's identifiers.
    // it is expected that REGEX_PLACEHOLDER will be replaced with the desired identifier via .replaceAll()
    public static final String REGEX_FORMAT;
    public static final String SECTION_ACTIVE_STEP_IDENTIFIER = "active";
    public static final String SECTION_TAPPING_STEP_IDENTIFIER = "tapping";
    public static final String SECTION_TREMOR_STEP_IDENTIFIER = "tremor";

    static {
        String startRegexFormat = "^" + REGEX_PLACEHOLDER + "(\\..*)?";
        String middleRegexFormat = ".*\\." + REGEX_PLACEHOLDER + "(\\..*)?";
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
        String leftRegex = REGEX_FORMAT.replaceAll(REGEX_PLACEHOLDER, "left");
        if (identifier.matches(leftRegex)) {
            return Hand.LEFT;
        }

        String rightRegex = REGEX_FORMAT.replaceAll(REGEX_PLACEHOLDER, "right");
        if (identifier.matches(rightRegex)) {
            return Hand.RIGHT;
        }

        return null;
    }

    /**
     * Returns the order the hands should go in for the given task result.
     * @param result The result to get the hand order from.
     * @return the order the hands should go in for the given task result.
     */
    @Nullable
    public static List<String> getHandOrder(@NonNull TaskResult result) {
        AnswerResult<List<String>> handOrderResult =
                result.getAnswerResult(ShowHandSelectionStepFragment.HAND_ORDER_KEY);
        if (handOrderResult != null) {
            return handOrderResult.getAnswer();
        }

        return null;
    }

    /**
     * Returns the next hand that should go from the given task result, or null if no next hand
     * should go.
     * @param result The task result to figure out which hand should go next from.
     * @return the next hand that should go from the given task result.
     */
    @Nullable
    public static Hand nextHand(@NonNull TaskResult result) {
        // find that hand sections.
        List<String> handOrder = HandStepHelper.getHandOrder(result);
        if (handOrder == null) {
            // If we don't have a hand order we return.
            return null;
        }

        Hand firstHand = Hand.fromString(handOrder.get(0));
        if (!HandStepHelper.hasGone(firstHand, result)) {
            return firstHand;
        } else {
            String secondHandString = handOrder.size() == 2 ? handOrder.get(1) : null;
            if (secondHandString != null) {
                Hand secondHand = Hand.fromString(secondHandString);
                if (!HandStepHelper.hasGone(secondHand, result)) {
                    return secondHand;
                }
            }
        }

        return null;
    }

    public static DisplayString getHandString(@Nullable DisplayString fromJson, @NonNull String stepIdentifier) {
        Hand hand = HandStepHelper.whichHand(stepIdentifier);
        if (fromJson == null) {
            return null;
        }

        String originalString = fromJson.getDisplayString();
        if (hand != null && originalString != null) {
            originalString = originalString.replaceAll(JSON_PLACEHOLDER, hand.toString().toUpperCase());
        }

        return DisplayString.create(null, originalString);
    }

    /**
     * Returns `true` if the given hand has already gone, `false` otherwise.
     * @param hand The hand to test whether has already gone.
     * @param taskResult The task result to figure out if the hand has already gone from.
     * @return `true` if the given hand has already gone, `false` otherwise.
     */
    public static boolean hasGone(@NonNull Hand hand,
                                  @NonNull TaskResult taskResult) {
        // If the hand has gone there must be either a string that has anything.handString.anything,
        // or handString.anything.
        String handString = hand.toString();
        String handRegex = REGEX_FORMAT.replaceAll(REGEX_PLACEHOLDER, handString);
        List<Result> resultMatches = taskResult.getResultsMatchingRegex(handRegex);
        for (Result result : resultMatches) {
            String identifier = result.getIdentifier();
            if (identifier.endsWith("." + SECTION_ACTIVE_STEP_IDENTIFIER)
                    || identifier.endsWith("." + SECTION_TAPPING_STEP_IDENTIFIER)
                    || identifier.equals("." + SECTION_TREMOR_STEP_IDENTIFIER)) {
                return true;
            }
        }

        return false;
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
                SectionStep sectionStep = (SectionStep)step;
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
