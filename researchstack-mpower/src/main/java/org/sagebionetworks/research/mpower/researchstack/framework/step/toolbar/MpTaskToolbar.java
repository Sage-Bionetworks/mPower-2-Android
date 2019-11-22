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

package org.sagebionetworks.research.mpower.researchstack.framework.step.toolbar;

import static org.sagebionetworks.research.mpower.researchstack.framework.step.toolbar.MpTaskToolbarTintManipulator.DEFAULT_BACKGROUND_COLOR;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.sagebionetworks.researchstack.backbone.step.Step;
import org.sagebionetworks.researchstack.backbone.step.SubtaskStep;
import org.sagebionetworks.researchstack.backbone.task.OrderedTask;
import org.sagebionetworks.researchstack.backbone.task.Task;
import org.sagebionetworks.researchstack.backbone.ui.step.layout.StepLayout;
import org.sagebionetworks.researchstack.backbone.ui.views.StepSwitcher;
import org.sagebionetworks.research.mpower.researchstack.R;

/**
 * Created by TheMDP on 10/26/17.
 */

public class MpTaskToolbar extends Toolbar {

    private static final long PROGRESS_BAR_ANIM_DURATION = 200; // ms
    private static final int PROGRESS_BAR_SCALE_FACTOR = 1000; // allows smooth animation

    private ProgressBar progressBar;
    public ProgressBar getProgressBar() {
        return progressBar;
    }

    private @ColorRes int defaultStepProgressColor = R.color.white;
    public void setDefaultStepProgressColor(@ColorRes int colorRes) {
        defaultStepProgressColor = colorRes;
    }

    private @ColorRes int defaultBackgroundColor = R.color.transparent;
    public void setDefaultBackgroundColor(@ColorRes int colorRes) {
        defaultBackgroundColor = colorRes;
    }

    private @ColorRes int defaultTintColor = R.color.white;
    public void setDefaultTintColor(@ColorRes int colorRes) {
        defaultTintColor = colorRes;
    }

    private boolean hideLeftIconOverride;
    public void setHideLeftIconOverride(boolean hideLeftIcon) {
        hideLeftIconOverride = hideLeftIcon;
    }

    private @DrawableRes int defaultLeftIcon = R.drawable.mp_back_icon;
    public void setDefaultLeftIcon(@DrawableRes int drawableRes) {
        defaultLeftIcon = drawableRes;
    }

    private @DrawableRes int defaultRightIcon = R.drawable.mp_cancel_icon;
    public void setDefaultRightIcon(@DrawableRes int drawableRes) {
        defaultRightIcon = drawableRes;
    }

    private @ColorRes int defaultStatusBarColor = R.color.sageResearchPrimaryDark;
    public void setDefaultStatusBarColor(@ColorRes int colorRes) {
        defaultStatusBarColor = colorRes;
    }

    private boolean defaultBehindToolbar = false;
    public void setDefaultBehindToolbar(boolean defaultBehindToolbar) {
        this.defaultBehindToolbar = defaultBehindToolbar;
    }

    public MpTaskToolbar(Context context) {
        super(context);
        commonInit();
    }

    public MpTaskToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        commonInit();
    }

    public MpTaskToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        commonInit();
    }

    private void commonInit() {
        setBackgroundResource(R.drawable.mp_toolbar_background);

        progressBar = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.INVISIBLE);
        progressBar.setProgressDrawable(ContextCompat.getDrawable(getContext(), R.drawable.mp_progress_bar));
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                getContext().getResources().getDimensionPixelOffset(R.dimen.mp_progress_bar_height));
        params.rightMargin = getContext().getResources().getDimensionPixelOffset(R.dimen.rsb_padding_large);
        params.gravity = Gravity.CENTER;
        addView(progressBar, params);
    }

    public void setIcons(ActionBar actionBar, @DrawableRes int leftIcon, @DrawableRes int rightIcon) {
        if (actionBar != null && leftIcon != MpTaskToolbarIconManipulator.NO_ICON) {
            actionBar.setHomeAsUpIndicator(leftIcon);
        }
        if (hideLeftIconOverride && actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }

        MenuItem rightItem = getMenu().findItem(R.id.rsb_clear_menu_item);
        if (rightItem != null) {
            if (rightIcon == MpTaskToolbarIconManipulator.NO_ICON) {
                rightItem.setVisible(false);
            } else {
                rightItem.setVisible(true);
                rightItem.setIcon(ContextCompat.getDrawable(getContext(), rightIcon));
            }
        }
    }

    public void setTint(@ColorRes int color) {
        int colorRes = ContextCompat.getColor(getContext(), color);
        Drawable drawable = getNavigationIcon();
        if (drawable != null) {
            drawable.setColorFilter(colorRes, PorterDuff.Mode.SRC_ATOP);
        }
        for (int i = 0; i < getMenu().size(); i++) {
            MenuItem menuItem = getMenu().getItem(i);
            if (menuItem != null && menuItem.getIcon() != null) {
                menuItem.getIcon().setColorFilter(colorRes, PorterDuff.Mode.SRC_ATOP);
            }
        }
    }

    public void setProgress(int progress, int max) {
        int scaledFrom = progressBar.getProgress();
        int scaledTo = progress * PROGRESS_BAR_SCALE_FACTOR;
        int scaledMax = max * PROGRESS_BAR_SCALE_FACTOR;

        // if the max changed we can't safely animate the change
        boolean animate = (progressBar.getMax() == scaledMax);

        if (animate) {
            MpProgressBarAnimation anim = new MpProgressBarAnimation(progressBar, scaledFrom, scaledTo);
            anim.setDuration(PROGRESS_BAR_ANIM_DURATION);
            progressBar.startAnimation(anim);
        } else {
            progressBar.setMax(scaledMax);
            progressBar.setProgress(scaledTo);
        }
    }

    public void showProgressInToolbar(boolean showProgress) {
        // Hide with INVISIBLE so that the "Step 1 of 4" title does not show automatically
        progressBar.setVisibility(showProgress ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * Helper method for refreshing the toolbar icons for a new step layout
     * @param currentStepLayout current step layout showing
     * @param actionBar the actionBar for icon
     */
    public void refreshToolbarIcons(StepLayout currentStepLayout, ActionBar actionBar) {

        if (currentStepLayout == null) {
            return;
        }

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Icon manipulator
        if (currentStepLayout instanceof MpTaskToolbarIconManipulator) {
            MpTaskToolbarIconManipulator iconManipulator =
                    (MpTaskToolbarIconManipulator)currentStepLayout;
            setIcons(actionBar, iconManipulator.bpToolbarLeftIcon(), iconManipulator.bpToolbarRightIcon());
        } else {
            setIcons(actionBar, defaultLeftIcon, defaultRightIcon);
        }

        // Tint manipulator
        if (currentStepLayout instanceof MpTaskToolbarTintManipulator) {
            MpTaskToolbarTintManipulator tintManipulator =
                    (MpTaskToolbarTintManipulator)currentStepLayout;
            setTint(tintManipulator.bpToolbarTintColor());
        } else {
            setTint(defaultTintColor);
        }
    }

    /**
     * Helper method for refreshing the toolbar progress View and
     * an optional step progress TextView that will display a text representation of progress
     * for a new step layout
     *
     * @param currentStepLayout the current step layout
     * @param stepProgressTv an optional textview for step progress that is not a part of the toolbar
     * @return if this method showed the progress bar and textview
     */
    public boolean refreshStepProgress(
            StepLayout currentStepLayout,
            @Nullable TextView stepProgressTv,
            @Nullable Progress stepProgress) {

        boolean showProgressInToolbar = true;

        if (currentStepLayout instanceof MpTaskToolbarProgressManipulator) {
            showProgressInToolbar = ((MpTaskToolbarProgressManipulator)currentStepLayout).mpToolbarShowProgress();
        }

        // The text color of the step progress defaults to white,
        // but is set to a darker theme for all tint colors other than white
        @ColorRes int stepProgressTextColorRes = defaultStepProgressColor;
        if (currentStepLayout instanceof MpTaskToolbarTintManipulator) {
            if (((MpTaskToolbarTintManipulator)currentStepLayout).bpToolbarTintColor() != R.color.white) {
                stepProgressTextColorRes = R.color.appTextDark;
            }
        }
        int stepProgressTextColor = ResourcesCompat.getColor(getResources(), stepProgressTextColorRes, null);
        if (stepProgressTv != null) {
            stepProgressTv.setTextColor(stepProgressTextColor);
        }

        if (showProgressInToolbar && stepProgressTv != null) {
            stepProgressTv.setText(stepProgress.stepProgressText);
            stepProgressTv.setVisibility(View.VISIBLE);
            setProgress(stepProgress.progress, stepProgress.max);
        }

        if (!showProgressInToolbar && stepProgressTv != null) {
            stepProgressTv.setVisibility(View.GONE);
        }

        showProgressInToolbar(showProgressInToolbar);

        return showProgressInToolbar;
    }

    /**
     * Helper method to hide or show the full toolbar container
     * @param current step layout showing
     * @param toolbarContainer the container of the toolbar, it is ok if it is just this class
     */
    public void refreshViewHideToolbar(StepLayout current, View toolbarContainer) {
        boolean hideToolbar = false;
        if (current instanceof MpTaskHideToolbarManipulator) {
            hideToolbar = ((MpTaskHideToolbarManipulator)current).bpToolbarHide();
        }
        toolbarContainer.setVisibility(hideToolbar ? View.GONE : View.VISIBLE);
    }

    /**
     * Helper method to move the step layout content below or behind the toolbar
     * @param stepSwitcher must be inside a RelativeLayout
     * @param current step layout
     */
    public void refreshViewBehindToolbar(StepSwitcher stepSwitcher, StepLayout current) {
        boolean showBehindToolbar = defaultBehindToolbar;
        if (current instanceof MpTaskBehindToolbarManipulator) {
            showBehindToolbar = ((MpTaskBehindToolbarManipulator)current).mpToolbarStepLayoutBehind();
        }
        RelativeLayout.LayoutParams rootParams = (RelativeLayout.LayoutParams)stepSwitcher.getLayoutParams();
        if (showBehindToolbar) {
            rootParams.removeRule(RelativeLayout.BELOW);
            rootParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        } else {
            rootParams.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
            rootParams.addRule(RelativeLayout.BELOW, R.id.bp_toolbar_container);
        }
        stepSwitcher.setLayoutParams(rootParams);
    }

    /**
     * Helper method to set the status bar color for the current step layout
     * @param activity the activity
     * @param current step layout
     */
    public void setStatusBarColor(Activity activity, StepLayout current) {
        // Allow for customization of the status bar
        @ColorRes int statusBarColor = defaultStatusBarColor;
        if (current instanceof MpTaskStatusBarManipulator) {
            MpTaskStatusBarManipulator manipulator = (MpTaskStatusBarManipulator)current;
            if (manipulator.mpStatusBarColor() != MpTaskStatusBarManipulator.DEFAULT_COLOR) {
                statusBarColor = manipulator.mpStatusBarColor();
            }
        }
        int color = ResourcesCompat.getColor(getResources(), statusBarColor, null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(color);
        }
    }

    /**
     * @param current step layout
     * @param toolbarContainer the view containing the toolbar
     */
    public void refreshToolbarBackgroundColor(StepLayout current, View toolbarContainer) {
        if (current instanceof MpTaskToolbarTintManipulator) {
            MpTaskToolbarTintManipulator manipulator = (MpTaskToolbarTintManipulator)current;
            if (manipulator.mpToolbarBackgroundColor() == DEFAULT_BACKGROUND_COLOR) {
                toolbarContainer.setBackgroundResource(defaultBackgroundColor);
            } else {
                toolbarContainer.setBackgroundResource(manipulator.mpToolbarBackgroundColor());
            }
        } else {
            toolbarContainer.setBackgroundResource(defaultBackgroundColor);
        }
    }

    /**
     * Helper method to create step progress text for an orderedTask
     * @param task must be an ordered task for this to work containing the currentStep
     * @param currentStep in the task
     */
    public Progress createOrderedTaskStepProgressText(Task task, Step currentStep) {
        // Set the step progress
        if (task instanceof OrderedTask) {
            OrderedTask orderedTask = (OrderedTask)task;

            // This computes the base step identifier for any potential nested steps
            int lastIndexOfSubtask = currentStep.getIdentifier().lastIndexOf(".") + 1;
            String subtaskStepId = currentStep.getIdentifier()
                    .substring(lastIndexOfSubtask, currentStep.getIdentifier().length());
            int indexOfStep = indexOfSubtaskStep(orderedTask, subtaskStepId);

            int stepCount = 0;
            for (Step step : orderedTask.getSteps()) {
                if (!step.getIdentifier().equals("completion")) {
                    stepCount++;
                }
            }

            // If we could not find the step, look recursively within the Subtask steps for it
            if (indexOfStep == -1) {
                for (Step step : orderedTask.getSteps()) {
                    if (step instanceof SubtaskStep) {
                        Task subtaskTask = ((SubtaskStep)step).getSubtask();
                        Progress subTaskProgress =
                                createOrderedTaskStepProgressText(subtaskTask, currentStep);
                        if (subTaskProgress != null) {
                            return subTaskProgress;
                        }
                    }
                }
                return null;
            } else {
                int progress = indexOfStep + 1;
                return new Progress(progress, stepCount, buildStepProgressText(progress, stepCount));
            }
        } else {
            throw new IllegalStateException("MpTaskToolbar only works with OrderedTasks");
        }
    }

    private int indexOfSubtaskStep(OrderedTask task, String stepId) {
        for (int i = 0; i < task.getSteps().size(); i++) {
            if (stepId.equals(task.getSteps().get(i).getIdentifier())) {
                return i;
            }
        }
        return -1;
    }

    private CharSequence buildStepProgressText(int stepIndex, int max) {
        int progress = stepIndex;

        // Set up the text and styling of the step 1 of 5, 2 of 5, etc.
        String progressStr = String.valueOf(progress); // array index 0 should be 1
        String maxString = String.valueOf(max);
        String stepProgressStr = String.format(
                getContext().getString(R.string.mp_step_progress), progressStr, maxString);

        SpannableStringBuilder str = new SpannableStringBuilder(stepProgressStr);
        str.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                stepProgressStr.indexOf(progressStr),
                stepProgressStr.indexOf(progressStr) + progressStr.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return str;
    }

    public class Progress {
        public int progress;
        public int max;
        public CharSequence stepProgressText;

        public Progress(int progress, int max, CharSequence stepProgressText) {
            this.progress = progress;
            this.max = max;
            this.stepProgressText = stepProgressText;
        }
    }
}
