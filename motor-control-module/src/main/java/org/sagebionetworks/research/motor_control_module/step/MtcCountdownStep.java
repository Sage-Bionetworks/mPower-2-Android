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

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import org.sagebionetworks.research.domain.async.AsyncActionConfiguration;
import org.sagebionetworks.research.domain.result.interfaces.TaskResult;
import org.sagebionetworks.research.domain.step.interfaces.CountdownStep;
import org.sagebionetworks.research.domain.step.ui.action.Action;
import org.sagebionetworks.research.domain.step.ui.theme.ColorTheme;
import org.sagebionetworks.research.domain.step.ui.theme.ImageTheme;
import org.sagebionetworks.research.domain.task.navigation.strategy.StepNavigationStrategy;
import org.sagebionetworks.research.motor_control_module.show_step_fragment.FirstRunHelper;

import java.util.Map;
import java.util.Set;

@AutoValue
public abstract class MtcCountdownStep implements CountdownStep, StepNavigationStrategy.SkipStepStrategy {
    public static final String TYPE = AppStepType.MTC_COUNTDOWN;

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract MtcCountdownStep build();

        @NonNull
        public abstract Builder setActions(@NonNull Map<String, Action> actions);

        @NonNull
        public abstract Builder setAsyncActions(@NonNull Set<AsyncActionConfiguration> asyncActions);

        @NonNull
        public abstract Builder setBackgroundAudioRequired(boolean isBackgroundAudioRequired);

        @NonNull
        public abstract Builder setColorTheme(@Nullable ColorTheme colorTheme);

        @NonNull
        public abstract Builder setCommands(@NonNull Set<String> commands);

        @NonNull
        public abstract Builder setDetail(@Nullable String detail);

        @NonNull
        public abstract Builder setDuration(@Nullable Double duration);

        @NonNull
        public abstract Builder setIsFirstRunOnly(boolean isFirstRunOnly);

        @NonNull
        public abstract Builder setFootnote(@Nullable String footnote);

        @NonNull
        public abstract Builder setHiddenActions(@NonNull Set<String> hiddenActions);

        @NonNull
        public abstract Builder setIdentifier(@NonNull String identifier);

        @NonNull
        public abstract Builder setImageTheme(@Nullable ImageTheme imageTheme);

        @NonNull
        public abstract Builder setSpokenInstructions(@NonNull Map<String, String> spokenInstructions);

        @NonNull
        public abstract Builder setText(@Nullable String text);

        @NonNull
        public abstract Builder setTitle(@Nullable String title);
    }

    public static TypeAdapter<MtcCountdownStep> typeAdapter(Gson gson) {
        return new AutoValue_MtcCountdownStep.GsonTypeAdapter(gson)
                .setDefaultActions(ImmutableMap.of())
                .setDefaultAsyncActions(ImmutableSet.of())
                .setDefaultCommands(ImmutableSet.of())
                .setDefaultHiddenActions(ImmutableSet.of())
                .setDefaultSpokenInstructions(ImmutableMap.of());
    }

    public static Builder builder() {
        return new AutoValue_MtcCountdownStep.Builder()
                .setActions(ImmutableMap.of())
                .setAsyncActions(ImmutableSet.of())
                .setCommands(ImmutableSet.of())
                .setHiddenActions(ImmutableSet.of())
                .setSpokenInstructions(ImmutableMap.of())
                .setIsFirstRunOnly(false)
                .setBackgroundAudioRequired(false);
    }

    public abstract boolean getIsFirstRunOnly();

    public abstract Builder toBuilder();

    @Override
    public boolean shouldSkip(@NonNull TaskResult taskResult) {
        boolean shouldSkip = HandStepNavigationRuleHelper.shouldSkip(this.getIdentifier(), taskResult) ||
                (this.getIsFirstRunOnly() && !FirstRunHelper.isFirstRun(taskResult));
        return shouldSkip;
    }

    @Override
    @NonNull
    public MtcCountdownStep copyWithIdentifier(@NonNull String identifier) {
        return this.toBuilder().setIdentifier(identifier).build();
    }

    @NonNull
    @Override
    public String getType() {
        return TYPE;
    }
}
