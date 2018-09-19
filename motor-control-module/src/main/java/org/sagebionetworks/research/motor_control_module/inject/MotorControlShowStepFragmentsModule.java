package org.sagebionetworks.research.motor_control_module.inject;

import org.sagebionetworks.research.motor_control_module.show_step_fragment.ShowCompletionStepFragment;
import org.sagebionetworks.research.motor_control_module.show_step_fragment.ShowInstructionStepFragment;
import org.sagebionetworks.research.motor_control_module.show_step_fragment.ShowMPowerActiveUIStepFragment;
import org.sagebionetworks.research.motor_control_module.show_step_fragment.ShowOverviewStepFragment;
import org.sagebionetworks.research.motor_control_module.show_step_fragment.ShowTappingCompletionStepFragment;
import org.sagebionetworks.research.motor_control_module.show_step_fragment.hand_selection.ShowHandSelectionStepFragment;
import org.sagebionetworks.research.motor_control_module.show_step_fragment.tapping.ShowTappingStepFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class MotorControlShowStepFragmentsModule {
    @ContributesAndroidInjector
    abstract ShowHandSelectionStepFragment contributeShowHandSelectionStepFragmentInjector();

    @ContributesAndroidInjector
    abstract ShowInstructionStepFragment contributeShowInstructionStepFragmentInjector();

    @ContributesAndroidInjector
    abstract ShowMPowerActiveUIStepFragment contributeShowMPowerActiveUIStepFragmentInjector();

    @ContributesAndroidInjector
    abstract ShowOverviewStepFragment contributeShowOverviewStepFragmentInjector();

    @ContributesAndroidInjector
    abstract ShowTappingCompletionStepFragment contributeShowTappingCompletionStepFragmentInjector();

    @ContributesAndroidInjector
    abstract ShowTappingStepFragment contributeShowTappingStepFragmentInjector();

    @ContributesAndroidInjector
    abstract ShowCompletionStepFragment contributeShowCompletionStepFragmentInjector();
}
