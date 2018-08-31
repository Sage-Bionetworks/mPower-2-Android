package org.sagebionetworks.research.motor_control_module.inject;

import org.sagebionetworks.research.domain.inject.StepModule.StepClassKey;
import org.sagebionetworks.research.mobile_ui.inject.ShowStepModule.ShowStepFragmentFactory;
import org.sagebionetworks.research.mobile_ui.inject.ShowStepModule.StepViewKey;
import org.sagebionetworks.research.motor_control_module.show_step_fragment.hand_selection.ShowHandSelectionStepFragment;
import org.sagebionetworks.research.motor_control_module.step.AppStepType;
import org.sagebionetworks.research.motor_control_module.step.HandSelectionStep;
import org.sagebionetworks.research.motor_control_module.step_view.HandSelectionStepView;
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
public abstract class HandSelectionStepModule {
    @Provides
    @IntoMap
    @StepViewKey(HandSelectionStepView.TYPE)
    static ShowStepFragmentFactory provideHandSelectionStepFragmentFactory() {
        return ShowHandSelectionStepFragment::newInstance;
    }

    @Provides
    @IntoMap
    @StepClassKey(HandSelectionStep.class)
    static String provideHandSelectedStepTypeKey() {
        return HandSelectionStep.TYPE_KEY;
    }

    @Provides
    @IntoMap
    @StepTypeKey(AppStepType.HAND_SELECTION)
    static StepViewModule.InternalStepViewFactory provideHandSelectionStepViewFactory() {
        return HandSelectionStepView::fromHandSelectionStep;
    }

    @Provides
    @IntoMap
    @StepViewClassKey(HandSelectionStepView.TYPE)
    static ShowStepViewModelFactory<?, ? extends StepView> provideHandSelectionStepVMF() {
        return new ShowUIStepViewModelFactory<HandSelectionStepView>();
    }
}
