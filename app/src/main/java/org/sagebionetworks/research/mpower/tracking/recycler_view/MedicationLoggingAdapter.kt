package org.sagebionetworks.research.mpower.tracking.recycler_view

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.mpower2_medication_review_widget.view.days_label
import kotlinx.android.synthetic.main.mpower2_symptoms_logging_item.view.item_title
import kotlinx.android.synthetic.main.mpower2_symptoms_logging_item.view.time_button
import kotlinx.android.synthetic.main.mpower2_triggers_logging_item.view.checkmark
import kotlinx.android.synthetic.main.mpower2_triggers_logging_item.view.undo_button
import org.sagebionetworks.research.mobile_ui.widget.ActionButton
import org.sagebionetworks.research.mpower.R
import org.sagebionetworks.research.mpower.tracking.recycler_view.MedicationLoggingItem.TYPE.SCHEDULE
import org.sagebionetworks.research.mpower.tracking.recycler_view.MedicationLoggingItem.TYPE.TITLE
import org.sagebionetworks.research.mpower.tracking.view_model.configs.MedicationConfig
import org.sagebionetworks.research.mpower.tracking.view_model.configs.Schedule
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SimpleTrackingItemLog
import org.sagebionetworks.research.mpower.tracking.widget.UnderlinedButton
import org.threeten.bp.format.DateTimeFormatter

class MedicationLoggingAdapter(private val items: List<MedicationLoggingItem>,
        val logs: Map<String, SimpleTrackingItemLog>,
        private val listener: MedicationLoggingListener) : RecyclerView.Adapter<ViewHolder>() {

    companion object {
        const val TYPE_TITLE = 0
        const val TYPE_SCHEDULE = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_TITLE -> MedicationTitleViewHolder(
                    inflater.inflate(R.layout.mpower2_medication_title_view_holder, parent, false))
            else -> MedicationScheduleViewHolder(
                    inflater.inflate(R.layout.mpower2_medication_schedule_view_holder, parent, false))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position].type) {
            TITLE -> TYPE_TITLE
            SCHEDULE -> TYPE_SCHEDULE
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (items[position].type) {
            TITLE -> {
                val title = items[position] as MedicationLoggingTitle
                val titleHolder = holder as MedicationTitleViewHolder
                // update the view to contain the correct information.
                titleHolder.title.text = title.title
            }

            SCHEDULE -> {
                val scheduleItem = items[position] as MedicationLoggingSchedule
                val scheduleHolder = holder as MedicationScheduleViewHolder
                val log = logs[scheduleItem.config.identifier]
                val isLogged = when (log) {
                    null -> false
                // TODO rkolmos 09/19/2018 make isLogged correct
                    else -> true
                }
                // update the view to contain the correct information.
                val loggedViewVisibility = if (isLogged) View.VISIBLE else View.GONE
                val unloggedViewVisibility = if (isLogged) View.GONE else View.VISIBLE
                scheduleHolder.checkmark.visibility = loggedViewVisibility
                scheduleHolder.takenAtLabel.visibility = loggedViewVisibility
                scheduleHolder.undoButton.visibility = loggedViewVisibility
                scheduleHolder.daysLabel.visibility = unloggedViewVisibility
                scheduleHolder.takenButton.visibility = unloggedViewVisibility
                val formatter = DateTimeFormatter.ofPattern("h:mm a")
                val timeText = formatter.format(scheduleItem.schedule.time) +
                        holder.itemView.resources.getString(R.string.medication_logging_time_button_suffix)
                scheduleHolder.timeButton.text = timeText
                val daysText = when {
                    scheduleItem.schedule.everday -> holder.itemView.resources.getString(R.string.medication_schedule_everyday)
                    else -> scheduleItem.schedule.days.joinToString(",")
                }

                scheduleHolder.daysLabel.text = daysText
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}

abstract class MedicationLoggingItem {
    enum class TYPE {
        TITLE, SCHEDULE
    }

    abstract val type: TYPE
}

class MedicationLoggingTitle(val title: String) : MedicationLoggingItem() {
    override val type: TYPE = TITLE
}

class MedicationLoggingSchedule(val config: MedicationConfig, val schedule: Schedule) : MedicationLoggingItem() {
    override val type: TYPE = SCHEDULE
}

class MedicationTitleViewHolder(view: View) : ViewHolder(view) {
    val title: TextView = view.item_title
}

class MedicationScheduleViewHolder(view: View) : ViewHolder(view) {
    val checkmark: ImageView = view.checkmark
    val takenButton: ActionButton = view.taken_button
    val undoButton: UnderlinedButton = view.undo_button
    val timeButton: UnderlinedButton = view.time_button
    val takenAtLabel: TextView = view.taken_at_label
    val daysLabel: TextView = view.days_label
}

interface MedicationLoggingListener {
    fun onTakenPressed(itemIdentifier: String, schedule: Schedule, position: Int)
    fun onUndoPressed(itemIdentifier: String, schedule: Schedule, position: Int)
}