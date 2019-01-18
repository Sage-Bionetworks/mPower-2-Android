package org.sagebionetworks.research.mpower.tracking.view_model.logs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import org.sagebionetworks.research.mpower.tracking.view_model.configs.Schedule;
import org.threeten.bp.Instant;

import java.util.List;
import java.util.Set;

@AutoValue
public abstract class MedicationLog implements TrackingItemLog {
    @AutoValue.Builder
    public abstract static class Builder {
        public abstract MedicationLog build();

        @NonNull
        public abstract Builder setIdentifier(@NonNull String identifier);

        @NonNull
        public abstract Builder setText(@Nullable String text);

        @NonNull
        public abstract Builder setDosage(@NonNull String dosage);

        @NonNull
        public abstract Builder setScheduleItems(@NonNull List<Schedule> scheduleItems);

        @NonNull
        public abstract Builder setLoggedDate(@Nullable Instant loggedDate);

        @NonNull
        public abstract Builder setTimestamps(@NonNull Set<MedicationTimestamp> timestamps);
    }

    @Nullable
    @Override
    public abstract String getText();

    /**
     * @return The timestamps to use to mark the medication as "taken".
     */
    @NonNull
    public abstract Set<MedicationTimestamp> getTimestamps();

    /**
     * @return The scheduled items associated with this medication log.
     */
    @NonNull
    public abstract List<Schedule> getScheduleItems();

    /**
     * @return A string answer value for the dosage.
     */
    @NonNull
    public abstract String getDosage();

    @Nullable
    @Override
    public abstract Instant getLoggedDate();

    @NonNull
    public static Builder builder() {
        return new AutoValue_MedicationLog.Builder()
                .setTimestamps(ImmutableSet.of());
    }

    @NonNull
    public static TypeAdapter<MedicationLog> typeAdapter(Gson gson) {
        return new AutoValue_MedicationLog.GsonTypeAdapter(gson);
    }

    @NonNull
    public abstract Builder toBuilder();
}
