package org.sagebionetworks.research.motor_control_module.step;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.sagebionetworks.research.domain.result.interfaces.TaskResult;

public class HandStepNavigationRuleHelper {
    // private constructor to prevent instantiation
    private HandStepNavigationRuleHelper() {}

    public static boolean shouldSkip(@NonNull String stepIdentifier, @NonNull TaskResult taskResult) {
        HandStepHelper.Hand hand = HandStepHelper.whichHand(stepIdentifier);
        HandStepHelper.Hand nextHand = HandStepHelper.nextHand(taskResult);
        // TODO: check accuracy of logic
        return hand != null && hand != nextHand;
    }

    @Nullable
    public static String getNextStepIdentifier(@NonNull String stepIdentifier,
                                               @NonNull TaskResult taskResult) {
        HandStepHelper.Hand thisHand = HandStepHelper.whichHand(stepIdentifier);
        HandStepHelper.Hand nextHand = HandStepHelper.nextHand(taskResult);
        if(thisHand != null && nextHand != null && thisHand != nextHand) {
            return nextHand.toString();
        }

        return null;
    }
}
