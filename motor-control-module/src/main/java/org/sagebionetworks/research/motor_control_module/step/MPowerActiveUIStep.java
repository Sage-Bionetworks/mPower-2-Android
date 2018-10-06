package org.sagebionetworks.research.motor_control_module.step;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import org.sagebionetworks.research.domain.async.AsyncActionConfiguration;
import org.sagebionetworks.research.domain.result.interfaces.TaskResult;
import org.sagebionetworks.research.domain.step.interfaces.ActiveUIStep;
import org.sagebionetworks.research.domain.step.ui.action.Action;
import org.sagebionetworks.research.domain.step.ui.theme.ColorTheme;
import org.sagebionetworks.research.domain.step.ui.theme.ImageTheme;
import org.sagebionetworks.research.domain.task.navigation.strategy.StepNavigationStrategy;

import java.util.Map;
import java.util.Set;


@AutoValue
public abstract class MPowerActiveUIStep implements ActiveUIStep, StepNavigationStrategy.SkipStepStrategy,
        StepNavigationStrategy.NextStepStrategy {
    public static final String TYPE_KEY = AppStepType.MPOWER_ACTIVE;

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract MPowerActiveUIStep build();

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

    public static TypeAdapter<MPowerActiveUIStep> typeAdapter(Gson gson) {
        return new AutoValue_MPowerActiveUIStep.GsonTypeAdapter(gson)
                .setDefaultActions(ImmutableMap.of())
                .setDefaultAsyncActions(ImmutableSet.of())
                .setDefaultCommands(ImmutableSet.of())
                .setDefaultHiddenActions(ImmutableSet.of())
                .setDefaultSpokenInstructions(ImmutableMap.of());
    }

    public static Builder builder() {
        return new AutoValue_MPowerActiveUIStep.Builder()
                .setActions(ImmutableMap.of())
                .setAsyncActions(ImmutableSet.of())
                .setCommands(ImmutableSet.of())
                .setBackgroundAudioRequired(false)
                .setHiddenActions(ImmutableSet.of());
    }

    public abstract Builder toBuilder();

    @Override
    public String getType() {
        return TYPE_KEY;
    }

    @Override
    public String getNextStepIdentifier(TaskResult taskResult) {
        String nextStepIdentifier = HandStepNavigationRuleHelper.getNextStepIdentifier(this.getIdentifier(), taskResult);
        return nextStepIdentifier;
    }

    @Override
    public boolean shouldSkip(TaskResult taskResult) {
        boolean shouldSkip = HandStepNavigationRuleHelper.shouldSkip(this.getIdentifier(), taskResult);
        return shouldSkip;
    }

    @Override
    @NonNull
    public MPowerActiveUIStep copyWithIdentifier(@NonNull String identifier) {
        return this.toBuilder().setIdentifier(identifier).build();
    }
}
