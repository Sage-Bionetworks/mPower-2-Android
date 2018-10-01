package org.sagebionetworks.research.mpower.tracking.view_model.configs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;

import java.util.Collections;
import java.util.List;

/**
 * Config specific to the medication task.
 */
// TODO rkolmos 09/12/2018 add the correct data to this object.
@AutoValue
public abstract class MedicationConfig implements TrackingItemConfig {
    @AutoValue.Builder
    public abstract static class Builder {
        public abstract MedicationConfig build();

        @NonNull
        public abstract Builder setIdentifier(@NonNull String identifier);

        @NonNull
        public abstract Builder setDosage(@Nullable String dosage);

        @NonNull
        public abstract Builder setSchedules(@NonNull List<Schedule> schedules);
    }

    @Nullable
    public abstract String getDosage();

    @NonNull
    public abstract List<Schedule> getSchedules();

    @NonNull
    public static Builder builder() {
        return new AutoValue_MedicationConfig.Builder()
                .setSchedules(Collections.singletonList(new Schedule("0")));
    }

    @NonNull
    public abstract Builder toBuilder();

    @Override
    public boolean isConfigured() {
        return getDosage() != null && !getSchedules().isEmpty();
    }
}
