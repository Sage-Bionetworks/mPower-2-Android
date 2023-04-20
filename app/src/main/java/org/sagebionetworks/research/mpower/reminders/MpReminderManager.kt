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
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import org.sagebionetworks.research.mpower.R.string
import org.sagebionetworks.research.mpower.research.MpIdentifier
import org.sagebionetworks.research.mpower.research.StudyBurstConfiguration
import org.sagebionetworks.research.sageresearch.reminders.MpReminderAlarmReceiver
import org.sagebionetworks.research.sageresearch.reminders.REMINDER_JSON_KEY
import org.sagebionetworks.research.sageresearch.reminders.Reminder
import org.sagebionetworks.research.sageresearch.reminders.ReminderManager
import org.sagebionetworks.research.sageresearch.reminders.ReminderScheduleIgnoreRule
import org.sagebionetworks.research.sageresearch.reminders.ReminderScheduleRules
import org.slf4j.LoggerFactory
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import java.util.concurrent.TimeUnit

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

    /**
     * @return true if the study burst reminder is scheduled, false otherwise
     */
    fun isStudyBurstReminderScheduled(): Boolean {
        return isReminderScheduled(MpIdentifier.STUDY_BURST_REMINDER)
    }

    fun cancelReminderUpdated(context: Context, reminder: Reminder) {
        val logger = LoggerFactory.getLogger(ReminderManager::class.java)
        (context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager)?.let { alarmManager ->
            // Let's check for a previously scheduled reminder with the same guid
            // Some Reminder data may have changed, but if it has the same guid it should always be canceled
            sharedPrefs.getString(reminder.guid, null)?.let { reminderJson ->
                reminderFromJson(reminderJson)?.let {
                    val pendingIntent = pendingIntentForReminderUpdated(context, it)
                    alarmManager.cancel(pendingIntent)
                    pendingIntent.cancel()
                }
            }
            // Always cancel the reminder as it currently exists as well
            val pendingIntent = pendingIntentForReminderUpdated(context, reminder)
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            sharedPrefs.edit().remove(reminder.guid).apply()
        } ?: run {
            logger.warn("Failed to obtain alarm service to cancel all reminders")
        }
    }

    private fun pendingIntentForReminderUpdated(context: Context, reminder: Reminder): PendingIntent {
        val intent = Intent(context, reminderAlarmReceiver)
        intent.action = reminder.action
        intent.putExtra(REMINDER_JSON_KEY, jsonFromReminder(reminder))
        intent.data = Uri.parse(reminder.guid)
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
        return PendingIntent.getBroadcast(context, reminder.code, intent, flag)
    }

    fun scheduleReminderUpdated(context: Context, reminder: Reminder) {
        val logger = LoggerFactory.getLogger(ReminderManager::class.java)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: run {
            logger.warn("Could not obtain alarm service to schedule reminder")
            return
        }

        // Enable receiver so it can intercept alarm broadcasts
        enableReceiver(context, true)
        // Cancel any previously scheduled reminders that have the same pending intent
        cancelReminderUpdated(context, reminder)
        val pendingIntent = pendingIntentForReminderUpdated(context, reminder)

        // Persist the reminder info so we can cancel it later if we need to
        val reminderJson = jsonFromReminder(reminder)
        sharedPrefs.edit().putString(reminder.guid, reminderJson).apply()

        val initialAlarmDateTime = reminder.reminderScheduleRules.initialAlarmTime
        val initialAlarmEpochMillis = TimeUnit.SECONDS.toMillis(
                initialAlarmDateTime.atZone(ZoneId.systemDefault()).toEpochSecond())

        reminder.reminderScheduleRules.repeatAlarmInterval?.let {
            logger.info("Setting repeat reminder with info $reminder")
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, initialAlarmEpochMillis, it, pendingIntent)
        } ?: run {
            logger.info("Setting reminder with info $reminder")
            alarmManager.set(AlarmManager.RTC_WAKEUP, initialAlarmEpochMillis, pendingIntent)
        }
    }
}