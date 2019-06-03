package org.sagebionetworks.research.motor_control_module.step_view;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.sagebionetworks.research.domain.step.interfaces.Step;
import org.sagebionetworks.research.motor_control_module.step.AppStepType;
import org.sagebionetworks.research.motor_control_module.step.HandStepHelper;
import org.sagebionetworks.research.motor_control_module.step.MPowerActiveUIStep;
import org.sagebionetworks.research.presentation.DisplayString;
import org.sagebionetworks.research.presentation.mapper.DrawableMapper;
import org.sagebionetworks.research.presentation.model.ColorThemeView;
import org.sagebionetworks.research.presentation.model.ImageThemeView;
import org.sagebionetworks.research.presentation.model.action.ActionView;
import org.sagebionetworks.research.presentation.model.implementations.ActiveUIStepViewBase;
import org.sagebionetworks.research.presentation.model.interfaces.ActiveUIStepView;
import org.threeten.bp.Duration;

import java.util.HashMap;
import java.util.Map;

import static org.sagebionetworks.research.motor_control_module.step.HandStepHelper.JSON_PLACEHOLDER;

public class MPowerActiveUIStepView extends ActiveUIStepViewBase {
    public static final String TYPE = AppStepType.MPOWER_ACTIVE;

    public MPowerActiveUIStepView(@NonNull String identifier, @NonNull ImmutableMap<String, ActionView> actions,
                                  @Nullable DisplayString title, @Nullable DisplayString text, @Nullable DisplayString detail,
                                  @Nullable DisplayString footnote, @Nullable ColorThemeView colorTheme, @Nullable ImageThemeView imageTheme,
                                  @NonNull Duration duration, @NonNull final Map<String, String> spokenInstructions,
                                  @NonNull final ImmutableSet<String> commands, boolean isBackgroundAudioRequired) {
        super(identifier, actions, title, text, detail, footnote, colorTheme, imageTheme,
                duration, spokenInstructions, commands, isBackgroundAudioRequired);
    }

    public static MPowerActiveUIStepView fromMPowerActiveUIStep(Step step, DrawableMapper mapper) {
        if (!(step instanceof MPowerActiveUIStep)) {
            throw new IllegalArgumentException("Provided step: " + step + " is not an MPowerAcitveUIStep");
        }

        ActiveUIStepView activeUIStepView = ActiveUIStepViewBase.fromActiveUIStep(step, mapper);
        return new MPowerActiveUIStepView(activeUIStepView.getIdentifier(),
                activeUIStepView.getActions(), activeUIStepView.getTitle(), activeUIStepView.getText(),
                activeUIStepView.getDetail(), activeUIStepView.getFootnote(), activeUIStepView.getColorTheme(),
                activeUIStepView.getImageTheme(), activeUIStepView.getDuration(),
                activeUIStepView.getSpokenInstructions(), activeUIStepView.getCommands(),
                activeUIStepView.isBackgroundAudioRequired());
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Map<String, String> getSpokenInstructions() {
        Map<String, String> formatted = new HashMap<>();
        for (Map.Entry<String, String> entry: super.getSpokenInstructions().entrySet()) {
            String entryValue = entry.getValue();
            HandStepHelper.Hand hand = HandStepHelper.whichHand(getIdentifier());
            if (hand != null) {
                entryValue = entryValue.replaceAll(JSON_PLACEHOLDER, hand.toString());
            }
            formatted.put(entry.getKey(), entryValue);
        }
        return formatted;
    }

}
