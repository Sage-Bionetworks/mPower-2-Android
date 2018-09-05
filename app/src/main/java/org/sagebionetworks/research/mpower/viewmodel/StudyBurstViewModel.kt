package org.sagebionetworks.research.mpower.viewmodel

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModelProviders
import android.support.annotation.VisibleForTesting
import android.support.v4.app.FragmentActivity
import org.sagebionetworks.bridge.rest.model.Study
import org.sagebionetworks.research.mpower.research.MpRsdIdentifier
import org.sagebionetworks.research.mpower.research.StudyBurstConfiguration
import org.sagebionetworks.research.mpower.viewmodel.ItemType.ACTIVITIES
import org.sagebionetworks.research.mpower.viewmodel.ItemType.MEDICATION
import org.sagebionetworks.research.mpower.viewmodel.ItemType.SYMPTOMS
import org.sagebionetworks.research.mpower.viewmodel.ItemType.TRIGGERS
import org.sagebionetworks.research.sageresearch.dao.room.ScheduledActivityEntity
import org.sagebionetworks.research.sageresearch.extensions.filterByActivityId
import org.sagebionetworks.research.sageresearch.extensions.startOfDay
import org.sagebionetworks.research.sageresearch.extensions.startOfNextDay
import org.sagebionetworks.research.sageresearch.viewmodel.ScheduleViewModel
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZonedDateTime

open class StudyBurstViewModel(app: Application): ScheduleViewModel(app) {

    companion object {
        @JvmStatic
        fun create(activity: FragmentActivity): StudyBurstViewModel {
            return ViewModelProviders.of(activity).get(StudyBurstViewModel::class.java)
        }
    }

    private val excludeIds = setOf(MpRsdIdentifier.STUDY_BURST_COMPLETED)
    private val excludeTaskGroup = excludeIds.map { it.identifier }.toHashSet()

    // TODO: mdephillips 9/4/18 what happens if clock ticks past midnight during this ViewModel's lifetime?
    // TODO: mdephillips 9/4/18 possible solution: have an observer wait x seconds that triggers a re-query
    @VisibleForTesting
    open val queryDateStart: Instant = ZonedDateTime.now().startOfDay().toInstant()

    @VisibleForTesting
    open val queryDateEnd: Instant = ZonedDateTime.now().startOfNextDay().toInstant()

    private var studyBurstLiveData: LiveData<StudyBurstItem>? = null
    /**
     * Fetches the schedules for all activities finished today and consolidates them into history items.
     * @return the live data for history item updates, will always be the same live data object
     */
    fun liveData(): LiveData<StudyBurstItem> {
        val liveDataChecked = studyBurstLiveData ?: Transformations.map(
                scheduleDao().excludeActivityGroupFinishedBetween(
                        excludeTaskGroup, queryDateStart, queryDateEnd)) { consolidate(it) }
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
    private fun consolidate(items: List<ScheduledActivityEntity>): StudyBurstItem  {
        return StudyBurstItem(items)
    }
}

data class StudyBurstItem(
        val schedules: List<ScheduledActivityEntity>,
        val expiresOn: LocalDateTime? = null,
        val studyBurstConfiguration: StudyBurstConfiguration = StudyBurstConfiguration())

