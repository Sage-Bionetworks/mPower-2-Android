package org.sagebionetworks.research.mpower.tracking.model;


import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;


@AutoValue
public abstract class TrackingItem implements SelectionUIFormItem, Parcelable {
    @AutoValue.Builder
    public abstract static class Builder {
        public abstract TrackingItem build();

        @NonNull
        public abstract Builder setIdentifier(@NonNull String identifier);

        @NonNull
        public abstract Builder setDetail(@Nullable String detail);

        @NonNull
        public abstract Builder setSectionIdentifier(@NonNull String sectionIdentifier);
    }

    @NonNull
    public abstract String getIdentifier();

    @Nullable
    public abstract String getDetail();

    @NonNull
    public abstract String getSectionIdentifier();

    @NonNull
    public static Builder builder() {
        return new AutoValue_TrackingItem.Builder();
    }

    @NonNull
    public static TypeAdapter<TrackingItem> typeAdapter(Gson gson) {
        return new AutoValue_TrackingItem.GsonTypeAdapter(gson);
    }
}
