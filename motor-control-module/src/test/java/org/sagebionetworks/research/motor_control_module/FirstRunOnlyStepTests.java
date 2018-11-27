package org.sagebionetworks.research.motor_control_module;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.research.domain.result.implementations.TaskResultBase;
import org.sagebionetworks.research.domain.result.interfaces.AnswerResult;
import org.sagebionetworks.research.domain.result.interfaces.TaskResult;
import org.sagebionetworks.research.domain.step.interfaces.Step;
import org.sagebionetworks.research.domain.task.Task;
import org.sagebionetworks.research.domain.task.navigation.StepNavigator;
import org.sagebionetworks.research.motor_control_module.step.InstructionStep;
import org.sagebionetworks.research.presentation.perform_task.PerformTaskViewModel;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.research.motor_control_module.StepTestHelper.mockResultWithIdentifier;
import static org.sagebionetworks.research.motor_control_module.StepTestHelper.mockStepWithIdentifier;
import static org.sagebionetworks.research.motor_control_module.StepTestHelper.simulateAdvance;

/**
 * The test task for these tests has the following structure
 *
 * introduction (step)
 * instructionFirstRunOnly (instruction step first run only)
 * instructionNonFirstRunOnly (instruction step not first run only)
 * completion (step)
 */
public class FirstRunOnlyStepTests {
    private Task task;
    private StepNavigator stepNavigator;

    @Before
    public void init() {
        this.task = mock(Task.class);
        when(this.task.getIdentifier()).thenReturn("testTask");
        when(this.task.getAsyncActions()).thenReturn(ImmutableSet.of());
        List<String> progressMarkers = Arrays.asList("instruction", "left.instruction", "completion");
        when(this.task.getProgressMarkers()).thenReturn(ImmutableList.copyOf(progressMarkers));
        List<Step> steps = new ArrayList<>();
        steps.add(mockStepWithIdentifier("introduction"));
        InstructionStep instructionFirstRunOnly = InstructionStep.builder()
                .setActions(ImmutableMap.of())
                .setIdentifier("instructionStepFirstRunOnly")
                .setIsFirstRunOnly(true)
                .build();
        steps.add(instructionFirstRunOnly);
        InstructionStep instructionNotFirstRunOnly = InstructionStep.builder()
                .setActions(ImmutableMap.of())
                .setIdentifier("instructionStepNonFirstRunOnly")
                .setIsFirstRunOnly(false)
                .build();
        steps.add(instructionNotFirstRunOnly);
        steps.add(mockStepWithIdentifier("completion"));
        when(this.task.getSteps()).thenReturn(ImmutableList.copyOf(steps));
        this.stepNavigator = DaggerNavigationTestComponent
                .builder()
                .build()
                .stepNavigatorFactory()
                .create(this.task, this.task.getProgressMarkers());
    }

    private AnswerResult<ZonedDateTime> mockFirstRunResult(boolean isFirstRun, TaskResult taskResult) {
        @SuppressWarnings("unchecked")
        AnswerResult<ZonedDateTime> result = (AnswerResult<ZonedDateTime>) mock(AnswerResult.class);
        ZonedDateTime taskResultStart = ZonedDateTime.ofInstant(taskResult.getStartTime(), ZoneId.of("Z"));
        ZonedDateTime fakeLastRun = isFirstRun ? taskResultStart.minusMonths(2) : taskResultStart.minusDays(1);
        when(result.getAnswer()).thenReturn(fakeLastRun);
        when(result.getIdentifier()).thenReturn(PerformTaskViewModel.LAST_RUN_RESULT_ID);
        return result;
    }

    @Test
    public void test_nonFirstRun() {
        List<Step> steps = this.task.getSteps();
        TaskResult taskResult = new TaskResultBase("testTask", Instant.now(), UUID.randomUUID());
        taskResult = taskResult.addAsyncResult(mockFirstRunResult(false, taskResult));
        taskResult = taskResult.addStepHistory(mockResultWithIdentifier("introduction"));
        Step currentStep = steps.get(0);
        // After the introduction step we should skip to the first run only instruction.
        Step expectedNextStep = steps.get(2);
        taskResult = simulateAdvance(this.stepNavigator, currentStep, expectedNextStep, taskResult);
        currentStep = expectedNextStep;
        // After the non first run only instruction step we should navigate to the completion step
        expectedNextStep = steps.get(3);
        simulateAdvance(this.stepNavigator, currentStep, expectedNextStep, taskResult);
    }

    @Test
    public void test_firstRun() {
        List<Step> steps = this.task.getSteps();
        TaskResult taskResult = new TaskResultBase("testTask", Instant.now(), UUID.randomUUID());
        taskResult = taskResult.addAsyncResult(mockFirstRunResult(true, taskResult));
        taskResult = taskResult.addStepHistory(mockResultWithIdentifier("introduction"));
        Step currentStep = steps.get(0);
        // After the introduction step we should navigate to the first run only instruction.
        Step expectedNextStep = steps.get(1);
        taskResult = simulateAdvance(this.stepNavigator, currentStep, expectedNextStep, taskResult);
        currentStep = expectedNextStep;
        // After the first run only step we should navigate to the non first run only instruction.
        expectedNextStep = steps.get(2);
        taskResult = simulateAdvance(this.stepNavigator, currentStep, expectedNextStep, taskResult);
        currentStep = expectedNextStep;
        // After the non first run only instruction step we should navigate to the completion step
        expectedNextStep = steps.get(3);
        simulateAdvance(this.stepNavigator, currentStep, expectedNextStep, taskResult);
    }
}
