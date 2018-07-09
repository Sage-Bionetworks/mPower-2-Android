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

package org.sagebionetworks.research.motor_control_module.inject;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import org.sagebionetworks.research.mobile_ui.inject.ShowStepFragmentModule;
import org.sagebionetworks.research.mobile_ui.inject.ShowStepFragmentModule.ShowStepFragmentFactory;
import org.sagebionetworks.research.mobile_ui.inject.ShowStepFragmentModule.StepViewKey;
import org.sagebionetworks.research.motor_control_module.show_step_fragment.hand_selection.ShowHandSelectionStepFragment;
import org.sagebionetworks.research.motor_control_module.show_step_fragment.ShowInstructionStepFragment;
import org.sagebionetworks.research.motor_control_module.show_step_fragment.ShowOverviewStepFragment;
import org.sagebionetworks.research.motor_control_module.step_view.HandSelectionStepView;
import org.sagebionetworks.research.motor_control_module.step_view.InstructionStepView;
import org.sagebionetworks.research.motor_control_module.step_view.OverviewStepView;

@Module(includes = {ShowStepFragmentModule.class})
public class AppShowStepFragmentModule {
    @Provides
    @IntoMap
    @StepViewKey(InstructionStepView.TYPE)
    static ShowStepFragmentFactory provideShowInstructionStepFragmentFactory() {
        return ShowInstructionStepFragment::newInstance;
    }

    @Provides
    @IntoMap
    @StepViewKey(OverviewStepView.TYPE)
    static ShowStepFragmentFactory provideShowOverviewStepFragmentFactory() {
        return ShowOverviewStepFragment::newInstance;
    }

    @Provides
    @IntoMap
    @StepViewKey(HandSelectionStepView.TYPE)
    static ShowStepFragmentFactory provideHandSelectionStepFragmentFactory() {
        return ShowHandSelectionStepFragment::newInstance;
    }
}
