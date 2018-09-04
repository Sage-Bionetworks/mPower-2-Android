package org.sagebionetworks.research.motor_control_module.result;

import android.support.annotation.NonNull;
import android.support.annotation.Size;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import org.sagebionetworks.research.domain.result.interfaces.Result;
import org.sagebionetworks.research.motor_control_module.show_step_fragment.tapping.TappingSample;
import org.threeten.bp.Instant;
import org.threeten.bp.ZonedDateTime;

@AutoValue
public abstract class TappingResult implements Result {
    // TODO: switch to ImmutableIntArray @liujoshua a2018/08/30
    @AutoValue.Builder
    public abstract static class Builder {
        public abstract TappingResult build();

        public abstract ImmutableList.Builder<TappingSample> samplesBuilder();

        public abstract Builder setButtonBoundLeft(@NonNull @Size(4) int[] buttonBounds);

        public abstract Builder setButtonBoundRight(@NonNull @Size(4) int[] buttonBounds);

        public abstract Builder setEndTime(@NonNull Instant endTime);

        public abstract Builder setIdentifier(@NonNull String identifier);

        public abstract Builder setStartTime(@NonNull Instant endTime);

        public abstract Builder setStepViewSize(@NonNull @Size(2) int[] size);

        public abstract Builder setZonedEndTime(@NonNull ZonedDateTime endTime);

        public abstract Builder setZonedStartTime(@NonNull ZonedDateTime startTime);
    }

    public static final String TYPE_KEY = AppResultType.TAPPING;

    public static Builder builder() {
        return new AutoValue_TappingResult.Builder();
    }

    public static TypeAdapter<TappingResult> typeAdapter(Gson gson) {
        return new AutoValue_TappingResult.GsonTypeAdapter(gson);
    }

    /**
     * Returns teh bounds rectangle of the second tapping button.
     *
     * @return the bounds rectangle of the second tapping button.
     */
    @NonNull
    @Size(4)
    public abstract int[] getButtonBoundLeft();

    /**
     * Returns the bounds rectangle of the first tapping button.
     *
     * @return the bounds rectangle of the first tapping button.
     */
    @NonNull
    @Size(4)
    public abstract int[] getButtonBoundRight();

    @Override
    @NonNull
    public abstract Instant getEndTime();

    @Override
    @NonNull
    public String getType() {
        return TYPE_KEY;
    }

    /**
     * Returns the list of tapping samples for this result.
     *
     * @return the list of tapping samples for this result.
     */
    @NonNull
    public abstract ImmutableList<TappingSample> getSamples();

    /**
     * Returns the size of the view this result is associated with.
     *
     * @return the size of the view this result is associated with.
     */
    @NonNull
    @Size(2)
    public abstract int[] getStepViewSize();

    @NonNull
    public abstract ZonedDateTime getZonedEndTime();

    @NonNull
    public abstract ZonedDateTime getZonedStartTime();

    @NonNull
    public abstract Builder toBuilder();
}
