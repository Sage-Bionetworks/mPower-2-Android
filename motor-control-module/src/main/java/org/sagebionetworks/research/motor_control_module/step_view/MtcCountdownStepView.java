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

package org.sagebionetworks.research.motor_control_module.step_view;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.sagebionetworks.research.domain.step.interfaces.CountdownStep;
import org.sagebionetworks.research.domain.step.interfaces.Step;
import org.sagebionetworks.research.motor_control_module.step.AppStepType;
import org.sagebionetworks.research.presentation.DisplayString;
import org.sagebionetworks.research.presentation.mapper.DrawableMapper;
import org.sagebionetworks.research.presentation.model.ColorThemeView;
import org.sagebionetworks.research.presentation.model.ImageThemeView;
import org.sagebionetworks.research.presentation.model.action.ActionView;
import org.sagebionetworks.research.presentation.model.implementations.ActiveUIStepViewBase;
import org.sagebionetworks.research.presentation.model.interfaces.CountdownStepView;
import org.threeten.bp.Duration;

import java.util.Map;

public class MtcCountdownStepView extends ActiveUIStepViewBase implements CountdownStepView {
    public static final String TYPE = AppStepType.MTC_COUNTDOWN;

    public MtcCountdownStepView(@NonNull String identifier,
            @NonNull ImmutableMap<String, ActionView> actions,
            @Nullable DisplayString title, @Nullable DisplayString text,
            @Nullable DisplayString detail, @Nullable DisplayString footnote,
            @Nullable ColorThemeView colorTheme, @Nullable ImageThemeView imageTheme,
            @NonNull Duration duration, @NonNull final Map<String, String> spokenInstructions,
            @NonNull final ImmutableSet<String> commands, boolean isBackgroundAudioRequired) {
        super(identifier, actions, title, text, detail, footnote,
                colorTheme, imageTheme, duration, spokenInstructions, commands, isBackgroundAudioRequired);
    }

    public static MtcCountdownStepView fromMtcCountdownStep(Step step, DrawableMapper mapper) {
        if (!(step instanceof CountdownStep)) {
            throw new IllegalArgumentException("Provided step: " + step + " is not a CountdownStep");
        }

        ActiveUIStepViewBase activeStep = ActiveUIStepViewBase.fromActiveUIStep(step, mapper);
        return new MtcCountdownStepView(activeStep.getIdentifier(),
                activeStep.getActions(), activeStep.getTitle(), activeStep.getText(),
                activeStep.getDetail(), activeStep.getFootnote(), activeStep.getColorTheme(),
                activeStep.getImageTheme(), activeStep.getDuration(), activeStep.getSpokenInstructions(),
                activeStep.getCommands(), activeStep.isBackgroundAudioRequired());
    }

    @NonNull
    @Override
    public String getType() {
        return TYPE;
    }
}
