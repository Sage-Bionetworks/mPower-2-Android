package org.sagebionetworks.research.mpower.tracking.view_model.configs;

import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;

/**
 * Represents a Selection from the selection screen of a Tracking Task, and any configuration options that need to
 * be added to it. For instance in the medication task the config would include information such as the time of day,
 * and dosage for the medication.
 */
public interface TrackingItemConfig {
    /**
     * Returns the identifier of this config.
     * @return the identifier of this config.
     */
    String getIdentifier();

    /**
     * Returns the tracking item for this config.
     * @return the tracking item for this config.
     */
    TrackingItem getTrackingItem();

    /**
     * Returns true if the user has configured this item, false otherwise.
     * @return true if the user has configured this item, false otherwise.
     */
    boolean isConfigured();
}
