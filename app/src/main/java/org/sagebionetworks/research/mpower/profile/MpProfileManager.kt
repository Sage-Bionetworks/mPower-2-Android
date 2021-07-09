/*
 * BSD 3-Clause License
 *
 * Copyright 2021  Sage Bionetworks. All rights reserved.
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

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.MediatorLiveData
import io.reactivex.Single
import org.sagebionetworks.bridge.android.manager.AuthenticationManager
import org.sagebionetworks.bridge.android.manager.models.ProfileDataManager
import org.sagebionetworks.bridge.android.manager.models.ProfileDataSource
import org.sagebionetworks.bridge.rest.model.StudyParticipant
import org.sagebionetworks.research.sageresearch.dao.room.AppConfigRepository
import org.sagebionetworks.research.sageresearch.dao.room.ReportEntity
import org.sagebionetworks.research.sageresearch.dao.room.ReportRepository
import org.sagebionetworks.research.sageresearch.profile.ProfileDataLoader

/**
 * The ProfileManager loads ProfileDataSources and a ProfileDataManager from AppConfig, useing the data
 * from these it loads the necessary Reports, StudyParticipant and returns a ProfileDataLoader.
 */
class MpProfileManager(
        val reportRepo: ReportRepository,
        val appConfigRepo: AppConfigRepository,
        val authManager: AuthenticationManager) {


    fun loadProfileDataSources() : LiveData<Map<String, ProfileDataSource>> {
        val profileDataSourceLiveData = LiveDataReactiveStreams.fromPublisher(
                appConfigRepo.profileDataSources.toFlowable())
        return profileDataSourceLiveData
    }

    private fun loadDefaultProfileDataManager() : Single<ProfileDataManager> {
        return appConfigRepo.profileDataManagers.map {
            it.get("ProfileManager")
        }
    }

    private fun loadDefaultProfileManagerLiveData() : LiveData<ProfileDataManager> {
        return LiveDataReactiveStreams.fromPublisher(loadDefaultProfileDataManager().toFlowable())
    }

    private fun loadReports(reportKeys: Set<String>): LiveData<Map<String, ReportEntity?>> {
        val mediator = MediatorLiveData<Map<String, ReportEntity?>>().apply {
            val numSources = reportKeys.size
            val reportMap: MutableMap<String, ReportEntity?> = mutableMapOf()

            fun update() {
                if (numSources == reportMap.size) {
                    this.value = reportMap
                }
            }

            for (key in reportKeys) {
                val reportLiveData = reportRepo.fetchMostRecentReport(key)
                addSource(reportLiveData) {
                    val report = it?.firstOrNull()
                    reportMap.put(key, report)
                    update()
                }

            }

        }
        return mediator
    }

    fun profileDataLoader(): LiveData<ProfileDataLoader> {
        val mediator = MediatorLiveData<ProfileDataLoader>().apply {
            var profileDataManager: ProfileDataManager? = null
            var reportMap: Map<String, ReportEntity?>? = null

            fun update() {
                if (profileDataManager != null && reportMap != null) {
                    val user = authManager.userSessionInfo
                    val studyParticipant = StudyParticipant()
                    studyParticipant.dataGroups = user?.dataGroups ?: listOf()
                    studyParticipant.firstName = user?.firstName ?: ""
                    studyParticipant.externalId = user?.externalId
                    studyParticipant.sharingScope = user?.sharingScope

                    this.value = ProfileDataLoader(profileDataManager!!, studyParticipant, reportMap!!)
                }
            }

            addSource(loadDefaultProfileManagerLiveData()) {
                profileDataManager = it
                it?.reportIds?.let { item ->
                    addSource(loadReports(item)) {
                        reportMap = it
                        update()
                    }
                    update()
                }
            }
        }
        return mediator
    }
}