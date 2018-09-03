package com.hw.photomovie.segment.animation;

import android.graphics.RectF;

/**
 * Created by huangwei on 2015/6/10.
 */
public class DstAnimation extends SegmentAnimation {

    public RectF mDstRect = new RectF();

    public DstAnimation(RectF dstRect) {
        mDstRect.set(dstRect);
    }

    @Override
    public RectF update(float progress) {
        return mDstRect;
    }

    public void updateDstRect(RectF dstRect){
        mDstRect.set(dstRect);
    }
}
