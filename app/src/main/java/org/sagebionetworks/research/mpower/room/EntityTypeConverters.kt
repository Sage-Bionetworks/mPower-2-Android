package org.sagebionetworks.research.mpower.room

import android.arch.persistence.room.TypeConverter

import com.google.gson.reflect.TypeToken
import org.joda.time.DateTime
import org.sagebionetworks.bridge.rest.RestUtils
import org.sagebionetworks.bridge.rest.model.ActivityType
import org.sagebionetworks.bridge.rest.model.ScheduleStatus
import org.sagebionetworks.bridge.rest.model.ScheduledActivityListV4

//
//  Copyright © 2016-2018 Sage Bionetworks. All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification,
// are permitted provided that the following conditions are met:
//
// 1.  Redistributions of source code must retain the above copyright notice, this
// list of conditions and the following disclaimer.
//
// 2.  Redistributions in binary form must reproduce the above copyright notice,
// this list of conditions and the following disclaimer in the documentation and/or
// other materials provided with the distribution.
//
// 3.  Neither the name of the copyright holder(s) nor the names of any contributors
// may be used to endorse or promote products derived from this software without
// specific prior written permission. No license is granted to the trademarks of
// the copyright holders even if such marks are included in this software.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//

/**
 * This class controls how objects are converted to and from data types supported by SqlLite
 * Room recognizes which class types can be converted by
 * the @TypeConverter annotation, and inferred by the method structure
 */
class EntityTypeConverters {

    private val schemaRefListType = object : TypeToken<List<RoomSchemaReference>>() {}.type
    private val surveyRefListType = object : TypeToken<List<RoomSurveyReference>>() {}.type

    @TypeConverter
    fun fromTimestamp(value: Long?): DateTime? {
        val valueChecked = value ?: return null
        return DateTime(valueChecked)
    }

    @TypeConverter
    fun toTimestamp(value: DateTime?): Long? {
        val valueChecked = value ?: return null
        return valueChecked.toDate().time
    }

    @TypeConverter
    fun toClientData(value: String?): ClientData? {
        val valueChecked = value ?: return null
        return RestUtils.GSON.fromJson(valueChecked, ClientData::class.java)
    }

    @TypeConverter
    fun fromClientData(value: ClientData?): String? {
        val valueChecked = value ?: return null
        return RestUtils.GSON.toJson(valueChecked)
    }

    @TypeConverter
    fun toScheduleStatus(value: String?): ScheduleStatus? {
        val valueChecked = value ?: return null
        return ScheduleStatus.fromValue(valueChecked)
    }

    @TypeConverter
    fun fromActivityType(value: ActivityType?): String? {
        val valueCheck = value ?: return null
        return valueCheck.toString()
    }

    @TypeConverter
    fun toActivityType(value: String?): ActivityType? {
        val valueChecked = value ?: return null
        return ActivityType.fromValue(valueChecked)
    }

    @TypeConverter
    fun fromScheduleStatus(value: ScheduleStatus?): String? {
        val valueCheck = value ?: return null
        return valueCheck.toString()
    }

    @TypeConverter
    fun toSchemaReferenceList(value: String?): List<RoomSchemaReference>? {
        val valueChecked = value ?: return null
        return RestUtils.GSON.fromJson<List<RoomSchemaReference>>(valueChecked, schemaRefListType)
    }

    @TypeConverter
    fun fromSchemaReferenceList(value: List<RoomSchemaReference>?): String? {
        val valueCheck = value ?: return null
        return RestUtils.GSON.toJson(valueCheck, schemaRefListType)
    }

    @TypeConverter
    fun toSurveyReferenceList(value: String?): List<RoomSurveyReference>? {
        val valueChecked = value ?: return null
        return RestUtils.GSON.fromJson<List<RoomSurveyReference>>(valueChecked, surveyRefListType)
    }

    @TypeConverter
    fun fromRoomSurveyReferenceList(refList: List<RoomSurveyReference>?): String? {
        return RestUtils.GSON.toJson(refList, surveyRefListType)
    }

    fun fromScheduledActivityListV4(value: ScheduledActivityListV4?): List<ScheduledActivityEntity>? {
        val valueChecked = value ?: return null
        var activities = ArrayList<ScheduledActivityEntity>()
        for (scheduledActivity in valueChecked.items) {
            var roomActivity = RestUtils.GSON.fromJson(
                    RestUtils.GSON.toJson(scheduledActivity), ScheduledActivityEntity::class.java)
            scheduledActivity.clientData.let {
                roomActivity.clientData = ClientData(it)
            }
            activities.add(roomActivity)
        }
        return activities
    }
}