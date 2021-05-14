package org.sagebionetworks.research.mpower.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.VisibleForTesting
import com.google.common.base.Preconditions.checkArgument
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.sagebionetworks.researchstack.backbone.model.TaskModel
import org.sagebionetworks.bridge.rest.RestUtils
import org.sagebionetworks.research.mpower.R
import org.sagebionetworks.research.mpower.research.CompletionTask
import org.sagebionetworks.research.mpower.research.DataSourceManager
import org.sagebionetworks.research.mpower.research.MpIdentifier.BACKGROUND
import org.sagebionetworks.research.mpower.research.MpIdentifier.DEMOGRAPHICS
import org.sagebionetworks.research.mpower.research.MpIdentifier.HEART_SNAPSHOT
import org.sagebionetworks.research.mpower.research.MpIdentifier.STUDY_BURST_COMPLETED
import org.sagebionetworks.research.mpower.research.MpIdentifier.STUDY_BURST_COMPLETED_UPLOAD
import org.sagebionetworks.research.mpower.research.MpIdentifier.STUDY_BURST_REMINDER
import org.sagebionetworks.research.mpower.research.MpTaskInfo.HeartSnapshot
import org.sagebionetworks.research.mpower.research.MpTaskInfo.Tapping
import org.sagebionetworks.research.mpower.research.MpTaskInfo.Tremor
import org.sagebionetworks.research.mpower.research.MpTaskInfo.WalkAndBalance
import org.sagebionetworks.research.mpower.research.StudyBurstConfiguration
import org.sagebionetworks.research.mpower.researchstack.MpResearchStackArchiveFactory
import org.sagebionetworks.research.mpower.researchstack.framework.MpDataProvider
import org.sagebionetworks.research.sageresearch.dao.room.ReportRepository
import org.sagebionetworks.research.sageresearch.dao.room.ScheduleRepository
import org.sagebionetworks.research.sageresearch.dao.room.ScheduledActivityEntity
import org.sagebionetworks.research.sageresearch.dao.room.ScheduledActivityEntityDao
import org.sagebionetworks.research.sageresearch.extensions.availableToday
import org.sagebionetworks.research.sageresearch.extensions.endOfDay
import org.sagebionetworks.research.sageresearch.extensions.filterByActivityId
import org.sagebionetworks.research.sageresearch.extensions.inSameDayAs
import org.sagebionetworks.research.sageresearch.extensions.scheduleClosestToNow
import org.sagebionetworks.research.sageresearch.extensions.startOfDay
import org.sagebionetworks.research.sageresearch.extensions.toInstant
import org.sagebionetworks.research.sageresearch.manager.ActivityGroup
import org.sagebionetworks.research.sageresearch.manager.TaskInfo
import org.sagebionetworks.research.sageresearch.viewmodel.ReportAndScheduleViewModel
import org.sagebionetworks.research.sageresearch.viewmodel.SingleLiveEvent
import org.sagebionetworks.researchstack.backbone.model.UserHealth.Gender
import org.sagebionetworks.researchstack.backbone.result.TaskResult
import org.sagebionetworks.researchstack.backbone.utils.StepResultHelper
import org.slf4j.LoggerFactory
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.ChronoUnit
import java.lang.Integer.MAX_VALUE
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.time.days

/**
 * The StudyBurstViewModel has access to all schedules and reports that are needed to manage the state
 * and to manipulate the state of the user's study burst progress.
 */
open class StudyBurstViewModel(
        scheduleDao: ScheduledActivityEntityDao,
        scheduleRepo: ScheduleRepository, reportRepo: ReportRepository,
        val studyBurstSettingsDao: StudyBurstSettingsDao) :
        ReportAndScheduleViewModel(scheduleDao, scheduleRepo, reportRepo) {

    private val logger = LoggerFactory.getLogger(
            ScheduleRepository::class.java)

    class Factory @Inject constructor(
            private val scheduledActivityEntityDao: ScheduledActivityEntityDao,
            private val scheduleRepository: ScheduleRepository,
            private val reportRepository: ReportRepository,
            private val studyBurstSettingsDao: StudyBurstSettingsDao) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            checkArgument(modelClass.isAssignableFrom(StudyBurstViewModel::class.java))

            return StudyBurstViewModel(scheduledActivityEntityDao,
                    scheduleRepository, reportRepository, studyBurstSettingsDao) as T
        }
    }

    companion object {
        internal val defaultTaskSortOrder: List<String> get() {
            return DataSourceManager.measuringGroup.activityIdentifiers.toList()
        }

        internal val defaultTaskInfoSortOrder: List<TaskInfo> get() {
            return listOf(Tapping, WalkAndBalance, Tremor, HeartSnapshot)
        }
    }

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

    init {
        // This could be improved by specifying the following lines of code globally after the dagger injections
        // However, that is done in SageResearchAppSDKModule which has many other dependencies
        // and I don't think we need to duplicate all these at this time -mdephillips 11/15/18
        val archiveFactory = MpResearchStackArchiveFactory()
        scheduleRepo.researchStackUploadArchiveFactory = archiveFactory
    }

    @VisibleForTesting
    protected open fun now(): LocalDateTime {
        return LocalDateTime.now()
    }

    /**
     * @property studyBurstBeingMarkedAsCompleted kept track so it's not marked twice
     */
    private var studyBurstBeingMarkedAsCompleted: ScheduledActivityEntity? = null

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

    val loadRsSurveyLiveData: SingleLiveEvent<TaskModel> = SingleLiveEvent()
    /**
     * Load a ResearchStack based survey from bridge
     * For response, please see @property rsSurveyLoadedLiveData and scheduleErrorLiveData
     * @param survey must be a survey, otherwise an error will be thrown
     * @return a LiveData<TaskModel> for easier consumption, but it is based on the Survey bridge class type
     */
    fun loadRsSurvey(survey: ScheduledActivityEntity) {
        compositeDispose.add(scheduleRepo.loadResearchStackSurvey(survey).toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    val taskModel = RestUtils.toType(it, TaskModel::class.java)
                    loadRsSurveyLiveData.value = taskModel
                    scheduleErrorLiveData.postValue(null)
                }, { t ->
                    scheduleErrorLiveData.postValue(t.localizedMessage)
                    loadRsSurveyLiveData.value = null
                }))
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
            val activityIdentifiers = activityGroup()?.activityIdentifiers?.
                    union(config.completionTaskIdentifiers())
            val todayData = todayLiveData ?: createActivityGroupAvailableBetween(
                    activityIdentifiers ?: setOf(), todayQuery())
            todayLiveData = todayData
            mediator.addSource(todayData) { _ ->
                consolidateFromCurrentValues()?.let {
                    mediator.postValue(it)
                }
            }

            // Add the live data request for study burst completed activities
            val completedData = completedBurstsLiveData ?: createActivityGroupAvailableBetween(
                    setOf(STUDY_BURST_COMPLETED), studyBurstQuery())
            completedBurstsLiveData = completedData
            mediator.addSource(completedData) { _ ->
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
     * @param lifecycleOwner that previously was observing the return of the liveData() function
     * @return a new LiveData object that is refreshed based on the current time of day
     */
    fun refreshLiveData(lifecycleOwner: LifecycleOwner): LiveData<StudyBurstItem> {
        liveData().removeObservers(lifecycleOwner)
        studyBurstLiveData = null
        return liveData()
    }

    /**
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

        logger.info("Posting LiveData update")
        return createStudyBurstItem(schedules)
    }

    @VisibleForTesting
    protected open fun createStudyBurstItem(schedules: List<ScheduledActivityEntity>): StudyBurstItem {
        return StudyBurstItem(
                config, activityGroup(), schedules, now(), timezone,
                shouldContinueStudyBurst(), studyBurstSettingsDao.getTaskSortOrder(),
                studyBurstSettingsDao, shouldShowHeartSnapshot())
    }

    open fun shouldShowHeartSnapshot(): Boolean {
        return MpDataProvider.getInstance().hasHeartSnapshotDataGroup()
    }

    /**
     * @param studyMarker to mark as complete on bridge and to upload the result info to s3
     */
    private fun markCompleted(item: StudyBurstItem, studyMarker: ScheduledActivityEntity) {

        if (studyBurstBeingMarkedAsCompleted != null) {
            return
        }

        // Use the upload identifier instead
        val taskResult = org.sagebionetworks.researchstack.backbone.result.TaskResult(STUDY_BURST_COMPLETED_UPLOAD)
        val stepId = "TaskStep" // can be anything, identifier not used in the upload
        val stepResult = org.sagebionetworks.researchstack.backbone.result.StepResult<Any>(
                org.sagebionetworks.researchstack.backbone.step.Step(stepId))
        studyMarker.finishedOn = Instant.now()

        stepResult.results["taskOrder"] = getTaskSortOrder().joinToString()
        // Fill in info about the finished tasks' guid
        item.finishedSchedules.forEach {
            it.activityIdentifier()?.let { identifier ->
                it.finishedOn?.let { _ ->
                    stepResult.results["$identifier.scheduleGuid"] = it.guid
                }
            }
        }

        taskResult.setStepResultForStepIdentifier(stepId, stepResult)

        studyBurstBeingMarkedAsCompleted = studyMarker
        updateScheduleToBridge(studyMarker)

        // The scheduleRepo is setup to use our custom StudyBurstResearchStackArchiveFactory
        // which will correctly archive the entire TaskResult into a single file "tasks"
        uploadResearchStackTaskResultToS3(studyMarker, taskResult)
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
     * @return true if sort order hasn't been set yet or it is from yesterday, false otherwise
     */
    private fun isTaskSortOrderStale(): Boolean {
        val sortOrderDate = studyBurstSettingsDao.getTaskSortOrderTimestamp() ?: return true
        return sortOrderDate.inSameDayAs(now())
    }

    @VisibleForTesting
    protected open fun getTaskSortOrder(): List<String> {
        // Check for stale sort order and update if appropriate
        if (isTaskSortOrderStale()) {
            activityGroup()?.activityIdentifiers?.toList()?.shuffled()?.let {
                studyBurstSettingsDao.setOrderedTasks(it, now())
                return it
            } ?: return defaultTaskSortOrder
        }
        return studyBurstSettingsDao.getTaskSortOrder()
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
         * @property date used to create this study burst item
         */
        private val date: LocalDateTime,
        /**
         * @property timezone used to create this study burst item
         */
        private val timezone: ZoneId,
        /**
         * @property shouldContinueStudyBurst if the user be shown more days of the study burst beyond the initial 14 days.
         */
        private val shouldContinueStudyBurst: Boolean,
        /**
         * @property studyBurstTasksSortOrder order of the study burst tasks
         */
        val studyBurstTasksSortOrder: List<String>,
        /**
         * @property studyBurstSettingsDao shared preferences used to save/load data
         */
        val studyBurstSettingsDao: StudyBurstSettingsDao,
        /**
         * @property shouldShowHeartSnapshot true if heart snapshot logic should be included, false otherwise
         */
        val shouldShowHeartSnapshot: Boolean) {

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
                (totalMeasuringActivitiesCount == filteredFinishedOn.first?.size && filteredFinishedOn.third != null)

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

        val mutableSorted = sortedOrderedTasks.toMutableList()
        if (shouldShowHeartSnapshot) {
            mutableSorted.add(HeartSnapshot) // Heart Snapshot always goes at the end
        }

        var firstUnfinishedReached = false
        orderedTasks = mutableSorted.map {
            val schedule = schedules.filterByActivityId(it.identifier).availableToday()?.scheduleClosestToNow()
            val isFinished = isStudyBurstTaskFinished(it.identifier)
            val isSkipped = studyBurstSettingsDao.isTaskSkippedForToday(it.identifier)
            val isActive = (isFinished || !firstUnfinishedReached) && !isSkipped
            if (!firstUnfinishedReached) {
                firstUnfinishedReached = (!isFinished && !isSkipped)
            }
            // Tasks are active and can be run if they are finished or they are the first unfinished in the list
            StudyBurstTaskInfo(schedule, it, isActive, isFinished, isSkipped)
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
        var numerator = finishedOrSkippedScheduledCount.toFloat()
        var denominator = totalMeasuringActivitiesCount.toFloat()

        if (shouldShowHeartSnapshot) {
            denominator += 1
            if (isHeartSnapshotFinished()) {
                numerator += 1
            }
        }

        return numerator / denominator
    }

    /**
     * @property totalMeasuringActivitiesCount total number of activities.
     */
    val totalMeasuringActivitiesCount: Int get() {
        return activityGroup?.activityIdentifiers?.size ?: 1
    }

    /**
     * @property isCompletedForToday Is the Study burst completed for today?
     */
    val isCompletedForToday: Boolean get() {
        if (!hasStudyBurst) {
            return true
        }
        // Make sure both daily tasks and one-time heart snapshot if applicable are complete
        val dailyTasksCompletedForToday = (finishedOrSkippedScheduledCount >= totalMeasuringActivitiesCount)
        val heartSnapshotHiddenOrComplete = (!shouldShowHeartSnapshot || isHeartSnapshotFinished())
        return dailyTasksCompletedForToday && heartSnapshotHiddenOrComplete
    }

    /**
     * @property finishedOrSkippedScheduledCount the count of finished or skipped daily study burst tasks
     */
    val finishedOrSkippedScheduledCount: Int get() {
        return finishedSchedules.size + studyBurstSettingsDao.todaysSkippedTaskCount()
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
        if (isCompletedForToday) {
            todayUnfinishedCompletionActivity?.let { return it }
        }
        return null
    }

    /**
     * @property todayUnfinishedCompletionActivity based on the day and finished status of the completion tasks
     */
    val todayUnfinishedCompletionActivity: ScheduledActivityEntity? get() {
        todayCompletionTask?.activityIdentifiers?.forEach {
            if (!hasCompletionTaskActivityBeenFinished(it)) {
                schedules.filterByActivityId(it).firstOrNull()?.let { schedule ->
                    return schedule
                }
            }
        }
        return null
    }

    /**
     * @property pastUnfinishedCompletionActivity based on the day and finished status of the completion tasks
     */
    val pastUnfinishedCompletionActivity: ScheduledActivityEntity? get() {
        pastCompletionTasks.forEach { completionTask ->
            completionTask.activityIdentifiers.forEach { activityId ->
                if (!hasCompletionTaskActivityBeenFinished(activityId)) {
                    schedules.filterByActivityId(activityId).firstOrNull()?.let {
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

    /**
     * @return the motivation survey backing schedule
     */
    val motivationSurvey: ScheduledActivityEntity? get() {
        return schedules.filterByActivityId(config.motivationIdentifier).firstOrNull()
    }

    val demographicsSurvey: ScheduledActivityEntity? get() {
        return schedules.filterByActivityId(DEMOGRAPHICS).firstOrNull()
    }

    val backgroundSurvey: ScheduledActivityEntity? get() {
        return schedules.filterByActivityId(BACKGROUND).firstOrNull()
    }

    val studyBurstReminderTask: ScheduledActivityEntity? get() {
        return schedules.filterByActivityId(STUDY_BURST_REMINDER).firstOrNull()
    }

    /**
     * @return true if the schedule is finished and has posted a report
     */
    private fun hasReport(schedule: ScheduledActivityEntity?): Boolean {
        if (schedule == null) return false
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
        val pastUnfinishedCompletionTasks = getUnfinishedSchedule()
        if (!isCompletedForToday && pastUnfinishedCompletionTasks == null) {
            val title = context.getString(R.string.study_burst_action_bar_title)
            var details: String? = null
            millisToExpiration?.let {
                details = timeUntilStudyBurstExpiration
            } ?: run {
                var activitiesTodoCount = totalMeasuringActivitiesCount - finishedOrSkippedScheduledCount
                if (shouldShowHeartSnapshot && !isHeartSnapshotFinished()) {
                    activitiesTodoCount += 1
                }
                if (activitiesTodoCount == 1) {  // Singular
                    details = context.getString(R.string.study_burst_activity_to_do)
                } else { // plural
                    details = context.getString(R.string.study_burst_activities_to_do).format(activitiesTodoCount)
                }
            }
            return TodayActionBarItem(title, details, null)
        }
        pastUnfinishedCompletionTasks?.let {
            return TodayActionBarItem(it.first, it.second, null)
        }
        if (hasStudyBurst) {  // Study burst is done for the day
            return TodayActionBarItem(
                    context.getString(R.string.study_burst_action_bar_title),
                    context.getString(R.string.study_burst_all_activities_complete),
                    null, false)
        } else { // Study burst as a whole is done and we are waiting until the next one
            var detail = context.getString(R.string.study_burst_not_active)
            var daysUntilNextStudyBurst = -1
            schedules.sortedBy { it.scheduledOn }.firstOrNull()?.scheduledOn?.let {
                daysUntilNextStudyBurst = 91 - (ChronoUnit.DAYS.between(it, date).toInt() % 91)
            }
            if (daysUntilNextStudyBurst == 1) {
                detail = context.getString(R.string.study_burst_1_day_until_next)
            } else if (daysUntilNextStudyBurst > 1) {
                detail = context.getString(R.string.study_burst_days_until_next, daysUntilNextStudyBurst.toString())
            }
            return TodayActionBarItem(context.getString(R.string.study_burst_action_bar_title),
                    detail, null, false)
        }
    }

    val millisToExpiration: Long? get() {
        expiresOn?.let {
            val millis = it.toEpochMilli() - Instant.now().toEpochMilli()
            // check for a negative time, because this means that progress did exist, but now it is over
            if (millis < 0) {
                return null
            }
            return millis
        } ?: return null
    }

    val timeUntilStudyBurstExpiration: String? get() {
        millisToExpiration?.let {
            if (it < 0) {
                return null
            } else {
                val secondsToExpiration = it / 1000
                val progressDate = ZonedDateTime.now().startOfDay().plusSeconds(secondsToExpiration)
                val dateEpochMillis = progressDate.toEpochSecond() * 1000
                val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                return formatter.format(Date(dateEpochMillis))
            }
        } ?: return null
    }

    /**
     * @return true if the task is finished and is included in the finishedSchedules, false otherwise
     */
    fun isStudyBurstTaskFinished(taskIdentifier: String): Boolean {
        if (taskIdentifier == HEART_SNAPSHOT) {
            return isHeartSnapshotFinished()
        }
        return null != finishedSchedules.firstOrNull { it.activityIdentifier() == taskIdentifier }
    }

    /**
     * @return true if the heart snapshot has been finished for the current study burst
     */
    fun isHeartSnapshotFinished(): Boolean {
        if (!hasStudyBurst) {
            return true
        }
        val now = date
        val dayCount = dayCount ?: run { return true }
        val lastFinishedDate = studyBurstSettingsDao.getLastSnapshotComplete() ?: run {
            // We have a study burst, but have not finished a heart snapshot yet
            return false
        }
        val studyBurstStart = now.minusDays(dayCount.toLong()).startOfDay()
        return lastFinishedDate.isAfter(studyBurstStart)
    }

    fun skipTaskForToday(taskIdentifier: String) {
        studyBurstSettingsDao.skipTaskForToday(taskIdentifier)
    }

    fun isTaskSkippedForToday(taskIdentifier: String): Boolean {
        return studyBurstSettingsDao.isTaskSkippedForToday(taskIdentifier)
    }
}

data class TodayActionBarItem(
        val title: String,
        val detail: String?,
        @DrawableRes val image: Int?,
        val isClickable: Boolean = true)

data class StudyBurstTaskInfo(
        val schedule: ScheduledActivityEntity?,
        val task: TaskInfo,
        val isActive: Boolean,
        val isComplete: Boolean,
        val isSkipped: Boolean)

open class StudyBurstSettingsDao @Inject constructor(context: Context) {
    val orderKey = "StudyBurstTaskOrder"
    val timestampKey = "StudyBurstTimestamp"
    val lastHeartSnapshotCompleteTimestamp = "LastHeartSnapshotCompleteTimestamp"

    val kSkippedTaskDateIdentifier = "SkippedTaskDateIdentifier"
    val kSkippedTaskArrayIdentifier = "SkippedTaskArrayIdentifier"

    companion object {
        private val TAG = StudyBurstSettingsDao::class.java.simpleName
    }

    private val prefs = context.getSharedPreferences("StudyBurstViewModel", Context.MODE_PRIVATE)

    @SuppressLint("ApplySharedPref") // Need it to immediately reflect the finished state
    open fun setSnapshotComplete() {
        prefs.edit().putString(lastHeartSnapshotCompleteTimestamp, LocalDateTime.now().toString()).commit()
    }

    @VisibleForTesting
    open fun getLastSnapshotComplete(): LocalDateTime? {
        prefs.getString(lastHeartSnapshotCompleteTimestamp, null)?.let {
            return LocalDateTime.parse(it)
        } ?: return null
    }

    @SuppressLint("ApplySharedPref") // Need it to immediately reflect the finished state
    open fun skipTaskForToday(taskIdentifier: String) {
        // Heart snapshot is skipped differently
        if (taskIdentifier == HEART_SNAPSHOT) {
            setSnapshotComplete()
            return
        }

        // Always reset todays skipped date
        val now = LocalDateTime.now()
        val editPrefs = prefs.edit()
        editPrefs.putString(kSkippedTaskDateIdentifier, now.toString())

        val skippedTaskIds = todaysSkippedTasks() ?: run {
            editPrefs.putStringSet(kSkippedTaskArrayIdentifier, setOf(taskIdentifier))
            editPrefs.commit()
            return
        }

        // Add the task to the existing set of todays skipped identifiers
        skippedTaskIds.add(taskIdentifier)
        editPrefs.putStringSet(kSkippedTaskArrayIdentifier, skippedTaskIds.toSet())
        editPrefs.commit()
    }

    open fun isTaskSkippedForToday(taskIdentifier: String): Boolean {
        val skippedTaskIds = todaysSkippedTasks() ?: run {
            return false
        }
        return skippedTaskIds.contains(taskIdentifier)
    }

    /// The list of skipped task identifiers for today, nil or empty if none available
    open fun todaysSkippedTasks(): ArrayList<String>? {
        // Check if there was a task skipped already today, and if so, if the
        // supplied task was one of the ones skipped.
        val skipDateStr = prefs.getString(kSkippedTaskDateIdentifier, null) ?: run {
            return null
        }

        val skipDate = LocalDateTime.parse(skipDateStr) ?: run {
            Log.e(TAG, "Error parsing skip task date")
            return null
        }

        // Is the date after today's start of day?  If no, consider it not applicable to today
        if (!skipDate.isAfter(LocalDateTime.now().startOfDay())) {
            return null
        }

        val taskArrayList = arrayListOf<String>()
        prefs.getStringSet(kSkippedTaskArrayIdentifier, null)?.forEach {
            taskArrayList.add(it)
        }

        return taskArrayList
    }

    open fun todaysSkippedTaskCount(): Int {
        return (todaysSkippedTasks() ?: arrayListOf()).size
    }

    @VisibleForTesting
    open fun setOrderedTasks(sortOrder: List<String>, timestamp: LocalDateTime) {
        val editPrefs = prefs.edit()
        editPrefs.putString(orderKey, RestUtils.GSON.toJson(sortOrder))
        editPrefs.putString(timestampKey, timestamp.toString())
        editPrefs.apply()
    }

    open fun getTaskSortOrder(): List<String> {
        prefs.getString(orderKey, null)?.let {
            return RestUtils.GSON.fromJson(it, object : TypeToken<List<String>>() {}.type)
        } ?: return StudyBurstViewModel.defaultTaskSortOrder
    }

    open fun getTaskSortOrderTimestamp(): LocalDateTime? {
        prefs.getString(timestampKey, null)?.let {
            return LocalDateTime.parse(it)
        } ?: return null
    }

    open fun saveGenderAndBirthYear(taskResult: TaskResult) {
        val gender = StepResultHelper.findStringResult(
                taskResult, "sex") ?: run { return }
        val birthYear = StepResultHelper.findIntegerResult(
                "birthYear", taskResult) ?: run { return }

        val editor = prefs.edit()
        editor.putString("gender", gender)
        editor.putInt("birthYear", birthYear)
        editor.apply()
    }

    open fun loadGenderAndBirthYear(): GenderAndBirthYear? {
        prefs.getString("gender", null)?.let {
            val birthYear = prefs.getInt("birthYear", -1)
            if (birthYear >= 0) {
                return GenderAndBirthYear(it, birthYear)
            }
        }
        return null
    }
}

data class GenderAndBirthYear(
        val gender: String,
        val birthYear: Int)