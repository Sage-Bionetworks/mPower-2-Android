package org.sagebionetworks.research.mpower.research

import org.sagebionetworks.research.domain.task.TaskInfoBase
import org.sagebionetworks.research.mpower.research.MpIdentifier.*
import org.sagebionetworks.research.mpower.research.MpTaskInfo.Tapping
import org.sagebionetworks.research.mpower.research.MpTaskInfo.Tremor
import org.sagebionetworks.research.mpower.research.MpTaskInfo.WalkAndBalance
import org.sagebionetworks.research.sageresearch.manager.ActivityGroup
import org.sagebionetworks.research.sageresearch.manager.ActivityGroupObject
import org.sagebionetworks.research.sageresearch.manager.TaskInfo
import org.sagebionetworks.research.sageresearch.manager.TaskInfoObject
import org.threeten.bp.LocalDateTime
import java.util.Random

class DataSourceManager {

    // TODO: mdephillips 9/4/18 the contents of this companion object should come from bridge config,
    // TODO: mdephillips 9/4/18 but that architecture hasn't been finished yet
    companion object {

        val studyBurstGroup = ActivityGroupObject("Study Burst", "Study Burst",
                activityIdentifiers = setOf(
                        TAPPING, TREMOR, WALK_AND_BALANCE, STUDY_BURST_COMPLETED))

        val measuringGroup = ActivityGroupObject(
                "Measuring", "Measuring",
                tasks = setOf(Tapping, WalkAndBalance, Tremor),
                activityIdentifiers = setOf(TAPPING, TREMOR, WALK_AND_BALANCE),
                schedulePlanGuid = "3d898a6f-1ef2-4ece-9e9f-025d94bcd130")

        val trackingGroup = ActivityGroupObject(
                "Tracking", "Tracking",
                activityIdentifiers = setOf(SYMPTOMS, MEDICATION, TRIGGERS),
                activityGuidMap = mapOf(
                        SYMPTOMS to "60868b71-30a4-4e04-a00b-3aca6651deb2",
                        MEDICATION to "273c4518-7cb6-4496-b1dd-c0b5bf291b09",
                        TRIGGERS to "b0f07b7e-408e-4d50-9368-8220971e570c"
                ))

        val surveyActivityGroupIdentifier = "Surveys"
        val surveyGroup = ActivityGroupObject(
                surveyActivityGroupIdentifier, surveyActivityGroupIdentifier,
                activityIdentifiers = setOf(DEMOGRAPHICS, ENGAGEMENT, MOTIVATION, BACKGROUND))

        val installedGroups: Array<ActivityGroup>
            get() {
                return arrayOf(studyBurstGroup, measuringGroup, trackingGroup)
            }

        fun installedGroup(forIdentifier: String): ActivityGroup? {
            return installedGroups.firstOrNull { it.identifier == forIdentifier }
        }

        @JvmStatic
        fun defaultEngagementGroups(): Set<Set<String>> {
            return setOf(
                    setOf("gr_SC_DB","gr_SC_CS"),
                    setOf("gr_BR_AD","gr_BR_II"),
                    setOf("gr_ST_T","gr_ST_F"),
                    setOf("gr_DT_F","gr_DT_T"))
        }

        @JvmStatic
        fun randomDefaultEngagementGroups(): Set<String> {
            return defaultEngagementGroups().randomElement() ?: setOf()
        }
    }
}

object MpTaskInfo {
    val Tapping = TaskInfoObject(TAPPING, TAPPING,
            imageName = "ic_finger_tapping", estimatedMinutes = 1)

    val WalkAndBalance = TaskInfoObject(WALK_AND_BALANCE, WALK_AND_BALANCE,
            imageName = "ic_walk_and_stand", estimatedMinutes = 6)

    val Tremor = TaskInfoObject(TREMOR, TREMOR,
            imageName = "ic_tremor", estimatedMinutes = 4)
}

data class CompletionTask(
        val activityIdentifiers: LinkedHashSet<String>,
        val day: Int) {
    fun preferredIdentifier(): String? {
        return activityIdentifiers.intersect(linkedSetOf(DEMOGRAPHICS, ENGAGEMENT))
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
        val identifier: String = MpIdentifier.STUDY_BURST_COMPLETED,
        /**
         * @property numberOfDays in the study burst.
         */
        val numberOfDays: Int = 14,
        /**
         * @property minimumRequiredDays in the study burst.
         */
        val minimumRequiredDays: Int = 10,
        /**
         * @property maxDayCount The maximum number of days in a study burst.
         */
        var maxDayCount: Int = 19,
        /**
         * @property expiresLimit the time limit (in seconds) until the progress expires, defaults to 60 minutes
         */
        val expiresLimit: Long = 60 * 60L,
        /**
         * @property taskGroupIdentifier used to mark the active tasks included in the study burst.
         */
        val taskGroupIdentifier: String = MpIdentifier.MEASURING,
        /**
         * @property motivationIdentifier The identifier for the initial engagement survey.
         */
        val motivationIdentifier: String = MpIdentifier.MOTIVATION,
        /**
         * @property completionTasks for each day of the study burst.
         */
        val completionTasks: Set<CompletionTask> = setOf(
                CompletionTask(linkedSetOf(STUDY_BURST_REMINDER, DEMOGRAPHICS), 1),
                CompletionTask(linkedSetOf(BACKGROUND), 9)/**,
                // TODO: mdephillips 10/16/18 Add engagement back in when development is done on those features
                CompletionTask(linkedSetOf(ENGAGEMENT), 14)*/),
        /**
         * @property engagementGroups set of the possible engagement data groups.
         */
        val engagementGroups: Set<Set<String>>? = DataSourceManager.defaultEngagementGroups()
) {
    /**
     * @return a set of the completion task's activity identifiers
     */
    fun completionTaskIdentifiers(): Set<String> {
        return completionTasks.flatMap { it.activityIdentifiers }.union(setOf(motivationIdentifier))
    }

    /**
     * @return a randomized set of possible combinations of engagement groups.
     */
    fun randomEngagementGroups(): Set<String>? {
        return engagementGroups?.mapNotNull { it.randomElement() }?.toSet()
    }

    /**
     * @return the start of the expiration time window
     */
    fun startTimeWindow(now: LocalDateTime): LocalDateTime {
        return now.minusSeconds(expiresLimit)
    }
}

/**
 * @return a random element in the set if any exist
 */
fun <T> Set<T>?.randomElement(): T? {
    if (this == null) {
        return null
    }
    if (isEmpty()) {
        return null
    }
    return elementAt(Random().nextInt(size))
}