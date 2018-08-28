package org.sagebionetworks.research.mpower

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertNull
import org.joda.time.DateTime
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.sagebionetworks.bridge.rest.RestUtils
import org.sagebionetworks.bridge.rest.model.ActivityType
import org.sagebionetworks.bridge.rest.model.ScheduleStatus
import org.sagebionetworks.bridge.rest.model.ScheduledActivityListV4
import org.sagebionetworks.research.sageresearch.dao.room.EntityTypeConverters
import org.sagebionetworks.research.sageresearch.dao.room.ResearchDatabase
import org.sagebionetworks.research.sageresearch.dao.room.RoomScheduledActivityDao
import org.sagebionetworks.research.sageresearch.dao.room.ScheduledActivityEntity
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.io.IOException
import java.nio.charset.Charset

@RunWith(AndroidJUnit4::class)
// ran into multidex issues moving this to a library project, leaving it here for now
class RoomScheduledActivityTests {

    companion object {
        lateinit var database: ResearchDatabase
        lateinit var activityDao: RoomScheduledActivityDao

        // List to test most queries
        lateinit var roomActivityList: List<ScheduledActivityEntity>

        // List to test timezone changes
        lateinit var timeZoneEstActivityList: List<ScheduledActivityEntity>
        lateinit var timeZonePstActivityList: List<ScheduledActivityEntity>

        @BeforeClass @JvmStatic fun setup() {
            database = Room.inMemoryDatabaseBuilder(
                    InstrumentationRegistry.getTargetContext(), ResearchDatabase::class.java)
                    .allowMainThreadQueries().build()

            activityDao = database.activitiesDao()

            val dbTestActivityList = RestUtils.GSON.fromJson(
                    resourceAsString("test_scheduled_activities.json"),
                    ScheduledActivityListV4::class.java)
            roomActivityList = EntityTypeConverters().fromScheduledActivityListV4(dbTestActivityList) ?: ArrayList()

            val timeZoneEstTestList = RestUtils.GSON.fromJson(
                    resourceAsString("test_activities_timezones_est.json"),
                    ScheduledActivityListV4::class.java)
            timeZoneEstActivityList = EntityTypeConverters().fromScheduledActivityListV4(timeZoneEstTestList) ?: ArrayList()

            val timeZonePstTestList = RestUtils.GSON.fromJson(
                    resourceAsString("test_activities_timezones_pst.json"),
                    ScheduledActivityListV4::class.java)
            timeZonePstActivityList = EntityTypeConverters().fromScheduledActivityListV4(timeZonePstTestList) ?: ArrayList()
        }

        @AfterClass @JvmStatic fun teardown() {
            database.close()
        }

        fun resourceAsString(filename: String): String? {
            var json: String? = null
            try {
                val inputStream = RoomScheduledActivityTests::class.java
                        .classLoader.getResourceAsStream(filename)
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                inputStream.close()
                json = String(buffer, Charset.defaultCharset())
            } catch (e: IOException) {
                assertNull("Error loading class resource", e)
            }
            return json
        }
    }

    @Before
    fun setupForEachTest() {
        activityDao.clear()
        activityDao.insert(roomActivityList)
    }

    @Test
    fun setup_testInitialDatabaseAndJsonSetup() {
        assertNotNull(roomActivityList)
        assertEquals(8, roomActivityList.size)
    }

    @Test
    fun insert_test() {
        activityDao.clear()
        activityDao.insert(roomActivityList.first())
        assertTaskContains(arrayOf(roomActivityList.first().guid), activityDao.getAll())
    }

    @Test
    fun insert_testAll() {
        activityDao.clear()
        activityDao.insert(arrayListOf(roomActivityList[0], roomActivityList[1]))
        assertTaskContains(arrayOf(roomActivityList[0].guid, roomActivityList[1].guid), activityDao.getAll())
    }

    @Test
    fun delete_test() {
        activityDao.clear()
        assertEquals(0, activityDao.getAll().size)
    }

    @Test
    fun serialization_testTaskReference() {
        assertMedicationTaskReferenceActivity(roomActivityList.first())
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
        assertMotivationSurveyReferenceActivity(roomActivityList.get(2))
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
        val dbActivities = activityDao.getAll()
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
        val dbActivities = activityDao.get("Medication")
        assertEquals(1, dbActivities.size)
        assertMedicationTaskReferenceActivity(dbActivities.first())
    }

    @Test
    fun query_testSurveyIdentifier() {
        val dbActivities = activityDao.get("Motivation")
        assertEquals(1, dbActivities.size)
        assertMotivationSurveyReferenceActivity(dbActivities.first())
    }

    @Test
    fun query_testCompoundIdentifier() {
        val dbActivities = activityDao.get("compound-from-def")
        assertEquals(1, dbActivities.size)
        assertCompoundActivity(dbActivities.first())
    }

    @Test
    fun query_testMedicationDateFound() {
        val date = LocalDateTime.parse("2018-08-17T12:00:00.000-04:00", DateTimeFormatter.ISO_DATE_TIME)
        val dbActivities = activityDao.get("Medication", date)
        assertEquals(1, dbActivities.size)
        assertMedicationTaskReferenceActivity(dbActivities.first())
    }

    @Test
    fun query_testMedicationDateNotFound() {
        val date = LocalDateTime.parse("2018-08-14T00:00:00.000-04:00", DateTimeFormatter.ISO_DATE_TIME)
        val dbActivities = activityDao.get("Medication", date)
        assertEquals(0, dbActivities.size)
    }

    @Test
    fun query_testMedicationDateStartEdgeCaseFound() {
        val date = LocalDateTime.parse("2018-08-17T00:00:00.000-04:00", DateTimeFormatter.ISO_DATE_TIME)
        val dbActivities = activityDao.get("Medication", date)
        assertEquals(1, dbActivities.size)
        assertMedicationTaskReferenceActivity(dbActivities.first())
    }

    @Test
    fun query_testMedicationDateEndEdgeCaseFound() {
        val date = LocalDateTime.parse("2018-08-18T00:00:00.000-04:00", DateTimeFormatter.ISO_DATE_TIME)
        val dbActivities = activityDao.get("Medication", date)
        assertEquals(1, dbActivities.size)
        assertMedicationTaskReferenceActivity(dbActivities.first())
    }

    @Test
    fun query_testMedicationDateEndEdgeCaseNotFound() {
        val date = LocalDateTime.parse("2018-08-18T00:00:00.001-04:00", DateTimeFormatter.ISO_DATE_TIME)
        val dbActivities = activityDao.get("Medication", date)
        assertEquals(0, dbActivities.size)
    }

    @Test
    fun query_testTaskGroup() {
        val dbActivities = activityDao.get(arrayOf("Medication", "Motivation"))
        assertEquals(2, dbActivities.size)
        assertMedicationTaskReferenceActivity(dbActivities.first())
        assertMotivationSurveyReferenceActivity(dbActivities.get(1))
    }

    @Test
    fun query_testDate() {
        val date = LocalDateTime.parse("2018-08-17T14:00:00.000-04:00", DateTimeFormatter.ISO_DATE_TIME)
        val dbActivities = activityDao.get(date)
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
        val dbActivities = activityDao.getAvailableOn(date)
        assertEquals(4, dbActivities.size)
        assertTaskContains(arrayOf("273c4518-7cb6-4496-b1dd-c0b5bf291b09:2018-08-17T00:00:00.000",
                "fe79d987-28a2-4ccd-bcf3-b3d07b925a6b:2018-08-17T00:00:00.000",
                "178ef89c-78b9-4861-b308-3dda0daf756d:2018-08-17T13:40:39.183",
                "e6fe761f-6187-4e8f-b659-bce4edc98f06:2018-08-17T13:40:39.183"), dbActivities)
    }

    @Test fun query_testTimezoneChangePstToEst() {
        activityDao.clear()
        activityDao.insert(timeZonePstActivityList)

        // Tests if user switched time zones from the db stored data which was inserted in PST
        val estDateInclude = LocalDateTime.parse("2018-08-17T00:00:00.000-05:00", DateTimeFormatter.ISO_DATE_TIME)

        // We should still get the activity in the db because scheduledOn is considered a LocalTimeZone
        val dbActivitiesInclude = activityDao.getAvailableOn(estDateInclude)
        assertEquals(1, dbActivitiesInclude.size)

        val estDateExclude = LocalDateTime.parse("2018-08-18T00:00:01.000-05:00", DateTimeFormatter.ISO_DATE_TIME)

        val dbActivitiesExclude = activityDao.getAvailableOn(estDateExclude)
        assertEquals(0, dbActivitiesExclude.size)
    }

    @Test fun query_testTimezoneChangeEstToPst() {
        activityDao.clear()
        activityDao.insert(timeZoneEstActivityList)

        // Tests if user switched time zones from the db stored data which was inserted in PST
        val pstDateInclude = LocalDateTime.parse("2018-08-17T23:59:00.000-08:00", DateTimeFormatter.ISO_DATE_TIME)

        // We should still get the activity in the db because scheduledOn is considered a LocalTimeZone
        val dbActivitiesInclude = activityDao.getAvailableOn(pstDateInclude)
        assertEquals(1, dbActivitiesInclude.size)

        val estDateExclude = LocalDateTime.parse("2018-08-16T23:59:00.000-08:00", DateTimeFormatter.ISO_DATE_TIME)

        val dbActivitiesExclude = activityDao.getAvailableOn(estDateExclude)
        assertEquals(0, dbActivitiesExclude.size)
    }

    fun assertTaskContains(guids: Array<String>, activityList: List<ScheduledActivityEntity>) {
        assertEquals(0, activityList.filter { !guids.contains(it.guid) }.size)
    }
}