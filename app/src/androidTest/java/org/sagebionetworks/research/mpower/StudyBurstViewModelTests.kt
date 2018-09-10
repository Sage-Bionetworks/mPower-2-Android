package org.sagebionetworks.research.mpower

import android.app.Application
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.google.gson.Gson
import junit.framework.Assert.*
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.sagebionetworks.bridge.rest.RestUtils
import org.sagebionetworks.bridge.rest.model.StudyParticipant
import org.sagebionetworks.research.mpower.research.CompletionTask
import org.sagebionetworks.research.mpower.research.DataSourceManager
import org.sagebionetworks.research.mpower.research.MpIdentifier.*
import org.sagebionetworks.research.mpower.research.StudyBurstConfiguration
import org.sagebionetworks.research.mpower.viewmodel.StudyBurstViewModel
import org.sagebionetworks.research.sageresearch.dao.room.ScheduledActivityEntity
import org.sagebionetworks.research.sageresearch.dao.room.ScheduledActivityEntityDao
import org.sagebionetworks.research.sageresearch.extensions.endOfDay
import org.sagebionetworks.research.sageresearch.extensions.filterByActivityId
import org.sagebionetworks.research.sageresearch.extensions.startOfDay
import org.sagebionetworks.research.sageresearch.extensions.startOfNextDay
import org.sagebionetworks.research.sageresearch.manager.ActivityGroup
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
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
class StudyBurstViewModelTests: RoomTestHelper() {
    companion object {
        lateinit var application: Application
        val studyBurstList = "test_study_burst_schedules.json"
        val testResourceMap = TestResourceHelper.testResourceMap(setOf(studyBurstList))
        val gson = Gson()

        @BeforeClass
        @JvmStatic
        fun setup() {
            RoomTestHelper.setup()
            application = InstrumentationRegistry.getTargetContext().applicationContext as Application
        }
    }

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
                setOf(CompletionTask(setOf(STUDY_BURST_REMINDER, DEMOGRAPHICS), 2),
                        CompletionTask(setOf(BACKGROUND), 10),
                        CompletionTask(setOf(ENGAGEMENT), 13)),
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



//        let scheduleManager = TestStudyBurstScheduleManager(.day1_startupState)
//        guard loadSchedules(scheduleManager) else {
//            XCTFail("Failed to load the schedules and reports.")
//            return
//        }
//
//        XCTAssertNil(scheduleManager.updateFailed_error)
//        XCTAssertNotNil(scheduleManager.update_fetchedActivities)
//        XCTAssertNotNil(scheduleManager.activityGroup)
//        XCTAssertEqual(scheduleManager.dayCount, 1)
//        XCTAssertTrue(scheduleManager.hasStudyBurst)
//        XCTAssertEqual(scheduleManager.finishedSchedules.count, 0)
//        XCTAssertFalse(scheduleManager.isCompletedForToday)
//        XCTAssertFalse(scheduleManager.isLastDay)
//        XCTAssertEqual(scheduleManager.calculateThisDay(), 1)
//        XCTAssertEqual(scheduleManager.pastSurveys.count, 0)
//        XCTAssertNotNil(scheduleManager.todayCompletionTask)
//
//        let demographics = scheduleManager.scheduledActivities.filter {
//            $0.activityIdentifier == RSDIdentifier.demographics.stringValue
//        }
//        XCTAssertEqual(demographics.count, 1)
//
//        let completionTask = scheduleManager.engagementTaskPath()
//        XCTAssertNotNil(completionTask, "scheduleManager.engagementTaskPath()")
//
//        XCTAssertNil(scheduleManager.actionBarItem, "scheduleManager.actionBarItem")
//
//        let thisDay = scheduleManager.calculateThisDay()
//        XCTAssertEqual(thisDay, 1)
//
//        let pastTasks = scheduleManager.getPastTasks(for: thisDay)
//        XCTAssertEqual(pastTasks.count, 0)
//
//        XCTAssertNotNil(scheduleManager.todayCompletionTask, "scheduleManager.todayCompletionTask")
//        let todayCompletionTask = scheduleManager.getTodayCompletionTask(for: thisDay)
//        XCTAssertNotNil(todayCompletionTask, "scheduleManager.getTodayCompletionTask(for: thisDay)")
//
//        let unfinishedSchedule = scheduleManager.getUnfinishedSchedule()
//        XCTAssertNil(unfinishedSchedule, "scheduleManager.getUnfinishedSchedule(from: pastTasks)")
    }
}

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
        /// The time to use as the time until today's finished tasks will expire. Default = 15 min.
        val timeUntilExpires: Long = 15 * 60 * 1000L,
        /// The data groups to set for this participant.
        var dataGroups: Set<String> = setOf("gr_SC_DB","gr_BR_II","gr_ST_T","gr_DT_T"),
        /// The time to set as "now".
        val now: LocalDateTime = LocalDateTime.now(),
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

        val day1_startupState =
                StudySetup(studyBurstDay = 0,
                        studyBurstFinishedOnDays = setOf(),
                        studyBurstSurveyFinishedOnDays = mapOf(),
                        finishedTodayTasks = linkedSetOf())

        val day1_startupState_BRII_DTT =
                StudySetup(studyBurstDay = 0,
                        studyBurstFinishedOnDays = setOf(),
                        studyBurstSurveyFinishedOnDays = mapOf(),
                        finishedTodayTasks = linkedSetOf(),
                        timeUntilExpires = 0,
                        dataGroups = setOf("gr_SC_DB","gr_BR_II","gr_ST_T","gr_DT_T"))

        val day1_startupState_BRII_DTF =
                StudySetup(studyBurstDay = 0,
                        studyBurstFinishedOnDays = setOf(),
                        studyBurstSurveyFinishedOnDays = mapOf(),
                        finishedTodayTasks = linkedSetOf(),
                        timeUntilExpires = 0,
                        dataGroups = setOf("gr_SC_DB","gr_BR_II","gr_ST_T","gr_DT_F"))

        val day1_startupState_BRAD_DTT =
                StudySetup(studyBurstDay = 0,
                        studyBurstFinishedOnDays = setOf(),
                        studyBurstSurveyFinishedOnDays = mapOf(),
                        finishedTodayTasks = linkedSetOf(),
                        timeUntilExpires = 0,
                        dataGroups = setOf("gr_SC_DB","gr_BR_AD","gr_ST_T","gr_DT_T"))

        val day1_startupState_BRAD_DTF =
                StudySetup(studyBurstDay = 0,
                        studyBurstFinishedOnDays = setOf(),
                        studyBurstSurveyFinishedOnDays = mapOf(),
                        finishedTodayTasks = linkedSetOf(),
                        timeUntilExpires = 0,
                        dataGroups = setOf("gr_SC_DB","gr_BR_AD","gr_ST_T","gr_DT_F"))

        val day1_noTasksFinished =
                StudySetup(studyBurstDay = 0,
                        studyBurstFinishedOnDays = setOf(),
                        studyBurstSurveyFinishedOnDays = previousFinishedSurveys(0),
                        finishedTodayTasks = linkedSetOf())

        val day1_twoTasksFinished =
                StudySetup(studyBurstDay = 0,
                        studyBurstFinishedOnDays = setOf(),
                        studyBurstSurveyFinishedOnDays = previousFinishedSurveys(0),
                        finishedTodayTasks = linkedSetOf(TAPPING, WALK_AND_BALANCE))

        val day1_tasksFinished_surveysNotFinished =
                StudySetup(studyBurstDay = 0,
                        studyBurstFinishedOnDays = setOf(),
                        studyBurstSurveyFinishedOnDays = previousFinishedSurveys(0),
                        finishedTodayTasks = linkedSetOf(MEASURING))

        val day1_tasksFinished_surveysNotFinished_BRII_DTT =
                StudySetup(studyBurstDay = 0,
                        studyBurstFinishedOnDays = setOf(),
                        studyBurstSurveyFinishedOnDays = previousFinishedSurveys(0),
                        finishedTodayTasks = linkedSetOf(),
                        timeUntilExpires = 15,
                        dataGroups = setOf("gr_SC_DB","gr_BR_II","gr_ST_T","gr_DT_T"))

        val day1_tasksFinished_surveysNotFinished_BRII_DTF =
                StudySetup(studyBurstDay = 0,
                        studyBurstFinishedOnDays = setOf(),
                        studyBurstSurveyFinishedOnDays = previousFinishedSurveys(0),
                        finishedTodayTasks = linkedSetOf(),
                        timeUntilExpires = 15,
                        dataGroups = setOf("gr_SC_DB","gr_BR_II","gr_ST_T","gr_DT_F"))

        val day1_tasksFinished_surveysNotFinished_BRAD_DTT =
                StudySetup(studyBurstDay = 0,
                        studyBurstFinishedOnDays = setOf(),
                        studyBurstSurveyFinishedOnDays = previousFinishedSurveys(0),
                        finishedTodayTasks = linkedSetOf(),
                        timeUntilExpires = 15,
                        dataGroups = setOf("gr_SC_DB","gr_BR_AD","gr_ST_T","gr_DT_T"))

        val day1_tasksFinished_surveysNotFinished_BRAD_DTF =
                StudySetup(studyBurstDay = 0,
                        studyBurstFinishedOnDays = setOf(),
                        studyBurstSurveyFinishedOnDays = previousFinishedSurveys(0),
                        finishedTodayTasks = linkedSetOf(),
                        timeUntilExpires = 15,
                        dataGroups = setOf("gr_SC_DB","gr_BR_AD","gr_ST_T","gr_DT_F"))

        val day1_tasksFinished_surveysFinished =
                StudySetup(studyBurstDay = 0,
                        studyBurstFinishedOnDays = setOf(0),
                        studyBurstSurveyFinishedOnDays = previousFinishedSurveys(1),
                        finishedTodayTasks = linkedSetOf(MEASURING))

        val day1_allFinished_2HoursAgo =
                StudySetup(studyBurstDay = 0,
                        studyBurstFinishedOnDays = setOf(0),
                        studyBurstSurveyFinishedOnDays = previousFinishedSurveys(1),
                        finishedTodayTasks = linkedSetOf(MEASURING),
                        timeUntilExpires = -2 * 60 * 60)

        val day2_surveysNotFinished =
                StudySetup(studyBurstDay = 1,
                        studyBurstFinishedOnDays = setOf(0),
                        studyBurstSurveyFinishedOnDays = previousFinishedSurveys(0),
                        finishedTodayTasks = linkedSetOf())

        val day2_tasksNotFinished_surveysFinished =
                StudySetup(studyBurstDay = 1,
                        studyBurstFinishedOnDays = setOf(0),
                        studyBurstSurveyFinishedOnDays = previousFinishedSurveys(1),
                        finishedTodayTasks = linkedSetOf())

        val day9_twoTasksFinished =
                StudySetup(studyBurstDay = 8,
                        studyBurstFinishedOnDays = finishedOnDays(7, 0),
                        studyBurstSurveyFinishedOnDays = previousFinishedSurveys(7),
                        finishedTodayTasks = linkedSetOf(TAPPING, WALK_AND_BALANCE))

        val day9_tasksFinished =
                StudySetup(studyBurstDay = 8,
                        studyBurstFinishedOnDays = finishedOnDays(7, 0),
                        studyBurstSurveyFinishedOnDays = previousFinishedSurveys(7),
                        finishedTodayTasks = linkedSetOf(MEASURING))

        val day11_tasksFinished_noMissingDays =
                StudySetup(studyBurstDay = 10,
                        studyBurstFinishedOnDays = finishedOnDays(10, 0),
                        studyBurstSurveyFinishedOnDays = previousFinishedSurveys(10),
                        finishedTodayTasks = linkedSetOf(MEASURING))


        val day14_missing1_tasksFinished_engagementNotFinished =
                StudySetup(studyBurstDay = 13,
                        studyBurstFinishedOnDays = finishedOnDays(13, 1),
                        studyBurstSurveyFinishedOnDays = previousFinishedSurveys(12),
                        finishedTodayTasks = linkedSetOf(MEASURING))

        val day14_missing6_tasksFinished_engagementNotFinished =
                StudySetup(studyBurstDay = 13,
                        studyBurstFinishedOnDays = finishedOnDays(13, 6),
                        studyBurstSurveyFinishedOnDays = previousFinishedSurveys(12),
                        finishedTodayTasks = linkedSetOf(MEASURING))

        val day14_tasksFinished_engagementNotFinished =
                StudySetup(studyBurstDay = 13,
                        studyBurstFinishedOnDays = finishedOnDays(13, 0),
                        studyBurstSurveyFinishedOnDays = previousFinishedSurveys(12),
                        finishedTodayTasks = linkedSetOf(MEASURING))

        val day15_missing1_engagementNotFinished =
                StudySetup(studyBurstDay = 14,
                        studyBurstFinishedOnDays = finishedOnDays(14, 1),
                        studyBurstSurveyFinishedOnDays = previousFinishedSurveys(12),
                        finishedTodayTasks = linkedSetOf())

        val day15_burstCompleted_engagementNotFinished =
                StudySetup(studyBurstDay = 14,
                        studyBurstFinishedOnDays = finishedOnDays(14, 0),
                        studyBurstSurveyFinishedOnDays = previousFinishedSurveys(12),
                        finishedTodayTasks = linkedSetOf())

        val day15_burstCompleted_engagementFinished =
                StudySetup(studyBurstDay = 14,
                        studyBurstFinishedOnDays = finishedOnDays(14, 0),
                        studyBurstSurveyFinishedOnDays = previousFinishedSurveys(14),
                        finishedTodayTasks = linkedSetOf())

        val day21_missing6_engagementNotFinished =
                StudySetup(studyBurstDay = 20,
                        studyBurstFinishedOnDays = finishedOnDays(18, 6),
                        studyBurstSurveyFinishedOnDays = previousFinishedSurveys(12),
                        finishedTodayTasks = linkedSetOf())

        val day21_tasksFinished_noMissingDays =
                StudySetup(studyBurstDay = 20,
                        studyBurstFinishedOnDays = finishedOnDays(14, 0),
                        studyBurstSurveyFinishedOnDays = previousFinishedSurveys(14),
                        finishedTodayTasks = linkedSetOf())

        val day89_tasksFinished_noMissingDays =
                StudySetup(studyBurstDay = 88,
                        studyBurstFinishedOnDays = finishedOnDays(14, 0),
                        studyBurstSurveyFinishedOnDays = previousFinishedSurveys(14),
                        finishedTodayTasks = linkedSetOf())
    }

    /// The date when the participant started the study. Hardcoded to 6:15AM local time.
    fun createdOn(): LocalDateTime {
        return now.startOfDay().startOfNextDay().minusDays(studyBurstDay.toLong()).plusSeconds((8.5 * 60 * 60).toLong())
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
        dao.clear()

    }

    private fun buildMeasuringTasks(): List<ScheduledActivityEntity> {
        var schedules: MutableList<ScheduledActivityEntity> = mutableListOf()
        val activityGroup = DataSourceManager.installedGroup(config.taskGroupIdentifier) ?: return listOf()

        // set default for the sort order
        var sortOrder = activityGroup
                .activityIdentifiers.filter { !finishedTodayTasks.contains(it) }
        sortOrder = sortOrder.shuffled()
        viewModel?.setOrderedTasks(sortOrder, now)

        val studyBurstFinishedOn = mapStudyBurstFinishedOn()
        val studyBurstDates = studyBurstFinishedOn.values.sorted().toMutableList()
        sortOrder.forEachIndexed { idx, id ->
            val finishedTime = timeUntilExpires - (3600 * 1000L) + (idx * 4 * 60 * 1000L)
            if (finishedTodayTasks.contains(id)) {
                studyBurstDates.add(now.plusSeconds(finishedTime).atZone(timezone).toInstant())
            }
            var scheduledOn = createdOn()
            studyBurstDates.forEach {
                val finishedOn = it.minusSeconds(idx * 4 * 60L)
                templateSchedules.filterByActivityId(id).firstOrNull()?.let {
                    it.scheduledOn = scheduledOn
                    it.finishedOn = finishedOn
                    it.expiresOn = null
                    it.clientData = null
                    it.schedulePlanGuid = activityGroup.schedulePlanGuid
                    schedules.add(it)
                    // Set the scheduled on to the finished on, because the measuring activity group
                    // has a persistent schedule on Bridge
                    scheduledOn = finishedOn.atZone(timezone).toLocalDateTime()
                }
            }

            templateSchedules.filterByActivityId(id).firstOrNull()?.let {
                it.scheduledOn = scheduledOn
                it.expiresOn = null
                it.finishedOn = null
                it.clientData = null
                it.schedulePlanGuid = activityGroup.schedulePlanGuid
                schedules.add(it)
            }
        }
        return schedules
    }

    private fun buildStuyBurstTasks(): List<ScheduledActivityEntity> {
        var schedules: MutableList<ScheduledActivityEntity> = mutableListOf()
        // only add the study burst marker for this group, but add one for each day.
        val createdOn = createdOn()
        val studyBurstFinishedOn = mapStudyBurstFinishedOn()
        for (day in 0 until 19) {
            for (burst in 0 until 3) {
                val scheduledOn = createdOn.startOfDay().plusDays(day + (burst * 90L))
                val finishedOn = if (burst == 0) studyBurstFinishedOn[day] else null
                templateSchedules.filterByActivityId(STUDY_BURST_COMPLETED).firstOrNull()?.let {
                    it.scheduledOn = scheduledOn
                    it.expiresOn = scheduledOn.plusDays(1)
                    it.finishedOn = finishedOn
                    it.clientData = null
                    it.schedulePlanGuid = null
                    schedules.add(it)
                }
             }
        }

        val surveyMap = mapStudyBurstSurveyFinishedOn()

        return schedules
    }


//        let surveyMap = studySetup.mapStudyBurstSurveyFinishedOn()
//
//        // Add all the surveys that are suppose to be from the server.
//        SurveyReference.all.forEach {
//            let survey = createSchedule(with: $0.identifier,
//            scheduledOn: studySetup.createdOn,
//            expiresOn: nil,
//            finishedOn: surveyMap[$0.identifier],
//            clientData: nil,
//            schedulePlanGuid: nil,
//            survey: $0)
//            self.schedules.append(survey)
//        }
//    }
}

class MockStudyBurstViewModel(app: Application, val studySetup: StudySetup): StudyBurstViewModel(app) {

    override val studyBurstConfiguration = studySetup.config

    override fun activityGroup(): ActivityGroup? {
        return DataSourceManager.installedGroup(studyBurstConfiguration.taskGroupIdentifier)
    }

    override fun todayQuery(): Pair<LocalDateTime, LocalDateTime> {
        return Pair(studySetup.now.startOfDay(), studySetup.now.endOfDay())
    }

    override fun studyBurstQuery(): Pair<LocalDateTime, LocalDateTime> {
        return Pair(studySetup.now.startOfDay(), studySetup.now.endOfDay())
    }

    private var sortOrder: List<String>? = null
    private var timestamp: LocalDateTime? = null

    public override fun setOrderedTasks(sortOrder: List<String>, timestamp: LocalDateTime) {
        this.sortOrder = sortOrder
        this.timestamp = timestamp
    }

    override fun getTaskSortOrder(): List<String>? {
        return sortOrder
    }

    override fun getTaskSortOrderTimestamp(): LocalDateTime? {
        return timestamp
    }
}