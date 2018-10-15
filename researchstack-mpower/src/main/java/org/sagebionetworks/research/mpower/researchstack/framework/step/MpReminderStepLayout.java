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

package org.sagebionetworks.research.mpower.researchstack.framework.step;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.gson.annotations.SerializedName;

import org.researchstack.backbone.result.StepResult;
import org.researchstack.backbone.result.TaskResult;
import org.researchstack.backbone.step.FormStep;
import org.researchstack.backbone.step.QuestionStep;
import org.researchstack.backbone.task.NavigableOrderedTask;
import org.researchstack.backbone.ui.callbacks.StepCallbacks;
import org.researchstack.backbone.utils.ResUtils;
import org.sagebionetworks.research.mpower.researchstack.framework.step.toolbar.MpTaskBehindToolbarManipulator;
import org.sagebionetworks.research.mpower.researchstack.framework.step.toolbar.MpTaskHideToolbarManipulator;

import java.util.List;

import org.sagebionetworks.research.mpower.researchstack.R;

/**
 * Created by rianhouston on 12/29/17.
 */

public class MpReminderStepLayout extends MpFormStepLayout implements MpTaskHideToolbarManipulator,
        MpTaskBehindToolbarManipulator {

    private static final String LOG_TAG = MpReminderStepLayout.class.getCanonicalName();

    public static final String REMINDER_TIME_RESULT_ID      = "reminderTime";

    protected ImageView imageView;
    protected Step bpReminderStep;

    public MpReminderStepLayout(Context context) {
        super(context);
    }

    public MpReminderStepLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MpReminderStepLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MpReminderStepLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public int getContentResourceId() {
        return R.layout.mp_step_layout_reminder;
    }

    @Override
    public void initialize(org.researchstack.backbone.step.Step step, StepResult result) {
        validateAndSetBpReminderStep(step);

        if(result == null) {
            result = new StepResult<>(bpReminderStep);
        }

        // populate results from preferences
        for(QuestionStep qs: bpReminderStep.getFormSteps()) {
            StepResult rs = new StepResult(qs);
            if(qs.getIdentifier().equals(REMINDER_TIME_RESULT_ID)) {
                // TODO: mdephillips 10/10/18 plug in saved reminder time
                rs.setResult(9);
            }
            result.setResultForIdentifier(qs.getIdentifier(), rs);
        }

        super.initialize(step, result);
    }

    @Override
    public void setupViews() {
        super.setupViews();
        int defaultPadding = getResources().getDimensionPixelOffset(R.dimen.rsb_padding_small);

        // override the padding set in BpFormStepLayout cause we prefer this not to scroll
        textContainer.setPadding(defaultPadding, defaultPadding, defaultPadding, 0);
    }

    protected void validateAndSetBpReminderStep(org.researchstack.backbone.step.Step step) {
        if (!(step instanceof Step)) {
            throw new IllegalStateException("BpReminderStepLayout only works with BpReminderStep");
        }
        this.bpReminderStep = (Step) step;
    }

    @Override
    public void onComplete() {
        Log.d(LOG_TAG, "onComplete()");

        // schedule reminder
        String timeStr = (String)stepResult.getResultForIdentifier(REMINDER_TIME_RESULT_ID).getResult();
        // TODO: mdephillips 10/10/18 save reminder time and schedule
//        MpReminderManager.setReminderTime(timeStr, reminderType);
//        MpReminderManager.rescheduleReminders(getContext());

        callbacks.onSaveStep(StepCallbacks.ACTION_NEXT, bpReminderStep, stepResult);
    }

    @Override
    protected void initStepLayout(FormStep step) {
        super.initStepLayout(step);

        imageView = findViewById(R.id.mp_image_view);
        refreshImage(bpReminderStep.getImage());
    }

    @Override
    protected void onSkipButtonClicked(View v) {
        Log.d(LOG_TAG, "onSkipButtonClicked()");;
        hideKeyboard();
        super.onSkipClicked();
    }

    protected void refreshImage(String imageName) {
        if (imageName != null) {
            int drawableInt = ResUtils.getDrawableResourceId(getContext(), imageName);
            if (drawableInt != 0) {
                imageView.setImageResource(drawableInt);
                imageView.setVisibility(View.VISIBLE);
            }
        } else {
            imageView.setVisibility(View.GONE);
        }
    }

    public class SurveyItem extends MpFormSurveyItem {

        @SerializedName("neverSkip")
        public Boolean neverSkip;

        @SerializedName("image")
        public String image;

        @SerializedName("reminderType")
        public String reminderType;

        @SerializedName("hideToolbar")
        public Boolean hideToolbar;
    }

    public static class Step extends MpFormStep implements NavigableOrderedTask.NavigationSkipRule {

        private boolean neverSkip;
        private boolean hideToolbar;
        private String image;
        private String reminderType;

        /* Default constructor needed for serialization/deserialization of object */
        public Step() {
            super();
        }

        public Step(String identifier, String title, String detailText) {
            super(identifier, title, detailText);
        }

        public Step(String identifier, String title, String text, List<QuestionStep> steps) {
            super(identifier, title, text, steps);
        }

        public void setNeverSkip(boolean neverSkip) {
            this.neverSkip = neverSkip;
        }

        public boolean getNeverSkip() {
            return neverSkip;
        }

        public void setHideToolbar(boolean hideToolbar) {
            this.hideToolbar = hideToolbar;
        }

        public boolean getHideToolbar() {
            return hideToolbar;
        }

        public void setImage(String img) {
            image = img;
        }

        public String getImage() {
            return image;
        }

        public void setReminderType(String type) {
            reminderType = type;
        }

        public String getReminderType() {
            return reminderType;
        }


        @Override
        public Class getStepLayoutClass() {
            return MpReminderStepLayout.class;
        }

        @Override
        public boolean shouldSkipStep(TaskResult result, List<TaskResult> additionalTaskResults) {
            if (neverSkip) {
                return false;
            }
            // TODO: mdephillips skip if reminder has already been set
//            return BpPrefs.getInstance().hasReminderBeenSet(
//                    MpReminderManager.Type.fromString(reminderType));
            return false;
        }
    }

    @Override
    public boolean bpToolbarHide() {
        return bpReminderStep.hideToolbar;
    }

    @Override
    public boolean mpToolbarStepLayoutBehind() {
        return true;
    }
}