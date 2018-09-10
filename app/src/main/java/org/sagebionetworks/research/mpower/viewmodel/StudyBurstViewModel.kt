package org.sagebionetworks.research.mpower.viewmodel

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.ViewModelProviders
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.support.annotation.VisibleForTesting
import android.support.v4.app.FragmentActivity
import com.google.gson.reflect.TypeToken
import org.researchstack.backbone.utils.ResUtils
import org.sagebionetworks.bridge.rest.RestUtils
import org.sagebionetworks.research.mpower.research.DataSourceManager
import org.sagebionetworks.research.mpower.research.MpIdentifier.*
import org.sagebionetworks.research.mpower.research.StudyBurstConfiguration
import org.sagebionetworks.research.sageresearch.dao.room.ScheduledActivityEntity
import org.sagebionetworks.research.sageresearch.extensions.endOfDay
import org.sagebionetworks.research.sageresearch.extensions.startOfDay
import org.sagebionetworks.research.sageresearch.manager.ActivityGroup
import org.sagebionetworks.research.sageresearch.viewmodel.ScheduleViewModel
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
    protected open val studyBurstConfiguration = StudyBurstConfiguration()
    // TODO: mdephillips 9/8/18 get this from bridge config
    @VisibleForTesting
    protected open fun activityGroup(): ActivityGroup? {
        return DataSourceManager.installedGroup(studyBurstConfiguration.taskGroupIdentifier)
    }

    /**
     * Today's query will check for all of today's activity schedules that are unfinished or finished today
     * @return the start and end range for the query
     */
    @VisibleForTesting
    protected open fun todayQuery(): Pair<LocalDateTime, LocalDateTime> {
        return Pair(LocalDateTime.now().startOfDay(), LocalDateTime.now().endOfDay())
    }

    /**
     * Study burst query will check for all the study burst completed tasks within the number of day range
     * @return the start and end range for the query
     */
    @VisibleForTesting
    protected open fun studyBurstQuery(): Pair<LocalDateTime, LocalDateTime> {
        val numberOfDays = 2L * studyBurstConfiguration.numberOfDays
        return Pair(
                LocalDateTime.now().startOfDay().minusDays(numberOfDays),
                LocalDateTime.now().startOfDay().plusDays(numberOfDays).minusNanos(1))
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

    private fun getStudyBurst(schedules: List<ScheduledActivityEntity>): ScheduledActivityEntity? {
        return null
    }

    /**
     * @param items schedule items from live data query
     * @return a list of history items derived from today's finished schedules
     */
    private fun consolidateFromCurrentValues(): StudyBurstItem?  {
        // We can only consolidate and create a study burst item if we have all the request
        if (todayLiveData?.value == null || completedBurstsLiveData?.value == null) {
            return null
        }


        return null
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
        val expiresOn: LocalDateTime? = null,
        /**
         * @property pastDaysCount The count of past days in a study burst.
         */
        val pastDaysCount: Int,
        /**
         * @property dayCount What day of the study burst should be displayed?
         */
        val dayCount: Int,
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

