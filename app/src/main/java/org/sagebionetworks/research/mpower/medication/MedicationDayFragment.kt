package org.sagebionetworks.research.mpower.medication

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
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
        val ARG_SCHED_ID = "ARG_SCHED_ID"
        val ARG_NAME = "ARG_NAME"
        val ARG_TIME = "ARG_TIME"
        val ARG_DAYS = "ARG_DAYS"

        fun newInstance(id: String, name: String, time: String, days: String): MedicationDayFragment {
            val fragment = MedicationDayFragment()
            val args = Bundle()
            args.putString(ARG_SCHED_ID, id)
            args.putString(ARG_NAME, name)
            args.putString(ARG_TIME, time)
            args.putString(ARG_DAYS, days)
            fragment.arguments = args
            return fragment
        }
    }

    lateinit var customView: View
    lateinit var listener: DaySelectedListener
    var selectedDays: MutableList<String> = arrayListOf()

    lateinit var schedId: String
    lateinit var name: String
    lateinit var time: String
    lateinit var days: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            if (arguments != null) {
                schedId = arguments!!.getString(ARG_SCHED_ID)
                name = arguments!!.getString(ARG_NAME)
                time = arguments!!.getString(ARG_TIME)
                days = arguments!!.getString(ARG_DAYS)

                selectedDays = days.split(",").toMutableList()
                val size = selectedDays.size
                LOGGER.debug("Selected days on init: $size")
            } else {
                LOGGER.warn("No arguments found")
                name = "Default"
                time = "9:15 PM"
            }
        } else {
            schedId = savedInstanceState.getString(ARG_SCHED_ID)
            name = savedInstanceState.getString(ARG_NAME)
            time = savedInstanceState.getString(ARG_TIME)
            days = savedInstanceState.getString(ARG_DAYS)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        LOGGER.debug("onCreateView()")
        return customView
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        LOGGER.debug("onCreateDialog()")

        var inflater: LayoutInflater = activity!!.layoutInflater
        customView = inflater.inflate(R.layout.dialog_medication_day, null)

        return AlertDialog.Builder(context!!)
                .setView(customView)
                .create()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LOGGER.debug("onViewCreated()")

        day_selection_title.text = getString(R.string.medication_day_selection_title, name, time)
        var recycler = medication_day_recycler
        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = DayAdapter(getDays(), context!!)
        recycler.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        day_selection_back.setOnClickListener { _ ->
            dismiss()
        }

        day_selection_save.setOnClickListener { _ ->
            listener.onDaySelected(schedId, selectedDays.joinToString(","))
            dismiss()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as DaySelectedListener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement DaySelectedListener")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(ARG_SCHED_ID, schedId)
        outState.putString(ARG_NAME, name)
        outState.putString(ARG_TIME, time)
        outState.putString(ARG_DAYS, days)
    }

    fun getDays(): ArrayList<String> {
        return ArrayList<String>(Arrays.asList(*resources.getStringArray(R.array.days_of_the_week)))
    }

    inner class DayAdapter(val items: ArrayList<String>, val context: Context) :
            RecyclerView.Adapter<DayViewHolder>() {

        override fun getItemCount(): Int {
            return items.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
            return DayViewHolder(
                    LayoutInflater.from(context).inflate(layout.list_item_day, parent, false))
        }

        override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
            var text = items.get(position)
            holder.tvDay.text = text
            if (selectedDays.contains(text)) {
                holder.root.setBackgroundResource(R.color.royal300)
            } else {
                holder.root.setBackgroundResource(R.color.appWhite)
            }

            holder.root.setOnClickListener { view ->
                if (selectedDays.contains(text)) {
                    selectedDays.remove(text)
                    view.setBackgroundResource(R.color.appWhite)
                } else {
                    selectedDays.add(text)
                    view.setBackgroundResource(R.color.royal300)
                }
            }
        }
    }
}

interface DaySelectedListener {
    fun onDaySelected(id: String, days: String)
}

class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var root = view
    val tvDay = view.day_text
}