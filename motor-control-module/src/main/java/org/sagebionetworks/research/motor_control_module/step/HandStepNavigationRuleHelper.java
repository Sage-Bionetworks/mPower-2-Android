package org.sagebionetworks.research.motor_control_module.step;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.sagebionetworks.research.domain.result.interfaces.TaskResult;
import org.sagebionetworks.research.domain.step.interfaces.SectionStep;
import org.sagebionetworks.research.domain.step.interfaces.Step;
import org.sagebionetworks.research.domain.task.Task;

public class HandStepNavigationRuleHelper {
    // private constructor to prevent instantiation
    private HandStepNavigationRuleHelper() {}

    public static boolean shouldSkip(@NonNull String stepIdentifier, @NonNull TaskResult taskResult) {
        HandStepHelper.Hand hand = HandStepHelper.whichHand(stepIdentifier);
        HandStepHelper.Hand nextHand = HandStepHelper.nextHand(taskResult);
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
