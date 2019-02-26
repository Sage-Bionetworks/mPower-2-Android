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

package org.sagebionetworks.research.motor_control_module.result;

import static org.sagebionetworks.research.motor_control_module.show_step_fragment.hand_selection.HandSelection.LEFT;
import static org.sagebionetworks.research.motor_control_module.show_step_fragment.hand_selection.HandSelection.RIGHT;

import android.support.annotation.NonNull;

import com.google.common.collect.ImmutableList;

import org.sagebionetworks.research.domain.result.AnswerResultType;
import org.sagebionetworks.research.domain.result.implementations.AnswerResultBase;
import org.sagebionetworks.research.motor_control_module.show_step_fragment.hand_selection.HandSelection;
import org.threeten.bp.Instant;

public class HandSelectionResult extends AnswerResultBase<String> {

    @NonNull
    private final ImmutableList<String> handOrder;
    /**
     * @return the random hand order when both is the answer, it will be ["left", "right"] or ["right", "left"]
     *         when only left is selected, it will just be ["left"], or ["right"],
     */
    public ImmutableList<String> getHandOrder() {
        return handOrder;
    }

    public HandSelectionResult(@NonNull final String identifier, @NonNull final Instant startTime,
            @NonNull final Instant endTime, final @HandSelection String handSelection) {
        super(identifier, startTime, endTime, handSelection, AnswerResultType.STRING);
        if (HandSelection.BOTH.equals(handSelection)) {
            // If the user selected both we randomize the hand order.
            if (Math.random() < .5) {
                handOrder = ImmutableList.of(LEFT, RIGHT);
            } else {
                handOrder = ImmutableList.of(RIGHT, LEFT);
            }
        } else {
            handOrder = ImmutableList.of(handSelection);
        }
    }
}
