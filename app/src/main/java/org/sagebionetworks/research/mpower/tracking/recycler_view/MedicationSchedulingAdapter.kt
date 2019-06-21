package org.sagebionetworks.research.mpower.tracking.recycler_view

import android.os.Handler
import android.os.Looper
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.mpower2_dosage_details.view.*
import org.sagebionetworks.research.mpower.R
import org.sagebionetworks.research.mpower.tracking.fragment.MedicationDayFragment
import org.sagebionetworks.research.mpower.tracking.view_model.logs.DosageItem
import org.sagebionetworks.research.mpower.tracking.view_model.logs.MedicationTimestamp
import org.sagebionetworks.research.sageresearch.extensions.localizedAndJoin
import org.slf4j.LoggerFactory
import org.threeten.bp.format.DateTimeFormatter

class MedicationAdapter(var items: MutableList<DosageItem>, val listener: Listener) : RecyclerView.Adapter<DosageViewHolder>() {

    private val LOGGER = LoggerFactory.getLogger(
            MedicationAdapter::class.java)

    fun addDosage(dosage: DosageItem) {
        items.add(0, dosage)
        Handler(Looper.getMainLooper()).post {
            notifyItemInserted(0)
        }
        if (items.size == 2) {
            Handler(Looper.getMainLooper()).post {
                notifyItemChanged(1)
            }
        }
    }

    fun removeDosage(dosage: DosageItem) {
        var position = items.indexOf(dosage)
        if (items.remove(dosage)) {
            Handler(Looper.getMainLooper()).post {
                notifyItemRemoved(position)
                notifyItemChanged(0)
            }
        }
    }

    fun updateDosage(dosage: DosageItem) {
        val index = items.indexOf(dosage)
        Handler(Looper.getMainLooper()).post {
            notifyItemChanged(index)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DosageViewHolder {
        return DosageViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.mpower2_dosage_details, parent, false))
    }

    override fun onBindViewHolder(dosageViewHolder: DosageViewHolder, position: Int) {
        val dosage = items[position]

        dosageViewHolder.removeButton.setOnClickListener{
            listener.onRemovePressed(dosage)
        }
        if (items.size < 2) {
            dosageViewHolder.removeButton.visibility = View.GONE
        } else {
            dosageViewHolder.removeButton.visibility = View.VISIBLE
        }

        if (dosageViewHolder.textWatcher != null) {
            dosageViewHolder.dosageText.removeTextChangedListener(dosageViewHolder.textWatcher)
        }
        dosageViewHolder.dosageText.setText(dosage.dosage)
        val textWatcher = object: TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                listener.onDosageTextChange(dosage, editable.toString())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        }
        dosageViewHolder.textWatcher = textWatcher
        dosageViewHolder.dosageText.addTextChangedListener(textWatcher)
        if (dosage.dosage.isEmpty()) {
            dosageViewHolder.dosageText.requestFocus()

        }

        dosageViewHolder.radioButtonAnytime.setOnCheckedChangeListener(null)
        dosageViewHolder.radioButtonAnytime.isChecked = dosage.isAnytime
        dosageViewHolder.radioButtonSchedule.isChecked = !dosage.isAnytime

        dosageViewHolder.radioButtonAnytime.setOnCheckedChangeListener { _, isChecked ->
            listener.onAnytimeSet(dosage, isChecked)
        }

        val visibility = if (dosage.isAnytime) View.GONE else View.VISIBLE
        dosageViewHolder.dayContainer.visibility = visibility
        dosageViewHolder.timeContainer.visibility = visibility

        if (dosage.timestamps.isEmpty() || dosage.isAnytime) {
            dosageViewHolder.timeText.text =
                    dosageViewHolder.timeText.resources.getString(R.string.medication_select_times)
        } else {
            val formatter = DateTimeFormatter.ofPattern("h:mm a")
            val times = mutableListOf<String>()
            for (timeStamp: MedicationTimestamp in dosage.timestamps) {
                times.add(formatter.format(timeStamp.localTimeOfDay))
            }

            dosageViewHolder.timeText.text = times.localizedAndJoin(dosageViewHolder.timeText.context)
        }
        dosageViewHolder.timeContainer.setOnClickListener { _ ->
            LOGGER.debug("Time clicked")
            listener.onTimeSelectionPressed(dosage)
        }

        dosageViewHolder.dayContainer.setOnClickListener { _ ->
            LOGGER.debug("Day container clicked")
            listener.onDaySelectionPressed(dosage)
        }

        dosageViewHolder.dayText.text = if (dosage.daysOfWeek.size == 7) {
            dosageViewHolder.dayText.context.getString(R.string.medication_schedule_everyday)
        } else if (dosage.daysOfWeek.isEmpty()) {
            dosageViewHolder.dayText.context.getString(R.string.medication_select_days)
        } else {
            MedicationDayFragment.getDayStringSet(dosageViewHolder.dayText.resources, dosage.daysOfWeek)
                    .localizedAndJoin(dosageViewHolder.dayText.context)
        }
    }
}

class DosageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val dosageText = view.dosage_edit_text
    val radioButtonAnytime = view.radio_button_any_time!!
    val radioButtonSchedule = view.radio_button_schedule!!
    //val checkbox = view.schedule_anytime!!
    val timeContainer = view.select_times!!
    val timeText = timeContainer.title
    val dayContainer = view.select_days!!
    val dayText = dayContainer.title
    val removeButton = view.dosage_remove
    var textWatcher :TextWatcher? = null
}

interface Listener {
    fun onTimeSelectionPressed(dosage: DosageItem)
    fun onDaySelectionPressed(dosage: DosageItem)
    fun onAnytimeSet(dosage: DosageItem, anytime: Boolean)
    fun onDosageTextChange(dosage: DosageItem, title: String)
    fun onRemovePressed(dosage: DosageItem)
}