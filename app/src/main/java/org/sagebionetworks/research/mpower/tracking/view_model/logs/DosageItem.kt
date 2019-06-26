package org.sagebionetworks.research.mpower.tracking.view_model.logs

import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

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
         * @property dailySet that represents all the possible values for [DosageItem.daysOfWeek]
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




    fun copy(clearLoggedDate: Boolean): DosageItem {
        val timestampsCopy = HashSet<MedicationTimestamp>()
        for (timestamp in timestamps) {
            timestampsCopy.add(timestamp.copy(clearLoggedDate))
        }
        val daysOfWeekCopy = HashSet<Int>()
        daysOfWeekCopy.addAll(daysOfWeek)
        return DosageItem(dosage, daysOfWeekCopy, timestampsCopy)

    }

}
