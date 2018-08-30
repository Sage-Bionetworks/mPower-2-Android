/*
 * Copyright 2015 Apple Inc.
 * Ported to Android from ResearchKit/ResearchKit 1.5
 */

package org.sagebionetworks.research.motor_control_module.show_step_fragment.tapping;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import org.threeten.bp.Instant;

@AutoValue
public abstract class TappingSample {
    @AutoValue.Builder
    public abstract static class Builder {
        public abstract TappingSample build();

        public abstract Builder setButtonIdentifier(@TappingButtonIdentifier @NonNull String buttonIdentifier);

        public abstract Builder setDuration(@NonNull double duration);

        public abstract Builder setLocation(float[] location);

        public abstract Builder setStepPath(@NonNull String stepPath);

        public Builder setTimestamp(@Nullable Instant timestamp) {
            return setTimestamp(toEpochSeconds(timestamp));
        }

        public abstract Builder setTimestamp(double timestamp);

        public abstract Builder setUptime(double uptime);

        public Builder setUptime(@NonNull Instant uptimeInstant) {
            return setUptime(toEpochSeconds(uptimeInstant));
        }
    }

    public static Builder builder() {
        return new AutoValue_TappingSample.Builder();
    }

    public static double toEpochSeconds(Instant instant) {
        return (double) instant.getEpochSecond()
                + (double) instant.getNano() / 1_000_000_000;
    }

    public static TypeAdapter<TappingSample> typeAdapter(Gson gson) {
        return new AutoValue_TappingSample.GsonTypeAdapter(gson);
    }

    @TappingButtonIdentifier
    @NonNull
    public abstract String getButtonIdentifier();

    public abstract double getDuration();

    @NonNull
    public abstract float[] getLocation();

    @NonNull
    public abstract String getStepPath();

    public abstract double getTimestamp();

    @NonNull
    public abstract double getUptime();

    @NonNull
    public abstract Builder toBuilder();
}
