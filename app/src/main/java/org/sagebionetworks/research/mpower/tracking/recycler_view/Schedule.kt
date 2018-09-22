package org.sagebionetworks.research.mpower.tracking.recycler_view

data class Schedule(var id: String) {
    var everday: Boolean = true
    var anytime: Boolean = false
    var time: String = "7:00 AM"
    var days: List<String> = arrayListOf()
}

