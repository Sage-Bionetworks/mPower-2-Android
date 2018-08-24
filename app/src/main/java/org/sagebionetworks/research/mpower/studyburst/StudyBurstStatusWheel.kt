package org.sagebionetworks.research.mpower.studyburst

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.mpower2_study_burst_status_bar.view.*
import org.sagebionetworks.research.mpower.R
import kotlinx.android.synthetic.main.study_burst_status_wheel.*;
import kotlinx.android.synthetic.main.study_burst_status_wheel.view.*

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