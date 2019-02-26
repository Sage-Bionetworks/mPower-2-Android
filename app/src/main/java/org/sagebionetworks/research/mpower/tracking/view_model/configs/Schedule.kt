package org.sagebionetworks.research.mpower.tracking.view_model.configs

import org.threeten.bp.LocalTime

data class Schedule(var id: String) {
    var everday: Boolean = true
    var anytime: Boolean = false
    var time: LocalTime = LocalTime.MIN
    var days: List<String> = arrayListOf()
}

