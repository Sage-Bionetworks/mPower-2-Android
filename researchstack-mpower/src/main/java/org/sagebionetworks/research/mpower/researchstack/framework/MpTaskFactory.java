package org.sagebionetworks.research.mpower.researchstack.framework;

import static org.sagebionetworks.research.mpower.researchstack.framework.MpSurveyItemAdapter.*;
import static org.sagebionetworks.research.mpower.researchstack.framework.MpSurveyItemAdapter.MP_INTEGER_SURVEY_ITEM_TYPE;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.researchstack.backbone.ResourceManager;
import org.researchstack.backbone.ResourcePathManager;
import org.researchstack.backbone.answerformat.AnswerFormat;
import org.researchstack.backbone.model.TaskModel;
import org.researchstack.backbone.model.survey.BooleanQuestionSurveyItem;
import org.researchstack.backbone.model.survey.ChoiceQuestionSurveyItem;
import org.researchstack.backbone.model.survey.IntegerRangeSurveyItem;
import org.researchstack.backbone.model.survey.QuestionSurveyItem;
import org.researchstack.backbone.model.survey.SurveyItem;
import org.researchstack.backbone.model.survey.SurveyItemType;
import org.researchstack.backbone.model.survey.TextfieldSurveyItem;
import org.researchstack.backbone.model.survey.factory.SurveyFactory;
import org.researchstack.backbone.model.taskitem.TaskItem;
import org.researchstack.backbone.model.taskitem.TaskItemAdapter;
import org.researchstack.backbone.step.QuestionStep;
import org.researchstack.backbone.task.SmartSurveyTask;
import org.researchstack.backbone.task.Task;
import org.sagebionetworks.bridge.researchstack.onboarding.BridgeSurveyFactory;
import org.sagebionetworks.research.mpower.researchstack.framework.step.MpFormStep;
import org.sagebionetworks.research.mpower.researchstack.framework.step.MpFormSurveyItem;
import org.sagebionetworks.research.mpower.researchstack.framework.step.MpInstructionStep;
import org.sagebionetworks.research.mpower.researchstack.framework.step.MpInstructionSurveyItem;
import org.sagebionetworks.research.mpower.researchstack.framework.step.MpPhoneInstructionStep;
import org.sagebionetworks.research.mpower.researchstack.framework.step.MpReminderStepLayout;
import org.sagebionetworks.research.mpower.researchstack.framework.step.MpSmartSurveyTask;
import org.sagebionetworks.research.mpower.researchstack.framework.step.body.MpBooleanAnswerFormat;
import org.sagebionetworks.research.mpower.researchstack.framework.step.body.MpCheckboxAnswerFormat;
import org.sagebionetworks.research.mpower.researchstack.framework.step.body.MpChoiceAnswerFormat;
import org.sagebionetworks.research.mpower.researchstack.framework.step.body.MpIntegerAnswerFormat;
import org.sagebionetworks.research.mpower.researchstack.framework.step.body.MpMultiCheckboxAnswerFormat;
import org.sagebionetworks.research.mpower.researchstack.framework.step.body.MpRadioButtonAnswerFormat;
import org.sagebionetworks.research.mpower.researchstack.framework.step.body.MpTextQuestionBody;

import java.util.Collections;
import java.util.List;

public class MpTaskFactory extends BridgeSurveyFactory {

    private Gson gson;

    public MpTaskFactory() {
        super();
        gson = createGson();
    }

    public Task createTask(Context context, String resourceName) {
        ResourcePathManager.Resource resource = ResourceManager.getInstance().getResource(resourceName);
        String json = ResourceManager.getResourceAsString(context,
                ResourceManager.getInstance().generatePath(resource.getType(), resource.getName()));
        Gson gson = createGson(); // Do not store this gson as a member variable, it has a link to Context
        TaskItem taskItem = gson.fromJson(json, TaskItem.class);
        return super.createTask(context, taskItem);
    }

    public Gson getGson() {
        return gson;
    }

    @NonNull
    public MpSmartSurveyTask createMpSmartSurveyTask(
            @NonNull Context context, @NonNull TaskModel taskModel) {
        return new MpSmartSurveyTask(context, taskModel);
    }

    @Override
    protected void setupCustomStepCreator() {
        setCustomStepCreator(new MpCustomStepCreator());
    }

    private Gson createGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(SurveyItem.class, new MpSurveyItemAdapter());
        builder.registerTypeAdapter(TaskItem.class, new TaskItemAdapter());
        return builder.create();
    }

    protected class MpCustomStepCreator extends BridgeCustomStepCreator {
        @Override
        public org.researchstack.backbone.step.Step createCustomStep(
                Context context, SurveyItem item, boolean isSubtaskStep, SurveyFactory factory) {
            if (item.getCustomTypeValue() != null) {
                switch (item.getCustomTypeValue()) {
                    case MpSurveyItemAdapter.MP_INSTRUCTION_SURVEY_ITEM_TYPE:
                        if (!(item instanceof MpInstructionSurveyItem)) {
                            throw new IllegalStateException(
                                    "Error in json parsing, Mp_instruction types must be MpInstructionSurveyItem");
                        }
                        return createMpInstructionStep(context, (MpInstructionSurveyItem) item);
                    case MpSurveyItemAdapter.MP_INSTRUCTION_PHONE_SURVEY_ITEM_TYPE:
                        if (!(item instanceof MpInstructionSurveyItem)) {
                            throw new IllegalStateException(
                                    "Error in json parsing, Mp_phone_instruction types must " +
                                            "be MpInstructionSurveyItem");
                        }
                        return createMpPhoneInstructionStep((MpInstructionSurveyItem) item);
                    case MpSurveyItemAdapter.MP_FORM_SURVEY_ITEM_TYPE:
                        if (!(item instanceof MpFormSurveyItem)) {
                            throw new IllegalStateException("Error in json parsing, Mp_form types must be MpFormSurveyItem");
                        }
                        return createMpFormStep(context, (MpFormSurveyItem)item);
                    case MpSurveyItemAdapter.MP_REMINDER_SURVEY_ITEM_TYPE:
                        if (!(item instanceof MpReminderStepLayout.SurveyItem)) {
                            throw new IllegalStateException("Error in json parsing, mpReminder types must be MpReminderStepLayout.SurveyItem");
                        }
                        return createMpReminderStep(context, (MpReminderStepLayout.SurveyItem)item);
                    case MP_BOOLEAN_SURVEY_ITEM_TYPE:
                    case MP_INTEGER_SURVEY_ITEM_TYPE:
                    case MP_MULTIPLE_CHOICE_SURVEY_ITEM_TYPE:
                    case MP_SINGLE_CHOICE_SURVEY_ITEM_TYPE:
                    case MP_CHECKBOX_SURVEY_ITEM_TYPE:
                    case MP_MULTI_CHECKBOX_SURVEY_ITEM_TYPE:
                        if (!(item instanceof QuestionSurveyItem)) {
                            throw new IllegalStateException("Error in json parsing " + item.getCustomTypeValue() + ", types must be QuestionSurveyItem");
                        }
                        // Even though these weren't wrapped in a form step, we are going to wrap
                        // them in a MpFormStep so that the UI looks appropriate
                        QuestionSurveyItem questionItem = (QuestionSurveyItem)item;
                        MpFormSurveyItem compoundQuestionSurveyItem = new MpFormSurveyItemWrapper();
                        compoundQuestionSurveyItem.identifier = item.identifier + "Form";
                        compoundQuestionSurveyItem.items = Collections.singletonList(item);
                        compoundQuestionSurveyItem.skipIdentifier = questionItem.skipIdentifier;
                        compoundQuestionSurveyItem.skipIfPassed = questionItem.skipIfPassed;
                        compoundQuestionSurveyItem.expectedAnswer = questionItem.expectedAnswer;
                        return createMpFormStep(context, compoundQuestionSurveyItem);
                }
            }
            return super.createCustomStep(context, item, isSubtaskStep, factory);
        }
    }

    public static class MpFormSurveyItemWrapper extends MpFormSurveyItem {
        /* Default constructor needed for serilization/deserialization of object */
        public MpFormSurveyItemWrapper() {
            super();
        }

        @Override
        public String getCustomTypeValue() {
            return MpSurveyItemAdapter.MP_FORM_SURVEY_ITEM_TYPE;
        }
    }

    @Override
    public AnswerFormat createCustomAnswerFormat(Context context, QuestionSurveyItem item) {
        if (item.getCustomTypeValue() != null) {
            switch (item.getCustomTypeValue()) {
                case MP_BOOLEAN_SURVEY_ITEM_TYPE:
                    return createMpBooleanAnswerFormat(context, item);
                case MP_TEXT_SURVEY_ITEM_TYPE:
                    return createMpTextAnswerFormat(item);
                case MP_INTEGER_SURVEY_ITEM_TYPE:
                    return createMpIntegerAnswerFormat(context, item);
                case MP_MULTIPLE_CHOICE_SURVEY_ITEM_TYPE:
                case MP_SINGLE_CHOICE_SURVEY_ITEM_TYPE:
                    return createMpChoiceAnswerFormat(context, item);
                case MP_CHECKBOX_SURVEY_ITEM_TYPE:
                    return createMpCheckboxAnswerFormat(context, item);
                case MP_MULTI_CHECKBOX_SURVEY_ITEM_TYPE:
                    return createMpMultiCheckboxAnswerFormat(context, item);
            }
        }
        return super.createCustomAnswerFormat(context, item);
    }

    protected MpFormStep createMpFormStep(Context context, MpFormSurveyItem item) {
        if (item.items == null || item.items.isEmpty()) {
            throw new IllegalStateException("compound surveys must have step items to proceed");
        }
        List<QuestionStep> questionSteps = super.formStepCreateQuestionSteps(context, item);
        MpFormStep step = new MpFormStep(item.identifier, item.title, item.text, questionSteps);
        fillMpFormStep(step, item);
        return step;
    }

    protected void fillMpFormStep(MpFormStep step, MpFormSurveyItem item) {
        fillNavigationFormStep(step, item);
        if (item.statusBarColorRes != null) {
            step.statusBarColorRes = item.statusBarColorRes;
        }
        if (item.backgroundColorRes != null) {
            step.backgroundColorRes = item.backgroundColorRes;
        }
        if (item.imageColorRes != null) {
            step.imageBackgroundColorRes = item.imageColorRes;
        }
        if (item.buttonTitle != null) {
            step.buttonTitle = item.buttonTitle;
        }
        if (item.hideBackButton != null && item.hideBackButton) {
            step.hideBackButton = item.hideBackButton;
        }
        if (item.textContainerBottomPaddingRes != null) {
            step.textContainerBottomPaddingRes = item.textContainerBottomPaddingRes;
        }
        if (item.bottomLinkTaskId != null) {
            step.bottomLinkTaskId = item.bottomLinkTaskId;
        }
    }

    public MpInstructionStep createMpInstructionStep(Context context, MpInstructionSurveyItem item) {
        MpInstructionStep step = new MpInstructionStep(item.identifier, item.title, item.text);
        fillMpInstructionStep(step, item);
        return step;
    }

    public void fillMpInstructionStep(MpInstructionStep step, MpInstructionSurveyItem item) {
        fillInstructionStep(step, item);
        if (item.buttonText != null) {
            step.buttonText = item.buttonText;
        }
        if (item.backgroundColorRes != null) {
            step.backgroundColorRes = item.backgroundColorRes;
        }
        if (item.backgroundDrawableRes != null) {
            step.backgroundDrawableRes = item.backgroundDrawableRes;
        }
        if (item.imageColorRes != null) {
            step.imageBackgroundColorRes = item.imageColorRes;
        }
        if (item.tintColorRes != null) {
            step.tintColorRes = item.tintColorRes;
        }
        if (item.statusBarColorRes != null) {
            step.statusBarColorRes = item.statusBarColorRes;
        }
        if (item.hideProgress) {
            step.hideProgress = true;
        }
        if (item.behindToolbar) {
            step.behindToolbar = true;
        }
        if (item.hideToolbar) {
            step.hideToolbar = true;
        }
        if (item.mediaVolume) {
            step.mediaVolume = true;
        }
        if (item.textColorRes != null) {
            step.textColorRes = item.textColorRes;
        }
        if (item.textContainerHeightRes != null) {
            step.textContainerHeightRes = item.textContainerHeightRes;
        }
        if (item.bottomLinkText != null) {
            step.bottomLinkText = item.bottomLinkText;
        }
        if (item.bottomLinkStepId != null) {
            step.bottomLinkStepId = item.bottomLinkStepId;
        }
        if (item.bottomLinkTaskId != null) {
            step.bottomLinkTaskId = item.bottomLinkTaskId;
        }
        if (item.bottomLinkColorRes != null) {
            step.bottomLinkColorRes = item.bottomLinkColorRes;
        }
        if (item.topCrop) {
            step.topCrop = true;
        }
        if (item.centerText != null && item.centerText) {
            step.centerText = true;
        }
        if (item.soundRes != null) {
            step.soundRes = item.soundRes;
        }
        if (item.submitBarColorRes != null) {
            step.submitBarColorRes = item.submitBarColorRes;
        }
        if (item.advanceOnImageClick != null && item.advanceOnImageClick) {
            step.advanceOnImageClick = true;
        }
        if (item.actionEndOnNext != null && item.actionEndOnNext) {
            step.actionEndOnNext = true;
        }
        if (item.bottomContainerColorRes != null) {
            step.bottomContainerColorRes = item.bottomContainerColorRes;
        }
    }

    public MpBooleanAnswerFormat createMpBooleanAnswerFormat(Context context, QuestionSurveyItem item) {
        if (!(item instanceof BooleanQuestionSurveyItem)) {
            throw new IllegalStateException("Error in json parsing, QUESTION_BOOLEAN types must be BooleanQuestionSurveyItem");
        }
        MpBooleanAnswerFormat format = new MpBooleanAnswerFormat();
        fillBooleanAnswerFormat(context, format, (BooleanQuestionSurveyItem)item);
        return format;
    }

    public MpTextQuestionBody.AnswerFormat createMpTextAnswerFormat(QuestionSurveyItem item) {
        if (!(item instanceof TextfieldSurveyItem)) {
            throw new IllegalStateException("Error in json parsing, " +
                    "MpText types must be TextfieldSurveyItem");
        }

        MpTextQuestionBody.AnswerFormat format = new MpTextQuestionBody.AnswerFormat();
        fillTextAnswerFormat(format, (TextfieldSurveyItem)item);
        return format;
    }

    public MpIntegerAnswerFormat createMpIntegerAnswerFormat(Context context, QuestionSurveyItem item) {
        if (!(item instanceof IntegerRangeSurveyItem)) {
            throw new IllegalStateException("Error in json parsing, QUESTION_INTEGER types must be IntegerRangeSurveyItem");
        }
        MpIntegerAnswerFormat format = new MpIntegerAnswerFormat();
        fillIntegerAnswerFormat(format, (IntegerRangeSurveyItem)item);
        return format;
    }

    public MpChoiceAnswerFormat createMpChoiceAnswerFormat(Context context, QuestionSurveyItem item) {
        if (!(item instanceof ChoiceQuestionSurveyItem)) {
            throw new IllegalStateException("Error in json parsing, this type must be ChoiceQuestionSurveyItem");
        }
        MpChoiceAnswerFormat format = new MpChoiceAnswerFormat();
        fillChoiceAnswerFormat(format, (ChoiceQuestionSurveyItem)item);
        // Override setting multiple choice answer format, since it is a custom survey type
        if (MP_MULTIPLE_CHOICE_SURVEY_ITEM_TYPE.equals(item.getCustomTypeValue())) {
            format.setAnswerStyle(AnswerFormat.ChoiceAnswerStyle.MultipleChoice);
        }
        return format;
    }

    public MpCheckboxAnswerFormat createMpCheckboxAnswerFormat(Context context, QuestionSurveyItem item) {
        if (!(item instanceof BooleanQuestionSurveyItem)) {
            throw new IllegalStateException("Error in json parsing, QUESTION_BOOLEAN types must be BooleanQuestionSurveyItem");
        }
        MpCheckboxAnswerFormat format = new MpCheckboxAnswerFormat();
        fillBooleanAnswerFormat(context, format, (BooleanQuestionSurveyItem)item);
        return format;
    }

    public MpMultiCheckboxAnswerFormat createMpMultiCheckboxAnswerFormat(Context context, QuestionSurveyItem item) {
        if (!(item instanceof ChoiceQuestionSurveyItem)) {
            throw new IllegalStateException("Error in json parsing, this type must be ChoiceQuestionSurveyItem");
        }
        MpMultiCheckboxAnswerFormat format = new MpMultiCheckboxAnswerFormat();
        fillChoiceAnswerFormat(format, (ChoiceQuestionSurveyItem)item);
        return format;
    }

    public MpRadioButtonAnswerFormat createMpRadioAnswerFormat(Context context, QuestionSurveyItem item) {
        if (!(item instanceof ChoiceQuestionSurveyItem)) {
            throw new IllegalStateException("Error in json parsing, this type must be ChoiceQuestionSurveyItem");
        }
        if (item.type == SurveyItemType.QUESTION_MULTIPLE_CHOICE) {
            throw new IllegalStateException("Radio button types can only be single choice");
        }
        MpRadioButtonAnswerFormat format = new MpRadioButtonAnswerFormat();
        fillChoiceAnswerFormat(format, (ChoiceQuestionSurveyItem)item);
        return format;
    }

    protected MpReminderStepLayout.Step createMpReminderStep(
            Context context, MpReminderStepLayout.SurveyItem item) {
        if (item.items == null || item.items.isEmpty()) {
            throw new IllegalStateException("compound surveys must have step items to proceed");
        }
        List<QuestionStep> questionSteps = super.formStepCreateQuestionSteps(context, item);
        MpReminderStepLayout.Step step = new MpReminderStepLayout.Step(
                item.identifier, item.title, item.text, questionSteps);
        fillMpFormStep(step, item);
        step.setImage(item.image);
        step.setReminderType(item.reminderType);
        if (item.neverSkip != null && item.neverSkip) {
            step.setNeverSkip(true);
        }
        if (item.hideToolbar != null && item.hideToolbar) {
            step.setHideToolbar(true);
        }
        return step;
    }

    protected MpPhoneInstructionStep createMpPhoneInstructionStep(MpInstructionSurveyItem item) {
        MpPhoneInstructionStep step = new MpPhoneInstructionStep(
                item.identifier, item.title, item.text);
        fillMpInstructionStep(step, item);
        return step;
    }
}
