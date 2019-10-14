package org.sagebionetworks.research.mpower.profile

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.google.common.base.Preconditions
import org.sagebionetworks.research.mpower.researchstack.framework.step.MpSmartSurveyTask
import org.sagebionetworks.research.sageresearch.dao.room.AppConfigRepository
import org.sagebionetworks.research.sageresearch.dao.room.ReportRepository
import org.sagebionetworks.research.sageresearch.dao.room.SurveyRepository
import org.sagebionetworks.research.sageresearch.profile.ProfileViewModel
import org.sagebionetworks.research.sageresearch.repos.BridgeRepositoryManager
import javax.inject.Inject


class MPowerProfileViewModel(bridgeRepoManager: BridgeRepositoryManager,
                                  reportRepo: ReportRepository,
                                  appConfigRepo: AppConfigRepository,
                                  surveyRepo: SurveyRepository): ProfileViewModel(bridgeRepoManager, reportRepo, appConfigRepo, surveyRepo) {

    class Factory @Inject constructor(private val bridgeRepoManager: BridgeRepositoryManager, private val reportRepo: ReportRepository, private val appConfigRepo: AppConfigRepository, private val surveyRepo: SurveyRepository): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            Preconditions.checkArgument(modelClass.isAssignableFrom(MPowerProfileViewModel::class.java))
            return MPowerProfileViewModel(bridgeRepoManager, reportRepo, appConfigRepo, surveyRepo) as T
        }
    }

    var currentSurveyTask: MpSmartSurveyTask? = null;

}

