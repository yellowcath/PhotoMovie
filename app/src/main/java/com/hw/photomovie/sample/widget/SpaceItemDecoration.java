package com.hw.photomovie.sample.widget;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.hw.photomovie.sample.R;
import com.hw.photomovie.util.AppResources;

/**
 * Created by huangwei on 2018/9/9.
 */
public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

    private int mSpace;

    public SpaceItemDecoration(int space) {
        mSpace = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int pos = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewAdapterPosition();
        if (pos == 0) {
            outRect.left = mSpace;
        }
        outRect.right = mSpace;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);

    }
}
