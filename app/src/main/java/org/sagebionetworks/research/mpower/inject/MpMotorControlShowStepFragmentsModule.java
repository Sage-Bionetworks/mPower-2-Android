package org.sagebionetworks.research.mpower.inject;

import org.sagebionetworks.research.mobile_ui.inject.ShowStepFragmentScope;
import org.sagebionetworks.research.modules.motor_control.inject.MotorControlShowStepFragmentsModule;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class MpMotorControlShowStepFragmentsModule extends MotorControlShowStepFragmentsModule {
    @ContributesAndroidInjector
    @ShowStepFragmentScope
    abstract MpShowOverviewStepFragment contributeMpOverviewStepFragmentInjector();
}
