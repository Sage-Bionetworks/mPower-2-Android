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

package org.sagebionetworks.research.mpower;

import android.content.Intent;

import org.sagebionetworks.bridge.android.access.BridgeAccessFragment;
import org.sagebionetworks.bridge.android.manager.AuthenticationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class EntryFragment extends BridgeAccessFragment {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntryFragment.class);

    @Inject
    AuthenticationManager authenticationManager;

    @Override
    public void onRequireAuthentication() {
        LOGGER.debug("Showing MpPhoneAuthActivity");

        startActivity(new Intent(getContext(), MpPhoneAuthActivity.class));
    }

    @Override
    public void onRequireConsent() {
        LOGGER.debug("Showing WebConsentFragment");

        getChildFragmentManager().beginTransaction()
                .replace(R.id.container, new WebConsentFragment())
                .commit();
    }

    @Override
    public void onAccessGranted() {
        LOGGER.debug("Showing MainFragment");

        // requires participants to have clinical_consent data group
        // web consent fragment itself uses a feature flag to determine where participants are sent
        if (getResources().getBoolean(R.bool.require_clinical_consent)
                && !authenticationManager.getUserSessionInfo()
                .getDataGroups().contains("clinical_consent")) {
            LOGGER.info("clinical_consent data group required, even for consented users");
            onRequireConsent();
            return;
        }

        getChildFragmentManager().beginTransaction()
                .replace(R.id.container, new MainFragment())
                .commit();
    }
}
