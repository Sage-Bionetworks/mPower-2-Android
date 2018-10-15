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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.researchstack.backbone.ui.MainActivity;
import org.sagebionetworks.research.mpower.researchstack.R;

/**
 * Created by rianhouston on 11/13/17.
 */

public class MpAlarmReceiver extends BroadcastReceiver {

    private static final String NOTIFICATION_CHANNEL_ID = "MpReminderManager";
    private static final String NOTIFICATION_CHANNEL_TITLE = "My BP Lab Reminders";
    private static final String NOTIFICATION_CHANNEL_DESC =
            "My BP Lab Check-in reminders to remind you to complete your check-ins.";

    @Override
    public void onReceive(Context context, Intent intent) {

        int code = intent.getIntExtra(MpReminderManager.KEY_CODE, 0);
        Log.v(MpReminderManager.LOG_TAG, "Reminder received from " + code);
        String title = intent.getStringExtra(MpReminderManager.KEY_TITLE);
        String content = intent.getStringExtra(MpReminderManager.KEY_CONTENT);
        String action = intent.getAction();

        // Trigger the notification
        // TODO: mdephillips 10/10/18 how do we show the correct activity here?
        showNotification(context, MainActivity.class, title, content, code, action);
    }

    public void showNotification(Context context, Class<?> cls, String title, String content, int code, String action) {

    // Starting with API 26, notifications must be contained in a channel
    if (Build.VERSION.SDK_INT >= 26) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_TITLE,
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(NOTIFICATION_CHANNEL_DESC);
            notificationManager.createNotificationChannel(channel);
        }
    }

      Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

      Intent notificationIntent = new Intent(context, cls);
      notificationIntent.setAction(action);
      notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

      TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
      stackBuilder.addParentStack(cls);
      stackBuilder.addNextIntent(notificationIntent);

      PendingIntent pendingIntent = stackBuilder.getPendingIntent(
              code, PendingIntent.FLAG_UPDATE_CURRENT);

      NotificationCompat.Builder builder =
              new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
      Notification notification = builder.setContentTitle(title)
              .setContentText(content).setAutoCancel(true)
              .setSound(alarmSound).setSmallIcon(R.mipmap.ic_launcher)
              .setContentIntent(pendingIntent).build();

      NotificationManager notificationManager = (NotificationManager)
              context.getSystemService(Context.NOTIFICATION_SERVICE);
      if (notificationManager != null) {
          notificationManager.notify(code, notification);
      }
    }
}
