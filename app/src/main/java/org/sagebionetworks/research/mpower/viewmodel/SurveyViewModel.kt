package org.sagebionetworks.research.mpower.viewmodel

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModelProviders

import android.support.annotation.VisibleForTesting
import android.support.v4.app.FragmentActivity
import org.sagebionetworks.research.mpower.research.DataSourceManager

import org.sagebionetworks.research.mpower.research.StudyBurstConfiguration
import org.sagebionetworks.research.sageresearch.dao.room.ScheduledActivityEntity
import org.sagebionetworks.research.sageresearch.viewmodel.ScheduleViewModel
import org.threeten.bp.LocalDateTime

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
 * SurveyViewModel contains a live data object that queries for all non-excluded surveys unfinished and available now.
 */
open class SurveyViewModel(app: Application): ScheduleViewModel(app) {

    companion object {
        @JvmStatic
        fun create(activity: FragmentActivity): SurveyViewModel {
            return ViewModelProviders.of(activity).get(SurveyViewModel::class.java)
        }
    }

    @VisibleForTesting
    open val excludeGroup: Set<String> = DataSourceManager.installedGroups
            .map { it.activityIdentifiers.map { it.identifier } }.flatMap { it }.toSet()
            .union(StudyBurstConfiguration.completionTaskIdentifiers.map { it.identifier })

    // TODO: mdephillips 9/4/18 what happens if clock ticks past midnight during this ViewModel's lifetime?
    // TODO: mdephillips 9/4/18 possible solution: have an observer wait x seconds that triggers a re-query
    @VisibleForTesting
    open val queryDate: LocalDateTime = LocalDateTime.now()

    private var surveyLiveDateTime: LiveData<List<ScheduledActivityEntity>>? = null
    /**
     * Fetches the schedules for all non-excluded surveys unfinished and available now.
     * @return the live data for the schedules, it will always be the same live data object.
     */
    fun liveData(): LiveData<List<ScheduledActivityEntity>> {
        val liveDataChecked = surveyLiveDateTime ?:
            scheduleDao().excludeSurveyGroupUnfinishedAvailableOn(excludeGroup, queryDate)
        surveyLiveDateTime = liveDataChecked
        return liveDataChecked
    }
}