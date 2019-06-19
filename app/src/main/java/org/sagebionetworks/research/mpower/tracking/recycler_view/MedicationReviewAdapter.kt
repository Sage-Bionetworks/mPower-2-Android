package org.sagebionetworks.research.mpower.tracking.recycler_view

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.sagebionetworks.research.mpower.R
import org.sagebionetworks.research.mpower.tracking.view_model.logs.MedicationLog
import org.sagebionetworks.research.mpower.tracking.widget.MedicationReviewWidget

class MedicationReviewAdapter(val configs: List<MedicationLog>, private val listener: MedicationReviewListener)
    : RecyclerView.Adapter<MedicationReviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationReviewViewHolder {
        val widget: MedicationReviewWidget = LayoutInflater.from(parent.context)
                .inflate(R.layout.mpower2_medication_review_view_holder, parent, false) as MedicationReviewWidget
        return MedicationReviewViewHolder(widget, listener)
    }

    override fun onBindViewHolder(holder: MedicationReviewViewHolder, position: Int) {
        val config = configs[position]
        holder.setContent(config, position)
    }

    override fun getItemCount(): Int {
        return configs.size
    }
}

class MedicationReviewViewHolder(val widget: MedicationReviewWidget, private val listener: MedicationReviewListener)
    : RecyclerView.ViewHolder(widget) {

    fun setContent(config: MedicationLog, position: Int) {
        val title = config.identifier
        widget.title.text = title
        if (config.dosageItems.isEmpty()) {
            widget.editButton.text = widget.context.getString(R.string.button_add_details)
        } else {
            widget.daysLabel.visibility = View.GONE
            widget.timeLabel.text = widget.context.resources.getQuantityString(R.plurals.number_of_doses, config.dosageItems.size, config.dosageItems.size)
            widget.editButton.text = widget.context.getString(R.string.button_edit)
        }

//        if (config.schedules[0].isAnytime()) {
//            widget.timeLabel.setText(R.string.medication_schedule_anytime)
//            widget.daysLabel.visibility = View.GONE
//        } else {
//            val days: MutableSet<Int> = mutableSetOf()
//            for (schedule in config.schedules) {
//                days.addAll(schedule.daysOfWeek)
//            }
//
//            val dayString = if (days.isEmpty()) {
//                widget.context.resources.getString(R.string.medication_schedule_everyday)
//            } else {
//                MedicationDayFragment.getDayStringSet(widget.resources, days)
//                        .localizedAndJoin(widget.context)
//            }
//
//            widget.daysLabel.visibility = View.VISIBLE
//            widget.daysLabel.text = dayString
//            val times: MutableList<LocalTime> = mutableListOf()
//            for (schedule in config.schedules) {
//                schedule.getLocalTimeOfDay()?.let {
//                    times.add(it)
//                }
//            }
//
//            val sortedTimes = SortUtil.sortTimesList(times)
//            val formatter = DateTimeFormatter.ofPattern("h:mm a")
//            val timesStrings: MutableList<String> = mutableListOf()
//            for (time in sortedTimes) {
//                timesStrings.add(formatter.format(time))
//            }
//
//            widget.timeLabel.text = timesStrings.localizedAndJoin(widget.context)
//        }
        widget.editButton.setOnClickListener { _ -> listener.editButtonPressed(config, position) }
        widget.root.setOnClickListener { _ -> listener.editButtonPressed(config, position) }
    }
}

interface MedicationReviewListener {
    fun editButtonPressed(config: MedicationLog, position: Int)
}