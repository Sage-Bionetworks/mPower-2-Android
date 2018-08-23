package org.sagebionetworks.research.mpower.tracking.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import org.sagebionetworks.research.domain.async.AsyncActionConfiguration;
import org.sagebionetworks.research.domain.step.interfaces.UIStep;
import org.sagebionetworks.research.domain.step.ui.action.Action;

import java.util.Map;
import java.util.Set;

@AutoValue
public abstract class TrackingSubstepInfo implements UIStep {
    @AutoValue.Builder
    public abstract static class Builder {
        public abstract TrackingSubstepInfo build();

        @NonNull
        public abstract Builder setIdentifier(@NonNull String identifier);

        @NonNull
        public abstract Builder setType(@NonNull String type);

        @NonNull
        public abstract Builder setAsyncActions(@NonNull Set<AsyncActionConfiguration> asyncActions);

        @NonNull
        public abstract Builder setActions(@NonNull Map<String, Action> actions);

        @NonNull
        public abstract Builder setHiddenActions(@NonNull Set<String> hiddenActions);

        @NonNull
        public abstract Builder setTitle(@Nullable String title);

        @NonNull
        public abstract Builder setText(@Nullable String text);

        @NonNull
        public abstract Builder setDetail(@Nullable String detail);

        @NonNull
        public abstract Builder setFootnote(@Nullable String footnote);
    }

    @NonNull
    public TrackingSubstepInfo copyWithIdentifier(@NonNull String identifier) {
        return this.toBuilder().setIdentifier(identifier).build();
    }

    @NonNull
    public static Builder builder() {
        return new AutoValue_TrackingSubstepInfo.Builder()
                .setActions(ImmutableMap.of())
                .setAsyncActions(ImmutableSet.of())
                .setHiddenActions(ImmutableSet.of());
    }

    @NonNull
    public abstract Builder toBuilder();

    @NonNull
    public static TypeAdapter<TrackingSubstepInfo> typeAdapter(Gson gson) {
        return new AutoValue_TrackingSubstepInfo.GsonTypeAdapter(gson)
                .setDefaultActions(ImmutableMap.of())
                .setDefaultAsyncActions(ImmutableSet.of())
                .setDefaultHiddenActions(ImmutableSet.of());
    }


}
