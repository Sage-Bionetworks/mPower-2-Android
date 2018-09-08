package org.sagebionetworks.research.mpower.studyburst

import android.view.View

interface MedicationItem {
    var type: Type
    fun bindView(view: View, item: MedicationItem)
}

enum class Type {
    DOSAGE, SCHEDULE, ADD
}

data class Dosage(var name: String?) : MedicationItem {
    override var type: Type = Type.DOSAGE
    override fun bindView(view: View, item: MedicationItem) {

    }
}
data class Schedule(var id: String) : MedicationItem {
    override var type: Type = Type.SCHEDULE
    var everday: Boolean = true
    var anytime: Boolean = false
    var time: String = "7:00 AM"
    var days: List<String> = arrayListOf()
    override fun bindView(view: View, item: MedicationItem) {

    }
}

data class Add(var label: String?) : MedicationItem {
    override var type: Type = Type.ADD
    override fun bindView(view: View, item: MedicationItem) {

    }
}