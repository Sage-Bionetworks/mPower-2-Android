package org.sagebionetworks.research.mpower.room

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import org.threeten.bp.OffsetDateTime

@Entity
data class RoomSchemaReference(@SerializedName("guid") @PrimaryKey var guid: String) {
    @SerializedName("identifier")
    var identifier: String? = null

    @SerializedName("createdOn")
    var createdOn: OffsetDateTime? = null

    @SerializedName("href")
    var href: String? = null

    @SerializedName("type")
    var type: String? = null
}