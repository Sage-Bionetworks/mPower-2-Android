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

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import hu.akarnokd.rxjava.interop.RxJavaInterop
import io.reactivex.Single
import org.sagebionetworks.bridge.android.manager.AuthenticationManager
import org.sagebionetworks.bridge.android.manager.models.ProfileDataSource
import org.sagebionetworks.bridge.researchstack.BridgeDataProvider
import org.sagebionetworks.bridge.rest.model.SharingScope
import org.sagebionetworks.bridge.rest.model.StudyParticipant
import org.sagebionetworks.bridge.rest.model.Survey
import org.sagebionetworks.bridge.rest.model.SurveyReference
import org.sagebionetworks.research.sageresearch.dao.room.AppConfigRepository
import org.sagebionetworks.research.sageresearch.dao.room.ReportRepository
import org.sagebionetworks.research.sageresearch.dao.room.ScheduledActivityEntity
import org.sagebionetworks.research.sageresearch.dao.room.SurveyRepository
import org.sagebionetworks.research.sageresearch.profile.ProfileDataLoader
import org.sagebionetworks.research.sageresearch.profile.ProfileManager
import org.sagebionetworks.research.sageresearch.repos.BridgeRepositoryManager
import org.slf4j.LoggerFactory

open class MpBaseProfileViewModel(
        val bridgeRepoManager: BridgeRepositoryManager,
        val reportRepo: ReportRepository,
        val appConfigRepo: AppConfigRepository,
        val surveyRepo: SurveyRepository,
        val authManager: AuthenticationManager,
        app: Application) : AndroidViewModel(app) {

    private val LOGGER = LoggerFactory.getLogger(MpBaseProfileViewModel::class.java)

    val profileManager = MpProfileManager(reportRepo, appConfigRepo, authManager, app)

    val compositeDisposable = io.reactivex.disposables.CompositeDisposable()

    var currentScheduledActivity: ScheduledActivityEntity? = null

    private var cachedProfileDataLoader: ProfileDataLoader? = null

    private val sharedPrefs: SharedPreferences = MpProfileManager.createSharedPrefs(app)

    private fun profileDataLoader(): LiveData<ProfileDataLoader> {
        return profileManager.profileDataLoader()
    }

    private fun profileDataSource(): LiveData<Map<String, ProfileDataSource>> {
        return profileManager.loadProfileDataSources()
    }

    fun profileData(key: String): LiveData<Pair<ProfileDataSource?, ProfileDataLoader?>> {

        val mediator = MediatorLiveData<Pair<ProfileDataSource?, ProfileDataLoader?>>().apply {
            var profileDataLoader: ProfileDataLoader? = null
            var profileDataSource: ProfileDataSource? = null

            fun update() {
                if (profileDataLoader != null && profileDataSource != null) {
                    this.value = Pair(profileDataSource, profileDataLoader)
                }
            }

            addSource(profileDataSource()) {
                if (it != null) {
                    profileDataSource = it.get(key)
                }
                update()
            }

            addSource(profileDataLoader()) {
                profileDataLoader = it
                cachedProfileDataLoader = it
                update()
            }
        }
        return mediator

    }

    fun saveStudyParticipantValue(value: String, profileItemKey: String) {
        var studyParticipant: StudyParticipant? = null
        when (profileItemKey) {
            "firstName" -> {
                MpProfileManager.updateFirstNameInCache(sharedPrefs, value)
                cachedProfileDataLoader?.participantData?.firstName = value
                studyParticipant = StudyParticipant()
                studyParticipant.firstName = value
            }
            "sharingScope" -> {
                val scope = SharingScope.fromValue(value)
                MpProfileManager.updateSharingScopeInCache(sharedPrefs, value)
                cachedProfileDataLoader?.participantData?.sharingScope = scope
                studyParticipant = StudyParticipant()
                studyParticipant.sharingScope = scope
            }
        }
        if (studyParticipant != null) {
            compositeDisposable.add(RxJavaInterop.toV2Observable(BridgeDataProvider.getInstance()
                    .updateStudyParticipant(studyParticipant))
                    .subscribe(
                            { userSessionInfo -> LOGGER.info("Successfully updated study participant") },
                            { throwable -> LOGGER.warn("Error updating study participant") }))
        }
    }

    fun loadSurvey(surveyReference: SurveyReference): Single<Survey> {
        return surveyRepo.getSurvey(surveyReference).doOnSuccess { loadScheduledActivity(it.identifier) }
    }

    private fun loadScheduledActivity(surveyId: String) {
        compositeDisposable.add(bridgeRepoManager.scheduleRepo.scheduleDao.activityGroupFlowable(setOf(surveyId))
                .firstOrError()
                .subscribe({ currentScheduledActivity = it.firstOrNull() }, { "fail" })
        )
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}
