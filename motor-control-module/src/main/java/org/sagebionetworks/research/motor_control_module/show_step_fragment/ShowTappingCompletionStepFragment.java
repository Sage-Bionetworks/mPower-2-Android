package org.sagebionetworks.research.motor_control_module.show_step_fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import org.sagebionetworks.research.mobile_ui.show_step.view.ShowStepFragmentBase;
import org.sagebionetworks.research.mobile_ui.show_step.view.ShowUIStepFragmentBase;
import org.sagebionetworks.research.motor_control_module.R;
import org.sagebionetworks.research.motor_control_module.step_binding.TappingCompletionStepViewBinding;
import org.sagebionetworks.research.motor_control_module.step_view.TappingCompletionStepView;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;
import org.sagebionetworks.research.presentation.show_step.show_step_view_models.ShowUIStepViewModel;

public class ShowTappingCompletionStepFragment extends
        ShowUIStepFragmentBase<TappingCompletionStepView, ShowUIStepViewModel<TappingCompletionStepView>,
                TappingCompletionStepViewBinding> {
    public static ShowTappingCompletionStepFragment newInstance(@NonNull StepView stepView) {
        ShowTappingCompletionStepFragment fragment = new ShowTappingCompletionStepFragment();
        Bundle args = ShowStepFragmentBase.createArguments(stepView);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.mpower2_tapping_completion_step;
    }

    @NonNull
    @Override
    protected TappingCompletionStepViewBinding instantiateAndBindBinding(View view) {
        return new TappingCompletionStepViewBinding(view);
    }
}
