package org.sagebionetworks.research.mpower

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import junit.framework.Assert.*
import org.joda.time.DateTime
import org.junit.Test
import org.junit.runner.RunWith
import org.researchstack.backbone.result.TaskResult
import org.sagebionetworks.bridge.rest.exceptions.EntityNotFoundException
import org.sagebionetworks.bridge.rest.model.Message
import org.sagebionetworks.bridge.rest.model.ScheduledActivity
import org.sagebionetworks.research.mpower.ScheduleRepositoryTests.MockScheduleRepository.Companion.participantCreatedOn
import org.sagebionetworks.research.mpower.ScheduleRepositoryTests.MockScheduleRepository.Companion.syncDateFirst
import org.sagebionetworks.research.sageresearch.dao.room.ScheduledActivityEntity
import org.sagebionetworks.research.sageresearch.dao.room.ScheduledActivityEntityDao
import org.sagebionetworks.research.sageresearch.viewmodel.ScheduleRepository
import org.sagebionetworks.research.sageresearch.viewmodel.ScheduleRepositoryHelper
import rx.functions.Action1

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

@RunWith(AndroidJUnit4::class)
// ran into multi-dex issues moving this to a library project, leaving it here for now
class ScheduleRepositoryTests: RoomTestHelper() {

    // No need to test any value but 14, because that is currently a bridge limitation
    val maxRequestDays = 14

    companion object {
        val activityList = "test_scheduled_activities.json"
        val testResourceMap = TestResourceHelper.testResourceMap(setOf(activityList))
    }

    @Test
    fun query_needsSyncedWithBridge() {
        // TODO: mdephillips 9/24/18 do tests
    }

    @Test
    fun syncStartDate_states() {
        val repo = MockScheduleRepository(InstrumentationRegistry.getTargetContext(), activityDao)
        assertNotNull(repo.syncStartDate)
        assertEquals(participantCreatedOn.withTimeAtStartOfDay(), repo.syncStartDate)
        repo.lastQueryEndDate = syncDateFirst
        assertEquals(syncDateFirst.withTimeAtStartOfDay(), repo.syncStartDate)
    }

    @Test
    fun scheduleUpdateFailed_noInternet() {
        val activities = testResourceMap[activityList] ?: listOf()
        activityDao.clear()
        activityDao.upsert(activities)
        val schedule1 = activities.firstOrNull()
        assertNotNull(schedule1)
        assertTrue(schedule1?.needsSyncedToBridge == null || schedule1.needsSyncedToBridge == false)
        val repo = MockScheduleRepository(InstrumentationRegistry.getTargetContext(), activityDao)
        val uuid = repo.createScheduleTaskRunUuid(schedule1!!)
        repo.throwableOnUpdate = Throwable("Unable to resolve host " +
                "\"webservices.sagebase.org\", no address associated with hostname")
        repo.updateSchedule(TaskResult("id"), uuid)
        val newSchedule1 = activityDao.activity(schedule1.guid)
        assertEquals(1, newSchedule1.size)
        assertTrue(newSchedule1.first().needsSyncedToBridge ?: false)
    }

    @Test
    fun scheduleUpdateFailed_accountNotFoundError() {
        val activities = testResourceMap[activityList] ?: listOf()
        activityDao.clear()
        activityDao.upsert(activities)
        val schedule1 = activities.firstOrNull()
        assertNotNull(schedule1)
        assertTrue(schedule1?.needsSyncedToBridge == null || schedule1.needsSyncedToBridge == false)
        val repo = MockScheduleRepository(InstrumentationRegistry.getTargetContext(), activityDao)
        val uuid = repo.createScheduleTaskRunUuid(schedule1!!)
        repo.throwableOnUpdate = EntityNotFoundException("Account not found.", "webservices.sagebase.org")
        repo.updateSchedule(TaskResult("id"), uuid)
        // See BridgeExtensions.isUnrecoverableAccountNotFoundError for logic
        // on why we don't try to re-upload account not found schedule update failures
        val newSchedule1 = activityDao.activity(schedule1.guid)
        assertEquals(1, newSchedule1.size)
        assertFalse(newSchedule1.first().needsSyncedToBridge ?: true)
    }

    @Test
    fun scheduleUpdateFailed_clientDataTooLarge() {
        val activities = testResourceMap[activityList] ?: listOf()
        activityDao.clear()
        activityDao.upsert(activities)
        val schedule1 = activities.firstOrNull()
        assertNotNull(schedule1)
        assertNull(schedule1?.needsSyncedToBridge)
        val repo = MockScheduleRepository(InstrumentationRegistry.getTargetContext(), activityDao)
        val uuid = repo.createScheduleTaskRunUuid(schedule1!!)
        repo.throwableOnUpdate = Throwable("Client data too large, please consider a smaller payload")
        repo.updateSchedule(TaskResult("id"), uuid)
        // See BridgeExtensions.isUnrecoverableClientDataTooLargeError for logic
        // on why we don't try to re-upload client data too large schedule update failures
        val newSchedule1 = activityDao.activity(schedule1.guid)
        assertEquals(1, newSchedule1.size)
        assertFalse(newSchedule1.first().needsSyncedToBridge ?: true)
    }

    @Test
    fun requestMap_LessThan14Days() {
        val start = DateTime.parse("2018-08-17T00:00:00.000-04:00")
        val end = DateTime.parse("2018-08-27T00:00:00.000-04:00")
        val requestMap = ScheduleRepositoryHelper.buildRequestMap(start, end, maxRequestDays)
        assertEquals(1, requestMap.keys.size)
        assertEquals(DateTime.parse("2018-08-17T00:00:00.000-04:00"), requestMap.keys.elementAt(0))
        assertEquals(DateTime.parse("2018-08-27T23:59:59.999-04:00"), requestMap[requestMap.keys.elementAt(0)])
    }

    @Test
    fun requestMap_MoreThan14DaysEven() {
        val start = DateTime.parse("2018-08-18T12:00:00.000-04:00")
        val end = DateTime.parse("2018-09-14T00:12:00.000-04:00")
        val requestMap = ScheduleRepositoryHelper.buildRequestMap(start, end, maxRequestDays)
        assertEquals(2, requestMap.keys.size)
        assertEquals(DateTime.parse("2018-09-01T00:00:00.000-04:00"), requestMap.keys.elementAt(0))
        assertEquals(DateTime.parse("2018-09-14T23:59:59.999-04:00"), requestMap[requestMap.keys.elementAt(0)])
        assertEquals(DateTime.parse("2018-08-18T00:00:00.000-04:00"), requestMap.keys.elementAt(1))
        assertEquals(DateTime.parse("2018-08-31T23:59:59.999-04:00"), requestMap[requestMap.keys.elementAt(1)])
    }

    @Test
    fun requestMap_MoreThan14DaysRemainder() {
        val start = DateTime.parse("2018-08-17T10:00:00.000-04:00")
        val end = DateTime.parse("2018-09-09T10:00:00.000-04:00")
        val requestMap = ScheduleRepositoryHelper.buildRequestMap(start, end, maxRequestDays)
        assertEquals(2, requestMap.keys.size)
        assertEquals(DateTime.parse("2018-08-27T00:00:00.000-04:00"), requestMap.keys.elementAt(0))
        assertEquals(DateTime.parse("2018-09-09T23:59:59.999-04:00"), requestMap[requestMap.keys.elementAt(0)])
        assertEquals(DateTime.parse("2018-08-17T00:00:00.000-04:00"), requestMap.keys.elementAt(1))
        assertEquals(DateTime.parse("2018-08-26T23:59:59.999-04:00"), requestMap[requestMap.keys.elementAt(1)])
    }

    class MockScheduleRepository(context: Context, scheduleDao: ScheduledActivityEntityDao): ScheduleRepository(context) {

        companion object {
            val participantCreatedOn = DateTime.parse("2018-08-10T10:00:00.000-04:00")
            val syncDateFirst = DateTime.parse("2018-08-24T10:00:00.000-04:00")
        }

        val dao: ScheduledActivityEntityDao = scheduleDao
        var throwableOnUpdate: Throwable? = null

        private var lastQueryEndDateVal: DateTime? = null
        override public var lastQueryEndDate: DateTime?
            get() { return lastQueryEndDateVal }
            set (value) { lastQueryEndDateVal = value }

        override fun now(): DateTime {
            return DateTime.now()
        }

        override fun studyStartDate(): DateTime? {
            return participantCreatedOn
        }

        override fun cacheSchedules(schedules: List<ScheduledActivityEntity>) {
            dao.upsert(schedules)
        }

        override fun updateActivityOnBridge(bridgeSchedule: ScheduledActivity, onNext: Action1<Message>, onError: Action1<Throwable>) {
            throwableOnUpdate?.let {
                onError.call(it)
            } ?: run {
                onNext.call(Message())
            }
        }

        override fun findSchedule(scheduleGuid: String, onNext: Action1<ScheduledActivityEntity>, onError: Action1<Throwable>) {
            dao.activity(scheduleGuid).firstOrNull()?.let {
                onNext.call(it)
            } ?: run {
                onError.call(Throwable("No schedule found in DB with guid $scheduleGuid"))
            }
        }
    }
}