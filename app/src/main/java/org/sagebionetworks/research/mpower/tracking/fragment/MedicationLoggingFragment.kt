package org.sagebionetworks.research.mpower.tracking.fragment

import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.View
import android.view.ViewConfiguration
import com.google.common.collect.ImmutableRangeSet
import com.google.common.collect.Range
import com.google.common.collect.RangeSet
import kotlinx.android.synthetic.main.mpower2_medication_logging_step.medication_label
import kotlinx.android.synthetic.main.mpower2_medication_logging_step.medication_recycler_view
import kotlinx.android.synthetic.main.mpower2_medication_logging_step.missed_medication_label
import kotlinx.android.synthetic.main.mpower2_medication_logging_step.missed_medication_recycler_view
import kotlinx.android.synthetic.main.mpower2_medication_logging_step.rs2_step_navigation_action_add_more
import kotlinx.android.synthetic.main.mpower2_medication_logging_step.rs2_step_navigation_action_cancel
import org.sagebionetworks.research.mobile_ui.show_step.view.SystemWindowHelper
import org.sagebionetworks.research.mobile_ui.show_step.view.SystemWindowHelper.Direction
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
import org.sagebionetworks.research.mpower.tracking.view_model.logs.MedicationLog
import org.sagebionetworks.research.presentation.model.interfaces.StepView
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter

typealias TimeBlock = Pair<String, RangeSet<LocalTime>>

/**
 * The fragment in which a user enters which medications have been taken and at what times, for a given day.
 */
class MedicationLoggingFragment : TrackingFragment<MedicationConfig, MedicationLog,
        MedicationTrackingTaskViewModel>() {

    /**
     * Provides values which define the 3 time blocks. The time blocks are as follows
     *     MORNING -> 12:00 AM - 12:00 PM
     *     AFTERNOON -> 12:00 PM - 4:00 PM
     *     NIGHT -> 4:00 PM - 12:00 AM
     *
     * NOTE: When a MedicationConfig is scheduled to be taken anytime it is considered to be in every time block.
     */
    companion object {

        fun newInstance(stepView: StepView): MedicationLoggingFragment {
            val fragment = MedicationLoggingFragment()
            val args = TrackingFragment.createArguments(stepView)
            fragment.arguments = args
            return fragment
        }

        private val CUTOFF_TIMES = listOf(LocalTime.MIDNIGHT.plusHours(5),
                LocalTime.NOON,
                LocalTime.NOON.plusHours(5),
                LocalTime.MIDNIGHT.minusHours(2))
    }

    private val timeBlocks: MutableSet<TimeBlock> = mutableSetOf()
    private val listener: MedicationLoggingListener = object : MedicationLoggingListener {
        override fun onTakenPressed(medicationIdentifier: String, schedule: Schedule, position: Int) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onUndoPressed(medicationIdentifier: String, schedule: Schedule, position: Int) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fiveAM = LocalTime.MIDNIGHT.plusHours(5)
        val fivePM = LocalTime.NOON.plusHours(5)
        val tenPM = LocalTime.NOON.plusHours(10)
        val morning = resources.getString(R.string.medication_logging_morning_time_block)
        val afternoon = resources.getString(R.string.medication_logging_afternoon_time_block)
        val evening = resources.getString(R.string.medication_logging_evening_time_block)
        val night = resources.getString(R.string.medication_logging_night_time_block)
        timeBlocks.add(Pair(morning, ImmutableRangeSet.of(Range.openClosed(fiveAM, LocalTime.NOON))))
        timeBlocks.add(Pair(afternoon, ImmutableRangeSet.of(Range.openClosed(LocalTime.NOON, fivePM))))
        timeBlocks.add(Pair(evening, ImmutableRangeSet.of(Range.openClosed(fivePM, tenPM))))
        timeBlocks.add(Pair(night, ImmutableRangeSet.unionOf(listOf(Range.lessThan(fiveAM), Range.atLeast(tenPM)))))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val topListener = SystemWindowHelper.getOnApplyWindowInsetsListener(Direction.TOP)
        rs2_step_navigation_action_add_more.setOnClickListener { _ ->
            val fragment = MedicationReviewFragment.newInstance(stepView)
            replaceWithFragment(fragment)
        }

        ViewCompat.setOnApplyWindowInsetsListener(rs2_step_navigation_action_cancel, topListener)
        setupItemDecorations()
        medication_recycler_view.isNestedScrollingEnabled = false
        missed_medication_recycler_view.isNestedScrollingEnabled = false
        medication_recycler_view.layoutManager = LinearLayoutManager(medication_recycler_view.context)
        missed_medication_recycler_view.layoutManager = LinearLayoutManager(missed_medication_recycler_view.context)
    }

    override fun onStart() {
        super.onStart()
        ViewCompat.requestApplyInsets(view)
        // Filter the schedules down to those that should appear in the current time blocks medications, and setup
        // the adapter.
        val now = LocalDateTime.now()
        val timeBlock = getTimeBlock(now.toLocalTime())
        val sortedConfigs = SortUtil.getActiveElementsSorted(viewModel.activeElementsById.value!!)
        val currentMedicationAdapter = MedicationLoggingAdapter(
                getCurrentTimeBlockMedications(timeBlock, now.toLocalDate(), sortedConfigs),
                viewModel.loggedElementsById.value!!, listener)
        medication_recycler_view.adapter = currentMedicationAdapter
        // Filter the schedules down to those that should appear in the missed medications and setup the adapter.
        val missedMedications = getMissedMedications(timeBlock, now, sortedConfigs)
        if (missedMedications.isEmpty()) {
            missed_medication_label.visibility = View.GONE
            missed_medication_recycler_view.visibility = View.GONE
        } else {
            missed_medication_label.visibility = View.VISIBLE
            missed_medication_recycler_view.visibility = View.VISIBLE
            val missedMedicationAdapter = MedicationLoggingAdapter(missedMedications,
                    viewModel.loggedElementsById.value!!, listener)
            missed_medication_recycler_view.adapter = missedMedicationAdapter
        }

        // setup the title to include the correct time block name
        val currentMedicationsTitle = resources.getString(R.string.medication_logging_current_medications,
                timeBlock.first)
        medication_label.text = currentMedicationsTitle
    }

    private fun setupItemDecorations() {
        val titleDivider = resources.getDrawable(R.drawable.mpower2_logging_item_decoration)
        val titleDecoration = SelectiveItemDividerDecoration(titleDivider,
                object : SelectiveItemDividerDecoration.Selector {
                    override fun shouldDrawDivider(current: ViewHolder, next: ViewHolder?): Boolean {
                        return current is MedicationScheduleViewHolder && next is MedicationTitleViewHolder
                    }
                })

        val scheduleDivider = resources.getDrawable(R.drawable.form_step_divider)
        val scheduleDecoration = SelectiveItemDividerDecoration(scheduleDivider,
                object : SelectiveItemDividerDecoration.Selector {
                    override fun shouldDrawDivider(current: ViewHolder, next: ViewHolder?): Boolean {
                        return current is MedicationScheduleViewHolder && next is MedicationScheduleViewHolder
                    }
                })

        medication_recycler_view.addItemDecoration(titleDecoration)
        medication_recycler_view.addItemDecoration(scheduleDecoration)
        missed_medication_recycler_view.addItemDecoration(titleDecoration)
        missed_medication_recycler_view.addItemDecoration(scheduleDecoration)
    }

    override fun getLayoutId(): Int {
        return R.layout.mpower2_medication_logging_step
    }

    /**
     * Returns the list of MedicationLoggingItems that should be displayed for the current time block.
     * A schedule should be displayed if it is anytime, or if it is for today and for the given time block. If a
     * config has a least one schedule a MedicationLoggingTitle will be added for it before all of its schedules.
     * @param today the date to use as the present when calculating which schedules are applicable
     * @return the list of MedicationLoggingItems that should be displayed for the current time block.
     */
    private fun getCurrentTimeBlockMedications(timeBlock: TimeBlock, today: LocalDate,
            sortedConfigs: List<MedicationConfig>): List<MedicationLoggingItem> {
        val items: MutableList<MedicationLoggingItem> = mutableListOf()
        for (config in sortedConfigs) {
            val schedules = config.schedules.filter { schedule ->
                schedule.anytime
                        || (isForToday(schedule, today) && timeBlock.second.contains(schedule.time))
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
    private fun getMissedMedications(timeBlock: TimeBlock, now: LocalDateTime, sortedConfigs: List<MedicationConfig>)
            : List<MedicationLoggingItem> {
        val containingRange = timeBlock.second.asRanges().first { range -> range.contains(now.toLocalTime()) }
        val lowerEndpoint = if (containingRange.hasLowerBound()) containingRange.lowerEndpoint() else LocalTime.MIN
        val missedRange = Range.lessThan(lowerEndpoint)
        val missedItems: MutableList<MedicationLoggingItem> = mutableListOf()
        for (config in sortedConfigs) {
            val missedSchedules = config.schedules.filter { schedule ->
                !schedule.anytime && isForToday(schedule, now.toLocalDate()) && missedRange.contains(
                        schedule.time) && !isLogged(config, schedule, timeBlock.first)
            }

            if (!missedSchedules.isEmpty()) {
                missedItems.add(MedicationLoggingTitle(config.identifier + " " + config.dosage))
                missedItems.addAll(missedSchedules.map { schedule -> MedicationLoggingSchedule(config, schedule) })
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
    private fun getTimeBlock(time: LocalTime): TimeBlock {
        return timeBlocks.first { pair -> pair.second.contains(time) }
    }

    /**
     * Returns 'true' if there is a log representing the given MedicationConfig logged for the given Schedule.
     * 'false' otherwise
     * @param config the MedicationConfig to check for logs for.
     * @param schedule the Schedule to check for a log for.
     * @param timeBlock the string representation of the timeblock the user is currently in
     * (e.g. morning, afternoon, evening, night)
     * @return 'true' if there is a log representing the given MedicationConfig logged for the given Schedule.
     * 'false' otherwise
     */
    private fun isLogged(config: MedicationConfig, schedule: Schedule, timeBlockName: String): Boolean {
        val log = viewModel.loggedElementsById.value!![config.identifier]
        val expectedTimeOfDay = when {
            schedule.anytime -> timeBlockName
            else -> {
                val formatter = DateTimeFormatter.ofPattern("HH:mm")
                formatter.format(schedule.time)
            }
        }

        val timestamps = log?.timestamps
        return timestamps?.any { timestamp -> timestamp.timeOfDay == expectedTimeOfDay } ?: false
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