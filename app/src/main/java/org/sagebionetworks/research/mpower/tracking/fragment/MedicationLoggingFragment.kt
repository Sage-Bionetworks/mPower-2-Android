package org.sagebionetworks.research.mpower.tracking.fragment

import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView.ItemDecoration
import android.support.v7.widget.RecyclerView.ViewHolder
import org.sagebionetworks.research.mpower.R
import org.sagebionetworks.research.mpower.tracking.SortUtil
import org.sagebionetworks.research.mpower.tracking.recycler_view.MedicationLoggingAdapter
import org.sagebionetworks.research.mpower.tracking.recycler_view.MedicationLoggingItem
import org.sagebionetworks.research.mpower.tracking.recycler_view.MedicationLoggingListener
import org.sagebionetworks.research.mpower.tracking.recycler_view.MedicationLoggingSchedule
import org.sagebionetworks.research.mpower.tracking.recycler_view.MedicationLoggingTitle
import org.sagebionetworks.research.mpower.tracking.recycler_view.MedicationScheduleViewHolder
import org.sagebionetworks.research.mpower.tracking.recycler_view.MedicationTitleViewHolder
import org.sagebionetworks.research.mpower.tracking.recycler_view.SelectiveItemDividerDecoration
import org.sagebionetworks.research.mpower.tracking.view_model.MedicationTrackingTaskViewModel
import org.sagebionetworks.research.mpower.tracking.view_model.configs.MedicationConfig
import org.sagebionetworks.research.mpower.tracking.view_model.configs.Schedule
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SimpleTrackingItemLog
import org.sagebionetworks.research.presentation.model.interfaces.StepView
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

/**
 * The fragment in which a user enters which medications have been taken and at what times, for a given day.
 */
class MedicationLoggingFragment : LoggingFragment<MedicationConfig, SimpleTrackingItemLog,
        MedicationTrackingTaskViewModel, MedicationLoggingAdapter>() {

    /**
     * Provides values which define the 3 time blocks. The time blocks are as follows
     *     MORNING -> 12:00 AM - 12:00 PM
     *     AFTERNOON -> 12:00 PM - 4:00 PM
     *     NIGHT -> 4:00 PM - 12:00 AM
     *
     * NOTE: When a MedicationConfig is scheduled to be taken anytime it is considered to be in every time block.
     */
    companion object {
        fun newInstance(stepView : StepView) : MedicationLoggingFragment {
            val fragment = MedicationLoggingFragment()
            val args = TrackingFragment.createArguments(stepView)
            fragment.arguments = args
            return fragment
        }

        // The time that all morning scheduled medications should be taken before.
        private val MORNING_CUTOFF = LocalTime.NOON
        // The time that all afternoon scheduled medications should be taken before.
        private val AFTERNOON_CUTOFF = LocalTime.NOON.plusHours(4)

        // Returns 'true' if the given config represents a morning medication, 'false' otherwise.
        private val MORNING_PREDICATE: (Schedule) -> Boolean = { schedule ->
            schedule.anytime || schedule.time.isBefore(MORNING_CUTOFF)
        }

        // Returns 'true' if the given config represents an afternoon medication, 'false' otherwise.
        private val AFTERNOON_PREDICATE: (Schedule) -> Boolean = { schedule ->
            schedule.anytime
                    || (schedule.time.isBefore(AFTERNOON_CUTOFF) && schedule.time.isAfter(MORNING_CUTOFF))
        }

        // Returns 'true' if the given config represent a night medication, 'false' otherwise.
        private val NIGHT_PREDICATE: (Schedule) -> Boolean = { schedule ->
            schedule.anytime || schedule.time.isAfter(AFTERNOON_CUTOFF)
        }
    }

    override fun onStart() {
        super.onStart()
        val titleDivider = resources.getDrawable(R.drawable.mpower2_logging_item_decoration)
        val titleDecoration = SelectiveItemDividerDecoration(titleDivider, object : SelectiveItemDividerDecoration.Selector {
            override fun shouldDrawDivider(current: ViewHolder, next: ViewHolder?): Boolean {
                return current is MedicationScheduleViewHolder && next is MedicationTitleViewHolder
            }
        })

        val scheduleDivider = resources.getDrawable(R.drawable.form_step_divider)
        val scheduleDecoration = SelectiveItemDividerDecoration(scheduleDivider, object : SelectiveItemDividerDecoration.Selector {
            override fun shouldDrawDivider(current: ViewHolder, next: ViewHolder?): Boolean {
                return current is MedicationScheduleViewHolder && next is MedicationScheduleViewHolder
            }
        })

        recyclerView.addItemDecoration(titleDecoration)
        recyclerView.addItemDecoration(scheduleDecoration)
    }

    override fun getNextFragment(): TrackingFragment<*, *, *> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun initializeAdapter(): MedicationLoggingAdapter {
        val listener = object : MedicationLoggingListener {
            override fun onTakenPressed(medicationIdentifier: String, schedule: Schedule, position: Int) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onUndoPressed(medicationIdentifier: String, schedule: Schedule, position: Int) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }

        val now = LocalDateTime.now()
        val sortedConfigs = SortUtil.getActiveElementsSorted(viewModel.activeElementsById.value!!)
        return MedicationLoggingAdapter(getCurrentTimeBlockMedications(now, sortedConfigs),
                viewModel.loggedElementsById.value!!, listener)
    }

    override fun initializeItemDecoration(): ItemDecoration? {
        return null
    }

    /**
     * Returns the list of MedicationLoggingItems that should be displayed for the current time block.
     * A schedule should be displayed if it is anytime, or if it is for today and for the given time block. If a
     * config has a least one schedule a MedicationLoggingTitle will be added for it before all of its schedules.
     * @param now the to use as the present when calculating which time block the user is currently in.
     * @return the list of MedicationLoggingItems that should be displayed for the current time block.
     */
    private fun getCurrentTimeBlockMedications(now: LocalDateTime, sortedConfigs: List<MedicationConfig>)
            : List<MedicationLoggingItem> {
        val predicate = when {
            now.toLocalTime().isBefore(MORNING_CUTOFF) -> MORNING_PREDICATE
            now.toLocalTime().isBefore(AFTERNOON_CUTOFF) -> AFTERNOON_PREDICATE
            else -> NIGHT_PREDICATE
        }

        val items: MutableList<MedicationLoggingItem> = mutableListOf()
        for (config in sortedConfigs) {
            val schedules = config.schedules.filter { schedule ->
                schedule.anytime || !(isForToday(schedule, now.toLocalDate()) && predicate(schedule))
            }

            if (!schedules.isEmpty()) {
                items.add(MedicationLoggingTitle(config.identifier + " " + config.dosage))
                items.addAll(schedules.map { schedule -> MedicationLoggingSchedule(config, schedule) })
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
    private fun getMissedMedications(now: LocalDateTime, sortedConfigs: List<MedicationConfig>)
            : List<MedicationLoggingItem> {
        val missedCutoff = getMissedTimeCutoff(now.toLocalTime())
        val missedItems: MutableList<MedicationLoggingItem> = mutableListOf()
        for (config in sortedConfigs) {
            val missedSchedules = config.schedules.filter { schedule ->
                isForToday(schedule, now.toLocalDate())
                        && isPassed(schedule, missedCutoff)
                        && !isLogged(config, schedule)
            }

            if (!missedSchedules.isEmpty()) {
                missedItems.add(MedicationLoggingTitle(config.identifier + " " + config.dosage))
                missedItems.addAll(missedSchedules.map { schedule -> MedicationLoggingSchedule(config, schedule) })
            }
        }

        return missedItems
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