package org.sagebionetworks.research.mpower.studyburst

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.study_burst_cell.view.*
import org.sagebionetworks.research.mpower.R

class StudyBurstAdapter(val items : List<StudyBurstItem>?, val context: Context) : RecyclerView.Adapter<ViewHolder>() {

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.study_burst_cell, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sbi = items?.get(position)
        if(sbi != null) {
            holder.tvMessage.text = sbi.title
            holder.tvDetails.text = sbi.detail
            holder.tvNumber.text = context.getString(R.string.study_burst_cell_number_label, position)
            if (sbi.active) {
                if (sbi.completed) {
                    holder.ivImage.setImageResource(sbi.completedImageResId)
                } else {
                    holder.ivImage.setImageResource(sbi.activeImageResId)
                }
                holder.tvMessage.setTextColor(context.getResources().getColor(R.color.black))
                holder.tvDetails.setTextColor(context.getResources().getColor(R.color.black))
            } else {
                holder.ivImage.setImageResource(sbi.inactiveImageResId)
                holder.tvMessage.setTextColor(context.getResources().getColor(R.color.appLightGray))
                holder.tvDetails.setTextColor(context.getResources().getColor(R.color.appLightGray))
            }
        }
    }
}

class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    val tvNumber = view.cell_number
    val tvMessage = view.cell_message
    val tvDetails = view.cell_details
    val ivImage = view.cell_image

}