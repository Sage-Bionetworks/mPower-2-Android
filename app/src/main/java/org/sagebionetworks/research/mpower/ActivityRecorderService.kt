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
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import dagger.android.DaggerService
import io.reactivex.Single
import org.sagebionetworks.research.domain.async.AsyncActionConfiguration
import org.sagebionetworks.research.domain.async.DeviceMotionRecorderConfigurationImpl
import org.sagebionetworks.research.domain.async.DistanceRecorderConfigurationImpl
import org.sagebionetworks.research.domain.async.RecorderType
import org.sagebionetworks.research.domain.result.interfaces.TaskResult
import org.sagebionetworks.research.domain.step.implementations.StepBase
import org.sagebionetworks.research.domain.step.interfaces.Step
import org.sagebionetworks.research.domain.task.Task
import org.sagebionetworks.research.domain.task.navigation.TaskBase
import org.sagebionetworks.research.presentation.inject.RecorderConfigPresentationFactory
import org.sagebionetworks.research.presentation.perform_task.TaskResultManager
import org.sagebionetworks.research.presentation.perform_task.TaskResultManager.TaskResultManagerConnection
import org.sagebionetworks.research.presentation.recorder.service.RecorderManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

class ActivityRecorderService : DaggerService() {
    private var isRecording = false

    @Inject
    lateinit var taskResultManager: TaskResultManager

    @Inject
    lateinit var recorderConfigPresentationFactory: RecorderConfigPresentationFactory

    private lateinit var recorderManager: RecorderManager

    private lateinit var task: Task

    private lateinit var taskIdentifier: String

    private lateinit var taskUUID: UUID

    private val asyncActions = mutableSetOf<AsyncActionConfiguration>(
        DeviceMotionRecorderConfigurationImpl.builder()
            .setIdentifier("motionRecorder")
            .setStartStepIdentifier("start")
            .setStopStepIdentifier("stop")
            .setFrequency(null)
            .setRecorderTypes(mutableSetOf(RecorderType.MOTION))
            .build()
//        DistanceRecorderConfigurationImpl.builder()
//            .setIdentifier("distanceRecorder")
//            .setStartStepIdentifier("start")
//            .setStopStepIdentifier("stop")
//            .build()
    )

    private val startStep = PassiveGaitStep("start", asyncActions)

    private val stopStep = PassiveGaitStep("stop", asyncActions)

    private val steps = mutableListOf<Step>(startStep, stopStep)

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: $taskResultManager, $recorderConfigPresentationFactory")

        task = TaskBase.builder()
                .setIdentifier("PassiveGait")
                .setAsyncActions(asyncActions)
                .setSteps(steps)
                .build()
        taskIdentifier = "PassiveGait"
        taskUUID = UUID.randomUUID()

        recorderManager = RecorderManager(task, taskIdentifier, taskUUID, this,
                taskResultManager, recorderConfigPresentationFactory)

        val singleTaskRResultManagerConn: Single<TaskResultManagerConnection> = taskResultManager.getTaskResultManagerConnection(taskIdentifier, taskUUID)
        Log.d(TAG, "$singleTaskRResultManagerConn")
        singleTaskRResultManagerConn
                .doOnSuccess { conn ->
                    println("TEST - doOnSuccss: $conn")
                    conn.asyncResultsObservable.doOnEach { r -> Log.d(TAG, "doOnEach: $r") }
                }
                .doOnError { e ->
                    println("TEST - doOnError: ${e.message}")
                }
    }

    private fun taskResultObserver(taskResult: TaskResult?) {
        Log.d(TAG, "Observed TaskResult: {$taskResult}")
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
//                CoroutineScope(Dispatchers.Default).launch {
//                    delay(MAX_RECORDING_TIME_MS)
//                    stopRecording()
//                }
                // NOTE: Currently RecorderService within RecorderManager is not bound, need to fix
                // don't care about navDirection
                recorderManager.onStepTransition(null, startStep, 0)
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
            recorderManager.onStepTransition(stopStep, null, 0)

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
        unbindService(recorderManager)
        super.onDestroy()
    }

    inner class PassiveGaitStep(identifier: String, asyncActions: Set<AsyncActionConfiguration>)
        : StepBase(identifier, asyncActions) {

        override fun copyWithIdentifier(identifier: String): PassiveGaitStep {
            throw UnsupportedOperationException("PassiveGait steps cannot be copied")
        }
    }

    companion object {
        private const val TAG = "ActivityRecorderService"

        private const val TRANSITION_PREFS = "transitionPrefs"

        private const val LAST_RECORDED_AT = "lastRecordedAt"

        private const val TIME_PATTERN = "HH:mm:ss"

        private const val FOREGROUND_NOTIFICATION_ID = 100

        private const val MIN_FREQUENCY_MS: Long = 0 // 1000 * 60 * 3

        private const val MAX_RECORDING_TIME_MS: Long = 1000 * 30

        const val EVENT = "EVENT"

        const val STARTED_WALKING = 1

        const val STOPPED_WALKING = 0
    }
}