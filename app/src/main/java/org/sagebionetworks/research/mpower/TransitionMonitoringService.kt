/*
 * BSD 3-Clause License
 *
 * Copyright 2020  Sage Bionetworks. All rights reserved.
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

package org.sagebionetworks.research.mpower

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat.Builder
import org.sagebionetworks.research.mpower.R.drawable
import org.sagebionetworks.research.mpower.R.string
import org.sagebionetworks.research.mpower.util.ActivityTransitionUtil
import org.sagebionetworks.research.mpower.util.ActivityTransitionUtil.OnSuccessListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransitionMonitoringService : Service() {

    private val binder = ServiceBinder()

    private lateinit var pendingIntent: PendingIntent

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()
        setupPendingIntentForActivityTransitions()
    }

    private fun setupPendingIntentForActivityTransitions() {
        Log.d(TAG, "setupPendingIntentForActivityTransitions")
        val intent = Intent(applicationContext, ActivityTransitionsReceiver::class.java)
        intent.action = ActivityTransitionsReceiver.INTENT_ACTION
        pendingIntent = PendingIntent.getBroadcast(applicationContext, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT)
    }

//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        Log.d(TAG, "onStartCommand: startId = $startId")
//        return START_REDELIVER_INTENT
//    }

    private fun timeToDateStr(dateInMs: Long): String {
        return if (dateInMs > 0) {
            SimpleDateFormat(TIME_PATTERN, Locale.US).format(Date(dateInMs))
        } else {
            ""
        }
    }

    private fun startForeground() {
        createNotificationChannel()
        startForeground(FOREGROUND_NOTIFICATION_ID, createNotification())
    }

    private fun createNotification(text: String? = null): Notification {
        return Builder(this, getString(string.foreground_channel_id))
                .setContentTitle(getText(string.monitoring_notification_title))
                .setContentText(text ?: getText(string.monitoring_notification_message))
                .setSmallIcon(drawable.ic_launcher_foreground)
                .build()
    }

    private fun updateNotification(text:String) {
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .notify(FOREGROUND_NOTIFICATION_ID, createNotification(text))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val title = getString(string.foreground_channel_title)
            val desc = getString(string.foreground_channel_desc)
            val importance = NotificationManager.IMPORTANCE_LOW
            val mChannel = NotificationChannel(getString(string.foreground_channel_id), title, importance)
            mChannel.description = desc
            // Register the channel with the system; can't change importance or other behaviors after this
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(mChannel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "onBind")
        startForeground()
        ActivityTransitionUtil.enable(this, pendingIntent, object : OnSuccessListener {
            override fun onSuccess() {
                // passiveGaitViewModel.trackingRegistered = true
            }
        })
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind")
        ActivityTransitionUtil.disable(this, pendingIntent, object : OnSuccessListener {
            override fun onSuccess() {
                // passiveGaitViewModel.trackingRegistered = true

            }
        })
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
    }

    inner class ServiceBinder : Binder() {
        val service: TransitionMonitoringService
            get() = this@TransitionMonitoringService
    }

    companion object {
        private const val TAG = "MonitoringService"

        private const val FOREGROUND_NOTIFICATION_ID = 10

        private const val TIME_PATTERN = "HH:mm:ss"
    }
}