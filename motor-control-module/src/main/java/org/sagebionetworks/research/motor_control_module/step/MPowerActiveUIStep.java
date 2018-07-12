package org.sagebionetworks.research.motor_control_module.step;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import org.sagebionetworks.research.domain.result.interfaces.TaskResult;
import org.sagebionetworks.research.domain.step.implementations.ActiveUIStepBase;
import org.sagebionetworks.research.domain.step.ui.action.interfaces.Action;
import org.sagebionetworks.research.domain.step.ui.theme.ColorTheme;
import org.sagebionetworks.research.domain.step.ui.theme.ImageTheme;
import org.sagebionetworks.research.domain.task.Task;
import org.sagebionetworks.research.domain.task.navigation.strategy.StepNavigationStrategy;
import org.sagebionetworks.research.motor_control_module.show_step_fragment.HandFragmentNavigationHelper;
import org.sagebionetworks.research.motor_control_module.show_step_fragment.HandStepHelper;

import java.util.Map;

public class MPowerActiveUIStep extends ActiveUIStepBase implements StepNavigationStrategy.SkipStepStrategy,
        StepNavigationStrategy.NextStepStrategy {

    public MPowerActiveUIStep(@NonNull String identifier, @NonNull Map<String, Action> actions, @Nullable String title, @Nullable String text, @Nullable String detail, @Nullable String footnote, @Nullable ColorTheme colorTheme, @Nullable ImageTheme imageTheme, @Nullable Double duration, boolean backgroundAudioRequired) {
        super(identifier, actions, title, text, detail, footnote, colorTheme, imageTheme, duration, backgroundAudioRequired);
    }

    @Override
    public String getNextStepIdentifier(Task task, TaskResult taskResult) {
        HandStepHelper.Hand thisHand = HandStepHelper.whichHand(this.getIdentifier());
        HandStepHelper.Hand nextHand = HandStepHelper.nextHand(task, taskResult);
        return thisHand != null && nextHand != null && thisHand != nextHand ? nextHand.toString() : null;
    }

    @Override
    public boolean shouldSkip(Task task, TaskResult taskResult) {
        return HandFragmentNavigationHelper.shouldSkip(this.getIdentifier(), task, taskResult);
    }
}
