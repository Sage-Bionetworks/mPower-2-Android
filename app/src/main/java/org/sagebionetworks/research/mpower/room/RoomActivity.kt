package org.sagebionetworks.research.mpower.room

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import org.sagebionetworks.bridge.rest.model.ActivityType

@Entity
data class RoomActivity(@SerializedName("guid") @PrimaryKey var guid: String) {
    @SerializedName("label")
    var label: String? = null

    @SerializedName("labelDetail")
    var labelDetail: String? = null

    @SerializedName("compoundActivity")
    var compoundActivity: RoomCompoundActivity? = null

    @SerializedName("task")
    var task: RoomTaskReference? = null

    @SerializedName("survey")
    var survey: RoomSurveyReference? = null

    @SerializedName("activityType")
    var activityType: ActivityType? = null

    @SerializedName("type")
    var type: String? = null
}