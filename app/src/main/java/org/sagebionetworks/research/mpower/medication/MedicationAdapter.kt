package org.sagebionetworks.research.mpower.medication

import android.app.TimePickerDialog
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.medication_add.view.*
import kotlinx.android.synthetic.main.medication_dosage.view.*
import kotlinx.android.synthetic.main.medication_schedule.view.*
import org.sagebionetworks.research.mpower.R.layout
import org.sagebionetworks.research.mpower.medication.Type.ADD
import org.sagebionetworks.research.mpower.medication.Type.DOSAGE
import org.sagebionetworks.research.mpower.medication.Type.SCHEDULE
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.Calendar

class MedicationAdapter(var name: String, var items : List<MedicationItem>, val context: Context, val listener: Listener)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val LOGGER = LoggerFactory.getLogger(MedicationAdapter::class.java)

    companion object {
        var DOSAGE_VIEW_TYPE = 0
        var SCHEDULE_VIEW_TYPE = 1
        var ADD_VIEW_TYPE = 2
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        lateinit var holder: RecyclerView.ViewHolder
        when(viewType) {
            DOSAGE_VIEW_TYPE ->
                holder = DosageViewHolder(
                        LayoutInflater.from(context).inflate(layout.medication_dosage, parent, false))
            SCHEDULE_VIEW_TYPE ->
                holder = ScheduleViewHolder(
                        LayoutInflater.from(context).inflate(layout.medication_schedule, parent, false))
            ADD_VIEW_TYPE ->
                holder = AddViewHolder(
                        LayoutInflater.from(context).inflate(layout.medication_add, parent, false))
        }
        return holder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items.get(position)
        when(item.type) {
            DOSAGE -> {
                var dh = holder as DosageViewHolder
                dh.input.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        var len = p0?.length
                        when(len) {
                            0 -> listener.enableNext(false)
                            else -> listener.enableNext(true)
                        }
                    }

                    override fun afterTextChanged(p0: Editable?) {}
                })
            }
            SCHEDULE -> {
                var sh = holder as ScheduleViewHolder
                var schedule = item as Schedule
                sh.checkbox.isChecked = schedule.anytime
                sh.checkbox.setOnCheckedChangeListener{ buttonView, isChecked ->
                    schedule.anytime = isChecked
                    listener.showAddSchedule(!isChecked)
                    listener.setAnytime(schedule,isChecked)
                }

                val visibility =  if (schedule.anytime) View.GONE else View.VISIBLE
                sh.dayContainer.visibility = visibility
                sh.timeContainer.visibility = visibility

                sh.timeText.text = schedule.time
                sh.timeContainer.setOnClickListener {
                    view ->
                        LOGGER.debug("Time clicked")
                        getDate(context, sh.timeText, schedule)
                }
                sh.dayContainer.setOnClickListener {
                    view ->
                        LOGGER.debug("Day container clicked")
                        listener.showDaySelection(name, schedule)
                }
                if(schedule.everday) {
                    sh.dayText.text = "Everyday"
                } else {
                    sh.dayText.text = schedule.days.joinToString("," )
                }
            }

            ADD -> {
                var ah = holder as AddViewHolder
                ah.button.setOnClickListener {
                    view ->
                        LOGGER.debug("Add button clicked.")
                        listener.addSchedule()
                }
            }

        }

    }

    override fun getItemViewType(position: Int): Int {

        val type = when (items[position].type) {
            DOSAGE -> DOSAGE_VIEW_TYPE
            SCHEDULE -> SCHEDULE_VIEW_TYPE
            ADD -> ADD_VIEW_TYPE
        }
        return type
    }

    fun getDate(context: Context, textView: TextView, schedule: Schedule) {

        var sdf = SimpleDateFormat("hh:mm aa")
        val cal = Calendar.getInstance()
        cal.setTime(sdf.parse(schedule.time))

        val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hours, minutes ->
            cal.set(Calendar.HOUR, hours)
            cal.set(Calendar.MINUTE, minutes)

            var format = sdf.format(cal.time)
            textView.text = format
            schedule.time = format

        }

        TimePickerDialog(context, timeSetListener,
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE), false).show()
    }

}

class DosageViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    val input = view.dosage_input
}

class ScheduleViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    val checkbox = view.schedule_anytime
    val timeText = view.time_text
    val timeContainer = view.time_container
    val dayContainer = view.day_container
    val dayText = view.day_text
}

class AddViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    val button = view.schedule_add
}

interface Listener {
    fun addSchedule()
    fun enableNext(enable: Boolean)
    fun showAddSchedule(show: Boolean)
    fun showDaySelection(name: String, schedule: Schedule)
    fun setAnytime(schedule: Schedule, anytime: Boolean)
}