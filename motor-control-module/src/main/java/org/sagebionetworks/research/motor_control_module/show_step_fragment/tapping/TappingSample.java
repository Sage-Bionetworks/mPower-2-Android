/*
 * Copyright 2015 Apple Inc.
 * Ported to Android from ResearchKit/ResearchKit 1.5
 */

package org.sagebionetworks.research.motor_control_module.show_step_fragment.tapping;

import androidx.annotation.NonNull;

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

        public abstract Builder setDuration(double duration);

        public abstract Builder setLocation(@NonNull float[] location);

        public abstract Builder setStepPath(@NonNull String stepPath);

        public abstract Builder setTimestamp(double timestamp);

        public abstract Builder setUptime(double uptime);

        public Builder setUptime(long uptimeMillis) {
            return setUptime((double) uptimeMillis / 1_000);
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

    // duration in seconds
    public abstract double getDuration();

    @NonNull
    public abstract float[] getLocation();

    @NonNull
    public abstract String getStepPath();

    // timestamp in seconds
    public abstract double getTimestamp();

    // uptime in seconds
    public abstract double getUptime();

    @NonNull
    public abstract Builder toBuilder();
}
