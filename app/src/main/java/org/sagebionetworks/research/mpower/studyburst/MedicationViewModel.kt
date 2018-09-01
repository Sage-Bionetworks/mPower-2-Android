package org.sagebionetworks.research.mpower.studyburst

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import org.sagebionetworks.research.mpower.R
import org.slf4j.LoggerFactory

class MedicationViewModel : ViewModel() {

    private val LOGGER = LoggerFactory.getLogger(MedicationViewModel::class.java)

    private val title = MutableLiveData<String>()
    private var items = MutableLiveData<List<MedicationItem>>()

    fun init() {
        title.value = "Sinamet"

        // TODO: remove when we have real data
        val list = arrayListOf<MedicationItem>()
        var sb1 = Dosage("Finger Tapping")
        list.add(sb1)
        var sb2 = Schedule(1)
        list.add(sb2)
        var sb3 = Add("Walk and Stand")
        list.add(sb3)

        items.value = list
    }

    fun getTitle(): LiveData<String> {
        return title
    }

    fun getItems(): LiveData<List<MedicationItem>> {
        return items
    }

    fun addSchedule() {
        var list: MutableList<MedicationItem> = items.value!!.toMutableList()
        var len = list.size - 1
        LOGGER.debug("Adding schedule with id: $len")
        //list.add(Schedule(len ))
        list.add(len-1, Schedule(len))
        items.value = list
    }


}