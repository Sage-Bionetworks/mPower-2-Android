package org.sagebionetworks.research.motor_control_module.show_step_fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.sagebionetworks.research.domain.result.interfaces.TaskResult;
import org.sagebionetworks.research.domain.task.Task;
import org.sagebionetworks.research.mobile_ui.show_step.view.FragmentSkipRule;
import org.sagebionetworks.research.mobile_ui.show_step.view.ShowStepFragmentBase;
import org.sagebionetworks.research.mobile_ui.show_step.view.ShowUIStepFragmentBase;
import org.sagebionetworks.research.mobile_ui.show_step.view.view_binding.UIStepViewBinding;
import org.sagebionetworks.research.presentation.model.interfaces.UIStepView;
import org.sagebionetworks.research.presentation.show_step.show_step_view_models.ShowUIStepViewModel;

public abstract class HandFragmentNavigationHelper {
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
        return thisHand != null && nextHand != null && thisHand != nextHand ? nextHand.toString() : null;
    }
}
