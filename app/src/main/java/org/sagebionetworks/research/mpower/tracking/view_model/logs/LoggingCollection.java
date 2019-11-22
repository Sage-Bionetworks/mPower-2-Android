package org.sagebionetworks.research.mpower.tracking.view_model.logs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;

import org.sagebionetworks.research.domain.result.interfaces.Result;
import org.sagebionetworks.research.sageresearch.dao.room.ReportResultDataMap;
import org.sagebionetworks.research.sageresearch.dao.room.ReportResultDataMapHelper;
import org.threeten.bp.Instant;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A logging collection stores a group of logs from a tracking task. This allows logs to be serialized/deserialized
 * in a manner consistent with the iOS implementation.
 * @param <E> The type of TrackingItemLog stored in the collection.
 */
@AutoValue
public abstract class LoggingCollection<E extends TrackingItemLog> implements Result, ReportResultDataMap {
    public static final String DEFAULT_TYPE = "loggingCollection";

    @AutoValue.Builder
    public abstract static class Builder<E extends TrackingItemLog> {
        public abstract LoggingCollection<E> build();

        @NonNull
        public abstract Builder<E> setStartDate(@Nullable Instant startDate);

        @NonNull
        public abstract Builder<E> setEndDate(@Nullable Instant endTime);

        @NonNull
        public abstract Builder<E> setIdentifier(@NonNull String identifier);

        @NonNull
        public abstract Builder<E> setItems(@NonNull List<E> items);

        @NonNull
        public abstract Builder<E> setType(@Nullable String type);
    }

    @Nullable
    @Override
    public Instant getStartTime() {
        return getStartDate();
    }

    @Nullable
    @Override
    public Instant getEndTime() {
        return getEndDate();
    }

    @Nullable
    public abstract Instant getStartDate();

    @Nullable
    public abstract Instant getEndDate();

    @NonNull
    @Override
    public abstract String getIdentifier();

    @NonNull
    public abstract ImmutableList<E> getItems();

    // Type field included to mirror iOS.
    @Nullable
    @Override
    public abstract String getType();

    public abstract Builder<E> toBuilder();

    public static <E extends TrackingItemLog> Builder<E> builder() {
        return new AutoValue_LoggingCollection.Builder<E>()
                .setItems(Collections.emptyList())
                .setType(DEFAULT_TYPE);
    }

    public static <E extends TrackingItemLog> TypeAdapter<LoggingCollection<E>> typeAdapter(Gson gson,
            TypeToken<? extends LoggingCollection<?>> token) {
        return new AutoValue_LoggingCollection.GsonTypeAdapter(gson, token);
    }

    @Nullable
    @Override
    public Map<String, Object> toDataMap() {
        return ReportResultDataMapHelper.toDataMap(this);
    }
}
