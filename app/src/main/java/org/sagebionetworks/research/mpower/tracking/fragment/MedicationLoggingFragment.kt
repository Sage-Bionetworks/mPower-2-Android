package org.sagebionetworks.research.mpower.tracking.fragment

import android.app.TimePickerDialog
import android.os.Bundle
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import android.view.View
import kotlinx.android.synthetic.main.mpower2_medication_logging_step.*
import org.joda.time.Minutes
import org.sagebionetworks.research.mobile_ui.show_step.view.SystemWindowHelper
import org.sagebionetworks.research.mobile_ui.show_step.view.SystemWindowHelper.Direction
import org.sagebionetworks.research.mpower.R
import org.sagebionetworks.research.mpower.tracking.recycler_view.*
import org.sagebionetworks.research.mpower.tracking.view_model.MedicationTrackingTaskViewModel
import org.sagebionetworks.research.mpower.tracking.view_model.logs.MedicationLog
import org.sagebionetworks.research.mpower.tracking.view_model.logs.MedicationTimestamp
import org.sagebionetworks.research.presentation.model.interfaces.StepView
import org.sagebionetworks.research.sageresearch.extensions.toInstant
import org.slf4j.LoggerFactory
import org.threeten.bp.*
import org.threeten.bp.zone.ZoneRulesException
import java.util.*

/**
 * The fragment in which a user enters which medications have been taken and at what times, for a given day.
 */
class MedicationLoggingFragment : TrackingFragment<MedicationLog, MedicationLog,
        MedicationTrackingTaskViewModel>() {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(MedicationLoggingFragment::class.java)

        fun newInstance(stepView: StepView): MedicationLoggingFragment {
            val fragment = MedicationLoggingFragment()
            val args = TrackingFragment.createArguments(stepView)
            fragment.arguments = args
            return fragment
        }
    }

    private inner class Listener(private val isCurrent: Boolean) : MedicationLoggingListener {
        override fun onAddDetailsPressed(medicationLog: MedicationLog) {
            val schedulingFragment = MedicationSchedulingFragment.newInstance(stepView, medicationLog.identifier)
            addChildFragmentOnTop(schedulingFragment, "MedicationDosageFragment")
        }

        override fun onTimePressed(currentLoggedDate: Instant, medicationIdentifier: String,
                scheduleItem: MedicationLoggingSchedule, position: Int) {

            var selectionItems = viewModel.getSelectionTimes(requireContext(), scheduleItem.dosageItem)
            selectionItems.forEach { it.isSelected = false }
            selectionItems.firstOrNull { it.identifier == scheduleItem.medicationTimestamp?.timeOfDay }?.isSelected = true
            var dialog = GridSelectionFragment.newInstance(
                    getString(R.string.medication_time_edit_title, medicationIdentifier),
                    selectionItems, true)
            dialog.listener = object : ItemsSelectedListener {
                override fun onItemsSelected(selectedItemIds: List<String>) {
                    if (selectedItemIds.isNotEmpty()) {
                        val timeStamp = LocalTime.parse(selectedItemIds.get(0), MedicationTimestamp.timeOfDayFormatter)
                        val updatedLoggedDate = ZonedDateTime.of(LocalDateTime.of(LocalDate.now(), timeStamp), ZoneId.systemDefault()).toInstant()

                        scheduleItem.medicationTimestamp?.let { medicationTimestamp ->
                            scheduleItem.dosageItem.timestamps.remove(medicationTimestamp)
                            val updatedTimeStamp = medicationTimestamp.toBuilder().setLoggedDate(updatedLoggedDate)?.build()
                            updatedTimeStamp?.let { scheduleItem.dosageItem.timestamps.add(it) }
                            val recyclerView = if (isCurrent) medication_recycler_view else missed_medication_recycler_view
                            val updatedScheduleItem = MedicationLoggingSchedule(
                                    scheduleItem.config, scheduleItem.dosageItem, updatedTimeStamp)
                            (recyclerView.adapter as MedicationLoggingAdapter).updateItem(updatedScheduleItem, position)
                        }
                    }
                }
            }
            dialog.show(parentFragmentManager, "Times select")
        }

        override fun onTakenPressed(medicationIdentifier: String, scheduleItem: MedicationLoggingSchedule, position: Int) {
            val now = LocalDateTime.now()
            val zoneId = try {
                ZoneId.systemDefault()
            } catch (e : ZoneRulesException) {
                LOGGER.warn("System default zoneId not set, defaulting to UTC time")
                ZoneId.of("Z")
            }

            var timestampBuilder = scheduleItem.medicationTimestamp?.toBuilder() ?: MedicationTimestamp.builder()
            if (scheduleItem.medicationTimestamp != null) {
                scheduleItem.dosageItem.timestamps.remove(scheduleItem.medicationTimestamp)
            }
            timestampBuilder.setLoggedDate(now.toInstant(zoneId))
            val timeStamp = timestampBuilder.build()
            scheduleItem.dosageItem.timestamps.add(timeStamp)
            val updatedScheduleItem = MedicationLoggingSchedule(scheduleItem.config, scheduleItem.dosageItem, timeStamp)
            val recyclerView = if (isCurrent) medication_recycler_view else missed_medication_recycler_view
            if (scheduleItem.dosageItem.isAnytime) {
                (recyclerView.adapter as MedicationLoggingAdapter).addItem(updatedScheduleItem, position+1)
            } else {
                (recyclerView.adapter as MedicationLoggingAdapter).updateItem(updatedScheduleItem, position)
            }

//            setSubmitButtonEnabled(true)
        }

        override fun onUndoPressed(medicationIdentifier: String, scheduleItem: MedicationLoggingSchedule, position: Int) {

            if (scheduleItem.medicationTimestamp != null) {
                scheduleItem.dosageItem.timestamps.remove(scheduleItem.medicationTimestamp)
            }

            val recyclerView = if (isCurrent) medication_recycler_view else missed_medication_recycler_view
            if (scheduleItem.dosageItem.isAnytime) {
                (recyclerView.adapter as MedicationLoggingAdapter).removeItem(scheduleItem)
            } else {
                var timestampBuilder = scheduleItem.medicationTimestamp?.toBuilder()
                        ?: MedicationTimestamp.builder()
                timestampBuilder.setLoggedDate(null)
                val timeStamp = timestampBuilder.build()
                scheduleItem.dosageItem.timestamps.add(timeStamp)
                val updatedScheduleItem = MedicationLoggingSchedule(scheduleItem.config, scheduleItem.dosageItem, timeStamp)
                (recyclerView.adapter as MedicationLoggingAdapter).updateItem(updatedScheduleItem, position)
            }

//            setSubmitButtonEnabled(shouldSubmitButtonBeEnabled())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupSubmitButton()
        val topListener = SystemWindowHelper.getOnApplyWindowInsetsListener(Direction.TOP)
        rs2_step_navigation_action_add_more.setOnClickListener { _ ->
            val fragment = MedicationReviewFragment.newInstance(stepView, false)
            addChildFragmentOnTop(fragment, "MedicationReview")
        }

        rs2_step_navigation_action_cancel.setOnClickListener { onCancelButtonClicked() }
        ViewCompat.setOnApplyWindowInsetsListener(rs2_step_navigation_action_cancel, topListener)
        setupItemDecorations()
        medication_recycler_view.isNestedScrollingEnabled = false
        missed_medication_recycler_view.isNestedScrollingEnabled = false
        medication_recycler_view.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
                medication_recycler_view.context)
        missed_medication_recycler_view.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
                missed_medication_recycler_view.context)
    }

    override fun onStart() {
        super.onStart()
        if (viewModel.activeElementsById.value.isNullOrEmpty()) {
            replaceWithFragment(MedicationListEmptyFragment.newInstance(stepView));
        }

        view?.let { ViewCompat.requestApplyInsets(it) }
        // Filter the schedules down to those that should appear in the current time blocks medications, and setup
        // the adapter.
        val now = LocalDateTime.now()
        val timeBlock = viewModel.getTimeBlock(now.toLocalTime())
        val currentMedicationAdapter = viewModel.loggedElementsById.value?.let {
            MedicationLoggingAdapter(
                viewModel.getCurrentTimeBlockMedications(timeBlock, now.toLocalDate()).toMutableList(),
                    it, Listener(true))
        }
        medication_recycler_view.adapter = currentMedicationAdapter
        // Filter the schedules down to those that should appear in the missed medications and setup the adapter.
        val missedMedications = viewModel.getMissedMedications(timeBlock, now).toMutableList()
        if (missedMedications.isEmpty()) {
            missed_medication_label.visibility = View.GONE
            missed_medication_recycler_view.visibility = View.GONE
        } else {
            missed_medication_label.visibility = View.VISIBLE
            missed_medication_recycler_view.visibility = View.VISIBLE
            val missedMedicationAdapter = viewModel.loggedElementsById.value?.let {
                MedicationLoggingAdapter(missedMedications,
                        it, Listener(false))
            }
            missed_medication_recycler_view.adapter = missedMedicationAdapter
        }

        // setup the title to include the correct time block name
        val currentMedicationsTitle = resources.getString(R.string.medication_logging_current_medications,
                timeBlock.first)
        medication_label.text = currentMedicationsTitle
    }

    private fun setupItemDecorations() {
        val titleDivider = ResourcesCompat.getDrawable(
                resources, R.drawable.mpower2_logging_item_decoration, null) ?: return

        val scheduleDivider = ResourcesCompat.getDrawable(
                resources, R.drawable.form_step_divider, null) ?: return

        val titleDecoration = SelectiveItemDividerDecoration(titleDivider,
                object : SelectiveItemDividerDecoration.Selector {
                    override fun shouldDrawDivider(current: ViewHolder, next: ViewHolder?): Boolean {
                        return current is MedicationLoggingAddDetailsViewHolder ||
                                (current is MedicationScheduleViewHolder && (next is MedicationTitleViewHolder || next is MedicationLoggingAddDetailsViewHolder))
                    }
                })

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
//
//    private fun getTimeOfDay(schedule: Schedule, now: LocalTime): String {
//        return when {
//            schedule.isAnytime() -> viewModel.getTimeBlock(now).first
//            else -> schedule.timeOfDay ?: ""
//        }
//    }

    /**
     * Called when the submit button is clicked.
     * @param view that was clicked.
     */
    protected fun onSubmitButtonClicked(view: View) {
        val loggingResult = viewModel.loggingCollection
        performTaskViewModel.addStepResult(loggingResult)
        performTaskFragment.goForward()
    }

    open protected fun setupSubmitButton() {
        if (rs2_step_navigation_action_bar == null) {
            return
        }
        setSubmitButtonEnabled(shouldSubmitButtonBeEnabled())
        rs2_step_navigation_action_bar.forwardButton.setText(R.string.button_submit)
        rs2_step_navigation_action_bar.forwardButton.setOnClickListener { onSubmitButtonClicked(it) }
    }

    /**
     * @param enabled when true, submit button will be enabled, when false it will be disabled.
     */
    protected fun setSubmitButtonEnabled(enabled: Boolean) {
        if (rs2_step_navigation_action_bar == null) {
            return
        }
        rs2_step_navigation_action_bar.setForwardButtonEnabled(enabled)
    }

    /**
     * @return if the user has interacted with any logging ui yet, false if they just arrived on the screen.
     */
    protected fun shouldSubmitButtonBeEnabled(): Boolean {
        return true
//        viewModel.loggedElementsById.value?.let {
//            for (medicationLog in it.values) {
//                if (medicationLog.loggedDate != null) {
//                    return true
//                }
//            }
//        }
//        return false
    }

    /**
     * Called when the cancel button is clicked.
     * @param view the cancel button view.
     */
    protected fun onCancelButtonClicked() {
        this.performTaskFragment.cancelTask(true)
    }
}
