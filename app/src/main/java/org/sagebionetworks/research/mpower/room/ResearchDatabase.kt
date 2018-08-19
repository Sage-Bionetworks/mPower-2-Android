package org.sagebionetworks.research.mpower.room

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters

@Database(entities = arrayOf(
        RoomSchemaReference::class,
        RoomSurveyReference::class,
        RoomTaskReference::class,
        RoomActivity::class,
        RoomScheduledActivity::class,
        RoomCompoundActivity::class),
        version = 1)
@TypeConverters(RoomTypeConverters::class)
abstract class ResearchDatabase : RoomDatabase() {
    abstract fun activitiesDao(): RoomScheduledActivityDao
}