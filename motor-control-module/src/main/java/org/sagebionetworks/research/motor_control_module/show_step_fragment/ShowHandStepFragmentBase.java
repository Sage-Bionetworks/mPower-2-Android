package org.sagebionetworks.research.motor_control_module.show_step_fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import org.sagebionetworks.research.mobile_ui.show_step.view.FragmentSkipRule;
import org.sagebionetworks.research.mobile_ui.show_step.view.ShowStepFragmentBase;
import org.sagebionetworks.research.mobile_ui.show_step.view.ShowUIStepFragmentBase;
import org.sagebionetworks.research.mobile_ui.show_step.view.view_binding.UIStepViewBinding;
import org.sagebionetworks.research.presentation.model.interfaces.UIStepView;
import org.sagebionetworks.research.presentation.show_step.show_step_view_models.ShowUIStepViewModel;

public abstract class ShowHandStepFragmentBase
        <S extends UIStepView, VM extends ShowUIStepViewModel<S>, SB extends UIStepViewBinding<S>>
        extends ShowUIStepFragmentBase<S, VM, SB> implements FragmentSkipRule {

    @Override
    public boolean shouldSkip() {
        // Necessary to get the stepView Argument from the bundle because onCreate hasn't
        // necessarily been called before this method is called.
        this.initialize();
        HandStepHelper.Hand hand = HandStepHelper.whichHand(this.stepView.getIdentifier());
        HandStepHelper.Hand nextHand = HandStepHelper.nextHand(this.performTaskViewModel.getTask(),
                this.performTaskViewModel.getTaskResult().getValue());
        return hand != null && hand != nextHand;
    }

    @Override
    @Nullable
    public String skipToIdentifier() {
        return HandStepHelper.nextHand(this.performTaskViewModel.getTask(),
                this.performTaskViewModel.getTaskResult().getValue()).toString();
    }
}
