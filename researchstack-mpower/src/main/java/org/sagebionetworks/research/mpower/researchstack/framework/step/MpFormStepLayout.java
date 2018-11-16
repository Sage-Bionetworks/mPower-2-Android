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
import android.os.Handler;
import android.support.annotation.ColorRes;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.UnderlineSpan;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.researchstack.backbone.factory.IntentFactory;
import org.researchstack.backbone.result.StepResult;
import org.researchstack.backbone.step.Step;
import org.researchstack.backbone.task.Task;
import org.researchstack.backbone.ui.step.body.StepBody;
import org.researchstack.backbone.ui.step.layout.FormStepLayout;
import org.researchstack.backbone.utils.LogExt;
import org.researchstack.backbone.utils.ResUtils;

import org.sagebionetworks.research.mpower.researchstack.framework.MpTaskFactory;

import static org.researchstack.backbone.ui.step.body.StepBody.VIEW_TYPE_DEFAULT;

import org.sagebionetworks.research.mpower.researchstack.R;
import org.sagebionetworks.research.mpower.researchstack.framework.MpViewTaskActivity;
import org.sagebionetworks.research.mpower.researchstack.framework.step.toolbar.MpTaskBehindToolbarManipulator;
import org.sagebionetworks.research.mpower.researchstack.framework.step.toolbar.MpTaskStatusBarManipulator;
import org.sagebionetworks.research.mpower.researchstack.framework.step.toolbar.MpTaskToolbarTintManipulator;

/**
 * Created by rianhouston on 11/22/17.
 */

public class MpFormStepLayout extends FormStepLayout implements
        MpTaskStatusBarManipulator, MpTaskBehindToolbarManipulator, MpTaskToolbarTintManipulator,
        MpFormStepLayoutBottomController, MpQuestionBodyResultChangedListener {

    protected MpFormStep mpFormStep;
    protected LinearLayout textContainer;

    protected LinearLayout bottomBodyContainer;

    protected Button backButton;
    protected Button nextButton;
    protected Button skipButton;

    protected boolean shouldManageNextButtonState;

    public MpFormStepLayout(Context context) {
    super(context);
  }

    public MpFormStepLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

    public MpFormStepLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MpFormStepLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void initialize(Step step, StepResult result) {
        validateAndSetBpFormStep(step);
        super.initialize(step, result);
        setupViews();
        refreshCustomSubmitBar();
        refreshNextButtonEnabled();
    }

    @Override
    protected void setupEditTextImeOptions() {
        shouldManageNextButtonState = false;
        // Before we setup IME options, check for MpFormStepBottomStepBody
        for (FormStepData stepData : subQuestionStepData) {
            if (stepData.getStepBody() instanceof MpFormStepBottomStepBody) {
                ((MpFormStepBottomStepBody)stepData.getStepBody())
                        .onMpFormStepAvailable(this);
            }
            if (stepData.getStepBody() instanceof MpFormResultChangedListener) {
                // We can only manage next button state if the step body
                // will tell us when its value is updated
                shouldManageNextButtonState = true;
                ((MpFormResultChangedListener)stepData.getStepBody())
                        .setMpFormResultChangedListener(this);
            }
        }
        super.setupEditTextImeOptions();
    }

  /**
   * Need to override this method to handle MpFormStepBottomStepBody views diefferently
   * than normal views.
   * @param onEditText
   */
  @Override
    protected void focusKeyboard(EditText onEditText) {
        for (FormStepData stepData : subQuestionStepData) {
            EditText editText = findEditText(stepData);

            if (editText != null && (editText.getText() == null || editText.getText().length() == 0)) {
                if (stepData.getStepBody() instanceof MpFormStepBottomStepBody) {
                    // the delay is needed here cause the step has not finished setup and will crash
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            editText.performClick();
                        }
                    }, 50);
                } else {
                    super.focusKeyboard(editText);
                }
                break;
            }
        }
    }

    @Override
    protected @IdRes int getFormTitleId() {
        return R.id.bp_text_container_title;
    }

    @Override
    protected @IdRes int getFormTextId() {
        return R.id.bp_text_container_text;
    }

    protected void setupViews() {
        bottomBodyContainer = findViewById(R.id.bp_form_step_layout_bottom_body);

        backButton = findViewById(R.id.bp_submit_bar_back);
        backButton.setOnClickListener(this::onBackButtonClicked);

        nextButton = findViewById(R.id.bp_submit_bar_next);
        nextButton.setOnClickListener(this::onNextButtonClicked);

        skipButton = findViewById(R.id.bp_submit_bar_skip);
        skipButton.setOnClickListener(this::onSkipButtonClicked);

        // Set text container default or custom padding
        textContainer = findViewById(R.id.mp_text_container);
        if (textContainer != null) {  // sub-classes may get rid of the text container
            int defaultPadding = getResources().getDimensionPixelOffset(R.dimen.rsb_padding_large);
            if (mpFormStep.textContainerBottomPaddingRes != null){
                int bottomPaddingRes = ResUtils.getDimenResourceId(
                        getContext(), mpFormStep.textContainerBottomPaddingRes);
                int bottomPadding = getResources().getDimensionPixelOffset(bottomPaddingRes);
                textContainer.setPadding(defaultPadding, defaultPadding, defaultPadding, bottomPadding);
            } else{
                textContainer.setPadding(defaultPadding, defaultPadding, defaultPadding, defaultPadding);
            }
        }

        if (mpFormStep.backgroundColorRes != null) {
            View container = findViewById(getBackgroundContainerRes());
            if (container != null) {
                @ColorRes int backgroundColor = ResUtils.getColorResourceId(
                        getContext(), mpFormStep.backgroundColorRes);
                if (backgroundColor != 0) {
                    container.setBackgroundResource(backgroundColor);
                }
            }
        }

        if (formSummaryTextview != null && formSummaryTextview.getText() != null) {
            formSummaryTextview.setLinkTextColor(
                    ResourcesCompat.getColor(getResources(), R.color.sageResearchPrimary, null));
            formSummaryTextview.setLinksClickable(true);
            formSummaryTextview.setAutoLinkMask(Linkify.PHONE_NUMBERS | Linkify.EMAIL_ADDRESSES);
            formSummaryTextview.setMovementMethod(LinkMovementMethod.getInstance());
            formSummaryTextview.setText(Html.fromHtml(formSummaryTextview.getText().toString()));
        }
    }

    protected @IdRes int getBackgroundContainerRes() {
        return R.id.bp_layout_form_container;
    }

    protected void validateAndSetBpFormStep(Step step) {
        if (!(step instanceof MpFormStep)) {
            throw new IllegalStateException("BpFormStepLayout only works with MpFormStep");
        }
        this.mpFormStep = (MpFormStep) step;
    }

    protected void refreshNextButtonEnabled() {
        boolean isAnswerValid = isAnswerValid(false);
        if (shouldManageNextButtonState) {
            LogExt.d(MpFormStepLayout.class, "Next button enabled " + isAnswerValid);
            setNextButtonEnabled(isAnswerValid);
        }
    }

    protected void setNextButtonEnabled(boolean enabled) {
        nextButton.setEnabled(enabled);
        nextButton.setAlpha(enabled ? 1.0f : 0.25f);
    }

    protected void onBackButtonClicked(View v) {
        hideKeyboard();
        isBackEventConsumed();
    }

    protected void onNextButtonClicked(View v) {
        hideKeyboard();
        super.onNextClicked();
    }

    @Override
    public boolean isBackEventConsumed() {
        if (isShowingBottomQuestionBody()) {
            hideBottomQuestionBody();
            return true;
        }
        return super.isBackEventConsumed();
    }

    protected void onSkipButtonClicked(View v) {
        hideKeyboard();
        if (mpFormStep.bottomLinkTaskId != null) {
            MpTaskFactory taskFactory = new MpTaskFactory();
            Task task = taskFactory.createTask(getContext(), mpFormStep.bottomLinkTaskId);
            getContext().startActivity(IntentFactory.INSTANCE.newTaskIntent(
                    getContext(), MpViewTaskActivity.class, task));
        } else {
            super.onSkipClicked();
        }
    }

    @Override
    public @LayoutRes int getFixedSubmitBarLayoutId() {
        return R.layout.mp_layout_form_step_submit_bar;
    }

    @Override
    public int getContentResourceId() {
        return R.layout.mp_step_layout_form;
    }

    protected void refreshCustomSubmitBar() {
        backButton.setText(R.string.rsb_AX_BUTTON_BACK);
        if (mpFormStep.hideBackButton) {
            backButton.setVisibility(View.GONE);
        }

        if (mpFormStep.buttonTitle == null) {
            nextButton.setText(R.string.rsb_BUTTON_NEXT);
        } else {
            nextButton.setText(mpFormStep.buttonTitle);
        }

        String skipButtonTitle = super.skipButtonTitle();
        if (skipButtonTitle != null) {
            // Leaving this as is, but not sure why buttons would need space given the underline
            skipButtonTitle = "   " + skipButtonTitle + "   "; // give the button some space
            SpannableString content = new SpannableString(skipButtonTitle);
            content.setSpan(new UnderlineSpan(), 0, skipButtonTitle.length(), 0);
            skipButton.setText(content);
        } else if (formStep.getSkipTitle() != null) {
            // If the formStep has a skipTitle, then set the text for the skipButton appropriately
            skipButtonTitle = formStep.getSkipTitle();
            SpannableString content = new SpannableString(skipButtonTitle);
            content.setSpan(new UnderlineSpan(), 0, skipButtonTitle.length(), 0);
            skipButton.setText(content);
        }
        skipButton.setVisibility(skipButtonTitle == null ? View.GONE : View.VISIBLE);
    }

    /**
     * @param stepBody to inflate and show in the bottom container
     */
    public void showBottomQuestionBody(StepBody stepBody) {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        View shadow = new View(getContext());
        shadow.setBackgroundResource(R.drawable.mp_tab_top_shadow);
        int shadowHeight =  getResources()
                .getDimensionPixelOffset(R.dimen.mp_bottom_container_shadow_height);
        bottomBodyContainer.addView(shadow, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, shadowHeight));

        View stepBodyView = stepBody.getBodyView(
                VIEW_TYPE_DEFAULT, inflater, bottomBodyContainer);
        bottomBodyContainer.addView(stepBodyView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    /**
     * Call to hide the bottom question body
     */
    public void hideBottomQuestionBody() {
        bottomBodyContainer.removeAllViews();
    }

    @Override
    public boolean isShowingBottomQuestionBody() {
        return bottomBodyContainer.getChildCount() > 0;
    }

    @Override
    public int mpStatusBarColor() {
        if (mpFormStep.statusBarColorRes != null) {
            return ResUtils.getColorResourceId(getContext(), mpFormStep.statusBarColorRes);
        } else if (mpFormStep.backgroundColorRes != null) {
            return ResUtils.getColorResourceId(getContext(), mpFormStep.backgroundColorRes);
        } else if (mpFormStep.imageBackgroundColorRes != null) {
            return ResUtils.getColorResourceId(getContext(), mpFormStep.imageBackgroundColorRes);
        }
        return MpTaskStatusBarManipulator.DEFAULT_COLOR;
    }

    @Override
    public boolean mpToolbarStepLayoutBehind() {
        return false;
    }

    @Override
    public int bpToolbarTintColor() {
        return R.color.white;
    }

    @Override
    public int mpToolbarBackgroundColor() {
        return R.color.sageResearchPrimary;
    }

    @Override
    public void onResultChanged(StepBody stepBody) {
        refreshNextButtonEnabled();
    }

    /**
     * A StepBody that wants to use the bottom controller will implement this
     */
    public interface MpFormStepBottomStepBody {
        void onMpFormStepAvailable(MpFormStepLayoutBottomController controller);
    }

    /**
     * A StepBody that interacts with the bottom controller will call this
     */
    public interface MpBottomControllerOnResultChangedNotifyListener {
        void onResultChanged();
    }

    /**
     * A StepBody will implement this to be able to tell this class when results have changed
     */
    public interface MpFormResultChangedListener {
        void setMpFormResultChangedListener(MpQuestionBodyResultChangedListener listener);
    }
}
