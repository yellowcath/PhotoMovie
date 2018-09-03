package com.hw.photomovie.segment.animation;

import android.graphics.Rect;
import android.graphics.RectF;
import com.hw.photomovie.util.PhotoUtil;

/**
 * Created by huangwei on 2015/5/29.
 */
public class SrcScaleAnimation extends SrcAnimation {

    private float mFrom;
    private float mTo;

    private RectF mMaxShowRect = new RectF();

    private float mFromW, mFromH, mToW, mToH;
    private float mX, mY;

    public SrcScaleAnimation(Rect srcRect, RectF srcShowRect, RectF dstRect, float from, float to) {
        super(srcRect, srcShowRect, dstRect);
        mFrom = from;
        mTo = to;
        updateDstRect(dstRect);
    }

    @Override
    public RectF update(float progress) {
        mProgress = mInterpolator.getInterpolation(progress);
        float w = mFromW + (mToW - mFromW) * mProgress;
        float h = mFromH + (mToH - mFromH) * mProgress;
        mSrcShowRect.set(mX - w / 2, mY - h / 2, mX + w / 2, mY + h / 2);
        return mSrcShowRect;
    }

    @Override
    public void updateDstRect(RectF dstRect) {
        mDstRect = dstRect;
        mMaxShowRect.set(PhotoUtil.getCroppedRect(null, mSrcRect.width(), mSrcRect.height(), dstRect.width(), dstRect.height()));
        mX = mSrcRect.centerX();
        mY = mSrcRect.centerY();
        if (mFrom >= mTo) {
            mToW = mMaxShowRect.width();
            mToH = mMaxShowRect.height();
            mFromH = mToH * (mTo / mFrom);
            mFromW = mToW * (mTo / mFrom);
        } else {
            mFromW = mMaxShowRect.width();
            mFromH = mMaxShowRect.height();
            mToH = mFromH * (mFrom / mTo);
            mToW = mFromW * (mFrom / mTo);
        }

        update(mProgress);
    }
}
