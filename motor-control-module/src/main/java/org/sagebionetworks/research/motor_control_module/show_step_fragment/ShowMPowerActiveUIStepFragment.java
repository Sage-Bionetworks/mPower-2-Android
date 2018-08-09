package org.sagebionetworks.research.motor_control_module.show_step_fragment;

import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import org.sagebionetworks.research.domain.result.interfaces.TaskResult;
import org.sagebionetworks.research.domain.task.Task;
import org.sagebionetworks.research.mobile_ui.perform_task.PerformTaskFragment;
import org.sagebionetworks.research.mobile_ui.show_step.view.ShowActiveUIStepFragment;
import org.sagebionetworks.research.mobile_ui.show_step.view.ShowStepFragmentBase;
import org.sagebionetworks.research.mobile_ui.show_step.view.view_binding.ActiveUIStepViewBinding;
import org.sagebionetworks.research.mobile_ui.widget.ActionButton;
import org.sagebionetworks.research.motor_control_module.R;
import org.sagebionetworks.research.presentation.model.interfaces.ActiveUIStepView;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;

public class ShowMPowerActiveUIStepFragment extends ShowActiveUIStepFragment {
    @NonNull
    public static ShowMPowerActiveUIStepFragment newInstance(@NonNull StepView stepView) {
        ShowMPowerActiveUIStepFragment fragment = new ShowMPowerActiveUIStepFragment();
        Bundle arguments = ShowStepFragmentBase.createArguments(stepView);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public int getLayoutId() {
        return R.layout.mpower2_active_step;
    }

    @NonNull
    @Override
    protected ActiveUIStepViewBinding<ActiveUIStepView> instantiateAndBindBinding(View view) {
        return new ActiveUIStepViewBinding<>(view);
    }

    @Override
    protected void update(ActiveUIStepView stepView) {
        super.update(stepView);
        Task task = this.performTaskViewModel.getTask();
        TaskResult taskResult = this.performTaskViewModel.getTaskResult().getValue();
        HandStepUIHelper.update(stepView, this.stepViewBinding);
        // Underline the skip button
        ActionButton skipButton = this.stepViewBinding.getSkipButton();
        if (skipButton != null) {
            skipButton.setPaintFlags(skipButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }
    }
}
