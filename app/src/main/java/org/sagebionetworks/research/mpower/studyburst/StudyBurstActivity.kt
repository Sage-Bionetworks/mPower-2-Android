package org.sagebionetworks.research.mpower.studyburst

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_study_burst.expiresText
import kotlinx.android.synthetic.main.activity_study_burst.expiresTextContainer
import kotlinx.android.synthetic.main.activity_study_burst.skip_button
import kotlinx.android.synthetic.main.activity_study_burst.studyBurstBack
import kotlinx.android.synthetic.main.activity_study_burst.studyBurstMessage
import kotlinx.android.synthetic.main.activity_study_burst.studyBurstRecycler
import kotlinx.android.synthetic.main.activity_study_burst.studyBurstStatusWheel
import kotlinx.android.synthetic.main.activity_study_burst.studyBurstTitle
import kotlinx.android.synthetic.main.activity_study_burst.studyBurstTopProgressBar
import kotlinx.android.synthetic.main.activity_study_burst.study_burst_next
import org.sagebase.crf.CrfTaskIntentFactory
import org.sagebionetworks.research.domain.repository.TaskRepository
import org.sagebionetworks.research.domain.task.TaskInfoView
import org.sagebionetworks.research.mpower.R
import org.sagebionetworks.research.mpower.TaskLauncher
import org.sagebionetworks.research.mpower.TaskLauncher.TaskLaunchState.Type.LAUNCH_ERROR
import org.sagebionetworks.research.mpower.research.DataSourceManager
import org.sagebionetworks.research.mpower.research.MpIdentifier
import org.sagebionetworks.research.mpower.research.MpIdentifier.HEART_SNAPSHOT
import org.sagebionetworks.research.mpower.research.MpIdentifier.WALK_AND_BALANCE
import org.sagebionetworks.research.mpower.researchstack.framework.MpDataProvider
import org.sagebionetworks.research.mpower.sageresearch.PerformTaskWithResultActivity
import org.sagebionetworks.research.mpower.viewmodel.StudyBurstItem
import org.sagebionetworks.research.mpower.viewmodel.StudyBurstTaskInfo
import org.sagebionetworks.research.mpower.viewmodel.StudyBurstViewModel
import org.sagebionetworks.research.presentation.model.TaskView
import org.sagebionetworks.research.sageresearch.dao.room.ScheduledActivityEntity
import org.sagebionetworks.researchstack.backbone.result.TaskResult
import org.sagebionetworks.researchstack.backbone.ui.ViewTaskActivity
import org.sagebionetworks.researchstack.backbone.utils.ResUtils
import org.slf4j.LoggerFactory
import javax.inject.Inject

// Used with activity.startActivityForResult()
const val STUDY_BURST_REQUEST_CODE = 1483
// Used with activity.setResult()
const val STUDY_BURST_EXTRA_GUID_OF_TASK_TO_RUN = "StudyBurstActivity.Guid.ToRun"

class StudyBurstActivity : AppCompatActivity(), StudyBurstAdapterListener {

    private val logger = LoggerFactory.getLogger(StudyBurstActivity::class.java)

    companion object {
        private val CRF_REQUEST_CODE: Int = 1591
    }

    /**
     * @property studyBurstViewModel encapsulates all read/write data operations
     */
    private val studyBurstViewModel: StudyBurstViewModel by lazy {
        ViewModelProviders.of(this, studyBurstViewModelFactory).get(StudyBurstViewModel::class.java)
    }

    /**
     * @property taskLauncher used to launch the study burst tasks
     */
    @Inject lateinit var taskLauncher: TaskLauncher

    /**
     * @property taskRepository used to create walk and balance test
     */
    @Inject lateinit var taskRepository: TaskRepository

    /**
     * @property studyBurstViewModelFactory used to create a StudyBurstViewModel instance injected through Dagger
     */
    @Inject lateinit var studyBurstViewModelFactory: StudyBurstViewModel.Factory

    /**
     * @property studyBurstAdapter used in the RecyclerView
     */
    private var studyBurstAdapter: StudyBurstAdapter? = null

    /**
     * @property viewModelObserver keeps track of the previous observer so we can refresh after the expiration timer
     */
    private var viewModelObserver: Observer<StudyBurstItem>? = null

    /**
     * @propert countdownTask used to countdown the progress to the study burst ends
     */
    private var countdownTask: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        logger.debug("StudyBurstActivity.onCreate()")
        setContentView(R.layout.activity_study_burst)

        studyBurstRecycler.layoutManager = androidx.recyclerview.widget.GridLayoutManager(this, 2)
        observeLiveData()

        study_burst_next.setOnClickListener { onNextClicked() }
        skip_button.setOnClickListener { onSkipClicked() }
        studyBurstBack.setOnClickListener {
            finishActivity(null)
        }
    }

    override fun onStop() {
        super.onStop()
        countdownTask?.cancel()
    }

    /**
     * This should be called from the onCreate() function only once.
     * It will observer the study burst view model in all lifecycle scenarios.
     */
    private fun observeLiveData() {
        // StudyBurstItem actually can't be null but appears Nullable because of the Observer @Nullable annotation
        viewModelObserver = Observer { item -> item?.let {
            setupStudyBurstTitle(it)
            setupStudyBurstMessage(it)
            setupExpiresOnText(it)
            setupStatusBarWheel(it)
            setupStudyBurstTopProgress(it)
            setupStudyBurstAdapter(it)

            // If there are no more items to run, the user has done all their study burst activities
            if (studyBurstAdapter?.nextItem == null) {
                item.nextCompletionActivityToShow?.let { nextCompletionActivityToShow ->
                    // If we don't have a study burst, and we have the next completion activity to show,
                    // we should send the user back to the tracking tab fragment to do the completion task
                    finishActivity(nextCompletionActivityToShow)
                } ?: run {
                    finishActivity(null)
                }
            }
        }}
        viewModelObserver?.let {
            studyBurstViewModel.liveData().observe(this, it)
        }
    }

    /**
     * @param scheduleToRun upon returning to the previous screen, if null, none will be run
     */
    private fun finishActivity(scheduleToRun: ScheduledActivityEntity?) {
        logger.info("finishActivity with $scheduleToRun")
        // LiveData updates happen so quickly, we can sometimes get duplicate finish requests
        if (isFinishing) {
            return
        }
        scheduleToRun?.let {
            val resultIntent = Intent()
            resultIntent.putExtra(STUDY_BURST_EXTRA_GUID_OF_TASK_TO_RUN, it)
            setResult(RESULT_OK, resultIntent)
        } ?: run {
            setResult(Activity.RESULT_CANCELED)
        }
        finish()
    }

    /**
     * This should only be called when you need to force a refresh on the view model queries to the db.
     * Some scenarios of when that would be useful are when time passes into tomorrow, or when the expiration timer
     * is finished and we need to reset all previously finished study burst activities.
     */
    private fun refreshLiveData() {
        // Refresh view model observer to force refresh data
        viewModelObserver?.let {
            studyBurstViewModel.liveData().removeObservers(this)
            studyBurstViewModel.refreshLiveData(this).observe(this, it)
        }
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
                    refreshLiveData()
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
        } ?: run {
            finishActivity(null) // no more items to run, leave screen
        }
    }

    private fun onSkipClicked() {
        studyBurstAdapter?.nextItem?.task?.identifier?.let {
            studyBurstViewModel.studyBurstSettingsDao.skipTaskForToday(it)
            refreshLiveData()
        }
    }

    /**
     * StudyBurstAdapterListener function, called when a task icon in the RecyclerView is selected.
     */
    override fun itemSelected(item: StudyBurstTaskInfo) {

        val uuid = studyBurstViewModel.createScheduleTaskRunUuid(item.schedule?.guid)

        if (item.task.identifier == HEART_SNAPSHOT) {
            // Look for saved answers to gender/age questions if we have them
            val intent = studyBurstViewModel.studyBurstSettingsDao.loadGenderAndBirthYear()?.let {
                CrfTaskIntentFactory.getHeartRateSnapshotTaskIntent(this, it.gender, it.birthYear)
            } ?: run {
                CrfTaskIntentFactory.getHeartRateSnapshotTaskIntent(this)
            }
            startActivityForResult(intent, CRF_REQUEST_CODE)
        } else {
            taskLauncher.launchTask(this, item.task.identifier, uuid)
                    .observe(this, Observer {
                        when (it?.state) {
                            LAUNCH_ERROR -> {
                                Toast.makeText(this,
                                        "Error launching  " + item.task.identifier,
                                        Toast.LENGTH_LONG).show()
                            }
                        }
                    })
        }
    }

    @Override
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CRF_REQUEST_CODE && resultCode == RESULT_OK) {
            val taskResult = data?.getSerializableExtra(ViewTaskActivity.EXTRA_TASK_RESULT) as? TaskResult
                    ?: run { return }

            MpDataProvider.getInstance().uploadTaskResult(this, taskResult)

            studyBurstViewModel.studyBurstSettingsDao.saveGenderAndBirthYear(taskResult)
            studyBurstViewModel.studyBurstSettingsDao.setSnapshotComplete()
            studyBurstViewModel.saveResearchStackReports(taskResult)

            // Force refresh is necessary as heart snapshot logic is not reflected in live data
            refreshLiveData()
        }
    }
}