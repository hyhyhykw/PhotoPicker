package com.hy.picker.utils;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

/**
 * Created time : 2018/8/27 15:36.
 *
 * @author HY
 */
public class DefaultItemDecoration extends RecyclerView.ItemDecoration {
    private final Drawable mDivider;
    private final int mDividerWidth;
    private final int mDividerHeight;
    private final List<Integer> mViewTypeList = new ArrayList<>();

    /**
     * @param color decoration line color.
     */
    public DefaultItemDecoration(@ColorInt int color) {
        this(color, 2, 2, -1);
    }

    /**
     * @param color           line color.
     * @param dividerWidth    line width.
     * @param dividerHeight   line height.
     * @param excludeViewType don't need to draw the ViewType of the item of the split line.
     */
    public DefaultItemDecoration(@ColorInt int color, int dividerWidth, int dividerHeight, int... excludeViewType) {
        mDivider = new ColorDrawable(color);
        mDividerWidth = dividerWidth;
        mDividerHeight = dividerHeight;
        for (int i : excludeViewType) {
            mViewTypeList.add(i);
        }
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        if (position < 0) return;

        RecyclerView.Adapter adapter = parent.getAdapter();
        if (null == adapter) return;
        if (mViewTypeList.contains(adapter.getItemViewType(position))) {
            outRect.set(0, 0, 0, 0);
            return;
        }

        int columnCount = getSpanCount(parent);
        int childCount = adapter.getItemCount();

        boolean firstRaw = isFirstRaw(position, columnCount);
        boolean lastRaw = isLastRaw(position, columnCount, childCount);
        boolean firstColumn = isFirstColumn(position, columnCount);
        boolean lastColumn = isLastColumn(position, columnCount);

        if (columnCount == 1) {
            if (firstRaw) {
                outRect.set(0, 0, 0, mDividerHeight / 2);
            } else if (lastRaw) {
                outRect.set(0, mDividerHeight / 2, 0, 0);
            } else {
                outRect.set(0, mDividerHeight / 2, 0, mDividerHeight / 2);
            }
        } else {
            if (firstRaw && firstColumn) { // right, bottom
                outRect.set(0, 0, mDividerWidth / 2, mDividerHeight / 2);
            } else if (firstRaw && lastColumn) { // left, right
                outRect.set(mDividerWidth / 2, 0, 0, mDividerHeight / 2);
            } else if (firstRaw) { // left, right, bottom
                outRect.set(mDividerWidth / 2, 0, mDividerWidth / 2, mDividerHeight / 2);
            } else if (lastRaw && firstColumn) { // top, right
                outRect.set(0, mDividerHeight / 2, mDividerWidth / 2, 0);
            } else if (lastRaw && lastColumn) { // left, top
                outRect.set(mDividerWidth / 2, mDividerHeight / 2, 0, 0);
            } else if (lastRaw) { // left, top, right
                outRect.set(mDividerWidth / 2, mDividerHeight / 2, mDividerWidth / 2, 0);
            } else if (firstColumn) { // top, right, bottom
                outRect.set(0, mDividerHeight / 2, mDividerWidth / 2, mDividerHeight / 2);
            } else if (lastColumn) { // left, top, bottom
                outRect.set(mDividerWidth / 2, mDividerHeight / 2, 0, mDividerHeight / 2);
            } else { // left, bottom.
                outRect.set(mDividerWidth / 2, mDividerHeight / 2, mDividerWidth / 2, mDividerHeight / 2);
            }
        }
    }

    private int getSpanCount(@NonNull RecyclerView parent) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            return ((GridLayoutManager) layoutManager).getSpanCount();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            return ((StaggeredGridLayoutManager) layoutManager).getSpanCount();
        }
        return 1;
    }

    private boolean isFirstRaw(int position, int columnCount) {
        return position < columnCount;
    }

    private boolean isLastRaw(int position, int columnCount, int childCount) {
        if (columnCount == 1)
            return position + 1 == childCount;
        else {
            int lastRawItemCount = childCount % columnCount;
            int rawCount = (childCount - lastRawItemCount) / columnCount + (lastRawItemCount > 0 ? 1 : 0);

            int rawPositionJudge = (position + 1) % columnCount;
            if (rawPositionJudge == 0) {
                int rawPosition = (position + 1) / columnCount;
                return rawCount == rawPosition;
            } else {
                int rawPosition = (position + 1 - rawPositionJudge) / columnCount + 1;
                return rawCount == rawPosition;
            }
        }
    }

    private boolean isFirstColumn(int position, int columnCount) {
        if (columnCount == 1)
            return true;
        return position % columnCount == 0;
    }

    private boolean isLastColumn(int position, int columnCount) {
        if (columnCount == 1)
            return true;
        return (position + 1) % columnCount == 0;
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        drawHorizontal(c, parent);
        drawVertical(c, parent);
    }

    public void drawHorizontal(@NonNull Canvas c,@NonNull RecyclerView parent) {
        c.save();
        RecyclerView.Adapter adapter = parent.getAdapter();
        if (null == adapter) return;
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            int childPosition = parent.getChildAdapterPosition(child);
            if (childPosition < 0) continue;

            if (mViewTypeList.contains(adapter.getItemViewType(childPosition)))
                continue;
            final int left = child.getLeft();
            final int top = child.getBottom();
            final int right = child.getRight();
            final int bottom = top + mDividerHeight;
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
        c.restore();
    }

    public void drawVertical(@NonNull Canvas c,@NonNull RecyclerView parent) {
        c.save();
        RecyclerView.Adapter adapter = parent.getAdapter();
        if (null == adapter) return;
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            int childPosition = parent.getChildAdapterPosition(child);
            if (childPosition < 0) continue;

            if (mViewTypeList.contains(adapter.getItemViewType(childPosition)))
                continue;
            final int left = child.getRight();
            final int top = child.getTop();
            final int right = left + mDividerWidth;
            final int bottom = child.getBottom();

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
        c.restore();
    }

}
