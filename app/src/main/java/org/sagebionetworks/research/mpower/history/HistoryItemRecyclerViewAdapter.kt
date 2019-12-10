/*
 * BSD 3-Clause License
 *
 * Copyright 2019  Sage Bionetworks. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1.  Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2.  Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * 3.  Neither the name of the copyright holder(s) nor the names of any contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission. No license is granted to the trademarks of
 * the copyright holders even if such marks are included in this software.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.sagebionetworks.research.mpower.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_historyitem.view.item_detail
import kotlinx.android.synthetic.main.fragment_historyitem.view.item_text
import org.sagebionetworks.research.mpower.R
import org.sagebionetworks.research.mpower.history.HistoryItemRecyclerViewAdapter.ViewHolder
import org.sagebionetworks.research.mpower.history.HistoryItemType.DATE_BUCKET
import org.sagebionetworks.research.mpower.history.HistoryItemType.TIME_BUCKET

class HistoryItemRecyclerViewAdapter()
    : PagedListAdapter<HistoryItem, ViewHolder>(HISTORY_ITEM_COMPARATOR) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflator = LayoutInflater.from(parent.context)
        when (viewType) {
            VIEW_TYPE_DATE_BUCKET -> {
                return ViewHolder(inflator.inflate(R.layout.fragment_historyitem_day, parent, false))
            }
            VIEW_TYPE_TIME_BUCKET -> {
                return ViewHolder(inflator.inflate(R.layout.fragment_historyitem_time, parent, false))
            }
            else ->{
                return ViewHolder(inflator.inflate(R.layout.fragment_historyitem, parent, false))
            }
        }
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.mTitleView.text = item?.title(holder.mView.resources)
        holder.mDetailView.text = item?.details(holder.mView.resources)
        if (item != null) {
            holder.imageView?.setImageResource(item.iconId)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)?.type) {
            DATE_BUCKET -> VIEW_TYPE_DATE_BUCKET
            TIME_BUCKET -> VIEW_TYPE_TIME_BUCKET
            else -> VIEW_TYPE_TITLE_DETAILS
        }
    }


    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mTitleView: TextView = mView.item_text
        val mDetailView: TextView = mView.item_detail
        //Load using findVieWById so we can handle rows that don't have icons
        val imageView: ImageView? = mView.findViewById(R.id.icon)

        override fun toString(): String {
            return super.toString() + " '" + mTitleView.text + "'"
        }
    }

    companion object {

        const val VIEW_TYPE_DATE_BUCKET = 0
        const val VIEW_TYPE_TIME_BUCKET = 1
        const val VIEW_TYPE_TITLE_DETAILS = 2

        val HISTORY_ITEM_COMPARATOR = object : DiffUtil.ItemCallback<HistoryItem>() {
            override fun areContentsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean =
                    oldItem == newItem

            override fun areItemsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
                return oldItem == newItem
            }

        }

    }

}
