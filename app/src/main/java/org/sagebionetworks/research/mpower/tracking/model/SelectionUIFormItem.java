package org.sagebionetworks.research.mpower.tracking.model;

import androidx.annotation.NonNull;

/**
 * Marker to indicate that the class implementing this interface can be displayed on a SelectionFragment.
 */
public interface SelectionUIFormItem {
    @NonNull
    String getIdentifier();
}
