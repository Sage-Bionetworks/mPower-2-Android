package org.sagebionetworks.research.mpower.tracking.view_model.logs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;


@AutoValue
public abstract class MedicationTimestamp {

    /**
     * @property timeOfDayFormatter used to convert back and forth from [LocalTime] to timeOfDay [String]
     */
    public static final DateTimeFormatter timeOfDayFormatter = DateTimeFormatter.ofPattern("HH:mm");

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract MedicationTimestamp build();

        @Nullable
        public abstract Builder setLoggedDate(@Nullable Instant loggedDate);

        @Nullable
        public abstract Builder setTimeOfDay(@Nullable String timeOfDay);
    }

    @Nullable
    public abstract Instant getLoggedDate();


    @Nullable
    public abstract String getTimeOfDay();    /**
     * @return timeOfDay [String] converted to a LocalTime.  Returns null if timeOfDay is also null.
     */

    public LocalTime getLocalTimeOfDay() {
        if (getTimeOfDay() != null) {
            return LocalTime.parse(getTimeOfDay(), timeOfDayFormatter);
        }
        return null;
    }

    public MedicationTimestamp copy(boolean clearLoggedDate) {
        if (clearLoggedDate) {
            return builder().setTimeOfDay(getTimeOfDay()).build();
        } else {
            return toBuilder().build();
        }
    }


    @NonNull
    public static Builder builder() {
        return new AutoValue_MedicationTimestamp.Builder().setLoggedDate(null);
    }

    @NonNull
    public static TypeAdapter<MedicationTimestamp> typeAdapter(Gson gson) {
        return new AutoValue_MedicationTimestamp.GsonTypeAdapter(gson);
    }

    @NonNull
    public abstract Builder toBuilder();
}
