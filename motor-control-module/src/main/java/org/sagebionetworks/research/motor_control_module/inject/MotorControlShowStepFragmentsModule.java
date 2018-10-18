package org.sagebionetworks.research.motor_control_module.inject;

import org.sagebionetworks.research.mobile_ui.inject.ShowStepFragmentScope;
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
    @ShowStepFragmentScope
    abstract ShowHandSelectionStepFragment contributeShowHandSelectionStepFragmentInjector();

    @ContributesAndroidInjector
    @ShowStepFragmentScope
    abstract ShowInstructionStepFragment contributeShowInstructionStepFragmentInjector();

    @ContributesAndroidInjector
    @ShowStepFragmentScope
    abstract ShowMPowerActiveUIStepFragment contributeShowMPowerActiveUIStepFragmentInjector();

    @ContributesAndroidInjector
    @ShowStepFragmentScope
    abstract ShowOverviewStepFragment contributeShowOverviewStepFragmentInjector();

    @ContributesAndroidInjector
    @ShowStepFragmentScope
    abstract ShowTappingCompletionStepFragment contributeShowTappingCompletionStepFragmentInjector();

    @ContributesAndroidInjector
    @ShowStepFragmentScope
    abstract ShowTappingStepFragment contributeShowTappingStepFragmentInjector();

    @ContributesAndroidInjector
    @ShowStepFragmentScope
    abstract ShowCompletionStepFragment contributeShowCompletionStepFragmentInjector();
}
