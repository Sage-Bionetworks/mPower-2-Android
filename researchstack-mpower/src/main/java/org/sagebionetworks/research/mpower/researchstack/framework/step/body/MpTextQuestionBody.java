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

import androidx.annotation.LayoutRes;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.sagebionetworks.researchstack.backbone.answerformat.TextAnswerFormat;
import org.sagebionetworks.researchstack.backbone.result.StepResult;
import org.sagebionetworks.researchstack.backbone.step.Step;
import org.sagebionetworks.researchstack.backbone.ui.step.body.TextQuestionBody;

import org.sagebionetworks.research.mpower.researchstack.R;
import org.sagebionetworks.research.mpower.researchstack.framework.step.MpFormStepLayout.MpFormResultChangedListener;
import org.sagebionetworks.research.mpower.researchstack.framework.step.MpQuestionBodyResultChangedListener;

/**
 * Created by TheMDP on 1/30/18.
 */

public class MpTextQuestionBody extends TextQuestionBody
        implements MpFormResultChangedListener {

    protected MpQuestionBodyResultChangedListener resultChangedListener;

    public MpTextQuestionBody(Step step, StepResult result) {
        super(step, result);
    }

    public @LayoutRes
    int getBodyViewRes() {
        return R.layout.mp_step_body_text;
    }

    @Override
    public View getBodyView(int viewType, LayoutInflater inflater, ViewGroup parent) {
        View body = super.getBodyView(viewType, inflater, parent);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // no-op
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // no-op
            }

            @Override
            public void afterTextChanged(Editable editable) {
                notifyResultChangedListeners();
            }
        });
        if (step.getPlaceholder() == null) {
            editText.setHint(null);
        }
        return body;
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

    public static class AnswerFormat extends TextAnswerFormat {
        public AnswerFormat() {
            super();
        }

        public AnswerFormat(int maximumLength) {
            super(maximumLength);
        }

        public QuestionType getQuestionType() {
            // For this to work, we must also provide custom code in BpTaskHelper for this format
            return () -> MpTextQuestionBody.class;
        }
    }
}
