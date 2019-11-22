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

package org.sagebionetworks.research.mpower.tracking.view_model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.sagebionetworks.research.mpower.tracking.fragment.TrackingFragment;
import org.sagebionetworks.research.mpower.tracking.fragment.TriggersLoggingFragment;
import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;
import org.sagebionetworks.research.mpower.tracking.model.TrackingStepView;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.SimpleTrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.LoggingCollection;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SimpleTrackingItemLog;

/**
 * Subclass of TrackingTaskViewModel which uses LogTypes specific to the Symptoms task.
 */
public class TriggersTrackingTaskViewModel extends TrackingTaskViewModel<SimpleTrackingItemConfig, SimpleTrackingItemLog> {

    protected TriggersTrackingTaskViewModel(
            @NonNull final TrackingStepView stepView,
            @Nullable final LoggingCollection<SimpleTrackingItemLog> previousLoggingCollection) {
        super(stepView, previousLoggingCollection);
    }

    @Override
    protected LoggingCollection<SimpleTrackingItemLog> instantiateLoggingCollection() {
        return LoggingCollection.<SimpleTrackingItemLog>builder()
                .setIdentifier(TrackingTaskViewModel.LOGGING_COLLECTION_IDENTIFIER)
                .build();
    }

    @Override
    protected SimpleTrackingItemLog instantiateLogForUnloggedItem(@NonNull final SimpleTrackingItemConfig config) {
        return SimpleTrackingItemLog.builder()
                .setIdentifier(config.getIdentifier())
                .setText(config.getIdentifier())
                .build();
    }

    @Override
    protected SimpleTrackingItemConfig instantiateConfigFromSelection(@NonNull final TrackingItem item) {
        return SimpleTrackingItemConfig.builder().setIdentifier(item.getIdentifier()).setTrackingItem(item).build();
    }

    @Override
    protected void proceedToInitialFragmentOnSecondRun(TrackingFragment trackingFragment) {
        trackingFragment.replaceWithFragment(TriggersLoggingFragment.newInstance(this.stepView));
    }
}
