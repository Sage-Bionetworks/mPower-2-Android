package org.sagebionetworks.research.motor_control_module.step_view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

import org.sagebionetworks.research.domain.step.interfaces.Step;
import org.sagebionetworks.research.motor_control_module.step.AppStepType;
import org.sagebionetworks.research.motor_control_module.step.TappingCompletionStep;
import org.sagebionetworks.research.presentation.DisplayString;
import org.sagebionetworks.research.presentation.mapper.DrawableMapper;
import org.sagebionetworks.research.presentation.model.ColorThemeView;
import org.sagebionetworks.research.presentation.model.ImageThemeView;
import org.sagebionetworks.research.presentation.model.action.ActionView;
import org.sagebionetworks.research.presentation.model.implementations.UIStepViewBase;
import org.sagebionetworks.research.presentation.model.interfaces.UIStepView;

public class TappingCompletionStepView extends CompletionStepView {
    public static final String TYPE = AppStepType.TAPPING_COMPLETION;

    public TappingCompletionStepView(@NonNull String identifier, int navDirection, @NonNull ImmutableMap<String, ActionView> actions,
                                     @Nullable DisplayString title, @Nullable DisplayString text, @Nullable DisplayString detail,
                                     @Nullable DisplayString footnote, @Nullable ColorThemeView colorTheme,
                                     @Nullable ImageThemeView imageTheme) {
        super(identifier, navDirection, actions, title, text, detail, footnote, colorTheme, imageTheme);
    }

    @NonNull
    public static TappingCompletionStepView fromTappingCompletionStep(@NonNull Step step, @NonNull DrawableMapper mapper) {
        if (!(step instanceof TappingCompletionStep)) {
            throw new IllegalArgumentException("Provided step: " + step + " is not a TappingCompletionStep.");
        }

        UIStepView stepView = UIStepViewBase.fromUIStep(step, mapper);
        return new TappingCompletionStepView(stepView.getIdentifier(), stepView.getNavDirection(),
                stepView.getActions(), stepView.getTitle(), stepView.getText(), stepView.getDetail(),
                stepView.getFootnote(), stepView.getColorTheme(), stepView.getImageTheme());
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
