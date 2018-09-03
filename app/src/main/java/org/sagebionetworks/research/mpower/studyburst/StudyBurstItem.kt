package org.sagebionetworks.research.mpower.studyburst


data class StudyBurstItem(var title: String?) {
    var detail: String? = ""
    var activeImageResId: Int = 0
    var inactiveImageResId: Int = 0
    var completedImageResId: Int = 0
    var active: Boolean = false
    var completed: Boolean = false

}