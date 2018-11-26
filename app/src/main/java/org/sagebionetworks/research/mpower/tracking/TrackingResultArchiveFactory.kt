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

package org.sagebionetworks.research.mpower.tracking

import com.google.common.collect.ImmutableSet
import org.joda.time.DateTime
import org.sagebionetworks.bridge.data.JsonArchiveFile
import org.sagebionetworks.research.domain.result.interfaces.Result
import org.sagebionetworks.research.mpower.tracking.view_model.TrackingTaskViewModel
import org.sagebionetworks.research.mpower.tracking.view_model.logs.LoggingCollection
import org.sagebionetworks.research.sageresearch.dao.room.EntityTypeConverters
import org.sagebionetworks.research.sageresearch_app_sdk.archive.AbstractResultArchiveFactory
import javax.inject.Inject

/**
 * TrackingResultArchiveFactory controls the
 */
class TrackingResultArchiveFactory @Inject constructor(): AbstractResultArchiveFactory.ResultArchiveFactory {
    override fun isSupported(result: Result): Boolean {
        return result is LoggingCollection<*>
    }

    override fun toArchiveFiles(result: Result): ImmutableSet<JsonArchiveFile> {
        (result as? LoggingCollection<*>)?.let {
            val filename = TrackingTaskViewModel.LOGGING_COLLECTION_IDENTIFIER
            val startDateTime = DateTime(it.startTime.toEpochMilli())
            val json = EntityTypeConverters().bridgeGson.toJson(it)
            return ImmutableSet.of(JsonArchiveFile(filename, startDateTime, json))
        }
        return ImmutableSet.of()
    }
}