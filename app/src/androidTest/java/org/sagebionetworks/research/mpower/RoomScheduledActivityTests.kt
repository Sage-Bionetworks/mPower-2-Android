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
import org.sagebionetworks.research.mpower.room.ResearchDatabase
import org.sagebionetworks.research.mpower.room.ScheduledActivityEntity
import org.sagebionetworks.research.mpower.room.RoomScheduledActivityDao
import org.sagebionetworks.research.mpower.room.EntityTypeConverters
import java.io.IOException
import java.nio.charset.Charset

@RunWith(AndroidJUnit4::class)
class RoomScheduledActivityTests {

    companion object {
        lateinit var database: ResearchDatabase
        lateinit var activityDao: RoomScheduledActivityDao
        lateinit var activityList: ScheduledActivityListV4
        lateinit var roomActivityList: List<ScheduledActivityEntity>

        @BeforeClass @JvmStatic fun setup() {
            database = Room.inMemoryDatabaseBuilder(
                    InstrumentationRegistry.getTargetContext(), ResearchDatabase::class.java)
                    .allowMainThreadQueries().build()

            activityDao = database.activitiesDao()

            activityList = RestUtils.GSON.fromJson(
                    resourceAsString("mpower_activities.json"),
                    ScheduledActivityListV4::class.java)

            roomActivityList = EntityTypeConverters().fromScheduledActivityListV4(activityList) ?: ArrayList()
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
        assertNotNull(activityList)
        assertEquals(6, activityList.items?.size)

        assertNotNull(roomActivityList)
        assertEquals(6, roomActivityList.size)
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
        assertEquals(DateTime.parse("2018-08-17T00:00-04:00").toDate().time, activity?.scheduledOn?.toDate()?.time)
        assertNotNull(activity?.expiresOn)
        assertEquals(DateTime.parse("2018-08-18T00:00-04:00").toDate().time, activity?.expiresOn?.toDate()?.time)
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
        assertEquals(DateTime.parse("2018-08-17T17:44:05.114Z").toDate().time, activity?.startedOn?.toDate()?.time)
        assertNotNull(activity?.finishedOn)
        assertEquals(DateTime.parse("2018-08-17T18:09:28.610Z").toDate().time, activity?.finishedOn?.toDate()?.time)
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
        assertEquals(6, dbActivities.size)
        assertMedicationTaskReferenceActivity(dbActivities.first())
        assertMotivationSurveyReferenceActivity(dbActivities[2])
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
    fun query_testMedicationDateFound() {
        val date = DateTime.parse("2018-08-17T12:00:00.000-04:00")
        val dbActivities = activityDao.get("Medication", date)
        assertEquals(1, dbActivities.size)
        assertMedicationTaskReferenceActivity(dbActivities.first())
    }

    @Test
    fun query_testMedicationDateNotFound() {
        val date = DateTime.parse("2018-08-14T00:00:00.000-04:00")
        val dbActivities = activityDao.get("Medication", date)
        assertEquals(0, dbActivities.size)
    }

    @Test
    fun query_testMedicationDateStartEdgeCaseFound() {
        val date = DateTime.parse("2018-08-17T00:00:00.000-04:00")
        val dbActivities = activityDao.get("Medication", date)
        assertEquals(1, dbActivities.size)
        assertMedicationTaskReferenceActivity(dbActivities.first())
    }

    @Test
    fun query_testMedicationDateEndEdgeCaseFound() {
        val date = DateTime.parse("2018-08-18T00:00:00.000-04:00")
        val dbActivities = activityDao.get("Medication", date)
        assertEquals(1, dbActivities.size)
        assertMedicationTaskReferenceActivity(dbActivities.first())
    }

    @Test
    fun query_testMedicationDateEndEdgeCaseNotFound() {
        val date = DateTime.parse("2018-08-18T00:00:0.001-04:00")
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
        val date = DateTime.parse("2018-08-17T14:00:0.000-04:00")
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
        val date = DateTime.parse("2018-08-17T14:00:0.000-04:00")
        val dbActivities = activityDao.getAvailableOn(date)
        assertEquals(4, dbActivities.size)
        assertTaskContains(arrayOf("273c4518-7cb6-4496-b1dd-c0b5bf291b09:2018-08-17T00:00:00.000",
                "fe79d987-28a2-4ccd-bcf3-b3d07b925a6b:2018-08-17T00:00:00.000",
                "178ef89c-78b9-4861-b308-3dda0daf756d:2018-08-17T13:40:39.183",
                "e6fe761f-6187-4e8f-b659-bce4edc98f06:2018-08-17T13:40:39.183"), dbActivities)
    }

    fun assertTaskContains(guids: Array<String>, activityList: List<ScheduledActivityEntity>) {
        assertEquals(0, activityList.filter { !guids.contains(it.guid) }.size)
    }
}