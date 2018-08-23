package org.sagebionetworks.research.mpower.room

import org.joda.time.DateTime
import org.sagebionetworks.bridge.android.BridgeConfig
import org.sagebionetworks.bridge.researchstack.BridgeDataProvider

//
//  Copyright © 2016-2018 Sage Bionetworks. All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification,
// are permitted provided that the following conditions are met:
//
// 1.  Redistributions of source code must retain the above copyright notice, this
// list of conditions and the following disclaimer.
//
// 2.  Redistributions in binary form must reproduce the above copyright notice,
// this list of conditions and the following disclaimer in the documentation and/or
// other materials provided with the distribution.
//
// 3.  Neither the name of the copyright holder(s) nor the names of any contributors
// may be used to endorse or promote products derived from this software without
// specific prior written permission. No license is granted to the trademarks of
// the copyright holders even if such marks are included in this software.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//

/**
 * Default data source handler for scheduled activities. This manager is used to get `ScheduledActivityEntity`
 * objects and upload the task results for Bridge services. By default, this manager will fetch all the
 * activities, but will *not* cache them all in memory. Instead, it will filter out those activities that are
 * valid for today and the most recent finished activity (if any) for each activity identifier.
 */
open class ScheduleManager {
    /**
     * @property configuration Pointer to the shared configuration to use.
     */
    val configuration: BridgeConfig = BridgeDataProvider.getInstance().bridgeConfig

    /**
     * @property now This is an internal value that can be used in testing instead of using `DateTime.now()` directly.
     * It can then be overridden by a test subclass of this manager in order to return a known date.
     */
    open val now: DateTime get() = DateTime.now()

    /**
     * @property identifier that can be used for mapping this schedule manager to the displayed schedules.
     */
    open var identifier: String = "Today"

    /**
     * @property scheduledActivities This is an array of the activities fetched by the the server or database
     * after being filtered to ignore unwanted activities
     */
    open var scheduledActivities: Array<ScheduledActivityEntity> = arrayOf()

    init {

    }

    /**
     * Load the scheduled activities from cache using the `fetchRequests()` for this schedule manager.
     */
    fun loadScheduledActivities() {

    }
}