package org.sagebionetworks.research.mpower.tracking.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.support.v7.app.AppCompatDialogFragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.dialog_medication_day.day_selection_back
import kotlinx.android.synthetic.main.dialog_medication_day.day_selection_save
import kotlinx.android.synthetic.main.dialog_medication_day.day_selection_title
import kotlinx.android.synthetic.main.dialog_medication_day.medication_day_recycler
import kotlinx.android.synthetic.main.list_item_day.view.day_text
import org.sagebionetworks.research.mpower.R
import org.sagebionetworks.research.mpower.R.layout
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

        fun getDayStringSet(resources: Resources, indexSet: Set<Int>): Set<String> {
            return getDays(resources).filterIndexed { index, _ ->
                // index + 1 is because days start on index 1 (sunday), there is no day 0
                indexSet.contains((index + 1))
            }.toSet()
        }
    }

    lateinit var customView: View
    lateinit var listener: DaySelectedListener
    lateinit var selectedDays: MutableList<Int>
    lateinit var name: String
    lateinit var time: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var days: ArrayList<String>? = null
        if (savedInstanceState == null) {
            val args = arguments
            if (args != null) {
                name = args.getString(ARG_NAME) ?: ""
                time = args.getString(ARG_TIME) ?: ""
                selectedDays = args.getIntegerArrayList(ARG_DAYS) ?: mutableListOf()
            } else {
                LOGGER.warn("No arguments found")
                name = "Default"
                time = "9:15 PM"
            }
        } else {
            name = savedInstanceState.getString(ARG_NAME) ?: ""
            time = savedInstanceState.getString(ARG_TIME) ?: ""
            selectedDays = savedInstanceState.getIntegerArrayList(ARG_DAYS) ?: mutableListOf()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        LOGGER.debug("onCreateView()")
        return customView
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        LOGGER.debug("onCreateDialog()")

        customView = LayoutInflater.from(activity)
                .inflate(R.layout.dialog_medication_day, null)

        return AlertDialog.Builder(context)
                .setView(customView)
                .create()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LOGGER.debug("onViewCreated()")

        day_selection_title.text = getString(R.string.medication_day_selection_title, name, time)
        var recycler = medication_day_recycler
        recycler.layoutManager = LinearLayoutManager(context)
        val dayStrings = getDays(resources)
        recycler.adapter = DayAdapter(ArrayList((0..(dayStrings.size-1)).toList()), dayStrings, context!!)
        recycler.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

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
            RecyclerView.Adapter<DayViewHolder>() {

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
            if (selectedDays.contains(adjustedPosition)) {
                holder.root.setBackgroundResource(R.color.royal300)
            } else {
                holder.root.setBackgroundResource(R.color.appWhite)
            }

            holder.root.setOnClickListener { view ->
                if (selectedDays.contains(adjustedPosition)) {
                    selectedDays.remove(adjustedPosition)
                    view.setBackgroundResource(R.color.appWhite)
                } else {
                    selectedDays.add(adjustedPosition)
                    view.setBackgroundResource(R.color.royal300)
                }
            }
        }
    }
}

interface DaySelectedListener {
    fun onDaySelected(scheduleName: String, days: Set<Int>)
}

class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var root = view
    val tvDay = view.day_text
}