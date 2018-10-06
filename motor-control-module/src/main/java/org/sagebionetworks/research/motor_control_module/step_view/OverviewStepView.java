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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.sagebionetworks.research.domain.step.StepType;
import org.sagebionetworks.research.domain.step.interfaces.Step;
import org.sagebionetworks.research.motor_control_module.step.Icon;
import org.sagebionetworks.research.motor_control_module.step.OverviewStep;
import org.sagebionetworks.research.presentation.DisplayString;
import org.sagebionetworks.research.presentation.mapper.DrawableMapper;
import org.sagebionetworks.research.presentation.model.ColorThemeView;
import org.sagebionetworks.research.presentation.model.ImageThemeView;
import org.sagebionetworks.research.presentation.model.action.ActionView;
import org.sagebionetworks.research.presentation.model.implementations.UIStepViewBase;
import org.sagebionetworks.research.presentation.model.interfaces.UIStepView;

import java.util.ArrayList;
import java.util.List;

public class OverviewStepView extends UIStepViewBase {
    public static final String TYPE = StepType.OVERVIEW;

    private final ImmutableList<IconView> iconViews;

    @NonNull
    public static OverviewStepView fromOverviewStep(@NonNull Step step, DrawableMapper mapper) {
        if (!(step instanceof OverviewStep)) {
            throw new IllegalArgumentException("Provided step: " + step + " is not an OverviewStep");
        }

        UIStepView uiStepView = UIStepViewBase.fromUIStep(step, mapper);
        OverviewStep overviewStep = (OverviewStep) step;
        List<IconView> iconViews = new ArrayList<>();
        for (Icon icon : overviewStep.getIcons()) {
            iconViews.add(IconView.fromIcon(icon, mapper));
        }

        return new OverviewStepView(uiStepView.getIdentifier(), uiStepView.getNavDirection(), uiStepView.getActions(),
                uiStepView.getTitle(), uiStepView.getText(), uiStepView.getDetail(), uiStepView.getFootnote(),
                uiStepView.getColorTheme(), uiStepView.getImageTheme(), iconViews);
    }

    public OverviewStepView(@NonNull final String identifier, final int navDirection,
            @NonNull final ImmutableMap<String, ActionView> actions,
            @Nullable final DisplayString title,
            @Nullable final DisplayString text,
            @Nullable final DisplayString detail,
            @Nullable final DisplayString footnote,
            @Nullable final ColorThemeView colorTheme,
            @Nullable final ImageThemeView imageTheme,
            @NonNull final List<IconView> iconViews) {
        super(identifier, navDirection, actions, title, text, detail, footnote, colorTheme, imageTheme);
        this.iconViews = ImmutableList.copyOf(iconViews);
    }

    @NonNull
    @StepType
    public String getType() {
        return TYPE;
    }

    @NonNull
    public ImmutableList<IconView> getIconViews() {
        return this.iconViews;
    }
}
