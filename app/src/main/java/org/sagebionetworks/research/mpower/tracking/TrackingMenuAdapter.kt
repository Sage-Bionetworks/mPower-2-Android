/*
 * BSD 3-Clause License
 *
 * Copyright 2021  Sage Bionetworks. All rights reserved.
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

package org.sagebionetworks.research.mpower.tracking

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.sagebionetworks.research.mpower.R
import org.sagebionetworks.research.mpower.tracking.TrackingMenuAdapter.ViewHolder
import java.util.ArrayList

data class TrackingMenuItem(
        val textResourceId: Int,
        val imageResourceId: Int,
        val isComplete: Boolean)

interface TrackingMenuItemAdapterListener {
    fun onTaskClicked(textResourceId: Int)
}

class TrackingMenuAdapter(
    public var dataSet: ArrayList<TrackingMenuItem>,
    private val listener: TrackingMenuItemAdapterListener) :
        RecyclerView.Adapter<ViewHolder>() {

    override fun getItemCount() = dataSet.size

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(viewGroup.context).inflate(
                R.layout.tracking_menu_list_item, viewGroup, false))
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = dataSet[position]
        viewHolder.textView.setText(item.textResourceId)
        viewHolder.imageView.setImageResource(item.imageResourceId)
        viewHolder.checkmark.visibility = if (item.isComplete) { View.VISIBLE } else { View.GONE }
        viewHolder.container.setOnClickListener {
            listener.onTaskClicked(item.textResourceId)
        }
    }

    open class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val container: View = view.findViewById(R.id.list_item_container)
        val textView: TextView = view.findViewById(R.id.list_item_text_view)
        val imageView: ImageView = view.findViewById(R.id.list_item_image_view)
        val checkmark: ImageView = view.findViewById(R.id.list_item_check_mark)
    }
}