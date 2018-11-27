package org.sagebionetworks.research.motor_control_module.inject;

import org.sagebionetworks.research.motor_control_module.step.CompletionStep;

import dagger.Module;

@Module(includes = {InstructionStepModule.class, CompletionStepModule.class, HandSelectionStepModule.class, MPowerActiveStepModule.class,
        OverviewStepModule.class, TappingCompletionStepModule.class, TappingStepModule.class, MtcCountdownStepModule.class})
public class MotorControlStepModule {
}
