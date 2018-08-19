package org.sagebionetworks.research.mpower.room

import android.arch.persistence.room.TypeConverter
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.sagebionetworks.bridge.rest.RestUtils
import org.sagebionetworks.bridge.rest.model.ActivityType
import org.sagebionetworks.bridge.rest.model.ScheduleStatus
import org.threeten.bp.OffsetDateTime

class RoomTypeConverters {
    val roomGson = GsonBuilder()
            .registerTypeAdapter(OffsetDateTime::class.java, OffsetDateTimeTypeAdapter())
            .create()

    private val schemaRefListType = object : TypeToken<List<RoomSchemaReference>>() {}.type
    private val surveyRefListType = object : TypeToken<List<RoomSurveyReference>>() {}.type

    @TypeConverter
    fun toOffsetDateTime(value: String?): OffsetDateTime? {
        return value?.let {
            return roomGson.fromJson(value, OffsetDateTime::class.java)
        }
    }

    @TypeConverter
    fun fromOffsetDateTime(date: OffsetDateTime?): String? {
        return roomGson.toJson(date)
    }

    @TypeConverter
    fun toScheduleStatus(value: String?): ScheduleStatus? {
        return value?.let {
            return ScheduleStatus.fromValue(value)
        }
    }

    @TypeConverter
    fun fromActivityType(type: ActivityType?): String? {
        return type.toString()
    }

    @TypeConverter
    fun toActivityType(value: String?): ActivityType? {
        return value?.let {
            return ActivityType.fromValue(value)
        }
    }

    @TypeConverter
    fun fromScheduleStatus(status: ScheduleStatus?): String? {
        return status.toString()
    }

    @TypeConverter
    fun toSchemaReferenceList(value: String?): List<RoomSchemaReference>? {
        return value?.let {
            return roomGson.fromJson<List<RoomSchemaReference>>(value, schemaRefListType)
        }
    }

    @TypeConverter
    fun fromSchemaReferenceList(refList: List<RoomSchemaReference>?): String? {
        return RestUtils.GSON.toJson(refList, schemaRefListType)
    }

    @TypeConverter
    fun toSurveyReferenceList(value: String?): List<RoomSurveyReference>? {
        return value?.let {
            return roomGson.fromJson<List<RoomSurveyReference>>(value, surveyRefListType)
        }
    }

    @TypeConverter
    fun fromRoomSurveyReferenceList(refList: List<RoomSurveyReference>?): String? {
        return RestUtils.GSON.toJson(refList, surveyRefListType)
    }
}