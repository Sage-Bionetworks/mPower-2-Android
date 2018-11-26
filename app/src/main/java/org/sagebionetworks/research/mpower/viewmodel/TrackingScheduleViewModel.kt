/*
 * BSD 3-Clause License
 *
 * Copyright 2018  Sage Bionetworks. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1.  Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2.  Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * 3.  Neither the name of the copyright holder(s) nor the names of any contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission. No license is granted to the trademarks of
 * the copyright holders even if such marks are included in this software.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.sagebionetworks.research.mpower.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.google.common.base.Preconditions
import org.sagebionetworks.research.mpower.research.MpIdentifier.MEDICATION
import org.sagebionetworks.research.mpower.research.MpIdentifier.SYMPTOMS
import org.sagebionetworks.research.mpower.research.MpIdentifier.TRIGGERS
import org.sagebionetworks.research.sageresearch.dao.room.ScheduleRepository
import org.sagebionetworks.research.sageresearch.dao.room.ScheduledActivityEntity
import org.sagebionetworks.research.sageresearch.dao.room.ScheduledActivityEntityDao
import org.sagebionetworks.research.sageresearch.extensions.filterByActivityId
import org.sagebionetworks.research.sageresearch.viewmodel.ScheduleViewModel
import javax.inject.Inject

class TrackingScheduleViewModel(scheduleDao: ScheduledActivityEntityDao,
        scheduleRepository: ScheduleRepository) : ScheduleViewModel(scheduleDao, scheduleRepository) {

    class Factory @Inject constructor(private val scheduledActivityEntityDao: ScheduledActivityEntityDao,
            private val scheduleRepository: ScheduleRepository) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            Preconditions.checkArgument(modelClass.isAssignableFrom(TrackingScheduleViewModel::class.java))
            return TrackingScheduleViewModel(scheduledActivityEntityDao, scheduleRepository) as T
        }
    }

    private var trackingSchedulesLiveData: LiveData<TrackingSchedules>? = null
    /**
     * Fetches the schedules for all tracking activities and consolidates them into a TrackingSchedules object.
     * @return the live data for tracking schedules updates, will always be the same live data object.
     */
    fun liveData(): LiveData<TrackingSchedules> {
        val liveDataChecked = trackingSchedulesLiveData ?:
            Transformations.map(scheduleDao.activityGroup(setOf(TRIGGERS, SYMPTOMS, MEDICATION))) {
                consolidate(it)
            }
        trackingSchedulesLiveData = liveDataChecked
        return liveDataChecked
    }

    /**
     * @param items schedule items from live data query
     * @return a list of history items derived from today's finished schedules
     */
    private fun consolidate(items: List<ScheduledActivityEntity>): TrackingSchedules  {
        return TrackingSchedules(
                items.filterByActivityId(TRIGGERS).firstOrNull(),
                items.filterByActivityId(SYMPTOMS).firstOrNull(),
                items.filterByActivityId(MEDICATION).firstOrNull())
    }
}

/**
 * TrackingSchedules holds the most recent schedules for triggers, symptoms, and medication.
 */
data class TrackingSchedules(
        /**
         * @property triggers task most recent, null if none can be found.
         */
        val triggers: ScheduledActivityEntity? = null,
        /**
         * @property symptoms task most recent, null if none can be found.
         */
        val symptoms: ScheduledActivityEntity? = null,
        /**
         * @medication triggers task most recent, null if none can be found.
         */
        val medication: ScheduledActivityEntity? = null)