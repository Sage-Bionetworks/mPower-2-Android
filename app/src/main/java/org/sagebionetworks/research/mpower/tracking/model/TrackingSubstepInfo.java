package org.sagebionetworks.research.mpower.tracking.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import org.sagebionetworks.research.domain.step.ui.action.Action;

import java.util.Map;

/**
 * Represents the information about what type of screen should be displayed for on of the parts (Selection, Logging)
 * for a Tracking Task. This information includes what language should be displayed as well as what type of screen
 * this substep should use.
 */
@AutoValue
public abstract class TrackingSubstepInfo {
    @AutoValue.Builder
    public abstract static class Builder {
        public abstract TrackingSubstepInfo build();

        @NonNull
        public abstract Builder setType(@Nullable String type);

        @NonNull
        public abstract Builder setActions(@Nullable Map<String, Action> actions);

        @NonNull
        public abstract Builder setTitle(@Nullable String title);

        @NonNull
        public abstract Builder setText(@Nullable String text);

        @NonNull
        public abstract Builder setDetail(@Nullable String detail);

        @NonNull
        public abstract Builder setFootnote(@Nullable String footnote);
    }

    @Nullable
    public abstract Map<String, Action> getActions();

    @Nullable
    public abstract String getType();

    @Nullable
    public abstract String getTitle();

    @Nullable
    public abstract String getText();

    @Nullable
    public abstract String getDetail();

    @Nullable
    public abstract String getFootnote();

    @NonNull
    public static Builder builder() {
        return new AutoValue_TrackingSubstepInfo.Builder()
                .setActions(ImmutableMap.of());
    }

    @NonNull
    public abstract Builder toBuilder();

    @NonNull
    public static TypeAdapter<TrackingSubstepInfo> typeAdapter(Gson gson) {
        return new AutoValue_TrackingSubstepInfo.GsonTypeAdapter(gson)
                .setDefaultActions(ImmutableMap.of());
    }
}
