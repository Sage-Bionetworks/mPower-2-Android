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
import android.support.annotation.VisibleForTesting;
import android.widget.ImageView.ScaleType;

import org.researchstack.backbone.answerformat.AnswerFormat;
import org.researchstack.backbone.model.Choice;
import org.researchstack.backbone.model.TaskModel;
import org.researchstack.backbone.model.TaskModel.EnumerationModel;
import org.researchstack.backbone.model.TaskModel.RuleModel;
import org.researchstack.backbone.model.TaskModel.StepModel;
import org.researchstack.backbone.model.survey.ChoiceQuestionSurveyItem;
import org.researchstack.backbone.model.survey.IntegerRangeSurveyItem;
import org.researchstack.backbone.model.survey.QuestionSurveyItem;
import org.researchstack.backbone.model.survey.SurveyItemType;
import org.researchstack.backbone.model.survey.TextfieldSurveyItem;
import org.researchstack.backbone.result.StepResult;
import org.researchstack.backbone.result.TaskResult;
import org.researchstack.backbone.step.QuestionStep;
import org.researchstack.backbone.step.Step;
import org.researchstack.backbone.task.SmartSurveyTask;

import org.researchstack.backbone.utils.LogExt;
import org.researchstack.backbone.utils.StepResultHelper;
import org.sagebionetworks.bridge.rest.model.StudyParticipant;

import org.sagebionetworks.research.mpower.researchstack.framework.MpDataProvider;
import org.sagebionetworks.research.mpower.researchstack.framework.MpTaskFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rx.subscriptions.CompositeSubscription;

public class MpSmartSurveyTask extends SmartSurveyTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(MpSmartSurveyTask.class);

    protected transient MpTaskFactory taskFactory = new MpTaskFactory();

    public static final String FORM_STEP_SUFFIX = "MpSmartSurveyTaskForm";
    private static final String STUDY_BURST_COMPLETE_STEP_ID = "studyBurstCompletion";

    protected Map<String, List<RuleModel>> beforeRules;
    protected Map<String, List<TaskModel.RuleModel>> afterRules;
    protected List<String> dataGroups;

    /**
     * The simple path the user has taken to get to the current step identifier
     */
    protected List<String> surveyPath;

    /**
     * Used to manage adding data groups
     */
    protected transient CompositeSubscription dataGroupSubscriptions = new CompositeSubscription();

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
        beforeRules = new HashMap<>();
        afterRules = new HashMap<>();
        staticStepIdentifiers = new ArrayList<>(taskModel.elements.size());
        surveyPath = new ArrayList<>();
        initDataGroups();
        for (TaskModel.StepModel stepModel : taskModel.elements) {
            String stepIdentifier = null;
            switch (stepModel.type) {
                case SURVEY_TYPE_QUESTION: {
                    AnswerFormat answerFormat = from(context, stepModel);

                    QuestionStep questionStep = new QuestionStep(stepModel.identifier, null, answerFormat);
                    questionStep.setText(stepModel.promptDetail);
                    // MP survey questions are not skip-able
                    questionStep.setOptional(false);
                    List<QuestionStep> questionStepList = new ArrayList<>();
                    questionStepList.add(questionStep);

                    String formStepIdentifier = stepModel.identifier + FORM_STEP_SUFFIX;
                    MpFormStep formStep = new MpFormStep(formStepIdentifier,
                            stepModel.prompt, stepModel.promptDetail, questionStepList);
                    // MP survey questions are not skip-able
                    formStep.setOptional(false);

                    steps.add(formStep);
                    staticStepIdentifiers.add(formStepIdentifier);
                    stepIdentifier = formStepIdentifier;
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
                    stepIdentifier = stepModel.identifier;
                    break;
                }
                default: {
                    LOGGER.error("Ignored step model " + stepModel.identifier +
                            " because of unknown type " + stepModel.type);
                    break;
                }
            }
            if (stepIdentifier != null) {
                if (stepModel.beforeRules != null) {
                    beforeRules.put(stepIdentifier, stepModel.beforeRules);
                }
                if (stepModel.afterRules != null) {
                    afterRules.put(stepIdentifier, stepModel.afterRules);
                }
                if (stepModel.constraints != null && stepModel.constraints.rules != null) {
                    rules.put(stepIdentifier, stepModel.constraints.rules);
                }
            }
        }

        dynamicStepIdentifiers = new ArrayList<>(staticStepIdentifiers);
    }

    @VisibleForTesting
    protected void initDataGroups() {
        // getLocalDataGroups returns an immutable list, so make sure to copy it
        dataGroups = new ArrayList<>(MpDataProvider.getInstance().getLocalDataGroups());
    }

    @Override
    public Step getStepAfterStep(Step step, TaskResult result) {
        String currentIdentifier = step == null ? null : step.getIdentifier();

        int nextStepIdx = (step == null) ? 0 : (staticStepIdentifiers.indexOf(currentIdentifier) + 1);
        if (nextStepIdx >= staticStepIdentifiers.size()) {
            return null; // end of survey
        }
        String nextStepIdentifier = staticStepIdentifiers.get(nextStepIdx);

        { // Change dynamic step identifiers based on after rules and step results
            List<TaskModel.RuleModel> stepRules = afterRules.get(currentIdentifier);
            if (stepRules != null && !stepRules.isEmpty()) {
                String skipToStep;
                LogExt.d(getClass(), "Rules exist for this step");
                Object answer = answerForIdentifier(currentIdentifier, result);
                skipToStep = processRules(stepRules, answer);
                if (END_OF_SURVEY_MARKER.equals(skipToStep)) {
                    return null;
                }
                if (skipToStep != null) {
                    // A skipToStep identifier may not have the correct suffix, make sure it does
                    nextStepIdentifier = reformatSkipToStepIdentifier(skipToStep);
                }
            }
        }

        // Change dynamic step identifiers based on before rules and step results
        if (nextStepIdentifier != null) {
            boolean shouldContinueToFindNextStep;
            do {
                List<TaskModel.RuleModel> stepRules = beforeRules.get(nextStepIdentifier);
                if (stepRules != null && !stepRules.isEmpty()) {
                    String skipToStep = processRules(stepRules, null);
                    if (skipToStep == null) {
                        shouldContinueToFindNextStep = false;
                    } else if (END_OF_SURVEY_MARKER.equals(skipToStep)) {
                        return null; // end of survey
                    } else if (NEXT_SURVEY_ELEMENT.equals(skipToStep)) {
                        int staticNextStepIdx = staticStepIdentifiers.indexOf(nextStepIdentifier) + 1;
                        if (staticNextStepIdx >= staticStepIdentifiers.size()) {
                            return null; // end of survey
                        }
                        nextStepIdentifier = staticStepIdentifiers.get(staticNextStepIdx);
                        shouldContinueToFindNextStep = true;
                    } else {
                        nextStepIdentifier = reformatSkipToStepIdentifier(skipToStep);
                        shouldContinueToFindNextStep = true;
                    }
                } else {
                    shouldContinueToFindNextStep = false;
                }
            } while (shouldContinueToFindNextStep);
        }

        // Add to the survey path so we can correctly navigate backwards in the path
        if (nextStepIdentifier != null) {
            surveyPath.add(nextStepIdentifier);
        }

        return nextStepIdentifier == null ? null : getStep(nextStepIdentifier);
    }

    @Override
    protected Step getStep(String identifier) {
        Step step = super.getStep(identifier);
        if (step == null) {
            // Some skipTo fields may not have our artificial Form step suffix on it
            step = super.getStep(identifier + FORM_STEP_SUFFIX);
        }
        return step;
    }

    @Override
    public Step getStepBeforeStep(Step step, TaskResult result) {
        if (surveyPath.isEmpty() || surveyPath.size() == 1) {
            return null;
        }
        int currentStepIdx = surveyPath.size() - 1;
        String previousIdentifier = surveyPath.get(currentStepIdx - 1);
        surveyPath.remove(currentStepIdx);
        return getStep(previousIdentifier);
    }

    protected String reformatSkipToStepIdentifier(String identifier) {
        if (identifier == null) {
            return null;
        }
        if (getStep(identifier) == null) {
            return identifier + FORM_STEP_SUFFIX;
        }
        return identifier;
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
        item.placeholderText = model.constraints.patternPlaceholder;
        fillQuestionSurveyItem(item, model);
        return item;
    }

    protected <T extends IntegerRangeSurveyItem> T fillIntegerSurveyItem(T item, StepModel model) {
        item.min = model.constraints.minValue;
        item.max = model.constraints.maxValue;
        item.placeholderText = model.constraints.patternPlaceholder;
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
        // There is a chance that title will not be used and just prompt and promptDetail
        if (model.title == null && model.promptDetail != null) {
            item.title = model.prompt;
            item.text = model.promptDetail;
        }
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

    @Override
    protected String processRules(List<RuleModel> stepRules, Object answer) {
        String skipToIdentifier = super.processRules(stepRules, answer);

        // Rules were either not applicable, or not processed by the sub-class
        if (skipToIdentifier == null) {
            for (RuleModel rule : stepRules) {
                if (rule.dataGroups != null && !rule.dataGroups.isEmpty()) {
                    if (OPERATOR_ALL.equals(rule.operator)) {
                        if (dataGroups.containsAll(rule.dataGroups)) {
                            skipToIdentifier = rule.skipTo;
                        }
                    } else if (OPERATOR_ANY.equals(rule.operator)) {
                        boolean containsAny = false;
                        for (String dataGroup : rule.dataGroups) {
                            if (dataGroups.contains(dataGroup)) {
                                containsAny = true;
                            }
                        }
                        // If display if rule isn't met, skip to next survey element
                        if (rule.displayIf != null && rule.displayIf && !containsAny) {
                            skipToIdentifier = NEXT_SURVEY_ELEMENT;
                        } else if (rule.displayUnless != null && rule.displayUnless && containsAny) {
                            // If display unless rule is met, skip to next survey element
                            skipToIdentifier = NEXT_SURVEY_ELEMENT;
                        } else if (containsAny && rule.skipTo != null) {
                            // All other any data group will be based on the skipTo action
                            skipToIdentifier = rule.skipTo;
                        }
                    }
                    else {
                        LOGGER.warn("Unknown data group operator " + rule.operator);
                    }
                }
            }
        }

        return skipToIdentifier;
    }

    /**
     * Depending on the result, there may have been some rules that require post-processing,
     * like when a certain answer will add a data group.
     * @param taskResult from completing this smart survey task
     */
    public void processTaskResult(TaskResult taskResult) {
        // Currently, the only after-task rules that need enforced are the "assignDataGroup" actions
        for (String afterRuleKey : afterRules.keySet()) {
            List<RuleModel> ruleList = afterRules.get(afterRuleKey);
            if (ruleList != null) {
                for (RuleModel rule : ruleList) {
                    if (rule.assignDataGroup != null) {
                        Object answer = answerForIdentifier(afterRuleKey, taskResult);
                        // After finding the correct result, compare the rule
                        if (rule.value != null && answer != null) {
                            if (OPERATOR_EQUAL.equals(rule.operator) &&
                                    rule.value.equals(answer)) {
                                assignDataGroup(rule.assignDataGroup);
                            } else if (OPERATOR_NOT_EQUAL.equals(rule.operator) &&
                                    !rule.value.equals(answer)) {
                                assignDataGroup(rule.assignDataGroup);
                            } else {
                                LOGGER.warn("Operator " + rule.operator +
                                        " not supported with assignDataGroup function");
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Finds an answer object in a TaskResult
     * @param stepIdentifier to use as an identifier to find the result
     * @param taskResult to holding the StepResult list
     * @return the root answer value, null if none was found
     */
    @Override
    protected Object answerForIdentifier(String stepIdentifier, TaskResult taskResult) {
        // We wrap answers in an extra Form step for UI purposes
        // To get the correct base answer, we need to provide the correct step identifier without the suffix
        if (stepIdentifier.endsWith(FORM_STEP_SUFFIX)) {
            stepIdentifier = stepIdentifier.substring(
                    0, stepIdentifier.length() - FORM_STEP_SUFFIX.length());
        }
        return super.answerForIdentifier(stepIdentifier, taskResult);
    }

    protected void assignDataGroup(String dataGroup) {
        dataGroups.add(dataGroup);
        // Convert to set to make sure that data groups are unique
        final Set<String> finalDataGroupSet = new HashSet<>(dataGroups);
        final List<String> finalDataGroups = new ArrayList<>();
        finalDataGroups.addAll(finalDataGroupSet);

        StudyParticipant studyParticipant = new StudyParticipant();
        studyParticipant.dataGroups(finalDataGroups);
        dataGroupSubscriptions.add(MpDataProvider.getInstance()
                .updateStudyParticipant(studyParticipant).subscribe(userSessionInfo -> {
                    LOGGER.info("Successfully updated data groups to " + finalDataGroups);
                }, throwable -> {
                    LOGGER.warn("Error updating data groups to " + finalDataGroups);
                }));
    }
}
