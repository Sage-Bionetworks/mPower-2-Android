package org.sagebionetworks.research.mpower.medication

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import org.slf4j.LoggerFactory

class MedicationViewModel : ViewModel() {

    private val LOGGER = LoggerFactory.getLogger(
            MedicationViewModel::class.java)

    private val title = MutableLiveData<String>()
    private var items = MutableLiveData<List<MedicationItem>>()

    private var addButton = Add(
            "add")

    fun init() {
        title.value = "Sinamet"

        // TODO: remove when we have real data
        val list = arrayListOf<MedicationItem>()
        var dosage = Dosage(
                "dosage")
        list.add(dosage)
        var schedule = Schedule(
                "1")
        schedule.time = "06:30 AM"
        schedule.everday = false
        schedule.days = arrayListOf("Monday", "Wednesday")
        list.add(schedule)

        list.add(addButton)

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
        list.add(len, Schedule(
                len.toString()))
        items.value = list
    }

    fun showAddSchedule(show: Boolean) {
        LOGGER.debug("showAddSchedule(): $show")
        var list: MutableList<MedicationItem> = items.value!!.toMutableList()
        if(show) {
            list.add(addButton)
        } else {
            list.remove(addButton)
        }
        items.value = list
    }

    fun setScheduleDays(id: String, days: List<String>) {
        LOGGER.debug("setScheduleDays(): $days, $id")
        var list: List<MedicationItem> = items.value ?: listOf()
        for(item in list) {
            when(item.type) {
                Type.SCHEDULE -> {
                    var sched: Schedule = item as Schedule
                    if(sched.id == id) {
                        LOGGER.debug("Found schedule: $id")
                        if(days.size < 7) {
                            sched.everday = false
                            sched.days = days
                        } else {
                            sched.everday = true
                            sched.days = arrayListOf()
                        }
                    }
                }
            }
        }
        items.value = list
    }

    fun deleteOtherSchedules(schedule: Schedule) {
        var list: MutableList<MedicationItem> = items.value!!.toMutableList()
        val iterator = list.iterator()
        while(iterator.hasNext()){
            val item = iterator.next()
            when(item.type) {
                Type.SCHEDULE -> {
                    var sched: Schedule = item as Schedule
                    if (sched.id != schedule.id) {
                        iterator.remove()
                    }
                }
            }
        }
//        for(item in list) {
//            when(item.type) {
//                Type.SCHEDULE -> {
//                    var sched: Schedule = item as Schedule
//                    if(schedule.id == sched.id) {
//                        LOGGER.debug("Found schedule: $schedule.id")
//                    } else {
//                        LOGGER.debug("Removing item...")
//                        list.remove(item)
//                    }
//                }
//            }
//        }

        items.value = list
    }

}