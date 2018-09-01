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
data class Schedule(var id: Int) : MedicationItem {
    override var type: Type = Type.SCHEDULE
    override fun bindView(view: View, item: MedicationItem) {

    }
}

data class Add(var label: String?) : MedicationItem {
    override var type: Type = Type.ADD
    override fun bindView(view: View, item: MedicationItem) {

    }
}