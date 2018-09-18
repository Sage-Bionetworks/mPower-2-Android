package org.sagebionetworks.research.mpower.tracking.view_model

import org.sagebionetworks.research.mpower.tracking.recycler_view.Schedule
import org.sagebionetworks.research.mpower.tracking.model.TrackingItem
import org.sagebionetworks.research.mpower.tracking.model.TrackingStepView
import org.sagebionetworks.research.mpower.tracking.view_model.configs.MedicationConfig
import org.sagebionetworks.research.mpower.tracking.view_model.logs.LoggingCollection
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SimpleTrackingItemLog
import org.slf4j.LoggerFactory

class MedicationTrackingTaskViewModel(stepView: TrackingStepView,
        previousLoggingCollection: LoggingCollection<SimpleTrackingItemLog>?)
    : TrackingTaskViewModel<MedicationConfig, SimpleTrackingItemLog>(stepView, previousLoggingCollection) {

    private val LOGGER = LoggerFactory.getLogger(MedicationTrackingTaskViewModel::class.java)

    override fun instantiateLoggingCollection(): LoggingCollection<SimpleTrackingItemLog> {
        return LoggingCollection.builder<SimpleTrackingItemLog>()
                .setIdentifier("trackedItems")
                .build()
    }

    override fun instantiateLogForUnloggedItem(config: MedicationConfig): SimpleTrackingItemLog {
        return SimpleTrackingItemLog.builder()
                .setIdentifier(config.identifier)
                .setText(config.identifier)
                .build()
    }

    override fun instantiateConfigFromSelection(item: TrackingItem): MedicationConfig {
        return MedicationConfig.builder()
                .setIdentifier(item.identifier)
                .setTrackingItem(item)
                .build()
    }

    /**
     * Adds a schedule to the list of schedules for the given item identifier
     * @param itemIdentifier The identifier of the config to add a schedule for.
     */
    fun addSchedule(itemIdentifier: String) {
        LOGGER.debug("addSchedule(): $itemIdentifier")
        val config: MedicationConfig? = activeElementsById.value!![itemIdentifier]
        if (config == null) {
            LOGGER.warn("addSchedule() called on $itemIdentifier which is not active")
            return
        }

        val schedules: MutableList<Schedule> = config.schedules
        val len = schedules.size - 1
        LOGGER.debug("Adding schedule with id: $len")
        schedules.add(len, Schedule(len.toString()))
        val activeElements = activeElementsById.value!!
        activeElements[itemIdentifier] = config.toBuilder().setSchedules(schedules).build()
        activeElementsById.value = activeElements
    }

    /**
     * Sets the days for the given schedule identifier, in the given item identifier to the given days.
     * @param itemIdentifier The identifier of the config to set the schedule days in.
     * @param scheduleIdentifier The identifier of the schedule within the config to set the days in.
     * @param days The list of days to set the schedules days to.
     */
    fun setScheduleDays(itemIdentifier: String, scheduleIdentifier: String, days: List<String>) {
        LOGGER.debug("setScheduleDays(): $itemIdentifier, $scheduleIdentifier, $days")
        val config = activeElementsById.value!![itemIdentifier]
        if (config == null) {
            LOGGER.warn("setScheduleDays() called on itemIdentifier $itemIdentifier which is not active")
            return
        }

        var schedules: MutableList<Schedule> = config.schedules
        for (schedule in schedules) {
            if (schedule.id == scheduleIdentifier) {
                LOGGER.debug("Found schedule: $scheduleIdentifier")
                if (days.size < 7) {
                    schedule.everday = false
                    schedule.days = days
                } else {
                    schedule.everday = true
                    schedule.days = arrayListOf()
                }
            }
        }

        val activeElements = activeElementsById.value!!
        activeElements[itemIdentifier] = config.toBuilder().setSchedules(schedules).build()
        activeElementsById.value = activeElements
    }

    /**
     * Deletes all the schedules that aren't the given schedule within the config specified by the itemIdentifier
     * @param itemIdentifier The identifier of the config to delete all the other schedules from.
     * @param schedule The only schedule in the given config that shouldn't be deleted.
     */
    fun deleteOtherSchedules(itemIdentifier: String, schedule: Schedule) {
        LOGGER.debug("deleteOtherSchedules(): $itemIdentifier, $schedule")
        val config = activeElementsById.value!![itemIdentifier]
        if (config == null) {
            LOGGER.warn("setScheduleDays() called on itemIdentifier $itemIdentifier which is not active")
            return
        }

        var schedules: MutableList<Schedule> = config.schedules
        val iterator = schedules.iterator()
        while (iterator.hasNext()) {
            val schedule = iterator.next()
            if (schedule.id != schedule.id) {
                iterator.remove()
            }
        }

        val activeElements = activeElementsById.value!!
        activeElements[itemIdentifier] = config.toBuilder().setSchedules(schedules).build()
        activeElementsById.value = activeElements
    }
}

