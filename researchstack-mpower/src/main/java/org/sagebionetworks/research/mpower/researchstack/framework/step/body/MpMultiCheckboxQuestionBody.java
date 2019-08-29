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

package org.sagebionetworks.research.mpower.researchstack.framework.step.body;

import android.content.res.Resources;
import android.support.annotation.ColorRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import org.sagebionetworks.researchstack.backbone.model.Choice;
import org.sagebionetworks.researchstack.backbone.result.StepResult;
import org.sagebionetworks.researchstack.backbone.step.Step;
import org.sagebionetworks.researchstack.backbone.ui.step.body.BodyAnswer;
import org.sagebionetworks.researchstack.backbone.ui.step.body.SingleChoiceQuestionBody;

import org.sagebionetworks.research.mpower.researchstack.R;
import org.sagebionetworks.research.mpower.researchstack.framework.step.MpFormStepLayout.MpFormResultChangedListener;
import org.sagebionetworks.research.mpower.researchstack.framework.step.MpQuestionBodyResultChangedListener;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by rianhouston on 11/27/17.
 */

public class MpMultiCheckboxQuestionBody<T> extends SingleChoiceQuestionBody
    implements MpFormResultChangedListener {

    protected Set<Object> currentSelectedMultipleSet;

    protected MpQuestionBodyResultChangedListener resultChangedListener;

    public MpMultiCheckboxQuestionBody(Step step, StepResult result) {
        super(step, result);

        currentSelectedMultipleSet = new HashSet<>();

        if (result != null && result.getResult() != null &&
                (result.getResult() instanceof Object[])) {
            Object[] resultArray = (Object[])result.getResult();
            if (resultArray != null && resultArray.length > 0) {
                currentSelectedMultipleSet.addAll(Arrays.asList(resultArray));
            }
        }
    }

    protected void notifyResultChangedListeners() {
        if (resultChangedListener != null) {
            resultChangedListener.onResultChanged(this);
        }
    }

    @Override
    public void setMpFormResultChangedListener(MpQuestionBodyResultChangedListener listener) {
        this.resultChangedListener = listener;
    }

    @Override
    public View getBodyView(int viewType, LayoutInflater inflater, ViewGroup parent) {

        Resources res = parent.getResources();
        LinearLayout view = new LinearLayout(parent.getContext());
        view.setOrientation(LinearLayout.VERTICAL);

        for (int i = 0; i < choices.length; i++) {
            Choice choice = choices[i];
            View v = inflater.inflate(R.layout.mp_step_body_checkbox, view,false);
            v.setBackgroundResource(viewBackground());
            final CheckBox cb = v.findViewById(R.id.checkbox);
            cb.setButtonDrawable(R.drawable.mp_checkbox_royal500);
            cb.setText(choice.getText());
            cb.setId(i);

            boolean isSelected = isValueSelected(choice.getValue());
            cb.setChecked(isSelected);

            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    Choice<T> c = choices[compoundButton.getId()];
                    Object selectedValue = c.getValue();
                    if(b) {
                        currentSelectedMultipleSet.add(selectedValue);
                    } else {
                        currentSelectedMultipleSet.remove(selectedValue);
                    }
                    notifyResultChangedListeners();
                }
            });

            if(i != 0) {
                View space = new View(parent.getContext());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    res.getDimensionPixelSize(R.dimen.mp_single_choice_question_margin));
                space.setLayoutParams(lp);
                view.addView(space);
            }

            view.addView(v);
        }

        LinearLayout.MarginLayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setPadding(0, res.getDimensionPixelSize(R.dimen.mp_single_choice_question_margin), 0,
            res.getDimensionPixelSize(R.dimen.mp_single_choice_question_margin));

        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(res.getColor(viewBackground()));

        return view;
    }

    protected boolean isValueSelected(Object value) {
        boolean isSelected = false;
        isSelected = currentSelectedMultipleSet != null &&
                currentSelectedMultipleSet.contains(value);
        return isSelected;
    }

    protected @ColorRes int viewBackground() {
        return R.color.white;
    }

    @Override
    public StepResult getStepResult(boolean skipped) {
        if (skipped) {
            currentSelectedMultipleSet.clear();
            result.setResult((T[]) currentSelectedMultipleSet.toArray());
        } else {
            result.setResult((T[]) currentSelectedMultipleSet.toArray());
        }
        return result;
    }

    @Override
    public BodyAnswer getBodyAnswerState() {
        return BodyAnswer.VALID;
    }
}
