/*
 * BSD 3-Clause License
 *
 * Copyright 2019  Sage Bionetworks. All rights reserved.
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

package org.sagebionetworks.research.mpower.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.common.base.Preconditions
import org.sagebionetworks.research.mpower.research.MpIdentifier
import org.sagebionetworks.research.sageresearch.dao.room.HistoryItemManager
import org.sagebionetworks.research.sageresearch.dao.room.ReportEntity
import org.sagebionetworks.research.sageresearch.dao.room.ReportRepository
import javax.inject.Inject

class HistoryItemViewModel(val historyItemManager: HistoryItemManager, val reportRepo: ReportRepository): ViewModel() {

    class Factory @Inject constructor(private val historyItemManager: HistoryItemManager, val reportRepo: ReportRepository): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            Preconditions.checkArgument(modelClass.isAssignableFrom(HistoryItemViewModel::class.java))
            return HistoryItemViewModel(historyItemManager, reportRepo) as T
        }
    }

    /**
     * Trigger calls to Bridge server to load all reports required by history view.
     */
    fun loadHistoryReports() {
        val reportIds = listOf(MpIdentifier.MEDICATION, MpIdentifier.TRIGGERS, MpIdentifier.SYMPTOMS,
                MpIdentifier.WALK_AND_BALANCE, MpIdentifier.TREMOR, MpIdentifier.TAPPING)

        for (id in reportIds) {
            reportRepo.fetchMostRecentReport(id)
        }

    }

}