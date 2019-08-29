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

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.ColorRes;
import android.support.v4.content.res.ResourcesCompat;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.sagebionetworks.researchstack.backbone.answerformat.AnswerFormat;
import org.sagebionetworks.researchstack.backbone.model.Choice;
import org.sagebionetworks.researchstack.backbone.result.StepResult;
import org.sagebionetworks.researchstack.backbone.step.Step;
import org.sagebionetworks.researchstack.backbone.ui.adapter.TextWatcherAdapter;
import org.sagebionetworks.researchstack.backbone.ui.step.body.BodyAnswer;
import org.sagebionetworks.researchstack.backbone.ui.step.body.SingleChoiceQuestionBody;

import org.sagebionetworks.research.mpower.researchstack.R;
import org.sagebionetworks.research.mpower.researchstack.framework.step.MpFormStepLayout.MpFormResultChangedListener;
import org.sagebionetworks.research.mpower.researchstack.framework.step.MpQuestionBodyResultChangedListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by rianhouston on 11/27/17.
 */

public class MpChoiceQuestionBody<T> extends SingleChoiceQuestionBody
    implements MpFormResultChangedListener {
    private static final String LOG_TAG = MpChoiceQuestionBody.class.getCanonicalName();

    private static final String EDIT_TEXT = "EDIT_TEXT";

    protected Set<Object> currentSelectedMultipleSet;
    protected List<WeakReference<View>> bodyViewList;
    protected List<WeakReference<TextView>> bodyTextViewList;

    protected MpQuestionBodyResultChangedListener resultChangedListener;

    public MpChoiceQuestionBody(Step step, StepResult result) {
        super(step, result);

        bodyViewList = new ArrayList<>();
        bodyTextViewList = new ArrayList<>();

        if (isMutlipleChoice()) {
            // Restore results
            currentSelectedMultipleSet = new HashSet<>();

            if (result != null && result.getResult() != null &&
                    (result.getResult() instanceof Object[])) {
                Object[] resultArray = (Object[])result.getResult();
                if (resultArray != null && resultArray.length > 0) {
                    currentSelectedMultipleSet.addAll(Arrays.asList(resultArray));
                }
            } else if (result != null && result.getResult() != null &&
                    (result.getResult() instanceof List)) {
                List resultList = (List)result.getResult();
                if (resultList != null && !resultList.isEmpty()) {
                    currentSelectedMultipleSet.addAll(resultList);
                }
            }
        }

        // if the choices contain an EDIT_TEXT and we have a result but it is not already
        // set, then it must be the user entered value
        if (result != null && currentSelected == null && hasEditText() && result.getResult() != null) {
            currentSelected = result.getResult();
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

    protected boolean isMutlipleChoice() {
        if (format != null && format.getAnswerStyle() == AnswerFormat.ChoiceAnswerStyle.MultipleChoice) {
            return true;
        }
        return false;
    }

    protected boolean hasEditText() {
        boolean hasEditText = false;
        for(Choice c: choices) {
            if (EDIT_TEXT.equals(c.getValue())) {
                hasEditText = true;
                break;
            }
        }


        return hasEditText;
    }

    protected boolean isEditText(Choice choice) {
        return EDIT_TEXT.equals(choice.getValue());
    }

    @Override
    public View getBodyView(int viewType, LayoutInflater inflater, ViewGroup parent) {

        Resources res = parent.getResources();
        LinearLayout view = new LinearLayout(parent.getContext());
        view.setOrientation(LinearLayout.VERTICAL);

        final int selectedTextColor = ResourcesCompat.getColor(
                parent.getResources(), selectedTextColor(), null);
        final int unselectedTextColor = ResourcesCompat.getColor(
                parent.getResources(), unselectedTextColor(), null);

        for (int i = 0; i < choices.length; i++) {
            Choice choice = choices[i];
            View v = inflater.inflate(R.layout.mp_step_body_single_choice, view,false);

            final TextView tv = v.findViewById(R.id.single_choice_text);
            tv.setText(choice.getText());

            final EditText et = v.findViewById(R.id.single_choice_edit);
            et.setHint(choice.getText());
            et.addTextChangedListener(new TextWatcherAdapter() {
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if(charSequence == null || charSequence.length() == 0) {
                        currentSelected = choice.getText();
                    } else {
                        currentSelected = charSequence.toString();
                    }
                }
            });
            InputFilter[] filterArray = new InputFilter[1];
            // Per a bridge restriction, limit character count to 100
            filterArray[0] = new InputFilter.LengthFilter(100);
            et.setFilters(filterArray);
            et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int i, KeyEvent keyEvent) {
                    if (i == EditorInfo.IME_ACTION_DONE) {
                        InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        return true;
                    }
                    return false;
                }
            });
            v.setId(i);

            boolean isEditText = isEditText(choice);
            boolean isSelected = (isEditText) ?
                    isValueSelected(choices) : isValueSelected(choice.getValue());
            v.setBackgroundResource(isSelected ? selectedBackground() : unselectedBackground());
            tv.setTextColor(isSelected ? selectedTextColor : unselectedTextColor);

            if(isEditText && isSelected) {
                tv.setVisibility(View.GONE);
                et.setVisibility(View.VISIBLE);
                et.setText(currentSelected.toString());
            }

            v.setOnClickListener(selectedView -> {
                Choice<T> c = choices[selectedView.getId()];
                Object selectedValue = c.getValue();
                boolean wasViewSelected = isValueSelected(c.getValue());
                if (isMutlipleChoice()) {
                    if (wasViewSelected) {
                        selectedView.setBackgroundResource(unselectedBackground());
                        currentSelectedMultipleSet.remove(selectedValue);
                        tv.setTextColor(unselectedTextColor);
                    } else {
                        selectedView.setBackgroundResource(selectedBackground());
                        tv.setTextColor(selectedTextColor);
                        currentSelectedMultipleSet.add(selectedValue);
                    }
                } else {
                    currentSelected = selectedValue;
                    resetViewSelection();
                    selectedView.setBackgroundResource(selectedBackground());

                    if(selectedValue instanceof String && isEditText(choice)) {
                        currentSelected = (et.getText().length() == 0) ? c.getText() : et.getText().toString();
                        tv.setVisibility(View.GONE);
                        et.setVisibility(View.VISIBLE);
                        et.requestFocus();
                        InputMethodManager imm = (InputMethodManager)et.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
                    } else {
                        tv.setTextColor(selectedTextColor);
                    }
                }
                notifyResultChangedListeners();
            });

            if(i != 0) {
                View space = new View(parent.getContext());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    res.getDimensionPixelSize(R.dimen.mp_single_choice_question_margin));
                space.setLayoutParams(lp);
                view.addView(space);
                space.setBackgroundResource(dividerColor());
            }

            bodyViewList.add(new WeakReference<>(v));
            bodyTextViewList.add(new WeakReference<>(tv));

            view.addView(v);

            if (i >= (choices.length - 1)) {
                View space = new View(parent.getContext());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        res.getDimensionPixelSize(R.dimen.mp_single_choice_question_margin));
                space.setLayoutParams(lp);
                view.addView(space);
                space.setBackgroundResource(dividerColor());
            }
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
        if (isMutlipleChoice()) {
            isSelected = currentSelectedMultipleSet != null &&
                    currentSelectedMultipleSet.contains(value);
        } else {
            isSelected = currentSelected != null && currentSelected.equals(value);
        }
        return isSelected;
    }

    /**
     * For an EDIT_TEXT choice, see if the value matches any other choices.
     * @param choices
     * @return
     */
    protected boolean isValueSelected(Choice[] choices) {
        if(currentSelected == null) return false;

        boolean isSelected = false;
        for(Choice c: choices) {
            if(currentSelected.equals(c.getValue())) {
                isSelected = true;
                break;
            }
        }
        return !isSelected;
    }

    protected void resetViewSelection() {
        for (WeakReference<View> viewWeakReference: bodyViewList) {
            if (viewWeakReference != null && viewWeakReference.get() != null) {
                View view = viewWeakReference.get();
                view.setBackgroundResource(unselectedBackground());
                view.findViewById(R.id.single_choice_edit).setVisibility(View.GONE);
            }
        }
        for (WeakReference<TextView> viewWeakReference: bodyTextViewList) {
            if (viewWeakReference != null && viewWeakReference.get() != null) {
                int textColor = ResourcesCompat.getColor(
                        viewWeakReference.get().getResources(), unselectedTextColor(), null);
                viewWeakReference.get().setTextColor(textColor);
                viewWeakReference.get().setVisibility(View.VISIBLE);
            }
        }
    }

    protected @ColorRes int viewBackground() {
        return R.color.white;
    }

    protected @ColorRes int selectedBackground() {
        return R.color.royal100A25;
    }

    protected @ColorRes int unselectedBackground() {
        return R.color.white;
    }

    protected @ColorRes int selectedTextColor() {
        return R.color.rsb_warm_gray;
    }

    protected @ColorRes int unselectedTextColor() {
        return R.color.rsb_warm_gray;
    }

    protected @ColorRes int dividerColor() {
        return R.color.rsb_warm_gray;
    }

    @Override
    public StepResult getStepResult(boolean skipped) {
        if (isMutlipleChoice()) {
            if (skipped) {
                currentSelectedMultipleSet.clear();
                result.setResult((T[]) currentSelectedMultipleSet.toArray());
            } else {
                result.setResult((T[]) currentSelectedMultipleSet.toArray());
            }
            return result;
        }
        return super.getStepResult(skipped);
    }

    @Override
    public BodyAnswer getBodyAnswerState() {
        if (isMutlipleChoice()) {
            if (currentSelectedMultipleSet.isEmpty()) {
                return new BodyAnswer(false, org.sagebionetworks.researchstack.backbone.R.string.rsb_invalid_answer_choice);
            } else {
                return BodyAnswer.VALID;
            }
        }
        return super.getBodyAnswerState();
    }
}
