/*
 * BSD 3-Clause License
 *
 * Copyright 2018  Sage Bionetworks. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1.  Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2.  Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * 3.  Neither the name of the copyright holder(s) nor the names of any contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission. No license is granted to the trademarks of
 * the copyright holders even if such marks are included in this software.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.sagebionetworks.research.motor_control_module.step;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.gson.annotations.SerializedName;

import org.sagebionetworks.research.domain.interfaces.HashCodeHelper;
import org.sagebionetworks.research.domain.result.interfaces.TaskResult;
import org.sagebionetworks.research.domain.step.StepType;
import org.sagebionetworks.research.domain.step.implementations.ActiveUIStepBase;
import org.sagebionetworks.research.domain.step.ui.action.interfaces.Action;
import org.sagebionetworks.research.domain.step.ui.theme.ColorTheme;
import org.sagebionetworks.research.domain.step.ui.theme.ImageTheme;
import org.sagebionetworks.research.domain.task.Task;
import org.sagebionetworks.research.domain.task.navigation.strategy.StepNavigationStrategy;
import org.sagebionetworks.research.motor_control_module.show_step_fragment.FirstRunHelper;

import java.util.Map;

public class InstructionStep extends MPowerActiveUIStep implements StepNavigationStrategy.SkipStepStrategy,
        StepNavigationStrategy.NextStepStrategy {
    public static final String TYPE_KEY = StepType.INSTRUCTION;

    @SerializedName("isFirstRunOnly")
    private final boolean firstRunOnly;

    public InstructionStep(@NonNull final String identifier,
            @NonNull final Map<String, Action> actions,
            @Nullable final String title,
            @Nullable final String text,
            @Nullable final String detail,
            @Nullable final String footnote,
            @Nullable final ColorTheme colorTheme,
            @Nullable final ImageTheme imageTheme,
            @Nullable final Double duration, final boolean backgroundAudioRequired,
            final boolean firstRunOnly) {
        super(identifier, actions, title, text, detail, footnote, colorTheme, imageTheme, duration,
                backgroundAudioRequired);
        this.firstRunOnly = firstRunOnly;
    }

    @Override
    public InstructionStep copyWithIdentifier(@NonNull String identifier) {
        return new InstructionStep(identifier, this.getActions(), this.getTitle(), this.getText(), this.getDetail(),
                this.getFootnote(), this.getColorTheme(), this.getImageTheme(), this.getDuration(),
                this.isBackgroundAudioRequired(), this.firstRunOnly);
    }

    @NonNull
    @Override
    public String getType() {
        return TYPE_KEY;
    }

    @Override
    protected boolean equalsHelper(Object other) {
        InstructionStep step = (InstructionStep) other;
        return super.equalsHelper(other) &&
                this.isFirstRunOnly() == step.isFirstRunOnly();
    }

    @Override
    protected HashCodeHelper hashCodeHelper() {
        return super.hashCodeHelper()
                .addFields(this.isFirstRunOnly());
    }

    @Override
    protected ToStringHelper toStringHelper() {
        return super.toStringHelper()
                .add("isFirstRunOnly", this.isFirstRunOnly());
    }

    public boolean isFirstRunOnly() {
        return this.firstRunOnly;
    }

    @Override
    public String getNextStepIdentifier(Task task, TaskResult taskResult) {
        return HandStepNavigationRuleHelper.getNextStepIdentifier(this.getIdentifier(), task, taskResult);
    }

    @Override
    public boolean shouldSkip(Task task, TaskResult taskResult) {
        // We skip an instruction step if it should be skipped because of it's hand, or because
        // it is first run only.
        return HandStepNavigationRuleHelper.shouldSkip(this.getIdentifier(), task, taskResult) ||
                (this.firstRunOnly && !FirstRunHelper.isFirstRun(taskResult));
    }
}
