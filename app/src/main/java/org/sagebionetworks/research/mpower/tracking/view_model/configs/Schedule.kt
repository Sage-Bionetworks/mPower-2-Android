package org.sagebionetworks.research.mpower.tracking.view_model.configs

import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter

data class Schedule(
        /**
         * The time of the day as a string with the format "HH:mm".
         */
        var timeOfDay: String? = null,
        /**
         * The days of the week to include in the schedule. By default, this will be set to no days.
         */
        var daysOfWeek: Set<Int> = dailySet) {

    companion object {
        // Depending on the Locale, and the Calendar type, this may not be 7.
        // However, per iOS compatibility, their's is also hard-coded to 7 for now.
        const val totalDaysOfTheWeek = 7

        /**
         * @property timeOfDayFormatter used to convert back and forth from [LocalTime] to timeOfDay [String]
         */
        val timeOfDayFormatter = DateTimeFormatter.ofPattern("HH:mm")

        /**
         * @property dailySet that represents all the possible values for [Schedule.daysOfWeek]
         */
        val dailySet: Set<Int> = (1..totalDaysOfTheWeek).toSet()

        /**
         * @return the default initial schedule
         */
        fun defaultSchedule(): Schedule {
            return Schedule()
        }
    }

    /**
     * Secondary constructor to initialize the class easier with LocalTime
     * @param timeOfDay that will be converted and stored as a [String]
     * @param daysOfWeek set of days that are in the schedule
     */
    constructor(timeOfDay: LocalTime, daysOfWeek: Set<Int> = dailySet):
            this(timeOfDayFormatter.format(timeOfDay), daysOfWeek)

    /**
     * @param localTime to convert into the Schedule data class' timeOfDay [String]
     */
    fun setLocalTimeOfDay(localTime: LocalTime) {
        timeOfDay = timeOfDayFormatter.format(localTime)
    }

    /**
     * @return timeOfDay [String] converted to a LocalTime.  Returns null if timeOfDay is also null.
     */
    fun getLocalTimeOfDay(): LocalTime? {
        timeOfDay?.let {
            return LocalTime.parse(it, timeOfDayFormatter)
        }
        return null
    }

    /**
     * @return Is this a daily scheduled item?
     */
    fun isDaily(): Boolean {
        return daysOfWeek.size == totalDaysOfTheWeek
    }

    /**
     * @return if the timeOfDay val is null and schedule can be taken at anytime
     */
    fun isAnytime(): Boolean {
        return timeOfDay == null
    }
}

