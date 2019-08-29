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

package org.sagebionetworks.research.mpower.researchstack.researchstack_mpower;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.sagebionetworks.research.mpower.researchstack.framework.step.MpSmartSurveyTask.FORM_STEP_SUFFIX;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;
import android.widget.ImageView.ScaleType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sagebionetworks.researchstack.backbone.answerformat.AnswerFormat;
import org.sagebionetworks.researchstack.backbone.model.Choice;
import org.sagebionetworks.researchstack.backbone.model.TaskModel;
import org.sagebionetworks.researchstack.backbone.model.TaskModel.RuleModel;
import org.sagebionetworks.researchstack.backbone.result.StepResult;
import org.sagebionetworks.researchstack.backbone.result.TaskResult;
import org.sagebionetworks.researchstack.backbone.step.QuestionStep;
import org.sagebionetworks.researchstack.backbone.step.Step;
import org.sagebionetworks.researchstack.backbone.task.SmartSurveyTask;
import org.sagebionetworks.bridge.rest.RestUtils;
import org.sagebionetworks.research.mpower.researchstack.framework.MpTaskFactory;
import org.sagebionetworks.research.mpower.researchstack.framework.step.MpFormStep;
import org.sagebionetworks.research.mpower.researchstack.framework.step.MpInstructionStep;
import org.sagebionetworks.research.mpower.researchstack.framework.step.MpSmartSurveyTask;
import org.sagebionetworks.research.mpower.researchstack.framework.step.body.MpCheckboxAnswerFormat;
import org.sagebionetworks.research.mpower.researchstack.framework.step.body.MpChoiceAnswerFormat;
import org.sagebionetworks.research.mpower.researchstack.framework.step.body.MpIntegerAnswerFormat;
import org.sagebionetworks.research.mpower.researchstack.framework.step.body.MpMultiCheckboxAnswerFormat;
import org.sagebionetworks.research.mpower.researchstack.framework.step.body.MpRadioButtonAnswerFormat;
import org.sagebionetworks.research.mpower.researchstack.framework.step.body.MpTextQuestionBody;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class MpSmartSurveyTaskTests {

    private Context context;
    private TaskModel motivationTaskModel;
    private TaskModel backgroundTaskModel;
    private TaskModel demographicsTaskModel;

    @Before
    public void setupForTests() {
        if (context == null) {
            // Needs delayed init so context is ready to open json assets
            context = InstrumentationRegistry.getTargetContext();
        }
        if (motivationTaskModel == null) {
            motivationTaskModel = RestUtils.GSON.fromJson(
                    TestResourceHelper.jsonStrFromFile("Motivation.json"), TaskModel.class);
        }
        if (backgroundTaskModel == null) {
            backgroundTaskModel = RestUtils.GSON.fromJson(
                    TestResourceHelper.jsonStrFromFile("Background.json"), TaskModel.class);
        }
        if (demographicsTaskModel == null) {
            demographicsTaskModel = RestUtils.GSON.fromJson(
                    TestResourceHelper.jsonStrFromFile("Demographics.json"), TaskModel.class);
        }
    }

    @Test
    public void test_taskModelSetup() {
        assertNotNull(motivationTaskModel);
        assertNotNull(backgroundTaskModel);
        assertNotNull(demographicsTaskModel);
    }

    @Test
    public void test_motivationStepsAndRules() {
        assertNotNull(motivationTaskModel);
        MpSmartSurveyTask task = new MockMpTaskFactory().createMpSmartSurveyTask(context, motivationTaskModel);
        assertNotNull(task);
        assertTrue(task instanceof MpSmartSurveyTask);
        MockMpSmartSurveyTask mpTask = (MockMpSmartSurveyTask)task;
        assertNotNull(mpTask);
        {
            Step step = mpTask.getSteps().get(0);
            assertNotNull(step);
            assertTrue(step instanceof MpInstructionStep);
            {
                MpInstructionStep mpStep = (MpInstructionStep) step;
                assertEquals("motivation_II_T_Intro", mpStep.getIdentifier());
                assertEquals("Hello,", mpStep.getTitle());
                assertEquals(
                        "Research has found that people are much more likely to do something when they have created a plan for exactly when, where, and how they will do it.\n",
                        mpStep.getText());
                assertEquals(
                        "You might also want to use one of the mPower tasks, such as tapping, every day to make it a habit and, also, to give you even greater understanding of your Parkinson’s symptoms.\n",
                        mpStep.getMoreDetailText());

                List<RuleModel> beforeRules = mpTask.getBeforeRules().get(mpStep.getIdentifier());
                assertNotNull(beforeRules);
                assertEquals(3, beforeRules.size());
                {
                    RuleModel rule = beforeRules.get(0);
                    assertEquals("all", rule.operator);
                    assertNotNull(rule.dataGroups);
                    assertEquals(2, rule.dataGroups.size());
                    assertEquals("gr_BR_AD", rule.dataGroups.get(0));
                    assertEquals("gr_DT_T", rule.dataGroups.get(1));
                    assertEquals("motivation_AD_T", rule.skipTo);
                    assertEquals("SurveyRule", rule.type);
                }
                {
                    RuleModel rule = beforeRules.get(1);
                    assertEquals("all", rule.operator);
                    assertNotNull(rule.dataGroups);
                    assertEquals(2, rule.dataGroups.size());
                    assertEquals("gr_BR_AD", rule.dataGroups.get(0));
                    assertEquals("gr_DT_F", rule.dataGroups.get(1));
                    assertEquals("motivation_AD_F", rule.skipTo);
                    assertEquals("SurveyRule", rule.type);
                }
                {
                    RuleModel rule = beforeRules.get(2);
                    assertEquals("all", rule.operator);
                    assertNotNull(rule.dataGroups);
                    assertEquals(2, rule.dataGroups.size());
                    assertEquals("gr_DT_F", rule.dataGroups.get(0));
                    assertEquals("gr_BR_II", rule.dataGroups.get(1));
                    assertEquals("motivation_II_F_Intro", rule.skipTo);
                    assertEquals("SurveyRule", rule.type);
                }

                List<RuleModel> afterRules = mpTask.getAfterRules().get(mpStep.getIdentifier());
                assertTrue(afterRules == null || afterRules.isEmpty());
            }
        }
        {
            Step step = mpTask.getSteps().get(1);
            assertNotNull(step);
            assertTrue(step instanceof MpFormStep);
            {
                MpFormStep mpStep = (MpFormStep) step;
                assertEquals("motivation_II_T" + FORM_STEP_SUFFIX, mpStep.getIdentifier());
                assertEquals("To create a habit of using mPower, please complete this statement:", mpStep.getTitle());
                assertEquals("\"Each day, whenever I ______, I will complete the mPower battery.\"\n", mpStep.getText());
                assertNotNull(mpStep.getFormSteps());
                assertEquals(1, mpStep.getFormSteps().size());

                QuestionStep questionStep = mpStep.getFormSteps().get(0);
                assertEquals("motivation_II_T", questionStep.getIdentifier());

                AnswerFormat answerFormat = questionStep.getAnswerFormat();
                assertNotNull(answerFormat);
                assertTrue(answerFormat instanceof MpTextQuestionBody.AnswerFormat);
                MpTextQuestionBody.AnswerFormat mpAnswerFormat = (MpTextQuestionBody.AnswerFormat)answerFormat;
                assertEquals(255, mpAnswerFormat.getMaximumLength());
                assertEquals("Fill in the blank", mpAnswerFormat.getHintText());

                List<RuleModel> beforeRules = mpTask.getBeforeRules().get(mpStep.getIdentifier());
                assertTrue(beforeRules == null || beforeRules.isEmpty());

                List<RuleModel> afterRules = mpTask.getAfterRules().get(mpStep.getIdentifier());
                assertNotNull(afterRules);
                assertEquals(1, afterRules.size());
                {
                    RuleModel rule = afterRules.get(0);
                    assertEquals("always", rule.operator);
                    assertEquals(true, rule.endSurvey);
                    assertEquals("SurveyRule", rule.type);
                }
            }
        }
        {
            Step step = mpTask.getSteps().get(2);
            assertNotNull(step);
            assertTrue(step instanceof MpInstructionStep);
            {
                MpInstructionStep mpStep = (MpInstructionStep) step;
                assertEquals("motivation_II_F_Intro", mpStep.getIdentifier());
                assertEquals("Hello", mpStep.getTitle());
                assertEquals("Research has found that people are much more likely to do something when they have created a plan for exactly when, where, and how they will do it.\n", mpStep.getText());

                List<RuleModel> beforeRules = mpTask.getBeforeRules().get(mpStep.getIdentifier());
                assertTrue(beforeRules == null || beforeRules.isEmpty());

                List<RuleModel> afterRules = mpTask.getAfterRules().get(mpStep.getIdentifier());
                assertTrue(afterRules == null || afterRules.isEmpty());
            }
        }
        {
            Step step = mpTask.getSteps().get(3);
            assertNotNull(step);
            assertTrue(step instanceof MpFormStep);
            {
                MpFormStep mpStep = (MpFormStep) step;
                assertEquals("motivation_II_F" + FORM_STEP_SUFFIX, mpStep.getIdentifier());
                assertEquals("To create a habit of using mPower, please complete this statement:\n", mpStep.getTitle());
                assertEquals("\"Each day, whenever I ______, I will complete the mPower battery during the 2-week burst week.\" \n", mpStep.getText());
                assertNotNull(mpStep.getFormSteps());
                assertEquals(1, mpStep.getFormSteps().size());

                QuestionStep questionStep = mpStep.getFormSteps().get(0);
                assertEquals("motivation_II_F", questionStep.getIdentifier());

                AnswerFormat answerFormat = questionStep.getAnswerFormat();
                assertNotNull(answerFormat);
                assertTrue(answerFormat instanceof MpTextQuestionBody.AnswerFormat);
                MpTextQuestionBody.AnswerFormat mpAnswerFormat = (MpTextQuestionBody.AnswerFormat)answerFormat;
                assertEquals(255, mpAnswerFormat.getMaximumLength());
                assertEquals("Fill in the blank", mpAnswerFormat.getHintText());

                List<RuleModel> beforeRules = mpTask.getBeforeRules().get(mpStep.getIdentifier());
                assertTrue(beforeRules == null || beforeRules.isEmpty());

                List<RuleModel> afterRules = mpTask.getAfterRules().get(mpStep.getIdentifier());
                assertNotNull(afterRules);
                assertEquals(1, afterRules.size());
                {
                    RuleModel rule = afterRules.get(0);
                    assertEquals("always", rule.operator);
                    assertEquals(true, rule.endSurvey);
                    assertEquals("SurveyRule", rule.type);
                }
            }
        }
        {
            Step step = mpTask.getSteps().get(4);
            assertNotNull(step);
            assertTrue(step instanceof MpInstructionStep);
            {
                MpInstructionStep mpStep = (MpInstructionStep) step;
                assertEquals("motivation_AD_T", mpStep.getIdentifier());
                assertEquals("Hello,", mpStep.getTitle());
                assertEquals("The first assessment for the mPower study is coming up! To make it easier for you to remember to do these assessments, you can do the assessments during something you normally do every day, such as taking your meds. You might also want to use one of the mPower tasks, such as tapping, every day to make it a habit and, also, to give you even greater understanding of your Parkinson’s symptoms.", mpStep.getText());

                List<RuleModel> beforeRules = mpTask.getBeforeRules().get(mpStep.getIdentifier());
                assertTrue(beforeRules == null || beforeRules.isEmpty());

                List<RuleModel> afterRules = mpTask.getAfterRules().get(mpStep.getIdentifier());
                assertNotNull(afterRules);
                assertEquals(1, afterRules.size());
                {
                    RuleModel rule = afterRules.get(0);
                    assertEquals("always", rule.operator);
                    assertEquals(true, rule.endSurvey);
                    assertEquals("SurveyRule", rule.type);
                }
            }
        }
        {
            Step step = mpTask.getSteps().get(5);
            assertNotNull(step);
            assertTrue(step instanceof MpInstructionStep);
            {
                MpInstructionStep mpStep = (MpInstructionStep) step;
                assertEquals("motivation_AD_F", mpStep.getIdentifier());
                assertEquals("Hello,", mpStep.getTitle());
                assertEquals("The next burst of the mPower study is coming up! To ensure you successfully complete your mPower tasks each day during this burst, why don't you try doing the mPower tasks when you take your meds.", mpStep.getText());

                List<RuleModel> beforeRules = mpTask.getBeforeRules().get(mpStep.getIdentifier());
                assertTrue(beforeRules == null || beforeRules.isEmpty());

                List<RuleModel> afterRules = mpTask.getAfterRules().get(mpStep.getIdentifier());
                assertNotNull(afterRules);
                assertEquals(1, afterRules.size());
                {
                    RuleModel rule = afterRules.get(0);
                    assertEquals("always", rule.operator);
                    assertEquals(true, rule.endSurvey);
                    assertEquals("SurveyRule", rule.type);
                }
            }
        }
    }

    @Test
    public void test_demographicsStepsAndRules() {
        assertNotNull(demographicsTaskModel);
        MpSmartSurveyTask task = new MockMpTaskFactory().createMpSmartSurveyTask(context, demographicsTaskModel);
        assertNotNull(task);
        assertTrue(task instanceof MpSmartSurveyTask);
        MockMpSmartSurveyTask mpTask = (MockMpSmartSurveyTask) task;
        assertNotNull(mpTask);
        {
            Step step = mpTask.getSteps().get(0);
            assertNotNull(step);
            assertTrue(step instanceof MpInstructionStep);
            {
                MpInstructionStep mpStep = (MpInstructionStep) step;
                assertEquals("studyBurstCompletion", mpStep.getIdentifier());
                assertEquals("Congratulations!\n", mpStep.getTitle());
                assertEquals("You just completed the first day of your Study Burst.\n", mpStep.getText());
                assertEquals(
                        "The scientists are now starting to analyze your data. In order to complete their analysis, they have a short health survey for you to complete. Don’t worry, this shouldn’t take any more than  4 minutes.",
                        mpStep.getMoreDetailText());

                // The image for the step is set by the MpSmartTask specifically
                assertEquals("mp_study_burst_complete", mpStep.getImage());
                assertEquals("rsdCompletionGradientRight", mpStep.statusBarColorRes);
                assertEquals(ScaleType.FIT_START, mpStep.scaleType);
                assertTrue(mpStep.centerText);
                assertTrue(mpStep.hideProgress);
                assertTrue(mpStep.behindToolbar);

                List<RuleModel> beforeRules = mpTask.getBeforeRules().get(mpStep.getIdentifier());
                assertTrue(beforeRules == null || beforeRules.isEmpty());

                List<RuleModel> afterRules = mpTask.getAfterRules().get(mpStep.getIdentifier());
                assertTrue(afterRules == null || afterRules.isEmpty());
            }
        }
        {
            Step step = mpTask.getSteps().get(1);
            assertNotNull(step);
            assertTrue(step instanceof MpFormStep);
            {
                MpFormStep mpStep = (MpFormStep) step;
                assertEquals("birthYear" + FORM_STEP_SUFFIX, mpStep.getIdentifier());
                assertEquals("What is your year of birth?", mpStep.getTitle());
                assertNotNull(mpStep.getFormSteps());
                assertEquals(1, mpStep.getFormSteps().size());

                QuestionStep questionStep = mpStep.getFormSteps().get(0);
                assertEquals("birthYear", questionStep.getIdentifier());

                AnswerFormat answerFormat = questionStep.getAnswerFormat();
                assertNotNull(answerFormat);
                assertTrue(answerFormat instanceof MpIntegerAnswerFormat);
                MpIntegerAnswerFormat mpAnswerFormat = (MpIntegerAnswerFormat)answerFormat;
                assertEquals(1900, mpAnswerFormat.getMinValue());
                assertEquals(2018, mpAnswerFormat.getMaxValue());

                List<RuleModel> beforeRules = mpTask.getBeforeRules().get(mpStep.getIdentifier());
                assertTrue(beforeRules == null || beforeRules.isEmpty());

                List<RuleModel> afterRules = mpTask.getAfterRules().get(mpStep.getIdentifier());
                assertTrue(afterRules == null || afterRules.isEmpty());
            }
        }
        {
            Step step = mpTask.getSteps().get(2);
            assertNotNull(step);
            assertTrue(step instanceof MpFormStep);
            {
                MpFormStep mpStep = (MpFormStep) step;
                assertEquals("sex" + FORM_STEP_SUFFIX, mpStep.getIdentifier());
                assertEquals("What is your sex?", mpStep.getTitle());
                assertNotNull(mpStep.getFormSteps());
                assertEquals(1, mpStep.getFormSteps().size());

                QuestionStep questionStep = mpStep.getFormSteps().get(0);
                assertEquals("sex", questionStep.getIdentifier());

                AnswerFormat answerFormat = questionStep.getAnswerFormat();
                assertNotNull(answerFormat);
                assertTrue(answerFormat instanceof MpChoiceAnswerFormat);
                MpChoiceAnswerFormat mpAnswerFormat = (MpChoiceAnswerFormat)answerFormat;
                assertNotNull(mpAnswerFormat.getChoices());
                assertEquals(3, mpAnswerFormat.getChoices().length);
                {
                    Choice choice = mpAnswerFormat.getChoices()[0];
                    assertEquals("Female", choice.getText());
                    assertEquals("Female", choice.getValue());
                }
                {
                    Choice choice = mpAnswerFormat.getChoices()[1];
                    assertEquals("Male", choice.getText());
                    assertEquals("Male", choice.getValue());
                }
                {
                    Choice choice = mpAnswerFormat.getChoices()[2];
                    assertEquals("Other", choice.getText());
                    assertEquals("Other", choice.getValue());
                }

                List<RuleModel> beforeRules = mpTask.getBeforeRules().get(mpStep.getIdentifier());
                assertTrue(beforeRules == null || beforeRules.isEmpty());

                List<RuleModel> afterRules = mpTask.getAfterRules().get(mpStep.getIdentifier());
                assertTrue(afterRules == null || afterRules.isEmpty());
            }
        }
        {
            Step step = mpTask.getSteps().get(3);
            assertNotNull(step);
            assertTrue(step instanceof MpFormStep);
            {
                MpFormStep mpStep = (MpFormStep) step;
                assertEquals("diagnosis" + FORM_STEP_SUFFIX, mpStep.getIdentifier());
                assertEquals("Have you been diagnosed with Parkinson's Disease by a medical professional?", mpStep.getTitle());
                assertNotNull(mpStep.getFormSteps());
                assertEquals(1, mpStep.getFormSteps().size());

                QuestionStep questionStep = mpStep.getFormSteps().get(0);
                assertEquals("diagnosis", questionStep.getIdentifier());

                AnswerFormat answerFormat = questionStep.getAnswerFormat();
                assertNotNull(answerFormat);
                assertTrue(answerFormat instanceof MpChoiceAnswerFormat);
                MpChoiceAnswerFormat mpAnswerFormat = (MpChoiceAnswerFormat)answerFormat;
                assertNotNull(mpAnswerFormat.getChoices());
                assertEquals(3, mpAnswerFormat.getChoices().length);
                {
                    Choice choice = mpAnswerFormat.getChoices()[0];
                    assertEquals("Yes, I have PD", choice.getText());
                    assertEquals("parkinsons", choice.getValue());
                }
                {
                    Choice choice = mpAnswerFormat.getChoices()[1];
                    assertEquals("No, I do not have PD", choice.getText());
                    assertEquals("control", choice.getValue());
                }
                {
                    Choice choice = mpAnswerFormat.getChoices()[2];
                    assertEquals("Prefer not to answer", choice.getText());
                    assertEquals("no_answer", choice.getValue());
                }

                List<RuleModel> beforeRules = mpTask.getBeforeRules().get(mpStep.getIdentifier());
                assertTrue(beforeRules == null || beforeRules.isEmpty());

                List<RuleModel> afterRules = mpTask.getAfterRules().get(mpStep.getIdentifier());
                assertNotNull(afterRules);
                assertEquals(2, afterRules.size());
                {
                    RuleModel rule = afterRules.get(0);
                    assertEquals("eq", rule.operator);
                    assertEquals("parkinsons", rule.value);
                    assertEquals("parkinsons", rule.assignDataGroup);
                    assertEquals("SurveyRule", rule.type);
                }
                {
                    RuleModel rule = afterRules.get(1);
                    assertEquals("eq", rule.operator);
                    assertEquals("control", rule.value);
                    assertEquals("control", rule.assignDataGroup);
                    assertEquals("SurveyRule", rule.type);
                }
            }
        }
    }

    /**
     * No need to test all the survey questions in Background survey
     * There is a lot, so just test the types we haven't tested in the previous survey tests like checkbox and radio
     */
    @Test
    public void test_backgroundStepsAndRules() {
        assertNotNull(backgroundTaskModel);
        MpSmartSurveyTask task = new MockMpTaskFactory().createMpSmartSurveyTask(context, backgroundTaskModel);
        assertNotNull(task);
        assertTrue(task instanceof MpSmartSurveyTask);
        MockMpSmartSurveyTask mpTask = (MockMpSmartSurveyTask) task;
        assertNotNull(mpTask);
        {
            Step step = mpTask.getSteps().get(0);
            assertNotNull(step);
            assertTrue(step instanceof MpFormStep);
            {
                MpFormStep mpStep = (MpFormStep) step;
                assertEquals("ethnicity" + FORM_STEP_SUFFIX, mpStep.getIdentifier());
                assertEquals("What ethnicity do you identify with?", mpStep.getTitle());
                assertNotNull(mpStep.getFormSteps());
                assertEquals(1, mpStep.getFormSteps().size());

                QuestionStep questionStep = mpStep.getFormSteps().get(0);
                assertEquals("ethnicity", questionStep.getIdentifier());

                AnswerFormat answerFormat = questionStep.getAnswerFormat();
                assertNotNull(answerFormat);
                assertTrue(answerFormat instanceof MpRadioButtonAnswerFormat);
                MpRadioButtonAnswerFormat mpAnswerFormat = (MpRadioButtonAnswerFormat)answerFormat;
                assertNotNull(mpAnswerFormat.getChoices());
                assertEquals(3, mpAnswerFormat.getChoices().length);
                {
                    Choice choice = mpAnswerFormat.getChoices()[0];
                    assertEquals("Hispanic or Latino", choice.getText());
                    assertEquals("Hispanic or Latino", choice.getValue());
                }
                {
                    Choice choice = mpAnswerFormat.getChoices()[1];
                    assertEquals("Not Hispanic or Latino", choice.getText());
                    assertEquals("Not Hispanic or Latino", choice.getValue());
                }
                {
                    Choice choice = mpAnswerFormat.getChoices()[2];
                    assertEquals("Prefer not to answer", choice.getText());
                    assertEquals("Prefer not to answer", choice.getValue());
                }

                List<RuleModel> beforeRules = mpTask.getBeforeRules().get(mpStep.getIdentifier());
                assertTrue(beforeRules == null || beforeRules.isEmpty());

                List<RuleModel> afterRules = mpTask.getAfterRules().get(mpStep.getIdentifier());
                assertTrue(afterRules == null || afterRules.isEmpty());
            }
        }
        {
            Step step = mpTask.getSteps().get(1);
            assertNotNull(step);
            assertTrue(step instanceof MpFormStep);
            {
                MpFormStep mpStep = (MpFormStep) step;
                assertEquals("race" + FORM_STEP_SUFFIX, mpStep.getIdentifier());
                assertEquals("What race do you identify as?", mpStep.getTitle());
                assertEquals("(Select all that apply)", mpStep.getText());
                assertNotNull(mpStep.getFormSteps());
                assertEquals(1, mpStep.getFormSteps().size());

                QuestionStep questionStep = mpStep.getFormSteps().get(0);
                assertEquals("race", questionStep.getIdentifier());

                AnswerFormat answerFormat = questionStep.getAnswerFormat();
                assertNotNull(answerFormat);
                assertTrue(answerFormat instanceof MpMultiCheckboxAnswerFormat);
                MpMultiCheckboxAnswerFormat mpAnswerFormat = (MpMultiCheckboxAnswerFormat)answerFormat;
                assertNotNull(mpAnswerFormat.getChoices());
                assertEquals(6, mpAnswerFormat.getChoices().length);
                {
                    Choice choice = mpAnswerFormat.getChoices()[0];
                    assertEquals("American Indian or Alaska Native", choice.getText());
                    assertEquals("American Indian or Alaska Native", choice.getValue());
                }
                {
                    Choice choice = mpAnswerFormat.getChoices()[1];
                    assertEquals("Asian", choice.getText());
                    assertEquals("Asian", choice.getValue());
                }
                {
                    Choice choice = mpAnswerFormat.getChoices()[2];
                    assertEquals("Black or African American", choice.getText());
                    assertEquals("Black or African American", choice.getValue());
                }
                {
                    Choice choice = mpAnswerFormat.getChoices()[3];
                    assertEquals("Native Hawaiian or Other Pacific Islander", choice.getText());
                    assertEquals("Native Hawaiian or Other Pacific Islander", choice.getValue());
                }
                {
                    Choice choice = mpAnswerFormat.getChoices()[4];
                    assertEquals("White", choice.getText());
                    assertEquals("White", choice.getValue());
                }
                {
                    Choice choice = mpAnswerFormat.getChoices()[5];
                    assertEquals("Prefer not to answer", choice.getText());
                    assertEquals("Prefer not to answer", choice.getValue());
                }

                List<RuleModel> beforeRules = mpTask.getBeforeRules().get(mpStep.getIdentifier());
                assertTrue(beforeRules == null || beforeRules.isEmpty());

                List<RuleModel> afterRules = mpTask.getAfterRules().get(mpStep.getIdentifier());
                assertTrue(afterRules == null || afterRules.isEmpty());
            }
        }
    }

    @Test
    public void test_demographicsNavigationParkinsonsDataGroup() {
        assertNotNull(demographicsTaskModel);
        MpSmartSurveyTask task = new MockMpTaskFactory().createMpSmartSurveyTask(context, demographicsTaskModel);
        assertNotNull(task);
        assertTrue(task instanceof MpSmartSurveyTask);
        MockMpSmartSurveyTask mpTask = (MockMpSmartSurveyTask) task;
        assertNotNull(mpTask);

        TaskResult taskResult = new TaskResult(task.getIdentifier());
        putStringFormResult(taskResult, "parkinsons", "diagnosis" + FORM_STEP_SUFFIX, "diagnosis");

        Step step = null;
        // The only rule in the motivationTaskModel is for adding data models
        for (int i = 0; i <= mpTask.getSteps().size(); i++) {
            step = mpTask.getStepAfterStep(step, taskResult);
        }
        assertNull(step);
        mpTask.processTaskResult(taskResult);
        assertEquals(1, mpTask.dataGroupsAdded.size());
        assertEquals("parkinsons", mpTask.dataGroupsAdded.get(0));
    }

    @Test
    public void test_demographicsNavigationControlDataGroup() {
        assertNotNull(demographicsTaskModel);
        MpSmartSurveyTask task = new MockMpTaskFactory().createMpSmartSurveyTask(context, demographicsTaskModel);
        assertNotNull(task);
        assertTrue(task instanceof MpSmartSurveyTask);
        MockMpSmartSurveyTask mpTask = (MockMpSmartSurveyTask) task;
        assertNotNull(mpTask);

        TaskResult taskResult = new TaskResult(task.getIdentifier());
        StepResult<StepResult<String>> stepResult = new StepResult<>(new Step("diagnosis" + FORM_STEP_SUFFIX));
        StepResult<String> answerResult = new StepResult<>(new Step("diagnosis"));
        answerResult.setResult("control");
        stepResult.setResult(answerResult);
        taskResult.getResults().put("diagnosis" + FORM_STEP_SUFFIX, stepResult);

        Step step = null;
        // The only rule in the motivationTaskModel is for adding data models
        for (int i = 0; i <= mpTask.getSteps().size(); i++) {
            step = mpTask.getStepAfterStep(step, taskResult);
        }
        assertNull(step);
        mpTask.processTaskResult(taskResult);
        assertEquals(1, mpTask.dataGroupsAdded.size());
        assertEquals("control", mpTask.dataGroupsAdded.get(0));
    }

    @Test
    public void test_demographicsNavigationNoDataGroup() {
        assertNotNull(demographicsTaskModel);
        MpSmartSurveyTask task = new MockMpTaskFactory().createMpSmartSurveyTask(context, demographicsTaskModel);
        assertNotNull(task);
        assertTrue(task instanceof MpSmartSurveyTask);
        MockMpSmartSurveyTask mpTask = (MockMpSmartSurveyTask) task;
        assertNotNull(mpTask);

        TaskResult taskResult = new TaskResult(task.getIdentifier());
        StepResult<StepResult<String>> stepResult = new StepResult<>(new Step("diagnosis" + FORM_STEP_SUFFIX));
        StepResult<String> answerResult = new StepResult<>(new Step("diagnosis"));
        answerResult.setResult("no_answer");
        stepResult.setResult(answerResult);
        taskResult.getResults().put("diagnosis" + FORM_STEP_SUFFIX, stepResult);

        Step step = null;
        // The only rule in the motivationTaskModel is for adding data models
        for (int i = 0; i <= mpTask.getSteps().size(); i++) {
            step = mpTask.getStepAfterStep(step, taskResult);
        }
        assertNull(step);
        mpTask.processTaskResult(taskResult);
        assertEquals(0, mpTask.dataGroupsAdded.size());
    }

    @Test
    public void test_motivationNavigationAD_T() {
        assertNotNull(motivationTaskModel);
        List<String> dataGroups = Arrays.asList("gr_BR_AD", "gr_DT_T");
        MpSmartSurveyTask task = new MockMpTaskFactory(dataGroups).createMpSmartSurveyTask(context, motivationTaskModel);
        assertNotNull(task);
        assertTrue(task instanceof MpSmartSurveyTask);
        MockMpSmartSurveyTask mpTask = (MockMpSmartSurveyTask) task;
        assertNotNull(mpTask);

        TaskResult taskResult = new TaskResult(task.getIdentifier());
        Step step0 = mpTask.getStepAfterStep(null, taskResult);
        assertNotNull(step0);
        assertEquals("motivation_AD_T", step0.getIdentifier());

        Step step1 = mpTask.getStepAfterStep(step0, taskResult);
        assertNull(step1);
    }

    @Test
    public void test_motivationNavigationAD_F() {
        assertNotNull(motivationTaskModel);
        List<String> dataGroups = Arrays.asList("gr_BR_AD", "gr_DT_F");
        MpSmartSurveyTask task = new MockMpTaskFactory(dataGroups).createMpSmartSurveyTask(context, motivationTaskModel);
        assertNotNull(task);
        assertTrue(task instanceof MpSmartSurveyTask);
        MockMpSmartSurveyTask mpTask = (MockMpSmartSurveyTask) task;
        assertNotNull(mpTask);

        TaskResult taskResult = new TaskResult(task.getIdentifier());
        Step step0 = mpTask.getStepAfterStep(null, taskResult);
        assertNotNull(step0);
        assertEquals("motivation_AD_F", step0.getIdentifier());

        Step step1 = mpTask.getStepAfterStep(step0, taskResult);
        assertNull(step1);
    }

    @Test
    public void test_motivationNavigationII_F_Intro() {
        assertNotNull(motivationTaskModel);
        List<String> dataGroups = Arrays.asList("gr_DT_F", "gr_BR_II");
        MpSmartSurveyTask task = new MockMpTaskFactory(dataGroups).createMpSmartSurveyTask(context, motivationTaskModel);
        assertNotNull(task);
        assertTrue(task instanceof MpSmartSurveyTask);
        MockMpSmartSurveyTask mpTask = (MockMpSmartSurveyTask) task;
        assertNotNull(mpTask);

        TaskResult taskResult = new TaskResult(task.getIdentifier());
        Step step0 = mpTask.getStepAfterStep(null, taskResult);
        assertNotNull(step0);
        assertEquals("motivation_II_F_Intro", step0.getIdentifier());

        Step step1 = mpTask.getStepAfterStep(step0, taskResult);
        assertNotNull(step1);
        assertEquals("motivation_II_F" + FORM_STEP_SUFFIX, step1.getIdentifier());

        Step step2 = mpTask.getStepAfterStep(step1, taskResult);
        assertNull(step2);
    }

    /**
     * There are many possible navigation paths for the background survey.
     * To avoid 50 unit tests for each path, do all the paths in on test by going backwards to test all the scenarios.
     */
    @Test
    public void test_backgroundNavigationTests() {
        assertNotNull(backgroundTaskModel);

        { // Test exiting when you do not have an old mPower account
            TaskStepHolder holder = assertBackgroundTaskUpUntilExitJunction();
            MockMpSmartSurveyTask mpTask = holder.task;
            Step step = holder.step;
            TaskResult taskResult = holder.taskResult;

            putStringFormResult(taskResult, "No",
                    "participatedPrevmPower" + FORM_STEP_SUFFIX, "participatedPrevmPower");

            // No sends the user to the end of the survey
            step = mpTask.getStepAfterStep(step, taskResult);
            assertNull(step); // end of survey
        }

        { // Test exiting when you do not have an old mPower account
            TaskStepHolder holder = assertBackgroundTaskUpUntilExitJunction();
            MockMpSmartSurveyTask mpTask = holder.task;
            Step step = holder.step;
            TaskResult taskResult = holder.taskResult;

            putStringFormResult(taskResult, "Yes",
                    "participatedPrevmPower" + FORM_STEP_SUFFIX, "participatedPrevmPower");

            step = mpTask.getStepAfterStep(step, taskResult);
            assertNotNull(step);
            assertEquals("linkDataPrevmPower" + FORM_STEP_SUFFIX, step.getIdentifier()); // no rules, just keep going

            putStringFormResult(taskResult, "No",
                    "linkDataPrevmPower" + FORM_STEP_SUFFIX, "linkDataPrevmPower");

            // No sends the user to the info screen about changing answers in settings about linking the account
            step = mpTask.getStepAfterStep(step, taskResult);
            assertNotNull(step);
            assertEquals("noLinkData", step.getIdentifier()); // no rules, just keep going

            step = mpTask.getStepAfterStep(step, taskResult);
            assertNull(step); // end of survey
        }

        { // Test exiting when you do not have an old mPower account
            TaskStepHolder holder = assertBackgroundTaskUpUntilExitJunction();
            MockMpSmartSurveyTask mpTask = holder.task;
            Step step = holder.step;
            TaskResult taskResult = holder.taskResult;

            putStringFormResult(taskResult, "Yes",
                    "participatedPrevmPower" + FORM_STEP_SUFFIX, "participatedPrevmPower");

            step = mpTask.getStepAfterStep(step, taskResult);
            assertNotNull(step);
            assertEquals("linkDataPrevmPower" + FORM_STEP_SUFFIX, step.getIdentifier()); // no rules, just keep going

            putStringFormResult(taskResult, "Yes",
                    "linkDataPrevmPower" + FORM_STEP_SUFFIX, "linkDataPrevmPower");

            // Yes sends the user to the last survey question to enter in their old account email
            step = mpTask.getStepAfterStep(step, taskResult);
            assertNotNull(step);
            assertEquals("mPower1Email" + FORM_STEP_SUFFIX, step.getIdentifier()); // no rules, just keep going

            // Anything input here still sends user to info screen about changing in settings
            step = mpTask.getStepAfterStep(step, taskResult);
            assertNotNull(step);
            assertEquals("noLinkData", step.getIdentifier()); // no rules, just keep going

            step = mpTask.getStepAfterStep(step, taskResult);
            assertNull(step); // end of survey
        }
    }

    class TaskStepHolder {
        TaskResult taskResult;
        MockMpSmartSurveyTask task;
        Step step;
    }

    /**
     * To test all the different exit scenarios of the Background survey, we make a copy of the task
     * at the point at which we need to test an exit path.  That way, we can test them all in reproducible ways.
     * Task is stopped at the "participatedPrevmPower" + FORM_STEP_SUFFIX survey question
     */
    private TaskStepHolder assertBackgroundTaskUpUntilExitJunction() {
        MpSmartSurveyTask task = new MockMpTaskFactory().createMpSmartSurveyTask(context, backgroundTaskModel);
        assertNotNull(task);
        assertTrue(task instanceof MpSmartSurveyTask);
        MockMpSmartSurveyTask mpTask = (MockMpSmartSurveyTask) task;
        assertNotNull(mpTask);
        TaskResult taskResult = new TaskResult(task.getIdentifier());
        Step step = mpTask.getStepAfterStep(null, taskResult);
        assertNotNull(step);
        assertEquals("ethnicity" + FORM_STEP_SUFFIX, step.getIdentifier()); // no rules, just keep going

        step = mpTask.getStepAfterStep(step, taskResult);
        assertNotNull(step);
        assertEquals("race" + FORM_STEP_SUFFIX, step.getIdentifier()); // no rules, just keep going

        step = mpTask.getStepAfterStep(step, taskResult);
        assertNotNull(step);
        assertEquals("zipCode" + FORM_STEP_SUFFIX, step.getIdentifier()); // no rules, just keep going

        step = mpTask.getStepAfterStep(step, taskResult);
        assertNotNull(step);
        assertEquals("education" + FORM_STEP_SUFFIX, step.getIdentifier()); // no rules, just keep going

        step = mpTask.getStepAfterStep(step, taskResult);
        assertNotNull(step);
        assertEquals("employmentStatus" + FORM_STEP_SUFFIX, step.getIdentifier()); // no rules, just keep going

        step = mpTask.getStepAfterStep(step, taskResult);
        assertNotNull(step);
        assertEquals("liveAlone" + FORM_STEP_SUFFIX, step.getIdentifier()); // no rules, just keep going

        // If you do not have parkinson's as a data group (which we don't at this point), you will see this step
        step = mpTask.getStepAfterStep(step, taskResult);
        assertNotNull(step);
        assertEquals("areYouCaregiver" + FORM_STEP_SUFFIX, step.getIdentifier()); // no rules, just keep going

        step = mpTask.getStepBeforeStep(step, taskResult);
        assertNotNull(step);
        assertEquals("liveAlone" + FORM_STEP_SUFFIX, step.getIdentifier()); // no rules, just keep going

        mpTask.setDataGroups(Collections.singletonList("parkinsons"));

        // If you do have parkinson's as a data group (which we now do), you skip the caregiver step
        step = mpTask.getStepAfterStep(step, taskResult);
        assertNotNull(step);
        assertEquals("participatedPrevClinicalTrial" + FORM_STEP_SUFFIX, step.getIdentifier()); // no rules, just keep going

        step = mpTask.getStepAfterStep(step, taskResult);
        assertNotNull(step);
        assertEquals("smartphoneUse" + FORM_STEP_SUFFIX, step.getIdentifier()); // no rules, just keep going

        step = mpTask.getStepAfterStep(step, taskResult);
        assertNotNull(step);
        assertEquals("medicationForOtherConditions" + FORM_STEP_SUFFIX, step.getIdentifier()); // no rules, just keep going

        // Remove the data groups
        mpTask.setDataGroups(new ArrayList<>());

        // You only see onsetYearForm step if you have parkinson's as a data group (which we don't at this point)
        // so we will skip that step along with all of them up until participatedPrevmPowerForm
        step = mpTask.getStepAfterStep(step, taskResult);
        assertNotNull(step);
        assertEquals("participatedPrevmPower" + FORM_STEP_SUFFIX, step.getIdentifier()); // no rules, just keep going

        // However, at this point we want to test out the path of having parkinsons too, so go back to previous
        step = mpTask.getStepBeforeStep(step, taskResult);
        assertNotNull(step);
        assertEquals("medicationForOtherConditions" + FORM_STEP_SUFFIX, step.getIdentifier()); // no rules, just keep going

        mpTask.setDataGroups(Collections.singletonList("parkinsons"));

        // You will now see the ordered next step because with have parkinsons,
        step = mpTask.getStepAfterStep(step, taskResult);
        assertNotNull(step);
        assertEquals("onsetYear" + FORM_STEP_SUFFIX, step.getIdentifier()); // no rules, just keep going

        step = mpTask.getStepAfterStep(step, taskResult);
        assertNotNull(step);
        assertEquals("diagnosisYear" + FORM_STEP_SUFFIX, step.getIdentifier()); // no rules, just keep going

        step = mpTask.getStepAfterStep(step, taskResult);
        assertNotNull(step);
        assertEquals("medicationStartYear" + FORM_STEP_SUFFIX, step.getIdentifier()); // no rules, just keep going

        step = mpTask.getStepAfterStep(step, taskResult);
        assertNotNull(step);
        assertEquals("deepBrainStimulation" + FORM_STEP_SUFFIX, step.getIdentifier()); // no rules, just keep going

        // Depending on your answer to deepBrainStimulation, you will go a different path
        // First, let's test out answering "Yes"
        putStringFormResult(taskResult, "Yes",
                "deepBrainStimulation" + FORM_STEP_SUFFIX, "deepBrainStimulation");

        // Yes sends the user dbsYear
        step = mpTask.getStepAfterStep(step, taskResult);
        assertNotNull(step);
        assertEquals("dbsYear" + FORM_STEP_SUFFIX, step.getIdentifier()); // no rules, just keep going

        step = mpTask.getStepBeforeStep(step, taskResult);
        assertNotNull(step);
        assertEquals("deepBrainStimulation" + FORM_STEP_SUFFIX, step.getIdentifier()); // no rules, just keep going

        putStringFormResult(taskResult, "No",
                "deepBrainStimulation" + FORM_STEP_SUFFIX, "deepBrainStimulation");

        // No sends the user past dbsYear to otherSurgery
        step = mpTask.getStepAfterStep(step, taskResult);
        assertNotNull(step);
        assertEquals("otherSurgery" + FORM_STEP_SUFFIX, step.getIdentifier()); // no rules, just keep going

        // Depending on your answer to otherSurgeryForm, you will go a different path
        // First, let's test out answering "No"
        putStringFormResult(taskResult, "No",
                "otherSurgery" + FORM_STEP_SUFFIX, "otherSurgery");

        // No sends the user past otherSurgeryDetails to participatedPrevmPower
        step = mpTask.getStepAfterStep(step, taskResult);
        assertNotNull(step);
        assertEquals("participatedPrevmPower" + FORM_STEP_SUFFIX, step.getIdentifier()); // no rules, just keep going

        step = mpTask.getStepBeforeStep(step, taskResult);
        assertNotNull(step);
        assertEquals("otherSurgery" + FORM_STEP_SUFFIX, step.getIdentifier()); // no rules, just keep going

        putStringFormResult(taskResult, "Yes",
                "otherSurgery" + FORM_STEP_SUFFIX, "otherSurgery");

        // Yes sends the user to otherSurgeryDetails
        step = mpTask.getStepAfterStep(step, taskResult);
        assertNotNull(step);
        assertEquals("otherSurgeryDetails" + FORM_STEP_SUFFIX, step.getIdentifier()); // no rules, just keep going

        step = mpTask.getStepAfterStep(step, taskResult);
        assertNotNull(step);
        assertEquals("participatedPrevmPower" + FORM_STEP_SUFFIX, step.getIdentifier()); // no rules, just keep going

        TaskStepHolder taskStepHolder = new TaskStepHolder();
        taskStepHolder.task = mpTask;
        taskStepHolder.step = step;
        taskStepHolder.taskResult = taskResult;
        return taskStepHolder;
    }

    private void putStringFormResult(TaskResult result, String answer, String formIdentifier, String answerIdentifier) {
        StepResult<StepResult<String>> stepResult = new StepResult<>(new Step(formIdentifier));
        StepResult<String> answerResult = new StepResult<>(new Step(answerIdentifier));
        answerResult.setResult(answer);
        stepResult.setResult(answerResult);
        result.getResults().put(formIdentifier, stepResult);
    }

    class MockMpTaskFactory extends MpTaskFactory {
        List<String> dataGroups = new ArrayList();
        public MockMpTaskFactory() {
            super();
        }

        public MockMpTaskFactory(List<String> dataGroups) {
            this();
            this.dataGroups = dataGroups;
        }

        @Override @NonNull
        public MpSmartSurveyTask createMpSmartSurveyTask(
                @NonNull Context context, @NonNull TaskModel taskModel) {
            return new MockMpSmartSurveyTask(context, taskModel, dataGroups);
        }
    }

    class MockMpSmartSurveyTask extends MpSmartSurveyTask {
        public void setDataGroups(List<String> dataGroups) {
            this.dataGroups = dataGroups;
        }
        public List<String> dataGroupsAdded = new ArrayList<>();
        public List<Step> getSteps() {
            return steps;
        }
        public Map<String, List<RuleModel>> getBeforeRules() {
            return beforeRules;
        }
        public Map<String, List<RuleModel>> getAfterRules() {
            return afterRules;
        }

        public MockMpSmartSurveyTask(
                final Context context,
                final TaskModel taskModel,
                List<String> mockDataGroups) {
            super(context, taskModel);
            dataGroups = mockDataGroups;
        }

        @Override
        protected void initDataGroups() {
            // no-op so we can set them after the initializer
        }

        @Override
        protected void assignDataGroup(String dataGroup) {
            dataGroupsAdded.add(dataGroup);
        }
    }
}

