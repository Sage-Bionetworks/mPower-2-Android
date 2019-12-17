package org.sagebionetworks.research.mpower.researchstack.framework;

import androidx.annotation.VisibleForTesting;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;

import org.sagebionetworks.researchstack.backbone.model.survey.BooleanQuestionSurveyItem;
import org.sagebionetworks.researchstack.backbone.model.survey.ChoiceQuestionSurveyItem;
import org.sagebionetworks.researchstack.backbone.model.survey.IntegerRangeSurveyItem;
import org.sagebionetworks.researchstack.backbone.model.survey.SurveyItem;
import org.sagebionetworks.researchstack.backbone.model.survey.TextfieldSurveyItem;
import org.sagebionetworks.bridge.researchstack.task.creation.BridgeSurveyItemAdapter;
import org.sagebionetworks.research.mpower.researchstack.framework.step.MpFormSurveyItem;
import org.sagebionetworks.research.mpower.researchstack.framework.step.MpInstructionSurveyItem;

import java.util.Map;

public class MpSurveyItemAdapter extends BridgeSurveyItemAdapter {

    // Form Survey Items
    public static final String MP_FORM_SURVEY_ITEM_TYPE             = "mpForm";
    public static final String MP_TEXT_SURVEY_ITEM_TYPE             = "mpText";
    public static final String MP_INTEGER_SURVEY_ITEM_TYPE          = "mpInteger";
    public static final String MP_SINGLE_CHOICE_SURVEY_ITEM_TYPE    = "mpSingleChoice";
    public static final String MP_MULTIPLE_CHOICE_SURVEY_ITEM_TYPE  = "mpMultipleChoice";
    public static final String MP_CHECKBOX_SURVEY_ITEM_TYPE         = "mpCheckbox";
    public static final String MP_MULTI_CHECKBOX_SURVEY_ITEM_TYPE   = "mpMultiCheckbox";
    public static final String MP_BOOLEAN_SURVEY_ITEM_TYPE          = "mpBoolean";
    public static final String MP_SPINNER_SURVEY_ITEM_TYPE          = "mpSpinner";

    // Custom Survey Items
    public static final String MP_INSTRUCTION_SURVEY_ITEM_TYPE = "mpInstruction";
    public static final String MP_INSTRUCTION_PHONE_SURVEY_ITEM_TYPE = "mpPhoneInstruction";

    @VisibleForTesting
    static final Map<String, Class<? extends SurveyItem>> TYPE_TO_CLASS =
            ImmutableMap.<String, Class<? extends org.sagebionetworks.researchstack.backbone.model.survey.SurveyItem>>builder()

                    // Form Mappings
                    .put(MP_FORM_SURVEY_ITEM_TYPE,              MpFormSurveyItem.class)
                    .put(MP_TEXT_SURVEY_ITEM_TYPE,              TextfieldSurveyItem.class)
                    .put(MP_INTEGER_SURVEY_ITEM_TYPE,           IntegerRangeSurveyItem.class)
                    .put(MP_SINGLE_CHOICE_SURVEY_ITEM_TYPE,     ChoiceQuestionSurveyItem.class)
                    .put(MP_MULTIPLE_CHOICE_SURVEY_ITEM_TYPE,   ChoiceQuestionSurveyItem.class)
                    .put(MP_CHECKBOX_SURVEY_ITEM_TYPE,          BooleanQuestionSurveyItem.class)
                    .put(MP_MULTI_CHECKBOX_SURVEY_ITEM_TYPE,    ChoiceQuestionSurveyItem.class)
                    .put(MP_BOOLEAN_SURVEY_ITEM_TYPE,           BooleanQuestionSurveyItem.class)
                    .put(MP_SPINNER_SURVEY_ITEM_TYPE,           ChoiceQuestionSurveyItem.class)

                    // Custom Mappings
                    .put(MP_INSTRUCTION_SURVEY_ITEM_TYPE,       MpInstructionSurveyItem.class)
                    .put(MP_INSTRUCTION_PHONE_SURVEY_ITEM_TYPE, MpInstructionSurveyItem.class)
                    .build();

    @Override
    public Class<? extends SurveyItem> getCustomClass(String customType, JsonElement json) {
        if (customType != null && TYPE_TO_CLASS.containsKey(customType)) {
            return TYPE_TO_CLASS.get(customType);
        } else {
            return super.getCustomClass(customType, json);
        }
    }
}
