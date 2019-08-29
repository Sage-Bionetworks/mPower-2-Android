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

import android.support.annotation.ColorRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.LayoutParams;

import org.sagebionetworks.researchstack.backbone.model.Choice;
import org.sagebionetworks.researchstack.backbone.result.StepResult;
import org.sagebionetworks.researchstack.backbone.step.Step;
import org.sagebionetworks.researchstack.backbone.ui.step.body.SingleChoiceQuestionBody;
import org.sagebionetworks.research.mpower.researchstack.R;
import org.sagebionetworks.research.mpower.researchstack.framework.step.MpFormStepLayout.MpFormResultChangedListener;
import org.sagebionetworks.research.mpower.researchstack.framework.step.MpQuestionBodyResultChangedListener;

/**
 * Created by mdephillips on 10/14/18.
 */

public class MpRadioButtonQuestionBody<T> extends SingleChoiceQuestionBody
    implements MpFormResultChangedListener {

    protected MpQuestionBodyResultChangedListener resultChangedListener;

    public MpRadioButtonQuestionBody(Step step, StepResult result) {
        super(step, result);
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
        RadioGroup radioGroup = (RadioGroup)inflater.inflate(
                R.layout.mp_step_body_radio_group, parent, false);
        radioGroup.setBackgroundResource(viewBackground());

        for (int i = 0; i < choices.length; i++) {
            Choice choice = choices[i];
            final RadioButton rb = (RadioButton)inflater.inflate(
                    R.layout.mp_step_body_radio_button, radioGroup,false);
            rb.setButtonDrawable(R.drawable.mp_radio_royal500);
            rb.setText(choice.getText());
            rb.setId(i);

            boolean isSelected = isValueSelected(choice.getValue());
            rb.setChecked(isSelected);
            radioGroup.addView(rb, i, new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        radioGroup.setOnCheckedChangeListener((radioGroup1, i) -> {
            currentSelected = choices[i].getValue();
            notifyResultChangedListeners();
        });

        return radioGroup;
    }

    protected boolean isValueSelected(Object value) {
        return currentSelected != null && currentSelected.equals(value);
    }

    protected @ColorRes int viewBackground() {
        return R.color.white;
    }
}
