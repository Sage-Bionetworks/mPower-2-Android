package org.sagebionetworks.research.mpower.tracking.view_model;

import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;

public interface TrackingItemConfig extends HasTrackingItem {
    /**
     * Returns true if the user has configured this item, false otherwise.
     * @return true if the user has configured this item, false otherwise.
     */
    boolean isConfigured();
}
