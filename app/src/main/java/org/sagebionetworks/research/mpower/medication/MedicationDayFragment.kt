package org.sagebionetworks.research.mpower.medication

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.app.Dialog
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

class MedicationDayFragment: AppCompatDialogFragment() {

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
            args.putString(
                    ARG_SCHED_ID, id)
            args.putString(
                    ARG_NAME, name)
            args.putString(
                    ARG_TIME, time)
            args.putString(
                    ARG_DAYS, days)
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

        if(getArguments() != null) {
            schedId = arguments!!.getString(
                    ARG_SCHED_ID)
            name = arguments!!.getString(
                    ARG_NAME)
            time = arguments!!.getString(
                    ARG_TIME)
            days = arguments!!.getString(
                    ARG_DAYS)

            selectedDays = days.split(",").toMutableList()
            val size = selectedDays.size
            LOGGER.debug("Selected days on init: $size")
        } else {
            LOGGER.debug("No arguments found")
            name = "Boogie"
            time = "9:15 PM"
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View?
    {
        LOGGER.debug("onCreateView()")
        // Simply return the already inflated custom view
        //customView = inflater.inflate(R.layout.dialog_medication_day, container)
        return customView
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        LOGGER.debug("onCreateDialog()")
        // Inflate your view here
        var inflater: LayoutInflater = activity!!.layoutInflater
        customView = inflater.inflate(R.layout.dialog_medication_day, null)
        // Create Alert Dialog with your custom view
        return AlertDialog.Builder(context!!)
                //.setTitle(R.string.server_picker_dialog_title)
                .setView(customView)
                //.setNegativeButton(android.R.string.cancel, null)
                .create()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LOGGER.debug("onViewCreated()")

        // Perform remaining operations here. No null issues.
        day_selection_title.text = getString(R.string.medication_day_selection_title, name, time)
        var recycler = medication_day_recycler
        recycler.layoutManager = LinearLayoutManager(context)
        recycler.adapter = DayAdapter(getDays(), context!!)
        recycler.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        day_selection_back.setOnClickListener {
            _ -> dismiss()
        }

        day_selection_save.setOnClickListener {
            _ ->
                listener.onDaySelected(schedId, selectedDays.joinToString(","))
                dismiss()
        }


    }

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        try {
            listener = activity as DaySelectedListener
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString() + " must implement DaySelectedListener")
        }
    }

    fun getDays(): ArrayList<String> {
        return arrayListOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    }

    inner class DayAdapter(val items : ArrayList<String>, val context: Context) : RecyclerView.Adapter<DayViewHolder>() {

        // Gets the number of animals in the list
        override fun getItemCount(): Int {
            return items.size
        }

        // Inflates the item views
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
            return DayViewHolder(
                    LayoutInflater.from(context).inflate(layout.list_item_day, parent, false))
        }

        override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
            var text = items.get(position)
            holder.tvDay.text = text
            if(selectedDays.contains(text)) {
                holder.root.setBackgroundResource(R.color.royal300)
            } else {
                holder.root.setBackgroundResource(R.color.appWhite)
            }

            holder.root.setOnClickListener {
                view ->
                    //var text = items.get(position)
                    if(selectedDays.contains(text)) {
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


class DayViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    var root = view
    val tvDay = view.day_text
}