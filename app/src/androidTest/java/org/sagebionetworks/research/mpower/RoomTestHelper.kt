package org.sagebionetworks.research.mpower

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import junit.framework.Assert.assertNull
import org.junit.*
import org.junit.runner.*
import org.sagebionetworks.bridge.rest.RestUtils
import org.sagebionetworks.bridge.rest.model.ScheduledActivityListV4
import org.sagebionetworks.research.sageresearch.dao.room.EntityTypeConverters
import org.sagebionetworks.research.sageresearch.dao.room.ResearchDatabase
import org.sagebionetworks.research.sageresearch.dao.room.ScheduledActivityEntity
import org.sagebionetworks.research.sageresearch.dao.room.ScheduledActivityEntityDao
import java.io.IOException
import java.nio.charset.Charset
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
abstract class RoomTestHelper {

    companion object {
        lateinit var database: ResearchDatabase
        lateinit var activityDao: ScheduledActivityEntityDao

        @BeforeClass
        @JvmStatic fun setup() {
            database = Room.inMemoryDatabaseBuilder(
                    InstrumentationRegistry.getTargetContext(), ResearchDatabase::class.java)
                    .allowMainThreadQueries().build()

            activityDao = database.scheduleDao()
        }

        @AfterClass
        @JvmStatic fun teardown() {
            database.close()
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(InterruptedException::class)
    fun <T> getValue(liveData: LiveData<T>): T {
        val data = arrayOfNulls<Any>(1)
        val latch = CountDownLatch(1)
        val observer = object : Observer<T> {
            override fun onChanged(o: T?) {
                data[0] = o
                latch.countDown()
                liveData.removeObserver(this)
            }
        }
        liveData.observeForever(observer)
        latch.await(1, TimeUnit.SECONDS)

        return data[0] as T
    }
}

object TestResourceHelper {
    fun testResourceMap(resourceSet: Set<String>): Map<String, List<ScheduledActivityEntity>> {
        return resourceSet.associateBy( {it}, {testResource(it)} )
    }

    internal fun testResource(filename: String): List<ScheduledActivityEntity> {
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
        val testList = RestUtils.GSON.fromJson(json, ScheduledActivityListV4::class.java)
        return EntityTypeConverters().fromScheduledActivityListV4(testList) ?: ArrayList()
    }
}