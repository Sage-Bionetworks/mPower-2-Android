package org.sagebionetworks.research.mpower.tracking.recycler_view

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.sagebionetworks.research.mobile_ui.extensions.localizedAndJoin
import org.sagebionetworks.research.mpower.R
import org.sagebionetworks.research.mpower.tracking.SortUtil
import org.sagebionetworks.research.mpower.tracking.view_model.configs.MedicationConfig
import org.sagebionetworks.research.mpower.tracking.widget.MedicationReviewWidget
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter

class MedicationReviewAdapter(val configs: List<MedicationConfig>, private val listener: MedicationReviewListener)
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

    fun setContent(config: MedicationConfig, position: Int) {
        var title = config.identifier + " " + config.dosage
        widget.title.text = title
        if (config.schedules[0].anytime) {
            widget.timeLabel.setText(R.string.medication_schedule_anytime)
            widget.daysLabel.visibility = View.GONE
        } else {
            val days: MutableList<String> = mutableListOf()
            for (schedule in config.schedules) {
                days.addAll(schedule.days)
            }

            val dayString = if (days.isEmpty()) {
                widget.context.resources.getString(R.string.medication_schedule_everyday)
            } else {
                val sortedDays = SortUtil.sortDaysList(days, widget.context)
                sortedDays.localizedAndJoin(widget.context)
            }

            widget.daysLabel.visibility = View.VISIBLE
            widget.daysLabel.text = dayString
            val times: MutableList<LocalTime> = mutableListOf()
            for (schedule in config.schedules) {
                times.add(schedule.time)
            }

            val sortedTimes = SortUtil.sortTimesList(times)
            val formatter = DateTimeFormatter.ofPattern("h:mm a")
            val timesStrings: MutableList<String> = mutableListOf()
            for (time in sortedTimes) {
                timesStrings.add(formatter.format(time))
            }

            widget.timeLabel.text = timesStrings.localizedAndJoin(widget.context)
            widget.editButton.setOnClickListener { _ -> listener.editButtonPressed(config, position) }
        }
    }
}

interface MedicationReviewListener {
    fun editButtonPressed(config: MedicationConfig, position: Int)
}