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

package org.sagebionetworks.research.mpower.researchstack.framework;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;

import org.sagebionetworks.researchstack.backbone.StorageAccess;
import org.sagebionetworks.researchstack.backbone.result.TaskResult;
import org.sagebionetworks.researchstack.backbone.step.Step;
import org.sagebionetworks.researchstack.backbone.ui.ViewTaskActivity;
import org.sagebionetworks.researchstack.backbone.ui.step.layout.ActiveStepLayout;
import org.sagebionetworks.researchstack.backbone.ui.step.layout.StepLayout;

import org.sagebionetworks.research.mpower.researchstack.R;
import org.sagebionetworks.research.mpower.researchstack.framework.step.toolbar.MpTaskToolbar;
import org.sagebionetworks.research.mpower.researchstack.framework.step.toolbar.MpTaskToolbarActionManipulator;
import org.sagebionetworks.research.mpower.researchstack.framework.step.toolbar.MpTaskToolbarIconManipulator;
import org.sagebionetworks.research.mpower.researchstack.inject.MPowerResearchStackModule;

/**
 * Created by mdephillips on 12/11/17, edited to pull into the mPower RS framework on 10/14/18.
 *
 * The MpViewTaskActivity is a ViewTaskActivity that is themed with a Sage style tool bar and footer view.
 * It supports customization of the status bar, tool bar, and several view layouts by any StepLayout.
 */

public class MpViewTaskActivity extends ViewTaskActivity {

    protected ViewGroup toolbarContainer;
    protected TextView stepProgressTextView;

    protected MpTaskToolbar getToolbar() {
        if (toolbar != null && toolbar instanceof MpTaskToolbar) {
            return (MpTaskToolbar)toolbar;
        }
        return null;
    }

    @Override
    public void onDataAuth() {
        storageAccessUnregister();
        MPowerResearchStackModule.mockAuthenticate(this);
        super.onDataReady();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        boolean superResult = super.onCreateOptionsMenu(menu);
        // Needed to initialize the toolbar icons after the menu is inflated
        getToolbar().refreshToolbarIcons(currentStepLayout, getSupportActionBar());
        return superResult;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: mdephillips 10/12/18 we probably don't need this
        // Adjust font scale per specific device
        //TextUtils.adjustFontScale(getResources().getConfiguration(), this, BpMainActivity.MAX_FONT_SCALE);

        stepProgressTextView = findViewById(R.id.bp_step_progress_textview);

        toolbarContainer = findViewById(R.id.bp_toolbar_container);
        MpTaskToolbar toolbar = getToolbar();
        toolbar.setDefaultLeftIcon(R.drawable.mp_cancel_icon);
        toolbar.setDefaultRightIcon(MpTaskToolbarIconManipulator.NO_ICON);
        toolbar.setDefaultTintColor(R.color.white);
        toolbar.setDefaultBehindToolbar(false);
        toolbar.setDefaultStatusBarColor(R.color.sageResearchPrimary);
    }

    @Override
    public @IdRes
    int getViewSwitcherRootId() {
        return R.id.bp_step_switcher;
    }

    @Override
    public int getContentViewId() {
        return R.layout.mp_activity_view_task;
    }

    @Override
    public @IdRes int getToolbarResourceId() {
        return R.id.bp_task_toolbar;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        toolbarItemClicked(item, currentStepLayout, this);
        return true;
    }

    @Override
    public void showStep(Step step, boolean alwaysReplaceView) {
        super.showStep(step, alwaysReplaceView);

        MpTaskToolbar toolbar = getToolbar();
        toolbar.refreshToolbarIcons(currentStepLayout, getSupportActionBar());
        toolbar.refreshToolbarBackgroundColor(currentStepLayout, toolbarContainer);
        toolbar.refreshViewBehindToolbar(root, currentStepLayout);
        toolbar.refreshViewHideToolbar(currentStepLayout, toolbarContainer);

        MpTaskToolbar.Progress stepProgressText = toolbar.createOrderedTaskStepProgressText(task, currentStep);
        toolbar.refreshStepProgress(currentStepLayout, stepProgressTextView, stepProgressText);

        toolbar.setStatusBarColor(this, currentStepLayout);

        MpViewTaskActivity.refreshVolumeControl(this, currentStepLayout);
        MpViewTaskActivity.callTaskResultListener(taskResult, currentStepLayout);
    }

    /**
     * Helper method for when the toolbar item is clicked to be re-used in other ViewTaskActivitys
     */
    public static boolean toolbarItemClicked(
            MenuItem item, StepLayout current, ViewTaskActivity activity) {

        boolean clickWasConsumed = false;
        // Allow for customization of the toolbar
        if (current instanceof MpTaskToolbarActionManipulator) {
            MpTaskToolbarActionManipulator manipulator = (MpTaskToolbarActionManipulator) current;
            if(item.getItemId() == org.sagebionetworks.researchstack.backbone.R.id.rsb_clear_menu_item) {
                return manipulator.bpToolbarRightIconClicked();
            } else if (item.getItemId() == android.R.id.home) {
                clickWasConsumed = manipulator.bpToolbarLeftIconClicked();
            }
        }

        if(!clickWasConsumed && item.getItemId() == android.R.id.home) {
            activity.showConfirmExitDialog();
            return true;
        }

        return clickWasConsumed;
    }

    /**
     * Helper method to call the task result listener for a step layout
     * @param taskResult current task result for task activity
     * @param current step layout
     */
    public static void callTaskResultListener(TaskResult taskResult, StepLayout current) {
        // Let steps know about the task result if it needs to
        if (current instanceof MpResultListener) {
            ((MpResultListener)current).taskResult(taskResult);
        }
    }

    /**
     * Helper method for refreshing the volume control for a task activity
     * @param taskActivity that is displaying the step layout
     * @param current step layout
     */
    public static void refreshVolumeControl(Activity taskActivity, StepLayout current) {
        // Media Volume controls
        int streamType = AudioManager.USE_DEFAULT_STREAM_TYPE;
        if (current instanceof MediaVolumeController) {
            if (((MediaVolumeController)current).controlMediaVolume()) {
                streamType = AudioManager.STREAM_MUSIC;
            }
        } else if (current instanceof ActiveStepLayout) {
            // ActiveStepLayouts have verbal spoken instructions
            streamType = AudioManager.STREAM_MUSIC;
        }
        taskActivity.setVolumeControlStream(streamType);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            StepLayout layout = getCurrentStepLayout();
            if(layout instanceof MpActivityResultListener) {
                ((MpActivityResultListener)layout).onActivityFinished(requestCode, resultCode, data);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public interface MediaVolumeController {
        /**
         * @return if true, volume buttons will control media, not ringer
         */
        boolean controlMediaVolume();
    }
}
