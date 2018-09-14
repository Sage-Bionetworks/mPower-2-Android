package org.sagebionetworks.research.motor_control_module.inject;

import org.sagebionetworks.research.domain.inject.StepModule.StepClassKey;
import org.sagebionetworks.research.domain.step.StepType;
import org.sagebionetworks.research.mobile_ui.inject.ShowStepModule;
import org.sagebionetworks.research.motor_control_module.show_step_fragment.ShowCompletionStepFragment;
import org.sagebionetworks.research.motor_control_module.step.CompletionStep;
import org.sagebionetworks.research.motor_control_module.step_view.CompletionStepView;
import org.sagebionetworks.research.presentation.inject.ShowStepViewModelModule.StepViewClassKey;
import org.sagebionetworks.research.presentation.inject.StepViewModule;
import org.sagebionetworks.research.presentation.inject.StepViewModule.StepTypeKey;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;
import org.sagebionetworks.research.presentation.show_step.show_step_view_model_factories.ShowStepViewModelFactory;
import org.sagebionetworks.research.presentation.show_step.show_step_view_model_factories.ShowUIStepViewModelFactory;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;

@Module
public class CompletionStepModule {
    /**
     * Returns the type key for InstructionStep.class.
     *
     * @return the type key for InstructionStep.class.
     */
    @Provides
    @IntoMap
    @StepClassKey(CompletionStep.class)
    static String provideCompletionStepClassInfo() {
        return CompletionStep.TYPE_KEY;
    }

    @Provides
    @IntoMap
    @StepViewClassKey(CompletionStepView.TYPE)
    static ShowStepViewModelFactory<?, ? extends StepView> provideCompletionStepVMF() {
        return new ShowUIStepViewModelFactory<CompletionStepView>();
    }

    @Provides
    @IntoMap
    @StepTypeKey(StepType.COMPLETION)
    static StepViewModule.InternalStepViewFactory provideCompletionStepViewFactory() {
        return CompletionStepView::fromCompletionStep;
    }

    @Provides
    @IntoMap
    @ShowStepModule.StepViewKey(CompletionStepView.TYPE)
    static ShowStepModule.ShowStepFragmentFactory provideShowCompletionStepFragmentFactory() {
        return ShowCompletionStepFragment::newInstance;
    }
}
