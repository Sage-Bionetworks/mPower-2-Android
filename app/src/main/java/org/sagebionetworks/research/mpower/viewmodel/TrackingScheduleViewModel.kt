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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dampcake.gson.immutable.ImmutableAdapterFactory
import com.google.common.base.Preconditions
import com.google.common.reflect.TypeToken
import org.json.JSONException
import org.sagebionetworks.research.domain.result.implementations.TaskResultBase
import org.sagebionetworks.research.domain.result.interfaces.TaskResult
import org.sagebionetworks.research.mpower.inject.AutoValueGson_AppAutoValueTypeAdapterFactory
import org.sagebionetworks.research.mpower.research.MpIdentifier.MEDICATION
import org.sagebionetworks.research.mpower.research.MpIdentifier.SYMPTOMS
import org.sagebionetworks.research.mpower.research.MpIdentifier.TRIGGERS
import org.sagebionetworks.research.mpower.tracking.view_model.logs.LoggingCollection
import org.sagebionetworks.research.mpower.tracking.view_model.logs.MedicationLog
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SimpleTrackingItemLog
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SymptomLog
import org.sagebionetworks.research.sageresearch.dao.room.EntityTypeConverters
import org.sagebionetworks.research.sageresearch.dao.room.ReportEntity
import org.sagebionetworks.research.sageresearch.dao.room.ReportRepository
import org.sagebionetworks.research.sageresearch.dao.room.ScheduleRepository
import org.sagebionetworks.research.sageresearch.dao.room.ScheduledActivityEntity
import org.sagebionetworks.research.sageresearch.dao.room.ScheduledActivityEntityDao
import org.sagebionetworks.research.sageresearch.extensions.filterByActivityId
import org.sagebionetworks.research.sageresearch.viewmodel.ReportAndScheduleViewModel
import org.slf4j.LoggerFactory
import org.threeten.bp.LocalDateTime
import java.util.UUID
import javax.inject.Inject

class TrackingScheduleViewModel(scheduleDao: ScheduledActivityEntityDao,
        scheduleRepository: ScheduleRepository, reportRepository: ReportRepository):
            ReportAndScheduleViewModel(scheduleDao, scheduleRepository, reportRepository) {

    class Factory @Inject constructor(private val scheduledActivityEntityDao: ScheduledActivityEntityDao,
            private val scheduleRepository: ScheduleRepository,
            private val reportRepository: ReportRepository) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            Preconditions.checkArgument(modelClass.isAssignableFrom(TrackingScheduleViewModel::class.java))
            return TrackingScheduleViewModel(scheduledActivityEntityDao, scheduleRepository, reportRepository) as T
        }
    }

    private val logger = LoggerFactory.getLogger(TrackingScheduleViewModel::class.java);

    private var trackingSchedulesLiveData: LiveData<TrackingSchedules>? = null
    /**
     * Fetches the schedules for all tracking activities and consolidates them into a TrackingSchedules object.
     * @return the live data for tracking schedules updates, will always be the same live data object.
     */
    fun scheduleLiveData(): LiveData<TrackingSchedules> {
        val liveDataChecked = trackingSchedulesLiveData ?:
            Transformations.map(scheduleDao.activityGroup(setOf(TRIGGERS, SYMPTOMS, MEDICATION))) {
                return@map TrackingSchedules(
                        it.filterByActivityId(TRIGGERS).firstOrNull(),
                        it.filterByActivityId(SYMPTOMS).firstOrNull(),
                        it.filterByActivityId(MEDICATION).firstOrNull())
            }
        trackingSchedulesLiveData = liveDataChecked
        return liveDataChecked
    }

    private var trackingReportsLiveData: MediatorLiveData<TrackingReports>? = null
    /**
     * Fetches the most recent reports for all tracking activities.
     * @return the live data for the most recent tracking report updates, will always be the same live data object.
     */
    fun reportLiveData(): LiveData<TrackingReports> {
        val liveDataChecked = trackingReportsLiveData ?: {
            val triggerReportLiveData = mostRecentReport(TRIGGERS)
            val symptomReportLiveData = mostRecentReport(SYMPTOMS)
            val medicationReportLiveData = mostRecentReport(MEDICATION)

            val mediator = MediatorLiveData<TrackingReports>()
            mediator.addSource(triggerReportLiveData) {
                mediator.value = TrackingReports(
                        it?.firstOrNull(),
                        symptomReportLiveData.value?.firstOrNull(),
                        medicationReportLiveData.value?.firstOrNull())
            }
            mediator.addSource(symptomReportLiveData) {
                mediator.value = TrackingReports(
                        triggerReportLiveData.value?.firstOrNull(),
                        it?.firstOrNull(),
                        medicationReportLiveData.value?.firstOrNull())
            }
            mediator.addSource(medicationReportLiveData) {

                mediator.value = TrackingReports(
                        triggerReportLiveData.value?.firstOrNull(),
                        symptomReportLiveData.value?.firstOrNull(),
                        it?.firstOrNull())
            }
            mediator
        }.invoke()
        trackingReportsLiveData = liveDataChecked
        return liveDataChecked
    }

    /**
     * Creates a new TaskResult that contains the result of a previous run of the task by
     * de-serializing the report data into a LoggingCollection result and adding it to the TaskResult
     * @param report most recent for this taskId
     * @param taskId the identifier of the task to create the task result for
     * @param taskRunUuid the uuid of that will be attached to the task
     * @return a new TaskResult that contains the result of a previous run of the task by,
     *         null if it cannot be created for any reason.
     */
    fun createTaskResult(report: ReportEntity, taskId: String, taskRunUuid: UUID): TaskResult? {
        report.data?.data?.let { reportData ->
            when(taskId) {
                TRIGGERS -> object : TypeToken<LoggingCollection<SimpleTrackingItemLog>>(){}.type
                SYMPTOMS -> object : TypeToken<LoggingCollection<SymptomLog>>(){}.type
                MEDICATION -> object : TypeToken<LoggingCollection<MedicationLog>>(){}.type
                else -> null
            }?.let { type ->
                val gson = EntityTypeConverters().bridgeGsonBuilder
                        .registerTypeAdapterFactory(AutoValueGson_AppAutoValueTypeAdapterFactory())
                        .registerTypeAdapterFactory(ImmutableAdapterFactory.forGuava())
                        .create()
                val json = gson.toJson(reportData)
                var loggingCollection: LoggingCollection<*>? = null
                try {
                    loggingCollection = gson.fromJson(json, type)
                } catch (e: Throwable) {
                    // No need to crash the app, user will just have to redo selection
                    logger.error(e.message)
                    return TaskResultBase(taskId, taskRunUuid)
                }
                val taskResult = TaskResultBase(taskId, taskRunUuid)
                return taskResult.addAsyncResult(loggingCollection)
            }
        }
        return null
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
         * @medication medication task most recent, null if none can be found.
         */
        val medication: ScheduledActivityEntity? = null)

/**
 * TrackingReports holds the most recent Report for triggers, symptoms, and medication.
 */
data class TrackingReports(
        /**
         * @property triggers report most recent, null if none can be found.
         */
        val triggers: ReportEntity? = null,
        /**
         * @property symptoms report most recent, null if none can be found.
         */
        val symptoms: ReportEntity? = null,
        /**
         * @property medication report most recent, null if none can be found.
         */
        val medication: ReportEntity? = null)