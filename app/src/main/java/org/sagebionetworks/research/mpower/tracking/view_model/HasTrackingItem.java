package org.sagebionetworks.research.mpower.tracking.view_model;

import android.support.annotation.NonNull;

import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;

/**
 * Represents a data object which stores a TrackingItem as part of it's data.
 */
public interface HasTrackingItem {
    /**
     * Returns the TrackingItem that this is the configuration for.
     * @return the TrackingItem that this is the configuration for.
     */
    @NonNull
    TrackingItem getTrackingItem();
}
