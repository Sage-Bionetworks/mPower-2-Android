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

package org.sagebionetworks.research.mpower.reminders

import android.app.AlarmManager
import android.content.Context
import org.sagebionetworks.research.mpower.R.string
import org.sagebionetworks.research.mpower.research.MpIdentifier
import org.sagebionetworks.research.mpower.research.StudyBurstConfiguration
import org.sagebionetworks.research.sageresearch.extensions.startOfDay
import org.sagebionetworks.research.sageresearch.reminders.MpReminderAlarmReceiver
import org.sagebionetworks.research.sageresearch.reminders.Reminder
import org.sagebionetworks.research.sageresearch.reminders.ReminderManager
import org.sagebionetworks.research.sageresearch.reminders.ReminderScheduleIgnoreRule
import org.sagebionetworks.research.sageresearch.reminders.ReminderScheduleRules
import org.threeten.bp.LocalDateTime

val REMINDER_CODE_RUN_TASK = 1001
val REMINDER_ACTION_RUN_TASK = "REMINDER_ACTION_RUN_TASK"

val REMINDER_CODE_STUDY_BURST = 1002
val REMINDER_ACTION_STUDY_BURST = "REMINDER_ACTION_STUDY_BURST"

class MpReminderManager(context: Context): ReminderManager(context) {
    /**
     * @property reminderAlarmReceiver the receiver to associate with this manager
     */
    override val reminderAlarmReceiver: Class<*>
        get() {
            return MpReminderAlarmReceiver::class.java
        }

    /**
     * Reschedules all reminders based on the previously scheduled ones
     * @param context can be app, activity, or server
     */
    fun rescheduleAllReminders(context: Context) {
        cancelAllReminders(context).forEach {
            scheduleReminder(context, it)
        }
    }

    /**
     *  Schedules the study burst reminders
     *  @param context can be app or activity
     *  @param firstStudyBurstScheduledOn the scheduledOn from the earliest study burst schedule
     *  @param reminderTime the LocalDateTime representing the hour/minute of when the daily reminder happens
     */
    fun createStudyBurstReminder(
            context: Context,
            firstStudyBurstScheduledOn: LocalDateTime,
            reminderTime: LocalDateTime): Reminder {

        // TODO: mdephillips 10/25/18 this needs to be supported by bridge app config
        val studyConfig = StudyBurstConfiguration()

        val ignoreStart = firstStudyBurstScheduledOn.plusDays(studyConfig.numberOfDays.toLong())
        val ignoreEnd = firstStudyBurstScheduledOn.plusDays(studyConfig.repeatIntervalInDays)
        val ignoreRepeatInterval = studyConfig.repeatIntervalInDays
        val ignoreAlarmRules = ReminderScheduleIgnoreRule(ignoreStart, ignoreEnd, ignoreRepeatInterval)

        val reminderScheduleRules = ReminderScheduleRules(
                reminderTime, AlarmManager.INTERVAL_DAY,
                ignoreAlarmRules, true)

        val reminder = Reminder(
                MpIdentifier.STUDY_BURST_REMINDER, REMINDER_ACTION_STUDY_BURST,
                REMINDER_CODE_STUDY_BURST, reminderScheduleRules,
                title = context.getString(string.reminder_title_study_burst))

        return reminder
    }
}