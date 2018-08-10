package org.sagebionetworks.research.mpower.signup

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import dagger.android.AndroidInjection
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.AndroidSupportInjectionModule
import kotlinx.android.synthetic.main.activity_external_id_registration.*
import org.researchstack.backbone.model.User
import org.sagebionetworks.research.mpower.R

//
//  ExternalIDRegistrationActivity.kt
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

class ExternalIDRegistrationActivity(
        alertPresenter: ResearchAlertPresenter = ResearchAlertPresenter()) :
        AppCompatActivity(), TextWatcher, AlertPresenter by alertPresenter {

    // We create the viewModel using lazy delegate.
    // That’s the defined approach by Google to get the ViewModel.
    private val externalIDViewModel: ExternalIDRegistrationViewModel by lazy {
        ViewModelProviders.of(this).get(ExternalIDRegistrationViewModel::class.java)
    }

    private val viewModelObserver = Observer<Response<User>> {
        when(it?.status) {
            ResponseStatus.LOADING -> showLoading()
            ResponseStatus.SUCCESS -> success(it.data)
            ResponseStatus.ERROR -> error(it.error)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_external_id_registration)

        externalIDViewModel.result.observe(this, viewModelObserver)

        externalIdEditText.addTextChangedListener(this)
        nameEditText.addTextChangedListener(this)
        nextButton.setOnClickListener { nextButtonClicked() }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // no-op see afterTextChanged fun
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // no-op see afterTextChanged fun
    }

    override fun afterTextChanged(s: Editable?) {
        nextButton.isEnabled =
                !TextUtils.isEmpty(externalIdEditText.text) &&
                !TextUtils.isEmpty(nameEditText.text)
    }

    fun showLoading() {
        progressBar.visibility = View.VISIBLE
    }

    fun success(user: User?) {
        progressBar.visibility = View.GONE
        finish()
    }

    fun error(error: ResponseError?) {
        progressBar.visibility = View.GONE
        showErrorAlert(this, error)
    }

    fun nextButtonClicked() {
        externalIDViewModel.signUp(
                externalIdEditText.text.toString(),
                nameEditText.text.toString(),
                preConsentCheckBox.isChecked)
    }
}