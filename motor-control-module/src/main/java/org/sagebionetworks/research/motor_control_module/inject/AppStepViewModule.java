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
import org.sagebionetworks.research.domain.step.StepType;
import org.sagebionetworks.research.motor_control_module.step.AppStepType;
import org.sagebionetworks.research.motor_control_module.step_view.HandSelectionStepView;
import org.sagebionetworks.research.motor_control_module.step_view.InstructionStepView;
import org.sagebionetworks.research.motor_control_module.step_view.OverviewStepView;
import org.sagebionetworks.research.presentation.inject.StepViewModule;
import org.sagebionetworks.research.presentation.inject.StepViewModule.InternalStepViewFactory;
import org.sagebionetworks.research.presentation.inject.StepViewModule.StepTypeKey;

/**
 * Add app-specific steps.
 */
@Module(includes = StepViewModule.class)
public class AppStepViewModule {
    @Provides
    @IntoMap
    @StepTypeKey(StepType.INSTRUCTION)
    static InternalStepViewFactory provideInstructionStepViewFactory() {
        return InstructionStepView::fromInstructionStep;
    }

    @Provides
    @IntoMap
    @StepTypeKey(StepType.OVERVIEW)
    static InternalStepViewFactory provideOverviewStepViewFactory() {
        return OverviewStepView::fromOverviewStep;
    }

    @Provides
    @IntoMap
    @StepTypeKey(AppStepType.HAND_SELECTION)
    static InternalStepViewFactory provideHandSelectionStepViewFactory() {
        return HandSelectionStepView::fromHandSelectionStep;
    }
}
