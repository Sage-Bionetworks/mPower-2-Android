package org.sagebionetworks.research.motor_control_module.inject;

import org.sagebionetworks.research.domain.inject.StepModule.StepClassKey;
import org.sagebionetworks.research.mobile_ui.inject.ShowStepFragmentModule.ShowStepFragmentFactory;
import org.sagebionetworks.research.mobile_ui.inject.ShowStepFragmentModule.StepViewKey;
import org.sagebionetworks.research.motor_control_module.inject.subcomponents.ShowMPowerActiveUIStepFragmentSubcomponent;
import org.sagebionetworks.research.motor_control_module.show_step_fragment.ShowMPowerActiveUIStepFragment;
import org.sagebionetworks.research.motor_control_module.step.AppStepType;
import org.sagebionetworks.research.motor_control_module.step.MPowerActiveUIStep;
import org.sagebionetworks.research.motor_control_module.step_view.MPowerActiveUIStepView;
import org.sagebionetworks.research.presentation.inject.ShowStepViewModelModule.StepViewClassKey;
import org.sagebionetworks.research.presentation.inject.StepViewModule.InternalStepViewFactory;
import org.sagebionetworks.research.presentation.inject.StepViewModule.StepTypeKey;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;
import org.sagebionetworks.research.presentation.show_step.show_step_view_model_factories.AbstractShowStepViewModelFactory;
import org.sagebionetworks.research.presentation.show_step.show_step_view_model_factories.ShowActiveUIStepViewModelFactory;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.android.AndroidInjector;
import dagger.android.support.FragmentKey;
import dagger.multibindings.IntoMap;

@Module(subcomponents = ShowMPowerActiveUIStepFragmentSubcomponent.class)
public abstract class MPowerActiveStepModule {
    @Provides
    @IntoMap
    @StepViewKey(MPowerActiveUIStepView.TYPE)
    static ShowStepFragmentFactory provideMPowerActiveUIStepFragmentFactory() {
        return ShowMPowerActiveUIStepFragment::newInstance;
    }

    @Binds
    @IntoMap
    @FragmentKey(ShowMPowerActiveUIStepFragment.class)
    abstract AndroidInjector.Factory<? extends android.support.v4.app.Fragment>
    bindShowMPowerActiveUIStepFragmentInjectoryactory(ShowMPowerActiveUIStepFragmentSubcomponent.Builder builder);

    @Provides
    @IntoMap
    @StepViewClassKey(MPowerActiveUIStepView.TYPE)
    static AbstractShowStepViewModelFactory<?, ? extends StepView> provideMPowerActiveUIStepVMF() {
        return new ShowActiveUIStepViewModelFactory<>();
    }

    @Provides
    @IntoMap
    @StepClassKey(MPowerActiveUIStep.class)
    static String provideMPowerActiveUIStepTypeKey() {
        return MPowerActiveUIStep.TYPE_KEY;
    }

    @Provides
    @IntoMap
    @StepTypeKey(AppStepType.MPOWER_ACTIVE)
    static InternalStepViewFactory provideMPowerActiveUIStepViewFactory() {
        return MPowerActiveUIStepView::fromMPowerActiveUIStep;
    }
}
