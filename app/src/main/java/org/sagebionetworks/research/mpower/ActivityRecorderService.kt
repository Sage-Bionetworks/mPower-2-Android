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
import androidx.core.app.NotificationCompat.Builder
import dagger.android.DaggerService
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import org.sagebionetworks.research.domain.async.AsyncActionConfiguration
import org.sagebionetworks.research.domain.async.DeviceMotionRecorderConfigurationImpl
import org.sagebionetworks.research.domain.async.MotionRecorderType
import org.sagebionetworks.research.domain.step.implementations.StepBase
import org.sagebionetworks.research.domain.step.interfaces.Step
import org.sagebionetworks.research.domain.task.navigation.NavDirection
import org.sagebionetworks.research.domain.task.navigation.TaskBase
import org.sagebionetworks.research.mpower.ActivityRecorderService.State.CANCELING
import org.sagebionetworks.research.mpower.ActivityRecorderService.State.CONNECTING
import org.sagebionetworks.research.mpower.ActivityRecorderService.State.FINISHED
import org.sagebionetworks.research.mpower.ActivityRecorderService.State.NEW
import org.sagebionetworks.research.mpower.ActivityRecorderService.State.RECORDED
import org.sagebionetworks.research.mpower.ActivityRecorderService.State.RECORDING
import org.sagebionetworks.research.mpower.ActivityRecorderService.State.SAVING
import org.sagebionetworks.research.mpower.R.drawable
import org.sagebionetworks.research.mpower.R.string
import org.sagebionetworks.research.mpower.research.MpIdentifier
import org.sagebionetworks.research.mpower.util.Repeat
import org.sagebionetworks.research.presentation.perform_task.TaskResultManager
import org.sagebionetworks.research.presentation.perform_task.TaskResultManager.TaskResultManagerConnection
import org.sagebionetworks.research.presentation.perform_task.TaskResultProcessingManager
import org.sagebionetworks.research.presentation.recorder.RecorderActionType
import org.sagebionetworks.research.presentation.recorder.sensor.SensorRecorderConfigPresentationFactory
import org.sagebionetworks.research.presentation.recorder.service.RecorderManager
import org.sagebionetworks.research.presentation.recorder.service.RecorderManager.RecorderServiceConnectionListener
import org.sagebionetworks.research.presentation.recorder.service.RecorderService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

class ActivityRecorderService : DaggerService(), RecorderServiceConnectionListener {

    @Inject
    lateinit var taskResultManager: TaskResultManager

    @Inject
    lateinit var recorderConfigPresentationFactory: SensorRecorderConfigPresentationFactory

    @Inject
    lateinit var taskResultProcessingManager: TaskResultProcessingManager

    private var recorderManager: RecorderManager? = null

    private val asyncActions = mutableSetOf<AsyncActionConfiguration>(
        DeviceMotionRecorderConfigurationImpl.builder()
            .setIdentifier(ACTION_IDENTIFIER)
            .setStartStepIdentifier(RecorderActionType.START)
            .setStopStepIdentifier(RecorderActionType.STOP)
            .setFrequency(null)
            .setRecorderTypes(mutableSetOf(
                MotionRecorderType.USER_ACCELERATION,
                MotionRecorderType.MAGNETIC_FIELD,
                MotionRecorderType.ROTATION_RATE,
                MotionRecorderType.GYROSCOPE
            ))
            .build()
    )

    private val startStep = PassiveGaitStep(RecorderActionType.START, asyncActions)

    private val stopStep = PassiveGaitStep(RecorderActionType.STOP, asyncActions)

    private val steps = mutableListOf<Step>(startStep, stopStep)

    private val taskUUID = UUID.randomUUID()

    private val task = TaskBase.builder()
            .setIdentifier(TASK_IDENTIFIER)
            .setAsyncActions(asyncActions)
            .setSteps(steps)
            .build()

    private var elapsedTime = 0L

    private val recordTimer = Repeat(1000, {
        val elapsedTimeStr = "${MAX_RECORDING_TIME_SEC - ++elapsedTime}".padStart(2, '0')
        if (elapsedTime == MAX_RECORDING_TIME_SEC) {
            stopRecording()
        } else {
            updateNotification("${getText(string.recording_notification_message)}$elapsedTimeStr")
        }
    }, Dispatchers.Default)

    private var state: State = NEW

    private var recorderService: RecorderService? = null

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()
        startForeground()
    }

    private fun setupRecorderManager() {
        state = CONNECTING
        Log.d(TAG, "-- setupRecorderManager ($state): $taskUUID")
        recorderManager = RecorderManager(task, TASK_IDENTIFIER, taskUUID, this,
                taskResultManager, recorderConfigPresentationFactory, this)
        taskResultProcessingManager.registerTaskRun(TASK_IDENTIFIER, taskUUID)

//        val connection: Single<TaskResultManagerConnection> = taskResultManager.getTaskResultManagerConnection(
//                TASK_IDENTIFIER, taskUUID)
//        CompositeDisposable().add(
//                connection
//                        .observeOn(Schedulers.io())
//                        .flatMapCompletable { trmc: TaskResultManagerConnection ->
//                            Completable.mergeDelayError(
//                                            Flowable.fromIterable<TaskResultProcessor>(taskResultProcessors)
//                                                    .map { trp: TaskResultProcessor ->
//                                                        trmc.finalTaskResult
//                                                                .flatMapCompletable { taskResult: TaskResult? ->
//                                                                    trp.processTaskResult(
//                                                                            taskResult)
//                                                                }
//                                                    })
//                                    .doOnComplete { trmc.disconnect() }
//                        }
//                        .subscribe({
//                            Log.d(TAG, "Finished processing task for identifier: {$TASK_IDENTIFIER}, task run: {$taskUUID}")
//                        }) { t: Throwable? ->
//                            Log.w(TAG, "Error processing task result", t)
//                        })
    }

    /**
     * @see RecorderManager.RecorderServiceConnectionListener
     */
    override fun onRecorderServiceConnected(recorderService: RecorderService, bound: Boolean) {
        if (state == CONNECTING) {
            this.recorderService = recorderService
            recorderManager?.onStepTransition(null, startStep, NavDirection.SHIFT_RIGHT)
            state = RECORDING
            elapsedTime = 0
            recordTimer.start()
            Log.d(TAG, "onServiceConnected ($state)")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand ($state): startId = $startId")

        intent?.let {
            when (val event = it.getSerializableExtra(EVENT) as Event) {
                Event.STARTED_WALKING -> startRecording()
                Event.STOPPED_WALKING -> stopRecording()
                else -> Log.d(TAG, "EVENT is invalid: $event")
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
        if (state == NEW) {
            val sharedPrefs = getSharedPreferences(TRANSITION_PREFS, Context.MODE_PRIVATE)
            val lastRecordedAt = sharedPrefs.getLong(LAST_RECORDED_AT, -1)
            val now = Date().time

            Log.d(TAG, "-- lastRecordedAt: ${timeToDateStr(lastRecordedAt)}, now: ${timeToDateStr(now)}")

            if (now - lastRecordedAt > MIN_FREQUENCY_MS) {
                Log.d(TAG, "-- startRecording ($state): ${timeToDateStr(now)}")
                setupRecorderManager()
            } else {
                Log.d(TAG, "-- NOT RECORDING - ONLY RECORD ONCE EVERY $MIN_FREQUENCY_MS")
                finish()
            }
        }
    }

    private fun startForeground() {
        createNotificationChannel()
        startForeground(FOREGROUND_NOTIFICATION_ID, createNotification())
    }

    private fun createNotification(text: String? = null): Notification {
        return Builder(this, getString(string.foreground_channel_id))
                .setContentTitle(getText(string.recording_notification_title))
                .setContentText(text ?: getText(string.recording_notification_message))
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

    //WALK END EVENT - Ignore if recording is already done
    // Should not need to call startForeground as service is either already running or it wasn't and
    // this is just a NO-OP and a call to stopService.
    // Stop recording
    // Save to bridge - any recording is worth saving
    // Stop service
    private fun stopRecording() {
        if (state == RECORDING) {
            Log.d(
                TAG,
                "-- stopRecording ($state): ${SimpleDateFormat(TIME_PATTERN, Locale.US).format(Date())}"
            )

            recordTimer.cancel()

            state = RECORDED

            if (elapsedTime >= MIN_RECORDING_TIME_SEC) {
                recorderManager?.onStepTransition(stopStep, null, NavDirection.SHIFT_LEFT)

                val sharedPrefs = getSharedPreferences(TRANSITION_PREFS, Context.MODE_PRIVATE)
                sharedPrefs.edit().putLong(LAST_RECORDED_AT, Date().time).apply()

                saveToBridge()
            } else {
                cancelRecording()
            }
        }
        if (state == NEW|| state == CONNECTING) {
            finish()
        }

    }

    private fun saveToBridge() {
        state = SAVING
        Log.d(TAG, "-- saveToBridge ($state)")

        // MpDataProvider.getInstance().uploadTaskResult()
        // taskResultProcessingManager.registerTaskRun(TASK_IDENTIFIER, taskUUID)

        finish()
    }

    private fun cancelRecording() {
        state = CANCELING
        Log.d(TAG, "-- cancelRecording ($state)")
        this.recorderService?.cancelRecorder(taskUUID, ACTION_IDENTIFIER)
        finish()
    }

    private fun finish() {
        state = FINISHED
        Log.d(TAG, "-- finish ($state)")
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        recorderManager?.unbind()
        super.onDestroy()
    }

    inner class PassiveGaitStep(identifier: String, asyncActions: Set<AsyncActionConfiguration>)
        : StepBase(identifier, asyncActions) {

        override fun copyWithIdentifier(identifier: String): PassiveGaitStep {
            throw UnsupportedOperationException("PassiveGait steps cannot be copied")
        }
    }

    enum class Event {
        STARTED_WALKING, STOPPED_WALKING
    }

    private enum class State {
        NEW, CONNECTING, RECORDING, RECORDED, SAVING, CANCELING, FINISHED
    }

    companion object {
        private const val TAG = "ActivityRecorderService"

        private const val TASK_IDENTIFIER = MpIdentifier.PASSIVE_GAIT

        private const val ACTION_IDENTIFIER = MpIdentifier.PASSIVE_GAIT // "passiveGait"

        private const val TRANSITION_PREFS = "transitionPrefs"

        private const val LAST_RECORDED_AT = "lastRecordedAt"

        private const val TIME_PATTERN = "HH:mm:ss"

        private const val FOREGROUND_NOTIFICATION_ID = 100

        private const val MIN_FREQUENCY_MS: Long = 1000 * 60 * 3

        private const val MIN_RECORDING_TIME_SEC = 15L

        private const val MAX_RECORDING_TIME_SEC = 30L

        const val EVENT = "EVENT"
    }
}