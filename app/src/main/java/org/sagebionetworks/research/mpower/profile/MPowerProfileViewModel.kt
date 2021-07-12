package org.sagebionetworks.research.mpower.profile

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.common.base.Preconditions
import org.sagebionetworks.bridge.android.manager.AuthenticationManager
import org.sagebionetworks.bridge.android.manager.models.ProfileDataItem
import org.sagebionetworks.bridge.android.manager.models.ProfileItemProfileTableItem
import org.sagebionetworks.research.mpower.researchstack.framework.step.MpSmartSurveyTask
import org.sagebionetworks.research.sageresearch.dao.room.AppConfigRepository
import org.sagebionetworks.research.sageresearch.dao.room.ReportRepository
import org.sagebionetworks.research.sageresearch.dao.room.SurveyRepository
import org.sagebionetworks.research.sageresearch.repos.BridgeRepositoryManager
import org.sagebionetworks.researchstack.backbone.model.TaskModel
import java.util.*
import javax.inject.Inject


class MPowerProfileViewModel(
        bridgeRepoManager: BridgeRepositoryManager,
        reportRepo: ReportRepository,
        appConfigRepo: AppConfigRepository,
        surveyRepo: SurveyRepository,
        authManager: AuthenticationManager,
        app: Application):

        MpBaseProfileViewModel(bridgeRepoManager, reportRepo, appConfigRepo, surveyRepo, authManager, app) {

    class Factory @Inject constructor(
            private val bridgeRepoManager: BridgeRepositoryManager,
            private val reportRepo: ReportRepository,
            private val appConfigRepo: AppConfigRepository,
            private val surveyRepo: SurveyRepository,
            private val authManager: AuthenticationManager,
            private val app: Application): ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            Preconditions.checkArgument(modelClass.isAssignableFrom(MPowerProfileViewModel::class.java))
            return MPowerProfileViewModel(bridgeRepoManager, reportRepo, appConfigRepo, surveyRepo, authManager, app) as T
        }
    }

    var currentSurveyTask: MpSmartSurveyTask? = null
    var currentProfileItem: ProfileItemProfileTableItem? = null

    /**
     * Create a task for editing a ParticipantProfileDataItem.  The ProfileItemProfileTableItem must
     * have 'choices' defined.
     */
    fun createTask(profileItem: ProfileItemProfileTableItem, profileDataItem: ProfileDataItem) : TaskModel {
        val taskModel = TaskModel()
        taskModel.type = "Survey"
        taskModel.identifier = "ProfileTempTask"
        taskModel.name = profileItem.profileItemKey
        taskModel.guid = UUID.randomUUID().toString()

        val step = TaskModel.StepModel()
        step.type = "SurveyQuestion"
        step.identifier = profileItem.profileItemKey
        step.guid = UUID.randomUUID().toString()
        step.prompt = profileItem.title
        step.uiHint = "list"
        step.constraints = TaskModel.ConstraintsModel()
        step.constraints.dataType = profileDataItem.itemType
        step.constraints.allowMultiple = false
        step.constraints.enumeration = profileItem.valueMap?.map { getChoice(it.value, it.key) }

        taskModel.elements = listOf(step)
        return taskModel
    }

    private fun getChoice(label: String, value: String) : TaskModel.EnumerationModel {
        val choice = TaskModel.EnumerationModel()
        choice.type = "String"
        choice.label = label
        choice.value = value
        return choice
    }

}

