package org.sagebionetworks.research.motor_control_module.show_step_fragment;

import android.support.annotation.NonNull;
import android.view.View;

import org.sagebionetworks.research.mobile_ui.show_step.view.ShowStepFragmentBase;
import org.sagebionetworks.research.motor_control_module.R;
import org.sagebionetworks.research.motor_control_module.step_binding.FormUIStepViewBinding;
import org.sagebionetworks.research.presentation.model.interfaces.FormUIStepView;
import org.sagebionetworks.research.presentation.show_step.show_step_view_models.ShowUIStepViewModel;

public class ShowFormUIStepFragment extends
        ShowStepFragmentBase<FormUIStepView, ShowUIStepViewModel<FormUIStepView>,
                FormUIStepViewBinding<FormUIStepView>> {
    @Override
    protected int getLayoutId() {
        return R.layout.mpower2_form_step;
    }

    @NonNull
    @Override
    protected FormUIStepViewBinding<FormUIStepView> instantiateAndBindBinding(View view) {
        return new FormUIStepViewBinding<>(view);
    }
}
