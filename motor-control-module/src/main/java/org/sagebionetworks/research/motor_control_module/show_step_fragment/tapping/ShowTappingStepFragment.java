/*
 * Copyright 2015 Apple Inc.
 * Ported to Android from ResearchKit/ResearchKit 1.5
 */
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

package org.sagebionetworks.research.motor_control_module.show_step_fragment.tapping;

import android.arch.lifecycle.Observer;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;

import org.sagebionetworks.research.domain.result.interfaces.CollectionResult;
import org.sagebionetworks.research.domain.result.interfaces.Result;
import org.sagebionetworks.research.domain.result.interfaces.TaskResult;
import org.sagebionetworks.research.domain.task.Task;
import org.sagebionetworks.research.mobile_ui.show_step.view.ShowActiveUIStepFragmentBase;
import org.sagebionetworks.research.mobile_ui.show_step.view.ShowStepFragmentBase;
import org.sagebionetworks.research.mobile_ui.widget.ActionButton;
import org.sagebionetworks.research.motor_control_module.R;
import org.sagebionetworks.research.motor_control_module.result.TappingResult;
import org.sagebionetworks.research.motor_control_module.show_step_fragment.HandStepUIHelper;
import org.sagebionetworks.research.motor_control_module.step.HandStepHelper;
import org.sagebionetworks.research.motor_control_module.step_view.TappingStepView;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;
import org.sagebionetworks.research.presentation.show_step.show_step_view_models.ShowActiveUIStepViewModel;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShowTappingStepFragment extends
        ShowActiveUIStepFragmentBase<TappingStepView, ShowActiveUIStepViewModel<TappingStepView>,
                TappingStepViewBinding> {
    private String nextButtonTitle;
    private List<TappingSample> samples;
    private Instant tappingStart;
    private boolean expired;
    private Rect buttonRect1;
    private Rect buttonRect2;
    // View Size is stored as a Point to avoid pre API 21 issues.
    private Point viewSize;
    private int hitButtonCount;
    @TappingButtonIdentifier
    private String lastTappedButton;
    private Map<String, TappingSample> lastSample;


    // region Fragment
    @NonNull
    public static ShowTappingStepFragment newInstance(@NonNull StepView stepView) {
        ShowTappingStepFragment fragment = new ShowTappingStepFragment();
        Bundle arguments = ShowStepFragmentBase.createArguments(stepView);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void update(TappingStepView stepView) {
        super.update(stepView);
        TaskResult taskResult = this.performTaskViewModel.getTaskResult().getValue();
        HandStepUIHelper.update(taskResult, stepView, this.stepViewBinding);
        // Underline the skip button
        ActionButton skipButton = this.stepViewBinding.getSkipButton();
        if (skipButton != null) {
            skipButton.setPaintFlags(skipButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.mpower2_tapping_step;
    }

    @NonNull
    @Override
    protected TappingStepViewBinding instantiateAndBindBinding(View view) {
        return new TappingStepViewBinding(view);
    }

    @Override
    @Nullable
    protected Observer<Long> getCountdownObserver() {
        // The countdown observer now calls tapping finished when the countdown finishes and doesn't
        // update the count label.
        return count -> {
           if (count != null && count == 0) {
               this.tappingFinished(Instant.now());
           }
        };
    }
    // endregion

    // region Fragment Lifecycle
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // onCreate initializes samples and lastSample.
        super.onCreate(savedInstanceState);
        this.samples = new ArrayList<>();
        this.lastSample = new HashMap<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        // onCreateView registers the listeners for the tapping buttons, and overall view.
        View result = super.onCreateView(inflater, viewGroup, savedInstanceState);
        this.stepViewBinding.getLeftTapButton().setOnTouchListener((targetView, motionEvent) -> {
            targetView.performClick();
            this.handleMotionEvent(TappingButtonIdentifier.LEFT, motionEvent);
            return true;
        });

        this.stepViewBinding.getRightTapButton().setOnTouchListener((targetView, motionEvent) -> {
            targetView.performClick();
            this.handleMotionEvent(TappingButtonIdentifier.RIGHT, motionEvent);
            return true;
        });

        this.stepViewBinding.getUnitLabel().setText(R.string.tap_count_label);

        this.stepViewBinding.getRootView().setOnTouchListener((targetView, motionEvent) -> {
            targetView.performClick();
            this.handleMotionEvent(TappingButtonIdentifier.NONE, motionEvent);
            return true;
        });

        String countLabelText = this.hitButtonCount + "";
        this.stepViewBinding.getCountLabel().setText(countLabelText);
        // Hide the navigation action bar, so the user cannot press navigation buttons until the tapping
        // is finished.
        this.stepViewBinding.getNavigationActionBar().setVisibility(View.GONE);
        this.stepViewBinding.getNavigationActionBar().setAlpha(0f);
        this.updateTapCountLabel();
        // Add a PreDrawListener that updates the locations of the buttons for the tapping result.
        this.stepViewBinding.getRootView().getViewTreeObserver()
                .addOnPreDrawListener(this::updateButtonBounds);
        this.nextButtonTitle = this.getNextButtonLabel();
        return result;
    }

    /**
     * Private helper that can function as a PreDrawListener that updates the positions of the buttons,
     * and root view on the screen for the tapping result. Always returns true so drawing proceeds as normal.
     * @return true so drawing always happens as normal when used as a PreDrawListener.
     */
    private boolean updateButtonBounds() {
        ActionButton leftButton = this.stepViewBinding.getLeftTapButton();
        if (leftButton != null) {
            this.buttonRect1 = new Rect();
            this.buttonRect1.set(leftButton.getLeft(), leftButton.getTop(), leftButton.getRight(), leftButton.getBottom());
        }

        ActionButton rightButton = this.stepViewBinding.getRightTapButton();
        if (rightButton != null) {
            this.buttonRect2 = new Rect();
            this.buttonRect2.set(rightButton.getLeft(), rightButton.getTop(), rightButton.getRight(), rightButton.getBottom());
        }

        View view = this.stepViewBinding.getRootView();
        if (view != null) {
            this.viewSize = new Point(view.getWidth(), view.getHeight());
        }

        // Always return true to proceed with drawing.
        return true;
    }
    // endregion

    // region Samples
    // TODO rkolmos 08/16/2018 move this to the view model
    /**
     * Returns true if the user is currently tapping, false otherwise.
     * @return true if the user is currently tapping, false otherwise.
     */
    private boolean userIsTapping() {
        return !this.expired && this.tappingStart != null;
    }
    
    // TODO rkolmos 08/16/2018 move this to the view model
    /**
     * Creates a sample for the given motion event and buttonIdentifier
     * @param event The event to create a sample for.
     * @param buttonIdentifier The identifier of the button to create the sample for.
     */
    private void createSample(MotionEvent event, @TappingButtonIdentifier String buttonIdentifier) {
        if (!this.userIsTapping()) {
            return;
        }

        Instant uptime = Instant.ofEpochMilli(event.getEventTime());
        TappingSample tappingSample = TappingSample.builder()
                .setUptime(uptime)
                .setTimestamp(uptime.minusMillis(this.tappingStart.toEpochMilli()))
                .setButtonIdentifier(buttonIdentifier)
                // TODO create a real step pat:h.
                .setStepPath(this.stepView.getIdentifier())
                .setLocation(new Point((int)event.getX(), (int)event.getY()))
                .setDuration(Duration.ofMillis(0))
                .build();

        this.samples.add(tappingSample);
        this.lastSample.put(buttonIdentifier, tappingSample);
    }

    /**
     * Updates the lastSample using the given timestamp as the end of the sample.
     * @param timestamp The timestamp of when the sample should end.
     * @param buttonIdentifier The identifier of the button which corresponds to the sample.
     */
    private void updateLastSample(Instant timestamp, @TappingButtonIdentifier String buttonIdentifier) {
        final TappingSample lastSample = this.lastSample.get(buttonIdentifier);
        if (lastSample == null) {
            return;
        }

        int lastIndex = this.lastIndexMatching(lastSample);
        if (lastIndex == -1) {
            return;
        }

        this.lastSample.put(buttonIdentifier, null);
        TappingSample updatedSample = lastSample.toBuilder()
                .setDuration(Duration.ofMillis((timestamp.toEpochMilli() - lastSample.getUptime().toEpochMilli())))
                .build();
        this.samples.set(lastIndex, updatedSample);
    }

    /**
     * Returns the last index in samples of a TappingSample that matches the given sample.
     * @param sample The sample to get the last matching index of.
     * @return the last index in samples of a TappingSample that matches the given sample.
     */
    private int lastIndexMatching(TappingSample sample) {
        int lastIndex = -1;
        for (int i = 0; i < this.samples.size(); i++) {
            TappingSample currentSample = this.samples.get(i);
            if (currentSample.getUptime().equals(sample.getUptime())
                    && currentSample.getButtonIdentifier().equals(sample.getButtonIdentifier())) {
                lastIndex = i;
            }
        }

        return lastIndex;
    }
    // endregion

    // region Results
    /**
     * Updates the result for this step in the task to match the current state of this fragment.
     */
    private void updateTappingResult() {
        Result previousResult = this.findStepResult();
        String identifier;
        Instant startTime;
        if (previousResult instanceof TappingResult) {
            // If the previous result is a TappingResult we use it's identifier and startTime.
            TappingResult tappingResult = (TappingResult)previousResult;
            identifier = tappingResult.getIdentifier();
            startTime = tappingResult.getStartTime();
        } else {
            // Otherwise we default to the stepView's identifier, and now as the startTime.
            identifier = this.stepView.getIdentifier();
            startTime = previousResult != null ? previousResult.getStartTime() : Instant.now();
        }

        TappingResult updatedResult = TappingResult.builder()
                .setIdentifier(identifier)
                .setStartTime(startTime)
                .setEndTime(Instant.now())
                .setButtonRect1(this.buttonRect1)
                .setButtonRect2(this.buttonRect2)
                .setStepViewSize(this.viewSize)
                .setSamples(ImmutableList.copyOf(this.samples))
                .build();

        if (previousResult instanceof CollectionResult) {
            // If the step previously had a collection result we append the tapping result to it.
            CollectionResult collectionResult = (CollectionResult) previousResult;
            collectionResult = collectionResult.appendInputResult(updatedResult);
            this.performTaskViewModel.addStepResult(collectionResult);
        } else {
            // Otherwise we directly add the tapping result to the step history.
            this.performTaskViewModel.addStepResult(updatedResult);
        }
    }
    // endregion

    // region User Input Listeners
    /**
     * Called when a MotionEvent has occurred.
     * @param buttonIdentifier The identifier of the button that has been tapped.
     * @param motionEvent The MotionEvent that occurred.
     */
    private void handleMotionEvent(@TappingButtonIdentifier String buttonIdentifier,
                                   MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            this.buttonPressed(buttonIdentifier, motionEvent);
        } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            this.buttonReleased(buttonIdentifier, motionEvent);
        }
    }

    /**
     * Called when one of the tapping button's is pressed.
     * @param buttonIdentifier The identifer of the button that was pressed.
     * @param event The motion event corresponding to the button release.
     */
    private void buttonPressed(@TappingButtonIdentifier String buttonIdentifier, MotionEvent event) {
        int actionType = event.getAction();
        if (actionType != MotionEvent.ACTION_DOWN) {
            return;
        }

        if (this.tappingStart == null) {
            this.hitButtonCount = 0;
            this.tappingStart = Instant.ofEpochMilli(event.getEventTime());
            this.startCountdown();
        }

        if (this.lastTappedButton != buttonIdentifier) {
            // TODO say the word tap if accessibility voice is turned on.
        }

        this.lastTappedButton = buttonIdentifier;

        this.createSample(event, buttonIdentifier);
        // update the tap count
        if (!buttonIdentifier.equals(TappingButtonIdentifier.NONE) && !this.expired) {
            this.hitButtonCount++;
        }

        this.updateTapCountLabel();
    }

    /**
     * Called when one of the tapping button's is released.
     * @param buttonIdentifier The identifier of the button that was released.
     * @param event The motion event corresponding to the button release.
     */
    private void buttonReleased(@TappingButtonIdentifier String buttonIdentifier, MotionEvent event) {
        int actionType = event.getAction();
        if (actionType != MotionEvent.ACTION_UP) {
            return;
        }

        if (!this.expired && this.tappingStart != null) {
            this.updateLastSample(Instant.ofEpochMilli(event.getEventTime()), buttonIdentifier);
        }
    }
    // endregion

    // region UI
    /**
     * Updates the count label to display the number of times the user has hit a tap button.
     */
    private void updateTapCountLabel() {
        TextView tappingCountLabel = this.stepViewBinding.getCountLabel();
        if (tappingCountLabel != null) {
            String text = this.hitButtonCount + "";
            tappingCountLabel.setText(text);
        }
    }

    /**
     * Returns the String to display on the nextButton, either "Continue with <left or right> hand" or
     * or "Next" depending on whether there is another hand that is supposed to go after this one.
     * @return the String to display on the nextButton.
     */
    private String getNextButtonLabel() {
        TaskResult taskResult = this.performTaskViewModel.getTaskResult().getValue();
        if (taskResult != null) {
            HandStepHelper.Hand thisHand = HandStepHelper.whichHand(this.stepView.getIdentifier());
            if (thisHand != null) {
                List<String> handOrder = HandStepHelper.getHandOrder(taskResult);
                if (handOrder != null) {
                    String last = handOrder.size() == 2 ? handOrder.get(1) : handOrder.get(0);
                    if (!thisHand.toString().equals(last)) {
                        String nextButtonString = this.getResources().getString(R.string.tapping_continue_with_text);
                        return nextButtonString.replaceAll(HandStepHelper.JSON_PLACEHOLDER, last);

                    }
                }
            }
        }

        return this.getResources().getString(R.string.tapping_continue);
    }
    // endregion

    /**
     * Called when this tapping step finishes.
     * @param timestamp The Instant that tapping step finished  at.
     */
    private void tappingFinished(Instant timestamp) {
        this.expired = true;
        this.updateLastSample(timestamp, TappingButtonIdentifier.LEFT);
        this.updateLastSample(timestamp, TappingButtonIdentifier.RIGHT);
        this.updateTappingResult();
        this.stepViewBinding.getNavigationActionBar().setVisibility(View.VISIBLE);
        this.stepViewBinding.getNextButton().setText(this.nextButtonTitle);
        this.stepViewBinding.getTappingButtonView().animate().alpha(0f).setDuration(300)
                .withEndAction(() ->  {
                    this.stepViewBinding.getLeftTapButton().setVisibility(View.GONE);
                    this.stepViewBinding.getRightTapButton().setVisibility(View.GONE);
                })
                .start();
        this.stepViewBinding.getNavigationActionBar().animate().alpha(1f).setDuration(300).start();
    }
}
