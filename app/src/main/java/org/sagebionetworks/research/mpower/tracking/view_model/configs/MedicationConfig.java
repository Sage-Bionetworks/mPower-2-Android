package org.sagebionetworks.research.mpower.tracking.view_model.configs;

import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;

import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;

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
        public abstract Builder setTrackingItem(@NonNull TrackingItem trackingItem);
    }

    public static Builder builder() {
        return new AutoValue_MedicationConfig.Builder();
    }

    @Override
    public boolean isConfigured() {
        // TODO rkolmos 09/12/2018 implement this method when the config has the correct data.
        return false;
    }
}
