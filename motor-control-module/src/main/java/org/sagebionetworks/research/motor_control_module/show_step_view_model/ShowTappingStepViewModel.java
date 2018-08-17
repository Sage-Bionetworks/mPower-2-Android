package org.sagebionetworks.research.motor_control_module.show_step_view_model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.MotionEvent;

import com.google.common.collect.ImmutableList;

import org.sagebionetworks.research.domain.result.interfaces.CollectionResult;
import org.sagebionetworks.research.domain.result.interfaces.Result;
import org.sagebionetworks.research.domain.result.interfaces.TaskResult;
import org.sagebionetworks.research.motor_control_module.R;
import org.sagebionetworks.research.motor_control_module.result.TappingResult;
import org.sagebionetworks.research.motor_control_module.show_step_fragment.tapping.TappingButtonIdentifier;
import org.sagebionetworks.research.motor_control_module.show_step_fragment.tapping.TappingSample;
import org.sagebionetworks.research.motor_control_module.step.HandStepHelper;
import org.sagebionetworks.research.motor_control_module.step_view.TappingStepView;
import org.sagebionetworks.research.presentation.perform_task.PerformTaskViewModel;
import org.sagebionetworks.research.presentation.show_step.show_step_view_models.ShowActiveUIStepViewModel;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShowTappingStepViewModel extends ShowActiveUIStepViewModel<TappingStepView> {
    LiveData<Boolean> expired;
    List<TappingSample> samples;
    private Map<String, TappingSample> lastSample;
    MutableLiveData<Instant> tappingStart;
    MutableLiveData<Rect> buttonRect1;
    MutableLiveData<Rect> buttonRect2;
    MutableLiveData<Point> viewSize;
    MutableLiveData<Integer> hitButtonCount;
    MutableLiveData<String> lastTappedButtonIdentifier;


    public ShowTappingStepViewModel(
            final PerformTaskViewModel performTaskViewModel,
            final TappingStepView stepView) {
        super(performTaskViewModel, stepView);
        this.expired = Transformations.map(this.countdown, (count) -> count != null && count == 0);
        this.samples = new ArrayList<>();
        this.lastSample = new HashMap<>();
        this.tappingStart = new MutableLiveData<>();
        this.buttonRect1 = new MutableLiveData<>();
        this.buttonRect2 = new MutableLiveData<>();
        this.viewSize = new MutableLiveData<>();
        this.hitButtonCount = new MutableLiveData<>();
        this.lastTappedButtonIdentifier = new MutableLiveData<>();
    }

    public boolean userIsTapping() {
        return !this.expired.getValue() && this.tappingStart.getValue() != null;
    }

    public LiveData<String> getLastTappedButtonIdentifier() {
        return this.lastTappedButtonIdentifier;
    }

    public LiveData<Integer> getHitButtonCount() {
        return hitButtonCount;
    }

    public LiveData<Boolean> isExpired() {
        return this.expired;
    }

    public MutableLiveData<Instant> getTappingStart() {
        return this.tappingStart;
    }

    public MutableLiveData<Rect> getButtonRect1() {
        return this.buttonRect1;
    }

    public MutableLiveData<Rect> getButtonRect2() {
        return this.buttonRect2;
    }

    public MutableLiveData<Point> getViewSize() {
        return this.viewSize;
    }

    private void tappingFinished(Instant timestamp) {
        this.updateLastSample(timestamp, TappingButtonIdentifier.LEFT);
        this.updateLastSample(timestamp, TappingButtonIdentifier.RIGHT);
        this.updateTappingResult();
    }

    public void handleButtonPress(@TappingButtonIdentifier String buttonIdentifier, @NonNull MotionEvent event) {
        if (this.tappingStart.getValue() == null) {
            this.hitButtonCount.setValue(0);
            this.tappingStart.setValue(Instant.ofEpochMilli(event.getEventTime()));
        }

        this.createSample(event, buttonIdentifier);
        // update the tap count
        if (!buttonIdentifier.equals(TappingButtonIdentifier.NONE) && !this.isExpired().getValue()) {
            this.hitButtonCount.setValue(this.hitButtonCount.getValue() + 1);
        }
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

    /**
     * Creates a sample for the given motion event and buttonIdentifier
     * @param event The event to create a sample for.
     * @param buttonIdentifier The identifier of the button to create the sample for.
     */
    public void createSample(MotionEvent event, @TappingButtonIdentifier String buttonIdentifier) {
        if (!this.userIsTapping()) {
            return;
        }

        Instant uptime = Instant.ofEpochMilli(event.getEventTime());
        TappingSample tappingSample = TappingSample.builder()
                .setUptime(uptime)
                .setTimestamp(uptime.minusMillis(this.tappingStart.getValue().toEpochMilli()))
                .setButtonIdentifier(buttonIdentifier)
                // TODO create a real step path.
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
    public void updateLastSample(Instant timestamp, @TappingButtonIdentifier String buttonIdentifier) {
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
     * Returns the String to display on the nextButton, either "Continue with <left or right> hand" or
     * or "Next" depending on whether there is another hand that is supposed to go after this one.
     * @return the String to display on the nextButton.
     */
    @StringRes
    public int getNextButtonLabel() {
        TaskResult taskResult = this.performTaskViewModel.getTaskResult().getValue();
        if (taskResult != null) {
            HandStepHelper.Hand thisHand = HandStepHelper.whichHand(this.stepView.getIdentifier());
            if (thisHand != null) {
                List<String> handOrder = HandStepHelper.getHandOrder(taskResult);
                if (handOrder != null) {
                    String last = handOrder.size() == 2 ? handOrder.get(1) : handOrder.get(0);
                    if (!thisHand.toString().equals(last)) {
                        return R.string.tapping_continue_with_text;
                    }
                }
            }
        }

        return R.string.tapping_continue;
    }

    /**
     * Updates the result for this step in the task to match the current state of this fragment.
     */
    public void updateTappingResult() {
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
                .setButtonRect1(this.buttonRect1.getValue())
                .setButtonRect2(this.buttonRect2.getValue())
                .setStepViewSize(this.viewSize.getValue())
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
}
