package org.sagebionetworks.research.motor_control_module.step;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import org.sagebionetworks.research.domain.async.AsyncActionConfiguration;
import org.sagebionetworks.research.domain.result.implementations.ResultBase;
import org.sagebionetworks.research.domain.result.interfaces.Result;
import org.sagebionetworks.research.domain.step.interfaces.ThemedUIStep;
import org.sagebionetworks.research.domain.step.ui.action.Action;
import org.sagebionetworks.research.domain.step.ui.theme.ColorTheme;
import org.sagebionetworks.research.domain.step.ui.theme.ImageTheme;
import org.threeten.bp.Instant;

import java.util.Map;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

@AutoValue
public abstract class TappingCompletionStep implements ThemedUIStep {
    public static final String TYPE_KEY = AppStepType.TAPPING_COMPLETION;

    @AutoValue.Builder
    public abstract static class Builder {
        @NonNull
        public abstract TappingCompletionStep build();

        @NonNull
        public abstract Builder setActions(@NonNull Map<String, Action> actions);

        @NonNull
        public abstract Builder setAsyncActions(@NonNull Set<AsyncActionConfiguration> asyncActions);

        @NonNull
        public abstract Builder setColorTheme(@Nullable ColorTheme colorTheme);

        @NonNull
        public abstract Builder setDetail(@Nullable String detail);

        @NonNull
        public abstract Builder setFootnote(@Nullable String footnote);

        @NonNull
        public abstract Builder setHiddenActions(@NonNull Set<String> hiddenActions);

        @NonNull
        public abstract Builder setIdentifier(@NonNull String identifier);

        @NonNull
        public abstract Builder setImageTheme(@Nullable ImageTheme imageTheme);

        @NonNull
        public abstract Builder setText(@Nullable String text);

        @NonNull
        public abstract Builder setTitle(@Nullable String title);
    }

    public static Builder builder() {
        return new AutoValue_TappingCompletionStep.Builder()
                .setActions(ImmutableMap.of())
                .setAsyncActions(ImmutableSet.of())
                .setHiddenActions(ImmutableSet.of());
    }

    public static TypeAdapter<TappingCompletionStep> typeAdapter(Gson gson) {
        return new AutoValue_TappingCompletionStep.GsonTypeAdapter(gson)
                .setDefaultActions(ImmutableMap.of())
                .setDefaultAsyncActions(ImmutableSet.of())
                .setDefaultHiddenActions(ImmutableSet.of());
    }

    public abstract Builder toBuilder();

    @Override
    public String getType() {
        return TYPE_KEY;
    }

    @Override
    @NonNull
    public TappingCompletionStep copyWithIdentifier(@NonNull String identifier) {
        return this.toBuilder().setIdentifier(identifier).build();
    }

    public Result instantiateStepResult() {
        return new ResultBase(this.getIdentifier(), Instant.now(), Instant.now());
    }
}
