package org.sagebionetworks.research.motor_control_module.inject;

import org.sagebionetworks.research.domain.inject.StepModule.StepClassKey;
import org.sagebionetworks.research.domain.step.StepType;
import org.sagebionetworks.research.mobile_ui.inject.ShowStepModule;
import org.sagebionetworks.research.motor_control_module.show_step_fragment.ShowOverviewStepFragment;
import org.sagebionetworks.research.motor_control_module.step.OverviewStep;
import org.sagebionetworks.research.motor_control_module.step_view.OverviewStepView;
import org.sagebionetworks.research.presentation.inject.ShowStepViewModelModule.StepViewClassKey;
import org.sagebionetworks.research.presentation.inject.StepViewModule.InternalStepViewFactory;
import org.sagebionetworks.research.presentation.inject.StepViewModule.StepTypeKey;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;
import org.sagebionetworks.research.presentation.show_step.show_step_view_model_factories.ShowStepViewModelFactory;
import org.sagebionetworks.research.presentation.show_step.show_step_view_model_factories.ShowUIStepViewModelFactory;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;

@Module
public abstract class OverviewStepModule {

    /**
     * Returns the type key for OverviewStep.class.
     *
     * @return the type key for OverviewStep.class.
     */
    @Provides
    @IntoMap
    @StepClassKey(OverviewStep.class)
    static String provideOverviewStepClassInfo() {
        return OverviewStep.TYPE_KEY;
    }

    @Provides
    @IntoMap
    @StepViewClassKey(OverviewStepView.TYPE)
    static ShowStepViewModelFactory<?, ? extends StepView> provideOverviewStepVMF() {
        return new ShowUIStepViewModelFactory<OverviewStepView>();
    }

    @Provides
    @IntoMap
    @StepTypeKey(StepType.OVERVIEW)
    static InternalStepViewFactory provideOverviewStepViewFactory() {
        return OverviewStepView::fromOverviewStep;
    }

    @Provides
    @IntoMap
    @ShowStepModule.StepViewKey(OverviewStepView.TYPE)
    static ShowStepModule.ShowStepFragmentFactory provideShowOverviewStepFragmentFactory() {
        return ShowOverviewStepFragment::newInstance;
    }
}