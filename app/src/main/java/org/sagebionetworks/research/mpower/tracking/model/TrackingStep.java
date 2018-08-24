package org.sagebionetworks.research.mpower.tracking.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import org.sagebionetworks.research.domain.async.AsyncActionConfiguration;
import org.sagebionetworks.research.domain.step.interfaces.Step;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

@AutoValue
public abstract class TrackingStep implements Step, Serializable {
    public static final String TYPE_KEY = "tracking";

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract TrackingStep build();

        @NonNull
        public abstract Builder setIdentifier(@NonNull String identifier);

        @NonNull
        public abstract Builder setAsyncActions(@NonNull Set<AsyncActionConfiguration> asyncActions);

        @NonNull
        public abstract Builder setSelectionInfo(@NonNull TrackingSubstepInfo selectionInfo);

        @NonNull
        public abstract Builder setLoggingInfo(@NonNull TrackingSubstepInfo loggingInfo);

        @NonNull
        public abstract Builder setItems(@NonNull Set<TrackingItem> items);

        @NonNull
        public abstract Builder setSections(@NonNull Set<TrackingSection> sections);
    }

    @NonNull
    public abstract ImmutableSet<TrackingItem> getItems();

    @NonNull
    public abstract ImmutableSet<TrackingSection> getSections();

    @NonNull
    @SerializedName("selection")
    public abstract TrackingSubstepInfo getSelectionInfo();

    @NonNull
    @SerializedName("logging")
    public abstract TrackingSubstepInfo getLoggingInfo();

    @Override
    @NonNull
    public TrackingStep copyWithIdentifier(@NonNull String identifier) {
        return this.toBuilder().setIdentifier(identifier).build();
    }

    @Override
    @NonNull
    public String getType() {
        return TYPE_KEY;
    }

    @NonNull
    public static Builder builder() {
        return new AutoValue_TrackingStep.Builder()
                .setAsyncActions(ImmutableSet.of())
                .setItems(ImmutableSet.of())
                .setSections(ImmutableSet.of());
    }

    @NonNull
    public abstract Builder toBuilder();

    @NonNull
    public static TypeAdapter<TrackingStep> typeAdapter(Gson gson) {
        return new AutoValue_TrackingStep.GsonTypeAdapter(gson)
                .setDefaultAsyncActions(ImmutableSet.of())
                .setDefaultItems(ImmutableSet.of())
                .setDefaultSections(ImmutableSet.of());
    }
}