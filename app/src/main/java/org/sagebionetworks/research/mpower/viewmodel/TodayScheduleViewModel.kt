package org.sagebionetworks.research.mpower.viewmodel

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModelProviders
import android.support.v4.app.FragmentActivity

import org.sagebionetworks.research.sageresearch.dao.room.ScheduledActivityEntity

import android.arch.lifecycle.Transformations.map
import android.content.Context
import android.support.annotation.VisibleForTesting
import org.researchstack.backbone.utils.ResUtils
import org.sagebionetworks.research.mpower.research.MpIdentifier
import org.sagebionetworks.research.mpower.research.MpIdentifier.*

import org.sagebionetworks.research.mpower.viewmodel.ItemType.*

import org.sagebionetworks.research.sageresearch.extensions.filterByActivityId
import org.sagebionetworks.research.sageresearch.extensions.startOfDay
import org.sagebionetworks.research.sageresearch.extensions.startOfNextDay

import org.sagebionetworks.research.sageresearch.viewmodel.ScheduleViewModel
import org.threeten.bp.Instant
import org.threeten.bp.ZonedDateTime

//
//  Copyright © 2018 Sage Bionetworks. All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification,
// are permitted provided that the following conditions are met:
//
// 1.  Redistributions of source code must retain the above copyright notice, this
// list of conditions and the following disclaimer.
//
// 2.  Redistributions in binary form must reproduce the above copyright notice,
// this list of conditions and the following disclaimer in the documentation and/or
// other materials provided with the distribution.
//
// 3.  Neither the name of the copyright holder(s) nor the names of any contributors
// may be used to endorse or promote products derived from this software without
// specific prior written permission. No license is granted to the trademarks of
// the copyright holders even if such marks are included in this software.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//

/**
 * TodayScheduleViewModel fetches the schedules for all activities finished today,
 * and then consolidates them into history items.  History item live data will be updated when observed,
 * and when new schedules come down from bridge.
 */
open class TodayScheduleViewModel(app: Application): ScheduleViewModel(app) {

    companion object {
        @JvmStatic
        fun create(activity: FragmentActivity): TodayScheduleViewModel {
            return ViewModelProviders.of(activity).get(TodayScheduleViewModel::class.java)
        }
    }

    private val excludeIds = setOf(STUDY_BURST_COMPLETED)
    private val excludeTaskGroup = excludeIds.toSet()

    // TODO: mdephillips 9/4/18 what happens if clock ticks past midnight during this ViewModel's lifetime?
    // TODO: mdephillips 9/4/18 possible solution: have an observer wait x seconds that triggers a re-query
    @VisibleForTesting
    open val queryDateStart: Instant = ZonedDateTime.now().startOfDay().toInstant()

    @VisibleForTesting
    open val queryDateEnd: Instant = ZonedDateTime.now().startOfNextDay().toInstant()

    private var finishedTodayLiveData: LiveData<List<TodayHistoryItem>>? = null
    /**
     * Fetches the schedules for all activities finished today and consolidates them into history items.
     * @return the live data for history item updates, will always be the same live data object
     */
    fun liveData(): LiveData<List<TodayHistoryItem>> {
        val liveDataChecked = finishedTodayLiveData ?:
            map(scheduleDao().excludeActivityGroupFinishedBetween(
                excludeTaskGroup, queryDateStart, queryDateEnd)) { consolidate(it) }
        finishedTodayLiveData = liveDataChecked
        return liveDataChecked
    }

    /**
     * @param items schedule items from live data query
     * @return a list of history items derived from today's finished schedules
     */
    private fun consolidate(items: List<ScheduledActivityEntity>): List<TodayHistoryItem>  {
        val schedules = ArrayList(items)
        return ItemType.values().map {
            val filteredSchedules = { when (it) {
                ACTIVITIES -> schedules
                else -> {
                    val itemActivities = schedules.filterByActivityId(it.identifier)
                    schedules.removeAll(itemActivities)
                    itemActivities
                }
            }}.invoke()

            val count = { when (it) {
                ItemType.SYMPTOMS, ItemType.TRIGGERS, ItemType.MEDICATION -> {
                    // TODO: mdephillips 9/3/18 implement counting the study report items on this day
                    // TODO: mdephillips 9/3/18 instead of simply returning the schedules count
                    filteredSchedules.count()
                }
                else -> filteredSchedules.count()
            }}.invoke()

            TodayHistoryItem(it, filteredSchedules, count)
        }.filter { it.count > 0 }
    }
}

interface StringEnum {
    val rawValue: String
}

enum class ItemType(val identifier: String) {
    TRIGGERS(MpIdentifier.TRIGGERS),
    SYMPTOMS(MpIdentifier.SYMPTOMS),
    MEDICATION(MpIdentifier.MEDICATION),
    ACTIVITIES("activities")
}

/**
 * TodayHistoryItem is an organized data type that will help keep any schedule data parsing out of the fragment
 * @constructor
 * @param type
 * @param schedules from the MpRsdIdentifier identifier used to create this object.
 */
data class TodayHistoryItem(
        val type: ItemType,
        val schedules: List<ScheduledActivityEntity>,
        val count: Int) {

    fun imageRes(context: Context) = {
        ResUtils.getDrawableResourceId(context, type.identifier + "_task_icon")
    }

    fun title(context: Context) {
        val keyCountStr = if (count == 1) "singular" else "plural"
        val key = "task_" + keyCountStr + "_term_for_" + type.identifier
        context.getString(ResUtils.getStringResourceId(context, key)).format(count)
    }
}

/**
 * @param itemType the type of history item to find
 * @return the history item with desired type, null if item does not exists in the list with the type
 */
fun List<TodayHistoryItem>.find(itemType: ItemType): TodayHistoryItem? {
    return this.first { itemType == it.type }
}