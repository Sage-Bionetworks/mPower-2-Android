package org.sagebionetworks.research.mpower

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertNull
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.sagebionetworks.bridge.rest.RestUtils
import org.sagebionetworks.bridge.rest.model.ScheduleStatus
import org.sagebionetworks.bridge.rest.model.ScheduledActivityListV4
import org.sagebionetworks.research.mpower.room.ResearchDatabase
import org.sagebionetworks.research.mpower.room.RoomScheduledActivity
import org.sagebionetworks.research.mpower.room.RoomScheduledActivityDao
import org.sagebionetworks.research.mpower.room.RoomTypeConverters
import java.io.IOException
import java.nio.charset.Charset

@RunWith(AndroidJUnit4::class)
class RoomScheduledActivityTests {

    private var database: ResearchDatabase? = null
    private var activityDao: RoomScheduledActivityDao? = null
    private var activityList: ScheduledActivityListV4? = null
    private var roomActivityList: List<RoomScheduledActivity>? = null

    @Before @Test
    fun setupRoom() {
        database = Room.inMemoryDatabaseBuilder(
                InstrumentationRegistry.getTargetContext(), ResearchDatabase::class.java)
                .allowMainThreadQueries().build()
        assertNotNull(database)

        activityDao = database?.activitiesDao()
        assertNotNull(activityDao)

        activityList = RestUtils.GSON.fromJson(
                resourceAsString("mpower_activities.json"),
                ScheduledActivityListV4::class.java)
        assertNotNull(activityList)
        assertEquals(6, activityList?.items?.size)

        roomActivityList = RoomTypeConverters().fromScheduledActivityListV4(activityList)
        assertNotNull(roomActivityList)
        assertEquals(6, roomActivityList?.size)
    }

    @After
    fun tearDownRoom() {
        database?.close()
    }

    @Test
    fun serialization_testTaskReference() {
        assertMedicationTaskReferenceActivity(roomActivityList?.first())
    }

    fun assertMedicationTaskReferenceActivity(activity: RoomScheduledActivity?) {
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
    }

    @Test
    fun serialization_testSurveyReference() {
        assertMotivationSurveyReferenceActivity(roomActivityList?.get(2))
    }

    fun assertMotivationSurveyReferenceActivity(activity: RoomScheduledActivity?) {
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
    }

    @Test
    fun query_testAll() {
        reconfigureRoomDb()
        val dbActivities = activityDao?.getAll()
        assertNotNull(dbActivities)
        assertEquals(6, dbActivities?.size)
        assertMedicationTaskReferenceActivity(dbActivities?.first())
        assertMotivationSurveyReferenceActivity(dbActivities?.get(2))
    }

    @Test
    fun query_test() {
        reconfigureRoomDb()
        val dbActivities = activityDao?.getAll()
        assertNotNull(dbActivities)
        assertEquals(6, dbActivities?.size)
        assertMedicationTaskReferenceActivity(dbActivities?.first())
        assertMotivationSurveyReferenceActivity(dbActivities?.get(2))
    }

    @Test
    fun query_testTaskIdentifier() {
        reconfigureRoomDb()
        val dbActivities = activityDao?.get("Medication")
        assertNotNull(dbActivities)
        assertEquals(1, dbActivities?.size)
        assertMedicationTaskReferenceActivity(dbActivities?.first())
    }

    @Test
    fun query_testSurveyIdentifier() {
        reconfigureRoomDb()
        val dbActivities = activityDao?.get("Motivation")
        assertNotNull(dbActivities)
        assertEquals(1, dbActivities?.size)
        assertMotivationSurveyReferenceActivity(dbActivities?.first())
    }

    @Test
    fun query_testMedicationDateFound() {
        reconfigureRoomDb()
        val date = DateTime.parse("2018-08-17T12:00:00.000-04:00")
        val dbActivities = activityDao?.get("Medication", date)
        assertNotNull(dbActivities)
        assertEquals(1, dbActivities?.size)
        assertMedicationTaskReferenceActivity(dbActivities?.first())
    }

    @Test
    fun query_testMedicationDateNotFound() {
        reconfigureRoomDb()
        val date = DateTime.parse("2018-08-14T00:00:00.000-04:00")
        val dbActivities = activityDao?.get("Medication", date)
        assertNotNull(dbActivities)
        assertEquals(0, dbActivities?.size)
    }

    @Test
    fun query_testMedicationDateStartEdgeCaseFound() {
        reconfigureRoomDb()
        val date = DateTime.parse("2018-08-17T00:00:00.000-04:00")
        val dbActivities = activityDao?.get("Medication", date)
        assertNotNull(dbActivities)
        assertEquals(1, dbActivities?.size)
        assertMedicationTaskReferenceActivity(dbActivities?.first())
    }

    @Test
    fun query_testMedicationDateEndEdgeCaseFound() {
        reconfigureRoomDb()
        val date = DateTime.parse("2018-08-18T00:00:00.000-04:00")
        val dbActivities = activityDao?.get("Medication", date)
        assertNotNull(dbActivities)
        assertEquals(1, dbActivities?.size)
        assertMedicationTaskReferenceActivity(dbActivities?.first())
    }

    @Test
    fun query_testMedicationDateEndEdgeCaseNotFound() {
        reconfigureRoomDb()
        val date = DateTime.parse("2018-08-18T00:00:0.001-04:00")
        val dbActivities = activityDao?.get("Medication", date)
        assertNotNull(dbActivities)
        assertEquals(0, dbActivities?.size)
    }

    @Test
    fun query_testTaskGroup() {
        reconfigureRoomDb()
        val dbActivities = activityDao?.get(arrayOf("Medication", "Motivation"))
        assertNotNull(dbActivities)
        assertEquals(2, dbActivities?.size)
        assertMedicationTaskReferenceActivity(dbActivities?.first())
        assertMotivationSurveyReferenceActivity(dbActivities?.get(1))
    }

    @Test
    fun query_testAvailableOn() {
        reconfigureRoomDb()
        val date = DateTime.parse("2018-08-17T14:00:0.000-04:00")
        val dbActivities = activityDao?.getAvailableOn(date)
        assertNotNull(dbActivities)
        if (dbActivities == null) return
        assertEquals(4, dbActivities?.size)
        assertTaskContains(arrayOf("273c4518-7cb6-4496-b1dd-c0b5bf291b09:2018-08-17T00:00:00.000",
                "fe79d987-28a2-4ccd-bcf3-b3d07b925a6b:2018-08-17T00:00:00.000",
                "178ef89c-78b9-4861-b308-3dda0daf756d:2018-08-17T13:40:39.183",
                "e6fe761f-6187-4e8f-b659-bce4edc98f06:2018-08-17T13:40:39.183"), dbActivities)
    }

    fun reconfigureRoomDb() {
        assertNotNull(roomActivityList)
        activityDao?.clear()
        roomActivityList?.let {
            activityDao?.insert(it)
        }
    }

    fun assertTaskContains(guids: Array<String>, activityList: List<RoomScheduledActivity>) {
        assertEquals(0, activityList.filter { !guids.contains(it.guid) }.size)
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