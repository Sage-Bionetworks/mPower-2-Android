package org.sagebionetworks.research.mpower.studyburst

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.medication_add.view.*
import kotlinx.android.synthetic.main.medication_dosage.view.*
import kotlinx.android.synthetic.main.medication_schedule.view.schedule_anytime
import kotlinx.android.synthetic.main.medication_schedule.view.schedule_everyday
import kotlinx.android.synthetic.main.medication_schedule.view.schedule_time
import org.sagebionetworks.research.mpower.R
import org.slf4j.LoggerFactory

class MedicationAdapter(var items : List<MedicationItem>, val context: Context, val listener: Listener)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val LOGGER = LoggerFactory.getLogger(MedicationAdapter::class.java)

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        lateinit var holder: RecyclerView.ViewHolder
        when(viewType) {
            0 ->
                holder = DosageViewHolder(LayoutInflater.from(context).inflate(R.layout.medication_dosage, parent, false))
            1 ->
                holder = ScheduleViewHolder(LayoutInflater.from(context).inflate(R.layout.medication_schedule, parent, false))
            2 ->
                holder = AddViewHolder(LayoutInflater.from(context).inflate(R.layout.medication_add, parent, false))
        }
        return holder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items.get(position)
        when(item.type) {
            Type.DOSAGE -> {
                var dh = holder as DosageViewHolder
                dh.bindView(item as Dosage)
                dh.input.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        var len = p0?.length
                        when(len) {
                            0 -> listener.enableNext(false)
                            else -> listener.enableNext(true)
                        }
                    }

                    override fun afterTextChanged(p0: Editable?) {}
                })
            }
            Type.SCHEDULE -> {
                var sh = holder as ScheduleViewHolder
                sh.bindView(item as Schedule)
                sh.checkbox.setOnCheckedChangeListener{ buttonView, isChecked ->
                    if(isChecked) {
                        sh.everyday.visibility = View.GONE
                        sh.time.visibility = View.GONE
                    } else {
                        sh.everyday.visibility = View.VISIBLE
                        sh.time.visibility = View.VISIBLE
                    }
                }
            }

            Type.ADD -> {
                var ah = holder as AddViewHolder
                ah.bindView(item as Add)
                ah.button.setOnClickListener {
                    view ->
                        LOGGER.debug("Add button clicked.")
                        listener.addSchedule()
                }
            }

        }

    }

    override fun getItemViewType(position: Int): Int {

        val type = when (items[position].type) {
            Type.DOSAGE -> 0
            Type.SCHEDULE -> 1
            Type.ADD -> 2
        }
        return type
    }

}

class DosageViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    val input = view.dosage_input

    fun bindView(dosage: Dosage) {

    }

}

class ScheduleViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    val checkbox = view.schedule_anytime
    val time = view.schedule_time
    val everyday = view.schedule_everyday
    fun bindView(schedule: Schedule) {

    }
}

class AddViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    val button = view.schedule_add


    fun bindView(add: Add) {

    }
}

interface Listener {
    fun addSchedule()
    fun enableNext(enable: Boolean)
}