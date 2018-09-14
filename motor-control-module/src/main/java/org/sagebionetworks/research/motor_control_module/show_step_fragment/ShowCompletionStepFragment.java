/*
 * BSD 3-Clause License
 *
 * Copyright 2018  Sage Bionetworks. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1.  Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2.  Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * 3.  Neither the name of the copyright holder(s) nor the names of any contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission. No license is granted to the trademarks of
 * the copyright holders even if such marks are included in this software.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.sagebionetworks.research.motor_control_module.show_step_fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import org.sagebionetworks.research.domain.result.interfaces.AnswerResult;
import org.sagebionetworks.research.domain.result.interfaces.Result;
import org.sagebionetworks.research.domain.result.interfaces.TaskResult;
import org.sagebionetworks.research.mobile_ui.R;
import org.sagebionetworks.research.mobile_ui.show_step.view.ShowStepFragmentBase;
import org.sagebionetworks.research.mobile_ui.show_step.view.ShowUIStepFragmentBase;
import org.sagebionetworks.research.mobile_ui.show_step.view.view_binding.UIStepViewBinding;
import org.sagebionetworks.research.motor_control_module.step_view.CompletionStepView;
import org.sagebionetworks.research.presentation.DisplayString;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;
import org.sagebionetworks.research.presentation.perform_task.PerformTaskViewModel;
import org.sagebionetworks.research.presentation.show_step.show_step_view_models.ShowUIStepViewModel;

public class ShowCompletionStepFragment extends
        ShowUIStepFragmentBase<CompletionStepView, ShowUIStepViewModel<CompletionStepView>, UIStepViewBinding<CompletionStepView>> {
    private static final String PLACEHOLDER = "%@";

    @NonNull
    public static Integer getRunCount(@NonNull TaskResult taskResult) {
        for (Result result : taskResult.getAsyncResults()) {
            if (result.getIdentifier().equals(PerformTaskViewModel.RUN_COUNT_RESULT_ID) &&
                    result instanceof AnswerResult) {
                Object answer = ((AnswerResult) result).getAnswer();
                if (answer instanceof Integer) {
                    return (Integer) answer;
                }
            }
        }

        return 1;
    }

    @NonNull
    public static ShowCompletionStepFragment newInstance(@NonNull StepView stepView) {
        if (!(stepView instanceof CompletionStepView)) {
            throw new IllegalArgumentException("Step view: " + stepView + " is not a CompletionStepView.");
        }

        ShowCompletionStepFragment fragment = new ShowCompletionStepFragment();
        Bundle arguments = ShowStepFragmentBase.createArguments(stepView);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.rs2_show_completion_step_fragment_layout;
    }

    @NonNull
    @Override
    protected UIStepViewBinding<CompletionStepView> instantiateAndBindBinding(final View view) {
        return new UIStepViewBinding<>(view);
    }

    @Override
    protected void update(@NonNull CompletionStepView stepView) {
        super.update(stepView);
        int numberOfRuns = getRunCount(performTaskViewModel.getTaskResult()); // TODO make this accurate.

        String ordinal = OrdinalUtil.getNumberOrdinal(numberOfRuns);
        TextView titleLabel = this.stepViewBinding.getTitle();
        if (titleLabel != null) {
            DisplayString titleDisplayString = stepView.getTitle();
            if (titleDisplayString != null) {
                String title = titleDisplayString.getDisplayString();
                if (title != null) {
                    title = title.replaceAll(PLACEHOLDER, ordinal);
                    titleLabel.setText(title);
                }
            }
        }

        TextView textLabel = this.stepViewBinding.getText();
        if (textLabel != null) {
            DisplayString textDisplayString = stepView.getText();
            if (textDisplayString != null) {
                String text = textDisplayString.getDisplayString();
                if (text != null) {
                    text = text.replaceAll(PLACEHOLDER, ordinal);
                    textLabel.setText(text);
                }
            }
        }
    }
}
