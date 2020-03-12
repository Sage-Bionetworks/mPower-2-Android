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
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActivityRecorderService : Service() {
    private var isRecording = false

//    private val recorderService = RecorderService()
//
//    private var taskUUID: UUID? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: startId = $startId, isRecording = $isRecording")

        intent?.let {
            when (val event = it.getIntExtra(EVENT, -1)) {
                STARTED_WALKING -> startRecording()
                STOPPED_WALKING -> stopRecording()
                else -> throw IllegalArgumentException("EVENT is invalid: $event")
            }
        }

        return START_REDELIVER_INTENT
    }

    private fun timeToDateStr(dateInMs: Long): String {
        return if (dateInMs > 0) {
            SimpleDateFormat(TIME_PATTERN, Locale.US).format(Date(dateInMs))
        } else {
            ""
        }
    }

    //WALK START EVENT - Ignore if already recording
    // Call startForeground with notification to show user - need design from Design team
    // Create Task
    // Initialize RecorderManager
    // Record 30 seconds of walk data
    // Stop recording
    // Save to Bridge
    // Stop service
    private fun startRecording() {
        if (!isRecording) {
            val sharedPrefs = getSharedPreferences(TRANSITION_PREFS, Context.MODE_PRIVATE)
            val lastRecordedAt = sharedPrefs.getLong(LAST_RECORDED_AT, -1)
            val now = Date().time

            Log.d(TAG, "-- lastRecordedAt: ${timeToDateStr(lastRecordedAt)}, now: ${timeToDateStr(now)}")

            if (now - lastRecordedAt > MIN_FREQUENCY_MS) {
                Log.d(TAG, "-- startRecording ${timeToDateStr(now)}")
                isRecording = true
                startForeground()
                CoroutineScope(Dispatchers.Default).launch {
//                    taskUUID = UUID.randomUUID()
//                    recorderService.getActiveRecorders(taskUUID!!).forEach {
//                        it.value.start()
//                    }
                    delay(MAX_RECORDING_TIME_MS)
                    stopRecording()
                }
            } else {
                Log.d(TAG, "-- NOT RECORDING - ONLY RECORD ONCE EVERY $MIN_FREQUENCY_MS")
                stopSelf()
            }
        }
    }

    private fun startForeground() {
        createNotificationChannel()
        val notification: Notification = NotificationCompat.Builder(this, getString(R.string.foreground_channel_id))
            .setContentTitle(getText(R.string.recording_notification_title))
            .setContentText(getText(R.string.recording_notification_message))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
        startForeground(FOREGROUND_NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val title = getString(R.string.foreground_channel_title)
            val desc = getString(R.string.foreground_channel_desc)
            val importance = NotificationManager.IMPORTANCE_LOW
            val mChannel = NotificationChannel(getString(R.string.foreground_channel_id), title, importance)
            mChannel.description = desc
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }

    //WALK END EVENT - Ignore if recording is already done
    // Should not need to call startForeground as service is either already running or it wasn't and
    // this is just a NO-OP and a call to stopService.
    // Stop recording
    // Save to bridge - any recording is worth saving
    // Stop service
    private fun stopRecording() {
        if (isRecording) {
            Log.d(
                TAG,
                "-- stopRecording ${SimpleDateFormat(TIME_PATTERN, Locale.US).format(Date())}"
            )
            isRecording = false
//            CoroutineScope(Dispatchers.Default).launch {
//                recorderService.getActiveRecorders(taskUUID!!).forEach {
//                    it.value.stop()
//                }
//            }
            val sharedPrefs = getSharedPreferences(TRANSITION_PREFS, Context.MODE_PRIVATE)
            sharedPrefs.edit().putLong(LAST_RECORDED_AT, Date().time).apply()
        }
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
    }
    companion object {
        private const val TAG = "ActivityRecorderService"

        private const val TRANSITION_PREFS = "transitionPrefs"

        private const val LAST_RECORDED_AT = "lastRecordedAt"

        private const val TIME_PATTERN = "HH:mm:ss"

        private const val FOREGROUND_NOTIFICATION_ID = 100

        private const val MIN_FREQUENCY_MS: Long = 1000 * 60 * 3

        private const val MAX_RECORDING_TIME_MS: Long = 1000 * 30

        const val EVENT = "EVENT"

        const val STARTED_WALKING = 1

        const val STOPPED_WALKING = 0
    }
}