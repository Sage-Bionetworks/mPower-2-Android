/*
 * BSD 3-Clause License
 *
 * Copyright 2018  Sage Bionetworks. All rights reserved.
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

package org.sagebionetworks.research.mpower

import android.content.Intent
import org.sagebionetworks.bridge.android.access.BridgeAccessFragment
import org.sagebionetworks.bridge.android.manager.AuthenticationManager
import org.sagebionetworks.research.mpower.authentication.IntroductionActivity
import org.slf4j.LoggerFactory
import javax.inject.Inject

class EntryFragment : BridgeAccessFragment() {

    @Inject
    lateinit var authenticationManager: AuthenticationManager

    override fun onRequireAuthentication() {
        LOGGER.debug("Showing MpPhoneAuthActivity")

        startActivity(Intent(context, IntroductionActivity::class.java))
        activity?.finish()
    }

    override fun onRequireConsent() {
        LOGGER.debug("Showing WebConsentFragment")

        childFragmentManager.beginTransaction()
                .replace(R.id.container, WebConsentFragment())
                .commit()
    }

    override fun onAccessGranted() {
        LOGGER.debug("Showing MainFragment")

        if ("clinical" == BuildConfig.FLAVOR &&
                isMissingClinicalConsent()) {
            LOGGER.info("clinical_consent data group required, even for consented users")
            onRequireConsent()
            return
        }
        if (childFragmentManager.fragments.isEmpty() || !childFragmentManager.fragments[0].isVisible || !(childFragmentManager.fragments[0] is MainFragment)) {
            childFragmentManager.beginTransaction()
                    .replace(R.id.container, MainFragment())
                    .commit()
        }
    }

    /**
     * Requires participants to have clinical_consent data group
     * Web consent fragment itself uses a feature flag to determine where participants are sent
     */
    fun isMissingClinicalConsent(): Boolean {
        return resources.getBoolean(R.bool.require_clinical_consent)
                && !(authenticationManager.userSessionInfo?.dataGroups ?: listOf()).contains("clinical_consent")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Due to a behavior issue in nested child fragments
        // We must call the onActivityResult on all the children
        for (fragment in childFragmentManager.fragments) {
            LOGGER.info("Calling onActivityResult for fragment " + fragment.id)
            fragment.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(EntryFragment::class.java)
    }
}
