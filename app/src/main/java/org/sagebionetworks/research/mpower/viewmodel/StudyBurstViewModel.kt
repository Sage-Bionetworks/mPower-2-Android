package org.sagebionetworks.research.mpower.viewmodel

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.support.annotation.DrawableRes
import android.support.annotation.VisibleForTesting
import android.support.v4.app.FragmentActivity
import com.google.gson.reflect.TypeToken
import org.sagebionetworks.bridge.rest.RestUtils
import org.sagebionetworks.bridge.rest.model.ScheduledActivity
import org.sagebionetworks.research.domain.result.AnswerResultType.DATE
import org.sagebionetworks.research.domain.result.AnswerResultType.STRING
import org.sagebionetworks.research.domain.result.implementations.AnswerResultBase
import org.sagebionetworks.research.domain.result.implementations.TaskResultBase
import org.sagebionetworks.research.domain.result.interfaces.AnswerResult
import org.sagebionetworks.research.domain.result.interfaces.Result
import org.sagebionetworks.research.mpower.R
import org.sagebionetworks.research.mpower.research.CompletionTask
import org.sagebionetworks.research.mpower.research.DataSourceManager
import org.sagebionetworks.research.mpower.research.MpIdentifier.*
import org.sagebionetworks.research.mpower.research.MpTaskInfo.Tapping
import org.sagebionetworks.research.mpower.research.MpTaskInfo.Tremor
import org.sagebionetworks.research.mpower.research.MpTaskInfo.WalkAndBalance
import org.sagebionetworks.research.mpower.research.StudyBurstConfiguration
import org.sagebionetworks.research.sageresearch.dao.room.ScheduledActivityEntity
import org.sagebionetworks.research.sageresearch.extensions.availableToday
import org.sagebionetworks.research.sageresearch.extensions.endOfDay
import org.sagebionetworks.research.sageresearch.extensions.filterByActivityId
import org.sagebionetworks.research.sageresearch.extensions.inSameDayAs
import org.sagebionetworks.research.sageresearch.extensions.scheduleClosestToNow
import org.sagebionetworks.research.sageresearch.extensions.startOfDay
import org.sagebionetworks.research.sageresearch.extensions.toInstant
import org.sagebionetworks.research.sageresearch.manager.ActivityGroup
import org.sagebionetworks.research.sageresearch.manager.TaskInfo
import org.sagebionetworks.research.sageresearch.viewmodel.ScheduleViewModel
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import java.lang.Integer.MAX_VALUE
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

open class StudyBurstViewModel(app: Application): ScheduleViewModel(app) {

    companion object {
        @JvmStatic
        fun create(activity: FragmentActivity): StudyBurstViewModel {
            return ViewModelProviders.of(activity).get(StudyBurstViewModel::class.java)
        }

        internal val defaultTaskSortOrder: List<String> get() {
            return DataSourceManager.measuringGroup.activityIdentifiers.toList()
        }

        internal val defaultTaskInfoSortOrder: List<TaskInfo> get() {
            return listOf(Tapping, WalkAndBalance, Tremor)
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
            var activityIdentifiers = activityGroup()?.activityIdentifiers?.
                    union(config.completionTaskIdentifiers())
            val todayData = todayLiveData ?: createActivityGroupAvailableBetween(
                    activityIdentifiers ?: setOf(), todayQuery())
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
        val item = createStudyBurstItem(schedules)

        // Check for a new state where the study burst was just completed.
        if (item.studyBurstWasJustCompleted) {
            item.studyBurstMarker?.let {
                markCompleted(item, it)
            }
        }

        return item
    }

    @VisibleForTesting
    protected open fun createStudyBurstItem(schedules: List<ScheduledActivityEntity>): StudyBurstItem {
        return StudyBurstItem(
                config, activityGroup(), schedules, now(), timezone,
                shouldContinueStudyBurst(), getTaskSortOrder())
    }

    /**
     * @param studyMarker to mark as complete on bridge and to upload the result info to s3
     */
    private fun markCompleted(item: StudyBurstItem, studyMarker: ScheduledActivityEntity) {
        val identifier = studyMarker.activityIdentifier() ?: STUDY_BURST_COMPLETED
        val nowInstant = now().toInstant(timezone)

        // Creating the uuid through this function will make sure it's registered in the ScheduleRepository
        val uuid = createScheduleTaskRunUuid(studyMarker)

        val results = ArrayList<Result>()

        // Add task order result
        results.add(AnswerResultBase(
                "tasks.taskOrder", nowInstant, nowInstant,
                getTaskSortOrder().joinToString(), STRING))

        // json filename must be tasks, can I just prefix with "tasks." instead? TODO: mdephillips 10/9/18
        // Fill in info about the finished tasks' guids and start/end dates
        item.finishedSchedules.forEach {
            it.activityIdentifier()?.let { identifier ->
                it.finishedOn?.let { finishedOn ->
                    val startDate: Instant = it.startedOn ?: nowInstant
                    results.add(AnswerResultBase(
                            "tasks.$identifier.startDate", startDate, startDate, startDate, DATE))
                    val endDate: Instant = it.finishedOn ?: nowInstant
                    results.add(AnswerResultBase(
                            "tasks.$identifier.endDate", endDate, endDate, endDate, DATE))
                    results.add(AnswerResultBase(
                            "tasks.$identifier.scheduleGuid", nowInstant, nowInstant, it.guid, STRING))
                }
            }
        }

        val taskResult = TaskResultBase(
                identifier, studyMarker.startedOn ?: nowInstant,
                studyMarker.finishedOn, uuid, null, results, results)

        scheduleRepo.uploadTaskResult(taskResult)
    }

    /**
     * @return if the user be shown more days of the study burst beyond the initial 14 days.
     */
    protected open fun shouldContinueStudyBurst(): Boolean {
        // TODO: syoung 06/28/2018 Implement logic to manage saving state and asking the user if they want
        // to see more days of the study rather than just assuming that they should be shown more days.
        return true
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

    /**
     * @return true if sort order hasn't been set yet or it is from yesterday, false otherwise
     */
    protected fun isTaskSortOrderStale(): Boolean {
        val sortOrderDate = getTaskSortOrderTimestamp() ?: return true
        return sortOrderDate.inSameDayAs(now())
    }

    @VisibleForTesting
    protected open fun getTaskSortOrder(): List<String> {
        // Check for stale sort order and update if appropriate
        if (isTaskSortOrderStale()) {
            activityGroup()?.activityIdentifiers?.toList()?.shuffled()?.let {
                setOrderedTasks(it, now())
                return it
            } ?: return defaultTaskSortOrder
        }
        prefs.getString(orderKey, null)?.let {
            return RestUtils.GSON.fromJson(it, object : TypeToken<List<String>>() {}.type)
        } ?: return defaultTaskSortOrder
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
         * @property config for the ViewModel
         */
        val config: StudyBurstConfiguration = StudyBurstConfiguration(),
        /**
         * @property activityGroup that needs completed to finished a day's study burst
         */
        val activityGroup: ActivityGroup?,
        /**
         * @property schedules the list of schedules used to build this study burst item
         */
        val schedules: List<ScheduledActivityEntity>,
        /**
         * @property the date used to create this study burst item
         */
        private val date: LocalDateTime,
        /**
         * @property the timezone used to create this study burst item
         */
        private val timezone: ZoneId,
        /**
         * @property shouldContinueStudyBurst if the user be shown more days of the study burst beyond the initial 14 days.
         */
        private val shouldContinueStudyBurst: Boolean,
        /**
         * @property studyBurstTasksSortOrder order of the study burst tasks
         */
        val studyBurstTasksSortOrder: List<String>) {

    /**
     * @property orderedTasks the sorted task info objects that need done for a study burst's day to be completed.
     */
    val orderedTasks: List<StudyBurstTaskInfo>
    /**
     * @property finishedSchedules subset of the finished schedules.
     */
    val finishedSchedules: List<ScheduledActivityEntity>
    /**
     * @property expiresOn When does the study burst expire?
     */
    val expiresOn: Instant?
    /**
     * @property pastDaysCount The count of past days in a study burst.
     */
    val pastDaysCount: Int
    /**
     * @property dayCount What day of the study burst should be displayed?
     */
    val dayCount: Int?
    /**
     * @property missedDayCount Number of days in the study burst that were missed.
     */
    val missedDayCount: Int
    /**
     * @property hasStudyBurst Is there an active study burst?
     */
    val hasStudyBurst: Boolean
    /**
     * @property shouldMarkStudyBurstAsCompleted true if the view model should mark the study burst as completed
     */
    internal val shouldMarkStudyBurstAsCompleted: Boolean
    /**
     * @property studyBurstMarker the schedule for marking the study burst as complete
     */
    val studyBurstMarker: ScheduledActivityEntity?
    /*
     * @property studyBurstWasJustCompleted true if the last study burst activity was just finished, false otherwise
     */
    val studyBurstWasJustCompleted: Boolean

    init {
        val filteredFinishedOn = filterFinishedSchedules(schedules)

        finishedSchedules = filteredFinishedOn.first ?: listOf()
        // Find the study burst tasks and calculate the state
        val todayStart = date.startOfDay()
        studyBurstMarker = schedules.firstOrNull {
            it.activityIdentifier() == config.identifier &&
                    (it.scheduledOn?.inSameDayAs(date) ?: false)
        }
        val markerSchedules = schedules.filter {
            studyBurstMarker?.activityIdentifier() == it.activityIdentifier()
        }
        val pastSchedules = markerSchedules.filter {
            it.scheduledOn?.isBefore(todayStart) ?: false
        }

        val dayCount = pastSchedules.size + 1
        val missedDayCount = pastSchedules
                .map { if (it.finishedOn == null) 1 else 0 }.sum()
        val finishedCount = dayCount - missedDayCount
        val hasStudyBurst = (studyBurstMarker != null && ((dayCount <= config.numberOfDays) ||
                ((finishedCount < config.minimumRequiredDays) && shouldContinueStudyBurst)))

        val newMaxDayCount = markerSchedules.size
        if (newMaxDayCount > 0) {
            config.maxDayCount = markerSchedules.size
        }
        pastDaysCount = pastSchedules.size
        this.dayCount = if (hasStudyBurst && studyBurstMarker != null) dayCount else null
        this.missedDayCount = if (hasStudyBurst) missedDayCount else 0

        val todayStudyMarkerNotFinished =
                (studyBurstMarker != null && studyBurstMarker.finishedOn == null)
        val allTodaysTaskCompletedWithinFinishedOn =
                (totalActivitiesCount == filteredFinishedOn.first?.size && filteredFinishedOn.third != null)

        shouldMarkStudyBurstAsCompleted =
                (todayStudyMarkerNotFinished && allTodaysTaskCompletedWithinFinishedOn)

        expiresOn =
            if (studyBurstMarker != null && todayStudyMarkerNotFinished && !allTodaysTaskCompletedWithinFinishedOn) {
                filteredFinishedOn.third?.plusSeconds(config.expiresLimit)
            } else null

        this.hasStudyBurst = hasStudyBurst

        val sortedOrderedTasks = activityGroup?.tasks?.sortedWith(Comparator { o1, o2 ->
            studyBurstTasksSortOrder.indexOf(o1.identifier).compareTo(
                    studyBurstTasksSortOrder.indexOf(o2.identifier))
        }) ?: StudyBurstViewModel.defaultTaskInfoSortOrder
        var firstUnfinishedReached = false
        orderedTasks = sortedOrderedTasks.map {
            val schedule = schedules.filterByActivityId(it.identifier).availableToday()?.scheduleClosestToNow()
            val isFinished = isStudyBurstTaskFinished(it.identifier)
            val isActive = isFinished || !firstUnfinishedReached
            if (!firstUnfinishedReached) {
                firstUnfinishedReached = !isFinished
            }
            // Tasks are active and can be run if they are finished or they are the first unfinished in the list
            StudyBurstTaskInfo(schedule, it, isActive, isFinished)
        }

        studyBurstWasJustCompleted =
                isCompletedForToday &&
                studyBurstMarker != null &&
                studyBurstMarker.finishedOn == null
        // Computed property isCompletedForToday should be accessed last
        if (studyBurstWasJustCompleted) {
            // Only assign the study burst marker start/finish dates if the study burst was just finished
            studyBurstMarker?.startedOn = filteredFinishedOn.second ?: date.toInstant(timezone)
            studyBurstMarker?.finishedOn = filteredFinishedOn.third
        }
    }

    /**
     * @return progress What is the current progress on required activities?
     */
    val progress: Float get() {
        return finishedSchedules.size.toFloat() / totalActivitiesCount.toFloat()
    }

    /**
     * @property totalActivitiesCount total number of activities.
     */
    val totalActivitiesCount: Int get() {
        return activityGroup?.activityIdentifiers?.size ?: 1
    }

    /**
     * @property isCompletedForToday Is the Study burst completed for today?
     */
    val isCompletedForToday: Boolean get() {
        return !hasStudyBurst || (finishedSchedules.size == totalActivitiesCount)
    }

    /**
     * @property isLastDay Is this the last day of the study burst?
     */
    val isLastDay: Boolean get() {
        if (dayCount == null) return false
        if (!hasStudyBurst) return false
        val days = (pastDaysCount - missedDayCount) + 1
        return (days >= config.maxDayCount) ||
                ((dayCount >= config.numberOfDays) && (days >= config.minimumRequiredDays)) ||
                ((dayCount >= config.numberOfDays) && !shouldContinueStudyBurst)
    }

    /**
     * @return calculateThisDay calculated as the reference day for which surveys are available
     */
    fun calculateThisDay(): Int {
        if (!hasStudyBurst) return config.maxDayCount + 1
        val day = dayCount ?: return config.maxDayCount + 1
        if (day < config.numberOfDays) {
            return day
        } else {
            return if (isLastDay) config.numberOfDays else (pastDaysCount - missedDayCount) + 1
        }
    }

    /**
     * @property pastCompletionSurveys the set of survey identifiers that are available during this day of the study burst
     */
    val pastCompletionSurveys: LinkedHashSet<String> get() {
        val sortedIdentifiers = pastCompletionTasks.flatMap { task ->
            // Look to see if there is a report and include if *not* finished.
            val identifiers = task.activityIdentifiers.filter { identifier ->
                val schedule = schedules.firstOrNull { identifier == it.activityIdentifier() }
                !hasReport(schedule)
            }
            val sortedIdentifiers = identifiers.sortedWith(Comparator { lhs, rhs ->
                var lIdx = task.activityIdentifiers.indexOfFirst { lhs == it }
                if (lIdx == -1) lIdx = MAX_VALUE
                var rIdx = task.activityIdentifiers.indexOfFirst { rhs == it }
                if (rIdx == -1) rIdx = MAX_VALUE
                lIdx.compareTo(rIdx)
            })
            sortedIdentifiers
        }
        val sortedSet = linkedSetOf<String>()
        sortedIdentifiers.forEach { sortedSet.add(it) }
        return sortedSet
    }

    /**
     * @property pastCompletionTasks the set of activity identifiers that are available during this day of the study burst
     */
    val pastCompletionTasks: Set<CompletionTask> get() {
        val thisDay = calculateThisDay()
        return config.completionTasks.filter { it.day < thisDay }.toSet()
    }

    /**
     *  @property todayCompletionTask the tasks that need completed before the day's study burst activities
     */
    val todayCompletionTask: CompletionTask? get() {
        val thisDay = calculateThisDay()
        return config.completionTasks.firstOrNull { it.day == thisDay }
    }

    /**
     * @property nextCompletionActivityToShow based on the day and finished status of the completion tasks
     */
    val nextCompletionActivityToShow: ScheduledActivityEntity? get() {
        pastUnfinishedCompletionActivity?.let { return it }
        todayUnfinishedCompletionActivity?.let { return it }
        return null
    }

    /**
     * @property todayUnfinishedCompletionActivity based on the day and finished status of the completion tasks
     */
    val todayUnfinishedCompletionActivity: ScheduledActivityEntity? get() {
        todayCompletionTask?.activityIdentifiers?.forEach {
            if (!hasCompletionTaskActivityBeenFinished(it)) {
                schedules.filterByActivityId(it).firstOrNull()?.let {
                    return it
                }
            }
        }
        return null
    }

    /**
     * @property pastUnfinishedCompletionActivity based on the day and finished status of the completion tasks
     */
    val pastUnfinishedCompletionActivity: ScheduledActivityEntity? get() {
        pastCompletionTasks.forEach {
            it.activityIdentifiers.forEach {
                if (!hasCompletionTaskActivityBeenFinished(it)) {
                    schedules.filterByActivityId(it).firstOrNull()?.let {
                        return it
                    }
                }
            }
        }
        return null
    }

    /**
     * @param activityIdentifier to check for finished in schedules,
     *                           this should only be called on CompletionTask activity identifiers
     * @return if the completion task activity has been finished
     */
    private fun hasCompletionTaskActivityBeenFinished(activityIdentifier: String): Boolean {
        schedules.filterByActivityId(activityIdentifier).firstOrNull()?.let {
            return hasReport(it)
        } ?: return false
    }

    /**
     * @return true if use has done their motivation survey already, false otherwise
     */
    fun hasCompletedMotivationSurvey(): Boolean {
        return hasReport(schedules.filterByActivityId(config.motivationIdentifier).firstOrNull())
    }

    private fun hasReport(schedule: ScheduledActivityEntity?): Boolean {
        if (schedule == null) return false
        // TODO: mdephillips: ios code has this as the finished calculation
        // let finished = self.reports.contains(where: { $0.identifier == identifier})
        // For now, let's just return if the finishedOn date is set
        return schedule.finishedOn != null
    }

    /**
     * @param schedules full schedule list from all queries
     * @param gracePeriod time in seconds of the start time window calculation
     *                    to allow for the time it takes to complete a schedule
     * @return Triple containing the filtered schedules, and the window for study burst completion expiration
     */
    private fun filterFinishedSchedules(
            schedules: List<ScheduledActivityEntity>,
            gracePeriod: Long = 0): Triple<List<ScheduledActivityEntity>?, Instant?, Instant?> {

        var taskSchedules = mutableListOf<ScheduledActivityEntity>()
        val activityIdentifiers =
                activityGroup?.activityIdentifiers ?:
                return Triple(null, null, null)
        val now = date
        var finishedOn: Instant? = null
        var startedOn: Instant? = null
        schedules.forEach { schedule ->
            val activityId = schedule.activityIdentifier() ?: return@forEach
            if (!activityIdentifiers.contains(activityId)) return@forEach
            val scheduleFinished = schedule.finishedOn ?: return@forEach
            if (!scheduleFinished.inSameDayAs(now, timezone)) return@forEach
            val isNewer = (null == taskSchedules.find {
                val itFinished = it.finishedOn ?: return@find false
                schedule.activityIdentifier() == it.activityIdentifier() &&
                        itFinished.isAfter(scheduleFinished)
            })
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
            val startWindow = config.startTimeWindow(now.minusSeconds(gracePeriod)).toInstant(timezone)
            taskSchedules = taskSchedules.filter {
                val finishedChecked = it.finishedOn ?: return@filter false
                finishedChecked.isAfter(startWindow)
            }.toMutableList()
        }

        return Triple(taskSchedules, startedOn, finishedOn)
    }

    /**
     * @return the "unfinished" schedule if there is a past schedule that is following the user or
     *         else the user has completed their study burst activities for today.
     */
    fun getUnfinishedSchedule(): Pair<String, String?>? {
        (pastUnfinishedCompletionActivity ?:
            if (isCompletedForToday) todayUnfinishedCompletionActivity
            else null)?.let {
            return Pair(it.activity?.label ?: "", it.activity?.labelDetail)
        }
        return null
    }

    /**
     * @return action bar item with information for showing the study burst action bar.
     */
    fun getActionBarItem(context: Context): TodayActionBarItem? {
        if (!isCompletedForToday) {
            val title = context.getString(R.string.study_burst_action_bar_title)
            var details: String? = null
            expiresOn?.let {
                details = timeUntilStudyBurstExpiration
            } ?: run {
                val activitiesTodoCount = totalActivitiesCount - finishedSchedules.size
                details = context.getString(R.string.study_burst_activities_to_do).format(activitiesTodoCount)
            }
            return TodayActionBarItem(title, details, null)
        }
        getUnfinishedSchedule()?.let {
            return TodayActionBarItem(it.first, it.second, null)
        }
        return null
    }

    val millisToExpiration: Long? get() {
        expiresOn?.let {
            return it.toEpochMilli() - Instant.now().toEpochMilli()
        } ?: return null
    }

    val timeUntilStudyBurstExpiration: String? get() {
        millisToExpiration?.let {
            val secondsToExpiration = it / 1000
            val progressDate = ZonedDateTime.now().startOfDay().plusSeconds(secondsToExpiration)
            val dateEpochMillis = progressDate.toEpochSecond() * 1000
            val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            return formatter.format(Date(dateEpochMillis))
        } ?: return null
    }

    /**
     * @return true if the task is finished and is included in the finishedSchedules, false otherwise
     */
    fun isStudyBurstTaskFinished(taskIdentifier: String): Boolean {
        return null != finishedSchedules.firstOrNull { it.activityIdentifier() == taskIdentifier }
    }
}

data class TodayActionBarItem(
        val title: String,
        val detail: String?,
        @DrawableRes val image: Int?)

data class StudyBurstTaskInfo(
        val schedule: ScheduledActivityEntity?,
        val task: TaskInfo,
        val isActive: Boolean,
        val isComplete: Boolean)

