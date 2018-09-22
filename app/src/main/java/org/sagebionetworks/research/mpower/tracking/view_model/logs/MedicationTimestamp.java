package org.sagebionetworks.research.mpower.tracking.view_model.logs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import java.time.Instant;

@AutoValue
public abstract class MedicationTimestamp {
    @AutoValue.Builder
    public abstract static class Builder {
        public abstract MedicationTimestamp build();

        @NonNull
        public abstract Builder setLoggedDate(@NonNull Instant loggedDate);

        @NonNull
        public abstract Builder setTimeOfDay(@NonNull String timeOfDay);
    }

    @NonNull
    public abstract Instant getLoggedDate();

    @NonNull
    public abstract String getTimeOfDay();

    @NonNull
    public static Builder builder() {
        return new AutoValue_MedicationTimestamp.Builder();
    }

    @NonNull
    public static TypeAdapter<MedicationTimestamp> typeAdapter(Gson gson) {
        return new AutoValue_MedicationTimestamp.GsonTypeAdapter(gson);
    }

    @NonNull
    public abstract Builder toBuilder();
}
