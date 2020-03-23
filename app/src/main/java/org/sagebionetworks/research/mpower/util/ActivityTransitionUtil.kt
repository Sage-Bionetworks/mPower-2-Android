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

package org.sagebionetworks.research.mpower.util

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransition
import org.slf4j.LoggerFactory

object ActivityTransitionUtil {

    private val LOGGER = LoggerFactory.getLogger(ActivityTransitionUtil::class.java)

    private val RUNNING_Q_OR_LATER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    private const val ACTIVITY_RESULT_FREQUENCY_IN_MS: Long = 3000

    const val PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 45

    /**
     * Registers callbacks for [ActivityTransition] events via a custom [BroadcastReceiver]
     */
    fun enable(context:Context, pendingIntent:PendingIntent,
            onSuccessListener: OnSuccessListener? = null) {
        if (activityRecognitionPermissionApproved(context)) {
            LOGGER.debug("enableActivityTransitions()")
            val task: com.google.android.gms.tasks.Task<Void> = ActivityRecognition.getClient(context)
                    .requestActivityUpdates(ACTIVITY_RESULT_FREQUENCY_IN_MS, pendingIntent)
            // .requestActivityTransitionUpdates(ActivityTransitionRequest(activityTransitions), pendingIntent)

            task.addOnSuccessListener {
                onSuccessListener?.onSuccess()
                LOGGER.debug("Transitions Api was successfully registered.")
            }.addOnFailureListener { e ->
                LOGGER.error("Transitions Api could NOT be registered: $e", e)
            }
        }
//        else {
//            ActivityCompat.requestPermissions(
//                    activity, arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
//                    PERMISSION_REQUEST_ACTIVITY_RECOGNITION
//            )
//        }
    }

    /**
     * Unregisters callbacks for [ActivityTransition] events via a custom [BroadcastReceiver]
     */
    fun disable(context:Context, pendingIntent:PendingIntent,
            onSuccessListener: OnSuccessListener? = null) {
        LOGGER.debug("disableActivityTransitions()")
        val task: com.google.android.gms.tasks.Task<Void> = ActivityRecognition.getClient(context)
                .removeActivityUpdates(pendingIntent)
        // .removeActivityTransitionUpdates(pendingIntent)

        task.addOnSuccessListener {
            onSuccessListener?.onSuccess()
            LOGGER.debug("Transitions successfully unregistered.")
        }.addOnFailureListener { e ->
            LOGGER.error("Transitions could not be unregistered: $e", e)
        }
    }

    /**
     * On devices Android 10 and beyond (29+), you need to ask for the ACTIVITY_RECOGNITION via the
     * run-time permissions.
     */
    fun activityRecognitionPermissionApproved(context:Context): Boolean {
        // TODO: Integrate consent alongside os permissions
        return if (RUNNING_Q_OR_LATER) {
            PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            )
        } else {
            true
        }
    }

    interface OnSuccessListener {
        fun onSuccess()
    }
}