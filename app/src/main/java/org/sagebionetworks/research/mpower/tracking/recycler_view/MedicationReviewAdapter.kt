package org.sagebionetworks.research.mpower.tracking.recycler_view

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.mpower2_medication_review_widget.view.*
import org.sagebionetworks.research.mpower.R
import org.sagebionetworks.research.mpower.tracking.view_model.logs.MedicationLog

class MedicationReviewAdapter(val configs: List<MedicationLog>, private val listener: MedicationReviewListener)
    : androidx.recyclerview.widget.RecyclerView.Adapter<MedicationReviewViewHolder>() {

    companion object {
        const val TYPE_ADD = 0
        const val TYPE_EDIT = 1
    }

    override fun onBindViewHolder(holder: MedicationReviewViewHolder, position: Int) {
        val config = configs[position]
        holder.setContent(config, position)
    }

    override fun getItemCount(): Int {
        return configs.size
    }

    override fun getItemViewType(position: Int): Int {
        if (configs[position].dosageItems.isEmpty()) {
            return TYPE_ADD
        } else {
            return TYPE_EDIT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationReviewViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_ADD -> MedicationReviewViewHolder(
                    inflater.inflate(R.layout.mpower2_medication_review_widget_add_details, parent, false), listener)
            else -> MedicationReviewViewHolder(
                    inflater.inflate(R.layout.mpower2_medication_review_widget, parent, false), listener)
        }
    }


}

class MedicationReviewViewHolder(val widget: View, private val listener: MedicationReviewListener)
    : androidx.recyclerview.widget.RecyclerView.ViewHolder(widget) {

    fun setContent(config: MedicationLog, position: Int) {
        val title = config.identifier
        widget.item_title.text = title
        if (!config.dosageItems.isEmpty()) {
            widget.findViewById<TextView>(R.id.time_label).text = widget.context.resources.getQuantityString(R.plurals.number_of_doses, config.dosageItems.size, config.dosageItems.size)
        }

        widget.findViewById<TextView>(R.id.edit_button).setOnClickListener { _ -> listener.editButtonPressed(config, position) }
        widget.setOnClickListener { _ -> listener.editButtonPressed(config, position) }
    }
}

interface MedicationReviewListener {
    fun editButtonPressed(config: MedicationLog, position: Int)
}