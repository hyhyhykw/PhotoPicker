package com.hy.picker.utils;

import android.content.Context;
import android.graphics.Rect;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.hy.picker.R;

/**
 * Created time : 2018/12/8 8:49.
 *
 * @author HY
 */
public class MyGridItemDecoration extends RecyclerView.ItemDecoration {

    private int dp1;

    public MyGridItemDecoration(Context context) {
        dp1 = context.getResources().getDimensionPixelOffset(R.dimen.picker_item_divider);
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        if (position < 0) return;
        switch (position % 3) {
            case 0:
                outRect.set(0, 0, dp1 * 2, dp1 * 3);
                break;
            case 1:
                outRect.set(dp1, 0, dp1, dp1 * 3);
                break;
            case 2:
                outRect.set(dp1 * 2, 0, 0, dp1 * 3);
                break;
        }

    }
}
