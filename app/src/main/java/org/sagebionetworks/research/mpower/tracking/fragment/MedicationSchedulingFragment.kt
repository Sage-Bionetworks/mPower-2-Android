package org.sagebionetworks.research.mpower.tracking.fragment

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
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
import kotlinx.android.synthetic.main.mpower2_medication_scheduling.*
import org.sagebionetworks.research.mobile_ui.show_step.view.SystemWindowHelper
import org.sagebionetworks.research.mpower.R
import org.sagebionetworks.research.mpower.tracking.recycler_view.Listener
import org.sagebionetworks.research.mpower.tracking.recycler_view.MedicationAdapter
import org.sagebionetworks.research.mpower.tracking.view_model.MedicationTrackingTaskViewModel
import org.sagebionetworks.research.mpower.tracking.view_model.logs.DosageItem
import org.sagebionetworks.research.mpower.tracking.view_model.logs.MedicationLog
import org.sagebionetworks.research.mpower.tracking.view_model.logs.MedicationTimestamp
import org.sagebionetworks.research.presentation.model.interfaces.StepView
import org.slf4j.LoggerFactory

class MedicationSchedulingFragment :
        RecyclerViewTrackingFragment<MedicationLog, MedicationLog,
                MedicationTrackingTaskViewModel, MedicationAdapter>() {

    private val LOGGER = LoggerFactory.getLogger(MedicationSchedulingFragment::class.java)
    private val configOriginal: MedicationLog?
        get() {
            viewModel.activeElementsById.value?.let {
                return it[identifier]
            } ?: return null
        }

    private val config: MedicationLog?
        get() {
            return medScheduleViewModel.medicationLog
        }

    private lateinit var identifier: String
    private lateinit var medScheduleViewModel: MedicationSchedulingViewModel

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
        val medicationLog = configOriginal?.copy(false)
        medScheduleViewModel = ViewModelProviders.of(this, MedicationSchedulingViewModelFactory(medicationLog)).get(MedicationSchedulingViewModel::class.java)

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
            configOriginal?.dosageItems?.clear()
            configOriginal?.dosageItems?.addAll(config!!.dosageItems)
            if (fragmentManager!!.backStackEntryCount > 0) {
                fragmentManager!!.popBackStack()
            } else {
                val fragment = MedicationLoggingFragment.newInstance(stepView)
                replaceWithFragment(fragment)
            }
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

            override fun onTimeSelectionPressed(dosageItem: DosageItem) {

                var dialog = GridSelectionFragment.newInstance(
                        getString(R.string.medication_time_selection_title, identifier),
                        viewModel.getSelectionTimes(context!!, dosageItem), false)
                dialog.listener = object : ItemsSelectedListener {
                    override fun onItemsSelected(selectedItemIds: List<String>) {
                        val configUnwrapped = config ?: return
                        dosageItem.timestamps.clear()
                        val timestamps = selectedItemIds.map { MedicationTimestamp.builder().setTimeOfDay(it)!!.build() }
                        dosageItem.timestamps.addAll(timestamps)
                        adapter.updateDosage(dosageItem)
                    }
                }
                dialog.show(fragmentManager, "Times select")
            }

            override fun onDaySelectionPressed(dosageItem: DosageItem) {
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
                    dosageItem.timestamps.clear()
                    dosageItem.timestamps.add(MedicationTimestamp.builder().setTimeOfDay("08:00")!!.build())
                }
                adapter.updateDosage(dosageItem)
            }
        }

        if (config!!.dosageItems.isEmpty()) {
            val dose = DosageItem("", DosageItem.dailySet.toMutableSet(), mutableSetOf())
            config?.dosageItems?.add(dose)
        }
        return MedicationAdapter(config!!.dosageItems.toMutableList(), listener, medScheduleViewModel)
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
                    } else if (fragmentManager!!.backStackEntryCount > 0) {
                        fragmentManager!!.popBackStack()
                    } else {
                        val fragment = MedicationLoggingFragment.newInstance(stepView)
                        replaceWithFragment(fragment)
                    }
                }
            }
        })
        dialog.show(fragmentManager, "Remove medication")
    }

}

class MedicationSchedulingViewModelFactory(val medicationLog: MedicationLog?) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MedicationSchedulingViewModel(medicationLog) as T
    }
}

class MedicationSchedulingViewModel(medLog: MedicationLog?) : ViewModel() {

    val medicationLog = medLog
    var editIndex = if (medicationLog!!.dosageItems.size < 2) 0 else -1

}