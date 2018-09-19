package org.sagebionetworks.research.mpower.tracking.recycler_view

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import org.sagebionetworks.research.mpower.R
import org.sagebionetworks.research.mpower.tracking.SortUtil
import org.sagebionetworks.research.mpower.tracking.view_model.configs.MedicationConfig
import org.sagebionetworks.research.mpower.tracking.widget.MedicationReviewWidget
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter

class MedicationReviewAdapter(val configs : List<MedicationConfig>, private val listener : MedicationReviewListener)
    : RecyclerView.Adapter<MedicationReviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationReviewViewHolder {
        val widget : MedicationReviewWidget = LayoutInflater.from(parent.context)
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
        widget.title.text = config.identifier
        val times: MutableList<LocalTime> = mutableListOf()
        val days: MutableList<String> = mutableListOf()
        for (schedule in config.schedules) {
            times.add(schedule.time)
            days.addAll(schedule.days)
        }

        val sortedDays = SortUtil.sortDaysList(days, widget.context)
        val sortedTimes = SortUtil.sortTimesList(times)
        val formatter = DateTimeFormatter.ofPattern("h:mm a")
        val timesStrings: MutableList<String> = mutableListOf()
        for (time in sortedTimes) {
            timesStrings.add(formatter.format(time))
        }

        widget.timeLabel.text = timesStrings.joinToString(",")
        widget.daysLabel.text = sortedDays.joinToString(",")
        widget.editButton.setOnClickListener { _ -> listener.editButtonPressed(config, position) }
    }
}

interface MedicationReviewListener {
    fun editButtonPressed(config: MedicationConfig, position: Int)
}