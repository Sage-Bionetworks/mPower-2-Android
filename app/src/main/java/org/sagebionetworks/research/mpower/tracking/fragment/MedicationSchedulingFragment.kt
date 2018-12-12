package org.sagebionetworks.research.mpower.tracking.fragment

import android.app.TimePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView.ItemDecoration
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.medication_dosage.dosage_input
import kotlinx.android.synthetic.main.mpower2_logging_step.rs2_step_navigation_action_add_more
import org.sagebionetworks.research.mpower.R
import org.sagebionetworks.research.mpower.tracking.SortUtil
import org.sagebionetworks.research.mpower.tracking.recycler_view.Listener
import org.sagebionetworks.research.mpower.tracking.recycler_view.MedicationAdapter
import org.sagebionetworks.research.mpower.tracking.view_model.configs.Schedule
import org.sagebionetworks.research.mpower.tracking.view_model.MedicationTrackingTaskViewModel
import org.sagebionetworks.research.mpower.tracking.view_model.configs.MedicationConfig
import org.sagebionetworks.research.mpower.tracking.view_model.logs.MedicationLog
import org.sagebionetworks.research.presentation.model.interfaces.StepView
import org.sagebionetworks.research.sageresearch.extensions.toInstant
import org.slf4j.LoggerFactory
import org.threeten.bp.DateTimeUtils
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.zone.ZoneRulesException
import java.util.Calendar

class MedicationSchedulingFragment :
        RecyclerViewTrackingFragment<MedicationConfig, MedicationLog,
                MedicationTrackingTaskViewModel, MedicationAdapter>() {

    private val LOGGER = LoggerFactory.getLogger(MedicationSchedulingFragment::class.java)
    private val config: MedicationConfig?
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
        val str = getString(R.string.remove_medication)
        val content = SpannableString(getString(R.string.remove_medication))
        content.setSpan(UnderlineSpan(), 0, str.length, 0)
        detail.text = content
        detail.setOnClickListener(::onRemoveMedicationClicked)
        navigationActionBar.setActionButtonClickListener { actionButton ->
            val viewModelValue = viewModel.activeElementsById.value
                    ?: return@setActionButtonClickListener
            var configs = SortUtil.getActiveElementsSorted(viewModelValue)
            configs = configs.filter { config -> !config.isConfigured }
            val iterator = configs.iterator()
            val fragment = if (iterator.hasNext()) {
                val nextConfig = iterator.next()
                MedicationSchedulingFragment.newInstance(stepView, nextConfig.identifier)
            } else {
                MedicationReviewFragment.newInstance(stepView)
            }

            replaceWithFragment(fragment)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dosage_input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val configUnwrapped = config ?: return
                val updatedConfig = configUnwrapped.toBuilder().setDosage(p0.toString()).build()
                viewModel.addConfig(updatedConfig)
                rs2_step_navigation_action_add_more.visibility
                setupNextButtonEnabled()
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        rs2_step_navigation_action_add_more.setOnClickListener { addSchedule() }
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

            override fun onTimeSelectionPressed(schedule: Schedule, position: Int) {
                val cal = Calendar.getInstance()
                val zoneId : ZoneId = try {
                    ZoneId.systemDefault()
                } catch (e : Throwable) {
                    ZoneId.of("Z")
                }

                cal.time = DateTimeUtils.toDate(schedule.time.atDate(LocalDate.now()).toInstant(zoneId))
                val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hours, minutes ->
                    cal.set(Calendar.HOUR, hours)
                    cal.set(Calendar.MINUTE, minutes)
                    schedule.time = DateTimeUtils.toZonedDateTime(cal).toLocalTime()
                    val configUnwrapped = config ?: return@OnTimeSetListener
                    val schedules = configUnwrapped.schedules.toMutableList()
                    schedules[position] = schedule
                    adapter.updateSchedule(position, schedule)
                    val updatedConfig = configUnwrapped.toBuilder().setSchedules(schedules).build()
                    viewModel.addConfig(updatedConfig)
                }

                TimePickerDialog(context, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),
                        false)
                        .show()
            }

            override fun onDaySelectionPressed(schedule: Schedule, position: Int) {
                val formatter = DateTimeFormatter.ofPattern("h:mm a")
                LOGGER.debug("showDaySelection()")
                val dialog = MedicationDayFragment.newInstance(
                        schedule.id, identifier, formatter.format(schedule.time), schedule.days)
                dialog.listener = object : DaySelectedListener {
                    override fun onDaySelected(scheduleIdentifier: String, days: List<String>) {
                        val daysList = days.toMutableList()
                        daysList.removeAll { str -> str == "" }
                        if (daysList.size < 7 && !daysList.isEmpty()) {
                            schedule.everday = false
                            schedule.days = daysList
                        } else {
                            schedule.everday = true
                            schedule.days = arrayListOf()
                        }

                        schedule.days = daysList
                        val configUnwrapped = config ?: return
                        val index: Int = configUnwrapped.schedules.indexOf(schedule)
                        val schedules = configUnwrapped.schedules.toMutableList()
                        schedules[index] = schedule
                        val updatedConfig = configUnwrapped.toBuilder().setSchedules(schedules).build()
                        viewModel.addConfig(updatedConfig)
                        adapter.updateSchedule(position, schedule)
                    }
                }

                dialog.show(fragmentManager, "Day select")
            }

            override fun onAnytimeSet(schedule: Schedule, anytime: Boolean, position: Int) {
                val configUnwrapped = config ?: return
                val schedules = configUnwrapped.schedules.toMutableList()
                val iterator = schedules.iterator()
                while (iterator.hasNext()) {
                    val subSchedule = iterator.next()
                    if (subSchedule.id != subSchedule.id) {
                        iterator.remove()
                    }
                }

                schedule.anytime = anytime
                val updatedConfig = configUnwrapped.toBuilder().setSchedules(mutableListOf(schedule)).build()
                viewModel.addConfig(updatedConfig)
                // All of the schedules may have change so we refresh the adapter.
                Handler(Looper.getMainLooper()).post {
                    adapter.items = configUnwrapped.schedules
                    adapter.notifyDataSetChanged()
                }

                rs2_step_navigation_action_add_more.visibility = if (anytime) View.GONE else View.VISIBLE
            }
        }

        val schedules = config?.schedules?.toMutableList() ?: mutableListOf()
        return MedicationAdapter(schedules, listener)
    }

    override fun getLayoutId(): Int {
        return R.layout.mpower2_medication_scheduling
    }

    private fun addSchedule() {
        val configUnwrapped = config ?: return
        val schedules = configUnwrapped.schedules.toMutableList()
        val newSchedule = Schedule(
                schedules.size.toString())
        schedules.add(newSchedule)
        adapter.addSchedule(newSchedule)
        val updatedConfig = configUnwrapped.toBuilder().setSchedules(schedules).build()
        viewModel.addConfig(updatedConfig)
    }

    private fun setupNextButtonEnabled() {
        navigationActionBar.setForwardButtonEnabled(config?.isConfigured ?: false)
    }

    private fun onRemoveMedicationClicked(view: View) {
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