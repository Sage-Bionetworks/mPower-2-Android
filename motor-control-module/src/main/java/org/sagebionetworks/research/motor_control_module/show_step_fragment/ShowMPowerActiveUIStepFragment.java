package org.sagebionetworks.research.motor_control_module.show_step_fragment;

import static org.sagebionetworks.research.motor_control_module.step.HandStepHelper.JSON_PLACEHOLDER;

import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import org.sagebionetworks.research.domain.result.interfaces.TaskResult;
import org.sagebionetworks.research.mobile_ui.show_step.view.ShowActiveUIStepFragmentBase;
import org.sagebionetworks.research.mobile_ui.show_step.view.ShowStepFragmentBase;
import org.sagebionetworks.research.mobile_ui.show_step.view.view_binding.ActiveUIStepViewBinding;
import org.sagebionetworks.research.mobile_ui.widget.ActionButton;
import org.sagebionetworks.research.motor_control_module.R;
import org.sagebionetworks.research.motor_control_module.step.HandStepHelper;
import org.sagebionetworks.research.motor_control_module.step.HandStepHelper.Hand;
import org.sagebionetworks.research.motor_control_module.step_view.MPowerActiveUIStepView;
import org.sagebionetworks.research.presentation.model.action.ActionType;
import org.sagebionetworks.research.presentation.model.action.ActionView;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;
import org.sagebionetworks.research.presentation.model.interfaces.UIStepView;
import org.sagebionetworks.research.presentation.show_step.show_step_view_models.ShowActiveUIStepViewModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ShowMPowerActiveUIStepFragment extends ShowActiveUIStepFragmentBase
        <MPowerActiveUIStepView, ShowActiveUIStepViewModel<MPowerActiveUIStepView>, ActiveUIStepViewBinding<MPowerActiveUIStepView>> {
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

    @Override
    public void onStart() {
        super.onStart();
        if (!showStepViewModel.isCountdownRunning() && !showStepViewModel.isCountdownPaused()) {
            showStepViewModel.startCountdown();
        }
    }

    @NonNull
    @Override
    protected ActiveUIStepViewBinding<MPowerActiveUIStepView> instantiateAndBindBinding(View view) {
        return new ActiveUIStepViewBinding<>(view);
    }

    @Override
    protected void update(MPowerActiveUIStepView stepView) {
        super.update(stepView);
        TaskResult taskResult = this.performTaskViewModel.getTaskResult();
        HandStepUIHelper.update(taskResult, stepView, this.stepViewBinding);
        // Underline the skip button
        ActionButton skipButton = this.stepViewBinding.getSkipButton();
        if (skipButton != null) {
            skipButton.setPaintFlags(skipButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }
    }

    @Override
    protected void updateNavigationButtons(UIStepView stepView) {
        super.updateNavigationButtons(stepView);
        // To keep parity with iOS' active ui step action mappings, but still have our navigation bar work,
        // we must re-wire the mapping so that the skip button is info or "learn more", and the next button is skip
        ActionButton forwardButton = this.stepViewBinding.getNextButton();
        ActionView skipActionView = this.getSkipButtonActionView(stepView);
        this.updateButtonFromActionView(forwardButton, skipActionView);
        ActionView infoActionView = this.getInfoButtonActionView(stepView);
        ActionButton skipButton = this.stepViewBinding.getSkipButton();
        this.updateButtonFromActionView(skipButton, infoActionView);
    }

    @Override
    @Nullable
    @ActionType
    protected String getActionTypeFromActionButton(@NonNull ActionButton actionButton) {
        int actionButtonId = actionButton.getId();

        // To keep parity with iOS' active ui step action mappings, but still have our navigation bar work,
        // we must re-wire the mapping so that the skip button is info or "learn more", and the next button is skip
        if (org.sagebionetworks.research.mobile_ui.R.id.rs2_step_navigation_action_forward == actionButtonId) {
            return ActionType.SKIP;
        } else if (org.sagebionetworks.research.mobile_ui.R.id.rs2_step_navigation_action_skip == actionButtonId) {
            return ActionType.INFO;
        }

        return super.getActionTypeFromActionButton(actionButton);
    }
    
}
