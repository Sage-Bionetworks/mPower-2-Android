package org.sagebionetworks.research.mpower.inject;

import org.sagebionetworks.research.mobile_ui.inject.PerformTaskFragmentScope;
import org.sagebionetworks.research.mobile_ui.inject.ShowStepFragmentModule;
import org.sagebionetworks.research.mobile_ui.perform_task.PerformTaskFragment;
import org.sagebionetworks.research.motor_control_module.inject.MotorControlShowStepFragmentsModule;

import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@PerformTaskFragmentScope
@Subcomponent(modules = {ShowStepFragmentModule.class, MotorControlShowStepFragmentsModule.class})
public abstract class AppTaskActivityFragmentSubcomponent implements AndroidInjector<PerformTaskFragment> {

    @Subcomponent.Builder
    public abstract static class Builder extends AndroidInjector.Builder<PerformTaskFragment> {
        @Override
        public abstract AppTaskActivityFragmentSubcomponent build();
    }
}
