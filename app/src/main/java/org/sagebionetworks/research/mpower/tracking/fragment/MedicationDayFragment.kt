package org.sagebionetworks.research.mpower.tracking.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import kotlinx.android.synthetic.main.dialog_medication_day.day_selection_back
import kotlinx.android.synthetic.main.dialog_medication_day.day_selection_save
import kotlinx.android.synthetic.main.dialog_medication_day.day_selection_title
import kotlinx.android.synthetic.main.dialog_medication_day.medication_day_recycler
import kotlinx.android.synthetic.main.list_item_day.view.day_text
import org.sagebionetworks.research.mpower.R
import org.sagebionetworks.research.mpower.R.layout
import org.sagebionetworks.research.mpower.R2.string.medication_schedule_everyday
import org.sagebionetworks.research.mpower.tracking.view_model.logs.DosageItem
import org.slf4j.LoggerFactory
import java.util.Arrays

class MedicationDayFragment : AppCompatDialogFragment() {

    private val LOGGER = LoggerFactory.getLogger(
            MedicationDayFragment::class.java)

    companion object {
        val ARG_NAME = "ARG_NAME"
        val ARG_TIME = "ARG_TIME"
        val ARG_DAYS = "ARG_DAYS"

        fun newInstance(name: String, time: String, days: Set<Int>): MedicationDayFragment {
            val fragment = MedicationDayFragment()
            val args = Bundle()
            args.putString(ARG_NAME, name)
            args.putString(ARG_TIME, time)
            args.putIntegerArrayList(ARG_DAYS, ArrayList(days))
            fragment.arguments = args
            return fragment
        }

        fun getDays(resources: Resources): ArrayList<String> {
            return ArrayList(Arrays.asList(*resources.getStringArray(R.array.days_of_the_week)))
        }

        fun getDaysShort(resources: Resources): ArrayList<String> {
            return ArrayList(Arrays.asList(*resources.getStringArray(R.array.days_of_the_week_short)))
        }

        fun getDayStringSet(resources: Resources, indexSet: Set<Int>): Set<String> {
            return getDaysShort(resources).filterIndexed { index, _ ->
                // index + 1 is because days start on index 1 (sunday), there is no day 0
                indexSet.contains((index + 1))
            }.toSet()
        }
    }

    lateinit var listener: DaySelectedListener
    lateinit var selectedDays: MutableSet<Int>
    lateinit var name: String
    lateinit var time: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            val args = arguments
            if (args != null) {
                name = args.getString(ARG_NAME) ?: ""
                time = args.getString(ARG_TIME) ?: ""
                selectedDays = mutableSetOf<Int>()
                selectedDays.addAll(args.getIntegerArrayList(ARG_DAYS) ?: mutableSetOf())
            } else {
                LOGGER.warn("No arguments found")
                name = "Default"
                time = "9:15 PM"
            }
        } else {
            name = savedInstanceState.getString(ARG_NAME) ?: ""
            time = savedInstanceState.getString(ARG_TIME) ?: ""
            selectedDays = mutableSetOf<Int>()
            selectedDays.addAll(savedInstanceState.getIntegerArrayList(ARG_DAYS)!!)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        LOGGER.debug("onCreateView()")
        return inflater.inflate(R.layout.dialog_medication_day, null)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        LOGGER.debug("onCreateDialog()")
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog

    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LOGGER.debug("onViewCreated()")

        day_selection_title.text = getString(R.string.medication_day_selection_title, name)
        var recycler = medication_day_recycler
        recycler.layoutManager = androidx.recyclerview.widget.GridLayoutManager(context, 2)
        val dayStrings = getDays(resources)
        dayStrings.add(resources.getString(medication_schedule_everyday))
        recycler.adapter = DayAdapter(ArrayList((0..(dayStrings.size-1)).toList()), dayStrings, context!!)

        day_selection_back.setOnClickListener { _ ->
            dismiss()
        }

        day_selection_save.setOnClickListener { _ ->
            listener.onDaySelected(name, selectedDays.toSet())
            dismiss()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(ARG_NAME, name)
        outState.putString(ARG_TIME, time)
        outState.putIntegerArrayList(ARG_DAYS, ArrayList(selectedDays))
    }

    inner class DayAdapter(val items: ArrayList<Int>, val dayStrings: List<String>, val context: Context) :
            androidx.recyclerview.widget.RecyclerView.Adapter<DayViewHolder>() {

        override fun getItemCount(): Int {
            return items.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
            return DayViewHolder(
                    LayoutInflater.from(context).inflate(layout.list_item_day, parent, false))
        }

        override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
            holder.tvDay.text = dayStrings[items[position]]
            val adjustedPosition = position + 1
            if ((selectedDays.contains(adjustedPosition) && selectedDays.size < 7) || (selectedDays.size == 7 && position == 7)) {
                holder.tvDay.isSelected = true
                holder.tvDay.setCompoundDrawablesWithIntrinsicBounds(resources.getDrawable( R.drawable.ic_check_black_16dp), null, null ,null)
            } else {
                holder.tvDay.isSelected = false
                holder.tvDay.setCompoundDrawablesWithIntrinsicBounds(null, null, null ,null)

            }

            holder.tvDay.setOnClickListener { view ->
                if (position == 7) {
                    if (selectedDays.size != 7) {
                        selectedDays.addAll(DosageItem.dailySet)
                        holder.tvDay.isSelected = true

                        Handler(Looper.getMainLooper()).post {
                            notifyDataSetChanged()
                        }
                    }

                } else {
                    if (selectedDays.size == 7) {
                        selectedDays.clear()
                    }

                    if (selectedDays.contains(adjustedPosition)) {
                        selectedDays.remove(adjustedPosition)
                        holder.tvDay.isSelected = false
                    } else {
                        selectedDays.add(adjustedPosition)
                        holder.tvDay.isSelected = true
                    }
                    Handler(Looper.getMainLooper()).post {
                        notifyDataSetChanged()
                    }
                }
            }
        }
    }
}

interface DaySelectedListener {
    fun onDaySelected(scheduleName: String, days: Set<Int>)
}

class DayViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
    var root = view
    val tvDay = view.day_text
}