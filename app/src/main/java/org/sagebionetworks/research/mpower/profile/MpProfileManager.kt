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

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.MediatorLiveData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.android.DaggerService
import io.reactivex.Single
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.sagebionetworks.bridge.android.manager.AuthenticationManager
import org.sagebionetworks.bridge.android.manager.models.ProfileDataManager
import org.sagebionetworks.bridge.android.manager.models.ProfileDataSource
import org.sagebionetworks.bridge.rest.gson.ByteArrayToBase64TypeAdapter
import org.sagebionetworks.bridge.rest.gson.DateTimeTypeAdapter
import org.sagebionetworks.bridge.rest.gson.LocalDateTypeAdapter
import org.sagebionetworks.bridge.rest.model.SharingScope
import org.sagebionetworks.bridge.rest.model.StudyParticipant
import org.sagebionetworks.research.sageresearch.dao.room.AppConfigRepository
import org.sagebionetworks.research.sageresearch.dao.room.InstantAdapter
import org.sagebionetworks.research.sageresearch.dao.room.LocalDateAdapter
import org.sagebionetworks.research.sageresearch.dao.room.LocalDateTimeAdapter
import org.sagebionetworks.research.sageresearch.dao.room.ReportEntity
import org.sagebionetworks.research.sageresearch.dao.room.ReportRepository
import org.sagebionetworks.research.sageresearch.profile.ProfileDataLoader
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZonedDateTime

/**
 * The ProfileManager loads ProfileDataSources and a ProfileDataManager from AppConfig, useing the data
 * from these it loads the necessary Reports, StudyParticipant and returns a ProfileDataLoader.
 */
class MpProfileManager(
        val reportRepo: ReportRepository,
        val appConfigRepo: AppConfigRepository,
        val authManager: AuthenticationManager,
        context: Context) {

    companion object {
        const val participantKey = "StudyParticipant"

        fun createSharedPrefs(context: Context): SharedPreferences {
            return context.getSharedPreferences("MpProfileManager", DaggerService.MODE_PRIVATE)
        }

        fun cachedStudyParticipant(sharedPrefs: SharedPreferences): MpStudyParticipantProfile? {
            val participant = sharedPrefs.getString(participantKey, null) ?: run { return null }
            return Gson().fromJson(participant, MpStudyParticipantProfile::class.java)
        }

        fun cacheStudyParticipant(sharedPrefs: SharedPreferences, participant: MpStudyParticipantProfile) {
            val json = Gson().toJson(participant)
            sharedPrefs.edit().putString(participantKey, json).apply()
        }

        fun updateSharingScopeInCache(sharedPrefs: SharedPreferences, scope: String) {
            cachedStudyParticipant(sharedPrefs)?.let {
                val new = it.copy(sharingScope = scope)
                cacheStudyParticipant(sharedPrefs, new)
            }
        }

        fun updateFirstNameInCache(sharedPrefs: SharedPreferences, firstName: String) {
            cachedStudyParticipant(sharedPrefs)?.let {
                val new = it.copy(firstName = firstName)
                cacheStudyParticipant(sharedPrefs, new)
            }
        }
    }

    val sharedPrefs = createSharedPrefs(context)

    val gsonBuilder = GsonBuilder()
            .registerTypeAdapter(DateTime::class.java, DateTimeTypeAdapter())

    val gson = gsonBuilder.create()

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

            fun userSessionStudyParticipant(): StudyParticipant {
                val user = authManager.userSessionInfo
                val createdOn = user?.createdOn ?: DateTime.now()
                val datTimeStr = MpStudyParticipantProfile.createdOnFormatter.print(createdOn)
                val participantJson = "{\"createdOn\": \"$datTimeStr\"}"
                val studyParticipant = gson.fromJson(participantJson, StudyParticipant::class.java)
                studyParticipant.dataGroups = user?.dataGroups ?: listOf()
                studyParticipant.firstName = user?.firstName ?: ""
                studyParticipant.externalId = user?.externalId
                studyParticipant.sharingScope = user?.sharingScope
                return studyParticipant
            }

            fun update() {
                if (profileDataManager != null && reportMap != null) {
                    val cached = cachedStudyParticipant(sharedPrefs)?.toStudyParticipant(gson)
                    val participant = cached ?: userSessionStudyParticipant()
                    if (cached == null) {
                        val new = MpStudyParticipantProfile.fromStudyParticipant(participant)
                        cacheStudyParticipant(sharedPrefs, new)
                    }
                    this.value = ProfileDataLoader(profileDataManager!!, participant, reportMap!!)
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

data class MpStudyParticipantProfile(
        val dataGroups: List<String>?,
        val firstName: String?,
        val externalId: String?,
        val sharingScope: String?,
        val createdOnStr: String?) {

    companion object {
        val createdOnFormatter = ISODateTimeFormat.dateTime().withOffsetParsed()

        fun fromStudyParticipant(participant: StudyParticipant): MpStudyParticipantProfile {
            return MpStudyParticipantProfile(
                    participant.dataGroups,
                    participant.firstName,
                    participant.externalId,
                    participant.sharingScope.value,
                    createdOnFormatter.print(participant.createdOn ?: DateTime.now()))
        }
    }

    fun toStudyParticipant(gson: Gson): StudyParticipant {
        val createdOn = createdOnStr ?: createdOnFormatter.print(DateTime.now())
        val participantJson = "{\"createdOn\": \"$createdOn\"}"
        val studyParticipant = gson.fromJson(participantJson, StudyParticipant::class.java)
        studyParticipant.dataGroups = dataGroups
        studyParticipant.firstName = firstName
        studyParticipant.externalId = externalId
        studyParticipant.sharingScope = SharingScope.fromValue(sharingScope)
        return studyParticipant
    }
}