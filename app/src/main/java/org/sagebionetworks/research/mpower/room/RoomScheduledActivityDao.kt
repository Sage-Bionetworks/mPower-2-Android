package org.sagebionetworks.research.mpower.room

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query

@Dao
interface RoomScheduledActivityDao {
    @Query("SELECT * FROM roomscheduledactivity")
    fun getScheduledActivities(): List<RoomScheduledActivity>

    @Insert
    fun insert(roomScheduledActivity: RoomScheduledActivity)
}