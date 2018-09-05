package com.hw.photomovie.segment.animation;

import android.graphics.Rect;
import android.graphics.RectF;

/**
 * 作用在图片本身Rect的动画，可用于缩放平移
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
