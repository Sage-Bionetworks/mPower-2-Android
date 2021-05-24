package org.sagebionetworks.research.mpower.studyburst

import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.study_burst_cell.view.cell_details
import kotlinx.android.synthetic.main.study_burst_cell.view.cell_image
import kotlinx.android.synthetic.main.study_burst_cell.view.cell_title
import kotlinx.android.synthetic.main.study_burst_cell.view.checkmark
import kotlinx.android.synthetic.main.study_burst_cell.view.view_holder_root
import org.sagebionetworks.researchstack.backbone.utils.ResUtils
import org.sagebionetworks.research.mpower.R
import org.sagebionetworks.research.mpower.viewmodel.StudyBurstTaskInfo

class StudyBurstAdapter(context: Context, val items : List<StudyBurstTaskInfo>?) : androidx.recyclerview.widget.RecyclerView.Adapter<ViewHolder>() {

    var listener: StudyBurstAdapterListener? = null

    private val layoutInflater = LayoutInflater.from(context)
    private val estimatedTimeFormat = context.getString(R.string.task_info_estimated_time_label)
    private val blackAndWhiteFilter: ColorMatrixColorFilter by lazy {
        val matrix = ColorMatrix()
        matrix.setSaturation(0f)
        ColorMatrixColorFilter(matrix)
    }
    private val imageResMap: Map<String?, Int>? = items?.associateBy( { it.task.imageName }, {
        ResUtils.getDrawableResourceId(context, it.task.imageName)
    })
    private val blackColor = ResourcesCompat.getColor(context.resources, R.color.black, null)
    private val lightGrayColor = ResourcesCompat.getColor(context.resources, R.color.appLightGray, null)

    val nextItem: StudyBurstTaskInfo? get() {
        items?.forEach {
            if (!it.isComplete && !it.isSkipped) {
                return it
            }
        }
        return null
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(layoutInflater.inflate(R.layout.study_burst_cell, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        items?.get(position)?.let { item ->
            holder.tvTitle.text = item.task.title
            holder.tvDetails.text = estimatedTimeFormat.format(item.task.estimatedMinutes ?: 1)

            val imageRes = imageResMap?.get(item.task.imageName) ?: 0
            if (imageRes > 0) {
                holder.ivImage.visibility = View.VISIBLE
                holder.ivImage.setImageResource(imageRes)
            } else {
                holder.ivImage.visibility = View.INVISIBLE
            }

            if (item.isActive || item.isComplete) {
                holder.ivImage.colorFilter = null
                holder.ivImage.alpha = 1.0f
                holder.tvTitle.setTextColor(blackColor)
                holder.tvDetails.setTextColor(blackColor)
                holder.root.isEnabled = true
                holder.root.setOnClickListener { listener?.itemSelected(item) }
            } else {
                holder.root.isEnabled = false
                holder.ivImage.colorFilter = blackAndWhiteFilter
                holder.ivImage.alpha = 0.33f
                holder.tvTitle.setTextColor(lightGrayColor)
                holder.tvDetails.setTextColor(lightGrayColor)
            }

            holder.ivComplete.visibility = if (item.isComplete) View.VISIBLE else View.GONE
        }
    }
}

class ViewHolder (view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
    val root: View = view.view_holder_root
    val tvTitle: TextView = view.cell_title
    val tvDetails: TextView = view.cell_details
    val ivImage: ImageView = view.cell_image
    val ivComplete: ImageView = view.checkmark
}

interface StudyBurstAdapterListener {
    fun itemSelected(item: StudyBurstTaskInfo)
}