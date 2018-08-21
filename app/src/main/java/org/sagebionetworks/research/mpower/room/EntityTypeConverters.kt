package org.sagebionetworks.research.mpower.room

import android.arch.persistence.room.TypeConverter

import com.google.gson.reflect.TypeToken
import org.joda.time.DateTime
import org.sagebionetworks.bridge.rest.RestUtils
import org.sagebionetworks.bridge.rest.model.ActivityType
import org.sagebionetworks.bridge.rest.model.ScheduleStatus
import org.sagebionetworks.bridge.rest.model.ScheduledActivityListV4

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