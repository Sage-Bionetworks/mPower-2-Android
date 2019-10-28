package org.sagebionetworks.research.mpower.profile

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_withdraw_from_study.*
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.sagebionetworks.bridge.researchstack.BridgeDataProvider
import org.sagebionetworks.bridge.rest.RestUtils
import org.sagebionetworks.research.mpower.R
import org.sagebionetworks.research.mpower.researchstack.framework.MpTaskFactory
import org.sagebionetworks.research.mpower.researchstack.framework.MpViewTaskActivity
import org.sagebionetworks.researchstack.backbone.factory.IntentFactory
import org.sagebionetworks.researchstack.backbone.model.TaskModel
import org.sagebionetworks.researchstack.backbone.result.TaskResult
import org.sagebionetworks.researchstack.backbone.ui.ViewTaskActivity
import org.sagebionetworks.researchstack.backbone.ui.fragment.ActivitiesFragment
import org.sagebionetworks.researchstack.backbone.utils.StepResultHelper
import org.slf4j.LoggerFactory
import rx.subscriptions.CompositeSubscription
import javax.inject.Inject

class WithdrawFromStudyActivity : DaggerAppCompatActivity() {

    companion object {

        const val ARG_NAME_KEY = "arg_name_key"
        const val ARG_JOINED_DATE = "art_joined_date"
    }

    private val LOGGER = LoggerFactory.getLogger(WithdrawFromStudyActivity::class.java)

    private var disposable: Disposable? = null
    private var compositeSubscription = CompositeSubscription()

    @Inject
    lateinit var viewModelFactory: WithdrawFromStudyViewModel.Factory
    private lateinit var viewModel: WithdrawFromStudyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_withdraw_from_study)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(WithdrawFromStudyViewModel::class.java);
        val name = intent.getStringExtra(ARG_NAME_KEY)
        val joinedDate =  intent.getSerializableExtra(ARG_JOINED_DATE) as DateTime
        val formatter = DateTimeFormat.forPattern("MMMMM d, yyyy")
        this.titleTextView.setText(getString(R.string.withdraw_title, name, formatter.print(joinedDate)))
        buttonWithdraw.setOnClickListener {
            launchSurvey()
        }
    }

    fun launchSurvey() {

        disposable = viewModel.loadWithdrawSurvey()
                .map { RestUtils.toType(it, TaskModel::class.java) }
                .subscribe({ launchTask(it) }, {LOGGER.debug("Survey loading failed")})

    }

    fun launchTask(taskModel: TaskModel) {
        val factory = MpTaskFactory()
        val task = factory.createMpSmartSurveyTask(this, taskModel)
        startActivityForResult(IntentFactory.INSTANCE.newTaskIntent(this,
                MpViewTaskActivity::class.java, task), ActivitiesFragment.REQUEST_TASK)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == Activity.RESULT_OK && requestCode == ActivitiesFragment.REQUEST_TASK) {
            val taskResult = data!!.getSerializableExtra(ViewTaskActivity.EXTRA_TASK_RESULT) as TaskResult
            val stepResult = StepResultHelper.findStepResult(taskResult.results.values, "withdrawalReason")
            val result = stepResult.result
            if (result is Array<*>) {
                val reason = result.joinToString(separator = ", ")
                compositeSubscription.add(BridgeDataProvider.getInstance().withdrawAndSignout(this, reason).subscribe ( {finish()}, {finish()} ))
            }

        }
    }

    override fun onDestroy() {
        disposable?.dispose()
        compositeSubscription.clear()
        super.onDestroy()
    }

}
