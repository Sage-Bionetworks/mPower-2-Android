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

package org.sagebionetworks.research.mpower.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.common.base.Preconditions
import org.sagebionetworks.research.mpower.research.MpIdentifier
import org.sagebionetworks.research.sageresearch.dao.room.ReportRepository
import org.sagebionetworks.researchstack.backbone.result.StepResult
import org.sagebionetworks.researchstack.backbone.result.TaskResult
import org.sagebionetworks.researchstack.backbone.step.Step
import java.util.Date
import javax.inject.Inject

class PassiveGaitPermissionViewModel(val reportRepo: ReportRepository): ViewModel() {
    var passiveDataAllowed: Boolean = false

    class Factory @Inject constructor(
            private val reportRepo: ReportRepository) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            Preconditions.checkArgument(modelClass.isAssignableFrom(PassiveGaitPermissionViewModel::class.java))
            return PassiveGaitPermissionViewModel(reportRepo) as T
        }
    }

    fun createSaveTaskResult(passiveDataAllowed: Boolean): TaskResult? {
        val stepPassiveDataAllowed = StepResult<Boolean>(Step(PROFILE_KEY_PASSIVE_DATA_ALLOWED))
        stepPassiveDataAllowed.result = passiveDataAllowed

        val taskResult = TaskResult(MpIdentifier.PASSIVE_DATA_PERMISSION)
        taskResult.startDate = Date()
        taskResult.endDate = Date()
        taskResult.results[PROFILE_KEY_PASSIVE_DATA_ALLOWED] = stepPassiveDataAllowed

        return taskResult
    }

    companion object {
        const val PROFILE_KEY_PASSIVE_DATA_ALLOWED = "passiveDataAllowed"
    }
}
