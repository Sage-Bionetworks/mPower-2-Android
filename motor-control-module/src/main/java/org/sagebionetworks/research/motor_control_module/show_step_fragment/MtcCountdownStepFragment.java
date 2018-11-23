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

package org.sagebionetworks.research.motor_control_module.show_step_fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import org.sagebionetworks.research.mobile_ui.show_step.view.ShowCountdownStepFragment;
import org.sagebionetworks.research.mobile_ui.show_step.view.ShowStepFragmentBase;
import org.sagebionetworks.research.motor_control_module.step.HandStepHelper;
import org.sagebionetworks.research.motor_control_module.step.HandStepHelper.Hand;
import org.sagebionetworks.research.presentation.model.interfaces.CountdownStepView;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;

public class MtcCountdownStepFragment extends ShowCountdownStepFragment {

    @NonNull
    public static MtcCountdownStepFragment newInstance(@NonNull StepView stepView) {
        if (!(stepView instanceof CountdownStepView)) {
            throw new IllegalArgumentException("Step view: " + stepView + " is not a CountdownStepView.");
        }

        MtcCountdownStepFragment fragment = new MtcCountdownStepFragment();
        Bundle arguments = ShowStepFragmentBase.createArguments(stepView);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    protected void update(CountdownStepView stepView) {
        super.update(stepView);
        updateImage();
    }

    private void updateImage() {
        // If we are testing the right hand, the image will be shown always as left,
        // So we must flip it on the x-axis for it to appear as the right hand
        Hand hand = HandStepHelper.whichHand(stepView.getIdentifier());
        if (hand != null) {
            ImageView imageView = this.stepViewBinding.getImageView();
            if (imageView != null) {
                if (hand == Hand.RIGHT) {
                    imageView.setScaleX(-1);
                }
            }
        }
    }
}
