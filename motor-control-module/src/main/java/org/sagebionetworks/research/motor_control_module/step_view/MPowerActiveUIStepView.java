package org.sagebionetworks.research.motor_control_module.step_view;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

import org.sagebionetworks.research.domain.step.interfaces.Step;
import org.sagebionetworks.research.motor_control_module.step.AppStepType;
import org.sagebionetworks.research.motor_control_module.step.MPowerActiveUIStep;
import org.sagebionetworks.research.presentation.DisplayString;
import org.sagebionetworks.research.presentation.mapper.DrawableMapper;
import org.sagebionetworks.research.presentation.model.ColorThemeView;
import org.sagebionetworks.research.presentation.model.ImageThemeView;
import org.sagebionetworks.research.presentation.model.action.ActionView;
import org.sagebionetworks.research.presentation.model.implementations.ActiveUIStepViewBase;
import org.sagebionetworks.research.presentation.model.interfaces.ActiveUIStepView;
import org.threeten.bp.Duration;

import java.util.Map;

public class MPowerActiveUIStepView extends ActiveUIStepViewBase {
    public static final String TYPE = AppStepType.MPOWER_ACTIVE;

    public MPowerActiveUIStepView(@NonNull String identifier, int navDirection,
            @NonNull ImmutableMap<String, ActionView> actions, @Nullable DisplayString title,
            @Nullable DisplayString text, @Nullable DisplayString detail, @Nullable DisplayString footnote,
            @Nullable ColorThemeView colorTheme, @Nullable ImageThemeView imageTheme, @NonNull Duration duration,
            @NonNull final Map<String, String> spokenInstructions, boolean isBackgroundAudioRequired) {
        super(identifier, navDirection, actions, title, text, detail, footnote, colorTheme, imageTheme,
                duration, spokenInstructions, isBackgroundAudioRequired);
    }

    public static MPowerActiveUIStepView fromMPowerActiveUIStep(Step step, DrawableMapper mapper) {
        if (!(step instanceof MPowerActiveUIStep)) {
            throw new IllegalArgumentException("Provided step: " + step + " is not an MPowerAcitveUIStep");
        }

        ActiveUIStepView activeUIStepView = ActiveUIStepViewBase.fromActiveUIStep(step, mapper);
        return new MPowerActiveUIStepView(activeUIStepView.getIdentifier(), activeUIStepView.getNavDirection(),
                activeUIStepView.getActions(), activeUIStepView.getTitle(), activeUIStepView.getText(),
                activeUIStepView.getDetail(), activeUIStepView.getFootnote(), activeUIStepView.getColorTheme(),
                activeUIStepView.getImageTheme(), activeUIStepView.getDuration(),
                activeUIStepView.getSpokenInstructions(), activeUIStepView.isBackgroundAudioRequired());
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
