package org.sagebionetworks.research.mpower.tracking.view_model.logs;

import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;

import org.threeten.bp.Instant;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@AutoValue
public abstract class LoggingCollection<E extends TrackingItemLog> {
    @AutoValue.Builder
    public abstract static class Builder<E extends TrackingItemLog> {
        public abstract LoggingCollection<E> build();

        @NonNull
        public abstract Builder<E> setStartDate(@NonNull Instant startDate);

        @NonNull
        public abstract Builder<E> setEndDate(@NonNull Instant endDate);

        @NonNull
        public abstract Builder<E> setIdentifier(@NonNull String identifier);

        @NonNull
        public abstract Builder<E> setItems(@NonNull List<E> items);
    }

    @NonNull
    public abstract Instant getStartDate();

    @NonNull
    public abstract Instant getEndDate();

    @NonNull
    public abstract String getIdentifier();

    @NonNull
    public abstract ImmutableList<E> getItems();

    public static <E extends TrackingItemLog> Builder<E> builder() {
        return new AutoValue_LoggingCollection.Builder<E>();
    }

    public static <E extends TrackingItemLog> TypeAdapter<LoggingCollection<E>> typeAdapter(Gson gson,
            TypeToken<? extends LoggingCollection> token) {
        return new AutoValue_LoggingCollection.GsonTypeAdapter(gson, token)
                .setDefaultItems(ImmutableList.of());
    }
}
