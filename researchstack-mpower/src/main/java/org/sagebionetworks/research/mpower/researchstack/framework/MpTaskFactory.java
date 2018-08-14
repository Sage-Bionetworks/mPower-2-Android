package org.sagebionetworks.research.mpower.researchstack.framework;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.researchstack.backbone.ResourceManager;
import org.researchstack.backbone.ResourcePathManager;
import org.researchstack.backbone.model.survey.SurveyItem;
import org.researchstack.backbone.model.survey.factory.SurveyFactory;
import org.researchstack.backbone.model.taskitem.TaskItem;
import org.researchstack.backbone.model.taskitem.TaskItemAdapter;
import org.researchstack.backbone.task.Task;
import org.sagebionetworks.bridge.researchstack.onboarding.BridgeSurveyFactory;
import org.sagebionetworks.research.mpower.researchstack.step.MpInstructionStep;
import org.sagebionetworks.research.mpower.researchstack.step.MpInstructionSurveyItem;
import org.sagebionetworks.research.mpower.researchstack.step.MpPhoneInstructionStep;

public class MpTaskFactory extends BridgeSurveyFactory {

    protected class MpCustomStepCreator extends BridgeCustomStepCreator {
        @Override
        public org.researchstack.backbone.step.Step createCustomStep(
                Context context, SurveyItem item, boolean isSubtaskStep, SurveyFactory factory) {
            if (item.getCustomTypeValue() != null) {
                switch (item.getCustomTypeValue()) {
                    case MpSurveyItemAdapter.MP_INSTRUCTION_SURVEY_ITEM_TYPE:
                        if (!(item instanceof MpInstructionSurveyItem)) {
                            throw new IllegalStateException(
                                    "Error in json parsing, bp_instruction types must be BpInstructionSurveyItem");
                        }
                        return createMpInstructionStep((MpInstructionSurveyItem) item);
                    case MpSurveyItemAdapter.MP_INSTRUCTION_PHONE_SURVEY_ITEM_TYPE:
                        if (!(item instanceof MpInstructionSurveyItem)) {
                            throw new IllegalStateException(
                                    "Error in json parsing, bp_phone_instruction types must " +
                                            "be BpInstructionSurveyItem");
                        }
                        return createMpPhoneInstructionStep((MpInstructionSurveyItem) item);
                }
            }
            return super.createCustomStep(context, item, isSubtaskStep, factory);
        }
    }

    public static final String TASK_ID_SIGN_UP = "signup";

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

    protected MpInstructionStep createMpInstructionStep(MpInstructionSurveyItem item) {
        MpInstructionStep step = new MpInstructionStep(item.identifier, item.title, item.text);
        fillMpInstructionStep(step, item);
        return step;
    }

    protected MpPhoneInstructionStep createMpPhoneInstructionStep(MpInstructionSurveyItem item) {
        MpPhoneInstructionStep step = new MpPhoneInstructionStep(
                item.identifier, item.title, item.text);
        fillMpInstructionStep(step, item);
        return step;
    }

    protected void fillMpInstructionStep(MpInstructionStep step, MpInstructionSurveyItem item) {
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
}
