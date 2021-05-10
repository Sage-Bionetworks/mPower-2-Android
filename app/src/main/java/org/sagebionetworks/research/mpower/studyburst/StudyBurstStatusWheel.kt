package org.sagebionetworks.research.mpower.studyburst

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.study_burst_status_wheel.view.study_burst_progress_bar_day
import kotlinx.android.synthetic.main.study_burst_status_wheel.view.study_burst_progress_bar_day_count
import kotlinx.android.synthetic.main.study_burst_status_wheel.view.study_burst_progress_bar_dial
import org.sagebionetworks.research.mpower.R

class StudyBurstStatusWheel : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    init {
        LayoutInflater.from(context).inflate(R.layout.study_burst_status_wheel, this, true)
    }

    fun setLabel(label: String) {
        study_burst_progress_bar_day.text = label
    }

    fun setDayLabelVisibility(isVisible: Boolean) {
        study_burst_progress_bar_day.visibility = if (isVisible) { View.VISIBLE } else { View.GONE }
    }

    fun setProgress(progress: Int) {
        study_burst_progress_bar_dial.progress = progress
    }

    fun setMaxProgress(max: Int) {
        study_burst_progress_bar_dial.max = max
    }

    fun setDayCount(count: Int) {
        study_burst_progress_bar_day_count.text = count.toString()
    }
}