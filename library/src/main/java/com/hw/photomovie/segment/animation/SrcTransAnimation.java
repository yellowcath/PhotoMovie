package com.hw.photomovie.segment.animation;

import android.graphics.Rect;
import android.graphics.RectF;
import com.hw.photomovie.util.PhotoUtil;

/**
 * Created by huangwei on 2015/5/29.
 */
public class SrcTransAnimation extends SrcAnimation {

    private RectF mMaxShowRect = new RectF();
    /**
     * 转移开始前的初始位置
     */
    private RectF mInitRect = new RectF();

    private float mTransDisX, mTransDisY;
    private float mTransX, mTransY;

    public SrcTransAnimation(Rect srcRect, RectF srcShowRect, RectF dstRect, float transX, float transY) {
        super(srcRect, srcShowRect, dstRect);
        mTransX = transX;
        mTransY = transY;
        updateDstRect(dstRect);
    }

    @Override
    public RectF update(float progress) {
        mProgress = mInterpolator.getInterpolation(progress);
        mSrcShowRect.set(mInitRect);
        mSrcShowRect.offset(mTransDisX * mProgress, mTransDisY * mProgress);
        return mSrcShowRect;
    }

    @Override
    public void updateDstRect(RectF dstRect) {
        mDstRect = dstRect;
        mMaxShowRect.set(PhotoUtil.getCroppedRect(
                null,
                mSrcRect.width(),
                mSrcRect.height(),
                dstRect.width() * (1 + Math.abs(mTransX)),
                dstRect.height() * (1 + Math.abs(mTransY))));
        float w = mMaxShowRect.width() / (1 + Math.abs(mTransX));
        float h = mMaxShowRect.height() / (1 + Math.abs(mTransY));
        mSrcShowRect.set(0, 0, w, h);

        if (mTransX > 0) {
            mSrcShowRect.offsetTo(mMaxShowRect.left, mSrcShowRect.top);
        } else if (mTransX < 0) {
            mSrcShowRect.offsetTo(mMaxShowRect.right - mSrcShowRect.width(), mSrcShowRect.top);
        } else {
            mSrcShowRect.offsetTo(mMaxShowRect.centerX() - w / 2, mSrcShowRect.top);
        }

        if (mTransY > 0) {
            mSrcShowRect.offsetTo(mSrcShowRect.left, mMaxShowRect.top);
        } else if (mTransY < 0) {
            mSrcShowRect.offsetTo(mSrcShowRect.left, mMaxShowRect.bottom - mSrcShowRect.height());
        } else {
            mSrcShowRect.offsetTo(mSrcShowRect.left, mMaxShowRect.centerY() - h / 2);
        }

        mInitRect.set(mSrcShowRect);
        mTransDisX = mSrcShowRect.width() * mTransX;
        mTransDisY = mSrcShowRect.height() * mTransY;

        update(mProgress);
    }
}
