package org.sagebionetworks.research.mpower.tracking.view_model.logs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.threeten.bp.Instant;

/**
 * Represents a log for a TrackingItem. For example in the Symptoms task the log would store the information about the
 * severity, time, duration, and medication timing of an individual Symptom which is being logged.
 */
public interface TrackingItemLog {
    /**
     * Returns the identifier of this log.
     * @return the identifier of this log.
     */
    @NonNull
    String getIdentifier();

    /**
     * Returns the text of this log.
     * @return the text of this log.
     */
    @Nullable
    String getText();

    /**
     * Returns the Instant that is the timestamp for when this lcg was created.
     * @return the Instant that is the timestamp for when this log was created.
     */
    @Nullable
    Instant getLoggedDate();
}
