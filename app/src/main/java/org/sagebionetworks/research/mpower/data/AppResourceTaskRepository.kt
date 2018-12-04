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

package org.sagebionetworks.research.mpower.data

import android.content.Context
import com.google.gson.Gson
import io.reactivex.Single
import org.sagebionetworks.bridge.android.manager.AuthenticationManager
import org.sagebionetworks.research.data.ResourceTaskRepository
import org.sagebionetworks.research.domain.task.Task
import org.sagebionetworks.research.domain.task.navigation.TaskBase
import org.sagebionetworks.research.mpower.research.DataSourceManager
import org.sagebionetworks.research.mpower.tracking.model.TrackingStep
import javax.inject.Inject

class AppResourceTaskRepository
    @Inject constructor (context: Context, gson: Gson, private val authManager: AuthenticationManager):
        ResourceTaskRepository(context, gson) {

    companion object {
        const val medicationTimingTaskIdentifier = "ActivityTracking"
    }

    /**
     * @property shouldIncludeMedicationTiming true if user has been diagnosed with Parkinson's,
     *                                         false otherwise, they are in the control group.
     */
    val shouldIncludeMedicationTiming: Boolean get() {
        val dataGroups = authManager.userSessionInfo?.dataGroups ?: return false
        return dataGroups.contains(DataSourceManager.parkinsonsDataGroup)
    }

    override fun getTask(taskIdentifier: String?): Single<Task> {
        val taskId = taskIdentifier ?: run {
            return Single.error(Throwable("Cannot create a task with a null taskIdentifier"))
        }

        val isTrackingTask = DataSourceManager.trackingGroup.activityIdentifiers.contains(taskId)
        val isMeasuringTask = DataSourceManager.measuringGroup.activityIdentifiers.contains(taskId)

        return when {
            isTrackingTask -> getTrackingTask(taskId)
            isMeasuringTask -> getMeasuringTask(taskId)
            else -> super.getTask(taskIdentifier)
        }
    }

    /**
     * Creates a tracking task by using custom de-serializing to the TrackingStep json object.
     * @param trackingTaskId the identifier of the tracking task to load.
     * @return a Single that will do work to return the appropriate tracking task.
     */
    private fun getTrackingTask(trackingTaskId: String): Single<Task> {
        return Single.fromCallable {
            val trackingStep =
                    gson.fromJson(getJsonTaskAsset(trackingTaskId), TrackingStep::class.java)
            TaskBase.builder()
                    .setIdentifier(trackingStep.identifier)
                    .setSteps(listOf(trackingStep)).build()
        }
    }

    /**
     * Creates a measuring task by checking to see if the user has been diagnosed with Parkinson's.
     * If so, they will see a medication timing question step at the beginning of the task.
     * If not, the task will be created to send them right into measuring.
     * @param measuringTaskId the identifier of the measuring task to load.
     * @return a Single that will do work to return the appropriate measuring task.
     */
    private fun getMeasuringTask(measuringTaskId: String): Single<Task> {
        return super.getTask(measuringTaskId)
            .map {
                // There is special logic in mPower where a user with Parkinson's will see
                // a medication timing question before each measuring task.
                val allSteps = it.steps.toMutableList()
                if (shouldIncludeMedicationTiming) {
                    val activityTracking =
                            gson.fromJson(getJsonTaskAsset(medicationTimingTaskIdentifier), Task::class.java)
                    allSteps.addAll(0, activityTracking.steps)
                }
                it.copyWithSteps(allSteps)
            }
    }
}