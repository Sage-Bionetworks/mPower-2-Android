package org.sagebionetworks.research.mpower.studyburst

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import org.sagebionetworks.research.mpower.R

class StudyBurstViewModel : ViewModel() {

    private val title = MutableLiveData<String>()
    private val message = MutableLiveData<String>()
    private val expires = MutableLiveData<String>()
    private val dayNumber = MutableLiveData<Int>()
    private var items = MutableLiveData<List<StudyBurstItem>>()

    fun init() {
        title.value = "Great start!"
        message.value = "Complete the rest of todays activities within the next hour in order to complete the study burst for Day 12."
        expires.value = "00:53:04"
        dayNumber.value = 9

        // TODO: remove when we have real data
        val list = arrayListOf<StudyBurstItem>()
        var sb1 = StudyBurstItem()
        sb1.title = "Finger Tapping"
        sb1.detail = "1 minute"
        sb1.activeImageResId = R.drawable.ic_finger_tapping
        sb1.inactiveImageResId = R.drawable.ic_finger_tapping_inactive
        sb1.completedImageResId = R.drawable.ic_finger_tapping_done
        sb1.active = true
        sb1.completed = false
        list.add(sb1)
        var sb2 = StudyBurstItem()
        sb2.title = "Tremor Test"
        sb2.detail = "4 minutes"
        sb2.activeImageResId = R.drawable.ic_tremor
        sb2.inactiveImageResId = R.drawable.ic_tremor_inactive
        sb2.completedImageResId = R.drawable.ic_tremor_done
        sb2.active = true
        sb2.completed = true
        list.add(sb2)
        var sb3 = StudyBurstItem()
        sb3.title = "Walk and Stand"
        sb3.detail = "6 minutes"
        sb3.activeImageResId = R.drawable.ic_walk_and_stand
        sb3.inactiveImageResId = R.drawable.ic_walk_and_stand_inactive
        sb3.completedImageResId = R.drawable.ic_walk_and_stand_done
        sb3.active = false
        sb3.completed = false
        list.add(sb3)
        var sb4 = StudyBurstItem()
        sb4.title = "Cognition"
        sb4.detail = "3 minutes"
        sb4.activeImageResId = R.drawable.ic_cognitive
        sb4.inactiveImageResId = R.drawable.ic_cognitive_inactive
        sb4.completedImageResId = R.drawable.ic_cognitive_done
        sb4.active = true
        sb4.completed = false
        list.add(sb4)
        items.value = list
    }

    fun getMessage(): LiveData<String> {
        return message
    }

    fun getTitle(): LiveData<String> {
        return title
    }

    fun getExpires(): LiveData<String> {
        return expires
    }

    fun getItems(): LiveData<List<StudyBurstItem>> {
        return items
    }

    fun getDayCount(): LiveData<Int> {
        return dayNumber
    }

}