/*
 * BSD 3-Clause License
 *
 * Copyright 2020  Sage Bionetworks. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1.  Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2.  Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * 3.  Neither the name of the copyright holder(s) nor the names of any contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission. No license is granted to the trademarks of
 * the copyright holders even if such marks are included in this software.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.sagebionetworks.research.mpower.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.RadioButton
import androidx.lifecycle.ViewModelProvider
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_passive_gait_permission.*
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

class PassiveGaitPermissionActivity : DaggerAppCompatActivity() {

    private val LOGGER = LoggerFactory.getLogger(PassiveGaitPermissionActivity::class.java)

    private var disposable: Disposable? = null
    private var compositeSubscription = CompositeSubscription()

    @Inject
    lateinit var viewModelFactory: PassiveGaitPermissionViewModel.Factory
    private lateinit var viewModel: PassiveGaitPermissionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_passive_gait_permission)
        viewModel = ViewModelProvider(this, viewModelFactory).get(PassiveGaitPermissionViewModel::class.java)

        back_icon.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
    }

    fun showLoading(show: Boolean) {
        Handler(Looper.getMainLooper()).post {spinner.visibility = if (show) View.VISIBLE else View.GONE }
    }

    fun onRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            // Is the button now checked?
            val checked = view.isChecked

            // Check which radio button was clicked
            when (view.getId()) {
                R.id.radio_okay ->
                    if (checked) {
                        // TODO: Activate Passive Gait
                    }
                R.id.radio_not_now ->
                    if (checked) {
                        // TODO: Disable Passive Gait
                    }
            }
        }
    }

}
