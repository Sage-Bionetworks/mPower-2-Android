package org.sagebionetworks.research.mpower.tracking.view_model

import android.content.Context
import com.google.common.collect.ImmutableRangeSet
import com.google.common.collect.Range
import com.google.common.collect.RangeSet
import org.sagebionetworks.research.mpower.R
import org.sagebionetworks.research.mpower.tracking.SortUtil
import org.sagebionetworks.research.mpower.tracking.fragment.MedicationLoggingFragment
import org.sagebionetworks.research.mpower.tracking.fragment.SelectionItem
import org.sagebionetworks.research.mpower.tracking.fragment.TrackingFragment
import org.sagebionetworks.research.mpower.tracking.model.TrackingItem
import org.sagebionetworks.research.mpower.tracking.model.TrackingStepView
import org.sagebionetworks.research.mpower.tracking.recycler_view.MedicationLoggingAddDetails
import org.sagebionetworks.research.mpower.tracking.recycler_view.MedicationLoggingItem
import org.sagebionetworks.research.mpower.tracking.recycler_view.MedicationLoggingSchedule
import org.sagebionetworks.research.mpower.tracking.recycler_view.MedicationLoggingTitle
import org.sagebionetworks.research.mpower.tracking.view_model.logs.DosageItem
import org.sagebionetworks.research.mpower.tracking.view_model.logs.LoggingCollection
import org.sagebionetworks.research.mpower.tracking.view_model.logs.MedicationLog
import org.sagebionetworks.research.mpower.tracking.view_model.logs.MedicationTimestamp
import org.sagebionetworks.research.sageresearch.extensions.inSameDayAs
import org.slf4j.LoggerFactory
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

typealias TimeBlock = Pair<String, RangeSet<LocalTime>>

class MedicationTrackingTaskViewModel(context: Context,
        stepView: TrackingStepView,
        previousLoggingCollection: LoggingCollection<MedicationLog>?)
    : TrackingTaskViewModel<MedicationLog, MedicationLog>(stepView, previousLoggingCollection) {

    private val LOGGER = LoggerFactory.getLogger(MedicationTrackingTaskViewModel::class.java)

    private val timeBlocks: Set<TimeBlock>
    private val morningStart = LocalTime.MIDNIGHT.plusHours(5) // 5am
    private val eveningStart = LocalTime.NOON.plusHours(5)  // 5pm
    private val nightStart = LocalTime.NOON.plusHours(9)    // 9pm

    private var previousLoggingCollection: LoggingCollection<MedicationLog>? = null

    init {
        // TODO: move from presentation layer to UI layer; move test from androidTest to test @liujoshua 2018/10/03
        val resources = context.resources
        val morning = resources.getString(R.string.medication_logging_morning_time_block)
        val afternoon = resources.getString(R.string.medication_logging_afternoon_time_block)
        val evening = resources.getString(R.string.medication_logging_evening_time_block)
        val night = resources.getString(R.string.medication_logging_night_time_block)
        timeBlocks = setOf(
                Pair(morning, ImmutableRangeSet.of(Range.closed(morningStart, LocalTime.NOON))),
                Pair(afternoon, ImmutableRangeSet.of(Range.openClosed(LocalTime.NOON, eveningStart))),
                Pair(evening, ImmutableRangeSet.of(Range.openClosed(eveningStart, nightStart))),
                Pair(night, ImmutableRangeSet.unionOf(listOf(Range.lessThan(morningStart), Range.atLeast(nightStart))))
        )
    }

    companion object {

        val MILITARY_TIME_FORMATTER = DosageItem.timeOfDayFormatter

        /**
         * Returns 'true' if the given schedule should be logged on the given date, 'false' otherwise.
         * @param schedule the schedule to check whether the user should log on the given day.
         * @param today the day to check whether the user should log the schedule on.
         * @return 'true' if the given schedule should be logged on the given date, 'false' otherwise.
         */
        fun isForToday(schedule: DosageItem, today: LocalDate): Boolean {
            if (schedule.isAnytime || schedule.isDaily) {
                return true
            }

            val todayString = today.dayOfWeek.value
            return schedule.daysOfWeek.contains(todayString)
        }
    }

    override fun instantiateLoggingCollection(): LoggingCollection<MedicationLog> {
        return LoggingCollection.builder<MedicationLog>()
                .setIdentifier("trackedItems")
                .build()
    }

    override fun instantiateLogForUnloggedItem(config: MedicationLog): MedicationLog {
        return config;
//        return MedicationLog.builder()
//                .setIdentifier(config.identifier)
//                .setText(config.identifier)
//                .setDosageItems(config.dosageItems)
//                .build()
    }

    override fun instantiateConfigFromSelection(item: TrackingItem): MedicationLog {
        loggedElementsById.value?.get(item.identifier)?.let {
            // If we have an existing log, we can make a default config with it's values set
            return instantiateConfigFromLog(it)
        }
        return MedicationLog.builder()
                .setIdentifier(item.identifier)
                .setDosageItems(mutableListOf<DosageItem>())
                //.setSchedules(listOf(Schedule(Schedule.timeOfDayFormatter.format(LocalTime.MIDNIGHT))))
                .build()
    }

    private fun instantiateConfigFromLog(log: MedicationLog): MedicationLog {
        return log;
//        var items = log.dosageItems;
//        //TODO: Remove loggedDate from timestamps
//        return MedicationConfig.builder()
//                .setIdentifier(log.identifier)
//                .setDosageItems(log.dosageItems)
//                .build()
    }

    /**
     * Returns the list of MedicationLoggingItems that should be displayed for the current time block.
     * A schedule should be displayed if it is anytime, or if it is for today and for the given time block. If a
     * config has a least one schedule a MedicationLoggingTitle will be added for it before all of its schedules.
     * @param today the date to use as the present when calculating which schedules are applicable
     * @return the list of MedicationLoggingItems that should be displayed for the current time block.
     */
    fun getCurrentTimeBlockMedications(timeBlock: TimeBlock, today: LocalDate): List<MedicationLoggingItem> {
        val items: MutableList<MedicationLoggingItem> = mutableListOf()
        val sortedConfigs = SortUtil.getActiveElementsSorted(activeElementsById.value!!)
        for (config in sortedConfigs) {
            if (config.dosageItems.isEmpty()) {
                items.add(MedicationLoggingAddDetails(config))
            }

            for (dosage in config.dosageItems) {
                val todayString = today.dayOfWeek.value
                val isForToday = dosage.daysOfWeek.contains(todayString)

                val timeStamps = dosage.timestamps.filter { medTimestamp ->
                    dosage.isAnytime || (isForToday && timeBlock.second.contains(medTimestamp.localTimeOfDay))
                }

                if (dosage.isAnytime) {
                    items.add(MedicationLoggingTitle(config.identifier + " " + dosage.dosage))
                    items.add(MedicationLoggingSchedule(config, dosage, null))
                    items.addAll(timeStamps.map { timeStamp ->
                        MedicationLoggingSchedule(config, dosage, timeStamp)
                    })
                } else if (!timeStamps.isEmpty()) {
                    items.add(MedicationLoggingTitle(config.identifier + " " + dosage.dosage))
                    items.addAll(timeStamps.map { timeStamp ->
                        MedicationLoggingSchedule(config, dosage, timeStamp)
                    })
                }

            }
        }

        return items
    }

    /**
     * Returns a list of MedicationLoggingItems that should be displayed as the missed medication. A schedule
     * is considered missed if it isn't anytime, and the time block for it has passed, and there isn't a log for it.
     * @param now the time to use as the present when calculating which time block the user is currently in.
     * @return a list of all of the MedicationLoggingItems that should  be displayed as the missed medication.
     */
    fun getMissedMedications(timeBlock: TimeBlock, now: LocalDateTime)
            : List<MedicationLoggingItem> {
        val containingRange = timeBlock.second.asRanges().first { range -> range.contains(now.toLocalTime()) }
        val lowerEndpoint = if (containingRange.hasLowerBound()) containingRange.lowerEndpoint() else LocalTime.MIN
        val missedRange = Range.lessThan(lowerEndpoint)
        val missedItems: MutableList<MedicationLoggingItem> = mutableListOf()
        val sortedConfigs = SortUtil.getActiveElementsSorted(activeElementsById.value!!)
        for (config in sortedConfigs) {
            for (dosage in config.dosageItems) {
                val todayString = now.dayOfWeek.value
                val isForToday = dosage.daysOfWeek.contains(todayString)

                val timeStamps = dosage.timestamps.filter { medTimestamp ->
                    !dosage.isAnytime && isForToday && medTimestamp.loggedDate == null && missedRange.contains(medTimestamp.localTimeOfDay)
                }

                if (!timeStamps.isEmpty()) {
                    missedItems.add(MedicationLoggingTitle(config.identifier + " " + dosage.dosage))
                    missedItems.addAll(timeStamps.map { timeStamp ->
                        MedicationLoggingSchedule(config, dosage, timeStamp)
                    })
                }

            }
        }

        return missedItems
    }

    /**
     * Returns a pair containing the name of the current time block as the first, and the RangeSet of the current
     * time block as the second for the given time.
     * @param time The time to get the time block for.
     * @return a pair containing the name of the current time block as the first, and the RangeSet of the current
     * time block as the second for the given time.
     */
    fun getTimeBlock(time: LocalTime): TimeBlock {
        return timeBlocks.first { pair -> pair.second.contains(time) }
    }

    override fun proceedToInitialFragmentOnSecondRun(trackingFragment: TrackingFragment<*, *, *>) {
        trackingFragment.replaceWithFragment(MedicationLoggingFragment.newInstance(this.stepView))
    }

    override fun setupModelFromPreviousCollection(previousLoggingCollection: LoggingCollection<MedicationLog>) {
        var newDay = false;
        for (log in previousLoggingCollection.items) {
            for (dosage in log.dosageItems) {
                for (timestamp in dosage.timestamps) {
                    if (!(timestamp.loggedDate?.inSameDayAs(LocalDateTime.now(), ZoneId.systemDefault()) ?: true)) {
                        newDay = true;
                        break;
                    }
                }
            }
        }


        // For previous med logging collections, we need to pass more information to create the config
        val activeElements = HashMap<String, MedicationLog>()
        for (log in previousLoggingCollection.items) {
            var config = log;
            if (newDay) {
                config = log.copy(true)
            }

            activeElements[log.identifier] = config
        }
        activeElementsById.value = activeElements
        loggedElementsById.value = activeElements
    }

    fun getSelectionTimes(context: Context, dosageItem: DosageItem) : ArrayList<SelectionItem> {
        val formatter = DateTimeFormatter.ofPattern("h:mm a")
        val selectionItemList = arrayListOf<SelectionItem>()
        val header = SelectionItem(context.getString(R.string.medication_logging_morning_time_block), "morning", false, true)
        selectionItemList.add(header)

        val startTime = LocalTime.MIDNIGHT.plusHours(5)
        var curTime = startTime
        var done = false
        while (!done) {
            if (curTime == LocalTime.NOON) {
                val header = SelectionItem(context.getString(R.string.medication_logging_afternoon_time_block), "afternoon", false, true)
                selectionItemList.add(header)
            } else if (curTime == eveningStart) {
                val header = SelectionItem(context.getString(R.string.medication_logging_evening_time_block), "evening", false, true)
                selectionItemList.add(header)
            } else if (curTime == nightStart) {
                val header = SelectionItem(context.getString(R.string.medication_logging_night_time_block), "night", false, true)
                selectionItemList.add(header)
            }

            val identifier = MedicationTimestamp.timeOfDayFormatter.format(curTime)
            val text = formatter.format(curTime)
            val isSelected = dosageItem.timestamps.firstOrNull {
                it.timeOfDay == identifier
            } != null
            selectionItemList.add(SelectionItem(text, identifier, isSelected, false))
            curTime = curTime.plusMinutes(30)

            if (curTime == startTime) {
                done = true
            }
        }
        return selectionItemList
    }

}

