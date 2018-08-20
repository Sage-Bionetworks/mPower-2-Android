package org.sagebionetworks.research.mpower.room

import android.arch.persistence.room.TypeConverter
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.joda.time.DateTime
import org.sagebionetworks.bridge.rest.RestUtils
import org.sagebionetworks.bridge.rest.model.ActivityType
import org.sagebionetworks.bridge.rest.model.ScheduleStatus
import org.sagebionetworks.bridge.rest.model.ScheduledActivityListV4

class RoomTypeConverters {

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

    fun fromScheduledActivityListV4(value: ScheduledActivityListV4?): List<RoomScheduledActivity>? {
        val valueChecked = value ?: return null
        return valueChecked.items.map {
            RestUtils.GSON.fromJson(RestUtils.GSON.toJson(it), RoomScheduledActivity::class.java)
        }
    }
}