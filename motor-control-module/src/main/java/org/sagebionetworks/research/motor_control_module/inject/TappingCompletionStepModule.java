package org.sagebionetworks.research.motor_control_module.inject;

import org.sagebionetworks.research.domain.inject.StepModule.StepClassKey;
import org.sagebionetworks.research.mobile_ui.inject.ShowStepFragmentModule.ShowStepFragmentFactory;
import org.sagebionetworks.research.mobile_ui.inject.ShowStepFragmentModule.StepViewKey;
import org.sagebionetworks.research.motor_control_module.inject.subcomponents.ShowTappingCompletionStepFragmentSubcomponent;
import org.sagebionetworks.research.motor_control_module.show_step_fragment.ShowTappingCompletionStepFragment;
import org.sagebionetworks.research.motor_control_module.step.AppStepType;
import org.sagebionetworks.research.motor_control_module.step.TappingCompletionStep;
import org.sagebionetworks.research.motor_control_module.step_view.TappingCompletionStepView;
import org.sagebionetworks.research.motor_control_module.step_view.TappingStepView;
import org.sagebionetworks.research.presentation.inject.ShowStepViewModelModule.StepViewClassKey;
import org.sagebionetworks.research.presentation.inject.StepViewModule.InternalStepViewFactory;
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

@Module(subcomponents = ShowTappingCompletionStepFragmentSubcomponent.class)
public abstract class TappingCompletionStepModule {
    @Provides
    @IntoMap
    @StepViewKey(TappingCompletionStepView.TYPE)
    static ShowStepFragmentFactory provideTappingCompletionStepFragmentFactory() {
        return ShowTappingCompletionStepFragment::newInstance;
    }

    @Binds
    @IntoMap
    @FragmentKey(ShowTappingCompletionStepFragment.class)
    abstract AndroidInjector.Factory<? extends android.support.v4.app.Fragment>
    bindShowTappingCompletionStepFragmentInjectoryactory(ShowTappingCompletionStepFragmentSubcomponent.Builder builder);

    @Provides
    @IntoMap
    @StepViewClassKey(TappingCompletionStepView.TYPE)
    static AbstractShowStepViewModelFactory<?, ? extends StepView> provideTappingCompletionStepVMF() {
        return new ShowUIStepViewModelFactory<TappingStepView>();
    }

    @Provides
    @IntoMap
    @StepClassKey(TappingCompletionStep.class)
    static String provideTappingCompletionStepTypeKey() {
        return TappingCompletionStep.TYPE_KEY;
    }

    @Provides
    @IntoMap
    @StepTypeKey(AppStepType.TAPPING_COMPLETION)
    static InternalStepViewFactory provideTappingCompletionStepViewFactory() {
        return TappingCompletionStepView::fromTappingCompletionStep;
    }
}
