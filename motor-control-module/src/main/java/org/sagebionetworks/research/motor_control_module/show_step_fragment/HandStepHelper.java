package org.sagebionetworks.research.motor_control_module.show_step_fragment;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.sagebionetworks.research.domain.result.interfaces.AnswerResult;
import org.sagebionetworks.research.domain.result.interfaces.TaskResult;
import org.sagebionetworks.research.motor_control_module.show_step_fragment.hand_selection.ShowHandSelectionStepFragment;

import java.util.List;

public abstract class HandStepHelper {
    public enum Hand {
        LEFT, RIGHT;

        public String toString() {
            return this == LEFT ? "left" : "right";
        }

        public Hand getOtherHand() {
            return this == LEFT ? RIGHT : LEFT;
        }
    }

    public static boolean shouldSkip(@NonNull Hand hand, @NonNull TaskResult result) {
        AnswerResult<List<String>> handOrderResult =
                result.getAnswerResult(ShowHandSelectionStepFragment.HAND_ORDER_KEY);
        List<String> handOrder = handOrderResult.getAnswer();
        String handString = hand.toString();
        boolean isValid = handOrder.contains(handString);
        if (!isValid) {
            // If the hand is invalid we should skip it.
            return true;
        }

        boolean shouldGoFirst = handString.equals(handOrder.get(0));
        Hand otherHand = hand.getOtherHand();
        boolean hasGone = result.getResult(handString) != null;
        boolean otherHasGone = result.getResult(otherHand.toString()) != null;
        // If we should go first and the other hand has gone, we must have already gone and therefore
        // should skip ourselves, if the other hand hasn't gone and we shouldn't go first we should
        // skip ourselves to allow the other hand to go first. Also if both have already gone
        // we should skip.
        return shouldGoFirst == otherHasGone || (hasGone && otherHasGone);
    }
}
