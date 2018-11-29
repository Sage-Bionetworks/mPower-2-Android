package org.sagebionetworks.research.mpower.tracking.view_model.logs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.AutoValue_SymptomLog.GsonTypeAdapter;
import org.threeten.bp.Instant;

/**
 * A subclass of Note log specific to the Symptoms Task. Stores the TrackingItem that is being logged, a timestamp,
 * the severity of the symptom (integer 0-3), a note from the user, a time and duration of the symptom,
 * and the symptoms timing relative to when the user has taken their medication.
 */
@AutoValue
public abstract class SymptomLog implements TrackingItemLog {
    @AutoValue.Builder
    public abstract static class Builder {
        public abstract SymptomLog build();

        @NonNull
        public abstract Builder setIdentifier(@NonNull String identifier);

        @NonNull
        public abstract Builder setText(@NonNull String text);

        @NonNull
        public abstract Builder setLoggedDate(@Nullable Instant loggedDate);

        @NonNull
        public abstract Builder setSeverity(@Nullable Integer severity);

        @NonNull
        public abstract Builder setNote(@Nullable String note);

        @NonNull
        public abstract Builder setDuration(@Nullable String duration);

        @NonNull
        public abstract Builder setMedicationTiming(@Nullable String medicationTiming);
    }

    /**
     * Returns the text of this log.
     * @return the text of this log.
     */
    @NonNull
    @Override
    public abstract String getText();

    @Nullable
    @Override
    public abstract Instant getLoggedDate();

    @Nullable
    public abstract Integer getSeverity();

    @Nullable
    public abstract String getMedicationTiming();

    @Nullable
    public abstract String getNote();

    @Nullable
    public abstract String getDuration();

    public static Builder builder() {
        return new AutoValue_SymptomLog.Builder();
    }

    public static TypeAdapter<SymptomLog> typeAdapter(Gson gson) {
        return new GsonTypeAdapter(gson);
    }

    public abstract Builder toBuilder();
}
