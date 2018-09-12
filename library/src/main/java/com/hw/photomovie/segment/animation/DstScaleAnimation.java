package com.hw.photomovie.segment.animation;

import android.graphics.Matrix;
import android.graphics.RectF;

/**
 * Created by huangwei on 2015/6/10.
 */
public class DstScaleAnimation extends DstAnimation {

    private float mFromScale;
    private float mToScale;
    private RectF mProgressDstRect = new RectF();
    private float mProgress;
    private Matrix mScaleMatrix = new Matrix();

    public DstScaleAnimation(RectF dstRect, float fromScale, float toScale) {
        super(dstRect);
        mFromScale = fromScale;
        mToScale = toScale;
    }

    @Override
    public RectF update(float progress) {
        mProgress = mInterpolator.getInterpolation(progress);
        mProgressDstRect.set(mDstRect);
        float scale = mFromScale + (mToScale - mFromScale) * mProgress;
        mScaleMatrix.setScale(scale, scale, mDstRect.centerX(), mDstRect.centerY());
        mScaleMatrix.mapRect(mProgressDstRect);
        return mProgressDstRect;
    }

    @Override
    public void updateDstRect(RectF dstRect) {
        super.updateDstRect(dstRect);
        update(mProgress);
    }
}
