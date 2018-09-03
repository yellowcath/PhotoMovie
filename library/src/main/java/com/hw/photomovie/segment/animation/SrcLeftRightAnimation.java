package com.hw.photomovie.segment.animation;

import android.graphics.Rect;
import android.graphics.RectF;
import com.hw.photomovie.util.PhotoUtil;

/**
 * Created by huangwei on 2015/6/10.
 * 从最左边扫到最右边
 */
public class SrcLeftRightAnimation extends SrcAnimation {
    private RectF mMaxShowRect = new RectF();
    /**
     * 转移开始前的初始位置
     */
    private RectF mInitRect = new RectF();

    private float mTransDisX;

    /**
     * @param srcRect
     * @param srcShowRect
     * @param dstRect
     */
    public SrcLeftRightAnimation(Rect srcRect, RectF srcShowRect, RectF dstRect) {
        super(srcRect, srcShowRect, dstRect);
        updateDstRect(dstRect);
    }

    @Override
    public RectF update(float progress) {
        mProgress = mInterpolator.getInterpolation(progress);
        mSrcShowRect.set(mInitRect);
        mSrcShowRect.offset(mTransDisX * mProgress, 0);
        return mSrcShowRect;
    }

    @Override
    public void updateDstRect(RectF dstRect) {
        mDstRect = dstRect;

        mMaxShowRect.set(PhotoUtil.getCroppedRect(
                null,
                mSrcRect.width(),
                mSrcRect.height(),
                dstRect.width(),
                dstRect.height()));

        float cy = mSrcRect.centerY();
        float h = mMaxShowRect.height();
        mInitRect.set(0, cy - h / 2f, mMaxShowRect.width(), cy + h / 2f);
        mTransDisX = mSrcRect.width() - mMaxShowRect.width();

        update(mProgress);
    }
}
