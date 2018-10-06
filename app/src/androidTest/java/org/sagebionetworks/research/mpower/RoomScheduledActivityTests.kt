package org.sagebionetworks.research.mpower

import androidx.test.runner.AndroidJUnit4
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertNull


import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.sagebionetworks.bridge.rest.model.ActivityType
import org.sagebionetworks.bridge.rest.model.ScheduleStatus
import org.sagebionetworks.research.sageresearch.dao.room.ScheduledActivityEntity
import org.threeten.bp.LocalDateTime
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
// ran into multi-dex issues moving this to a library project, leaving it here for now
class RoomScheduledActivityTests: RoomTestHelper() {

    companion object {
        val activityList = "test_scheduled_activities.json"
        val timeZonePstActivityList = "test_activities_timezones_pst.json"
        val timeZoneEstActivityList = "test_activities_timezones_est.json"
        val finishedBetweenActivityList = "test_schedules_finished_between.json"
        val recentlyFinishedActivityList = "test_schedules_recently_finished.json"

        val testResourceMap = TestResourceHelper.testResourceMap(setOf(
                activityList,
                timeZonePstActivityList,
                timeZoneEstActivityList,
                finishedBetweenActivityList,
                recentlyFinishedActivityList))
    }

    @Before
    fun setupForEachTest() {
        activityDao.clear()
        activityDao.upsert(testResourceMap[activityList] ?: listOf())
    }

    @Test
    fun setup_testInitialDatabaseAndJsonSetup() {
        assertNotNull(testResourceMap[activityList])
        assertEquals(8, testResourceMap[activityList]?.size)
    }

    @Test
    fun insert_test() {
        activityDao.clear()
        val first = testResourceMap[activityList]!!.first()
        activityDao.upsert(listOf(first))
        assertTaskContains(arrayOf(first.guid), getValue(activityDao.all()))
    }

    @Test
    fun insert_testAll() {
        activityDao.clear()
        activityDao.upsert(arrayListOf(testResourceMap[activityList]!![0], testResourceMap[activityList]!![1]))
        assertTaskContains(arrayOf(testResourceMap[activityList]!![0].guid,
                testResourceMap[activityList]!![1].guid), getValue(activityDao.all()))
    }

    @Test
    fun delete_test() {
        activityDao.clear()
        assertEquals(0, getValue(activityDao.all()).size)
    }

    @Test
    fun serialization_testTaskReference() {
        assertMedicationTaskReferenceActivity(testResourceMap[activityList]!!.first())
    }

    fun assertMedicationTaskReferenceActivity(activity: ScheduledActivityEntity?) {
        assertNotNull(activity)
        assertEquals("273c4518-7cb6-4496-b1dd-c0b5bf291b09:2018-08-17T00:00:00.000", activity?.guid)
        assertEquals("e24e6601-1822-48b0-8770-00870d870708", activity?.schedulePlanGuid)
        assertEquals(false, activity?.persistent)
        assertNotNull(activity?.scheduledOn)
        assertEquals(LocalDateTime.parse(
                "2018-08-17T00:00-04:00", DateTimeFormatter.ISO_DATE_TIME), activity?.scheduledOn)
        assertNotNull(activity?.expiresOn)
        assertEquals(LocalDateTime.parse(
                "2018-08-18T00:00-04:00", DateTimeFormatter.ISO_DATE_TIME), activity?.expiresOn)
        assertEquals(ScheduleStatus.EXPIRED, activity?.status)
        assertNotNull(activity?.activity)
        assertEquals("Medication", activity?.activity?.label)
        assertEquals("273c4518-7cb6-4496-b1dd-c0b5bf291b09", activity?.activity?.guid)
        assertNotNull(activity?.activity?.task)
        assertEquals("Medication", activity?.activity?.task?.identifier)
        assertNull(activity?.clientData?.data)
        assertEquals(ActivityType.TASK, activity?.activity?.activityType)
    }

    @Test
    fun serialization_testSurveyReference() {
        assertMotivationSurveyReferenceActivity(testResourceMap[activityList]!!.get(2))
    }

    fun assertMotivationSurveyReferenceActivity(activity: ScheduledActivityEntity?) {
        assertNotNull(activity)
        assertEquals("a341c893-615d-48e1-ab6a-d418af720269:2018-08-17T13:40:39.183", activity?.guid)
        assertNotNull(activity?.startedOn)
        assertEquals(DateTime.parse("2018-08-17T17:44:05.114Z").toDate().time, activity?.startedOn?.toEpochMilli())
        assertNotNull(activity?.finishedOn)
        assertEquals(DateTime.parse("2018-08-17T18:09:28.610Z").toDate().time, activity?.finishedOn?.toEpochMilli())
        assertNotNull(activity?.activity?.survey)
        assertEquals("Motivation", activity?.activity?.survey?.identifier)
        assertEquals("https://ws.sagebridge.org/v3/surveys/7beb8a71-65d0-4217" +
                "-a4fb-3bb029b55258/revisions/2018-08-07T22:41:52.186Z", activity?.activity?.survey?.href)
        assertEquals("{motivation_II_T=eat}", activity?.clientData?.data.toString())
        assertEquals(ActivityType.SURVEY, activity?.activity?.activityType)
    }

    @Test
    fun query_testAll() {
        val dbActivities = getValue(activityDao.all())
        assertEquals(8, dbActivities.size)
        assertMedicationTaskReferenceActivity(dbActivities.first())
        assertMotivationSurveyReferenceActivity(dbActivities[2])
        assertCompoundActivity(dbActivities[6])
        assertActivityWithSchemaActivity(dbActivities[7])
    }

    fun assertCompoundActivity(activity: ScheduledActivityEntity?) {
        assertNotNull(activity)
        assertEquals("2dc5dddb-cdb8-4291-9e98-183dd9b20e5f:2016-03-08T17:24:02.103", activity?.guid)
        assertEquals("Compound Activity From Definition", activity?.activity?.label)
        assertEquals(ActivityType.COMPOUND, activity?.activity?.activityType)
        assertNotNull(activity?.activity?.compoundActivity)
        assertEquals("compound-from-def", activity?.activity?.compoundActivity?.taskIdentifier)
        assertNotNull(activity?.activity?.compoundActivity?.schemaList)
        assertEquals(1, activity?.activity?.compoundActivity?.schemaList?.size)
        assertEquals("simple-test-schema", activity?.activity?.compoundActivity?.schemaList?.get(0)?.id)
        assertEquals(2L, activity?.activity?.compoundActivity?.schemaList?.get(0)?.revision)
        assertNotNull(activity?.activity?.compoundActivity?.surveyList)
        assertEquals(1, activity?.activity?.compoundActivity?.surveyList?.size)
        assertEquals("simple-survey-1", activity?.activity?.compoundActivity?.surveyList?.get(0)?.identifier)
        assertEquals("2a93e632-c760-4b52-b567-ec36b26acfa4", activity?.activity?.compoundActivity?.surveyList?.get(0)?.guid)
        assertEquals("http://localhost:9000/v3/surveys/2a93e632-c760-4b52-b567-ec36b26acfa4/revisions/2017-02-09T02:26:14.644Z",
                activity?.activity?.compoundActivity?.surveyList?.get(0)?.href)
        assertEquals(DateTime.parse("2017-02-09T02:26:14.644Z").toDate().time,
                activity?.activity?.compoundActivity?.surveyList?.get(0)?.createdOn?.toEpochMilli())
    }

    fun assertActivityWithSchemaActivity(activity: ScheduledActivityEntity?) {
        assertNotNull(activity)
        assertEquals("f34e5be9-df81-46a1-bf21-5a2994c44e9d:2016-03-08T17:24:02.103", activity?.guid)
        assertNotNull(activity?.activity?.task)
        assertEquals("back-compat-task", activity?.activity?.task?.identifier)
        assertNotNull(activity?.activity?.task?.schema)
        assertEquals("upload-v2-manual-test", activity?.activity?.task?.schema?.id)
        assertEquals(1L, activity?.activity?.task?.schema?.revision)
    }

    @Test
    fun query_testTaskIdentifier() {
        val dbActivities = getValue(activityDao.activityGroup(setOf("Medication")))
        assertEquals(1, dbActivities.size)
        assertMedicationTaskReferenceActivity(dbActivities.first())
    }

    @Test
    fun query_testSurveyIdentifier() {
        val dbActivities = getValue(activityDao.activityGroup(setOf("Motivation")))
        assertEquals(1, dbActivities.size)
        assertMotivationSurveyReferenceActivity(dbActivities.first())
    }

    @Test
    fun query_testCompoundIdentifier() {
        val dbActivities = getValue(activityDao.activityGroup(setOf("compound-from-def")))
        assertEquals(1, dbActivities.size)
        assertCompoundActivity(dbActivities.first())
    }

    @Test
    fun query_testMedicationDateFound() {
        val date = LocalDateTime.parse("2018-08-17T12:00:00.000-04:00", DateTimeFormatter.ISO_DATE_TIME)
        val dbActivities = getValue(activityDao.activityGroupAvailableOn(setOf("Medication"), date))
        assertEquals(1, dbActivities.size)
        assertMedicationTaskReferenceActivity(dbActivities.first())
    }

    @Test
    fun query_testMedicationDateNotFound() {
        val date = LocalDateTime.parse("2018-08-14T00:00:00.000-04:00", DateTimeFormatter.ISO_DATE_TIME)
        val dbActivities = getValue(activityDao.activityGroupAvailableOn(setOf("Medication"), date))
        assertEquals(0, dbActivities.size)
    }

    @Test
    fun query_testMedicationDateStartEdgeCaseFound() {
        val date = LocalDateTime.parse("2018-08-17T00:00:00.000-04:00", DateTimeFormatter.ISO_DATE_TIME)
        val dbActivities = getValue(activityDao.activityGroupAvailableOn(setOf("Medication"), date))
        assertEquals(1, dbActivities.size)
        assertMedicationTaskReferenceActivity(dbActivities.first())
    }

    @Test
    fun query_testMedicationDateEndEdgeCaseFound() {
        val date = LocalDateTime.parse("2018-08-18T00:00:00.000-04:00", DateTimeFormatter.ISO_DATE_TIME)
        val dbActivities = getValue(activityDao.activityGroupAvailableOn(setOf("Medication"), date))
        assertEquals(1, dbActivities.size)
        assertMedicationTaskReferenceActivity(dbActivities.first())
    }

    @Test
    fun query_testMedicationDateEndEdgeCaseNotFound() {
        val date = LocalDateTime.parse("2018-08-18T00:00:00.001-04:00", DateTimeFormatter.ISO_DATE_TIME)
        val dbActivities = getValue(activityDao.activityGroupAvailableOn(setOf("Medication"), date))
        assertEquals(0, dbActivities.size)
    }

    @Test
    fun query_testTaskGroup() {
        val dbActivities = getValue(activityDao.activityGroup(setOf("Medication", "Motivation")))
        assertEquals(2, dbActivities.size)
        assertMedicationTaskReferenceActivity(dbActivities.first())
        assertMotivationSurveyReferenceActivity(dbActivities.get(1))
    }

    @Test
    fun query_testTaskGroupAvailableOn() {
        val date = LocalDateTime.parse("2018-08-17T14:00:00.000-04:00", DateTimeFormatter.ISO_DATE_TIME)
        val dbActivities = getValue(activityDao.activityGroupAvailableOn(setOf("Medication", "Motivation"), date))
        assertEquals(2, dbActivities.size)
        assertMedicationTaskReferenceActivity(dbActivities.first())
        assertMotivationSurveyReferenceActivity(dbActivities.get(1))
    }

    @Test
    fun query_testDate() {
        val date = LocalDateTime.parse("2018-08-17T14:00:00.000-04:00", DateTimeFormatter.ISO_DATE_TIME)
        val dbActivities = getValue(activityDao.availableOn(date))
        assertEquals(5, dbActivities.size)
        assertTaskContains(arrayOf("273c4518-7cb6-4496-b1dd-c0b5bf291b09:2018-08-17T00:00:00.000",
                "fe79d987-28a2-4ccd-bcf3-b3d07b925a6b:2018-08-17T00:00:00.000",
                "178ef89c-78b9-4861-b308-3dda0daf756d:2018-08-17T13:40:39.183",
                "e6fe761f-6187-4e8f-b659-bce4edc98f06:2018-08-17T13:40:39.183",
                "a341c893-615d-48e1-ab6a-d418af720269:2018-08-17T13:40:39.183"), dbActivities)
    }

    @Test
    fun query_testAvailableOn() {
        val date = LocalDateTime.parse("2018-08-17T14:00:00.000-04:00", DateTimeFormatter.ISO_DATE_TIME)
        val dbActivities = getValue(activityDao.unfinishedAvailableOn(date))
        assertEquals(4, dbActivities.size)
        assertTaskContains(arrayOf("273c4518-7cb6-4496-b1dd-c0b5bf291b09:2018-08-17T00:00:00.000",
                "fe79d987-28a2-4ccd-bcf3-b3d07b925a6b:2018-08-17T00:00:00.000",
                "178ef89c-78b9-4861-b308-3dda0daf756d:2018-08-17T13:40:39.183",
                "e6fe761f-6187-4e8f-b659-bce4edc98f06:2018-08-17T13:40:39.183"), dbActivities)
    }

    @Test fun query_testTimezoneChangePstToEst() {
        activityDao.clear()
        activityDao.upsert(testResourceMap[timeZonePstActivityList]!!)

        // Tests if user switched time zones from the db stored data which was inserted in PST
        val estDateInclude = LocalDateTime.parse("2018-08-17T00:00:00.000-05:00", DateTimeFormatter.ISO_DATE_TIME)

        // We should still get the activity in the db because scheduledOn is considered a LocalTimeZone
        val dbActivitiesInclude = getValue(activityDao.unfinishedAvailableOn(estDateInclude))
        assertEquals(1, dbActivitiesInclude.size)

        val estDateExclude = LocalDateTime.parse("2018-08-18T00:00:01.000-05:00", DateTimeFormatter.ISO_DATE_TIME)

        val dbActivitiesExclude = getValue(activityDao.unfinishedAvailableOn(estDateExclude))
        assertEquals(0, dbActivitiesExclude.size)
    }

    @Test fun query_testTimezoneChangeEstToPst() {
        activityDao.clear()
        activityDao.upsert(testResourceMap[timeZoneEstActivityList]!!)

        // Tests if user switched time zones from the db stored data which was inserted in PST
        val pstDateInclude = LocalDateTime.parse("2018-08-17T23:59:00.000-08:00", DateTimeFormatter.ISO_DATE_TIME)

        // We should still get the activity in the db because scheduledOn is considered a LocalTimeZone
        val dbActivitiesInclude = getValue(activityDao.unfinishedAvailableOn(pstDateInclude))
        assertEquals(1, dbActivitiesInclude.size)

        val estDateExclude = LocalDateTime.parse("2018-08-16T23:59:00.000-08:00", DateTimeFormatter.ISO_DATE_TIME)

        val dbActivitiesExclude = getValue(activityDao.unfinishedAvailableOn(estDateExclude))
        assertEquals(0, dbActivitiesExclude.size)
    }

    @Test fun query_taskGroupFinishedBetweenUnfinished() {
        activityDao.clear()
        activityDao.upsert(testResourceMap[finishedBetweenActivityList]!!)
        val taskGroup = setOf("Medication", "Motivation")
        val start = ZonedDateTime.parse("2018-08-18T00:00:00.000Z").toInstant()
        val end = ZonedDateTime.parse("2018-08-19T00:00:00.000Z").toInstant()
        val dbActivities = getValue(activityDao.activityGroupFinishedBetween(taskGroup, start, end))
        assertEquals(0, dbActivities.size)
    }

    @Test fun query_taskGroupFinishedBetweenFinished() {
        activityDao.clear()
        activityDao.upsert(testResourceMap[finishedBetweenActivityList]!!)
        val taskGroup = setOf("Medication", "Motivation")
        val start = ZonedDateTime.parse("2018-08-17T00:00:00.000Z").toInstant()
        val end = ZonedDateTime.parse("2018-08-18T00:00:00.000Z").toInstant()
        val dbActivities = getValue(activityDao.activityGroupFinishedBetween(taskGroup, start, end))
        assertTaskContains(arrayOf(
                "273c4518-7cb6-4496-b1dd-c0b5bf291b09:2018-08-17T00:00:00.000",
                "a341c893-615d-48e1-ab6a-d418af720269:2018-08-17T00:00:00.000-04:00"), dbActivities)
    }

    @Test fun query_excludeTaskGroupFinishedBetween() {
        activityDao.clear()
        activityDao.upsert(testResourceMap[finishedBetweenActivityList]!!)
        val excludeTaskGroup = setOf("Medication")
        val start = ZonedDateTime.parse("2018-08-17T00:00:00.000Z").toInstant()
        val end = ZonedDateTime.parse("2018-08-18T00:00:00.000Z").toInstant()
        val dbActivities = getValue(activityDao.excludeActivityGroupFinishedBetween(excludeTaskGroup, start, end))
        // We excluded the "Medication" task, so we should just have "Motivation" task
        assertTaskContains(arrayOf("a341c893-615d-48e1-ab6a-d418af720269:2018-08-17T00:00:00.000-04:00"), dbActivities)
    }

    @Test fun query_excludeSurveyGroupAvailableOnExclude() {
        val excludeGroup = setOf("Demographics")
        val availableOn = LocalDateTime.parse("2018-08-17T14:00:00.000-04:00", DateTimeFormatter.ISO_DATE_TIME)
        val dbActivities = getValue(activityDao.excludeSurveyGroupUnfinishedAvailableOn(excludeGroup, availableOn))
        // We excluded the "Demographics" survey, so we should just have "Engagement" survey
        assertTaskContains(arrayOf("e6fe761f-6187-4e8f-b659-bce4edc98f06:2018-08-17T13:40:39.183"), dbActivities)
    }

    @Test fun query_excludeSurveyGroupAvailableOnExcludeAll() {
        val excludeGroup = setOf("Demographics", "Engagement")
        val availableOn = LocalDateTime.parse("2018-08-17T14:00:00.000-04:00", DateTimeFormatter.ISO_DATE_TIME)
        val dbActivities = getValue(activityDao.excludeSurveyGroupUnfinishedAvailableOn(excludeGroup, availableOn))
        // We excluded both "Demographics" and "Engagement" surveys
        assertEquals(0, dbActivities.size)
    }

    @Test fun query_excludeSurveyGroupAvailableOnNone() {
        val availableOn = LocalDateTime.parse("2018-08-17T13:00:00.000-04:00", DateTimeFormatter.ISO_DATE_TIME)
        val dbActivities = getValue(activityDao.excludeSurveyGroupUnfinishedAvailableOn(setOf(), availableOn))
        // There are no surveys available during that time
        assertEquals(0, dbActivities.size)
    }

    @Test fun query_mostRecentFinishedActivityNoneFinished() {
        activityDao.clear()
        activityDao.upsert(testResourceMap[recentlyFinishedActivityList]!!)
        val activityGroup = setOf("Motivation")
        val dbActivities = getValue(activityDao.mostRecentFinishedActivity(activityGroup))
        // There are no finished Motivation activities
        assertEquals(0, dbActivities.size)
    }

    @Test fun query_mostRecentFinishedActivity() {
        activityDao.clear()
        activityDao.upsert(testResourceMap[recentlyFinishedActivityList]!!)
        val activityGroup = setOf("Medication")
        val dbActivities = getValue(activityDao.mostRecentFinishedActivity(activityGroup))
        // There are 2 finished Medication activities, but the most recent is this guid
        assertTaskContains(arrayOf("273c4518-7cb6-4496-b1dd-c0b5bf291b09:2018-08-18T00:00:00.000-04:00"), dbActivities)
    }

    fun assertTaskContains(guids: Array<String>, activityList: List<ScheduledActivityEntity>) {
        assertEquals(guids.size, activityList.size)
        assertEquals(0, activityList.filter { !guids.contains(it.guid) }.size)
    }

    @Test fun query_needsSyncedWithBridge() {
        // TODO: mdephillips 9/24/18 do tests
    }
}