/*
 * BSD 3-Clause License
 *
 * Copyright 2018  Sage Bionetworks. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1.  Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2.  Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * 3.  Neither the name of the copyright holder(s) nor the names of any contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission. No license is granted to the trademarks of
 * the copyright holders even if such marks are included in this software.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.sagebionetworks.research.sageresearch.viewmodel

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import junit.framework.Assert.assertNull
import org.junit.AfterClass
import org.junit.BeforeClass
import org.sagebionetworks.bridge.rest.RestUtils
import org.sagebionetworks.bridge.rest.model.ScheduledActivityListV4
import org.sagebionetworks.research.sageresearch.dao.room.EntityTypeConverters
import org.sagebionetworks.research.sageresearch.dao.room.ResearchDatabase
import org.sagebionetworks.research.sageresearch.dao.room.ScheduledActivityEntity
import org.sagebionetworks.research.sageresearch.dao.room.ScheduledActivityEntityDao
import java.io.IOException
import java.nio.charset.Charset
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.support.test.runner.AndroidJUnit4
import org.junit.runner.RunWith
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
        return resourceSet.associateBy( {it}, {
            testResource(it)
        } )
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