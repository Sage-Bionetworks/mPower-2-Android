package org.sagebionetworks.research.mpower.tracking.view_model.logs;

import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;
import org.sagebionetworks.research.mpower.tracking.view_model.HasTrackingItem;
import org.threeten.bp.Instant;

/**
 * Represents a log for a TrackingItem. For example in the Symptoms task the log would store the information about the
 * severity, time, duration, and medication timing of an individual Symptom which is being logged.
 */
public interface TrackingItemLog extends HasTrackingItem {
    /**
     * Returns the Instant that is the timestamp for when this lcg was created.
     * @return the Instant that is the timestamp for when this log was created.
     */
    Instant getTimestamp();
}
