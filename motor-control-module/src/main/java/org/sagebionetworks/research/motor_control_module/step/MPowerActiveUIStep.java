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

import java.util.Map;

public class MPowerActiveUIStep extends ActiveUIStepBase implements StepNavigationStrategy.SkipStepStrategy,
        StepNavigationStrategy.NextStepStrategy {

    public MPowerActiveUIStep(@NonNull String identifier, @NonNull Map<String, Action> actions, @Nullable String title, @Nullable String text, @Nullable String detail, @Nullable String footnote, @Nullable ColorTheme colorTheme, @Nullable ImageTheme imageTheme, @Nullable Double duration, boolean backgroundAudioRequired) {
        super(identifier, actions, title, text, detail, footnote, colorTheme, imageTheme, duration, backgroundAudioRequired);
    }

    @Override
    public String getNextStepIdentifier(Task task, TaskResult taskResult) {
        return HandStepNavigationRuleHelper.getNextStepIdentifier(this.getIdentifier(), task, taskResult);
    }

    @Override
    public boolean shouldSkip(Task task, TaskResult taskResult) {
        return HandStepNavigationRuleHelper.shouldSkip(this.getIdentifier(), task, taskResult);
    }

    @Override
    @NonNull
    public MPowerActiveUIStep copyWithIdentifier(@NonNull String identifier) {
        return new MPowerActiveUIStep(identifier, this.getActions(), this.getTitle(), this.getText(),
                this.getDetail(), this.getFootnote(), this.getColorTheme(), this.getImageTheme(),
                this.getDuration(), this.isBackgroundAudioRequired());
    }
}
