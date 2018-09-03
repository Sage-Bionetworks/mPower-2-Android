package org.sagebionetworks.research.motor_control_module;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.research.domain.result.implementations.TaskResultBase;
import org.sagebionetworks.research.domain.result.interfaces.AnswerResult;
import org.sagebionetworks.research.domain.result.interfaces.Result;
import org.sagebionetworks.research.domain.result.interfaces.TaskResult;
import org.sagebionetworks.research.domain.step.interfaces.SectionStep;
import org.sagebionetworks.research.domain.step.interfaces.Step;
import org.sagebionetworks.research.domain.task.Task;
import org.sagebionetworks.research.domain.task.navigation.StepNavigator;
import org.sagebionetworks.research.domain.task.navigation.strategy.StrategyBasedNavigator;
import org.sagebionetworks.research.motor_control_module.step.HandStepHelper;
import org.sagebionetworks.research.motor_control_module.show_step_fragment.hand_selection.ShowHandSelectionStepFragment;
import org.sagebionetworks.research.motor_control_module.step.InstructionStep;
import org.sagebionetworks.research.motor_control_module.step.MPowerActiveUIStep;
import org.threeten.bp.Instant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.research.motor_control_module.StepTestHelper.mockResultWithIdentifier;
import static org.sagebionetworks.research.motor_control_module.StepTestHelper.simulateAdvance;

/**
 * The test task has the following structure:
 *
 * introduction (step)
 * instruction (instruction step) PROGRESS_MARKER
 * left (section step)
 *      left.instruction (instruction step) PROGRESS_MARKER
 *      left.active (mpower active step)
 * right (section step)
 *      right.instruction (instruction step)
 *      right.active (mpower active step)
 * completion (step) PROGRESS_MARKER
 */
public class HandStepTests {
    private Task task;
    private StepNavigator stepNavigator;

    @Before
    public void init() {
       this.task = mock(Task.class);
       when(this.task.getIdentifier()).thenReturn("testTask");
       when(this.task.getAsyncActions()).thenReturn(ImmutableSet.of());
       List<String> progressMarkers = Arrays.asList("instruction", "left_instruction", "completion");
       when(this.task.getProgressMarkers()).thenReturn(ImmutableList.copyOf(progressMarkers));
       List<Step> steps = new ArrayList<>();
        steps.add(StepTestHelper.mockStepWithIdentifier("introduction"));
       InstructionStep instruction = InstructionStep.builder().setActions(ImmutableMap.of())
               .setIdentifier("instruction").build();
       steps.add(instruction);
       InstructionStep leftInstruction = InstructionStep.builder().setActions(ImmutableMap.of())
               .setIdentifier("left_instruction").build();
       MPowerActiveUIStep leftActive = MPowerActiveUIStep.builder().setActions(ImmutableMap.of())
               .setIdentifier("left_active").build();
       steps.add(mockSectionStep("left", leftInstruction, leftActive));
       InstructionStep rightInstruction = InstructionStep.builder().setActions(ImmutableMap.of())
               .setIdentifier("right_instruction").build();
       MPowerActiveUIStep rightActive = MPowerActiveUIStep.builder().setActions(ImmutableMap.of())
               .setIdentifier("right_active").build();
       steps.add(mockSectionStep("right", rightInstruction, rightActive));
       steps.add(StepTestHelper.mockStepWithIdentifier("completion"));
       when(this.task.getSteps()).thenReturn(ImmutableList.copyOf(steps));
        this.stepNavigator = DaggerNavigationTestComponent
                .builder()
                .build()
                .stepNavigatorFactory()
                .create(this.task, this.task.getProgressMarkers());
    }

    private SectionStep mockSectionStep(String identifier, Step... steps) {
        SectionStep sectionStep = mock(SectionStep.class);
        when(sectionStep.getIdentifier()).thenReturn(identifier);
        when(sectionStep.getSteps()).thenReturn(ImmutableList.copyOf(steps));
        return sectionStep;
    }

    private AnswerResult<List<String>> mockHandOrderResult(HandStepHelper.Hand... handOrder) {
        @SuppressWarnings("unchecked")
        AnswerResult<List<String>> result = (AnswerResult<List<String>>) mock(AnswerResult.class);
        List<String> handOrderAnswer = new ArrayList<>();
        for (HandStepHelper.Hand hand : handOrder) {
            handOrderAnswer.add(hand.toString());
        }

        when(result.getAnswer()).thenReturn(handOrderAnswer);
        when(result.getIdentifier()).thenReturn(ShowHandSelectionStepFragment.HAND_ORDER_KEY);
        return result;
    }

    @Test
    public void test_LeftOnly() {
        List<Step> steps = this.task.getSteps();
        TaskResult taskResult = new TaskResultBase("testTask", Instant.now(), UUID.randomUUID());
        taskResult = taskResult.addStepHistory(mockHandOrderResult(HandStepHelper.Hand.LEFT));
        taskResult = taskResult.addStepHistory(mockResultWithIdentifier("introduction"));
        Step currentStep = steps.get(0);
        // After the intro we should navigate to the instruction step.
        Step expectedNextStep = steps.get(1);
        taskResult = simulateAdvance(this.stepNavigator, currentStep, expectedNextStep, taskResult);
        currentStep = expectedNextStep;
        // After the instruction step we should navigate to left.instruction
        expectedNextStep = ((SectionStep)steps.get(2)).getSteps().get(0);
        taskResult = simulateAdvance(this.stepNavigator, currentStep, expectedNextStep, taskResult);
        currentStep = expectedNextStep;
        // After the left instruction step we should navigate to left.active
        expectedNextStep = ((SectionStep)steps.get(2)).getSteps().get(1);
        taskResult = simulateAdvance(this.stepNavigator, currentStep, expectedNextStep, taskResult);
        currentStep = expectedNextStep;
        // After the left active step we should navigate to the completion
        expectedNextStep = steps.get(4);
        simulateAdvance(this.stepNavigator, currentStep, expectedNextStep, taskResult);
    }

    @Test
    public void test_RightOnly() {
        List<Step> steps = this.task.getSteps();
        TaskResult taskResult = new TaskResultBase("testTask", Instant.now(), UUID.randomUUID());
        taskResult = taskResult.addStepHistory(mockHandOrderResult(HandStepHelper.Hand.RIGHT));
        taskResult = taskResult.addStepHistory(mockResultWithIdentifier("introduction"));
        Step currentStep = steps.get(0);
        // After the intro we should navigate to the instruction step.
        Step expectedNextStep = steps.get(1);
        taskResult = simulateAdvance(this.stepNavigator, currentStep, expectedNextStep, taskResult);
        currentStep = expectedNextStep;
        // After the instruction step we should navigate to right.instruction
        expectedNextStep = ((SectionStep)steps.get(3)).getSteps().get(0);
        taskResult = simulateAdvance(this.stepNavigator, currentStep, expectedNextStep, taskResult);
        currentStep = expectedNextStep;
        // After the right instruction step we should navigate to right.active
        expectedNextStep = ((SectionStep)steps.get(3)).getSteps().get(1);
        taskResult = simulateAdvance(this.stepNavigator, currentStep, expectedNextStep, taskResult);
        currentStep = expectedNextStep;
        // After the right active step we should navigate to the completion
        expectedNextStep = steps.get(4);
        simulateAdvance(this.stepNavigator, currentStep, expectedNextStep, taskResult);
    }

    @Test
    public void test_Both_LeftThenRight() {
        List<Step> steps = this.task.getSteps();
        TaskResult taskResult = new TaskResultBase("testTask", Instant.now(), UUID.randomUUID());
        taskResult = taskResult.addStepHistory(mockHandOrderResult(HandStepHelper.Hand.LEFT, HandStepHelper.Hand.RIGHT));
        taskResult = taskResult.addStepHistory(mockResultWithIdentifier("introduction"));
        Step currentStep = steps.get(0);
        // After the intro we should navigate to the instruction step.
        Step expectedNextStep = steps.get(1);
        taskResult = simulateAdvance(this.stepNavigator, currentStep, expectedNextStep, taskResult);
        currentStep = expectedNextStep;
        // After the instruction step we should navigate to left.instruction
        expectedNextStep = ((SectionStep)steps.get(2)).getSteps().get(0);
        taskResult = simulateAdvance(this.stepNavigator, currentStep, expectedNextStep, taskResult);
        currentStep = expectedNextStep;
        // After the left instruction step we should navigate to left.active
        expectedNextStep = ((SectionStep)steps.get(2)).getSteps().get(1);
        taskResult = simulateAdvance(this.stepNavigator, currentStep, expectedNextStep, taskResult);
        currentStep = expectedNextStep;
        // After the left active step we should navigate to right.instruction
        expectedNextStep = ((SectionStep)steps.get(3)).getSteps().get(0);
        taskResult = simulateAdvance(this.stepNavigator, currentStep, expectedNextStep, taskResult);
        currentStep = expectedNextStep;
        // After the right instruction step we should navigate to right.active
        expectedNextStep = ((SectionStep)steps.get(3)).getSteps().get(1);
        taskResult = simulateAdvance(this.stepNavigator, currentStep, expectedNextStep, taskResult);
        currentStep = expectedNextStep;
        // After the right active step we should navigate to the completion
        expectedNextStep = steps.get(4);
        simulateAdvance(this.stepNavigator, currentStep, expectedNextStep, taskResult);
    }

    @Test
    public void test_Both_RightThenLeft() {
        List<Step> steps = this.task.getSteps();
        TaskResult taskResult = new TaskResultBase("testTask", Instant.now(), UUID.randomUUID());
        taskResult = taskResult.addStepHistory(mockHandOrderResult(HandStepHelper.Hand.RIGHT, HandStepHelper.Hand.LEFT));
        taskResult = taskResult.addStepHistory(mockResultWithIdentifier("introduction"));
        Step currentStep = steps.get(0);
        // After the intro we should navigate to the instruction step.
        Step expectedNextStep = steps.get(1);
        taskResult = simulateAdvance(this.stepNavigator, currentStep, expectedNextStep, taskResult);
        currentStep = expectedNextStep;
        // After the instruction step we should navigate to right.instruction
        expectedNextStep = ((SectionStep)steps.get(3)).getSteps().get(0);
        taskResult = simulateAdvance(this.stepNavigator, currentStep, expectedNextStep, taskResult);
        currentStep = expectedNextStep;
        // After the right instruction step we should navigate to right.active
        expectedNextStep = ((SectionStep)steps.get(3)).getSteps().get(1);
        taskResult = simulateAdvance(this.stepNavigator, currentStep, expectedNextStep, taskResult);
        currentStep = expectedNextStep;
        // After the right active step we should navigate to left.instruction
        expectedNextStep = ((SectionStep)steps.get(2)).getSteps().get(0);
        taskResult = simulateAdvance(this.stepNavigator, currentStep, expectedNextStep, taskResult);
        currentStep = expectedNextStep;
        // After the left instruction step we should navigate to left.active
        expectedNextStep = ((SectionStep)steps.get(2)).getSteps().get(1);
        taskResult = simulateAdvance(this.stepNavigator, currentStep, expectedNextStep, taskResult);
        currentStep = expectedNextStep;
        // After the left active step we should navigate to the completion
        expectedNextStep = steps.get(4);
        simulateAdvance(this.stepNavigator, currentStep, expectedNextStep, taskResult);
    }
}
