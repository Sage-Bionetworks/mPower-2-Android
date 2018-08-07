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

import org.sagebionetworks.research.domain.inject.InputFieldsModule;
import org.sagebionetworks.research.domain.inject.StepModule;
import org.sagebionetworks.research.domain.inject.StepModule.StepClassKey;
import org.sagebionetworks.research.motor_control_module.step.HandSelectionStep;
import org.sagebionetworks.research.motor_control_module.step.InstructionStep;
import org.sagebionetworks.research.motor_control_module.step.MPowerActiveUIStep;
import org.sagebionetworks.research.motor_control_module.step.OverviewStep;
import org.sagebionetworks.research.motor_control_module.step.TappingCompletionStep;
import org.sagebionetworks.research.motor_control_module.step.TappingStep;
import org.sagebionetworks.research.presentation.inject.ShowStepViewModelModule;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;

/**
 * Add app-specific steps.
 */
@Module(includes = {InputFieldsModule.class, StepModule.class})
public class AppStepModule {
    /**
     * Returns the type key for InstructionStep.class.
     *
     * @return the type key for InstructionStep.class.
     */
    @Provides
    @IntoMap
    @StepClassKey(InstructionStep.class)
    static String provideInstructionStepClassInfo() {
        return InstructionStep.TYPE_KEY;
    }

    /**
     * Returns the type key for OverviewStep.class.
     *
     * @return the type key for OverviewStep.class.
     */
    @Provides
    @IntoMap
    @StepClassKey(OverviewStep.class)
    static String provideOverviewStepTypeKey() {
        return OverviewStep.TYPE_KEY;
    }

    @Provides
    @IntoMap
    @StepClassKey(HandSelectionStep.class)
    static String provideHandSelectedStepTypeKey() {
        return HandSelectionStep.TYPE_KEY;
    }

    @Provides
    @IntoMap
    @StepClassKey(MPowerActiveUIStep.class)
    static String provideMPowerActiveUIStepTypeKey() {
        return MPowerActiveUIStep.TYPE_KEY;
    }

    @Provides
    @IntoMap
    @StepClassKey(TappingStep.class)
    static String provideTappingStepTypeKey() {
        return TappingStep.TYPE_KEY;
    }

    @Provides
    @IntoMap
    @StepClassKey(TappingCompletionStep.class)
    static String provideTappingCompletionStepTypeKey() {
        return TappingCompletionStep.TYPE_KEY;
    }
}
