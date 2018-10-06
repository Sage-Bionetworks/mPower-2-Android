package org.sagebionetworks.research.mpower.tracking.view_model.configs;


import androidx.annotation.NonNull;

import com.google.auto.value.AutoValue;

import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;

/**
 * A basic TrackingItemConfig which only stores the TrackingItem it is a configuration for.
 */
@AutoValue
public abstract class SimpleTrackingItemConfig implements TrackingItemConfig {
    @AutoValue.Builder
    public abstract static class Builder {
        public abstract SimpleTrackingItemConfig build();

        @NonNull
        public abstract Builder setIdentifier(@NonNull String identifier);

        @NonNull
        public abstract Builder setTrackingItem(@NonNull TrackingItem trackingItem);
    }

    public static Builder builder() {
        return new AutoValue_SimpleTrackingItemConfig.Builder();
    }

    @Override
    public boolean isConfigured() {
        // This config has no data beyond it's TrackingItem so it is always configured.
        return true;
    }
}
