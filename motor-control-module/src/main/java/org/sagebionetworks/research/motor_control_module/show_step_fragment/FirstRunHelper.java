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

package org.sagebionetworks.research.motor_control_module.show_step_fragment;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import org.sagebionetworks.research.domain.result.interfaces.AnswerResult;
import org.sagebionetworks.research.domain.result.interfaces.Result;
import org.sagebionetworks.research.domain.result.interfaces.TaskResult;
import org.sagebionetworks.research.presentation.perform_task.PerformTaskViewModel;
import org.threeten.bp.Instant;
import org.threeten.bp.ZonedDateTime;

public class FirstRunHelper {
    // private constructor to prevent instantiation
    private FirstRunHelper() {}

    public static ZonedDateTime getLastRunDate(@NonNull TaskResult taskResult) {
        for (Result result : taskResult.getAsyncResults()) {
            if (result.getIdentifier().equals(PerformTaskViewModel.LAST_RUN_RESULT_ID) &&
                    result instanceof AnswerResult) {
                Object answer = ((AnswerResult) result).getAnswer();
                if (answer instanceof ZonedDateTime) {
                    return (ZonedDateTime) answer;
                }
            }
        }

        return null;
    }

    public static boolean isFirstRun(@Nullable TaskResult result) {
        if (result != null && result.getResult(ShowOverviewStepFragment.INFO_TAPPED_RESULT_ID) == null) {
            ZonedDateTime lastRunDate = getLastRunDate(result);
            if (lastRunDate != null) {
                Instant taskStartInstant = result.getStartTime();
                ZonedDateTime taskStartDate = ZonedDateTime.ofInstant(taskStartInstant, lastRunDate.getZone());
                // This is a first run if it has been at least a month since the last run.
                return lastRunDate.isBefore(taskStartDate.minusMonths(1));
            }
        }

        // The info button was tapped, or The task result or last run date was null making this a first run.
        return true;
    }
}
