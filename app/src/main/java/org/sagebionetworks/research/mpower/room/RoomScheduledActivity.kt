package org.sagebionetworks.research.mpower.room

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import org.sagebionetworks.bridge.rest.model.ScheduleStatus

import org.threeten.bp.OffsetDateTime

@Entity
data class RoomScheduledActivity(@SerializedName("guid") @PrimaryKey var guid: String) {
    @SerializedName("schedulePlanGuid")
    var schedulePlanGuid: String? = null

    @SerializedName("startedOn")
    var startedOn: OffsetDateTime? = null

    @SerializedName("finishedOn")
    var finishedOn: OffsetDateTime? = null

    @SerializedName("scheduledOn")
    var scheduledOn: OffsetDateTime? = null

    @SerializedName("expiresOn")
    var expiresOn: OffsetDateTime? = null

    @SerializedName("activity")
    var activity: RoomActivity? = null

    @SerializedName("persistent")
    var persistent: Boolean? = null

    @SerializedName("clientData")
    var clientData: String? = null

    @SerializedName("status")
    var status: ScheduleStatus? = null

    @SerializedName("type")
    var type: String? = null
}