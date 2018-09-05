package org.sagebionetworks.research.motor_control_module.inject;

import com.google.gson.TypeAdapterFactory;

import org.sagebionetworks.research.domain.inject.StepModule.StepClassKey;
import org.sagebionetworks.research.domain.step.StepType;
import org.sagebionetworks.research.mobile_ui.inject.ShowStepModule;
import org.sagebionetworks.research.motor_control_module.MotorControlAutoValueTypeAdapterFactory;
import org.sagebionetworks.research.motor_control_module.show_step_fragment.ShowInstructionStepFragment;
import org.sagebionetworks.research.motor_control_module.step.InstructionStep;
import org.sagebionetworks.research.motor_control_module.step_view.InstructionStepView;
import org.sagebionetworks.research.presentation.inject.ShowStepViewModelModule.StepViewClassKey;
import org.sagebionetworks.research.presentation.inject.StepViewModule;
import org.sagebionetworks.research.presentation.inject.StepViewModule.StepTypeKey;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;
import org.sagebionetworks.research.presentation.show_step.show_step_view_model_factories.ShowStepViewModelFactory;
import org.sagebionetworks.research.presentation.show_step.show_step_view_model_factories.ShowUIStepViewModelFactory;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.IntoSet;

@Module
public abstract class InstructionStepModule {
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

    @Provides
    @IntoMap
    @StepViewClassKey(InstructionStepView.TYPE)
    static ShowStepViewModelFactory<?, ? extends StepView> provideInstructionStepVMF() {
        return new ShowUIStepViewModelFactory<InstructionStepView>();
    }

    @Provides
    @IntoMap
    @StepTypeKey(StepType.INSTRUCTION)
    static StepViewModule.InternalStepViewFactory provideInstructionStepViewFactory() {
        return InstructionStepView::fromInstructionStep;
    }

    @Provides
    @IntoSet
    static TypeAdapterFactory provideMotorControlAutoValueTypeAdapterFactory() {
        return MotorControlAutoValueTypeAdapterFactory.create();
    }

    @Provides
    @IntoMap
    @ShowStepModule.StepViewKey(InstructionStepView.TYPE)
    static ShowStepModule.ShowStepFragmentFactory provideShowInstructionStepFragmentFactory() {
        return ShowInstructionStepFragment::newInstance;
    }
}