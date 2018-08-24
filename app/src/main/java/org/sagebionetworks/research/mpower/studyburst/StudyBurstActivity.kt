package org.sagebionetworks.research.mpower.studyburst


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_study_burst.*
import kotlinx.android.synthetic.main.study_burst_cell.view.*
import org.slf4j.LoggerFactory

import org.sagebionetworks.research.mpower.R
import org.sagebionetworks.research.mpower.TaskLauncher
import javax.inject.Inject


class StudyBurstActivity : AppCompatActivity() {
    private val LOGGER = LoggerFactory.getLogger(StudyBurstActivity::class.java)

    private val studyBurstViewModel: StudyBurstViewModel by lazy {
        ViewModelProviders.of(this).get(StudyBurstViewModel::class.java)
    }

    @Inject
    lateinit var taskLauncher: TaskLauncher

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        LOGGER.debug("StudyBurstActivity.onCreate()")
        setContentView(R.layout.activity_study_burst)

        studyBurstViewModel.getDaysMissed().observe(this, Observer {
            missed ->
                var message: String
                var total: Int? = studyBurstViewModel.getDayCount().value
                when(missed) {
                    0 -> message = getString(R.string.study_burst_message_day)
                    1 -> message = getString(R.string.study_burst_message_days_missed_one, total)
                    else -> {
                        message = getString(R.string.study_burst_message_days_missed, total, missed)
                    }

                }
                studyBurstMessage.setText(message)
        })
        studyBurstViewModel.getTitle().observe(this, Observer {
            text -> studyBurstTitle.setText(text)
        })
        studyBurstViewModel.getExpires().observe(this, Observer {
            text ->
                expiresText.text = getResources().getString(R.string.study_burst_progress_message, text)
        })
        studyBurstViewModel.getDayNumber().observe(this, Observer {
            count ->
                studyBurstStatusWheel.setDayCount(count ?: 0)
                studyBurstStatusWheel.setProgress(count ?: 0)
        })
        studyBurstViewModel.getDayCount().observe(this, Observer {
            count -> studyBurstStatusWheel.setMaxProgress(count ?: 0)

        })

        studyBurstViewModel.getItems().observe(this, Observer {
            items ->
                LOGGER.debug("How many items: " + items?.size)
                var completed = items?.count { it.completed } ?: 0
                LOGGER.debug("Number completed: $completed")
                studyBurstTopProgressBar.progress = completed

                val layoutInflater:LayoutInflater = LayoutInflater.from(applicationContext)
                for(i in 1..4) {
                    var cell: ViewGroup? = null
                    val sbi = items?.get(i-1)
                    // TODO: a better way to do this dynamically?
                    when(i) {
                        1 -> cell = cell_1
                        2 -> cell = cell_2
                        3 -> cell = cell_3
                        4 -> cell = cell_4
                    }

                    val view: View = layoutInflater.inflate(R.layout.study_burst_cell, cell, false)
                    view.cell_number.text = "#"+i
                    view.cell_message.text = sbi?.title
                    view.cell_details.text = sbi?.detail
                    if(sbi != null && sbi.active) {
                        view.cell_message.setTextColor(getResources().getColor(R.color.black))
                        view.cell_details.setTextColor(getResources().getColor(R.color.black))
                        if(sbi != null && sbi.completed) {
                            view.cell_image.setImageResource(sbi?.completedImageResId!!)
                        } else {
                            view.cell_image.setImageResource(sbi?.activeImageResId!!)
                        }
                    } else {
                        view.cell_message.setTextColor(getResources().getColor(R.color.appLightGray))
                        view.cell_details.setTextColor(getResources().getColor(R.color.appLightGray))
                        view.cell_image.setImageResource(sbi?.inactiveImageResId!!)
                    }
                    cell?.addView(view)

                }
        })
        studyBurstViewModel.init()

        studyBurstBack.setOnClickListener { _ -> finish() }
    }

    override fun onResume() {
        super.onResume()
        LOGGER.debug("StudyBurstActivity.onResume()")
    }
}