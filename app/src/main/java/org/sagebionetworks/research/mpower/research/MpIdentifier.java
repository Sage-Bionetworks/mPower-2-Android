package org.sagebionetworks.research.mpower.research;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@StringDef({MpIdentifier.TRIGGERS, MpIdentifier.SYMPTOMS, MpIdentifier.MEDICATION,
        MpIdentifier.STUDY_BURST_COMPLETED, MpIdentifier.STUDY_BURST_COMPLETED_UPLOAD,
        MpIdentifier.TAPPING, MpIdentifier.WALK_AND_BALANCE, MpIdentifier.TREMOR, MpIdentifier.DEMOGRAPHICS,
        MpIdentifier.BACKGROUND, MpIdentifier.ENGAGEMENT, MpIdentifier.MOTIVATION, MpIdentifier.STUDY_BURST_REMINDER,
        MpIdentifier.MEASURING, MpIdentifier.TRACKING, MpIdentifier.HEALTH_SURVEYS, MpIdentifier.AUTHENTICATE,
        MpIdentifier.PASSIVE_DATA_PERMISSION, MpIdentifier.PASSIVE_GAIT})
public @interface MpIdentifier {
    String TRIGGERS = "Triggers";
    String SYMPTOMS = "Symptoms";
    String MEDICATION = "Medication";
    String STUDY_BURST_COMPLETED = "study-burst-task";
    String STUDY_BURST_COMPLETED_UPLOAD = "StudyBurst"; // upload identifier differs from schedule task identifier
    String TAPPING = "Tapping";
    String WALK_AND_BALANCE = "WalkAndBalance";
    String TREMOR = "Tremor";
    String DEMOGRAPHICS = "Demographics";
    String BACKGROUND = "Background";
    String ENGAGEMENT = "Engagement";
    String MOTIVATION = "Motivation";
    String STUDY_BURST_REMINDER = "StudyBurstReminder";
    String MEASURING = "Measuring";
    String TRACKING = "Tracking";
    String HEALTH_SURVEYS = "Health Surveys";
    String AUTHENTICATE = "Signup";
    String PASSIVE_DATA_PERMISSION = "PassiveDataPermission";
    String PASSIVE_GAIT = "PassiveGait";
}
