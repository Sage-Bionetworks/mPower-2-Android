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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.text.TextUtils
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Handles intents from from the Transitions API.
 */
class ActivityTransitionsReceiver : BroadcastReceiver() {
    private val LOGGER = LoggerFactory.getLogger(ActivityTransitionsReceiver::class.java)

    override fun onReceive(context: Context, intent: Intent) {
        LOGGER.debug("onReceive")

        if (!TextUtils.equals(INTENT_ACTION, intent.action)) {
            LOGGER.debug("Received an unsupported action in TransitionsReceiver: action = ${intent.action}")
            return
        }

        val sharedPrefs = context.getSharedPreferences(TRANSITION_PREFS, Context.MODE_PRIVATE)
        val currentActivityType = sharedPrefs.getInt(CURRENT_ACTIVITY_TYPE, -1)
        LOGGER.debug("CURRENT ACTIVITY TYPE: ${toActivityString(currentActivityType)}")

        if (ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent)
            result?.run {
                for (event in result.transitionEvents) {
                    val info =
                        "Transition: ${toActivityString(event.activityType)} (${toTransitionType(
                            event.transitionType
                        )}), (${TimeUnit.NANOSECONDS.toSeconds(event.elapsedRealTimeNanos)})" +
                        " ${SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())}"
                    LOGGER.debug(info)
                }
            }
        }
        if (ActivityRecognitionResult.hasResult(intent)) {
            val result = ActivityRecognitionResult.extractResult(intent)
            result?.run {
                for (detectedActivity in result.probableActivities) {
                    when (detectedActivity.type) {
                        DetectedActivity.STILL -> {
                            handleDetectedActivity(context, detectedActivity, sharedPrefs)
                        }
                        DetectedActivity.WALKING -> {
                            handleDetectedActivity(context, detectedActivity, sharedPrefs)
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun onStartedWalking(context: Context) {
        LOGGER.debug("onStartedWalking ${SimpleDateFormat(
                "HH:mm:ss",
                Locale.US
        ).format(Date())}")
        val i = Intent(context, ActivityRecorderService::class.java)
        i.putExtra(ActivityRecorderService.EVENT, ActivityRecorderService.STARTED_WALKING)
        context.startService(i)
    }

    private fun onStoppedWalking(context: Context) {
        LOGGER.debug("onStoppedWalking ${SimpleDateFormat(
                "HH:mm:ss",
                Locale.US
        ).format(Date())}")
        val i = Intent(context, ActivityRecorderService::class.java)
        i.putExtra(ActivityRecorderService.EVENT, ActivityRecorderService.STOPPED_WALKING)
        context.startService(i)
    }


    private fun handleDetectedActivity(context: Context, detectedActivity: DetectedActivity, sharedPrefs: SharedPreferences) {
        if (detectedActivity.confidence > 40) {
            val currentActivityType = sharedPrefs.getInt(CURRENT_ACTIVITY_TYPE, -1)
            val info =
                    "Activity: ${toActivityString(detectedActivity.type)} (${detectedActivity.confidence}) ${SimpleDateFormat(
                            "HH:mm:ss",
                            Locale.US
                    ).format(Date())}"
            LOGGER.debug(info)

            if (currentActivityType < 0) {
                // no currentActivityType
                sharedPrefs.edit().putInt(CURRENT_ACTIVITY_TYPE, detectedActivity.type).commit()
            } else if (currentActivityType != detectedActivity.type) {
                if (currentActivityType == DetectedActivity.STILL
                        && detectedActivity.type == DetectedActivity.WALKING) {
                    sharedPrefs.edit().putInt(CURRENT_ACTIVITY_TYPE, detectedActivity.type).commit()
                    // transition from still to walking
                    onStartedWalking(context)
                } else if (currentActivityType == DetectedActivity.WALKING
                        && detectedActivity.type == DetectedActivity.STILL) {
                    sharedPrefs.edit().putInt(CURRENT_ACTIVITY_TYPE, detectedActivity.type).commit()
                    // transition from walking to still
                    onStoppedWalking(context)
                } else {
                    // do nothing
                }
            }
        }
    }

    companion object {
        // Action fired when transitions are triggered.
        const val INTENT_ACTION = "org.sagebionetworks.research.mpower.TRANSITIONS_RECEIVER_ACTION"

        const val CURRENT_ACTIVITY_TYPE = "currentActivityType"

        const val TRANSITION_PREFS = "transitionPrefs"

        private fun toActivityString(activity: Int): String {
            return when (activity) {
                DetectedActivity.STILL -> "STILL"
                DetectedActivity.WALKING -> "WALKING"
                else -> "UNKNOWN"
            }
        }

        private fun toTransitionType(transitionType: Int): String {
            return when (transitionType) {
                ActivityTransition.ACTIVITY_TRANSITION_ENTER -> "ENTER"
                ActivityTransition.ACTIVITY_TRANSITION_EXIT -> "EXIT"
                else -> "UNKNOWN"
            }
        }
    }
}