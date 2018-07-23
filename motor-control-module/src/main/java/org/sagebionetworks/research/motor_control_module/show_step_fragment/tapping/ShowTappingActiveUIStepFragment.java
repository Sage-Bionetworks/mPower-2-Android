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

import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Size;
import android.util.SizeF;
import android.view.InputDevice;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;

import org.sagebionetworks.research.domain.result.interfaces.CollectionResult;
import org.sagebionetworks.research.domain.result.interfaces.Result;
import org.sagebionetworks.research.domain.result.interfaces.TaskResult;
import org.sagebionetworks.research.mobile_ui.show_step.view.ShowActiveUIStepFragmentBase;
import org.sagebionetworks.research.mobile_ui.show_step.view.ShowStepFragmentBase;
import org.sagebionetworks.research.mobile_ui.show_step.view.view_binding.ActiveUIStepViewBinding;
import org.sagebionetworks.research.mobile_ui.widget.ActionButton;
import org.sagebionetworks.research.motor_control_module.R;
import org.sagebionetworks.research.motor_control_module.result.TappingResult;
import org.sagebionetworks.research.motor_control_module.step.HandStepHelper;
import org.sagebionetworks.research.motor_control_module.step_view.TappingStepView;
import org.sagebionetworks.research.presentation.DisplayString;
import org.sagebionetworks.research.presentation.model.interfaces.ActiveUIStepView;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;
import org.sagebionetworks.research.presentation.show_step.show_step_view_models.ShowActiveUIStepViewModel;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

// TODO: rkolmos 06/04/2018 make this fragment use the correct generics.
public class ShowTappingActiveUIStepFragment extends
        ShowActiveUIStepFragmentBase<TappingStepView, ShowActiveUIStepViewModel<TappingStepView>,
                TappingStepViewBinding> {
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

    private interface GenericTouchListener {
        boolean onTouch(View view, MotionEvent motionEvent, int buttonId);
    }

    private GenericTouchListener MOTION_LISTENER = (view, motionEvent, buttonId) -> {
        view.performClick();
        @TappingButtonIdentifier String buttonIdentifier = this.getButtonIdentifier(buttonId);
        if (motionEvent.getAction() == MotionEvent.ACTION_BUTTON_PRESS) {
            this.buttonPressed(buttonIdentifier, motionEvent);
        } else if (motionEvent.getAction() == MotionEvent.ACTION_BUTTON_RELEASE) {
            this.buttonReleased(buttonIdentifier, motionEvent);
        }

        // We have now consumed the touch and no other views should use it.
        return true;
    };

    /**
     * Returns the button identifier for the given button.
     * @param buttonId the button get the the identifer of.
     * @return the button identifier for the given button.
     */
    @NonNull
    @TappingButtonIdentifier
    protected String getButtonIdentifier(int buttonId) {
        if (buttonId == this.stepViewBinding.getLeftTapButton().getId()) {
            return TappingButtonIdentifier.LEFT;
        } else if (buttonId == this.stepViewBinding.getRightTapButton().getId()) {
            return TappingButtonIdentifier.RIGHT;
        } else {
            return TappingButtonIdentifier.NONE;
        }
    }

    /**
     * Updates the result for this step in the task to match the current state of this fragment.
     */
    private void updateTappingResult() {
        Result previousResult = this.findStepResult();
        TappingResult tappingResult = null;
        if (previousResult instanceof TappingResult) {
            tappingResult = (TappingResult)previousResult;
        } else {
            tappingResult = TappingResult.builder()
                    .setIdentifier(this.stepView.getIdentifier())
                    .setStartTime(previousResult != null ? previousResult.getStartTime() : Instant.now())
                    .build();
        }

        TappingResult updatedResult = tappingResult.toBuilder()
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

    /**
     * Called when one of the tapping button's is released.
     * @param buttonIdentifier The identifier of the button that was released.
     * @param event The motion event corresponding to the button release.
     */
    protected void buttonReleased(@TappingButtonIdentifier String buttonIdentifier, MotionEvent event) {
        int actionType = event.getAction();
        if (actionType != MotionEvent.ACTION_BUTTON_RELEASE) {
            return;
        }

        if (!this.expired && this.tappingStart != null) {
            this.updateLastSample(Instant.ofEpochMilli(event.getEventTime()), buttonIdentifier);
        }
    }

    /**
     * Called when one of the tapping button's is pressed.
     * @param buttonIdentifier The identifer of the button that was pressed.
     * @param event The motion event corresponding to the button release.
     */
    protected void buttonPressed(@TappingButtonIdentifier String buttonIdentifier, MotionEvent event) {
        int actionType = event.getAction();
        if (actionType != MotionEvent.ACTION_BUTTON_PRESS) {
            return;
        }

        if (this.tappingStart == null) {
            this.hitButtonCount = 0;
            this.tappingStart = Instant.ofEpochMilli(event.getEventTime());
        }

        // TODO say the word tap if accessibility voice is turned on.
        this.lastTappedButton = buttonIdentifier;
        this.createSample(event, buttonIdentifier);
        // update the tap count
        if (!buttonIdentifier.equals(TappingButtonIdentifier.NONE)) {
            this.hitButtonCount++;
        }
        // TODO use the tapping count label instead of the count label.
        TextView tappingCountLabel = this.stepViewBinding.getCountdownLabel();
        if (tappingCountLabel != null) {
            String text = this.hitButtonCount + "";
            tappingCountLabel.setText(text);
        }
    }

    /**
     * Creates a sample for the given motion event and buttonIdentifier
     * @param event The event to create a sample for.
     * @param buttonIdentifier The identifier of the button to create the sample for.
     */
    protected void createSample(MotionEvent event, @TappingButtonIdentifier String buttonIdentifier) {
        if (this.expired || this.tappingStart == null) {
            return;
        }

        Instant uptime = Instant.ofEpochMilli(event.getEventTime());
        TappingSample tappingSample = TappingSample.builder()
                .setUptime(uptime)
                .setTimestampe(uptime.minusMillis(this.tappingStart.toEpochMilli()))
                .setButtonIdentifier(buttonIdentifier)
                .setLocation(new Point((int)event.getX(), (int)event.getY()))
                .setDuration(Duration.ofMillis(0))
                .build();

        this.samples.add(tappingSample);
        this.lastSample.put(buttonIdentifier, tappingSample);
    }

    protected void updateLastSample(Instant timestamp, @TappingButtonIdentifier String buttonIdentifier) {
        final TappingSample lastSample = this.lastSample.get(buttonIdentifier);
        int lastIndex = lastIndexMathcing(this.samples, lastSample);
        if (lastSample == null || lastIndex == -1) {
            return;
        }

        this.lastSample.put(buttonIdentifier, null);
        TappingSample updatedSample = lastSample.toBuilder()
                .setDuration(Duration.ofMillis((timestamp.toEpochMilli() - lastSample.getUptime().toEpochMilli())))
                .build();
        this.samples.set(lastIndex, updatedSample);
    }

    protected static int lastIndexMathcing(List<TappingSample> samples, TappingSample lastSample) {
        int lastIndex = -1;
        for (int i = 0; i < samples.size(); i++) {
            TappingSample sample = samples.get(i);
            if (sample.getUptime().equals(lastSample.getUptime())
                    && sample.getButtonIdentifier().equals(lastSample.getButtonIdentifier())) {
                lastIndex = i;
            }
        }

        return lastIndex;
    }

    protected void tappingFinished(Instant timestamp) {
        this.expired = true;
        this.updateLastSample(timestamp, TappingButtonIdentifier.LEFT);
        this.updateLastSample(timestamp, TappingButtonIdentifier.RIGHT);
        this.updateTappingResult();
    }

    @NonNull
    public static ShowTappingActiveUIStepFragment newInstance(@NonNull StepView stepView) {
        ShowTappingActiveUIStepFragment fragment = new ShowTappingActiveUIStepFragment();
        Bundle arguments = ShowStepFragmentBase.createArguments(stepView);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        View view = this.stepViewBinding.getRootView();
        this.viewSize = new Point(view.getWidth(), view.getHeight());
        ActionButton leftButton = this.stepViewBinding.getLeftTapButton();
        leftButton.setOnGenericMotionListener((targetView, motionEvent) ->
                MOTION_LISTENER.onTouch(targetView, motionEvent, leftButton.getId()));
        this.buttonRect1 = new Rect();
        this.buttonRect1.set(leftButton.getLeft(), leftButton.getTop(), leftButton.getRight(), leftButton.getBottom());
        ActionButton rightButton = this.stepViewBinding.getRightTapButton();
        rightButton.setOnGenericMotionListener((targetView, motionEvent) ->
                MOTION_LISTENER.onTouch(targetView, motionEvent, rightButton.getId()));
        this.buttonRect2 = new Rect();
        this.buttonRect2.set(rightButton.getLeft(), rightButton.getTop(), rightButton.getRight(), rightButton.getBottom());
        view.setOnGenericMotionListener((targetView, motionEvent) ->
                MOTION_LISTENER.onTouch(targetView, motionEvent, 0));
        this.stepViewBinding.getCountdownLabel().setText(R.string.tap_count_label);
        // Hide the navigation action bar.
        this.stepViewBinding.getNavigationActionBar().setAlpha(0f);
        this.setNextButtonLabel();
    }

    private void setNextButtonLabel() {
        HandStepHelper.Hand hand = HandStepHelper.whichHand(this.stepView.getIdentifier());
        TaskResult taskResult = this.performTaskViewModel.getTaskResult().getValue();
        if (hand != null && taskResult != null) {
            List<String> handOrder = HandStepHelper.getHandOrder(taskResult);
            if (handOrder != null) {
                String last = handOrder.size() == 2 ? handOrder.get(1) : handOrder.get(0);
                if (!hand.toString().equals(last)) {
                    DisplayString defaultString = DisplayString.create(R.string.tapping_continue_with_text, null);
                    DisplayString nextButtonText = HandStepHelper.getHandString(defaultString, hand.getOtherHand().toString());
                    this.stepViewBinding.getNextButton().setText(nextButtonText.getDisplayString());
                }
            }
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
}
