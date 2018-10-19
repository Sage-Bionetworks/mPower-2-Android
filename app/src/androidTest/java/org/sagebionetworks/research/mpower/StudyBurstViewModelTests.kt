package org.sagebionetworks.research.mpower

import android.app.Application
import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.support.test.InstrumentationRegistry
import android.support.test.filters.MediumTest
import android.support.test.runner.AndroidJUnit4
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue

import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.sagebionetworks.bridge.android.manager.BridgeManagerProvider
import org.sagebionetworks.bridge.rest.RestUtils
import org.sagebionetworks.bridge.rest.model.StudyParticipant
import org.sagebionetworks.research.mpower.research.CompletionTask
import org.sagebionetworks.research.mpower.research.DataSourceManager
import org.sagebionetworks.research.mpower.research.MpIdentifier.BACKGROUND
import org.sagebionetworks.research.mpower.research.MpIdentifier.DEMOGRAPHICS
import org.sagebionetworks.research.mpower.research.MpIdentifier.ENGAGEMENT
import org.sagebionetworks.research.mpower.research.MpIdentifier.MEASURING
import org.sagebionetworks.research.mpower.research.MpIdentifier.MOTIVATION
import org.sagebionetworks.research.mpower.research.MpIdentifier.STUDY_BURST_COMPLETED
import org.sagebionetworks.research.mpower.research.MpIdentifier.STUDY_BURST_REMINDER
import org.sagebionetworks.research.mpower.research.MpIdentifier.TAPPING
import org.sagebionetworks.research.mpower.research.MpIdentifier.TREMOR
import org.sagebionetworks.research.mpower.research.MpIdentifier.WALK_AND_BALANCE
import org.sagebionetworks.research.mpower.research.StudyBurstConfiguration
import org.sagebionetworks.research.mpower.viewmodel.StudyBurstSettingsDao
import org.sagebionetworks.research.mpower.viewmodel.StudyBurstViewModel
import org.sagebionetworks.research.sageresearch.dao.room.EntityTypeConverters
import org.sagebionetworks.research.sageresearch.dao.room.ScheduledActivityEntity
import org.sagebionetworks.research.sageresearch.dao.room.ScheduledActivityEntityDao
import org.sagebionetworks.research.sageresearch.extensions.filterByActivityId
import org.sagebionetworks.research.sageresearch.extensions.startOfDay
import org.sagebionetworks.research.sageresearch.manager.ActivityGroup
import org.sagebionetworks.research.sageresearch.viewmodel.ScheduleRepository
import org.sagebionetworks.research.sageresearch.viewmodel.ScheduledRepositorySyncStateDao
import org.sagebionetworks.research.sageresearch_app_sdk.TaskResultUploader
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter

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
class StudyBurstViewModelTests: RoomTestHelper() {
    companion object {
        lateinit var application: Application
        val studyBurstList = "test_study_burst_schedules.json"
        val testResourceMap = TestResourceHelper.testResourceMap(setOf(studyBurstList))
        val gson = Gson()

        @BeforeClass @JvmStatic
        fun setup() {
            RoomTestHelper.setup()
            application = InstrumentationRegistry.getTargetContext().applicationContext as Application
        }
    }

    @Rule
    @JvmField
    var instantExecutor = InstantTaskExecutorRule()

    val studyBurstSettingsDao = StudyBurstSettingsDao(InstrumentationRegistry.getTargetContext())
    val scheduleDao = database.scheduleDao()
    val scheduleRepo = ScheduleRepository(scheduleDao,
            ScheduledRepositorySyncStateDao(InstrumentationRegistry.getTargetContext()),
            BridgeManagerProvider.getInstance().surveyManager,
            BridgeManagerProvider.getInstance().activityManager,
            BridgeManagerProvider.getInstance().participantManager)

    @Before
    fun setupForEachTest() {
        activityDao.clear()
        activityDao.upsert(testResourceMap[studyBurstList] ?: listOf())
    }

    @Test
    fun testStudyBurstConfig_Serialization() {
        val studyConfigPreSerialization = StudyBurstConfiguration(
            STUDY_BURST_COMPLETED,
                20,
                14,
                19,
                (60 * 75 * 1000),
                MEASURING,
                MOTIVATION,
                setOf(CompletionTask(linkedSetOf(STUDY_BURST_REMINDER, DEMOGRAPHICS), 2),
                        CompletionTask(linkedSetOf(BACKGROUND), 10),
                        CompletionTask(linkedSetOf(ENGAGEMENT), 13)),
                setOf(
                        setOf("gr_SC_DB", "gr_SC_CS"),
                        setOf("gr_BR_AD", "gr_BR_II")))
        val jsonStr = gson.toJson(studyConfigPreSerialization)
        assertNotNull(jsonStr)
        val studyConfig = gson.fromJson(jsonStr, StudyBurstConfiguration::class.java)
        assertNotNull(studyConfig)
        assertEquals(STUDY_BURST_COMPLETED, studyConfig.identifier)
        assertEquals(20, studyConfig.numberOfDays)
        assertEquals(14, studyConfig.minimumRequiredDays)
        assertEquals(19, studyConfig.maxDayCount)
        assertEquals((60 * 75 * 1000), studyConfig.expiresLimit)
        assertEquals(MEASURING, studyConfig.taskGroupIdentifier)
        assertEquals(MOTIVATION, studyConfig.motivationIdentifier)
        assertNotNull(studyConfig.completionTasks)
        assertEquals(3, studyConfig.completionTasks.size)
        assertEquals(setOf(STUDY_BURST_REMINDER, DEMOGRAPHICS), studyConfig.completionTasks.elementAt(0).activityIdentifiers)
        assertEquals(2, studyConfig.completionTasks.elementAt(0).day)
        assertEquals(setOf(BACKGROUND), studyConfig.completionTasks.elementAt(1).activityIdentifiers)
        assertEquals(10, studyConfig.completionTasks.elementAt(1).day)
        assertEquals(setOf(ENGAGEMENT), studyConfig.completionTasks.elementAt(2).activityIdentifiers)
        assertEquals(13, studyConfig.completionTasks.elementAt(2).day)
        assertNotNull(studyConfig.engagementGroups)
        assertEquals(2, studyConfig.engagementGroups?.size)
        assertEquals(setOf("gr_SC_DB", "gr_SC_CS"), studyConfig.engagementGroups?.elementAt(0))
        assertEquals(setOf("gr_BR_AD", "gr_BR_II"), studyConfig.engagementGroups?.elementAt(1))
    }

    @Test
    fun testStudyBurstConfig_serverDeserialization() {
        val jsonStr = "{\n" +
                "            \"identifier\": \"foo\",\n" +
                "            \"type\": \"studyBurst\",\n" +
                "            \"numberOfDays\": 12,\n" +
                "            \"minimumRequiredDays\": 10,\n" +
                "            \"expiresLimit\": 120,\n" +
                "            \"taskGroupIdentifier\": \"GroupTwo\",\n" +
                "            \"motivationIdentifier\": \"Motivation\",\n" +
                "            \"completionTasks\": [\n" +
                "                { \"day\": 0, \"firstOnly\": true, \"activityIdentifiers\" : [\"boo\", \"goo\"] },\n" +
                "                { \"day\": 12, \"firstOnly\": true, \"activityIdentifiers\" : [\"coo\"] }\n" +
                "            ]\n" +
                "        }"

        val studyConfig = gson.fromJson(jsonStr, StudyBurstConfiguration::class.java)
        assertNotNull(studyConfig)
        assertEquals("foo", studyConfig.identifier)
        assertEquals(12, studyConfig.numberOfDays)
        assertEquals(120, studyConfig.expiresLimit)
        assertEquals(10, studyConfig.minimumRequiredDays)
        assertEquals("GroupTwo", studyConfig.taskGroupIdentifier)
        assertEquals(2, studyConfig.completionTasks.size)
    }

    @Test
    fun testStudyBurst_Day1_StartState() {
        val viewModel = MockStudyBurstViewModel(scheduleDao, scheduleRepo, studyBurstSettingsDao,
                StudySetup.day1_startupState)
        val item = getValue(viewModel.liveData())
        assertNotNull(item)
        assertNotNull(item.activityGroup)
        assertEquals(1, item.dayCount)
        assertTrue(item.hasStudyBurst)
        assertEquals(0, item.finishedSchedules.size)
        assertFalse(item.isCompletedForToday)
        assertFalse(item.isLastDay)
        assertEquals(1, item.calculateThisDay())
        assertEquals(0, item.pastCompletionTasks.size)
        assertEquals(0, item.pastCompletionSurveys.size)
        assertNotNull(item.todayCompletionTask)

        val demographics = item.schedules.filterByActivityId(DEMOGRAPHICS)
        assertEquals(1, demographics.size)
        val studyBurstReminder = item.schedules.filterByActivityId(STUDY_BURST_REMINDER)
        assertEquals(1, studyBurstReminder.size)

        assertFalse(item.hasCompletedMotivationSurvey())
        val completionTask = item.nextCompletionActivityToShow
        assertNotNull(completionTask)
        assertEquals(STUDY_BURST_REMINDER, completionTask?.activityIdentifier())

        assertNotNull(item.getActionBarItem(application))
        assertNotNull("Study Burst", item.getActionBarItem(application)?.title)
        assertNotNull("3 activities to do", item.getActionBarItem(application)?.detail)
        assertNull(item.getUnfinishedSchedule())
    }

    @Test
    fun testStudyBurstComplete_Day1() {
        val viewModel = MockStudyBurstViewModel(scheduleDao, scheduleRepo, studyBurstSettingsDao,
                StudySetup.day1_tasksFinished_surveysNotFinished)
        val item = getValue(viewModel.liveData())
        assertNotNull(item)
        assertNotNull(item.activityGroup)
        assertEquals(1, item.dayCount)
        assertTrue(item.hasStudyBurst)
        assertEquals(3, item.finishedSchedules.size)
        assertTrue(item.isCompletedForToday)
        assertFalse(item.isLastDay)
        assertEquals(1, item.calculateThisDay())
        assertEquals(0, item.pastCompletionTasks.size)
        assertEquals(0, item.pastCompletionSurveys.size)
        assertNotNull(item.todayCompletionTask)

        val demographics = item.schedules.filterByActivityId(DEMOGRAPHICS)
        assertEquals(1, demographics.size)

        assertTrue(item.hasCompletedMotivationSurvey())
        val completionTask = item.nextCompletionActivityToShow
        assertNotNull(completionTask)
        assertEquals(STUDY_BURST_REMINDER, completionTask?.activityIdentifier())

        assertNotNull(item.getActionBarItem(application))
        assertNotNull(item.getUnfinishedSchedule())
    }

    @Test
    fun testStudyBurstComplete_Day1_SurveysFinished() {
        val viewModel = MockStudyBurstViewModel(scheduleDao, scheduleRepo, studyBurstSettingsDao,
                StudySetup.day1_tasksFinished_surveysFinished)
        val item = getValue(viewModel.liveData())
        assertNotNull(item)
        assertNotNull(item.activityGroup)
        assertEquals(1, item.dayCount)
        assertTrue(item.hasStudyBurst)
        assertEquals(3, item.finishedSchedules.size)
        assertTrue(item.isCompletedForToday)
        assertFalse(item.isLastDay)
        assertEquals(1, item.calculateThisDay())
        assertEquals(0, item.pastCompletionTasks.size)
        assertEquals(0, item.pastCompletionSurveys.size)
        assertNotNull(item.todayCompletionTask)

        val demographics = item.schedules.filterByActivityId(DEMOGRAPHICS)
        assertEquals(1, demographics.size)

        assertTrue(item.hasCompletedMotivationSurvey())
        val completionTask = item.nextCompletionActivityToShow
        assertNull(completionTask)

        assertNull(item.getActionBarItem(application))
        assertNull(item.getUnfinishedSchedule())
    }

    @Test
    fun testStudyBurstComplete_Day2_Day1SurveysNotFinished() {
        val viewModel = MockStudyBurstViewModel(scheduleDao, scheduleRepo, studyBurstSettingsDao,
                StudySetup.day2_surveysNotFinished)
        val item = getValue(viewModel.liveData())
        assertNotNull(item)
        assertNotNull(item.activityGroup)
        assertEquals(2, item.dayCount)
        assertTrue(item.hasStudyBurst)
        assertEquals(0, item.finishedSchedules.size)
        assertFalse(item.isCompletedForToday)
        assertFalse(item.isLastDay)
        assertEquals(2, item.calculateThisDay())
        assertEquals(1, item.pastCompletionTasks.size)
        assertEquals(2, item.pastCompletionSurveys.size)
        assertNull(item.todayCompletionTask)

        val demographics = item.schedules.filterByActivityId(DEMOGRAPHICS)
        assertEquals(1, demographics.size)

        assertFalse(item.hasCompletedMotivationSurvey())
        val completionTask = item.nextCompletionActivityToShow
        assertNotNull(completionTask)
        assertEquals(STUDY_BURST_REMINDER, completionTask?.activityIdentifier())

        assertNotNull(item.getActionBarItem(application))
        assertNotNull(item.getUnfinishedSchedule())
    }

    @Test
    fun testStudyBurstComplete_Day15_Missing1() {
        val viewModel = MockStudyBurstViewModel(scheduleDao, scheduleRepo, studyBurstSettingsDao,
                StudySetup.day15_missing1_engagementNotFinished)
        val item = getValue(viewModel.liveData())
        assertNotNull(item)
        assertNotNull(item.activityGroup)
        assertNull(item.dayCount)
        assertFalse(item.hasStudyBurst)
        assertTrue(item.isCompletedForToday)

        val completionTask = item.nextCompletionActivityToShow
        assertNotNull(completionTask)
        assertEquals(ENGAGEMENT, completionTask?.activityIdentifier())

        assertNotNull(item.getActionBarItem(application))
        assertEquals("Engagement Survey", item.getActionBarItem(application)?.title)
        assertEquals("6 Minutes", item.getActionBarItem(application)?.detail)
        assertNotNull(item.getUnfinishedSchedule())
    }

    @Test
    fun testStudyBurstComplete_Day14_Missing1() {
        val viewModel = MockStudyBurstViewModel(scheduleDao, scheduleRepo, studyBurstSettingsDao,
                StudySetup.day14_missing1_tasksFinished_engagementNotFinished)
        val item = getValue(viewModel.liveData())
        assertNotNull(item)
        assertNotNull(item.activityGroup)
        assertEquals(14, item.dayCount)
        assertTrue(item.hasStudyBurst)
        assertEquals(3, item.finishedSchedules.size)
        assertTrue(item.isCompletedForToday)
        assertTrue(item.isLastDay)

        val completionTask = item.nextCompletionActivityToShow
        assertNotNull(completionTask)
        assertEquals(ENGAGEMENT, completionTask?.activityIdentifier())

        assertNotNull(item.getActionBarItem(application))
        assertEquals("Engagement Survey", item.getActionBarItem(application)?.title)
        assertEquals("6 Minutes", item.getActionBarItem(application)?.detail)
        assertNotNull(item.getUnfinishedSchedule())
    }

    @Test
    fun testStudyBurstComplete_Day14_Missing6() {
        val viewModel = MockStudyBurstViewModel(scheduleDao, scheduleRepo, studyBurstSettingsDao,
                StudySetup.day14_missing6_tasksFinished_engagementNotFinished)
        val item = getValue(viewModel.liveData())
        assertNotNull(item)
        assertNotNull(item.activityGroup)
        assertEquals(14, item.dayCount)
        assertTrue(item.hasStudyBurst)
        assertEquals(3, item.finishedSchedules.size)
        assertTrue(item.isCompletedForToday)
        assertFalse(item.isLastDay)

        val completionTask = item.nextCompletionActivityToShow
        assertNull(completionTask)
    }

    @Test
    fun testStudyBurstComplete_Day9_TasksFinished() {
        val viewModel = MockStudyBurstViewModel(scheduleDao, scheduleRepo, studyBurstSettingsDao,
                StudySetup.day9_tasksFinished)
        val item = getValue(viewModel.liveData())
        assertNotNull(item)
        assertNotNull(item.activityGroup)
        assertEquals(9, item.dayCount)
        assertTrue(item.hasStudyBurst)
        assertEquals(3, item.finishedSchedules.size)
        assertTrue(item.isCompletedForToday)
        assertFalse(item.isLastDay)
        assertNotNull(item.todayCompletionTask)

        val completionTask = item.nextCompletionActivityToShow
        assertNotNull(completionTask)
        assertEquals(BACKGROUND, completionTask?.activityIdentifier())

        assertNotNull(item.getActionBarItem(application))
        assertEquals("Background", item.getActionBarItem(application)?.title)
        assertEquals("4 Minutes", item.getActionBarItem(application)?.detail)
        assertNotNull(item.getUnfinishedSchedule())
    }

    @Test
    fun testStudyBurstComplete_Day21_Missing6() {
        val viewModel = MockStudyBurstViewModel(scheduleDao, scheduleRepo, studyBurstSettingsDao,
                StudySetup.day21_missing6_engagementNotFinished)
        val item = getValue(viewModel.liveData())
        assertNotNull(item)
        assertNotNull(item.activityGroup)
        assertNull(item.dayCount)
        assertFalse(item.hasStudyBurst)
        assertTrue(item.isCompletedForToday)

        val completionTask = item.nextCompletionActivityToShow
        assertNotNull(completionTask)
        assertEquals(ENGAGEMENT, completionTask?.activityIdentifier())

        assertNotNull(item.getActionBarItem(application))
        assertEquals("Engagement Survey", item.getActionBarItem(application)?.title)
        assertEquals("6 Minutes", item.getActionBarItem(application)?.detail)
        assertNotNull(item.getUnfinishedSchedule())
    }

    @Test
    fun testStudyBurstComplete_Day15_BurstComplete_EngagementNotComplete() {
        val viewModel = MockStudyBurstViewModel(scheduleDao, scheduleRepo, studyBurstSettingsDao,
                StudySetup.day15_burstCompleted_engagementNotFinished)
        val item = getValue(viewModel.liveData())
        assertNotNull(item)
        assertNotNull(item.activityGroup)
        assertNull(item.dayCount)
        assertFalse(item.hasStudyBurst)
        assertTrue(item.isCompletedForToday)
        assertFalse(item.isLastDay)

        val completionTask = item.nextCompletionActivityToShow
        assertNotNull(completionTask)
        assertEquals(ENGAGEMENT, completionTask?.activityIdentifier())

        assertNotNull(item.getActionBarItem(application))
        assertEquals("Engagement Survey", item.getActionBarItem(application)?.title)
        assertEquals("6 Minutes", item.getActionBarItem(application)?.detail)
        assertNotNull(item.getUnfinishedSchedule())
    }

    @Test
    fun testStudyBurstComplete_Day15_BurstComplete_EngagementComplete() {
        val viewModel = MockStudyBurstViewModel(scheduleDao, scheduleRepo, studyBurstSettingsDao,
                StudySetup.day15_burstCompleted_engagementFinished)
        val item = getValue(viewModel.liveData())
        assertNotNull(item)
        assertNotNull(item.activityGroup)
        assertNull(item.dayCount)
        assertFalse(item.hasStudyBurst)
        assertTrue(item.isCompletedForToday)
        assertFalse(item.isLastDay)

        assertNull(item.nextCompletionActivityToShow)
        assertNull(item.getActionBarItem(application))
        assertNull(item.getUnfinishedSchedule())
    }

    @Test
    fun testStudyBurstComplete_Day1_AllFinished_2HoursAgo() {
        val viewModel = MockStudyBurstViewModel(scheduleDao, scheduleRepo, studyBurstSettingsDao,
                StudySetup.day1_allFinished_2HoursAgo)
        val item = getValue(viewModel.liveData())
        assertNotNull(item)
        assertNotNull(item.activityGroup)
        assertEquals(1, item.dayCount)
        assertTrue(item.hasStudyBurst)
        assertEquals(3, item.finishedSchedules.size)
        assertTrue(item.isCompletedForToday)
        assertFalse(item.isLastDay)
        assertEquals(1, item.calculateThisDay())
        assertEquals(0, item.pastCompletionTasks.size)
        assertEquals(0, item.pastCompletionSurveys.size)
        assertNotNull(item.todayCompletionTask)

        assertTrue(item.hasCompletedMotivationSurvey())
        assertNull(item.nextCompletionActivityToShow)

        assertNull(item.getActionBarItem(application))
        assertNull(item.getUnfinishedSchedule())
    }

    // TODO: mdephillips 9/11/18 add reminder tests

    data class StudySetup(
            /// The schedules that will be used as schedules to create the study burst schedule data
            val templateSchedules: List<ScheduledActivityEntity> = listOf(),
            /// First name of the participant.
            val firstName: String = "Rumplestiltskin",
            /// Study Burst "day" where Day 0 is the day the participant was "created".
            val studyBurstDay: Int = 3,
            /// The days in the past when the particpant finished all the tasks.
            val studyBurstFinishedOnDays: Set<Int> = setOf(0, 2),
            /// The days when the study burst was finished.
            val studyBurstSurveyFinishedOnDays: Map<String, Int> = mapOf(),
            /// A list of the tasks to mark as finished today for a study burst. If included, this will be used to
            /// define the order of the tasks for display in the study burst view.
            var finishedTodayTasks: LinkedHashSet<String> = linkedSetOf(TAPPING, WALK_AND_BALANCE),
            /// The time in seconds to use as the time until today's finished tasks will expire. Default = 15 min.
            val timeUntilExpires: Long = 15 * 60,
            /// The data groups to set for this participant.
            var dataGroups: Set<String> = setOf("gr_SC_DB","gr_BR_II","gr_ST_T","gr_DT_T"),
            /// The time to set as "now".
            val now: LocalDateTime = LocalDateTime.parse("2018-09-11T12:00:00"),
            /// The sudy burst reminders
            var reminderTime: String? = null,
            /// Study burst configuration
            val config: StudyBurstConfiguration = StudyBurstConfiguration(),
            /// The study burst view model
            val viewModel: MockStudyBurstViewModel? = null,
            /// The Timezone to use
            val timezone: ZoneId = ZoneId.systemDefault()) {

        companion object {

            fun finishedOnDays(studyBurstDay: Int, missingCount: Int): Set<Int> {
                if (studyBurstDay == missingCount) {
                    return setOf()
                }
                var finishedDays = (0 until studyBurstDay).toMutableSet()
                if (missingCount > 0) {
                    val offset = finishedDays.size / (missingCount + 1)
                    for (ii in 0 until missingCount) {
                        finishedDays.remove(finishedDays.elementAt(offset * (ii + 1)))
                    }
                }
                return finishedDays
            }

            fun previousFinishedSurveys(studyBurstDay: Int): Map<String, Int> {
                var surveyMap: MutableMap<String, Int> = mutableMapOf()
                val config = StudyBurstConfiguration()
                config.completionTasks.forEach {
                    val day = it.day
                    if (studyBurstDay >= day) {
                        it.activityIdentifiers.forEach {
                            surveyMap[it] = maxOf(0, day - 1)
                        }
                    }
                }
                return surveyMap
            }

            val gson = EntityTypeConverters().bridgeGson
            val studyBurstList = "test_study_burst_schedules.json"
            val templateSchedules = TestResourceHelper.testResource(studyBurstList) 

            val day1_startupState =
                    StudySetup(templateSchedules = templateSchedules,
                            studyBurstDay = 0,
                            studyBurstFinishedOnDays = setOf(),
                            studyBurstSurveyFinishedOnDays = mapOf(),
                            finishedTodayTasks = linkedSetOf())

            val day1_startupState_BRII_DTT =
                    StudySetup(templateSchedules = templateSchedules,
                            studyBurstDay = 0,
                            studyBurstFinishedOnDays = setOf(),
                            studyBurstSurveyFinishedOnDays = mapOf(),
                            finishedTodayTasks = linkedSetOf(),
                            timeUntilExpires = 0,
                            dataGroups = setOf("gr_SC_DB","gr_BR_II","gr_ST_T","gr_DT_T"))

            val day1_startupState_BRII_DTF =
                    StudySetup(templateSchedules = templateSchedules,
                            studyBurstDay = 0,
                            studyBurstFinishedOnDays = setOf(),
                            studyBurstSurveyFinishedOnDays = mapOf(),
                            finishedTodayTasks = linkedSetOf(),
                            timeUntilExpires = 0,
                            dataGroups = setOf("gr_SC_DB","gr_BR_II","gr_ST_T","gr_DT_F"))

            val day1_startupState_BRAD_DTT =
                    StudySetup(templateSchedules = templateSchedules,
                            studyBurstDay = 0,
                            studyBurstFinishedOnDays = setOf(),
                            studyBurstSurveyFinishedOnDays = mapOf(),
                            finishedTodayTasks = linkedSetOf(),
                            timeUntilExpires = 0,
                            dataGroups = setOf("gr_SC_DB","gr_BR_AD","gr_ST_T","gr_DT_T"))

            val day1_startupState_BRAD_DTF =
                    StudySetup(templateSchedules = templateSchedules,
                            studyBurstDay = 0,
                            studyBurstFinishedOnDays = setOf(),
                            studyBurstSurveyFinishedOnDays = mapOf(),
                            finishedTodayTasks = linkedSetOf(),
                            timeUntilExpires = 0,
                            dataGroups = setOf("gr_SC_DB","gr_BR_AD","gr_ST_T","gr_DT_F"))

            val day1_noTasksFinished =
                    StudySetup(templateSchedules = templateSchedules,
                            studyBurstDay = 0,
                            studyBurstFinishedOnDays = setOf(),
                            studyBurstSurveyFinishedOnDays = previousFinishedSurveys(0),
                            finishedTodayTasks = linkedSetOf())

            val day1_twoTasksFinished =
                    StudySetup(templateSchedules = templateSchedules,
                            studyBurstDay = 0,
                            studyBurstFinishedOnDays = setOf(),
                            studyBurstSurveyFinishedOnDays = previousFinishedSurveys(0),
                            finishedTodayTasks = linkedSetOf(TAPPING, WALK_AND_BALANCE))

            val day1_tasksFinished_surveysNotFinished =
                    StudySetup(templateSchedules = templateSchedules,
                            studyBurstDay = 0,
                            studyBurstFinishedOnDays = setOf(),
                            studyBurstSurveyFinishedOnDays = previousFinishedSurveys(0),
                            finishedTodayTasks = linkedSetOf(TAPPING, TREMOR, WALK_AND_BALANCE))

            val day1_tasksFinished_surveysNotFinished_BRII_DTT =
                    StudySetup(templateSchedules = templateSchedules,
                            studyBurstDay = 0,
                            studyBurstFinishedOnDays = setOf(),
                            studyBurstSurveyFinishedOnDays = previousFinishedSurveys(0),
                            finishedTodayTasks = linkedSetOf(),
                            timeUntilExpires = 15,
                            dataGroups = setOf("gr_SC_DB","gr_BR_II","gr_ST_T","gr_DT_T"))

            val day1_tasksFinished_surveysNotFinished_BRII_DTF =
                    StudySetup(templateSchedules = templateSchedules,
                            studyBurstDay = 0,
                            studyBurstFinishedOnDays = setOf(),
                            studyBurstSurveyFinishedOnDays = previousFinishedSurveys(0),
                            finishedTodayTasks = linkedSetOf(),
                            timeUntilExpires = 15,
                            dataGroups = setOf("gr_SC_DB","gr_BR_II","gr_ST_T","gr_DT_F"))

            val day1_tasksFinished_surveysNotFinished_BRAD_DTT =
                    StudySetup(templateSchedules = templateSchedules,
                            studyBurstDay = 0,
                            studyBurstFinishedOnDays = setOf(),
                            studyBurstSurveyFinishedOnDays = previousFinishedSurveys(0),
                            finishedTodayTasks = linkedSetOf(),
                            timeUntilExpires = 15,
                            dataGroups = setOf("gr_SC_DB","gr_BR_AD","gr_ST_T","gr_DT_T"))

            val day1_tasksFinished_surveysNotFinished_BRAD_DTF =
                    StudySetup(templateSchedules = templateSchedules,
                            studyBurstDay = 0,
                            studyBurstFinishedOnDays = setOf(),
                            studyBurstSurveyFinishedOnDays = previousFinishedSurveys(0),
                            finishedTodayTasks = linkedSetOf(),
                            timeUntilExpires = 15,
                            dataGroups = setOf("gr_SC_DB","gr_BR_AD","gr_ST_T","gr_DT_F"))

            val day1_tasksFinished_surveysFinished =
                    StudySetup(templateSchedules = templateSchedules,
                            studyBurstDay = 0,
                            studyBurstFinishedOnDays = setOf(0),
                            studyBurstSurveyFinishedOnDays = previousFinishedSurveys(1),
                            finishedTodayTasks = linkedSetOf(TAPPING, TREMOR, WALK_AND_BALANCE))

            val day1_allFinished_2HoursAgo =
                    StudySetup(templateSchedules = templateSchedules,
                            studyBurstDay = 0,
                            studyBurstFinishedOnDays = setOf(0),
                            studyBurstSurveyFinishedOnDays = previousFinishedSurveys(1),
                            finishedTodayTasks = linkedSetOf(TAPPING, TREMOR, WALK_AND_BALANCE),
                            timeUntilExpires = (-2 * 60 * 60))

            val day2_surveysNotFinished =
                    StudySetup(templateSchedules = templateSchedules,
                            studyBurstDay = 1,
                            studyBurstFinishedOnDays = setOf(0),
                            studyBurstSurveyFinishedOnDays = previousFinishedSurveys(0),
                            finishedTodayTasks = linkedSetOf())

            val day2_tasksNotFinished_surveysFinished =
                    StudySetup(templateSchedules = templateSchedules,
                            studyBurstDay = 1,
                            studyBurstFinishedOnDays = setOf(0),
                            studyBurstSurveyFinishedOnDays = previousFinishedSurveys(1),
                            finishedTodayTasks = linkedSetOf())

            val day9_twoTasksFinished =
                    StudySetup(templateSchedules = templateSchedules,
                            studyBurstDay = 8,
                            studyBurstFinishedOnDays = finishedOnDays(7, 0),
                            studyBurstSurveyFinishedOnDays = previousFinishedSurveys(7),
                            finishedTodayTasks = linkedSetOf(TAPPING, WALK_AND_BALANCE))

            val day9_tasksFinished =
                    StudySetup(templateSchedules = templateSchedules,
                            studyBurstDay = 8,
                            studyBurstFinishedOnDays = finishedOnDays(7, 0),
                            studyBurstSurveyFinishedOnDays = previousFinishedSurveys(7),
                            finishedTodayTasks = linkedSetOf(TAPPING, TREMOR, WALK_AND_BALANCE))

            val day11_tasksFinished_noMissingDays =
                    StudySetup(templateSchedules = templateSchedules,
                            studyBurstDay = 10,
                            studyBurstFinishedOnDays = finishedOnDays(10, 0),
                            studyBurstSurveyFinishedOnDays = previousFinishedSurveys(10),
                            finishedTodayTasks = linkedSetOf(TAPPING, TREMOR, WALK_AND_BALANCE))


            val day14_missing1_tasksFinished_engagementNotFinished =
                    StudySetup(templateSchedules = templateSchedules,
                            studyBurstDay = 13,
                            studyBurstFinishedOnDays = finishedOnDays(13, 1),
                            studyBurstSurveyFinishedOnDays = previousFinishedSurveys(12),
                            finishedTodayTasks = linkedSetOf(TAPPING, TREMOR, WALK_AND_BALANCE))

            val day14_missing6_tasksFinished_engagementNotFinished =
                    StudySetup(templateSchedules = templateSchedules,
                            studyBurstDay = 13,
                            studyBurstFinishedOnDays = finishedOnDays(13, 6),
                            studyBurstSurveyFinishedOnDays = previousFinishedSurveys(12),
                            finishedTodayTasks = linkedSetOf(TAPPING, TREMOR, WALK_AND_BALANCE))

            val day14_tasksFinished_engagementNotFinished =
                    StudySetup(templateSchedules = templateSchedules,
                            studyBurstDay = 13,
                            studyBurstFinishedOnDays = finishedOnDays(13, 0),
                            studyBurstSurveyFinishedOnDays = previousFinishedSurveys(12),
                            finishedTodayTasks = linkedSetOf(TAPPING, TREMOR, WALK_AND_BALANCE))

            val day15_missing1_engagementNotFinished =
                    StudySetup(templateSchedules = templateSchedules,
                            studyBurstDay = 14,
                            studyBurstFinishedOnDays = finishedOnDays(14, 1),
                            studyBurstSurveyFinishedOnDays = previousFinishedSurveys(12),
                            finishedTodayTasks = linkedSetOf())

            val day15_burstCompleted_engagementNotFinished =
                    StudySetup(templateSchedules = templateSchedules,
                            studyBurstDay = 14,
                            studyBurstFinishedOnDays = finishedOnDays(14, 0),
                            studyBurstSurveyFinishedOnDays = previousFinishedSurveys(12),
                            finishedTodayTasks = linkedSetOf())

            val day15_burstCompleted_engagementFinished =
                    StudySetup(templateSchedules = templateSchedules,
                            studyBurstDay = 14,
                            studyBurstFinishedOnDays = finishedOnDays(14, 0),
                            studyBurstSurveyFinishedOnDays = previousFinishedSurveys(14),
                            finishedTodayTasks = linkedSetOf())

            val day21_missing6_engagementNotFinished =
                    StudySetup(templateSchedules = templateSchedules,
                            studyBurstDay = 20,
                            studyBurstFinishedOnDays = finishedOnDays(18, 6),
                            studyBurstSurveyFinishedOnDays = previousFinishedSurveys(12),
                            finishedTodayTasks = linkedSetOf())

            val day21_tasksFinished_noMissingDays =
                    StudySetup(templateSchedules = templateSchedules,
                            studyBurstDay = 20,
                            studyBurstFinishedOnDays = finishedOnDays(14, 0),
                            studyBurstSurveyFinishedOnDays = previousFinishedSurveys(14),
                            finishedTodayTasks = linkedSetOf())

            val day89_tasksFinished_noMissingDays =
                    StudySetup(templateSchedules = templateSchedules,
                            studyBurstDay = 88,
                            studyBurstFinishedOnDays = finishedOnDays(14, 0),
                            studyBurstSurveyFinishedOnDays = previousFinishedSurveys(14),
                            finishedTodayTasks = linkedSetOf())
        }

        /// The date when the participant started the study. Hardcoded to 6:00AM local time.
        fun createdOn(): LocalDateTime {
            return now.startOfDay().minusDays(studyBurstDay.toLong()).plusHours(6L)
        }

        fun finishedMotivation(): Boolean {
            return finishedTodayTasks.size > 0 || studyBurstFinishedOnDays.size > 0
        }

        // Generated days of the study burst to mark as finished. This only applies to days that are past.
        fun mapStudyBurstFinishedOn(): Map<Int, Instant> {
            val firstDay = createdOn().startOfDay().plusSeconds(8 * 60 * 60)
            return studyBurstFinishedOnDays.filter { it <= studyBurstDay }.associateBy( { it }, {
                // iOS does random # of minutes, but let's hardcode to 30 for test reliability
                firstDay.plusDays(it.toLong()).plusMinutes(30).atZone(timezone).toInstant()
            })
        }

        // Generated days of the study burst to mark as finished. This only applies to days that are past.
        fun mapStudyBurstSurveyFinishedOn(): Map<String, Instant> {
            val firstDay = createdOn().startOfDay().plusSeconds((8.5 * 60 * 60).toLong())
            val map = studyBurstSurveyFinishedOnDays.filterValues { it <= studyBurstDay }.mapValues {
                // iOS does random # of minutes, but let's hardcode to 30 for test reliability
                firstDay.plusDays(it.value.toLong()).plusMinutes(30).atZone(timezone).toInstant()
            }.toMutableMap()
            if (finishedMotivation()) {
                map[MOTIVATION] = firstDay.plusMinutes(2).atZone(timezone).toInstant()
            }
            return map
        }

        fun createParticipant(): StudyParticipant {
            val participantJson =
                    "{\n" +
                            "  \"firstName\": \"" + firstName + "\",\n" +
                            "  \"dataGroups\": " + dataGroups.toString() + ",\n" +
                            "  \"phoneVerified\": true,\n" +
                            "  \"createdO\": \"" + createdOn().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) + "\"\n" +
                            "}"
            return RestUtils.GSON.fromJson(participantJson, StudyParticipant::class.java)
        }

        fun populateDatabase(dao: ScheduledActivityEntityDao) {
            val schedules = listOf(buildMeasuringTasks(), buildStudyBurstTasks()).flatten()
            dao.clear()
            dao.upsert(schedules)
        }

        private fun buildMeasuringTasks(): List<ScheduledActivityEntity> {
            var schedules: MutableList<ScheduledActivityEntity> = mutableListOf()
            val activityGroup = DataSourceManager.installedGroup(config.taskGroupIdentifier) ?: return listOf()

            // set default for the sort order
            var sortOrder = finishedTodayTasks.toMutableList()
            var unfinished = activityGroup.activityIdentifiers.filter { !finishedTodayTasks.contains(it) }
            unfinished = unfinished.shuffled()
            sortOrder.addAll(unfinished)
            viewModel?.setOrderedTasks(sortOrder.toList(), now)

            val studyBurstFinishedOn = mapStudyBurstFinishedOn()
            val studyBurstDates = studyBurstFinishedOn.values.sorted().toMutableList()
            sortOrder.forEachIndexed { idx, id ->
                var datesToAdd = studyBurstDates.toMutableList()
                val finishedTime = timeUntilExpires - (3600) + (idx * 4 * 60)
                if (finishedTodayTasks.contains(id)) {
                    datesToAdd.add(now.plusSeconds(finishedTime).atZone(timezone).toInstant())
                }
                var scheduledOn = createdOn()
                datesToAdd.forEach {
                    val finishedOn = it.minusSeconds(idx * 4 * 60L)
                    scheduleFromTemplate(id)?.let {
                        it.guid = (it.activityIdentifier() ?: "") + scheduledOn.toString()
                        it.scheduledOn = scheduledOn
                        it.finishedOn = finishedOn
                        it.startedOn = finishedOn?.minusSeconds(3*60L)
                        it.expiresOn = null
                        it.clientData = null
                        it.schedulePlanGuid = activityGroup.schedulePlanGuid
                        schedules.add(it)
                        // Set the scheduled on to the finished on, because the measuring activity group
                        // has a persistent schedule on Bridge
                        scheduledOn = finishedOn.atZone(timezone).toLocalDateTime()
                    }
                }

                scheduleFromTemplate(id)?.let {
                    it.guid = (it.activityIdentifier() ?: "") + scheduledOn.toString()
                    it.scheduledOn = scheduledOn
                    it.expiresOn = null
                    it.finishedOn = null
                    it.startedOn = null
                    it.clientData = null
                    it.schedulePlanGuid = activityGroup.schedulePlanGuid
                    schedules.add(it)
                }
            }
            return schedules
        }

        private fun buildStudyBurstTasks(): List<ScheduledActivityEntity> {
            var schedules: MutableList<ScheduledActivityEntity> = mutableListOf()
            // only add the study burst marker for this group, but add one for each day.
            val createdOn = createdOn()
            val studyBurstFinishedOn = mapStudyBurstFinishedOn()
            for (day in 0 until 19) {
                for (burst in 0 until 3) {
                    val scheduledOn = createdOn.startOfDay().plusDays(day + (burst * 90L))
                    val finishedOn = if (burst == 0) studyBurstFinishedOn[day] else null
                    scheduleFromTemplate(STUDY_BURST_COMPLETED)?.let {
                        it.guid = (it.activityIdentifier() ?: "") + scheduledOn.toString()
                        it.scheduledOn = scheduledOn
                        it.expiresOn = scheduledOn.plusDays(1)
                        it.finishedOn = finishedOn
                        it.startedOn = finishedOn?.minusSeconds(3*60L)
                        it.clientData = null
                        it.schedulePlanGuid = null
                        schedules.add(it)
                    }
                }
            }

            val surveyMap = mapStudyBurstSurveyFinishedOn()
            DataSourceManager.surveyGroup.activityIdentifiers.union(setOf(STUDY_BURST_REMINDER)).forEach { id ->
                scheduleFromTemplate(id)?.let {
                    it.guid = (it.activityIdentifier() ?: "") + createdOn.toString()
                    it.scheduledOn = createdOn
                    it.expiresOn = null
                    it.finishedOn = surveyMap[id]
                    it.startedOn = surveyMap[id]?.minusSeconds(3*60L)
                    it.clientData = null
                    it.schedulePlanGuid = null
                    schedules.add(it)
                }
            }

            return schedules
        }

        private fun scheduleFromTemplate(activityId: String): ScheduledActivityEntity? {
            templateSchedules.filterByActivityId(activityId).firstOrNull()?.let {
                return gson.fromJson(gson.toJson(it), ScheduledActivityEntity::class.java)
            }
            return null
        }
    }

    class MockStudyBurstViewModel(
            scheduleDao: ScheduledActivityEntityDao, scheduleRepo: ScheduleRepository,
            val studyBurstSettingsDao: StudyBurstSettingsDao, val studySetup: StudySetup,
            taskResultUploader: TaskResultUploader = mock(TaskResultUploader::class.java)):

            StudyBurstViewModel(scheduleDao, scheduleRepo, studyBurstSettingsDao, taskResultUploader) {

        override val config = studySetup.config

        override fun activityGroup(): ActivityGroup? {
            return DataSourceManager.installedGroup(config.taskGroupIdentifier)
        }

        override fun now(): LocalDateTime {
            return studySetup.now
        }

        override val timezone: ZoneId get() {
            return studySetup.timezone
        }


        init {
            studySetup.populateDatabase(scheduleDao)
        }

        private var sortOrder: List<String>? = null
        private var timestamp: LocalDateTime? = null

        fun setOrderedTasks(sortOrder: List<String>, timestamp: LocalDateTime) {
            studyBurstSettingsDao.setOrderedTasks(sortOrder, timestamp)
        }

    }
}