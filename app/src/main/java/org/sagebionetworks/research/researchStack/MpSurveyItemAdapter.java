package org.sagebionetworks.research.researchStack;

import android.support.annotation.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import org.researchstack.backbone.model.survey.*;
import org.sagebionetworks.bridge.researchstack.task.creation.BridgeSurveyItemAdapter;
import org.sagebionetworks.research.mpower.step.MpInstructionSurveyItem;

import java.util.Map;

public class MpSurveyItemAdapter extends BridgeSurveyItemAdapter {

    // Custom Survey Items
    public static final String MP_INSTRUCTION_SURVEY_ITEM_TYPE          = "mpInstruction";
    public static final String MP_INSTRUCTION_PHONE_SURVEY_ITEM_TYPE    = "mpPhoneInstruction";

    @VisibleForTesting
    static final Map<String, Class<? extends SurveyItem>> TYPE_TO_CLASS =
            ImmutableMap.<String, Class<? extends org.researchstack.backbone.model.survey.SurveyItem>>builder()
                    // Consent Mappings


                    // Form Mappings


                    // Custom Mappings
                    .put(MP_INSTRUCTION_SURVEY_ITEM_TYPE,           MpInstructionSurveyItem.class)
                    .put(MP_INSTRUCTION_PHONE_SURVEY_ITEM_TYPE,     MpInstructionSurveyItem.class)

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
