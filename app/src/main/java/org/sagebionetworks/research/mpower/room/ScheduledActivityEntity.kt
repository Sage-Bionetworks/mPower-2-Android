package org.sagebionetworks.research.mpower.room

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime
import org.sagebionetworks.bridge.rest.model.ActivityType
import org.sagebionetworks.bridge.rest.model.ScheduleStatus
import org.threeten.bp.LocalDateTime

import java.util.ArrayList

@Entity
data class ScheduledActivityEntity(@SerializedName("guid") @PrimaryKey var guid: String) {
    @SerializedName("schedulePlanGuid")
    var schedulePlanGuid: String? = null

    @SerializedName("startedOn")
    var startedOn: DateTime? = null

    @SerializedName("finishedOn")
    @ColumnInfo(index = true)
    var finishedOn: DateTime? = null

    @SerializedName("scheduledOn")
    @ColumnInfo(index = true)
    var scheduledOn: LocalDateTime? = null

    @SerializedName("expiresOn")
    @ColumnInfo(index = true)
    var expiresOn: LocalDateTime? = null

    @SerializedName("activity")
    @Embedded(prefix = "activity_")
    var activity: RoomActivity? = null

    @SerializedName("persistent")
    var persistent: Boolean? = null

    @SerializedName("clientData")
    var clientData: ClientData? = null

    @SerializedName("status")
    var status: ScheduleStatus? = null

    @SerializedName("type")
    var type: String? = null
}

data class ClientData(var data: Any? = null)

data class RoomActivity(@SerializedName("guid") var guid: String) {
    @SerializedName("label")
    var label: String? = null

    @SerializedName("labelDetail")
    var labelDetail: String? = null

    @SerializedName("compoundActivity")
    @Embedded(prefix = "compound_")
    var compoundActivity: RoomCompoundActivity? = null

    @SerializedName("task")
    @Embedded(prefix = "task_")
    var task: RoomTaskReference? = null

    @SerializedName("survey")
    @Embedded(prefix = "survey_")
    var survey: RoomSurveyReference? = null

    @SerializedName("activityType")
    var activityType: ActivityType? = null

    @SerializedName("type")
    var type: String? = null
}


data class RoomSchemaReference(@SerializedName("id") var id: String? = null) {
    @SerializedName("revision")
    var revision: Long? = null

    @SerializedName("type")
    var type: String? = null
}

data class RoomTaskReference(@SerializedName("identifier") @ColumnInfo(index = true) var identifier: String) {
    @SerializedName("schema")
    @Embedded(prefix = "schema_")
    var schema: RoomSchemaReference? = null

    @SerializedName("type")
    var type: String? = null
}

data class RoomSurveyReference(@SerializedName("guid") var guid: String) {
    @SerializedName("identifier")
    @ColumnInfo(index = true)
    var identifier: String? = null

    @SerializedName("createdOn")
    var createdOn: DateTime? = null

    @SerializedName("href")
    var href: String? = null

    @SerializedName("type")
    var type: String? = null
}

data class RoomCompoundActivity(@SerializedName("taskIdentifier") @ColumnInfo(index = true) var taskIdentifier: String) {
    @SerializedName("schemaList")
    var schemaList: List<RoomSchemaReference> = ArrayList()

    @SerializedName("surveyList")
    var surveyList: List<RoomSurveyReference> = ArrayList()

    @SerializedName("type")
    var type: String? = null
}