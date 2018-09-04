package org.sagebionetworks.research.motor_control_module.inject;

import dagger.Module;

@Module(includes = {InstructionStepModule.class, HandSelectionStepModule.class, MPowerActiveStepModule.class,
        OverviewStepModule.class, TappingCompletionStepModule.class, TappingStepModule.class})
public class MotorControlStepModule {
}
