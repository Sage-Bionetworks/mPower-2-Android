package org.sagebionetworks.research.mpower.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import io.reactivex.disposables.Disposable
import org.sagebionetworks.bridge.rest.RestUtils
import org.sagebionetworks.bridge.rest.model.SurveyReference
import org.sagebionetworks.research.mpower.researchstack.framework.MpTaskFactory
import org.sagebionetworks.research.mpower.researchstack.framework.MpViewTaskActivity
import org.sagebionetworks.research.sageresearch.profile.ProfileSettingsFragment
import org.sagebionetworks.researchstack.backbone.factory.IntentFactory
import org.sagebionetworks.researchstack.backbone.model.TaskModel
import org.sagebionetworks.researchstack.backbone.result.TaskResult
import org.sagebionetworks.researchstack.backbone.ui.ViewTaskActivity
import org.sagebionetworks.researchstack.backbone.ui.fragment.ActivitiesFragment.REQUEST_TASK
import org.slf4j.LoggerFactory

class MPowerProfileSettingsFragment: ProfileSettingsFragment() {

    override fun newInstance(profileKey: String, isMainView: Boolean): ProfileSettingsFragment {
        return newFragmentInstance(profileKey, isMainView)
    }

    private val LOGGER = LoggerFactory.getLogger(MPowerProfileSettingsFragment::class.java)

    private var disposable: Disposable? = null


    override fun launchSurvey(surveyReference: SurveyReference) {
        disposable = profileViewModel.loadSurvey(surveyReference)
                .map { RestUtils.toType(it, TaskModel::class.java) }
                .firstOrError()
                .subscribe({ launchTask(it) }, {})

    }

    fun launchTask(taskModel: TaskModel) {
        val factory = MpTaskFactory()
        val smartSurveyTask = factory.createMpSmartSurveyTask(activity!!, taskModel)
        startActivityForResultParent(IntentFactory.INSTANCE.newTaskIntent(activity,
                MpViewTaskActivity::class.java, smartSurveyTask), REQUEST_TASK)

    }

    //Copied from TrackingTabFragment
    /**
     * Due to a behavior issue in nested child fragments (like this fragment)
     * We must call the startActivityForResult on the parent fragment if we can
     * @param intent to launch
     * @param requestCode to launch with
     */
    private fun startActivityForResultParent(intent: Intent, requestCode: Int) {
        var parent: Fragment? = this
        while (parent!!.parentFragment != null) {
            parent = parent.parentFragment
        }
        parent.startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        LOGGER.info("onActivityResult with requestCode $requestCode resultCode $resultCode")
        // Will be set if a survey was just successfully completed and uploaded
        var successfulSurveyUploadTaskId: String? = null
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_TASK) {
            val taskResult = data!!.getSerializableExtra(ViewTaskActivity.EXTRA_TASK_RESULT) as TaskResult

            LOGGER.info("Task was successfully finished")

//            if (trackingTabViewModel.currentSurveyTask != null) {
//                // This will trigger any after-rule processing like adding data groups based on survey answers
//                trackingTabViewModel.currentSurveyTask!!.processTaskResult(taskResult)
//            }
//            if (trackingTabViewModel.currentSurveySchedule != null) {
//                LOGGER.info("currentSurveySchedule non-null, uploading results")
//                trackingTabViewModel.currentSurveySchedule!!.startedOn = Instant.ofEpochMilli(taskResult.startDate.time)
//                trackingTabViewModel.currentSurveySchedule!!.finishedOn = Instant.ofEpochMilli(taskResult.endDate.time)
//                // This function updates the schedule on bridge and in the ScheduleRepository
//                studyBurstViewModel.updateScheduleToBridge(trackingTabViewModel.currentSurveySchedule!!)
//                // This function uploads the result of the task to S3
//                studyBurstViewModel.uploadResearchStackTaskResultToS3(trackingTabViewModel.currentSurveySchedule, taskResult)
//                // This function will generate a client data report for the research stack task result
//                reportViewModel.saveResearchStackReports(taskResult)
//
//                if (MOTIVATION == trackingTabViewModel.currentSurveySchedule!!.activityIdentifier()) {
//                    // send the user straight into the study burst
//                    goToStudyBurst()
//                }
//                successfulSurveyUploadTaskId = trackingTabViewModel.currentSurveySchedule!!.activityIdentifier()
//            } else {
//                LOGGER.info("currentSurveySchedule is null, cannot upload results")
//            }
        }
//        trackingTabViewModel.currentSurveyTask = null
//        trackingTabViewModel.currentSurveySchedule = null

    }

    override fun onDetach() {
        super.onDetach()
        disposable?.dispose()
    }

    companion object {

        @JvmStatic
        fun newFragmentInstance(profileKey: String, isMainView: Boolean) =
                MPowerProfileSettingsFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PROFILE_KEY, profileKey)
                        putBoolean(ARG_IS_MAIN_VIEW, isMainView)
                    }
                }
    }

}