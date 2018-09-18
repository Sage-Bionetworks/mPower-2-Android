package org.sagebionetworks.research.mpower.tracking.recycler_view

import android.app.TimePickerDialog
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.medication_add.view.schedule_add
import kotlinx.android.synthetic.main.medication_dosage.view.dosage_input
import kotlinx.android.synthetic.main.medication_schedule.view.day_container
import kotlinx.android.synthetic.main.medication_schedule.view.day_text
import kotlinx.android.synthetic.main.medication_schedule.view.schedule_anytime
import kotlinx.android.synthetic.main.medication_schedule.view.time_container
import kotlinx.android.synthetic.main.medication_schedule.view.time_text
import org.sagebionetworks.research.mpower.R
import org.sagebionetworks.research.mpower.tracking.recycler_view.Type.ADD
import org.sagebionetworks.research.mpower.tracking.recycler_view.Type.DOSAGE
import org.sagebionetworks.research.mpower.tracking.recycler_view.Type.SCHEDULE
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.Calendar

class MedicationAdapter(var items : List<MedicationItem>, val listener: Listener)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val LOGGER = LoggerFactory.getLogger(
            MedicationAdapter::class.java)

    companion object {
        var DOSAGE_VIEW_TYPE = 0
        var SCHEDULE_VIEW_TYPE = 1
        var ADD_VIEW_TYPE = 2
    }

    fun setDosage(dosage: String?) {
        val dosageItem : Dosage? = items[0] as? Dosage
        if (dosageItem == null) {
            LOGGER.warn("Dosage is not the first item in the adapters items list")
        }

        (items as MutableList)[0] = Dosage(dosage)
        notifyItemChanged(0)
    }

    fun addSchedule(schedule: Schedule) {
        if (!items.contains(schedule)) {
            (items as MutableList).add(items.size - 1, schedule)
            notifyItemChanged(items.size - 2)
            notifyItemChanged(items.size - 1)
        }
    }

    fun updateSchedule(position: Int, schedule: Schedule) {
        (items as MutableList)[position] = schedule
        notifyItemChanged(position)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        lateinit var holder: RecyclerView.ViewHolder
        when(viewType) {
            DOSAGE_VIEW_TYPE ->
                holder = DosageViewHolder(
                        LayoutInflater.from(parent.context).inflate(R.layout.medication_dosage, parent, false))
            SCHEDULE_VIEW_TYPE ->
                holder = ScheduleViewHolder(
                        LayoutInflater.from(parent.context).inflate(R.layout.medication_schedule, parent, false))
            ADD_VIEW_TYPE ->
                holder = AddViewHolder(
                        LayoutInflater.from(parent.context).inflate(R.layout.medication_add, parent, false))
        }

        return holder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when(item.type) {
            DOSAGE -> {
                val dosageViewHolder = holder as DosageViewHolder
                dosageViewHolder.input.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        listener.onDosageChanged(p0 as String, position)
                    }

                    override fun afterTextChanged(p0: Editable?) {}
                })
            }

            SCHEDULE -> {
                val scheduleViewHolder = holder as ScheduleViewHolder
                val schedule = item as Schedule
                scheduleViewHolder.checkbox.isChecked = schedule.anytime
                scheduleViewHolder.checkbox.setOnCheckedChangeListener{ _, isChecked ->
                    listener.onAnytimeSet(schedule, isChecked, position)
                }

                val visibility =  if (schedule.anytime) View.GONE else View.VISIBLE
                scheduleViewHolder.dayContainer.visibility = visibility
                scheduleViewHolder.timeContainer.visibility = visibility

                scheduleViewHolder.timeText.text = schedule.time
                scheduleViewHolder.timeContainer.setOnClickListener {
                    _ ->
                        LOGGER.debug("Time clicked")
                        listener.onTimeSelectionPressed(schedule, position)
                }

                scheduleViewHolder.dayContainer.setOnClickListener {
                    _ ->
                        LOGGER.debug("Day container clicked")
                        listener.onDaySelectionPressed(schedule, position)
                }

                if(schedule.everday) {
                    scheduleViewHolder.dayText.setText(R.string.medication_schedule_everyday)
                } else {
                    scheduleViewHolder.dayText.text = schedule.days.joinToString("," )
                }
            }

            ADD -> {
                val addViewHolder = holder as AddViewHolder
                addViewHolder.button.setOnClickListener {
                    _ ->
                        LOGGER.debug("Add button clicked.")
                        listener.onAddSchedulePressed()
                }
            }

        }

    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position].type) {
            DOSAGE -> DOSAGE_VIEW_TYPE
            SCHEDULE -> SCHEDULE_VIEW_TYPE
            ADD -> ADD_VIEW_TYPE
        }
    }

    fun getDate(context: Context, textView: TextView, schedule: Schedule) {

        val sdf = SimpleDateFormat("hh:mm aa")
        val cal = Calendar.getInstance()
        cal.time = sdf.parse(schedule.time)

        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hours, minutes ->
            cal.set(Calendar.HOUR, hours)
            cal.set(Calendar.MINUTE, minutes)

            val format = sdf.format(cal.time)
            textView.text = format
            schedule.time = format

        }

        TimePickerDialog(context, timeSetListener,
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE), false).show()
    }
}

class DosageViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    val input = view.dosage_input!!
}

class ScheduleViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    val checkbox = view.schedule_anytime!!
    val timeText = view.time_text!!
    val timeContainer = view.time_container!!
    val dayContainer = view.day_container!!
    val dayText = view.day_text!!
}

class AddViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    val button = view.schedule_add!!
}

interface Listener {
    fun onAddSchedulePressed()
    fun onDosageChanged(dosage: String?, position : Int)
    fun onTimeSelectionPressed(schedule: Schedule, position : Int)
    fun onDaySelectionPressed(schedule: Schedule, position : Int)
    fun onAnytimeSet(schedule: Schedule, anytime: Boolean, position : Int)
}