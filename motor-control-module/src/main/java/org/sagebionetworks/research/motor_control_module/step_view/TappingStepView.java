package org.sagebionetworks.research.motor_control_module.step_view;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.sagebionetworks.research.domain.step.interfaces.Step;
import org.sagebionetworks.research.motor_control_module.show_step_fragment.tapping.TappingButtonIdentifier;
import org.sagebionetworks.research.motor_control_module.step.AppStepType;
import org.sagebionetworks.research.motor_control_module.step.MPowerActiveUIStep;
import org.sagebionetworks.research.motor_control_module.step.TappingStep;
import org.sagebionetworks.research.presentation.DisplayString;
import org.sagebionetworks.research.presentation.mapper.DrawableMapper;
import org.sagebionetworks.research.presentation.model.ColorThemeView;
import org.sagebionetworks.research.presentation.model.ImageThemeView;
import org.sagebionetworks.research.presentation.model.action.ActionView;
import org.sagebionetworks.research.presentation.model.implementations.ActiveUIStepViewBase;
import org.sagebionetworks.research.presentation.model.interfaces.ActiveUIStepView;
import org.threeten.bp.Duration;

import java.util.Map;

public class TappingStepView extends MPowerActiveUIStepView {
    public static final String TYPE = AppStepType.TAPPING;

    public TappingStepView(@NonNull String identifier, @NonNull ImmutableMap<String, ActionView> actions,
                           @Nullable DisplayString title, @Nullable DisplayString text, @Nullable DisplayString detail,
                           @Nullable DisplayString footnote, @Nullable ColorThemeView colorTheme, @Nullable ImageThemeView imageTheme,
                           @NonNull Duration duration, @NonNull final Map<String, String> spokenInstructions,
                           @NonNull final ImmutableSet<String> commands, boolean isBackgroundAudioRequired) {
        super(identifier, actions, title, text, detail, footnote, colorTheme, imageTheme,
                duration, spokenInstructions, commands, isBackgroundAudioRequired);
    }

    @NonNull
    public static TappingStepView fromTappingStep(@NonNull Step step, @NonNull DrawableMapper mapper) {
        if (!(step instanceof TappingStep)) {
            throw new IllegalArgumentException("Provided Step: " + step + " is not a TappingStep.");
        }

        ActiveUIStepView stepView = ActiveUIStepViewBase.fromActiveUIStep(step, mapper);
        return new TappingStepView(stepView.getIdentifier(), stepView.getActions(),
                stepView.getTitle(), stepView.getText(), stepView.getDetail(), stepView.getFootnote(),
                stepView.getColorTheme(), stepView.getImageTheme(), stepView.getDuration(),
                stepView.getSpokenInstructions(), stepView.getCommands(), stepView.isBackgroundAudioRequired());
    }

    @Override
    @NonNull
    public String getType() {
        return TYPE;
    }
}
