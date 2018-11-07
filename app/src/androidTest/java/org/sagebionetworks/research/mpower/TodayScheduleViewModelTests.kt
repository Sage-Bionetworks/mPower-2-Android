package org.sagebionetworks.research.mpower

import android.app.Application
import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.support.test.InstrumentationRegistry
import android.support.test.filters.MediumTest
import android.support.test.runner.AndroidJUnit4
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.sagebionetworks.bridge.android.manager.BridgeManagerProvider
import org.sagebionetworks.research.mpower.viewmodel.ItemType.ACTIVITIES
import org.sagebionetworks.research.mpower.viewmodel.ItemType.MEDICATION
import org.sagebionetworks.research.mpower.viewmodel.ItemType.SYMPTOMS
import org.sagebionetworks.research.mpower.viewmodel.ItemType.TRIGGERS
import org.sagebionetworks.research.mpower.viewmodel.TodayScheduleViewModel
import org.sagebionetworks.research.mpower.viewmodel.find
import org.sagebionetworks.research.sageresearch.dao.room.ScheduledActivityEntityDao
import org.sagebionetworks.research.sageresearch.dao.room.ScheduleRepository
import org.sagebionetworks.research.sageresearch.dao.room.ScheduledRepositorySyncStateDao
import org.sagebionetworks.research.sageresearch.viewmodel.RoomTestHelper
import org.sagebionetworks.research.sageresearch.viewmodel.TestResourceHelper
import org.threeten.bp.Instant
import org.threeten.bp.ZonedDateTime

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
@MediumTest
class TodayScheduleViewModelTests: RoomTestHelper() {

    companion object {
        lateinit var application: Application
        val historyList = "test_today_history_schedules.json"
        val testResourceMap = TestResourceHelper.testResourceMap(setOf(historyList))

        @BeforeClass @JvmStatic
        fun setup() {
            RoomTestHelper.setup()
            application = InstrumentationRegistry.getTargetContext().applicationContext as Application
        }
    }

    @Rule
    @JvmField
    var instantExecutor = InstantTaskExecutorRule()
    val scheduleDao = database.scheduleDao()
    val scheduleRepo = ScheduleRepository(scheduleDao,
            ScheduledRepositorySyncStateDao(
                    InstrumentationRegistry.getTargetContext()),
            BridgeManagerProvider.getInstance().surveyManager,
            BridgeManagerProvider.getInstance().activityManager,
            BridgeManagerProvider.getInstance().participantManager,
            BridgeManagerProvider.getInstance().authenticationManager,
            BridgeManagerProvider.getInstance().uploadManager,
            BridgeManagerProvider.getInstance().bridgeConfig)

    @Before
    fun setupForEachTest() {
        activityDao.clear()
        activityDao.upsert(testResourceMap[historyList] ?: listOf())
    }

    @Test
    fun consolidate_noneFinished() {
        val viewModel = MockTodayScheduleViewModel(scheduleDao, scheduleRepo,
                ZonedDateTime.parse("2018-08-18T00:00:00.000Z").toInstant(),
                ZonedDateTime.parse("2018-08-19T00:00:00.000Z").toInstant())
        val historyItems = getValue(viewModel.liveData())
        assertEquals(0, historyItems.size)
    }

    @Test
    fun consolidate_allFinished() {
        val viewModel = MockTodayScheduleViewModel(scheduleDao, scheduleRepo,
                ZonedDateTime.parse("2018-08-17T00:00:00.000Z").toInstant(),
                ZonedDateTime.parse("2018-08-20T00:00:00.000Z").toInstant())
        val historyItems = getValue(viewModel.liveData())
        assertEquals(4, historyItems.size)

        val triggers = historyItems.find(TRIGGERS)
        assertNotNull(triggers)
        assertEquals(2, triggers?.count)
        assertEquals(2, triggers?.schedules?.count())
        assertEquals("273c4518-7cb6-4496-b1dd-c0b5bf291b21:2018-08-17T00:00:00.000-04:00",
                triggers?.schedules?.get(0)?.guid)
        assertEquals("273c4518-7cb6-4496-b1dd-c0b5bf291b21:2018-08-19T00:00:00.000-04:00",
                triggers?.schedules?.get(1)?.guid)
        assertNotNull(historyItems.find(SYMPTOMS))

        val symptoms = historyItems.find(SYMPTOMS)
        assertNotNull(symptoms)
        assertEquals(1, symptoms?.count)
        assertEquals(1, symptoms?.schedules?.count())
        assertEquals("273c4518-7cb6-4496-b1dd-c0b5bf291b20:2018-08-17T00:00:00.000-04:00",
                symptoms?.schedules?.get(0)?.guid)

        assertNotNull(historyItems.find(MEDICATION))
        val medication = historyItems.find(MEDICATION)
        assertNotNull(medication)
        assertEquals(1, medication?.count)
        assertEquals(1, medication?.schedules?.count())
        assertEquals("273c4518-7cb6-4496-b1dd-c0b5bf291b09:2018-08-17T00:00:00.000",
                medication?.schedules?.get(0)?.guid)

        val activities = historyItems.find(ACTIVITIES)
        assertNotNull(activities)
        assertEquals(2, activities?.count)
        assertEquals(2, activities?.schedules?.count())
        assertEquals("273c4518-7cb6-4496-b1dd-c0b5bf291b22:2018-08-17T00:00:00.000",
                activities?.schedules?.get(0)?.guid)
        assertEquals("a341c893-615d-48e1-ab6a-d418af720269:2018-08-17T00:00:00.000-04:00",
                activities?.schedules?.get(1)?.guid)
    }

    class MockTodayScheduleViewModel(
            private val scheduleDao: ScheduledActivityEntityDao,
            scheduleRepository: ScheduleRepository,
            start: Instant,
            end: Instant): TodayScheduleViewModel(scheduleDao,scheduleRepository) {

        override val queryDateStart = start
        override val queryDateEnd = end
    }
}