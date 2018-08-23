package org.sagebionetworks.research.mpower.room

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import org.joda.time.DateTime

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

/**
 * All interactions with the ScheduledActivityEntity table will be done through this interface.
 * Room auto-generates the implementations of these methods as well as checks the syntax of the SQL statements
 */
@Dao
interface RoomScheduledActivityDao {

    /// TODO: mdephillips 8/20/18 create the rest of the queries in iOS code repo file 'SBBScheduledActivity+Filters'
    /// TODO: mdephillips 8/20/18 should we return live data objects?

    /**
     * Get all the scheduled activities from the table
     * This may take a long time and use a lot of memory if the table is large, call with caution
     * @return all the scheduled activities in the table
     */
    @Query("SELECT * FROM scheduledactivityentity")
    fun getAll(): List<ScheduledActivityEntity>

    /**
     * Get all the scheduled activities with the identifier specified
     * @param identifier to use as a filter scheduled items
     * @return the list of scheduled activities
     */
    @Query("SELECT * FROM scheduledactivityentity WHERE " + RoomQuery.TASK_ID)
    fun get(identifier: String): List<ScheduledActivityEntity>

    /**
     * Get all the scheduled activities that have one of the identifiers specified in the task group
     * @param taskGroup a set of identifiers to use as a filter for scheduled items
     * @return the list of scheduled activities
     */
    @Query("SELECT * FROM scheduledactivityentity WHERE " + RoomQuery.TASK_GROUP_ID)
    fun get(taskGroup: Array<String>): List<ScheduledActivityEntity>

    /**
     * Get all the scheduled activities that are scheduled during and not expired yet during this date
     * @param date to filter the scheduled activities
     * @return the list of scheduled activities
     */
    @Query("SELECT * FROM scheduledactivityentity WHERE " + RoomQuery.AVAILABLE_DATE)
    fun get(date: DateTime): List<ScheduledActivityEntity>

    /**
     * Get all the scheduled activities that are available (not finished yet) and available schedule-wise at this date
     * @param date to filter the scheduled activities
     * @return the list of scheduled activities
     */
    @Query("SELECT * FROM scheduledactivityentity WHERE " + RoomQuery.NOT_FINISHED + " AND " + RoomQuery.AVAILABLE_DATE)
    fun getAvailableOn(date: DateTime): List<ScheduledActivityEntity>

    /**
     * Get all the scheduled activities that are available schedule-wise at this date and have the identifier
     * @param identifier to filter the scheduled activities
     * @param date to filter the scheduled activities
     * @return the list of scheduled activities
     */
    @Query("SELECT * FROM scheduledactivityentity WHERE " + RoomQuery.TASK_ID + " AND " + RoomQuery.AVAILABLE_DATE)
    fun get(identifier: String, date: DateTime): List<ScheduledActivityEntity>

    /**
     * @param roomScheduledActivity to insert into the database
     */
    @Insert
    fun insert(roomScheduledActivity: ScheduledActivityEntity)

    /**
     * @param roomScheduledActivityList to insert into the database
     */
    @Insert
    fun insert(roomScheduledActivityList: List<ScheduledActivityEntity>)

    /**
     * Deletes all rows in the table.  To be called on sign out or a cache clear.
     */
    @Query("DELETE FROM scheduledactivityentity")
    fun clear()
}

private class RoomQuery {
    companion object Constants {
        const val TASK_ID = "(activity_task_identifier=:identifier OR " +
                "activity_survey_identifier=:identifier OR " +
                "activity_compound_taskIdentifier=:identifier)"

        const val TASK_GROUP_ID = "(activity_task_identifier IN (:taskGroup) OR " +
                "activity_survey_identifier IN (:taskGroup) OR " +
                "activity_compound_taskIdentifier IN (:taskGroup))"

        const val NOT_FINISHED = "finishedOn IS NULL"
        const val FINISHED = "finishedOn IS NOT NULL"

        const val AVAILABLE_DATE =
                "((:date BETWEEN scheduledOn AND expiresOn) OR " +
                "(expiresOn IS NULL AND :date >= scheduledOn))"
    }
}