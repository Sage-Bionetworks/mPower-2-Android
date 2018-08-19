package org.sagebionetworks.research.mpower

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertNull
import org.joda.time.format.ISODateTimeFormat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.sagebionetworks.bridge.rest.RestUtils
import org.sagebionetworks.bridge.rest.model.ScheduledActivityListV4
import org.sagebionetworks.research.mpower.room.ResearchDatabase
import org.sagebionetworks.research.mpower.room.RoomScheduledActivity
import org.sagebionetworks.research.mpower.room.RoomScheduledActivityDao
import org.sagebionetworks.research.mpower.room.RoomTypeConverters
import java.io.IOException
import java.nio.charset.Charset

@RunWith(AndroidJUnit4::class)
class RoomScheduledActivityTests {

    private val DATE_FORMATTER = ISODateTimeFormat.dateTime().withOffsetParsed()

    private var database: ResearchDatabase? = null
    private var activityDao: RoomScheduledActivityDao? = null
    private var activityList: ScheduledActivityListV4? = null

    @Before
    fun setupRoom() {
        database = Room.inMemoryDatabaseBuilder(
                InstrumentationRegistry.getTargetContext(), ResearchDatabase::class.java)
                .allowMainThreadQueries().build()
        activityDao = database?.activitiesDao()

        activityList = RestUtils.GSON.fromJson(
                resourceAsString("mpower_activities.json"),
                ScheduledActivityListV4::class.java)
    }

    @After
    fun tearDownRoom() {
        database?.close()
    }

    @Test
    fun blankTest() {

        val list = activityList
        val dao = activityDao

        assertNotNull(list)
        assertNotNull(dao)

        if (list == null || dao == null) return

        val postInsertList = dao.getScheduledActivities()
        assertNotNull(postInsertList)
    }

    @Test
    fun serialization() {
        assertNotNull(activityList?.items?.first())
        activityList?.items?.first().let {
            val jsonString = RestUtils.GSON.toJson(it)
            val roomGson = RoomTypeConverters().roomGson
            val activity = roomGson.fromJson(jsonString, RoomScheduledActivity::class.java)
            assertNotNull(activity)
        }
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