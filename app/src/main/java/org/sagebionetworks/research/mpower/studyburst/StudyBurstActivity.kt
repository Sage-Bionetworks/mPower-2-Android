package org.sagebionetworks.research.mpower.studyburst


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_study_burst.*
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

        studyBurstRecycler.layoutManager = GridLayoutManager(this, 2)

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
                studyBurstMessage.text = message
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

                studyBurstRecycler.adapter = StudyBurstAdapter(items, this)

        })
        studyBurstViewModel.init()

        studyBurstBack.setOnClickListener { _ -> finish() }
    }

    override fun onResume() {
        super.onResume()
    }
}