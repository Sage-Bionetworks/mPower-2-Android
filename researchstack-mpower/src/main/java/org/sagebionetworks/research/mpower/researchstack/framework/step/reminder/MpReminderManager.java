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

package org.sagebionetworks.research.mpower.researchstack.framework.step.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import org.researchstack.backbone.utils.LogExt;
import org.sagebionetworks.bridge.rest.model.ScheduledActivity;
import org.sagebionetworks.research.mpower.researchstack.framework.MpPrefs;

import java.util.Calendar;
import java.util.Date;

import org.sagebionetworks.research.mpower.researchstack.R;

/**
 * Created by rianhouston on 11/13/17.
 */

public class MpReminderManager {

    static final String LOG_TAG = MpReminderManager.class.getCanonicalName();

    public static final String KEY_CODE = "KEY_CODE";
    public static final String KEY_TITLE = "KEY_TITLE";
    public static final String KEY_CONTENT = "KEY_CONTENT";

    public static final int NOTIFICATION_REQUEST_CODE = 2401;
    public static final String NOTIFICATION_ACTION_CODE = "MpNotificationAction";

    private static final int DEFAULT_HOUR = 17; // Afternoon 5 PM
    private static final int DEFAULT_MINUTE = 0;

    public static final String KEY_REMINDER_FREQUENCY_BODY_AND_MIND = "reminder_frequency_body_and_mind";
    public static final String KEY_REMINDER_FREQUENCY_MORNING = "reminder_frequency_morning";
    public static final String KEY_REMINDER_FREQUENCY_EVENING = "reminder_frequency_evening";
    public static final String KEY_REMINDER_TIME_BODY_AND_MIND = "reminder_time_body_and_mind";
    public static final String KEY_REMINDER_TIME_MORNING = "reminder_time_morning";
    public static final String KEY_REMINDER_TIME_EVENING = "reminder_time_evening";
    public static final String KEY_REMINDERS_NOTIFICATIONS_GLOBAL = "reminder_notifications_global";

    private final SharedPreferences prefs;

    public SharedPreferences getPrefs() {
        return prefs;
    }

    public MpReminderManager(Context context) {
        super();
        prefs = createPrefs(context);
    }

    private ScheduledActivity reminderActivity;
    public ScheduledActivity getReminderActivity() {
        return reminderActivity;
    }

    public boolean isInitialized() {
        return reminderActivity != null;
    }

    /**
     * Reschedule all the reminders based on the current state of BpPrefs
     * @param context used to schedule the alarm for the local notification
     */
    public void rescheduleReminders(Context context) {
        //DateTime studyStartDate = BpDataProvider.getInstance().getParticipantCreatedOn();
        //scheduleInsightNotifications(context, studyStartDate);
    }

    /**
     * @param context used to schedule the alarm for the local notification
     */
    private void setReminders(Context context) {
        // cancel already scheduled reminders
        cancelAllReminders(context);
        enableReceiver(context, true);

        if (!getRemindersOnGlobal()) {
            LogExt.d(MpReminderManager.class, "Not scheduling reminders for reminder type all reminders are turned off.");
            return;
        }

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) {
            Log.e(LOG_TAG, "AlarmManager is null! Can't schedule alarms");
            return;
        }

        Calendar setcalendar = Calendar.getInstance();
        setcalendar.setTimeInMillis(System.currentTimeMillis());

        setcalendar.set(Calendar.HOUR_OF_DAY, DEFAULT_HOUR);
        setcalendar.set(Calendar.MINUTE, DEFAULT_MINUTE);
        setcalendar.set(Calendar.SECOND, 0);

        final long EVERY_OTHER_DAY = (2 * AlarmManager.INTERVAL_DAY);
        //long interval = (frequency == Frequency.EVERYDAY) ?
        //        AlarmManager.INTERVAL_DAY : EVERY_OTHER_DAY;
        long interval = AlarmManager.INTERVAL_DAY;

        if (interval == EVERY_OTHER_DAY) {
            // To keep "every other day" consistent and reproducible,
            // Only notify users on even numbered days
            if (setcalendar.get(Calendar.DAY_OF_YEAR) % 2 != 0) {
                // This will push the day to tomorrow for the notification start
                setcalendar.set(Calendar.DAY_OF_YEAR, setcalendar.get(Calendar.DAY_OF_YEAR)+1);
            } else if(setcalendar.getTime().before(new Date())) {
                // We were already on an even day, because the first condition failed
                // So we should skip 2 days in the future for every other day
                setcalendar.set(Calendar.DAY_OF_YEAR, setcalendar.get(Calendar.DAY_OF_YEAR)+2);
            }
        } else {
            // if the scheduled time has already passed, change the day to tomorrow
            if(setcalendar.getTime().before(new Date())) {
                setcalendar.set(Calendar.DAY_OF_YEAR, setcalendar.get(Calendar.DAY_OF_YEAR)+1);
            }
        }

        LogExt.d(MpReminderManager.class,"Setting reminder for scheduled date: \n" +
                "at reminder time " + setcalendar.getTime().toString() + "\n");
        PendingIntent pendingIntent = pendingIntent(context);

        am.setRepeating(AlarmManager.RTC_WAKEUP,
                setcalendar.getTimeInMillis(), interval, pendingIntent);
    }

    private static void enableReceiver(Context context, boolean enable) {
        int state = enable ?
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        ComponentName receiver = new ComponentName(context, MpAlarmReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver, state, PackageManager.DONT_KILL_APP);
    }

    /**
     * @param context used to cancel the reminders
     @ @param type the type of alarm to cancel
     */
    public void cancelAllReminders(Context context) {
        enableReceiver(context, false);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        PendingIntent pendingIntent = pendingIntent(context);
        if (am != null) {
            am.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }

    private static PendingIntent pendingIntent(Context context) {
        int code = 0;
        String title = context.getResources().getString(R.string.app_name);
        String content = null;
        code = NOTIFICATION_REQUEST_CODE;
        content = "Test notification content";
        Intent intent1 = new Intent(context, MpAlarmReceiver.class);
        intent1.setAction(NOTIFICATION_ACTION_CODE);
        intent1.putExtra(KEY_CODE, code);
        intent1.putExtra(KEY_TITLE, title);
        intent1.putExtra(KEY_CONTENT, content);
        intent1.setData(Uri.parse(NOTIFICATION_ACTION_CODE));
        return PendingIntent.getBroadcast(
                context, code,
                intent1, PendingIntent.FLAG_ONE_SHOT);
    }

    /**
     * Parse the hour from a string like: '10:15 am'
     * @param timeStr
     * @return
     */
    private static int getHour(String timeStr) {
        try {
            int index = timeStr.indexOf(":");
            String hourStr = timeStr.substring(0, index);
            int hour = Integer.parseInt(hourStr);

            boolean isPM = timeStr.toLowerCase().contains("pm");
            if (isPM) hour = hour + 12;
            return hour;
        } catch(Exception ex) {
            return DEFAULT_HOUR;
        }
    }

    /**
     * Parse the minutes from a string like: '10:15 pm'
     * @param timeStr
     * @return
     */
    private static int getMinute(String timeStr) {
        try {
            int index = timeStr.indexOf(":");
            String minuteStr = timeStr.substring(index + 1, index + 3);
            int minute = Integer.parseInt(minuteStr);

            return minute;
        } catch(Exception ex) {
            return DEFAULT_MINUTE;
        }
    }

    /**
     * @return a TaskResult that contains the BpPrefs reminder settings
     */
//    protected TaskResult createTaskResultFromPrefs() {
//        TaskResult taskResult = new TaskResult(BpTaskFactory.TASK_ID_SETTINGS_REMINDERS);
//        taskResult.setStartDate(new Date());
//        taskResult.setEndDate(new Date());
//        // Set the state of the reminder types in the TaskResult
//        for (MpReminderManager.Type type : MpReminderManager.Type.values()) {
//            {
//                // Save the reminder frequency
//                String key = getFrequencyKeyForReminderType(type);
//                Frequency frequency = getReminderFrequency(type);
//                if (frequency != null) {
//                    String result = frequency.value();
//                    if (result != null) {
//                        StepResult<String> stepResult = new StepResult<>(new Step(key));
//                        stepResult.setResult(result);
//                        taskResult.setStepResultForStepIdentifier(key, stepResult);
//                    }
//                }
//            }
//            {
//                // Save the reminder time
//                String key = getTimeKeyForReminderType(type);
//                String result = getReminderTime(type);
//                StepResult<String> stepResult = new StepResult<>(new Step(key));
//                stepResult.setResult(result);
//                taskResult.setStepResultForStepIdentifier(key, stepResult);
//            }
//        }
//        {
//            // Save the global on/off flag for notifications
//            String key = BpPrefs.KEY_REMINDERS_NOTIFICATIONS_GLOBAL;
//            StepResult<Boolean> stepResult = new StepResult<>(new Step(key));
//            Boolean result = getRemindersOnGlobal();
//            stepResult.setResult(result);
//            taskResult.setStepResultForStepIdentifier(key, stepResult);
//        }
//        return taskResult;
//    }

//    protected void syncPrefsWithTaskResult(TaskResult taskResult) {
//        // Check to see if any of the BpPrefs values are null
//        // If so, set the BpPrefs value to the task result
//        if (taskResult == null) {
//            return;
//        }
//
//        // Make sure the state of the reminder type info are synced
//        for (MpReminderManager.Type type : MpReminderManager.Type.values()) {
//            // Sync the reminder frequency
//            if (getReminderFrequency(type) == null) {
//                String key = getFrequencyKeyForReminderType(type);
//                String result = StepResultHelper.findStringResult(taskResult, key);
//                MpReminderManager.Frequency value =
//                        MpReminderManager.Frequency.fromString(result);
//                if (value != null) {
//                    LogExt.d(MpReminderManager.class, "Bridge value of " + type +
//                            " frequency is more accurate than prefs");
//                    setReminderFrequency(value, type);
//                }
//            }
//            // Sync the reminder time
//            if (getReminderTimeWithNullDefault(type) == null) {
//                String key = getTimeKeyForReminderType(type);
//                String result = StepResultHelper.findStringResult(taskResult, key);
//                if (result != null) {
//                    LogExt.d(MpReminderManager.class, "Bridge value of " + type +
//                            " time is more accurate than prefs");
//                    setReminderTime(result, type);
//                }
//            }
//        }
//        // Syc the global on/off flag for notifications
//        if (!hasRemindersOnGlobal()) {
//            String key = BpPrefs.KEY_REMINDERS_NOTIFICATIONS_GLOBAL;
//            Boolean value = StepResultHelper.findBooleanResult(key, taskResult);
//            if (value != null) {
//                LogExt.d(MpReminderManager.class, "Bridge value of " + key + " = " + value +
//                        " is more accurate than prefs");
//                setRemindersOnGlobal(value);
//            }
//        }
//    }

    @VisibleForTesting
    SharedPreferences createPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setRemindersOnGlobal(boolean remindersOnGlobal) {
        LogExt.d(MpPrefs.class, "Setting remindersOnGlobal = " + remindersOnGlobal);
        prefs.edit().putBoolean(KEY_REMINDERS_NOTIFICATIONS_GLOBAL, remindersOnGlobal).apply();
    }

    public boolean getRemindersOnGlobal() {
        return prefs.getBoolean(KEY_REMINDERS_NOTIFICATIONS_GLOBAL, true);
    }

    public boolean hasRemindersOnGlobal() {
        return prefs.contains(KEY_REMINDERS_NOTIFICATIONS_GLOBAL);
    }
}
