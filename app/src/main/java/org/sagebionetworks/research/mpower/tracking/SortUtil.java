package org.sagebionetworks.research.mpower.tracking;

import android.content.Context;
import android.support.annotation.NonNull;

import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.tracking.model.SelectionUIFormItem;
import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;
import org.sagebionetworks.research.mpower.tracking.model.TrackingSection;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.SimpleTrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.TrackingItemConfig;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
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

    /**
     * Returns a sorted list of days in the order they appear in a week. First any strings that are not days will appear
     * in the list, then Sunday, Monday, Tuesday, etc.
     * @param daysList The list of days to return a sorted version of.
     * @param context The context to get the days of the week from.
     * @return a sorted list of the days in the order they appear in a week.
     */
    public static List<String> sortDaysList(@NonNull List<String> daysList, @NonNull Context context) {
        List<String> result = new ArrayList<>(daysList);
        Collections.sort(result, (o1, o2) -> Integer.compare(getDayIndex(o1, context), getDayIndex(o2, context)));
        return result;
    }

    /**
     * Returns the index of the day in the given week, (ex in the English week system Sunday -> 0, Monday -> 1, etc.
     * If a day that is not a part of the week is passed the index -1 is returned.
     * @param day The day to get the index of.
     * @param context The context to get the days of the week from.
     * @return the index of the given day in a week, or -1 if the given day is not a part of a week.
     */
    private static int getDayIndex(@NonNull String day, @NonNull Context context) {
        String[] daysOfWeek = context.getResources().getStringArray(R.array.days_of_the_week);
        for (int i = 0; i < daysOfWeek.length; i++) {
            if (day.equals(daysOfWeek[i])) {
                return i;
            }
        }

        return -1;
    }
}
