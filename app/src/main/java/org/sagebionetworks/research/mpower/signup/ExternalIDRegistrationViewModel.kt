package org.sagebionetworks.research.mpower.signup

import android.annotation.SuppressLint
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import org.researchstack.backbone.DataResponse
import org.researchstack.backbone.model.User
import org.sagebionetworks.bridge.rest.model.Message
import org.sagebionetworks.bridge.rest.model.SignUp
import org.sagebionetworks.research.mpower.researchstack.framework.MpDataProvider
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import javax.inject.Inject

//
//  ExternalIDRegistrationViewModel.kt
//  mPower2
//
//  Copyright © 2018 Sage Bionetworks. All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification,
// are permitted provided that the following conditions are met:
//
// 1.  Redistributions of source code must retain the above copyright notice, this
// list of conditions and the following disclaimer.
//
// 2.  Redistributions in binary form must reproduce the above copyright notice,
// this list of conditions and the following disclaimer in the documentation and/or
// other materials provided with the distribution.
//
// 3.  Neither the name of the copyright holder(s) nor the names of any contributors
// may be used to endorse or promote products derived from this software without
// specific prior written permission. No license is granted to the trademarks of
// the copyright holders even if such marks are included in this software.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//

/**
 * The ExternalIDRegistrationViewModel encapsulates data operations to
 * sign up and user and sign them in using only an external ID
 */
open class ExternalIDRegistrationViewModel: ViewModel() {

    private val compositeSubscription = CompositeSubscription()

    val dataProvider: MpDataProvider = MpDataProvider.getInstance()

    val result: MutableLiveData<Response<User>> = MutableLiveData()

    open fun signUp(externalID: String, firstName: String, checkForConsent: Boolean) {
        result.postValue(Response.loading())
        val credentials = credentials(externalID, firstName, checkForConsent)
        compositeSubscription.add(dataProvider.signUpStudyParticipant(credentials)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ message: Message ->
                    signIn(externalID)
                }) { throwable: Throwable ->
                    result.postValue(Response.unknownError(throwable.message ?: "Error on sign up"))
                })
    }

    // TODO: mdephillips 8/8/18 what happens when there is an onError call?
    @SuppressLint("RxSubscribeOnError")
    private fun signIn(externalId: String) {
        compositeSubscription.add(dataProvider.signIn(externalId, externalId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val user: User? = MpDataProvider.getInstance().user
                    if (user == null) {
                        result.postValue(Response.unknownError("Null user"))
                    } else {
                        result.postValue(Response.success(user))
                    }
                });
    }

    override fun onCleared() {
        super.onCleared()
        compositeSubscription.unsubscribe()
    }

    private fun credentials(externalID: String, firstName: String, preconsent: Boolean): SignUp {
        val signUp = SignUp()
        signUp.externalId = externalID
        signUp.password = externalID
        signUp.firstName = firstName
        signUp.isCheckForConsent = !preconsent

        // TODO emm 2018-05-03 if we move this code to BridgeApp,
        // we should probably use an RSDCohortRule or
        // some such instead of hardcoding these dataGroup names.
        var dataGroups: Set<String> = HashSet()
        dataGroups.plus("test_user")
        if (preconsent) {
            dataGroups.plus("test_no_consent")
        }
        // Assign the engagement data groups
        val studyBurstManager = StudyBurstScheduleManager()
        val randomEngagementGroup = studyBurstManager.randomEngagementDataGroup()
        if (randomEngagementGroup != null) {
            dataGroups.plus(randomEngagementGroup)
        }
        signUp.dataGroups = dataGroups.toList()

        return signUp
    }
}