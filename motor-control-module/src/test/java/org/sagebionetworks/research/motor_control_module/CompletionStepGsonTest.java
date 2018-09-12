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

package org.sagebionetworks.research.motor_control_module;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.research.domain.step.gson.UIStepGsonTest.UI_STEP_ASSERT_EQUALS;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.junit.Test;
import org.sagebionetworks.research.domain.step.gson.IndividualStepGsonTest;
import org.sagebionetworks.research.domain.step.interfaces.Step;
import org.sagebionetworks.research.domain.step.interfaces.UIStep;
import org.sagebionetworks.research.domain.step.ui.action.Action;
import org.sagebionetworks.research.motor_control_module.step.CompletionStep;

public class CompletionStepGsonTest extends IndividualStepGsonTest {
    @Test
    public void testExample_1() {
        CompletionStep expected = mock(CompletionStep.class);
        when(expected.getIdentifier()).thenReturn("testUIStep1");
        when(expected.getType()).thenReturn("completion");
        when(expected.getActions()).thenReturn(ImmutableMap.of());
        when(expected.getHiddenActions()).thenReturn(ImmutableSet.of());
        when(expected.getTitle()).thenReturn("title");
        when(expected.getText()).thenReturn("text");
        when(expected.getDetail()).thenReturn(null);
        when(expected.getFootnote()).thenReturn(null);

        Step rawStep = readStep("CompletionStep_1.json");
        assertTrue(rawStep instanceof UIStep);

        UIStep result = (UIStep) rawStep;
        UI_STEP_ASSERT_EQUALS.accept(expected, result);
    }

    @Test
    public void testExample_2() {
        Action goForward = mock(Action.class);
        when(goForward.getType()).thenReturn("default");
        when(goForward.getButtonTitle()).thenReturn("Go, Dogs!");

        CompletionStep expected = mock(CompletionStep.class);
        when(expected.getIdentifier()).thenReturn("testUIStep2");
        when(expected.getType()).thenReturn("completion");
        when(expected.getActions()).thenReturn(ImmutableMap.<String, Action>builder()
                .put("goForward", goForward).build());
        when(expected.getHiddenActions()).thenReturn(ImmutableSet.of());
        when(expected.getTitle()).thenReturn("title");
        when(expected.getText()).thenReturn("text");
        when(expected.getDetail()).thenReturn("detail");
        when(expected.getFootnote()).thenReturn("footnote");

        Step rawStep = readStep("CompletionStep_2.json");
        assertTrue(rawStep instanceof UIStep);

        UIStep result = (UIStep) rawStep;
        UI_STEP_ASSERT_EQUALS.accept(expected, result);
    }
}
