package com.hw.photomovie.segment.animation;

import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Created by huangwei on 2015/5/29.
 */
public abstract class SrcAnimation extends SegmentAnimation {

    protected Rect mSrcRect;
    protected RectF mSrcShowRect;
    protected RectF mDstRect;
    protected float mProgress;

    public SrcAnimation(Rect srcRect, RectF srcShowRect, RectF dstRect) {
        mSrcShowRect = srcShowRect;
        mSrcRect = srcRect;
        mDstRect = dstRect;
    }

    @Override
    public RectF update(float progress) {
        mProgress = progress;
        return mSrcShowRect;
    }

    public abstract void updateDstRect(RectF dstRect);
}
