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

package org.sagebionetworks.research.mpower.researchstack.framework.step;

import android.content.Context;
import android.widget.ImageView.ScaleType;

import com.google.common.collect.ImmutableSet;

import org.researchstack.backbone.answerformat.AnswerFormat;
import org.researchstack.backbone.model.Choice;
import org.researchstack.backbone.model.TaskModel;
import org.researchstack.backbone.model.TaskModel.EnumerationModel;
import org.researchstack.backbone.model.TaskModel.StepModel;
import org.researchstack.backbone.model.survey.BooleanQuestionSurveyItem;
import org.researchstack.backbone.model.survey.ChoiceQuestionSurveyItem;
import org.researchstack.backbone.model.survey.InstructionSurveyItem;
import org.researchstack.backbone.model.survey.IntegerRangeSurveyItem;
import org.researchstack.backbone.model.survey.QuestionSurveyItem;
import org.researchstack.backbone.model.survey.SurveyItemType;
import org.researchstack.backbone.model.survey.TextfieldSurveyItem;
import org.researchstack.backbone.step.InstructionStep;
import org.researchstack.backbone.step.QuestionStep;
import org.researchstack.backbone.task.SmartSurveyTask;
import org.sagebionetworks.research.mpower.researchstack.framework.MpTaskFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MpSmartSurveyTask extends SmartSurveyTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(MpSmartSurveyTask.class);

    protected transient MpTaskFactory taskFactory = new MpTaskFactory();

    private static final String STUDY_BURST_COMPLETE_STEP_ID = "studyBurstCompletion";

    /**
     * Creates a SmartSurveyTask from a {@link TaskModel} object
     *
     * @param context
     *         context for fetching any resources needed
     * @param taskModel
     *         Java representation of the task json
     */
    public MpSmartSurveyTask(final Context context, final TaskModel taskModel) {
        super(taskModel.identifier);
        steps = new ArrayList<>();
        rules = new HashMap<>();
        staticStepIdentifiers = new ArrayList<>(taskModel.elements.size());
        for (TaskModel.StepModel stepModel : taskModel.elements) {
            switch (stepModel.type) {
                case SURVEY_TYPE_QUESTION: {
                    AnswerFormat answerFormat = from(context, stepModel);

                    QuestionStep questionStep = new QuestionStep(stepModel.identifier, null, answerFormat);
                    questionStep.setText(stepModel.promptDetail);
                    // MP survey questions are not skip-able
                    questionStep.setOptional(false);
                    List<QuestionStep> questionStepList = new ArrayList<>();
                    questionStepList.add(questionStep);

                    String formStepIdentifier = stepModel.identifier + "Form";
                    MpFormStep formStep = new MpFormStep(formStepIdentifier,
                            stepModel.prompt, stepModel.promptDetail, questionStepList);
                    // MP survey questions are not skip-able
                    formStep.setOptional(false);

                    steps.add(formStep);
                    staticStepIdentifiers.add(formStepIdentifier);
                    rules.put(formStepIdentifier, stepModel.constraints.rules);
                    break;
                }
                /*
                In a survey JSON file, if you want to define a step that has text but no question,
                set the type to "SurveyTextOnly" instead of "SurveyQuestion"
                 */
                case SURVEY_TYPE_TEXT:
                case SURVEY_TYPE_INFO: {
                    MpInstructionSurveyItem item = fillInstructionSurveyItem(new MpInstructionSurveyItem(), stepModel);
                    MpInstructionStep instructionStep = taskFactory.createMpInstructionStep(context, item);
                    steps.add(instructionStep);
                    staticStepIdentifiers.add(stepModel.identifier);
                    break;
                }
                default: {
                    LOGGER.error("Ignored step model " + stepModel.identifier +
                            " because of unknown type " + stepModel.type);
                    break;
                }
            }
        }

        dynamicStepIdentifiers = new ArrayList<>(staticStepIdentifiers);
    }

    protected AnswerFormat from(Context context, TaskModel.StepModel model) {
        AnswerFormat answerFormat;
        switch (model.uiHint) {
            case UI_HINT_RADIO: {
                ChoiceQuestionSurveyItem item = fillChoiceSurveyItem(new ChoiceQuestionSurveyItem(), model);
                answerFormat = taskFactory.createMpRadioAnswerFormat(context, item);
                break;
            }
            case UI_HINT_CHECKBOX: {
                ChoiceQuestionSurveyItem item = fillChoiceSurveyItem(new ChoiceQuestionSurveyItem(), model);
                answerFormat = taskFactory.createMpMultiCheckboxAnswerFormat(context, item);
                break;
            }
            case UI_HINT_LIST: {
                ChoiceQuestionSurveyItem item = fillChoiceSurveyItem(new ChoiceQuestionSurveyItem(), model);
                answerFormat = taskFactory.createMpChoiceAnswerFormat(context, item);
                break;
            }
            case UI_HINT_NUMBER: {
                IntegerRangeSurveyItem item = fillIntegerSurveyItem(new IntegerRangeSurveyItem(), model);
                answerFormat = taskFactory.createMpIntegerAnswerFormat(context, item);
                break;
            }
            case UI_HINT_TEXT: {
                TextfieldSurveyItem item = fillTextSurveyItem(new TextfieldSurveyItem(), model);
                answerFormat = taskFactory.createMpTextAnswerFormat(item);
                break;
            }
            default: {
                answerFormat = super.from(context, model.constraints);
                break;
            }
        }
        return answerFormat;
    }

    protected <T extends TextfieldSurveyItem> T fillTextSurveyItem(T item, StepModel model) {
        item.isMultipleLines = model.constraints.multipleLines;
        item.maxLength = model.constraints.maxLength;
        fillQuestionSurveyItem(item, model);
        return item;
    }

    protected <T extends IntegerRangeSurveyItem> T fillIntegerSurveyItem(T item, StepModel model) {
        item.min = model.constraints.minValue;
        item.max = model.constraints.maxValue;
        fillQuestionSurveyItem(item, model);
        return item;
    }

    protected <T extends ChoiceQuestionSurveyItem> T fillChoiceSurveyItem(T item, StepModel model) {
        List<Choice> choiceList = new ArrayList<>();
        if (model.constraints != null && model.constraints.enumeration != null) {
            // TODO: mdephillips 10/14/18 check dataType if other than a "String", but not needed for mPower
            for (int i = 0; i < model.constraints.enumeration.size(); i++) {
                EnumerationModel enumerationModel = model.constraints.enumeration.get(i);
                choiceList.add(new Choice<>(enumerationModel.label, enumerationModel.value.toString()));
            }
        }
        item.items = choiceList;
        if (model.constraints != null) {
            item.type = model.constraints.allowMultiple ?
                    SurveyItemType.QUESTION_MULTIPLE_CHOICE :
                    SurveyItemType.QUESTION_SINGLE_CHOICE;
            if (UI_HINT_RADIO.equals(model.uiHint)) {
                item.type = SurveyItemType.QUESTION_SINGLE_CHOICE;
            }
        }
        fillQuestionSurveyItem(item, model);
        return item;
    }

    protected <T extends QuestionSurveyItem> T fillQuestionSurveyItem(T item, TaskModel.StepModel model) {
        item.identifier = model.identifier;
        item.title = model.title;
        item.text = model.prompt;
        return item;
    }

    protected <T extends MpInstructionSurveyItem> T fillInstructionSurveyItem(T item, TaskModel.StepModel model) {

        item.identifier = model.identifier;
        item.title = model.title;
        item.text = model.prompt;
        item.detailText = model.promptDetail;

        if (STUDY_BURST_COMPLETE_STEP_ID.equals(item.identifier)) {
            item.image = "mp_study_burst_complete";
            item.statusBarColorRes = "rsdCompletionGradientRight";
            item.scaleType = ScaleType.FIT_START;
        }

        item.centerText = true;
        item.hideProgress = true;
        item.behindToolbar = true;

        return item;
    }
}
