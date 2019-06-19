package org.sagebionetworks.research.mpower.tracking.fragment

import android.os.Bundle
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.view.ViewCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView.ItemDecoration
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.BindView
import kotlinx.android.synthetic.main.mpower2_medication_logging_step.*
import kotlinx.android.synthetic.main.mpower2_medication_scheduling.*
import kotlinx.android.synthetic.main.mpower2_medication_scheduling.rs2_step_navigation_action_bar
import org.sagebionetworks.research.mobile_ui.show_step.view.SystemWindowHelper
import org.sagebionetworks.research.mobile_ui.widget.ActionButton
import org.sagebionetworks.research.mpower.R
import org.sagebionetworks.research.mpower.tracking.SortUtil
import org.sagebionetworks.research.mpower.tracking.recycler_view.Listener
import org.sagebionetworks.research.mpower.tracking.recycler_view.MedicationAdapter
import org.sagebionetworks.research.mpower.tracking.view_model.MedicationTrackingTaskViewModel
import org.sagebionetworks.research.mpower.tracking.view_model.logs.DosageItem
import org.sagebionetworks.research.mpower.tracking.view_model.logs.MedicationLog
import org.sagebionetworks.research.mpower.tracking.view_model.logs.MedicationTimestamp
import org.sagebionetworks.research.presentation.model.interfaces.StepView
import org.slf4j.LoggerFactory
import org.threeten.bp.format.DateTimeFormatter

class MedicationSchedulingFragment :
        RecyclerViewTrackingFragment<MedicationLog, MedicationLog,
                MedicationTrackingTaskViewModel, MedicationAdapter>() {

    private val LOGGER = LoggerFactory.getLogger(MedicationSchedulingFragment::class.java)
    private val config: MedicationLog?
        get() {
            viewModel.activeElementsById.value?.let {
                return it[identifier]
            } ?: return null
        }

    private lateinit var identifier: String

    companion object {
        private val ARG_IDENTIFIER = "identifier"

        fun newInstance(step: StepView, identifier: String): MedicationSchedulingFragment {
            val fragment = MedicationSchedulingFragment()
            val args = TrackingFragment.createArguments(step)
            args.putString(ARG_IDENTIFIER, identifier)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LOGGER.debug("onCreate()")
        if (savedInstanceState != null) {
            identifier = savedInstanceState.getString(ARG_IDENTIFIER) ?: ""
        } else if (arguments != null) {
            identifier = (arguments as Bundle).getString(ARG_IDENTIFIER) ?: ""
        } else {
            LOGGER.warn("MedicationSchedulingFragment created without an identifier argument")
            return
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        title.text = identifier
        val str = getString(R.string.add_dose)
        val content = SpannableString(str)
        content.setSpan(UnderlineSpan(), 0, str.length, 0)
        detail.text = content
        //TODO: Add disabled state
        detail.setOnClickListener { addDose() }
        navigationActionBar.forwardButton.setText(R.string.button_save)
        navigationActionBar.setActionButtonClickListener { _ ->
            if (fragmentManager!!.backStackEntryCount > 0) {
                fragmentManager!!.popBackStack()
            } else {
                val fragment = MedicationLoggingFragment.newInstance(stepView)
                replaceWithFragment(fragment)
            }

//            val viewModelValue = viewModel.activeElementsById.value
//                    ?: return@setActionButtonClickListener
//            var configs = SortUtil.getActiveElementsSorted(viewModelValue)
//            configs = configs.filter { config -> !config.isConfigured }
//            val iterator = configs.iterator()
//            val fragment = if (iterator.hasNext()) {
//                val nextConfig = iterator.next()
//                MedicationSchedulingFragment.newInstance(stepView, nextConfig.identifier)
//            } else {
//                MedicationReviewFragment.newInstance(stepView)
//            }
//
//            replaceWithFragment(fragment)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rs2_step_navigation_action_backward.setOnClickListener { _ ->
            // Back button will always go back to the previous screen
            fragmentManager?.popBackStack()
        }

        val topInsetListener = SystemWindowHelper
                .getOnApplyWindowInsetsListener(SystemWindowHelper.Direction.TOP)
        ViewCompat.setOnApplyWindowInsetsListener(rs2_step_navigation_action_delete, topInsetListener)
        rs2_step_navigation_action_delete.setOnClickListener { _ ->
            onRemoveMedicationClicked()
        }

        setupNextButtonEnabled()
    }

    override fun initializeItemDecoration(): ItemDecoration? {
        val itemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        ResourcesCompat.getDrawable(resources, R.drawable.mpower2_logging_item_decoration, null)?.let {
            itemDecoration.setDrawable(it)
        }
        return itemDecoration
    }

    override fun initializeAdapter(): MedicationAdapter {
        val listener: Listener = object : Listener {
            override fun onRemovePressed(dosage: DosageItem) {
                val configUnwrapped = config ?: return
                configUnwrapped.dosageItems.remove(dosage)
                adapter.removeDosage(dosage)
            }

            override fun onDosageTextChange(dosageItem: DosageItem, title: String) {
                val configUnwrapped = config ?: return
                dosageItem.dosage = title
                setupNextButtonEnabled()
            }

            override fun onTimeSelectionPressed(schedule: DosageItem) {
//                val cal = Calendar.getInstance()
//                val zoneId : ZoneId = try {
//                    ZoneId.systemDefault()
//                } catch (e : Throwable) {
//                    ZoneId.of("Z")
//                }
//
//                schedule.getLocalTimeOfDay()?.let {
//                    cal.time = DateTimeUtils.toDate(it.atDate(LocalDate.now()).toInstant(zoneId))
//                }
//                val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hours, minutes ->
//                    cal.set(Calendar.HOUR, hours)
//                    cal.set(Calendar.MINUTE, minutes)
//                    schedule.setLocalTimeOfDay(DateTimeUtils.toZonedDateTime(cal).toLocalTime())
//                    val configUnwrapped = config ?: return@OnTimeSetListener
//                    val schedules = configUnwrapped.schedules.toMutableList()
//                    schedules[position] = schedule
//                    adapter.updateDosage(position, schedule)
//                    val updatedConfig = configUnwrapped.toBuilder().setSchedules(schedules).build()
//                    viewModel.addConfig(updatedConfig)
//                }
//
//                TimePickerDialog(context, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),
//                        false)
//                        .show()
            }

            override fun onDaySelectionPressed(dosageItem: DosageItem) {
                val formatter = DateTimeFormatter.ofPattern("h:mm a")
                LOGGER.debug("showDaySelection()")
                val dialog = MedicationDayFragment.newInstance(
                        identifier, "", dosageItem.daysOfWeek)
                dialog.listener = object : DaySelectedListener {
                    override fun onDaySelected(scheduleName: String, days: Set<Int>) {
                        val configUnwrapped = config ?: return
                        dosageItem.daysOfWeek.clear()
                        dosageItem.daysOfWeek.addAll(days)
                        adapter.updateDosage(dosageItem)
                    }
                }

                dialog.show(fragmentManager, "Day select")
            }

            override fun onAnytimeSet(dosageItem: DosageItem, anytime: Boolean) {
                if (anytime) {
                    dosageItem.timestamps.clear()
                    dosageItem.daysOfWeek.addAll(DosageItem.dailySet)
                } else {
                    dosageItem.timestamps.add(MedicationTimestamp.builder().setTimeOfDay("08:00")!!.build())
                }
                adapter.updateDosage(dosageItem)
            }
        }

//        var dosages = config?.dosageItems?.toMutableList() ?: mutableListOf(DosageItem.builder().setDaysOfWeek(mutableSetOf()).build())
        if (config!!.dosageItems.isEmpty()) {
            val dose = DosageItem("", DosageItem.dailySet.toMutableSet(), mutableSetOf())
            config?.dosageItems?.add(dose)
        }
        return MedicationAdapter(config!!.dosageItems.toMutableList(), listener)
    }

    override fun getLayoutId(): Int {
        return R.layout.mpower2_medication_scheduling
    }

    private fun addDose() {
        val configUnwrapped = config ?: return
        val dose = DosageItem("", DosageItem.dailySet.toMutableSet(), mutableSetOf())
        configUnwrapped.dosageItems.add(0, dose)
        adapter.addDosage(dose)
        setupNextButtonEnabled()
    }

    private fun setupNextButtonEnabled() {
        navigationActionBar.setForwardButtonEnabled(config?.isConfigured ?: false)
        detail.isEnabled = config?.isConfigured ?: false
        detail.isClickable = config?.isConfigured ?: false
    }

    private fun onRemoveMedicationClicked() {
        val dialog = MedicationRemoveFragment.newInstance(
                identifier, object : MedicationRemoveListener {
            override fun onRemoveMedicationConfirmed() {
                viewModel.removeLoggedElement(identifier)
                viewModel.removeConfig(identifier)
                viewModel.activeElementsById.value?.let {
                    if (it.isEmpty()) {
                        replaceWithFragment(MedicationSelectionFragment.newInstance(stepView))
                    } else {
                        replaceWithFragment(MedicationAddDetailsFragment.newInstance(stepView))
                    }
                }
            }
        })
        dialog.show(fragmentManager, "Remove medication")
    }
}