package org.sagebionetworks.research.mpower.profile

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.google.common.base.Preconditions
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import org.sagebionetworks.bridge.rest.model.Survey
import org.sagebionetworks.research.sageresearch.dao.room.AppConfigRepository
import org.sagebionetworks.research.sageresearch.dao.room.SurveyRepository
import javax.inject.Inject

class WithdrawFromStudyViewModel(val surveyRepo: SurveyRepository, val appConfigRepo: AppConfigRepository): ViewModel() {


    class Factory @Inject constructor(
            private val surveyRepo: SurveyRepository,
            private val appConfigRepo: AppConfigRepository) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            Preconditions.checkArgument(modelClass.isAssignableFrom(WithdrawFromStudyViewModel::class.java))

            return WithdrawFromStudyViewModel(surveyRepo, appConfigRepo) as T
        }
    }


    private val compositeDisposable = CompositeDisposable()

    fun loadWithdrawSurvey(): Single<Survey> {
        return appConfigRepo.getSurveyReference("Withdrawal").flatMap{surveyRepo.getSurvey(it) }
    }

}