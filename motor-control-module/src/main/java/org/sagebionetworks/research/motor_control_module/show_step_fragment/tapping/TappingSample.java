/*
 * Copyright 2015 Apple Inc.
 * Ported to Android from ResearchKit/ResearchKit 1.5
 */

package org.sagebionetworks.research.motor_control_module.show_step_fragment.tapping;

import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

@AutoValue
public abstract class TappingSample {
    @AutoValue.Builder
    public abstract static class Builder {
        public abstract TappingSample build();

        public abstract Builder setUptime(@NonNull Instant uptime);

        public abstract Builder setTimestampe(@Nullable Instant timestamp);

        public abstract Builder setStepPath(@NonNull String stepPath);

        public abstract Builder setButtonIdentifier(@TappingButtonIdentifier @NonNull String buttonIdentifier);

        public abstract Builder setLocation(@NonNull Point location);

        public abstract Builder setDuration(@NonNull Duration duration);
    }

    public static Builder builder() {
        return new AutoValue_TappingSample.Builder();
    }

    public static TypeAdapter<TappingSample> typeAdapter(Gson gson) {
        return new AutoValue_TappingSample.GsonTypeAdapter(gson);
    }

    public abstract Builder toBuilder();

    @NonNull
    public abstract Instant getUptime();

    @Nullable
    public abstract Instant getTimestamp();

    @NonNull
    public abstract String getStepPath();

    @TappingButtonIdentifier
    @NonNull
    public abstract String getButtonIdentifier();

    @NonNull
    public abstract Point getLocation();

    @NonNull
    public abstract Duration getDuration();
}
