package org.sagebionetworks.research.mpower.tracking.util;

import android.support.annotation.NonNull;

import org.sagebionetworks.research.mpower.Tasks;
import org.sagebionetworks.research.mpower.tracking.model.TrackingStepView;
import org.sagebionetworks.research.mpower.tracking.model.TrackingSubstepInfo;

/**
 * Util to encapsulate the logic of figuring out which task (TRIGGERS, MEDICATION, or SYMPTOMS) a TrackingStepView
 * represents.
 */
public class WhichTaskUtil {
    public static final String SYMPTOM_LOGGING_TYPE_KEY = "symptomLogging";
    public static final String MEDICATION_REMINDERS_TYPE_KEY = "medicationReminders";

    private WhichTaskUtil() {}

    /**
     * Returns the type of task that the given TrackingStepView represents.
     * @param trackingStepView the TrackingStepView to get the type of task for.
     * @return the type of task that the given TrackingStepView represents.
     */
    public static String whichTask(@NonNull TrackingStepView trackingStepView) {
        String selectionType = trackingStepView.getSelectionInfo().getType();
        TrackingSubstepInfo loggingInfo = trackingStepView.getLoggingInfo();
        String loggingType = null;
        if (loggingInfo != null) {
            loggingType = loggingInfo.getType();
        }

        String reminderType = null;
        TrackingSubstepInfo reminderInfo = trackingStepView.getRemindersInfo();
        if (reminderInfo != null) {
            reminderType = reminderInfo.getType();
        }

        if (selectionType == null && loggingType == null && reminderType == null) {
            return Tasks.TRIGGERS;
        } else if (selectionType == null && loggingType == null && reminderType.equals(MEDICATION_REMINDERS_TYPE_KEY)) {
            return Tasks.MEDICATION;
        } else if (selectionType == null && loggingType.equals(SYMPTOM_LOGGING_TYPE_KEY) && reminderType == null) {
            return Tasks.SYMPTOMS;
        }

        return null;
    }
}
