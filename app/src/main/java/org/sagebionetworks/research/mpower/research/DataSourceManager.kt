package org.sagebionetworks.research.mpower.research

import org.sagebionetworks.research.mpower.research.MpRsdIdentifier.*
import org.sagebionetworks.research.sageresearch.manager.ActivityGroup
import org.sagebionetworks.research.sageresearch.manager.ActivityGroupObject
import org.sagebionetworks.research.sageresearch.manager.RsdIdentifier

class DataSourceManager {

    // TODO: mdephillips 9/4/18 the contents of this companion object should come from bridge config,
    // TODO: mdephillips 9/4/18 but that architecture hasn't been finished yet
    companion object {

        val installedGroups: Array<ActivityGroup>
            get() {
                return arrayOf(
                        ActivityGroupObject("Study Burst", "Study Burst",
                                activityIdentifiers = setOf(TAPPING, TREMOR, WALK_AND_BALANCE,
                                        STUDY_BURST_COMPLETED)),

                        ActivityGroupObject(
                                "Measuring", "Measuring",
                                activityIdentifiers = setOf(TAPPING, TREMOR, WALK_AND_BALANCE),
                                schedulePlanGuid = "3d898a6f-1ef2-4ece-9e9f-025d94bcd130"),

                        ActivityGroupObject(
                                "Tracking", "Tracking",
                                activityIdentifiers = setOf(SYMPTOMS, MEDICATION, TRIGGERS),
                                activityGuidMap = mapOf(
                                        SYMPTOMS.identifier to "60868b71-30a4-4e04-a00b-3aca6651deb2",
                                        MEDICATION.identifier to "273c4518-7cb6-4496-b1dd-c0b5bf291b09",
                                        TRIGGERS.identifier to "b0f07b7e-408e-4d50-9368-8220971e570c"
                                ))
                )
            }
    }
}

// TODO: mdephillips 9/4/18 load dynamically from bridge config and include in StudyBurstViewModel
class StudyBurstConfiguration {
    companion object {
        val completionTaskIdentifiers: Set<RsdIdentifier> get() {
            return setOf(DEMOGRAPHICS, BACKGROUND, ENGAGEMENT, MOTIVATION, STUDY_BURST_REMINDER)
        }
    }
}

// TODO: mdephillips 9/4/18 is this how we want to represent task ids?  why does iOS wrap them in an enum vs a string const?
enum class MpRsdIdentifier: RsdIdentifier {
    TRIGGERS {
        override val identifier = "Triggers"
    },
    SYMPTOMS {
        override val identifier = "Symptoms"
    },
    MEDICATION {
        override val identifier = "Medication"
    },
    STUDY_BURST_COMPLETED {
        override val identifier = "study-burst-task"
    },
    TAPPING {
        override val identifier = "Tapping"
    },
    WALK_AND_BALANCE {
        override val identifier = "WalkAndBalance"
    },
    TREMOR {
        override val identifier = "Tremor"
    },
    DEMOGRAPHICS {
        override val identifier = "Demographics"
    },
    BACKGROUND {
        override val identifier = "Background"
    },
    ENGAGEMENT {
        override val identifier = "Engagement"
    },
    MOTIVATION {
        override val identifier = "Motivation"
    },
    STUDY_BURST_REMINDER {
        override val identifier = "StudyBurstReminder"
    }
}