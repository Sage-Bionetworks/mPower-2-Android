package org.sagebionetworks.research.mpower.history

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.dampcake.gson.immutable.ImmutableAdapterFactory
import com.google.common.reflect.TypeToken
import org.sagebionetworks.bridge.rest.RestUtils.GSON
import org.sagebionetworks.research.mpower.R
import org.sagebionetworks.research.mpower.history.HistoryItemType.DATE_BUCKET
import org.sagebionetworks.research.mpower.history.HistoryItemType.SYMPTOM
import org.sagebionetworks.research.mpower.history.HistoryItemType.MEDICATION
import org.sagebionetworks.research.mpower.history.HistoryItemType.TAPPING
import org.sagebionetworks.research.mpower.history.HistoryItemType.TIME_BUCKET
import org.sagebionetworks.research.mpower.history.HistoryItemType.TREMOR
import org.sagebionetworks.research.mpower.history.HistoryItemType.TRIGGER
import org.sagebionetworks.research.mpower.history.HistoryItemType.WALK_BALANCE
import org.sagebionetworks.research.mpower.history.MpHistoryItemManager.Companion.dateBucketDetailsDisplayFormat
import org.sagebionetworks.research.mpower.history.MpHistoryItemManager.Companion.dateBucketFormat
import org.sagebionetworks.research.mpower.history.MpHistoryItemManager.Companion.dateBucketTitleDisplayFormat
import org.sagebionetworks.research.mpower.history.MpHistoryItemManager.Companion.timeBucketFormat
import org.sagebionetworks.research.mpower.inject.AutoValueGson_AppAutoValueTypeAdapterFactory
import org.sagebionetworks.research.mpower.research.MpIdentifier
import org.sagebionetworks.research.mpower.tracking.view_model.logs.LoggingCollection
import org.sagebionetworks.research.mpower.tracking.view_model.logs.MedicationLog
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SimpleTrackingItemLog
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SymptomLog
import org.sagebionetworks.research.mpower.tracking.view_model.logs.TrackingItemLog
import org.sagebionetworks.research.sageresearch.dao.room.EntityTypeConverters
import org.sagebionetworks.research.sageresearch.dao.room.HistoryItemEntity
import org.sagebionetworks.research.sageresearch.dao.room.HistoryItemEntityDao
import org.sagebionetworks.research.sageresearch.dao.room.HistoryItemManager
import org.sagebionetworks.research.sageresearch.dao.room.ReportEntity
import org.sagebionetworks.research.sageresearch.dao.room.ReportRepository
import org.sagebionetworks.research.sageresearch.extensions.toInstant
import org.slf4j.LoggerFactory
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.floor

class MpHistoryItemManager(val historyItemDao: HistoryItemEntityDao): HistoryItemManager {

    companion object {

        val dateBucketFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
        val timeBucketFormat = DateTimeFormatter.ofPattern("h:mm a", Locale.US).withZone(ZoneId.systemDefault())

        val dateBucketTitleDisplayFormat = DateTimeFormatter.ofPattern("EEEE", Locale.getDefault())
        val dateBucketDetailsDisplayFormat = DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.getDefault())

        fun loadHistoryItem(entity: HistoryItemEntity): HistoryItem {
            val type = HistoryItemType.valueOf(entity.type)
            val details = HistoryDetails.parsDetails(entity.dataJson, type)
            return HistoryItem(type, entity.reportId, entity.dateBucket, entity.dateTime, details)
        }

    }

    private val logger = LoggerFactory.getLogger(MpHistoryItemManager::class.java);

    fun historyItems(): LiveData<PagedList<HistoryItem>> {
        return historyItemDao.historyItems().map { input: HistoryItemEntity -> loadHistoryItem(input) }.toLiveData(100)
    }

    override fun updateHistoryItems(reportId: String, reports: List<ReportEntity>) {
        //historyItemDao.clear()
        when(reportId) {
            MpIdentifier.TRIGGERS -> updateTriggersHistoryItems(reports)
            MpIdentifier.SYMPTOMS -> updateSymptomHistoryItems(reports)
            MpIdentifier.MEDICATION -> updateMedicationHistoryItems(reports)
            MpIdentifier.WALK_AND_BALANCE -> updateMeasurementHistoryItems(WALK_BALANCE, reports)
            MpIdentifier.TREMOR -> updateMeasurementHistoryItems(TREMOR, reports)
            MpIdentifier.TAPPING -> updateMeasurementHistoryItems(TAPPING, reports)
        }


    }

    private fun updateMeasurementHistoryItems(type: HistoryItemType, reports: List<ReportEntity>) {

        val historyList = mutableListOf<HistoryItemEntity>()
        for(report in reports) {
            val dateBucket = getDateBucket(report)
            val details = createMeasurementHistoryDetails(type, report)
            val historyItem = HistoryItem(type, report.identifier!!, dateBucket, report.dateTime!!, details)
            historyList.add(historyItem.toHistoryItemEntity())
            historyList.add(createDateBucketHistoryItemEntity(dateBucket))
            historyList.add(createTimeBucketHistoryItemEntity(dateBucket, report.dateTime!!))
        }
        historyItemDao.update(historyList)
    }

    private fun createMeasurementHistoryDetails(type: HistoryItemType, report: ReportEntity): HistoryDetails {
        //TODO: Parse measurement reports
        // "handSelection"
        // "medicationTiming"

        return when (type) {
            WALK_BALANCE -> return WalkBalanceDetails("")
            TREMOR -> return TremorDetails("")
            TAPPING -> return TapDetails("", 0, 0)
            else -> return UnknownMeasurementDetails("")
        }
    }

    private fun getDateBucket(report: ReportEntity): String {
        return report.localDate?.format(dateBucketFormat)?:LocalDateTime.ofInstant(report.dateTime, ZoneId.systemDefault()).toLocalDate().format(dateBucketFormat)
    }

    private fun createDateBucketHistoryItemEntity(dateBucket: String): HistoryItemEntity {
        val startOfDay = LocalDate.parse(dateBucket).atStartOfDay().toInstant(ZoneId.systemDefault())
        return HistoryItem(DATE_BUCKET, "DateBucket", dateBucket, startOfDay, DateBucketDetails()).toHistoryItemEntity()
    }

    private fun createTimeBucketHistoryItemEntity(dateBucket: String, dateTime: Instant): HistoryItemEntity {
        val minutes = dateTime.truncatedTo(ChronoUnit.MINUTES).epochSecond / 60
        val roundedMinutes = floor((minutes/15).toDouble()) * 15 //Round down to nearest 15 minute
        val roundedDateTime = Instant.ofEpochSecond(roundedMinutes.toLong() * 60)
        return HistoryItem(TIME_BUCKET, "TimeBucket", dateBucket, roundedDateTime, DateBucketDetails()).toHistoryItemEntity()
    }

    private fun updateTriggersHistoryItems(reports: List<ReportEntity>) {

        for(report in reports) {
            val loggingCollection = createLoggingCollection<SimpleTrackingItemLog>(report, MpIdentifier.TRIGGERS)
            loggingCollection?.let {log ->

                val dateBucket = getDateBucket(report)
                val triggerList = mutableListOf<HistoryItemEntity>()
                for (logItem in log.items) {
                    if (logItem.loggedDate != null) {
                        val details = TriggerDetails(logItem.text)
                        val historyItem = HistoryItem(TRIGGER, report.identifier!!, dateBucket, logItem.loggedDate!!,
                                details)
                        triggerList.add(historyItem.toHistoryItemEntity())
                        triggerList.add(createTimeBucketHistoryItemEntity(dateBucket, logItem.loggedDate!!))
                    }
                }
                triggerList.add(createDateBucketHistoryItemEntity(dateBucket))
                historyItemDao.update(triggerList)
            }
        }
    }

    private fun updateSymptomHistoryItems(reports: List<ReportEntity>) {

        for(report in reports) {
            val loggingCollection = createLoggingCollection<SymptomLog>(report, MpIdentifier.SYMPTOMS)
            loggingCollection?.let {log ->

                val dateBucket = getDateBucket(report)
                val symptomList = mutableListOf<HistoryItemEntity>()
                for (symptom in log.items) {
                    if (symptom.loggedDate != null) {
                        val details = SymptomDetails(symptom.text, symptom.medicationTiming, symptom.duration,
                                symptom.severity)
                        val historyItem = HistoryItem(SYMPTOM, report.identifier!!, dateBucket, symptom.loggedDate!!,
                                details)
                        symptomList.add(historyItem.toHistoryItemEntity())
                        symptomList.add(createTimeBucketHistoryItemEntity(dateBucket, symptom.loggedDate!!))
                    }
                }
                symptomList.add(createDateBucketHistoryItemEntity(dateBucket))
                historyItemDao.update(symptomList)
            }
        }
    }

    private fun updateMedicationHistoryItems(reports: List<ReportEntity>) {

        for(report in reports) {
            val loggingCollection = createLoggingCollection<MedicationLog>(report, MpIdentifier.MEDICATION)
            loggingCollection?.let {log ->

                val dateBucket = getDateBucket(report)
                val medList = mutableListOf<HistoryItemEntity>()
                for (med in log.items) {
                    for (dosage in med.dosageItems) {
                        for (timestamp in dosage.timestamps) {
                            if (timestamp.loggedDate != null) {
                                val details = MedicationDetails(med.identifier, dosage.dosage, timestamp.timeOfDay)
                                val historyItem = HistoryItem(MEDICATION, report.identifier!!, dateBucket, timestamp.loggedDate!!, details)
                                medList.add(historyItem.toHistoryItemEntity())
                                medList.add(createTimeBucketHistoryItemEntity(dateBucket, timestamp.loggedDate!!))
                            }
                        }
                    }
                }
                medList.add(createDateBucketHistoryItemEntity(dateBucket))
                historyItemDao.deleteAndUpdate(MpIdentifier.MEDICATION, dateBucket, medList)
            }
        }
    }

    private fun <T: TrackingItemLog> createLoggingCollection(report: ReportEntity, reportId: String): LoggingCollection<T>? {
        report.data?.data?.let { reportData ->
            when(reportId) {
                MpIdentifier.TRIGGERS -> object : TypeToken<LoggingCollection<SimpleTrackingItemLog>>(){}.type
                MpIdentifier.SYMPTOMS -> object : TypeToken<LoggingCollection<SymptomLog>>(){}.type
                MpIdentifier.MEDICATION -> object : TypeToken<LoggingCollection<MedicationLog>>(){}.type
                else -> null
            }?.let { type ->
                val gson = EntityTypeConverters().bridgeGsonBuilder
                        .registerTypeAdapterFactory(AutoValueGson_AppAutoValueTypeAdapterFactory())
                        .registerTypeAdapterFactory(ImmutableAdapterFactory.forGuava())
                        .create()
                val json = gson.toJson(reportData)
                var loggingCollection: LoggingCollection<T>? = null
                try {
                    loggingCollection = gson.fromJson(json, type)
                } catch (e: Throwable) {
                    // No need to crash the app, user will just have to redo selection
                    logger.error(e.message)

                }

                return loggingCollection
            }
        }
        return null

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
        return GSON.toJson(this)
    }

    companion object {

        fun parsDetails(json: String, type: HistoryItemType): HistoryDetails {
            return GSON.fromJson(json, type.detailsClass())
        }

    }

}

data class HistoryItem(
        val type: HistoryItemType,
        val reportId: String,
        val dateBucket: String,
        val dateTime: Instant,
        val historyDetails: HistoryDetails
) {

    val iconId: Int
        get() = historyDetails.iconId

    fun title(resources: Resources): String {
            when (type) {
                DATE_BUCKET -> return dateBucketTitleDisplayFormat.format(dateBucketFormat.parse(dateBucket))
                TIME_BUCKET -> return timeBucketFormat.format(dateTime)
                else -> return historyDetails.title(resources)
            }
        }

    fun details(resources: Resources): String {
            when (type) {
                DATE_BUCKET -> return dateBucketDetailsDisplayFormat.format(dateBucketFormat.parse(dateBucket))
                else -> return historyDetails.details(resources)
            }
        }

    fun toHistoryItemEntity(): HistoryItemEntity {
        return HistoryItemEntity(type.name, historyDetails.toJson(), reportId, dateBucket, dateTime)
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
        return resources.getString(R.string.measuring_left_label)
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
        return resources.getString(R.string.measuring_right_label)
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
        return medIdentifier + " " + dosage
    }

    override fun details(resources: Resources): String {
        return ""
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
