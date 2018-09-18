package org.sagebionetworks.research.mpower.tracking.fragment


import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.slf4j.LoggerFactory

import org.sagebionetworks.research.mpower.R
import org.sagebionetworks.research.mpower.tracking.recycler_view.Dosage
import org.sagebionetworks.research.mpower.tracking.recycler_view.Listener
import org.sagebionetworks.research.mpower.tracking.recycler_view.MedicationAdapter
import org.sagebionetworks.research.mpower.tracking.recycler_view.MedicationItem
import org.sagebionetworks.research.mpower.tracking.recycler_view.Schedule
import org.sagebionetworks.research.mpower.tracking.view_model.MedicationTrackingTaskViewModel
import org.sagebionetworks.research.mpower.tracking.view_model.configs.MedicationConfig
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SimpleTrackingItemLog
import org.sagebionetworks.research.presentation.model.interfaces.StepView

class MedicationSchedulingFragment :
        RecyclerViewTrackingFragment<MedicationConfig, SimpleTrackingItemLog,
                MedicationTrackingTaskViewModel, MedicationAdapter>() {
    private val LOGGER = LoggerFactory.getLogger(MedicationSchedulingFragment::class.java)
    private lateinit var identifier : String

    companion object {
        private val ARG_IDENTIFIER = "identifier"

        fun newInstance(step: StepView, identifier: String) : MedicationSchedulingFragment {
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
            identifier = savedInstanceState.getString(ARG_IDENTIFIER)
        } else if (arguments != null) {
            identifier = (arguments as Bundle).getString(ARG_IDENTIFIER)
        } else {
            LOGGER.warn("MedicationSchedulingFragment created without an identifier argument")
            return
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val View = super.onCreateView(inflater, container, savedInstanceState)
        title.text = identifier
        val str = getString(R.string.remove_medication)
        val content = SpannableString(getString(R.string.remove_medication))
        content.setSpan(UnderlineSpan(), 0, str.length, 0)
        detail.text = content
        return View
    }

    override fun initializeAdapter(): MedicationAdapter {
        val listener : Listener = object : Listener {
            override fun onAddSchedulePressed() {
                viewModel.addSchedule(identifier)
                val schedules = viewModel.activeElementsById.value!![identifier]?.schedules
                val newSchedule : Schedule? = schedules?.get(schedules.size - 1)
                if (newSchedule != null) {
                    adapter.addSchedule(newSchedule)
                }
            }

            override fun onDosageChanged(dosage: String?, position: Int) {
                var config = viewModel.activeElementsById.value!![identifier]!!
                config = config.toBuilder().setDosage(dosage).build()
                viewModel.addConfig(config)
                adapter.setDosage(config.dosage)
            }

            override fun onTimeSelectionPressed(schedule: Schedule, position: Int) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDaySelectionPressed(schedule: Schedule, position: Int) {
                LOGGER.debug("showDaySelection()")
                val days = schedule.days.joinToString(",")
                val dialog = MedicationDayFragment.newInstance(schedule.id, identifier, schedule.time, days)
                dialog.listener = object : DaySelectedListener {
                    override fun onDaySelected(scheduleIdentifier: String, days: String) {
                        val daysList = days.split(",")
                        viewModel.setScheduleDays(identifier, scheduleIdentifier, daysList)
                        schedule.days = daysList
                        adapter.updateSchedule(position, schedule)
                    }
                }

                dialog.show(fragmentManager, "Day select")
            }

            override fun onAnytimeSet(schedule: Schedule, anytime: Boolean, position: Int) {
                viewModel.deleteOtherSchedules(identifier, schedule)
                var config = viewModel.activeElementsById.value!![identifier]!!
                schedule.anytime = anytime
                config = config.toBuilder().setSchedules(mutableListOf(schedule)).build()
                viewModel.addConfig(config)
                // All of the schedules may have change so we refresh the adapter.
                adapter.items = getItems()
                adapter.notifyDataSetChanged()
            }
        }


        return MedicationAdapter(getItems(), listener)
    }

    private fun getItems() : MutableList<MedicationItem> {
        val items : MutableList<MedicationItem> = mutableListOf()
        val config = viewModel.activeElementsById.value!![identifier]
        if (config != null) {
            items.add(Dosage(config.dosage))
            items.addAll(config.schedules)
        }

        return items
    }

    override fun getLayoutId(): Int {
        return R.layout.mpower2_medication_scheduling
    }
}