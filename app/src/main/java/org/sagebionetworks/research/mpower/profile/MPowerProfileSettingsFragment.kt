package org.sagebionetworks.research.mpower.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import org.joda.time.DateTime
import org.sagebionetworks.bridge.android.manager.models.ProfileDataItem
import org.sagebionetworks.bridge.android.manager.models.ProfileItemProfileTableItem
import org.sagebionetworks.bridge.rest.RestUtils
import org.sagebionetworks.bridge.rest.model.SurveyReference
import org.sagebionetworks.research.mpower.reminders.StudyBurstReminderActivity
import org.sagebionetworks.research.mpower.researchstack.framework.MpTaskFactory
import org.sagebionetworks.research.mpower.researchstack.framework.MpViewTaskActivity
import org.sagebionetworks.research.mpower.viewmodel.PassiveGaitViewModel
import org.sagebionetworks.research.sageresearch.profile.ProfileSettingsFragment
import org.sagebionetworks.research.sageresearch.profile.ProfileViewModel
import org.sagebionetworks.researchstack.backbone.factory.IntentFactory
import org.sagebionetworks.researchstack.backbone.model.TaskModel
import org.sagebionetworks.researchstack.backbone.result.TaskResult
import org.sagebionetworks.researchstack.backbone.ui.ViewTaskActivity
import org.sagebionetworks.researchstack.backbone.ui.fragment.ActivitiesFragment.REQUEST_TASK
import org.sagebionetworks.researchstack.backbone.utils.StepResultHelper
import org.slf4j.LoggerFactory
import javax.inject.Inject

class MPowerProfileSettingsFragment: ProfileSettingsFragment() {

    override fun newInstance(profileKey: String, isMainView: Boolean): ProfileSettingsFragment {
        return newFragmentInstance(profileKey, isMainView)
    }

    private val LOGGER = LoggerFactory.getLogger(MPowerProfileSettingsFragment::class.java)

    private var disposable: Disposable? = null

    private lateinit var mPowerProfileViewModel: MPowerProfileViewModel

    @Inject
    lateinit var profileViewModelFactory: MPowerProfileViewModel.Factory

    private val passiveGaitViewModel: PassiveGaitViewModel by lazy {
        ViewModelProvider(requireActivity()).get(PassiveGaitViewModel::class.java)
    }

    override fun loadProfileViewModel(): ProfileViewModel {
        mPowerProfileViewModel = ViewModelProvider(this, profileViewModelFactory).get(MPowerProfileViewModel::class.java)
        return mPowerProfileViewModel
    }

    override fun launchSurvey(surveyReference: SurveyReference) {
        showLoading(true)
        disposable = profileViewModel.loadSurvey(surveyReference)
                .map { RestUtils.toType(it, TaskModel::class.java) }
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            launchTask(it)
                            showLoading(false)
                        },
                        {
                            showLoading(false)
                            LOGGER.debug("Survey loading failed")
                        })

    }

    fun launchTask(taskModel: TaskModel) {
        val factory = MpTaskFactory()
        mPowerProfileViewModel.currentSurveyTask = factory.createMpSmartSurveyTask(requireActivity(), taskModel)
        startActivityForResultParent(IntentFactory.INSTANCE.newTaskIntent(activity,
                MpViewTaskActivity::class.java, mPowerProfileViewModel.currentSurveyTask), REQUEST_TASK)

    }

    override fun launchEditParticipantItem(profileItem: ProfileItemProfileTableItem, profileDataItem: ProfileDataItem) {
        mPowerProfileViewModel.currentProfileItem = profileItem
        val task = mPowerProfileViewModel.createTask(profileItem, profileDataItem)
        launchTask(task)
    }

    //Copied from TrackingTabFragment
    /**
     * Due to a behavior issue in nested child fragments (like this fragment)
     * We must call the startActivityForResult on the parent fragment if we can
     * @param intent to launch
     * @param requestCode to launch with
     */
    private fun startActivityForResultParent(intent: Intent, requestCode: Int) {
        var parent: androidx.fragment.app.Fragment? = this
        while (parent?.parentFragment != null) {
            parent = parent.parentFragment
        }
        parent?.startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        LOGGER.info("onActivityResult with requestCode $requestCode resultCode $resultCode")

        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_TASK) {
            val taskResult = data?.getSerializableExtra(ViewTaskActivity.EXTRA_TASK_RESULT) as TaskResult

            LOGGER.info("Task was successfully finished")

            if (mPowerProfileViewModel.currentProfileItem != null) {
                val stepResult = StepResultHelper.findStepResult(taskResult.results.values, mPowerProfileViewModel.currentProfileItem?.profileItemKey)
                val result = stepResult.result
                if (result is String) {
                    profileViewModel.saveStudyParticipantValue(result, mPowerProfileViewModel.currentProfileItem?.profileItemKey?:"")
                    adapter?.notifyDataSetChanged()
                }
            } else {
                // This will trigger any after-rule processing like adding data groups based on survey answers
                mPowerProfileViewModel.currentSurveyTask?.processTaskResult(taskResult)

                profileViewModel.bridgeRepoManager.saveTaskResult(taskResult, mPowerProfileViewModel.currentScheduledActivity)

            }
        }
        mPowerProfileViewModel.currentSurveyTask = null
        mPowerProfileViewModel.currentScheduledActivity = null
        mPowerProfileViewModel.currentProfileItem = null
    }

    override fun launchStudyBurstReminderTime() {
        startActivityForResultParent(Intent(activity, StudyBurstReminderActivity::class.java), REQUEST_TASK)
    }

    override fun launchWithdraw(firstName: String, joinedDate: DateTime) {
        val intent = Intent(activity, WithdrawFromStudyActivity::class.java)
        intent.putExtra(WithdrawFromStudyActivity.ARG_NAME_KEY, firstName)
        intent.putExtra(WithdrawFromStudyActivity.ARG_JOINED_DATE, joinedDate)
        startActivity(intent)
    }

    override fun launchPassiveDataAllowed(profileItem: ProfileItemProfileTableItem, profileDataItem: ProfileDataItem, value: String?) {
        val intent = Intent(activity, PassiveGaitPermissionActivity::class.java)
        intent.putExtra(PassiveGaitPermissionActivity.ARG_PASSIVE_DATA_ALLOWED_VALUE, value)
        startActivityForResultParent(intent, REQUEST_TASK)
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
