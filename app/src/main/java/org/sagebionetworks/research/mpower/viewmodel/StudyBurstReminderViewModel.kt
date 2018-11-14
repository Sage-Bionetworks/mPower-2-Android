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
import android.content.Context
import com.google.common.base.Preconditions
import org.researchstack.backbone.result.StepResult
import org.researchstack.backbone.result.TaskResult
import org.researchstack.backbone.step.Step
import org.sagebionetworks.research.mpower.reminders.MpReminderManager
import org.sagebionetworks.research.mpower.research.MpIdentifier.STUDY_BURST_COMPLETED
import org.sagebionetworks.research.mpower.research.MpIdentifier.STUDY_BURST_REMINDER
import org.sagebionetworks.research.sageresearch.dao.room.ReportRepository
import org.sagebionetworks.research.sageresearch.dao.room.ScheduleRepository
import org.sagebionetworks.research.sageresearch.dao.room.ScheduledActivityEntityDao
import org.sagebionetworks.research.sageresearch.dao.room.mapValue
import org.sagebionetworks.research.sageresearch.extensions.filterByActivityId
import org.sagebionetworks.research.sageresearch.extensions.toThreeTenLocalDateTime
import org.sagebionetworks.research.sageresearch.viewmodel.ReportAndScheduleModel
import org.sagebionetworks.research.sageresearch.viewmodel.ReportAndScheduleViewModel
import org.slf4j.LoggerFactory
import org.threeten.bp.LocalDateTime
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

open class StudyBurstReminderViewModel(
        context: Context,
        scheduleDao: ScheduledActivityEntityDao,
        scheduleRepo: ScheduleRepository,
        reportRepo: ReportRepository):
    ReportAndScheduleViewModel(scheduleDao, scheduleRepo, reportRepo) {

    private val logger = LoggerFactory.getLogger(StudyBurstReminderViewModel::class.java)

    class Factory @Inject constructor(
            private val context: Context,
            private val scheduleDao: ScheduledActivityEntityDao,
            private val scheduleRepository: ScheduleRepository,
            private val reportRepository: ReportRepository) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            Preconditions.checkArgument(modelClass.isAssignableFrom(StudyBurstReminderViewModel::class.java))
            return StudyBurstReminderViewModel(context, scheduleDao, scheduleRepository, reportRepository) as T
        }
    }

    companion object {
        const val reminderTimeClientDataKey = "reminderTime"
        const val noReminderClientDataKey = "noReminder"
        const val defaultHour = 9
        const val defaultMinute = 0
        const val defaultDoNotRemindMe = false
    }

    private val timeDisplayFormatter = SimpleDateFormat("h:mm aaa", Locale.getDefault())
    private val dateFormatter = SimpleDateFormat("HH:mm:00.000", Locale.getDefault())
    private val reminderManager = MpReminderManager(context)
    private val startedOnTime = Date()

    /**
     * The local vars are meant to be used by an Activity that does not want the remote values
     * returned from the live data, but wants to be able to store local values of them across the activity life cycle
     */
    var localHour: Int = defaultHour
    var localMinute: Int = defaultMinute
    var localDoNotRemindMe: Boolean = defaultDoNotRemindMe

    private var reminderLiveData: LiveData<ReportAndScheduleModel>? = null
    /**
     * Fetches the schedules for all activities available today and also all study bursts completed in the past.
     * @return the live data for history item updates, will always be the same live data object
     */
    fun reminderLiveData(): LiveData<StudyBurstReminderState> {
        val liveData = reminderLiveData ?:
            mediatedLiveData(
                    listOf(
                            scheduleDao.oldestActivity(setOf(STUDY_BURST_COMPLETED)),
                            scheduleDao.activityGroup(setOf(STUDY_BURST_REMINDER))),
                    listOf(
                            mostRecentReport(STUDY_BURST_REMINDER)))
        reminderLiveData = liveData
        return Transformations.map(liveData) { convert(it) }
    }

    /**
     * Saves the study burst reminder configuration to bridge including the schedule, report, and S3 upload
     * And schedules the reminders with the reminder manager
     * @param context can be app or activity, needed to schedule alarms to show notifications
     * @param doNotRemindMe true if all study burst reminders should be canceled, false if they should be set
     * @param hour to schedule the reminder, if doNotRemindMe is true, anything can be supplied
     * @param minute to schedule the reminder, if doNotRemindMe is true, anything can be supplied
     */
    fun saveReminder(context: Context, doNotRemindMe: Boolean, hour: Int, minute: Int) {
        val reportAndSchedules = reminderLiveData?.value ?: return
        val reminderSchedule = reportAndSchedules.schedules
                .filterByActivityId(STUDY_BURST_REMINDER).firstOrNull() ?: return

        // This will schedule the reminder on the device at the correct interval
        updateRemindersOnDevice(context, StudyBurstReminderState(doNotRemindMe, hour, minute, doNotRemindMe))

        val taskResult = TaskResult(STUDY_BURST_REMINDER)
        taskResult.startDate = startedOnTime
        taskResult.endDate = Date()

        val reminderStepIdentifier = "Reminder"
        val reminderTimeStepIdentifier = "reminderTime"
        val reminderNoStepIdentifier = "noReminder"

        val stepResultTime = StepResult<String>(Step(reminderTimeStepIdentifier))
        stepResultTime.result = toResultString(hour, minute)

        val stepResultNoReminder = StepResult<Boolean>(Step(reminderNoStepIdentifier))
        stepResultNoReminder.result = doNotRemindMe

        val formStepResult: StepResult<StepResult<*>> = StepResult(Step(reminderStepIdentifier))
        formStepResult.results[reminderTimeStepIdentifier] = stepResultTime
        formStepResult.results[reminderNoStepIdentifier] = stepResultNoReminder

        taskResult.results[reminderStepIdentifier] = formStepResult

        updateScheduleToBridge(reminderSchedule)
        saveResearchStackReports(taskResult)
        uploadResearchStackTaskResultToS3(reminderSchedule, taskResult)
    }

    /**
     * This will check the state of notifications on the device, and determine if an update needs done
     * If an update does need done, it will set the notifications to the correct state
     * @param context can be app or activity, needed to schedule alarms to show notifications
     * @param reminderState the current state of the reminders
     */
    fun updateRemindersOnDevice(context: Context, reminderState: StudyBurstReminderState) {
        logger.info("updateRemindersOnDevice")
        val doNotRemindMe = reminderState.doNotRemindMe ?: return
        val reportAndSchedules = reminderLiveData?.value ?: return
        val firstStudyBurstSchedule = reportAndSchedules.schedules
                .filterByActivityId(STUDY_BURST_COMPLETED).firstOrNull() ?: return

        val hour = reminderState.reminderHour ?: 0
        val minute = reminderState.reminderMinute ?: 0

        val scheduledOn =
                firstStudyBurstSchedule.scheduledOn ?:
                studyStartDate()?.toThreeTenLocalDateTime() ?:
                LocalDateTime.now()

        val initialReminderTime = LocalDateTime.now()
                .withHour(hour)
                .withMinute(minute)
                .withSecond(0)
                .withNano(0)

        val reminder = reminderManager.createStudyBurstReminder(
                context, scheduledOn, initialReminderTime)

        if (doNotRemindMe) {
            logger.info("Canceling reminders")
            if (reminderState.isSetOnDevice) {
                reminderManager.cancelReminder(context, reminder)
            } else {
                logger.info("No need to cancel reminders, as they are already absent on the device")
            }
        } else {
            // No need to keep re-adding the alarms if they are already set
            if (!reminderState.isSetOnDevice) {
                logger.info("Scheduling reminders at hour=$hour, min=$minute")
                reminderManager.scheduleReminder(context, reminder)
            } else {
                logger.info("No need to schedule reminders, as they are already set on the device")
            }
        }
    }

    /**
     * @param items schedule items from live data query
     * @return a list of history items derived from today's finished schedules
     */
    private fun convert(model: ReportAndScheduleModel): StudyBurstReminderState?  {
        var hour: Int? = null
        var min: Int? = null
        model.reports.firstOrNull()?.data?.mapValue(reminderTimeClientDataKey, String::class.java)?.let {
            val calendar = Calendar.getInstance()
            calendar.time = dateFormatter.parse(it)
            hour = calendar.get(Calendar.HOUR_OF_DAY)
            min = calendar.get(Calendar.MINUTE)
        }
        val noReminder = model.reports.firstOrNull()?.data?.mapValue(noReminderClientDataKey, Boolean::class.java)
        val isSetOnDevice = reminderManager.isStudyBurstReminderScheduled()
        return StudyBurstReminderState(isSetOnDevice, hour, min, noReminder)
    }

    /**
     * Converts a hour minute pair to a time string for the task result
     */
    private fun toResultString(hour: Int, minute: Int): String {
        return dateFormatter.format(dateForTime(hour, minute))
    }

    private fun dateForTime(hour: Int, minute: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    /**
     * Converts a hour minute pair to a time string
     */
    fun toString(hour: Int, minute: Int): String {
        return timeDisplayFormatter.format(dateForTime(hour, minute))
    }
}

data class StudyBurstReminderState(
    /**
     * @property isSetOnDevice is true if study burst reminder is currently set for this device
     */
    val isSetOnDevice: Boolean = false,
    /**
     * @property reminderHour the hour when the user chose to be reminded, null if it was never set
     */
    val reminderHour: Int? = null,
    /**
     * @property reminderMinute the hour when the user chose to be reminded, null if it was never set
     */
    val reminderMinute: Int? = null,
    /**
     * @property doNotRemindMe true if the user chose not to ever be reminded, null if never set
     */
    val doNotRemindMe: Boolean? = null)