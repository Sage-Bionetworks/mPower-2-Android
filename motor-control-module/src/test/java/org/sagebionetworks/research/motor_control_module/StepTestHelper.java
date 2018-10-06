package org.sagebionetworks.research.motor_control_module;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.sagebionetworks.research.domain.result.interfaces.Result;
import org.sagebionetworks.research.domain.result.interfaces.TaskResult;
import org.sagebionetworks.research.domain.step.interfaces.Step;
import org.sagebionetworks.research.domain.task.navigation.StepNavigator;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class StepTestHelper {
    public static Step mockStepWithIdentifier(String identifier) {
        Step step = mock(Step.class);
        when(step.getIdentifier()).thenReturn(identifier);
        return step;
    }

    public static Result mockResultWithIdentifier(String identifier) {
        Result result = mock(Result.class);
        when(result.getIdentifier()).thenReturn(identifier);
        return result;
    }

    public static TaskResult simulateAdvance(@NonNull StepNavigator stepNavigator,
                                             @NonNull Step currentStep,
                                             @Nullable Step expectedNextStep,
                                             @NonNull TaskResult taskResult) {
        Step stepAfterCurrentStep = stepNavigator.getNextStep(currentStep, taskResult);
        if (expectedNextStep == null) {
            assertNull(stepAfterCurrentStep);
            return taskResult;
        } else {
            assertNotNull(stepAfterCurrentStep);
            assertEquals(expectedNextStep.getIdentifier(), stepAfterCurrentStep.getIdentifier());
            return taskResult.addStepHistory(mockResultWithIdentifier(expectedNextStep.getIdentifier()));
        }
    }

}
