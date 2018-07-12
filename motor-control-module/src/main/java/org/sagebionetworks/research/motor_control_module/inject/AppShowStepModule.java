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

import dagger.Binds;
import dagger.Module;
import dagger.android.AndroidInjector;
import dagger.android.support.FragmentKey;
import dagger.multibindings.IntoMap;
import org.sagebionetworks.research.motor_control_module.inject.subcomponents.*;
import org.sagebionetworks.research.motor_control_module.show_step_fragment.hand_selection.ShowHandSelectionStepFragment;
import org.sagebionetworks.research.motor_control_module.show_step_fragment.ShowInstructionStepFragment;
import org.sagebionetworks.research.motor_control_module.show_step_fragment.ShowOverviewStepFragment;

@Module(subcomponents = {ShowInstructionStepFragmentSubcomponent.class, ShowHandSelectionStepFragmentSubcomponent.class,
                ShowOverviewStepFragmentSubcomponent.class},
        includes = {AppStepModule.class, AppStepViewModule.class, AppShowStepViewModelModule.class,
                AppShowStepFragmentModule.class})
public abstract class AppShowStepModule {
    @Binds
    @IntoMap
    @FragmentKey(ShowInstructionStepFragment.class)
    abstract AndroidInjector.Factory<? extends android.support.v4.app.Fragment>
    bindShowInstructionStepFragmentInjectoryFactory(ShowInstructionStepFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(ShowOverviewStepFragment.class)
    abstract AndroidInjector.Factory<? extends android.support.v4.app.Fragment>
    bindShowOverviewStepFragmentInjectoryFactory(ShowOverviewStepFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(ShowHandSelectionStepFragment.class)
    abstract AndroidInjector.Factory<? extends android.support.v4.app.Fragment>
    bindShowHandSelectionStepFragmentInjectoryFactory(ShowHandSelectionStepFragmentSubcomponent.Builder builder);
}
