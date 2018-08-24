package org.sagebionetworks.research.mpower.room

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.SerializedName

import org.sagebionetworks.bridge.rest.model.ActivityType
import org.sagebionetworks.bridge.rest.model.ScheduleStatus
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime

import java.util.ArrayList

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

@Entity
data class ScheduledActivityEntity(@SerializedName("guid") @PrimaryKey var guid: String) {
    @SerializedName("schedulePlanGuid")
    var schedulePlanGuid: String? = null

    @SerializedName("startedOn")
    var startedOn: Instant? = null

    @SerializedName("finishedOn")
    @ColumnInfo(index = true)
    var finishedOn: Instant? = null

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
    var createdOn: Instant? = null

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