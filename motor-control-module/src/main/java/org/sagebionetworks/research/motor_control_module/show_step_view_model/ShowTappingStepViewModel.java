package org.sagebionetworks.research.motor_control_module.show_step_view_model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;
import android.support.annotation.StringRes;

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
import org.threeten.bp.ZonedDateTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShowTappingStepViewModel extends ShowActiveUIStepViewModel<TappingStepView> {

    MutableLiveData<Rect> buttonRect1;

    MutableLiveData<Rect> buttonRect2;

    LiveData<Boolean> expired;

    MutableLiveData<Integer> hitButtonCount;

    MutableLiveData<String> lastTappedButtonIdentifier;

    long tappingStartInUptimeMillis;

    final TappingResult.Builder tappingResultBuilder;

    public void setViewSize(@NonNull @Size(2) final int[] viewSize) {
        this.viewSize = viewSize;
    }

    int[] viewSize;

    private Map<String, TappingSample> buttonToLastSample;


    public ShowTappingStepViewModel(
            final PerformTaskViewModel performTaskViewModel,
            final TappingStepView stepView) {
        super(performTaskViewModel, stepView);
        this.expired = Transformations.map(this.countdown, (count) -> count != null && count == 0);
        this.buttonToLastSample = new HashMap<>();
        this.tappingStartInUptimeMillis = -1;
        this.buttonRect1 = new MutableLiveData<>();
        this.buttonRect2 = new MutableLiveData<>();
        this.viewSize = new int[2];
        this.hitButtonCount = new MutableLiveData<>();
        this.hitButtonCount.setValue(0);
        this.lastTappedButtonIdentifier = new MutableLiveData<>();

        ZonedDateTime zonedStart = ZonedDateTime.now();
        tappingResultBuilder = TappingResult.builder()
                .setZonedStartTime(zonedStart)
                .setStartTime(zonedStart.toInstant())
                .setIdentifier(stepView.getIdentifier());
    }

    /**
     * Creates a sample for the given motion event and buttonIdentifier
     * <p>
     * Uses top-left -> bottom-right coordinate system.
     *
     * @param buttonIdentifier
     *         The identifier of the button to create the sample for.
     * @param eventTimeInUptimeMillis touch event time in uptime millis
     * @param xCoord x coordinate of touch
     * @param yCoord y coordinate of touch
     */
    public void createSample(@TappingButtonIdentifier String buttonIdentifier,
            long eventTimeInUptimeMillis, float xCoord, float yCoord) {
        if (!this.userIsTapping()) {
            return;
        }

        TappingSample tappingSample = TappingSample.builder()
                .setUptime(eventTimeInUptimeMillis)
                .setTimestamp(eventTimeInUptimeMillis - tappingStartInUptimeMillis)
                .setButtonIdentifier(buttonIdentifier)
                // TODO create a real step path.
                .setStepPath(this.stepView.getIdentifier())
                .setLocation(new float[]{xCoord, yCoord}) // assumes touch screen, unit in display pixels
                .setDuration(0)
                .build();

        this.buttonToLastSample.put(buttonIdentifier, tappingSample);
    }


    public LiveData<Integer> getHitButtonCount() {
        return hitButtonCount;
    }

    public LiveData<String> getLastTappedButtonIdentifier() {
        return this.lastTappedButtonIdentifier;
    }

    /**
     * Returns the String to display on the nextButton, either "Continue with <left or right> hand" or or "Next"
     * depending on whether there is another hand that is supposed to go after this one.
     *
     * @return the String to display on the nextButton.
     */
    @StringRes
    public int getNextButtonLabel() {
        TaskResult taskResult = this.performTaskViewModel.getTaskResult();
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


    public void handleButtonPress(@TappingButtonIdentifier String buttonIdentifier, long eventTimeInUptimeMillis, float xCoord, float yCoord) {
        if (this.tappingStartInUptimeMillis < 0) {
            this.startCountdown();
            this.hitButtonCount.setValue(0);
            this.tappingStartInUptimeMillis = eventTimeInUptimeMillis;
        }

        this.createSample(buttonIdentifier, eventTimeInUptimeMillis, xCoord, yCoord);
        @TappingButtonIdentifier String nextButton = getNextButton(lastTappedButtonIdentifier.getValue(), buttonIdentifier);
        if (nextButton != null && nextButton.equals(buttonIdentifier)) {
            hitButtonCount.setValue(hitButtonCount.getValue() + 1);
            lastTappedButtonIdentifier.setValue(nextButton);
        }
    }

    @TappingButtonIdentifier
    @Nullable
    private String getNextButton(@TappingButtonIdentifier String previousButton, @NonNull @TappingButtonIdentifier String buttonIdentifier) {
        if (previousButton != null) {
            return previousButton.equals(TappingButtonIdentifier.LEFT) ? TappingButtonIdentifier.RIGHT : TappingButtonIdentifier.LEFT;
        } else {
            return !buttonIdentifier.equals(TappingButtonIdentifier.NONE) ? buttonIdentifier : null;
        }
    }

    public LiveData<Boolean> isExpired() {
        return this.expired;
    }

    public void setTappingButtonBounds(int buttonId, int[] topLeftWidthHeight) {
        if (buttonId == R.id.rightTappingButton) {
            tappingResultBuilder.setButtonBoundRight(topLeftWidthHeight);

        } else if (buttonId == R.id.leftTappingButton) {
            tappingResultBuilder.setButtonBoundLeft(topLeftWidthHeight);
        }
    }

    /**
     * Updates the buttonToLastSample using the given timestamp as the end of the sample.
     *
     * @param eventTimeInUptimeMillis
     *         The time in uptime millis of when the sample should end.
     * @param buttonIdentifier
     *         The identifier of the button which corresponds to the sample.
     */
    public void handleButtonUp(long eventTimeInUptimeMillis, @TappingButtonIdentifier String buttonIdentifier) {
        final TappingSample lastSample = this.buttonToLastSample.get(buttonIdentifier);
        if (lastSample == null) {
            return;
        }

        this.buttonToLastSample.put(buttonIdentifier, null);

        TappingSample updatedSample = lastSample.toBuilder()
                .setDuration((double) eventTimeInUptimeMillis / 1_000 - lastSample.getUptime())
                .build();

        tappingResultBuilder
                .samplesBuilder()
                .add(updatedSample);
    }

    /**
     * Updates the result for this step in the task to match the current state of this fragment.
     */
    public void updateTappingResult() {
        ZonedDateTime end = ZonedDateTime.now();
        tappingResultBuilder
                .setZonedEndTime(end)
                .setEndTime(end.toInstant())
                .setStepViewSize(viewSize)
                .setHitButtonCount(hitButtonCount.getValue())
                .build();

        Result previousResult = this.findStepResult();
        if (previousResult instanceof CollectionResult) {
            // If the step previously had a collection result we append the tapping result to it.
            CollectionResult collectionResult = (CollectionResult) previousResult;
            collectionResult = collectionResult.appendInputResult(tappingResultBuilder.build());
            this.performTaskViewModel.addStepResult(collectionResult);
        } else {
            // Otherwise we directly add the tapping result to the step history.
            this.performTaskViewModel.addStepResult(tappingResultBuilder.build());
        }
    }

    public boolean userIsTapping() {
        return (this.expired.getValue() != null && !this.expired.getValue()) && this.tappingStartInUptimeMillis >= 0;
    }
}
