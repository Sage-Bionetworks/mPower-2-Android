package org.sagebionetworks.research.mpower.studyburst

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import org.slf4j.LoggerFactory

import org.sagebionetworks.research.mpower.R
import kotlinx.android.synthetic.main.activity_study_burst.*
import org.researchstack.backbone.utils.ResUtils
import org.sagebionetworks.research.mpower.R.id.*
import org.sagebionetworks.research.mpower.TaskLauncher
import org.sagebionetworks.research.mpower.viewmodel.StudyBurstViewModel
import javax.inject.Inject


class StudyBurstActivity : AppCompatActivity() {
    private val LOGGER = LoggerFactory.getLogger(StudyBurstActivity::class.java)

    private val studyBurstViewModel: StudyBurstViewModel by lazy {
        StudyBurstViewModel.create(this)
    }

    @Inject
    lateinit var taskLauncher: TaskLauncher

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        LOGGER.debug("StudyBurstActivity.onCreate()")
        setContentView(R.layout.activity_study_burst)

        studyBurstRecycler.layoutManager = GridLayoutManager(this, 2)

        studyBurstViewModel.liveData().observe(this, Observer {

//            if (it.hasStudyBurst == false) {
//                // If we don't have a study burst, this means that we should send the user to a completion task
//                // Or, if we don't have any completion tasks, we should probably leave this Activity
//                return@Observer
//            }
//
//// The title string is the same regardless of how many days they've missed, if any.
//            // It will vary only by the current day of the study burst
//            studyBurstTitle.text = {
//                val total = it.dayCount ?: null
//                let formatStr = String.format("study_burst_title_day_%s", total)
//                let titleStr = Localization.localizedString(formatStr)
//            }}.invoke()
//
//                studyBurstMessage.text = {
//            val total = it.dayCount ?: 0
//            val stringRes = String.format("study_burst_message_day_%s", total)
//            when (it.mis) {
//            // The message will vary by the current day of the study burst
//                0 -> getString(ResUtils.getStringResourceId(this, stringRes))
//            // The message will be the same for each day of the study burst and will simply
//            // indicate the current day and the number of missed days
//                1 -> getString(R.string.study_burst_message_days_missed_one, total)
//                else -> getString(R.string.study_burst_message_days_missed, total, missedDays)
//            }
//        }.invoke()
        })
//        studyBurstViewModel.getDaysMissed().observe(this, Observer {
//            missed ->
//                var message: String
//                var total: Int? = studyBurstViewModel.getDayCount().value
//                when(missed) {
//                    0 -> message = getString(R.string.study_burst_message_day)
//                    1 -> message = getString(R.string.study_burst_message_days_missed_one, total)
//                    else -> {
//                        message = getString(R.string.study_burst_message_days_missed, total, missed)
//                    }
//
//                }
//                studyBurstMessage.text = message
//        })
//        studyBurstViewModel.getTitle().observe(this, Observer {
//            text -> studyBurstTitle.setText(text)
//        })
//        studyBurstViewModel.getExpires().observe(this, Observer {
//            text ->
//                expiresText.text = getResources().getString(R.string.study_burst_progress_message, text)
//        })
//        studyBurstViewModel.getDayNumber().observe(this, Observer {
//            count ->
//                studyBurstStatusWheel.setDayCount(count ?: 0)
//                studyBurstStatusWheel.setProgress(count ?: 0)
//        })
//        studyBurstViewModel.getDayCount().observe(this, Observer {
//            count -> studyBurstStatusWheel.setMaxProgress(count ?: 0)
//
//        })
//
//        studyBurstViewModel.getItems().observe(this, Observer {
//            items ->
//                LOGGER.debug("How many items: " + items?.size)
//                var completed = items?.count { it.completed } ?: 0
//                LOGGER.debug("Number completed: $completed")
//                studyBurstTopProgressBar.progress = completed
//
//                studyBurstRecycler.adapter = StudyBurstAdapter(items, this)
//
//        })
//        studyBurstViewModel.init()

        studyBurstBack.setOnClickListener { _ -> finish() }
    }

    override fun onResume() {
        super.onResume()
    }
}