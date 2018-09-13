package org.sagebionetworks.research.mpower.tracking;

import android.support.annotation.NonNull;

import org.sagebionetworks.research.mpower.tracking.model.SelectionUIFormItem;
import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;
import org.sagebionetworks.research.mpower.tracking.model.TrackingSection;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.SimpleTrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.TrackingItemConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility for sorting the active and available elements in a tracking task to ensure they are displayed to the user
 * in the correct order.
 */
public class SortUtil {
    private SortUtil() {}

    /**
     * Returns a sorted list (alphabetically) of the active elements from the activeElementsById mapping.
     * @param activeElementsById The mapping from identifier to config to get the sorted elements fro.
     * @return a sorted (alphabetically) list of the active elements from the activeElementsById mapping.
     */
    public static <E extends TrackingItemConfig> List<E> getActiveElementsSorted(@NonNull Map<String, E> activeElementsById) {
        List<E> result = new ArrayList<>(activeElementsById.values());
        Collections.sort(result, (o1, o2) -> o1.getIdentifier().compareTo(o2.getIdentifier()));
        return result;
    }

    /**
     * Returns a list of SelectionUIFormItems for all the availableElements. Elements are sorted so that the sections appear
     * in alphabetical order and then the items within each section appear in alphabetical order.
     * @param availableElements The available elements to get the sorted list from.
     * @return a list of SelectionUIFormItems for all the availableElements.
     */
    public static List<SelectionUIFormItem> getAvailableElementsSorted(@NonNull Map<TrackingSection, Set<TrackingItem>> availableElements) {
        List<SelectionUIFormItem> result = new ArrayList<>();
        // First we sort the sections.
        List<TrackingSection> sectionsSorted = new ArrayList<>(availableElements.keySet());
        Collections.sort(sectionsSorted, (o1, o2) -> o1.getIdentifier().compareTo(o2.getIdentifier()));
        // Next we sort the elements in each section.
        for (TrackingSection section : sectionsSorted) {
            result.add(section);
            List<TrackingItem> itemsInSectionSorted = new ArrayList<>(availableElements.get(section));
            Collections.sort(itemsInSectionSorted, (o1, o2) -> o1.getIdentifier().compareTo(o2.getIdentifier()));
            result.addAll(itemsInSectionSorted);
        }

        return result;
    }
}
