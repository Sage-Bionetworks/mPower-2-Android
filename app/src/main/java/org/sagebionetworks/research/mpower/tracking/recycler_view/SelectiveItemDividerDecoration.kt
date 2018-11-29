package org.sagebionetworks.research.mpower.tracking.recycler_view

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ItemDecoration
import android.support.v7.widget.RecyclerView.State
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.View
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SelectiveItemDividerDecoration(private val divider: Drawable,
        private val selector: Selector)
    : ItemDecoration() {

    interface Selector {
        fun shouldDrawDivider(current : ViewHolder, next : ViewHolder?) : Boolean
    }

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(SelectiveItemDividerDecoration::class.java)
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: State) {
        val viewHolder = parent.getChildViewHolder(view)
        val index = parent.indexOfChild(view)
        val nextView = if (index < parent.childCount) parent.getChildAt(index) else null
        nextView?.let {
            val nextViewHolder = parent.getChildViewHolder(it)
            if (selector.shouldDrawDivider(viewHolder, nextViewHolder)) {
                outRect.set(0, 0, 0, divider.intrinsicHeight)
            } else {
                super.getItemOffsets(outRect, view, parent, state)
            }
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        for (i in 0 until parent.childCount - 1) {
            val view = parent.getChildAt(i)
            val viewHolder = parent.getChildViewHolder(view)
            val nextViewHolder = parent.getChildViewHolder(parent.getChildAt(i + 1))
            if (selector.shouldDrawDivider(viewHolder, nextViewHolder)) {
                drawDivider(c, parent, view)
            }
        }

        val view = parent.getChildAt(parent.childCount - 1)
        val viewHolder = parent.getChildViewHolder(view)
        if (selector.shouldDrawDivider(viewHolder, null)) {
            drawDivider(c, parent, view)
        }
    }

    private fun drawDivider(c : Canvas?, parent : RecyclerView, view : View) {
        val canvasUnwrapped = c ?: return
        val params = view.layoutParams as RecyclerView.LayoutParams
        val top = view.bottom + params.bottomMargin
        val bottom = top + divider.intrinsicHeight
        divider.setBounds(parent.left, top, parent.right, bottom)
        divider.draw(canvasUnwrapped)
    }
}