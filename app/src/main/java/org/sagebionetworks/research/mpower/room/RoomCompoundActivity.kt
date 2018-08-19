package org.sagebionetworks.research.mpower.room

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.ArrayList

@Entity
data class RoomCompoundActivity(@SerializedName("taskIdentifier") @PrimaryKey var taskIdentifier: String) {
    @SerializedName("schemaList")
    var schemaList: List<RoomSchemaReference> = ArrayList()

    @SerializedName("surveyList")
    var surveyList: List<RoomSurveyReference> = ArrayList()

    @SerializedName("type")
    var type: String? = null
}