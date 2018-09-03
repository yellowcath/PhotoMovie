package com.hw.photomovie.segment.animation;

import android.graphics.RectF;

/**
 * Created by huangwei on 2015/6/10.
 */
public class DstTransAnimation extends DstAnimation {

    private float mTransX;
    private float mTransY;
    private RectF mProgressDstRect = new RectF();
    private float mProgress;

    public DstTransAnimation(RectF dstRect, float transX, float transY) {
        super(dstRect);
        mTransX = transX;
        mTransY = transY;
    }

    @Override
    public RectF update(float progress) {
        mProgress = mInterpolator.getInterpolation(progress);
        mProgressDstRect.set(mDstRect);
        mProgressDstRect.offset(mProgress * mDstRect.width() * mTransX, mProgress * mDstRect.height() * mTransY);
        return mProgressDstRect;
    }

    @Override
    public void updateDstRect(RectF dstRect) {
        super.updateDstRect(dstRect);
        update(mProgress);
    }
}
