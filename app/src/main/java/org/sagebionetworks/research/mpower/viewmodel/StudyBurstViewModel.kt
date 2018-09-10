package org.sagebionetworks.research.mpower.viewmodel

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.ViewModelProviders
import android.content.Context.MODE_PRIVATE
import android.support.annotation.VisibleForTesting
import android.support.v4.app.FragmentActivity
import com.google.gson.reflect.TypeToken
import org.sagebionetworks.bridge.rest.RestUtils
import org.sagebionetworks.research.mpower.research.DataSourceManager
import org.sagebionetworks.research.mpower.research.MpIdentifier.*
import org.sagebionetworks.research.mpower.research.StudyBurstConfiguration
import org.sagebionetworks.research.sageresearch.dao.room.ScheduledActivityEntity
import org.sagebionetworks.research.sageresearch.extensions.endOfDay
import org.sagebionetworks.research.sageresearch.extensions.inSameDayAs
import org.sagebionetworks.research.sageresearch.extensions.startOfDay
import org.sagebionetworks.research.sageresearch.manager.ActivityGroup
import org.sagebionetworks.research.sageresearch.viewmodel.ScheduleViewModel
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime

open class StudyBurstViewModel(app: Application): ScheduleViewModel(app) {

    companion object {
        @JvmStatic
        fun create(activity: FragmentActivity): StudyBurstViewModel {
            return ViewModelProviders.of(activity).get(StudyBurstViewModel::class.java)
        }
    }

    private val prefs = app.getSharedPreferences("StudyBurstViewModel", MODE_PRIVATE)

    // TODO: mdephillips 9/8/18 get this from bridge config
    @VisibleForTesting
    protected open val config = StudyBurstConfiguration()

    // TODO: mdephillips 9/8/18 get this from bridge config
    @VisibleForTesting
    protected open fun activityGroup(): ActivityGroup? {
        return DataSourceManager.installedGroup(config.taskGroupIdentifier)
    }

    /**
     * The total number of activities in a study burst task
     */
    private val totalActivitiesCount: Int get() {
        return activityGroup()?.activityIdentifiers?.size ?: 1
    }

    @VisibleForTesting
    protected open fun now(): LocalDateTime {
        return LocalDateTime.now()
    }

    /**
     * Today's query will check for all of today's activity schedules that are unfinished or finished today
     * @return the start and end range for the query
     */
    private fun todayQuery(): Pair<LocalDateTime, LocalDateTime> {
        return Pair(now().startOfDay(), now().endOfDay())
    }

    /**
     * Study burst query will check for all the study burst completed tasks within the number of day range
     * @return the start and end range for the query
     */
    private fun studyBurstQuery(): Pair<LocalDateTime, LocalDateTime> {
        val numberOfDays = 2L * config.numberOfDays
        return Pair(
                now().startOfDay().minusDays(numberOfDays),
                now().startOfDay().plusDays(numberOfDays).minusNanos(1))
    }

    @VisibleForTesting
    protected open var completedBurstsLiveData: LiveData<List<ScheduledActivityEntity>>? = null
    @VisibleForTesting
    protected open var todayLiveData: LiveData<List<ScheduledActivityEntity>>? = null
    @VisibleForTesting
    protected open var studyBurstLiveData: MediatorLiveData<StudyBurstItem>? = null
    /**
     * Fetches the schedules for all activities available today and also all study bursts completed in the past.
     * @return the live data for history item updates, will always be the same live data object
     */
    fun liveData(): LiveData<StudyBurstItem> {
        val liveDataChecked = studyBurstLiveData ?: {
            val mediator = MediatorLiveData<StudyBurstItem>()

            // Add the live data request for activities available today
            val todayData = todayLiveData ?: createActivityGroupAvailableBetween(
                    activityGroup()?.activityIdentifiers ?: setOf(), todayQuery())
            todayLiveData = todayData
            mediator.addSource(todayData) {
                consolidateFromCurrentValues()?.let {
                    mediator.postValue(it)
                }
            }

            // Add the live data request for study burst completed activities
            val completedData = completedBurstsLiveData ?: createActivityGroupAvailableBetween(
                    setOf(STUDY_BURST_COMPLETED), studyBurstQuery())
            completedBurstsLiveData = completedData
            mediator.addSource(completedData) {
                consolidateFromCurrentValues()?.let {
                    mediator.postValue(it)
                }
            }
            mediator
        }.invoke()
        studyBurstLiveData = liveDataChecked
        return liveDataChecked
    }

    /**
     * @param items schedule items from live data query
     * @return a list of history items derived from today's finished schedules
     */
    private fun consolidateFromCurrentValues(): StudyBurstItem?  {
        // We can only consolidate and create a study burst item if we have all the request
        val todaySchedules = todayLiveData?.value ?: return null
        val completedBurstSchedules = completedBurstsLiveData?.value ?: return null
        val schedules = listOf(todaySchedules, completedBurstSchedules).flatten()
        val filteredFinishedOn = filterFinishedSchedules(schedules)

        // Find the study burst tasks and calculate the state
        val todayStart = todayQuery().first
        val studyBurstMarkerId = config.identifier
        val studyMarker = schedules.firstOrNull {
            it.activityIdentifier() == studyBurstMarkerId &&
                    (it.scheduledOn?.inSameDayAs(todayStart) ?: false)
        }
        val markerSchedules = schedules.filter {
            studyMarker?.activityIdentifier() == it.activityIdentifier()
        }
        val pastSchedules = markerSchedules.filter {
            it.scheduledOn?.isBefore(todayStart) ?: false
        }

        val dayCount = pastSchedules.size + 1
        val missedDayCount = pastSchedules
                .map { if (it.finishedOn == null) 1 else 0 }
                .reduce { sum, element -> sum + element }
        val finishedCount = dayCount - missedDayCount
        val hasStudyBurst = (dayCount <= config.numberOfDays) ||
                ((finishedCount < config.minimumRequiredDays) && shouldContinueStudyBurst())

        val maxDaysCount = markerSchedules.size
        val pastDaysCount = pastSchedules.size
        val studyBurstDayCount = if (hasStudyBurst) dayCount else null
        val studyBurstMissedDaysCount = if (hasStudyBurst) missedDayCount else 0

        var expiresOn: Instant? = null
        if (studyMarker != null && studyMarker.finishedOn == null) {
            if (totalActivitiesCount == filteredFinishedOn.first?.size &&
                    filteredFinishedOn.third != null) {
                markCompleted(studyMarker, filteredFinishedOn)
            } else {
                expiresOn = filteredFinishedOn.third?.plusSeconds(config.expiresLimit)
            }
        }

        return StudyBurstItem(
                config, activityGroup(), schedules, filteredFinishedOn.first ?: listOf(),
                expiresOn, pastDaysCount, studyBurstDayCount, studyBurstMissedDaysCount, hasStudyBurst)
    }

    //studyMarker: SBBScheduledActivity, startedOn: Date?, finishedOn: Date, finishedSchedules: [SBBScheduledActivity]) {
    private fun markCompleted(
            studyMarker: ScheduledActivityEntity,
            filteredFinishedSchedules: Triple<List<ScheduledActivityEntity>?, Instant?, Instant?>) {

        studyMarker.startedOn = filteredFinishedSchedules.second ?: toInstant(now())
        studyMarker.finishedOn = filteredFinishedSchedules.third

        // TODO: mdephillips 9/10/18 archive, upload, and update schedule on bridge
//        let identifier = studyMarker.activityIdentifier ?? RSDIdentifier.studyBurstCompletedTask.stringValue
//        let schemaInfo: RSDSchemaInfo = {
//            guard let info = self.schemaInfo(for: identifier) else {
//            assertionFailure("Failed to retrieve schema info for \(String(describing: studyMarker.activityIdentifier))")
//            return RSDSchemaInfoObject(identifier: identifier, revision: 1)
//        }
//            return info
//        }()
//
//        do {
//
//            // build the archive
//            let archive = SBAScheduledActivityArchive(identifier: identifier, schemaInfo: schemaInfo, schedule: studyMarker)
//            var json: [String : Any] = [ "taskOrder" : self.orderedTasks.map { $0.identifier }.joined(separator: ",")]
//            finishedSchedules.forEach {
//                guard let identifier = $0.activityIdentifier, let finishedOn = $0.finishedOn else { return }
//                json["\(identifier).startDate"] = ($0.startedOn ?? today()).jsonObject()
//                json["\(identifier).endDate"] = finishedOn.jsonObject()
//                json["\(identifier).scheduleGuid"] = $0.guid
//            }
//            archive.insertDictionary(intoArchive: json, filename: "tasks", createdOn: finishedOn)
//
//            try archive.completeArchive(createdOn: finishedOn, with: nil)
//                studyMarker.clientData = json as NSDictionary
//
//                self.offMainQueue.async {
//                    archive.encryptAndUploadArchive()
//                }
//            }
//            catch let err {
//                debugPrint("Failed to archive the study burst data. \(err)")
//            }
//
//            self.sendUpdated(for: [studyMarker])
    }

    /**
     * @return if the user be shown more days of the study burst beyond the initial 14 days.
     */
    protected open fun shouldContinueStudyBurst(): Boolean {
        // TODO: syoung 06/28/2018 Implement logic to manage saving state and asking the user if they want
        // to see more days of the study rather than just assuming that they should be shown more days.
        return true
    }

    /**
     * @param schedules full schedule list from all queries
     * @param gracePeriod time in seconds of the start time window calculation
     *                    to allow for the time it takes to complete a schedule
     * @return Triple containing the filtered schedules, and the window for study burst completion expiration
     */
    fun filterFinishedSchedules(
            schedules: List<ScheduledActivityEntity>,
            gracePeriod: Long = 0): Triple<List<ScheduledActivityEntity>?, Instant?, Instant?> {

        var taskSchedules = mutableListOf<ScheduledActivityEntity>()
        val activityIdentifiers =
                activityGroup()?.activityIdentifiers ?:
                return Triple(null, null, null)
        val now = now()
        var finishedOn: Instant? = null
        var startedOn: Instant? = null
        schedules.forEach { schedule ->
            val activityId = schedule.activityIdentifier() ?: return@forEach
            if (!activityIdentifiers.contains(activityId)) return@forEach
            val scheduleFinished = schedule.finishedOn ?: return@forEach
            if (!scheduleFinished.inSameDayAs(now, timezone)) return@forEach
            val isNewer = null != taskSchedules.find {
                val existingFinishedOn = it.finishedOn ?: return@find false
                schedule.activityIdentifier() == it.activityIdentifier() &&
                        scheduleFinished.isAfter(existingFinishedOn)
            }
            if (!isNewer) return@forEach
            taskSchedules.find { schedule.activityIdentifier() == it.activityIdentifier() }?.let {
                taskSchedules.remove(it)
            }
            taskSchedules.add(schedule)
            if (finishedOn == null || scheduleFinished.isAfter(finishedOn)) {
                finishedOn = scheduleFinished
                startedOn = schedule.startedOn
            }
        }

        // If all the tasks are not finished then remove the ones that are outside the 1 hour window.
        if (taskSchedules.size < activityIdentifiers.size) {
            val startWindow = toInstant(config.startTimeWindow(now.minusSeconds(gracePeriod)))
            taskSchedules = taskSchedules.filter {
                val finishedChecked = it.finishedOn ?: return@filter false
                finishedChecked.isAfter(startWindow)
            }.toMutableList()
        }

        return Triple(taskSchedules, startedOn, finishedOn)
    }

    val orderKey = "StudyBurstTaskOrder"
    val timestampKey = "StudyBurstTimestamp"
    /**
     * @param sortOrder a set of strings that will determine study burst take execution order
     * @param timestamp the time when the sort order was last set
     */
    @VisibleForTesting
    protected open fun setOrderedTasks(sortOrder: List<String>, timestamp: LocalDateTime) {
        val editPrefs = prefs.edit()
        editPrefs.putString(orderKey, RestUtils.GSON.toJson(sortOrder))
        editPrefs.putString(timestampKey, timestamp.toString())
        editPrefs.apply()
    }

    @VisibleForTesting
    protected open fun getTaskSortOrder(): List<String>? {
        prefs.getString(orderKey, null)?.let {
            return RestUtils.GSON.fromJson(it, object : TypeToken<List<String>>() {}.type)
        } ?: return null
    }

    @VisibleForTesting
    protected open fun getTaskSortOrderTimestamp(): LocalDateTime? {
        prefs.getString(timestampKey, null)?.let {
            return LocalDateTime.parse(it)
        } ?: return null
    }
}

data class StudyBurstItem(
        /**
         * @property studyBurstConfiguration for the ViewModel
         */
        val studyBurstConfiguration: StudyBurstConfiguration = StudyBurstConfiguration(),
        /**
         * @property activityGroup that needs completed to finished a day's study burst
         */
        val activityGroup: ActivityGroup?,
        /**
         * @property schedules the list of schedules used to build this study burst item
         */
        val schedules: List<ScheduledActivityEntity>,
        /**
         * @property Subset of the finished schedules.
         */
        val finishedSchedules: List<ScheduledActivityEntity>,
        /**
         * @property expiresOn When does the study burst expire?
         */
        val expiresOn: Instant? = null,
        /**
         * @property pastDaysCount The count of past days in a study burst.
         */
        val pastDaysCount: Int,
        /**
         * @property dayCount What day of the study burst should be displayed?
         */
        val dayCount: Int?,
        /**
         * @property missedDayCount Number of days in the study burst that were missed.
         */
        val missedDayCount: Int,
        /**
         * @property hasStudyBurst Is there an active study burst?
         */
        val hasStudyBurst: Boolean) {

    /**
     * @return progress What is the current progress on required activities?
     */
    val progress: Float get() {
        return 1.0f
    }

    /**
     * @return total number of activities.
     */
    val totalActivitiesCount: Int get() {
        return activityGroup?.activityIdentifiers?.size ?: 1
    }
}

