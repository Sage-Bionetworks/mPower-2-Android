package org.sagebionetworks.research.mpower.tracking.view_model;

import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;

import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;
import org.threeten.bp.Instant;

@AutoValue
public abstract class SimpleTrackingItemLog implements TrackingItemLog {
    @AutoValue.Builder
    public abstract static class Builder {
        public abstract SimpleTrackingItemLog build();

        @NonNull
        public abstract Builder setTrackingItem(@NonNull TrackingItem trackingItem);

        @NonNull
        public abstract Builder setTimestamp(@NonNull Instant timestamp);
    }

    public static Builder builder() {
        return new AutoValue_SimpleTrackingItemLog.Builder();
    }

    public abstract Builder toBuilder();
}
