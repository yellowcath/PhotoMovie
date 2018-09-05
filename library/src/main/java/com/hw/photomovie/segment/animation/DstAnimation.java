package com.hw.photomovie.segment.animation;

import android.graphics.RectF;

/**
 * 作用在输出Rect的动画，可用于缩放平移
 */
public class DstAnimation extends SegmentAnimation {

    public RectF mDstRect ;

    public DstAnimation(RectF dstRect) {
        mDstRect = dstRect;
    }

    @Override
    public RectF update(float progress) {
        return mDstRect;
    }

    public void updateDstRect(RectF dstRect){
        mDstRect = dstRect;
    }
}
