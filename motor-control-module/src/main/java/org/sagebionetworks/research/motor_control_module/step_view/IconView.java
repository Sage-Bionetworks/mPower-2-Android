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
import com.google.auto.value.AutoValue;
import org.sagebionetworks.research.motor_control_module.step.Icon;
import org.sagebionetworks.research.presentation.DisplayDrawable;
import org.sagebionetworks.research.presentation.DisplayString;
import org.sagebionetworks.research.presentation.mapper.DrawableMapper;

import java.io.Serializable;

@AutoValue
public abstract class IconView implements Serializable {
    @AutoValue.Builder
    public abstract static class Builder {
        public abstract IconView build();

        @Nullable
        public abstract Builder setIcon(@Nullable DisplayDrawable icon);

        @Nullable
        public abstract Builder setTitle(@Nullable DisplayString title);
    }

    public static Builder builder() {
        return new AutoValue_IconView.Builder();
    }

    @NonNull
    public static IconView fromIcon(@NonNull Icon icon, DrawableMapper mapper) {
        String title = icon.getTitle();
        DisplayDrawable iconDrawable = DisplayDrawable.create(null, mapper.getDrawableFromName(
                icon.getIcon()));
        if (title != null) {
            return IconView.builder()
                    .setIcon(iconDrawable)
                    .setTitle(DisplayString.create(null, title))
                    .build();
        } else {
            return null;
        }
    }

    public abstract DisplayDrawable getIcon();

    public abstract DisplayString getTitle();
}
