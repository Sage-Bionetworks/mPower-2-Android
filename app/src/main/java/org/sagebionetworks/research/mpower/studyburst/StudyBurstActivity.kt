package org.sagebionetworks.research.mpower.studyburst

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.view.View
import android.widget.Toast
import dagger.android.AndroidInjection
import org.slf4j.LoggerFactory

import org.sagebionetworks.research.mpower.R
import kotlinx.android.synthetic.main.activity_study_burst.*
import org.researchstack.backbone.utils.ResUtils
import org.sagebionetworks.research.mpower.TaskLauncher
import org.sagebionetworks.research.mpower.TaskLauncher.TaskLaunchState.Type.LAUNCH_ERROR
import org.sagebionetworks.research.mpower.viewmodel.StudyBurstItem
import org.sagebionetworks.research.mpower.viewmodel.StudyBurstTaskInfo
import org.sagebionetworks.research.mpower.viewmodel.StudyBurstViewModel
import javax.inject.Inject


class StudyBurstActivity : AppCompatActivity(), StudyBurstAdapterListener {

    private val LOGGER = LoggerFactory.getLogger(StudyBurstActivity::class.java)

    /**
     * @property studyBurstViewModel encapsulates all read/write data operations
     */
    private val studyBurstViewModel: StudyBurstViewModel by lazy {
        StudyBurstViewModel.create(this)
    }

    /**
     * @property taskLauncher used to launch the study burst tasks
     */
    @Inject lateinit var taskLauncher: TaskLauncher

    /**
     * @property studyBurstAdapter used in the RecyclerView
     */
    private var studyBurstAdapter: StudyBurstAdapter? = null

    /**
     * @propert countdownTask used to countdown the progress to the study burst ends
     */
    private var countdownTask: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        LOGGER.debug("StudyBurstActivity.onCreate()")
        setContentView(R.layout.activity_study_burst)

        studyBurstRecycler.layoutManager = GridLayoutManager(this, 2)
        observeLiveData()
        study_burst_next.setOnClickListener { onNextClicked() }
        studyBurstBack.setOnClickListener { finish() }
    }

    private fun observeLiveData() {
        studyBurstViewModel.liveData().observe(this, Observer { it?.let {
            // StudyBurstItem actually can't be null but appears Nullable because of the Observer @Nullable annotation
            if (!it.hasStudyBurst) {
                // If we don't have a study burst, this means that we should send the user to a completion task
                // Or, if we don't have any completion tasks, we should probably leave this Activity
                it.nextCompletionActivityToShow?.let {
                    // TODO: mdephillips 9/12/18 run the completion task
                    Toast.makeText(this,
                            "Feature not implemented: run " + it.activityIdentifier(),
                            Toast.LENGTH_LONG).show()
                } ?: run {
                    // TODO: mdephillips 9/12/18 run the completion task
                    Toast.makeText(this,
                            "Study burst not available on this day",
                            Toast.LENGTH_LONG).show()
                    finish()
                }
                return@Observer
            }

            setupStudyBurstTitle(it)
            setupStudyBurstMessage(it)
            setupExpiresOnText(it)
            setupStatusBarWheel(it)
            setupStudyBurstTopProgress(it)
            setupStudyBurstAdapter(it)
        }})
    }

    override fun onStop() {
        super.onStop()
        countdownTask?.cancel()
    }

    /**
     * @return The title string is the same regardless of how many days they've missed, if any.
     *         It will vary only by the current day of the study burst
     */
    private fun setupStudyBurstTitle(item: StudyBurstItem) {
        if (item.dayCount == null) {
            studyBurstTitle.visibility = View.GONE
            return
        }
        studyBurstTitle.visibility = View.VISIBLE
        val stringRes = ResUtils.getStringResourceId(this,
                String.format("study_burst_title_day_%s", item.dayCount))
        studyBurstTitle.text = if (stringRes > 0) getString(stringRes) else null
    }

    /**
     * @return The message will vary by the current day of the study burst for no missed days
     *         For all else, the message will be the same for each day of the study burst and will simply
     *         indicate the current day and the number of missed days
     */
    private fun setupStudyBurstMessage(item: StudyBurstItem) {
        if (item.dayCount == null) {
            studyBurstMessage.visibility = View.GONE
            return
        }
        studyBurstMessage.visibility = View.VISIBLE
        studyBurstMessage.text = when (item.missedDayCount) {
            // If the user hasn't missed any days, the message will vary by the current day of the study burst
            0 -> {
                val stringRes = ResUtils.getStringResourceId(this,
                        String.format("study_burst_message_day_%s", item.dayCount))
                if (stringRes > 0) getString(stringRes) else ""
            }
            // The message will be the same for each day of the study burst and will simply
            // indicate the current day and the number of missed days
            1 -> getString(R.string.study_burst_message_days_missed_one, item.dayCount)
            else -> getString(R.string.study_burst_message_days_missed, item.dayCount, item.missedDayCount)
        }
    }

    /**
     * Sets up the expires on text using a Chronometer TextView or hides it if expires on is null
     */
    private fun setupExpiresOnText(item: StudyBurstItem) {
        if (item.millisToExpiration == null || item.timeUntilStudyBurstExpiration == null) {
            expiresTextContainer.visibility = View.GONE
            return
        }
        expiresTextContainer.visibility = View.VISIBLE
        countdownTask?.cancel()
        item.millisToExpiration?.let {
            countdownTask = object : CountDownTimer(it, 1000L) {
                override fun onTick(millisUntilFinished: Long) {
                    expiresText.text = item.timeUntilStudyBurstExpiration
                }

                override fun onFinish() {
                    expiresTextContainer.visibility = View.GONE
                    observeLiveData()
                }
            }
            countdownTask?.start()
        }
    }

    /**
     * Sets up the status bar wheel day number and progress
     */
    private fun setupStatusBarWheel(item: StudyBurstItem) {
        if (item.dayCount == null) {
            studyBurstStatusWheel.visibility = View.GONE
            return
        }
        studyBurstStatusWheel.visibility = View.VISIBLE
        studyBurstStatusWheel.setDayCount(item.dayCount)
        studyBurstStatusWheel.setMaxProgress(100)
        studyBurstStatusWheel.setProgress(Math.round(item.progress * 100))
    }

    /**
     * Sets up the top progress bar based on the current day of the study burst and the usual length
     */
    private fun setupStudyBurstTopProgress(item: StudyBurstItem) {
        if (item.dayCount == null) {
            studyBurstTopProgressBar.visibility = View.GONE
            return
        }
        studyBurstTopProgressBar.visibility = View.VISIBLE
        studyBurstTopProgressBar.max = item.config.numberOfDays
        studyBurstTopProgressBar.progress = item.dayCount
    }

    /**
     * Sets up the study burst adapter to display the tasks the user can complete
     */
    private fun setupStudyBurstAdapter(item: StudyBurstItem) {
        studyBurstAdapter = StudyBurstAdapter(this, item.orderedTasks)
        studyBurstAdapter?.listener = this
        studyBurstRecycler.adapter = studyBurstAdapter
    }

    /**
     * Next button run the next incomplete task
     */
    private fun onNextClicked() {
        studyBurstAdapter?.nextItem?.let {
            itemSelected(it)
        }
    }

    /**
     * StudyBurstAdapterListener function, called when a task icon in the RecyclerView is selected.
     */
    override fun itemSelected(item: StudyBurstTaskInfo) {
        val uuid = studyBurstViewModel.createScheduleTaskRunUuid(item.schedule)
        taskLauncher.launchTask(this, item.task.identifier, uuid)
                .observe(this, Observer {
                    when(it?.state) {
                        LAUNCH_ERROR -> {
                            Toast.makeText(this,
                                    "Error launching  " + item.task.identifier,
                                    Toast.LENGTH_LONG).show()
                        }
                    }
                })
    }
}