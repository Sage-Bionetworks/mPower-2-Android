package org.sagebionetworks.research.motor_control_module.inject;

import com.google.gson.TypeAdapterFactory;

import org.sagebionetworks.research.domain.inject.StepModule.StepClassKey;
import org.sagebionetworks.research.domain.step.StepType;
import org.sagebionetworks.research.mobile_ui.inject.ShowStepFragmentModule;
import org.sagebionetworks.research.motor_control_module.MotorControlAutoValueTypeAdapterFactory;
import org.sagebionetworks.research.motor_control_module.inject.subcomponents.ShowInstructionStepFragmentSubcomponent;
import org.sagebionetworks.research.motor_control_module.show_step_fragment.ShowInstructionStepFragment;
import org.sagebionetworks.research.motor_control_module.step.InstructionStep;
import org.sagebionetworks.research.motor_control_module.step_view.InstructionStepView;
import org.sagebionetworks.research.presentation.inject.ShowStepViewModelModule.StepViewClassKey;
import org.sagebionetworks.research.presentation.inject.StepViewModule;
import org.sagebionetworks.research.presentation.inject.StepViewModule.StepTypeKey;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;
import org.sagebionetworks.research.presentation.show_step.show_step_view_model_factories.AbstractShowStepViewModelFactory;
import org.sagebionetworks.research.presentation.show_step.show_step_view_model_factories.ShowUIStepViewModelFactory;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.android.AndroidInjector;
import dagger.android.support.FragmentKey;
import dagger.multibindings.IntoMap;
import dagger.multibindings.IntoSet;

@Module(subcomponents = {ShowInstructionStepFragmentSubcomponent.class})
public abstract class InstructionStepModule {
    @Binds
    @IntoMap
    @FragmentKey(ShowInstructionStepFragment.class)
    abstract AndroidInjector.Factory<? extends android.support.v4.app.Fragment>
    bindShowInstructionStepFragmentInjectoryFactory(ShowInstructionStepFragmentSubcomponent.Builder builder);

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
    static AbstractShowStepViewModelFactory<?, ? extends StepView> provideInstructionStepVMF() {
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
    @ShowStepFragmentModule.StepViewKey(InstructionStepView.TYPE)
    static ShowStepFragmentModule.ShowStepFragmentFactory provideShowInstructionStepFragmentFactory() {
        return ShowInstructionStepFragment::newInstance;
    }
}