package org.sagebionetworks.research.mpower.tracking.view_model.logs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import org.threeten.bp.Instant;

/**
 * A Basic TrackingItemLog which logs only which Item was logged, and a timestamp for when the log was created.
 */
@AutoValue
public abstract class SimpleTrackingItemLog implements TrackingItemLog {
    @AutoValue.Builder
    public abstract static class Builder {
        public abstract SimpleTrackingItemLog build();

        @NonNull
        public abstract Builder setIdentifier(@NonNull String identifier);

        @NonNull
        public abstract Builder setText(@NonNull String text);

        @NonNull
        public abstract Builder setLoggedDate(@Nullable Instant loggedDate);
    }

    @Nullable
    @Override
    public abstract Instant getLoggedDate();

    /**
     * Returns the text of this log.
     * @return the text of this log.
     */
    @NonNull
    @Override
    public abstract String getText();

    public static Builder builder() {
        return new AutoValue_SimpleTrackingItemLog.Builder();
    }

    public static TypeAdapter<SimpleTrackingItemLog> typeAdapter(Gson gson) {
        return new AutoValue_SimpleTrackingItemLog.GsonTypeAdapter(gson);
    }

    public abstract Builder toBuilder();
}
