package org.sagebionetworks.research.mpower.room

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class RoomTaskReference(@SerializedName("identifier") @PrimaryKey var identifier: String) {
    @SerializedName("schema")
    var schema: RoomSchemaReference? = null

    @SerializedName("type")
    var type: String? = null
}