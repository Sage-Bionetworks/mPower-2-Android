package org.sagebionetworks.research.mpower.tracking.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

/**
 * A TrackingSection represents a grouping of TrackingItems used by the UI to display the items in a logical way.
 */
@AutoValue
public abstract class TrackingSection implements SelectionUIFormItem {
    @AutoValue.Builder
    public abstract static class Builder {
        public abstract TrackingSection build();

        @NonNull
        public abstract Builder setIdentifier(@NonNull String identifier);

        @NonNull
        public abstract Builder setDetail(@Nullable String detail);
    }

    @Nullable
    public abstract String getDetail();

    @NonNull
    public static Builder builder() {
        return new AutoValue_TrackingSection.Builder();
    }

    @NonNull
    public static TypeAdapter<TrackingSection> typeAdapter(Gson gson) {
        return new AutoValue_TrackingSection.GsonTypeAdapter(gson);
    }
}
