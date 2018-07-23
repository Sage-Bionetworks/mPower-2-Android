package org.sagebionetworks.research.motor_control_module.result;

import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.Size;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import org.sagebionetworks.research.domain.result.interfaces.Result;
import org.sagebionetworks.research.motor_control_module.show_step_fragment.tapping.TappingSample;
import org.threeten.bp.Instant;

@AutoValue
public abstract class TappingResult implements Result {
    public static final String TYPE_KEY = AppResultType.TAPPING;

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract TappingResult build();

        public abstract Builder setIdentifier(@NonNull String identifier);

        public abstract Builder setStartTime(@NonNull Instant startTime);

        public abstract Builder setEndTime(@NonNull Instant endTime);

        public abstract Builder setSamples(@NonNull ImmutableList<TappingSample> samples);

        public abstract Builder setStepViewSize(@NonNull Point size);

        public abstract Builder setButtonRect1(@NonNull Rect buttonRect1);

        public abstract Builder setButtonRect2(@NonNull Rect buttonRect2);
    }

    public static Builder builder() {
        return new AutoValue_TappingResult.Builder();
    }

    public static TypeAdapter<TappingResult> typeAdapter(Gson gson) {
        return new AutoValue_TappingResult.GsonTypeAdapter(gson);
    }

    @Override
    @NonNull
    public String getType() {
        return TYPE_KEY;
    }

    public abstract Builder toBuilder();

    /**
     * Returns the list of tapping samples for this result.
     * @return the list of tapping samples for this result.
     */
    public abstract ImmutableList<TappingSample> getSamples();

    /**
     * Returns the size of the view this result is associated with.
     * @return the size of the view this result is associated with.
     */
    public abstract Point getStepViewSize();

    /**
     * Returns the bounds rectangle of the first tapping button.
     * @return the bounds rectangle of the first tapping button.
     */
    public abstract Rect getButtonRect1();

    /**
     * Returns teh bounds rectangle of the second tapping button.
     * @return the bounds rectangle of the second tapping button.
     */
    public abstract Rect getButtonRect2();
}
