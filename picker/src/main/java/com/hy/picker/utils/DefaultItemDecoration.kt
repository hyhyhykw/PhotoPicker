package com.hy.picker.utils

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import java.util.*

/**
 * Created time : 2018/8/27 15:36.
 *
 * @author HY
 */
class DefaultItemDecoration
/**
 * @param color           line color.
 * @param dividerWidth    line width.
 * @param dividerHeight   line height.
 * @param excludeViewType don't need to draw the ViewType of the item of the split line.
 */
@JvmOverloads constructor(@ColorInt color: Int,
                          private val dividerWidth: Int = 2,
                          private val dividerHeight: Int = 2,
                          vararg excludeViewType: Int = intArrayOf(-1)) : RecyclerView.ItemDecoration() {
    private val divider: Drawable
    private val viewTypeList = ArrayList(excludeViewType.asList())

    init {
        divider = ColorDrawable(color)
//        for (i in excludeViewType) {
//            viewTypeList.add(i)
//        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        if (position < 0) return

        val adapter = parent.adapter ?: return
        if (viewTypeList.contains(adapter.getItemViewType(position))) {
            outRect.set(0, 0, 0, 0)
            return
        }

        val columnCount = getSpanCount(parent)
        val childCount = adapter.itemCount

        val firstRaw = isFirstRaw(position, columnCount)
        val lastRaw = isLastRaw(position, columnCount, childCount)
        val firstColumn = isFirstColumn(position, columnCount)
        val lastColumn = isLastColumn(position, columnCount)

        if (columnCount == 1) {
            when {
                firstRaw -> outRect.set(0, 0, 0, dividerHeight / 2)
                lastRaw -> outRect.set(0, dividerHeight / 2, 0, 0)
                else -> outRect.set(0, dividerHeight / 2, 0, dividerHeight / 2)
            }
        } else {
            if (firstRaw && firstColumn) { // right, bottom
                outRect.set(0, 0, dividerWidth / 2, dividerHeight / 2)
            } else if (firstRaw && lastColumn) { // left, right
                outRect.set(dividerWidth / 2, 0, 0, dividerHeight / 2)
            } else if (firstRaw) { // left, right, bottom
                outRect.set(dividerWidth / 2, 0, dividerWidth / 2, dividerHeight / 2)
            } else if (lastRaw && firstColumn) { // top, right
                outRect.set(0, dividerHeight / 2, dividerWidth / 2, 0)
            } else if (lastRaw && lastColumn) { // left, top
                outRect.set(dividerWidth / 2, dividerHeight / 2, 0, 0)
            } else if (lastRaw) { // left, top, right
                outRect.set(dividerWidth / 2, dividerHeight / 2, dividerWidth / 2, 0)
            } else if (firstColumn) { // top, right, bottom
                outRect.set(0, dividerHeight / 2, dividerWidth / 2, dividerHeight / 2)
            } else if (lastColumn) { // left, top, bottom
                outRect.set(dividerWidth / 2, dividerHeight / 2, 0, dividerHeight / 2)
            } else { // left, bottom.
                outRect.set(dividerWidth / 2, dividerHeight / 2, dividerWidth / 2, dividerHeight / 2)
            }
        }
    }

    private fun getSpanCount(parent: RecyclerView): Int {
        val layoutManager = parent.layoutManager
        if (layoutManager is GridLayoutManager) {
            return layoutManager.spanCount
        } else if (layoutManager is StaggeredGridLayoutManager) {
            return layoutManager.spanCount
        }
        return 1
    }

    private fun isFirstRaw(position: Int, columnCount: Int): Boolean {
        return position < columnCount
    }

    private fun isLastRaw(position: Int, columnCount: Int, childCount: Int): Boolean {
        return if (columnCount == 1)
            position + 1 == childCount
        else {
            val lastRawItemCount = childCount % columnCount
            val rawCount = (childCount - lastRawItemCount) / columnCount + if (lastRawItemCount > 0) 1 else 0

            val rawPositionJudge = (position + 1) % columnCount
            if (rawPositionJudge == 0) {
                val rawPosition = (position + 1) / columnCount
                rawCount == rawPosition
            } else {
                val rawPosition = (position + 1 - rawPositionJudge) / columnCount + 1
                rawCount == rawPosition
            }
        }
    }

    private fun isFirstColumn(position: Int, columnCount: Int): Boolean {
        return if (columnCount == 1) true else position % columnCount == 0
    }

    private fun isLastColumn(position: Int, columnCount: Int): Boolean {
        return if (columnCount == 1) true else (position + 1) % columnCount == 0
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        try {
            drawHorizontal(c, parent)
            drawVertical(c, parent)
        } catch (e: Exception) {
        }

    }

    private fun drawHorizontal(c: Canvas, parent: RecyclerView) {
        c.save()
        val adapter = parent.adapter ?: return
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val childPosition = parent.getChildAdapterPosition(child)
            if (childPosition < 0) continue

            if (viewTypeList.contains(adapter.getItemViewType(childPosition)))
                continue
            val left = child.left
            val top = child.bottom
            val right = child.right
            val bottom = top + dividerHeight
            divider.setBounds(left, top, right, bottom)
            divider.draw(c)
        }
        c.restore()
    }

    private fun drawVertical(c: Canvas, parent: RecyclerView) {

        c.save()
        val adapter = parent.adapter ?: return
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val childPosition = parent.getChildAdapterPosition(child)
            if (childPosition < 0) continue

            if (viewTypeList.contains(adapter.getItemViewType(childPosition)))
                continue
            val left = child.right
            val top = child.top
            val right = left + dividerWidth
            val bottom = child.bottom

            divider.setBounds(left, top, right, bottom)
            divider.draw(c)
        }
        c.restore()
    }

}
