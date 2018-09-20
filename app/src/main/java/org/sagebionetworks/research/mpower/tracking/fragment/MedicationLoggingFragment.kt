package org.sagebionetworks.research.mpower.tracking.fragment

import android.support.v7.widget.RecyclerView.Adapter
import android.support.v7.widget.RecyclerView.ViewHolder
import org.sagebionetworks.research.mpower.tracking.view_model.MedicationTrackingTaskViewModel
import org.sagebionetworks.research.mpower.tracking.view_model.configs.MedicationConfig
import org.sagebionetworks.research.mpower.tracking.view_model.configs.Schedule
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SimpleTrackingItemLog
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

/**
 * The fragment in which a user enters which medications have been taken and at what times, for a given day.
 */
class MedicationLoggingFragment : LoggingFragment<MedicationConfig, SimpleTrackingItemLog,
        MedicationTrackingTaskViewModel, Adapter<ViewHolder>>() {

    /**
     * Provides values which define the 3 time blocks. The time blocks are as follows
     *     MORNING -> 12:00 AM - 12:00 PM
     *     AFTERNOON -> 12:00 PM - 4:00 PM
     *     NIGHT -> 4:00 PM - 12:00 AM
     *
     * NOTE: When a MedicationConfig is scheduled to be taken anytime it is considered to be in every time block.
     */
    companion object {
        // The time that all morning scheduled medications should be taken before.
        private val MORNING_CUTOFF = LocalTime.NOON
        // The time that all afternoon scheduled medications should be taken before.
        private val AFTERNOON_CUTOFF = LocalTime.NOON.plusHours(4)

        // Returns 'true' if the given config represents a morning medication, 'false' otherwise.
        private val MORNING_PREDICATE: (MedicationConfig) -> Boolean = { input ->
            input.schedules.any { schedule -> schedule.anytime || schedule.time.isBefore(MORNING_CUTOFF) }
        }

        // Returns 'true' if the given config represents an afternoon medication, 'false' otherwise.
        private val AFTERNOON_PREDICATE: (MedicationConfig) -> Boolean = { input ->
            input.schedules.any { schedule ->
                schedule.anytime
                        || (schedule.time.isBefore(AFTERNOON_CUTOFF) && schedule.time.isAfter(MORNING_CUTOFF))
            }
        }

        // Returns 'true' if the given config represent a night medication, 'false' otherwise.
        private val NIGHT_PREDICATE: (MedicationConfig) -> Boolean = { input ->
            input.schedules.any { schedule -> schedule.anytime || schedule.time.isAfter(AFTERNOON_CUTOFF) }
        }
    }

    override fun getNextFragment(): TrackingFragment<*, *, *> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun initializeAdapter(): Adapter<ViewHolder> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * Returns a list of all the MedicationConfigs which are scheduled to be taken during the same time block as
     * the given time.
     * @param now the to use as the present when calculating which time block the user is currently in.
     */
    private fun getCurrentTimeBlockMedications(now: LocalDateTime) : List<MedicationConfig> {
        val configs = viewModel.activeElementsById.value!!.values
        val predicate = when {
            now.toLocalTime().isBefore(MORNING_CUTOFF) -> MORNING_PREDICATE
            now.toLocalTime().isBefore(AFTERNOON_CUTOFF) -> AFTERNOON_PREDICATE
            else -> NIGHT_PREDICATE
        }

        return configs.filter { config -> predicate(config)
                && config.schedules.any { schedule ->  isForToday(schedule, now.toLocalDate()) }
        }
    }

    /**
     * Returns a list of all of the MedicationConfigs for which the time block they are scheduled to be taken at
     * has passed.
     * @param now the time to use as the present when calculating which time block the user is currently in.
     * @return a list of all of the MedicationConfigs for which the time block they are scheduled to be taken at
     * has passed.
     */
    private fun getMissedMedications(now: LocalDateTime): List<MedicationConfig> {
        val missedCutoff = getMissedTimeCutoff(now.toLocalTime())
        val configs = viewModel.activeElementsById.value!!.values
        return configs.filter { config ->
            config.schedules.any { schedule -> isForToday(schedule, now.toLocalDate())
                    && isPassed(schedule, missedCutoff)
                    && !isLogged(config, schedule) }
        }
    }

    /**
     * Returns the earliest time that is considered part of the time block the user is currently in.
     * @param now the time to use as the present when calculating which time block the user is currently in.
     * @return the earliest time that is considered part of the time block the user is currently in.
     */
    private fun getMissedTimeCutoff(now: LocalTime): LocalTime? {
        return when {
            now.isBefore(MORNING_CUTOFF) -> null
            now.isBefore(AFTERNOON_CUTOFF) -> MORNING_CUTOFF
            else -> AFTERNOON_CUTOFF
        }
    }

    /**
     * Returns 'true' if the time block for logging the given schedule has passed.
     * @param schedule the Schedule to check whether the time time block has passed for.
     * @param missedCutoff the earliest time that is considered part of the time block the user is currently in.
     * @return 'true' if the time block for logging the given schedule has passed.
     */
    private fun isPassed(schedule: Schedule, missedCutoff: LocalTime?): Boolean {
        return when (missedCutoff) {
            null -> false
            else -> !schedule.anytime && schedule.time.isBefore(missedCutoff)
        }
    }

    /**
     * Returns 'true' if there is a log representing the given MedicationConfig logged for the given Schedule.
     * 'false' otherwise
     * @param config the MedicationConfig to check for logs for.
     * @param schedule the Schedule to check for a log for.
     * @return 'true' if there is a log representing the given MedicationConfig logged for the given Schedule.
     * 'false' otherwise
     */
    private fun isLogged(config: MedicationConfig, schedule: Schedule): Boolean {
        val log = viewModel.loggedElementsById.value!![config.identifier]
        // TODO implement this once the MedicationLogs are implemented.
        return false
    }

    /**
     * Returns 'true' if the given schedule should be logged on the given date, 'false' otherwise.
     * @param schedule the schedule to check whether the user should log on the given day.
     * @param today the day to check whether the user should log the schedule on.
     * @return 'true' if the given schedule should be logged on the given date, 'false' otherwise.
     */
    private fun isForToday(schedule: Schedule, today: LocalDate): Boolean {
        if (schedule.anytime || schedule.everday) {
            return true
        }

        val todayString = today.dayOfWeek.name.toLowerCase()
        return schedule.days.contains(todayString)
    }
}