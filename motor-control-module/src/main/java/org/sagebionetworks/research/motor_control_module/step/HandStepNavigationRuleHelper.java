package org.sagebionetworks.research.motor_control_module.step;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.sagebionetworks.research.domain.result.interfaces.TaskResult;
import org.sagebionetworks.research.domain.step.interfaces.SectionStep;
import org.sagebionetworks.research.domain.step.interfaces.Step;
import org.sagebionetworks.research.domain.task.Task;

public abstract class HandStepNavigationRuleHelper {
    public static boolean shouldSkip(@NonNull String stepIdentifier, @NonNull Task task, @NonNull TaskResult taskResult) {
        HandStepHelper.Hand hand = HandStepHelper.whichHand(stepIdentifier);
        HandStepHelper.Hand nextHand = HandStepHelper.nextHand(task, taskResult);
        return hand != null && hand != nextHand;
    }

    @Nullable
    public static String getNextStepIdentifier(@NonNull String stepIdentifier, @NonNull Task task,
                                               @NonNull TaskResult taskResult) {
        HandStepHelper.Hand thisHand = HandStepHelper.whichHand(stepIdentifier);
        HandStepHelper.Hand nextHand = HandStepHelper.nextHand(task, taskResult);
        if(thisHand != null && nextHand != null && thisHand != nextHand) {
            Step resultStep = null;
            for (Step step : task.getSteps()) {
                if (step.getIdentifier().equals(nextHand.toString())) {
                    resultStep = step;
                }
            }

            if (resultStep != null) {
                while (resultStep instanceof SectionStep) {
                    resultStep = ((SectionStep) resultStep).getSteps().get(0);
                }

                return resultStep.getIdentifier();
            }
        }

        return null;
    }
}
