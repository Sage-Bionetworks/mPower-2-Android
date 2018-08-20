package org.sagebionetworks.research.mpower.room

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import org.joda.time.DateTime

@Dao
interface RoomScheduledActivityDao {
    @Query("SELECT * FROM roomscheduledactivity")
    fun getAll(): List<RoomScheduledActivity>

    @Query("SELECT * FROM roomscheduledactivity WHERE " + RoomQuery.TASK_ID)
    fun get(identifier: String): List<RoomScheduledActivity>

    @Query("SELECT * FROM roomscheduledactivity WHERE " + RoomQuery.AVAILABLE_DATE)
    fun get(date: DateTime): List<RoomScheduledActivity>

    @Query("SELECT * FROM roomscheduledactivity WHERE " + RoomQuery.TASK_ID + " AND " + RoomQuery.AVAILABLE_DATE)
    fun get(identifier: String, date: DateTime): List<RoomScheduledActivity>

    @Insert
    fun insert(roomScheduledActivity: RoomScheduledActivity)

    @Insert
    fun insert(roomScheduledActivityList: List<RoomScheduledActivity>)

    @Query("DELETE FROM roomscheduledactivity")
    fun clear()
}

private class RoomQuery {
    companion object Constants {
        const val TASK_ID = "(activity_task_identifier=:identifier OR " +
                "activity_survey_identifier=:identifier OR " +
                "activity_compound_taskIdentifier=:identifier)"

        const val NOT_FINISHED = "finishedOn IS NULL"
        const val FINISHED = "finishedOn IS NOT NULL"

        const val AVAILABLE_DATE = ":date BETWEEN scheduledOn AND expiresOn"
    }
}