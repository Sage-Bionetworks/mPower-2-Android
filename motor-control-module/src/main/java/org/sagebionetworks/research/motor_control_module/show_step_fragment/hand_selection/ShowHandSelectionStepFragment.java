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

package org.sagebionetworks.research.motor_control_module.show_step_fragment.hand_selection;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.sagebionetworks.research.domain.result.AnswerResultType;
import org.sagebionetworks.research.domain.result.ResultType;
import org.sagebionetworks.research.domain.result.implementations.AnswerResultBase;
import org.sagebionetworks.research.domain.result.interfaces.AnswerResult;
import org.sagebionetworks.research.mobile_ui.perform_task.PerformTaskFragment;
import org.sagebionetworks.research.mobile_ui.show_step.view.ShowStepFragmentBase;
import org.sagebionetworks.research.mobile_ui.show_step.view.ShowFormUIStepFragment;
import org.sagebionetworks.research.motor_control_module.R;
import org.sagebionetworks.research.presentation.model.form.ChoiceInputFieldViewBase;
import org.sagebionetworks.research.presentation.model.form.InputFieldView;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import java.util.ArrayList;
import java.util.List;

public class ShowHandSelectionStepFragment extends ShowFormUIStepFragment {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShowHandSelectionStepFragment.class);
    public static final String HAND_SELECTION_KEY = "handSelection";
    public static final String HAND_ORDER_KEY = "handOrder";

    @NonNull
    public static ShowHandSelectionStepFragment newInstance(@NonNull StepView stepView,
                                                            @NonNull PerformTaskFragment performTaskFragment) {
        ShowHandSelectionStepFragment fragment = new ShowHandSelectionStepFragment();
        Bundle arguments = ShowStepFragmentBase.createArguments(stepView, performTaskFragment);
        fragment.setArguments(arguments);
        fragment.initialize();
        return fragment;
    }

    public void writeHandSelectionResult(@HandSelection String handSelection) {
        List<String> result = new ArrayList<>();
        switch (handSelection) {
            case HandSelection.LEFT:
                result.add(HandSelection.LEFT);
                break;
            case HandSelection.RIGHT:
                result.add(HandSelection.RIGHT);
                break;
            case HandSelection.BOTH:
                // If the user selected both we randomize the hand order.
                if (Math.random() < .5) {
                    result.add(HandSelection.LEFT);
                    result.add(HandSelection.RIGHT);
                } else {
                    result.add(HandSelection.RIGHT);
                    result.add(HandSelection.LEFT);
                }
                break;
        }

        AnswerResult<List<String>> answerResult = new AnswerResultBase<>(HAND_ORDER_KEY, Instant.now(),
                Instant.now(), result, AnswerResultType.DATA);
        this.performTaskViewModel.addStepResult(answerResult);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = super.onCreateView(inflater, container, savedInstanceState);

        // The default value is both if the user hasn't selected before.
        SharedPreferences prefs = this.getSharedPreferencesForTask();
        @HandSelection String lastSelection = prefs.getString(HAND_SELECTION_KEY, HandSelection.BOTH);
        this.writeHandSelectionResult(lastSelection);
        RecyclerView recyclerView = this.stepViewBinding.getRecyclerView();
        if (recyclerView != null) {
            recyclerView.setHasFixedSize(true);
            LinearLayoutManager manager = new LinearLayoutManager(recyclerView.getContext());
            recyclerView.setLayoutManager(manager);
            DividerItemDecoration decoration = new DividerItemDecoration(recyclerView.getContext(),
                    manager.getOrientation());
            Drawable drawable = this.getContext().getResources().getDrawable(R.drawable.form_step_divider);
            decoration.setDrawable(drawable);
            recyclerView.addItemDecoration(decoration);
            List<InputFieldView> inputFields = stepView.getInputFields();
            if (inputFields.isEmpty()) {
                LOGGER.warn("Form step with no input fields created.");
                return result;
            } else if (inputFields.size() > 1) {
                LOGGER.warn("Form step with more than 1 input field created, using the first input field.");
            }

            InputFieldView inputField = inputFields.get(0);
            if (!(inputField instanceof ChoiceInputFieldViewBase<?>)) {
                LOGGER.warn("Form step with a non ChoiceInput field created.");
                return result;
            }

            ChoiceInputFieldViewBase<?> choiceInputField = (ChoiceInputFieldViewBase<?>)inputField;
            HandSelectionAdapter<?> adapter = new HandSelectionAdapter<>(this, recyclerView,
                    choiceInputField.getChoices(), lastSelection);
            recyclerView.setAdapter(adapter);
        }

        return result;
    }

    public SharedPreferences getSharedPreferencesForTask() {
        String taskId = this.performTaskViewModel.getTaskView().getIdentifier();
        return this.getContext().getSharedPreferences(taskId, Context.MODE_PRIVATE);
    }
}
