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

package org.sagebionetworks.research.mpower.sageresearch.inject;

import org.sagebionetworks.research.domain.inject.ActionModule;
import org.sagebionetworks.research.domain.inject.AsyncActionModule;
import org.sagebionetworks.research.domain.inject.InputFieldsModule;
import org.sagebionetworks.research.domain.inject.StepModule;
import org.sagebionetworks.research.mobile_ui.inject.PerformTaskModule;
import org.sagebionetworks.research.mobile_ui.inject.ShowStepModule;
import org.sagebionetworks.research.mobile_ui.perform_task.PerformTaskActivity;
import org.sagebionetworks.research.motor_control_module.inject.HandSelectionStepModule;
import org.sagebionetworks.research.motor_control_module.inject.InstructionStepModule;
import org.sagebionetworks.research.motor_control_module.inject.MPowerActiveStepModule;
import org.sagebionetworks.research.motor_control_module.inject.OverviewStepModule;
import org.sagebionetworks.research.motor_control_module.inject.TappingCompletionStepModule;
import org.sagebionetworks.research.motor_control_module.inject.TappingStepModule;

import dagger.Module;
import dagger.android.AndroidInjectionModule;
import dagger.android.ContributesAndroidInjector;

@Module(includes = {AndroidInjectionModule.class, ShowStepModule.class, InputFieldsModule.class, ActionModule.class,
        StepModule.class, AsyncActionModule.class, PerformTaskModule.class, InstructionStepModule.class,
        OverviewStepModule.class, MPowerActiveStepModule.class,
        TappingCompletionStepModule.class, TappingStepModule.class, HandSelectionStepModule.class})
public abstract class MPowerSageResearchModule {
    @ContributesAndroidInjector
    abstract PerformTaskActivity contributePerformTaskActivityInjector();

}