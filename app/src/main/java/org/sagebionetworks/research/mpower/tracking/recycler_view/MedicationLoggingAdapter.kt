package org.sagebionetworks.research.mpower.tracking.recycler_view

import android.os.Handler
import android.os.Looper
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.mpower2_medication_review_widget_add_details.view.*
import kotlinx.android.synthetic.main.mpower2_medication_schedule_view_holder.view.*
import kotlinx.android.synthetic.main.mpower2_medication_schedule_view_holder.view.time_button
import kotlinx.android.synthetic.main.mpower2_symptoms_logging_item.view.item_title
import org.sagebionetworks.research.mobile_ui.widget.ActionButton
import org.sagebionetworks.research.mpower.R
import org.sagebionetworks.research.mpower.tracking.fragment.MedicationDayFragment
import org.sagebionetworks.research.mpower.tracking.recycler_view.MedicationLoggingItem.TYPE.SCHEDULE
import org.sagebionetworks.research.mpower.tracking.recycler_view.MedicationLoggingItem.TYPE.TITLE
import org.sagebionetworks.research.mpower.tracking.recycler_view.MedicationLoggingItem.TYPE.ADD_DETAILS
import org.sagebionetworks.research.mpower.tracking.view_model.logs.DosageItem
import org.sagebionetworks.research.mpower.tracking.view_model.logs.MedicationLog
import org.sagebionetworks.research.mpower.tracking.view_model.logs.MedicationTimestamp
import org.sagebionetworks.research.mpower.tracking.widget.UnderlinedButton
import org.sagebionetworks.research.sageresearch.extensions.localizedAndJoin
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

class MedicationLoggingAdapter(private val items: MutableList<MedicationLoggingItem>,
        val logs: Map<String, MedicationLog>,
        private val listener: MedicationLoggingListener) : RecyclerView.Adapter<ViewHolder>() {

    companion object {
        const val TYPE_TITLE = 0
        const val TYPE_SCHEDULE = 1
        const val TYPE_ADD_DETAILS = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_TITLE -> MedicationTitleViewHolder(
                    inflater.inflate(R.layout.mpower2_medication_title_view_holder, parent, false))
            TYPE_ADD_DETAILS ->
                MedicationLoggingAddDetailsViewHolder(
                        inflater.inflate(R.layout.mpower2_medication_review_widget_add_details, parent, false))
            else -> MedicationScheduleViewHolder(
                    inflater.inflate(R.layout.mpower2_medication_schedule_view_holder, parent, false))
        }
    }

    fun updateItem(item: MedicationLoggingItem, position: Int) {
        items[position] = item
        Handler(Looper.getMainLooper()).post {
            notifyItemChanged(position)
        }
    }

    fun addItem(item: MedicationLoggingItem, position: Int) {
        items.add(position, item)
        Handler(Looper.getMainLooper()).post {
            notifyItemInserted(position)
        }
    }

    fun removeItem(item: MedicationLoggingItem) {
        var position = items.indexOf(item)
        if (items.remove(item)) {
            Handler(Looper.getMainLooper()).post {
                notifyItemRemoved(position)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position].type) {
            TITLE -> TYPE_TITLE
            SCHEDULE -> TYPE_SCHEDULE
            ADD_DETAILS -> TYPE_ADD_DETAILS
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

                val formatter  =
                        DateTimeFormatter.ofPattern("h:mm a")
                                .withLocale(Locale.getDefault())
                                .withZone(ZoneId.systemDefault())

                // Setup the labels for the not logged view
                if (scheduleItem.dosageItem.isAnytime) {
                    holder.notLoggedDaysLabel.setText(R.string.medication_schedule_anytime)
                    holder.notLoggedTimeLabel.setText(holder.itemView.resources.getString(R.string.medication_logging_take_at_label))
                } else {
                    holder.notLoggedTimeLabel.visibility = View.VISIBLE
                    scheduleItem.medicationTimestamp?.localTimeOfDay?.let {
                        val timeText =  holder.itemView.resources.getString(R.string.medication_logging_take_at_label) +
                                " " + formatter.format(it)
                        holder.notLoggedTimeLabel.text = timeText
                    }

                    val daysText = when {
                        scheduleItem.dosageItem.isDaily -> holder.itemView.resources.getString(R.string.medication_schedule_everyday)
                        else -> MedicationDayFragment.getDayStringSet(
                                scheduleHolder.notLoggedDaysLabel.resources, scheduleItem.dosageItem.daysOfWeek)
                                .localizedAndJoin(scheduleHolder.notLoggedDaysLabel.context)
                    }
                    scheduleHolder.notLoggedDaysLabel.text = daysText
                }

                // Setup the labels and buttons for the view when logged
                scheduleItem.medicationTimestamp?.loggedDate?.let {
                    val timeText = formatter.format(it) +
                            holder.itemView.resources.getString(R.string.medication_logging_time_button_suffix)
                    scheduleHolder.timeButton.text = timeText
                }

                val loggedViewVisibility = if (scheduleItem.isLogged) View.VISIBLE else View.GONE
                val notLoggedViewVisibility = if (scheduleItem.isLogged) View.GONE else View.VISIBLE

                scheduleHolder.loggedContainer.visibility = loggedViewVisibility
                scheduleHolder.loggedButtonContainer.visibility = loggedViewVisibility
                scheduleHolder.notLoggedContainer.visibility = notLoggedViewVisibility
                scheduleHolder.notLoggedButtonContainer.visibility = notLoggedViewVisibility

                // add click- listeners to the buttons
                scheduleHolder.takenButton.setOnClickListener { _ ->
                    val curPosition = items.indexOf(scheduleItem)
                    listener.onTakenPressed(scheduleItem.config.identifier, scheduleItem, curPosition)
                }
                scheduleHolder.undoButton.setOnClickListener { _ ->
                    val curPosition = items.indexOf(scheduleItem)
                    listener.onUndoPressed(scheduleItem.config.identifier, scheduleItem, curPosition)
                }
                scheduleHolder.timeButton.setOnClickListener { _ ->
                    scheduleItem.medicationTimestamp?.loggedDate?.let {
                        val curPosition = items.indexOf(scheduleItem)
                        listener.onTimePressed(it, scheduleItem.config.identifier, scheduleItem, curPosition)
                    }
                }
            }

            ADD_DETAILS -> {
                val addDetails = items[position] as MedicationLoggingAddDetails
                val viewHolder = holder as MedicationLoggingAddDetailsViewHolder
                viewHolder.title.setText(addDetails.config.identifier)
                viewHolder.addDetails.setOnClickListener { _ ->
                    listener.onAddDetailsPressed(addDetails.config)
                }

            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}

abstract class MedicationLoggingItem {
    enum class TYPE {
        TITLE, SCHEDULE, ADD_DETAILS
    }

    abstract val type: TYPE
}

class MedicationLoggingTitle(val title: String) : MedicationLoggingItem() {
    override val type: TYPE = TITLE
}

class MedicationLoggingAddDetails(val config: MedicationLog) : MedicationLoggingItem() {
    override val type: TYPE = ADD_DETAILS
}

class MedicationLoggingSchedule(val config: MedicationLog, val dosageItem: DosageItem, val medicationTimestamp: MedicationTimestamp?)
    : MedicationLoggingItem() {
    override val type: TYPE = SCHEDULE
    val isLogged: Boolean = medicationTimestamp != null && medicationTimestamp.loggedDate != null
}

class MedicationTitleViewHolder(view: View) : ViewHolder(view) {
    val title: TextView = view.item_title
}

class MedicationLoggingAddDetailsViewHolder(view: View) : ViewHolder(view) {
    val title: TextView = view.item_title
    val addDetails = view.edit_button
}

class MedicationScheduleViewHolder(view: View) : ViewHolder(view) {
    val takenButton: ActionButton = view.taken_button
    val undoButton: UnderlinedButton = view.undo_button
    val timeButton: UnderlinedButton = view.time_button

    val notLoggedTimeLabel: TextView = view.medication_schedule_time_label
    val notLoggedDaysLabel: TextView = view.medication_schedule_days_label

    val loggedContainer: View = view.logged_date_container
    val notLoggedContainer: View = view.not_logged_date_container
    val loggedButtonContainer: View = view.logged_date_container_button_container
    val notLoggedButtonContainer: View = view.not_logged_date_container_button_container
}

interface MedicationLoggingListener {
    fun onTakenPressed(medicationIdentifier: String, scheduleItem: MedicationLoggingSchedule, position: Int)
    fun onUndoPressed(medicationIdentifier: String, scheduleItem: MedicationLoggingSchedule, position: Int)
    fun onTimePressed(currentLoggedDate: Instant, medicationIdentifier: String,
            scheduleItem: MedicationLoggingSchedule, position: Int)
    fun onAddDetailsPressed(medicationLog: MedicationLog)
}