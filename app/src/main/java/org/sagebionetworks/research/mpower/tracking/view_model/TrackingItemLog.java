package org.sagebionetworks.research.mpower.tracking.view_model;

import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;
import org.threeten.bp.Instant;

public interface TrackingItemLog extends HasTrackingItem {
    /**
     * Returns the Instant that is the timestamp for when this lcg was created.
     * @return the Instant that is the timestamp for when this log was created.
     */
    Instant getTimestamp();
}
