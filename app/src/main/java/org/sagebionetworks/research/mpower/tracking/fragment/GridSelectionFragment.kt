package org.sagebionetworks.research.mpower.tracking.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.dialog_medication_day.*
import kotlinx.android.synthetic.main.list_item_day.view.*
import org.sagebionetworks.research.mpower.R
import org.sagebionetworks.research.mpower.R.layout
import org.slf4j.LoggerFactory


class GridSelectionFragment : AppCompatDialogFragment() {

    private val LOGGER = LoggerFactory.getLogger(
            MedicationDayFragment::class.java)

    companion object {
        val ARG_ITEMS = "ARG_ITEMS"
        val ARG_TITLE = "ARG_TITLE"
        val ARG_SINGLE_SELECT = "ARG_SINGLE_SELECT"

        val TYPE_HEADER = 1
        val TYPE_ITEM = 2

        fun newInstance(name: String, items: ArrayList<SelectionItem>, singleSelect: Boolean): GridSelectionFragment {
            val fragment = GridSelectionFragment()
            val args = Bundle()
            args.putParcelableArrayList(ARG_ITEMS, items)
            args.putString(ARG_TITLE, name)
            args.putBoolean(ARG_SINGLE_SELECT, singleSelect)
            fragment.arguments = args
            return fragment
        }
    }


    lateinit var listener: ItemsSelectedListener
    lateinit var title: String
    lateinit var items: ArrayList<SelectionItem>
    var singleSelect = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            val args = arguments
            if (args != null) {
                items = args.getParcelableArrayList<SelectionItem>(ARG_ITEMS) ?: ArrayList()
                title = args.getString(ARG_TITLE) ?: ""
                singleSelect = args.getBoolean(ARG_SINGLE_SELECT, false)
            } else {
                LOGGER.warn("No arguments found")
            }
        } else {
            items = savedInstanceState.getParcelableArrayList<SelectionItem>(ARG_ITEMS) ?: ArrayList()
            title = savedInstanceState.getString(ARG_TITLE) ?: ""
            singleSelect = savedInstanceState.getBoolean(ARG_SINGLE_SELECT)
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

        day_selection_title.text = title
        if (singleSelect) {
            day_selection_message.visibility = View.GONE
        }
        var recycler = medication_day_recycler
        val adapter = DayAdapter(items, requireContext())
        recycler.adapter = adapter

        val gridLayoutManager = androidx.recyclerview.widget.GridLayoutManager(context, 2)
        gridLayoutManager.setSpanSizeLookup(object : androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (adapter.getItemViewType(position) == TYPE_HEADER) {
                    2
                } else {
                    1
                }
            }
        })
        recycler.layoutManager = gridLayoutManager

        day_selection_back.setOnClickListener { _ ->
            dismiss()
        }

        day_selection_save.setOnClickListener { _ ->
            val selectedItems = items.filter { it.isSelected }
            val identifiers = selectedItems.map { it.identifier }
            listener.onItemsSelected(identifiers)
            dismiss()
        }
        updateSaveButtonEnabled()
    }

    private fun updateSaveButtonEnabled() {
        day_selection_save.isEnabled = items.firstOrNull { it.isSelected } != null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(ARG_ITEMS, items)
        outState.putString(ARG_TITLE, title)
        outState.putBoolean(ARG_SINGLE_SELECT, singleSelect)
    }

    inner class DayAdapter(val items: List<SelectionItem>, val context: Context) :
            androidx.recyclerview.widget.RecyclerView.Adapter<ItemViewHolder>() {

        override fun getItemCount(): Int {
            return items.size
        }

        override fun getItemViewType(position: Int): Int {
            if (items[position].isHeader) {
                return TYPE_HEADER
            }
            return TYPE_ITEM
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val layoutId = if (viewType == TYPE_HEADER) {
                layout.selection_item_header
            } else {
                layout.list_item_day
            }

            return ItemViewHolder(
                    LayoutInflater.from(context).inflate(layoutId, parent, false))
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val selectionItem = items[position]
            holder.tvDay.text = selectionItem.displayText
            if (getItemViewType(position) == TYPE_ITEM) {
                if (selectionItem.isSelected) {
                    holder.tvDay.isSelected = true
                    holder.tvDay.setCompoundDrawablesWithIntrinsicBounds(resources.getDrawable(R.drawable.ic_check_black_16dp), null, null, null)
                } else {
                    holder.tvDay.isSelected = false
                    holder.tvDay.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)

                }

                holder.tvDay.setOnClickListener { view ->
                    if (!singleSelect || !selectionItem.isSelected) {
                        if (singleSelect) {
                            items.forEach {
                                it.isSelected = false
                                Handler(Looper.getMainLooper()).post {
                                    notifyItemChanged(items.indexOf(it))
                                }
                            }
                        }

                        selectionItem.isSelected = !selectionItem.isSelected

                        Handler(Looper.getMainLooper()).post {
                            notifyItemChanged(position)
                        }
                        updateSaveButtonEnabled()
                    }
                }
            }
        }
    }
}

@Parcelize
data class SelectionItem(val displayText: String, val identifier: String, var isSelected: Boolean, val isHeader: Boolean) : Parcelable {}


interface ItemsSelectedListener {
    fun onItemsSelected(selectedItemIds: List<String>)
}

class ItemViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
    val tvDay: TextView = view.findViewById(R.id.day_text)
}
