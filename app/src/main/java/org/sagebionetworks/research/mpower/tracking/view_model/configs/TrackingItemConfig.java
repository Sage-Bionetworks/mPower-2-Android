package org.sagebionetworks.research.mpower.tracking.view_model.configs;

import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;
import org.sagebionetworks.research.mpower.tracking.view_model.HasTrackingItem;

public interface TrackingItemConfig extends HasTrackingItem {
    /**
     * Returns true if the user has configured this item, false otherwise.
     * @return true if the user has configured this item, false otherwise.
     */
    boolean isConfigured();
}
