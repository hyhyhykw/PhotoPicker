package com.hy.picker.utils

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.hy.picker.R

/**
 * Created time : 2018/12/8 8:49.
 *
 * @author HY
 */
class MyGridItemDecoration(context: Context) : RecyclerView.ItemDecoration() {

    private val dp1 = context.resources.getDimensionPixelOffset(R.dimen.picker_item_divider)

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        if (position < 0) return
        when (position % 4) {
            0 -> outRect.set(0, 0, dp1 * 3, dp1)
            1 -> outRect.set(dp1, 0, dp1 * 2, dp1 * 3)
            2 -> outRect.set(dp1 * 2, 0, dp1, dp1 * 3)
            3 -> outRect.set(dp1 * 3, 0, 0, dp1 * 3)
        }

    }
}
