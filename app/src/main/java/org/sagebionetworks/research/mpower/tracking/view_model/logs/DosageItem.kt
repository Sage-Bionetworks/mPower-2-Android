package org.sagebionetworks.research.mpower.tracking.view_model.logs

import com.google.auto.value.AutoValue
import com.google.common.collect.Collections2
import com.google.common.collect.ImmutableSet
import com.google.gson.Gson
import com.google.gson.TypeAdapter

import org.threeten.bp.Instant
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter

import java.util.ArrayList
import java.util.Collections
import java.util.HashSet

data class DosageItem (


        /**
         * @return A string answer value for the dosage.
         */
        var dosage: String,

        /**
         * The days of the week to take medication
         */
         val daysOfWeek: MutableSet<Int>,

        /**
         * @return The timestamps of when to take the medication as well as logging when "taken".
         */
        val timestamps: MutableSet<MedicationTimestamp>) {

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

    }




    val isAnytime: Boolean
        get() {
            if (timestamps.isEmpty()) {
                return true
            } else {
                for (timestamp in timestamps) {
                    if (timestamp.timeOfDay != null) {
                        return false
                    }
                }
                return true
            }
        }

    val isDaily: Boolean
        get() = daysOfWeek.size == 7




    fun copyAndClearLoggedDate(): DosageItem {
        val timestamps = HashSet<MedicationTimestamp>()
        for (timestamp in timestamps) {
            timestamps.add(timestamp.copyAndClearLoggedDate())
        }
        val daysOfWeek = HashSet<Int>()
        daysOfWeek.addAll(daysOfWeek)
        return DosageItem(dosage, daysOfWeek, timestamps)

    }

}
