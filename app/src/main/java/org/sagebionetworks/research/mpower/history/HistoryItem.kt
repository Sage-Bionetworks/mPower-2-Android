/*
 * BSD 3-Clause License
 *
 * Copyright 2019  Sage Bionetworks. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1.  Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2.  Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * 3.  Neither the name of the copyright holder(s) nor the names of any contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission. No license is granted to the trademarks of
 * the copyright holders even if such marks are included in this software.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.sagebionetworks.research.mpower.history

import android.content.res.Resources
import org.sagebionetworks.bridge.rest.RestUtils
import org.sagebionetworks.research.mpower.R
import org.sagebionetworks.research.mpower.history.HistoryItemType.DATE_BUCKET
import org.sagebionetworks.research.mpower.history.HistoryItemType.TIME_BUCKET
import org.sagebionetworks.research.sageresearch.dao.room.HistoryItemEntity
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId

data class HistoryItem(
        val type: HistoryItemType,
        val reportId: String,
        val dateBucket: LocalDate,
        val dateTime: Instant,
        val historyDetails: HistoryDetails
) {

    val iconId: Int
        get() = historyDetails.iconId

    fun title(resources: Resources): String {
        when (type) {
            DATE_BUCKET -> return MpHistoryItemManager.dateBucketTitleDisplayFormat.format(dateBucket)
            TIME_BUCKET -> return MpHistoryItemManager.timeBucketFormat.format(dateTime)
            else -> return historyDetails.title(resources)
        }
    }

    fun details(resources: Resources): String {
        when (type) {
            DATE_BUCKET -> return MpHistoryItemManager.dateBucketDetailsDisplayFormat.format(dateBucket)
            else -> return historyDetails.details(resources)
        }
    }

    fun toHistoryItemEntity(): HistoryItemEntity {
        return HistoryItemEntity(type.name, historyDetails.toJson(), reportId, dateBucket, dateTime, LocalDateTime.ofInstant(dateTime, ZoneId.systemDefault()).toLocalTime())
    }

}

enum class HistoryItemType {

    TAPPING {
        override fun <T:HistoryDetails> detailsClass(): Class<T> {
            return TapDetails::class.java as Class<T>
        }
    },
    TREMOR {
        override fun <T:HistoryDetails> detailsClass(): Class<T> {
            return TremorDetails::class.java as Class<T>
        }
    },
    WALK_BALANCE {
        override fun <T:HistoryDetails> detailsClass(): Class<T> {
            return WalkBalanceDetails::class.java as Class<T>
        }
    },
    MEDICATION {
        override fun <T:HistoryDetails> detailsClass(): Class<T> {
            return MedicationDetails::class.java as Class<T>
        }
    },
    TRIGGER {
        override fun <T:HistoryDetails> detailsClass(): Class<T> {
            return TriggerDetails::class.java as Class<T>
        }
    },
    SYMPTOM {
        override fun <T:HistoryDetails> detailsClass(): Class<T> {
            return SymptomDetails::class.java as Class<T>
        }
    },
    DATE_BUCKET {
        override fun <T:HistoryDetails> detailsClass(): Class<T> {
            return DateBucketDetails::class.java as Class<T>
        }
    },
    TIME_BUCKET {
        override fun <T:HistoryDetails> detailsClass(): Class<T> {
            return DateBucketDetails::class.java as Class<T>
        }
    };

    abstract fun <T:HistoryDetails> detailsClass():Class<T>
}

interface HistoryDetails {

    val iconId: Int
        get() = -1

    fun title(resources: Resources): String

    fun details(resources: Resources): String {
        return ""
    }

    fun toJson(): String {
        return RestUtils.GSON.toJson(this)
    }

    companion object {

        fun parsDetails(json: String, type: HistoryItemType): HistoryDetails {
            return RestUtils.GSON.fromJson(json, type.detailsClass())
        }

    }

}

data class WalkBalanceDetails(
        val medicationTiming: String
): HistoryDetails {

    override val iconId: Int
        get() = R.drawable.ic_walk_and_stand

    override fun title(resources: Resources): String {
        return resources.getString(R.string.measuring_center_label)
    }

    override fun details(resources: Resources): String {
        return ""
    }
}

data class TremorDetails(
        val medicationTiming: String
): HistoryDetails {

    override val iconId: Int
        get() = R.drawable.ic_tremor

    override fun title(resources: Resources): String {
        return resources.getString(R.string.measuring_right_label)
    }

    override fun details(resources: Resources): String {
        return ""
    }
}

data class UnknownMeasurementDetails(
        val medicationTiming: String
): HistoryDetails {

    override fun title(resources: Resources): String {
        return "Unknown Measurement type"
    }

    override fun details(resources: Resources): String {
        return ""
    }
}

data class TapDetails(
        val medicationTiming: String,
        val leftTapCount: Int,
        val rightTapCount: Int
): HistoryDetails {

    override val iconId: Int
        get() = R.drawable.ic_finger_tapping

    override fun title(resources: Resources): String {
        return resources.getString(R.string.measuring_left_label)
    }

    override fun details(resources: Resources): String {
        val right = if (rightTapCount > 0) resources.getString(R.string.right_hand_count, rightTapCount) else ""
        val left = if (leftTapCount > 0) resources.getString(R.string.left_hand_count, leftTapCount) else ""
        return right + ", " + left
    }
}

data class SymptomDetails(
        val text: String,
        val medicationTiming: String?,
        val durationLevel: String?,
        val severityLevel: Int?
): HistoryDetails {

    override val iconId: Int
        get() = R.drawable.ic_symptoms_purple

    override fun title(resources: Resources): String {
        return text
    }

    override fun details(resources: Resources): String {
        when (severityLevel) {
            1 -> return resources.getString(R.string.severity_mild)
            2 -> return resources.getString(R.string.severity_moderate)
            3 -> return resources.getString(R.string.severity_severe)
        }
        return ""
    }
}

data class TriggerDetails(
        val text: String
): HistoryDetails {

    override val iconId: Int
        get() = R.drawable.ic_trigger_purple

    override fun title(resources: Resources): String {
        return text
    }

    override fun details(resources: Resources): String {
        return ""
    }
}

data class MedicationDetails(
        val medIdentifier: String,
        val dosage: String,
        val timeOfDay: String?
): HistoryDetails {

    override val iconId: Int
        get() = R.drawable.ic_medication_purple

    override fun title(resources: Resources): String {
        return medIdentifier// + " " + dosage
    }

    override fun details(resources: Resources): String {
        return dosage
    }
}

class DateBucketDetails(): HistoryDetails {

    override fun title(resources: Resources): String {
        return ""
    }

    override fun details(resources: Resources): String {
        return ""
    }
}
