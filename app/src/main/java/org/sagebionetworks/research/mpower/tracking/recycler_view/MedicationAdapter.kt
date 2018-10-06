package org.sagebionetworks.research.mpower.tracking.recycler_view

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.medication_schedule.view.day_container
import kotlinx.android.synthetic.main.medication_schedule.view.day_text
import kotlinx.android.synthetic.main.medication_schedule.view.schedule_anytime
import kotlinx.android.synthetic.main.medication_schedule.view.time_container
import kotlinx.android.synthetic.main.medication_schedule.view.time_text
import org.sagebionetworks.research.mpower.R
import org.sagebionetworks.research.mpower.tracking.SortUtil
import org.sagebionetworks.research.mpower.tracking.view_model.configs.Schedule
import org.slf4j.LoggerFactory
import org.threeten.bp.format.DateTimeFormatter

class MedicationAdapter(var items: MutableList<Schedule>, val listener: Listener) : androidx.recyclerview.widget.RecyclerView.Adapter<ScheduleViewHolder>() {

    private val LOGGER = LoggerFactory.getLogger(
            MedicationAdapter::class.java)

    fun addSchedule(schedule: Schedule) {
        Handler(Looper.getMainLooper()).post {
            if (!items.contains(schedule)) {
                items.add(items.size, schedule)
                notifyDataSetChanged()
            }
        }
    }

    fun updateSchedule(position: Int, schedule: Schedule) {
        items[position] = schedule
        notifyItemChanged(position)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        return ScheduleViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.medication_schedule, parent, false))
    }

    override fun onBindViewHolder(scheduleViewHolder: ScheduleViewHolder, position: Int) {
        val schedule = items[position]
        scheduleViewHolder.checkbox.isChecked = schedule.anytime
        scheduleViewHolder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            listener.onAnytimeSet(schedule, isChecked, position)
        }

        val visibility = if (schedule.anytime) View.GONE else View.VISIBLE
        scheduleViewHolder.dayContainer.visibility = visibility
        scheduleViewHolder.timeContainer.visibility = visibility

        val formatter = DateTimeFormatter.ofPattern("h:mm a")
        scheduleViewHolder.timeText.text = formatter.format(schedule.time)
        scheduleViewHolder.timeContainer.setOnClickListener { _ ->
            LOGGER.debug("Time clicked")
            listener.onTimeSelectionPressed(schedule, position)
        }

        scheduleViewHolder.dayContainer.setOnClickListener { _ ->
            LOGGER.debug("Day container clicked")
            listener.onDaySelectionPressed(schedule, position)
        }

        if (schedule.everday) {
            scheduleViewHolder.dayText.setText(R.string.medication_schedule_everyday)
        } else {
            val days = SortUtil.sortDaysList(schedule.days, scheduleViewHolder.checkbox.context)
            scheduleViewHolder.dayText.text = days.joinToString(",")
        }
    }
}

class ScheduleViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
    val checkbox = view.schedule_anytime!!
    val timeText = view.time_text!!
    val timeContainer = view.time_container!!
    val dayContainer = view.day_container!!
    val dayText = view.day_text!!
}

interface Listener {
    fun onTimeSelectionPressed(schedule: Schedule, position: Int)
    fun onDaySelectionPressed(schedule: Schedule, position: Int)
    fun onAnytimeSet(schedule: Schedule, anytime: Boolean, position: Int)
}