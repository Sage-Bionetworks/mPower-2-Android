package org.sagebionetworks.research.mpower.research

import org.sagebionetworks.research.mpower.research.MpRsdIdentifier.*
import org.sagebionetworks.research.sageresearch.manager.ActivityGroup
import org.sagebionetworks.research.sageresearch.manager.ActivityGroupObject
import org.sagebionetworks.research.sageresearch.manager.RsdIdentifier
import java.util.Random

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
    },
    MEASURING {
        override val identifier = "Measuring"
    },
    TRACKING {
        override val identifier = "Tracking"
    },
    HEALTH_SURVEYS {
        override val identifier = "Health Surveys"
    }
}

data class CompletionTask(
        val activityIdentifiers: Set<RsdIdentifier>,
        val day: Int) {
    fun preferredIdentifier(): RsdIdentifier? {
        return activityIdentifiers.intersect(setOf(DEMOGRAPHICS, ENGAGEMENT))
                .firstOrNull() ?: activityIdentifiers.firstOrNull()
    }
}

/**
 * The study burst configuration is a data class that can be added to the `AppConfig.clientData`.
 */
data class StudyBurstConfiguration(
        /**
         * @property identifier of the task.
         */
        val identifier: String = STUDY_BURST_REMINDER.identifier,
        /**
         * @property numberOfDays in the study burst.
         */
        val numberOfDays: Int = 14,
        /**
         * @property minimumRequiredDays in the study burst.
         */
        val minimumRequiredDays: Int = 10,
        /**
         * @property expiresLimit the time limit until the progress expires, defaults to 60 minutes in ms
         */
        val expiresLimit: Long = (60 * 60 * 1000),
        /**
         * @property taskGroupIdentifier used to mark the active tasks included in the study burst.
         */
        val taskGroupIdentifier: RsdIdentifier = MEASURING,
        /**
         * @property motivationIdentifier The identifier for the initial engagement survey.
         */
        val motivationIdentifier: RsdIdentifier = MOTIVATION,
        /**
         * @property completionTasks for each day of the study burst.
         */
        val completionTasks: Set<CompletionTask> = setOf(
                CompletionTask(setOf(STUDY_BURST_REMINDER, DEMOGRAPHICS), 1),
                CompletionTask(setOf(BACKGROUND), 9),
                CompletionTask(setOf(ENGAGEMENT), 14)),
        /**
         * @property engagementGroups set of the possible engagement data groups.
         */
        val engagementGroups: Set<Set<String>>? = setOf(
                setOf("gr_SC_DB", "gr_SC_CS"),
                setOf("gr_BR_AD", "gr_BR_II"),
                setOf("gr_ST_T", "gr_ST_F"),
                setOf("gr_DT_F", "gr_DT_T"))
) {

    /**
     * @return
     */
    fun completionTaskIdentifiers(): Set<RsdIdentifier> {
        return completionTasks.flatMap { it.activityIdentifiers }.union(setOf(motivationIdentifier))
    }

    /**
     * @return a randomized set of possible combinations of engagement groups.
     */
    fun randomEngagementGroups(): Set<String>? {
        return engagementGroups?.mapNotNull { it.randomElement() }?.toSet()
    }

    /**
     * @return a random element in the set if any exist
     */
    private fun <T> Set<T>?.randomElement(): T? {
        if (this == null) {
            return null
        }
        if (isEmpty()) {
            return null
        }
        return elementAt(Random().nextInt(size))
    }
}