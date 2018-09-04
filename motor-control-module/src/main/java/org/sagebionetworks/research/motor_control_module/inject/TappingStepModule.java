package org.sagebionetworks.research.motor_control_module.inject;

import org.sagebionetworks.research.domain.inject.StepModule.StepClassKey;
import org.sagebionetworks.research.mobile_ui.inject.ShowStepModule.ShowStepFragmentFactory;
import org.sagebionetworks.research.mobile_ui.inject.ShowStepModule.StepViewKey;
import org.sagebionetworks.research.motor_control_module.show_step_fragment.tapping.ShowTappingStepFragment;
import org.sagebionetworks.research.motor_control_module.show_step_view_model.ShowTappingStepViewModelFactory;
import org.sagebionetworks.research.motor_control_module.step.AppStepType;
import org.sagebionetworks.research.motor_control_module.step.TappingStep;
import org.sagebionetworks.research.motor_control_module.step_view.TappingStepView;
import org.sagebionetworks.research.presentation.inject.ShowStepViewModelModule.StepViewClassKey;
import org.sagebionetworks.research.presentation.inject.StepViewModule.InternalStepViewFactory;
import org.sagebionetworks.research.presentation.inject.StepViewModule.StepTypeKey;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;
import org.sagebionetworks.research.presentation.show_step.show_step_view_model_factories.ShowStepViewModelFactory;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;

@Module
public abstract class TappingStepModule {
    @Provides
    @IntoMap
    @StepViewKey(TappingStepView.TYPE)
    static ShowStepFragmentFactory provideTappingStepFragmentFactory() {
        return ShowTappingStepFragment::newInstance;
    }

    @Provides
    @IntoMap
    @StepViewClassKey(TappingStepView.TYPE)
    static ShowStepViewModelFactory<?, ? extends StepView> provideTappingStepVMF() {
        return new ShowTappingStepViewModelFactory();
    }

    @Provides
    @IntoMap
    @StepClassKey(TappingStep.class)
    static String provideTappingStepTypeKey() {
        return TappingStep.TYPE_KEY;
    }

    @Provides
    @IntoMap
    @StepTypeKey(AppStepType.TAPPING)
    static InternalStepViewFactory provideTappingStepViewFactory() {
        return TappingStepView::fromTappingStep;
    }
}
