package com.hw.photomovie.segment;

import android.graphics.Color;
import com.hw.photomovie.opengl.GLESCanvas;
import com.hw.photomovie.segment.animation.SegmentAnimation;
import com.hw.photomovie.segment.animation.SrcScaleAnimation;
import com.hw.photomovie.segment.animation.SrcTransAnimation;

/**
 * Created by huangwei on 2015/5/29.
 */
public class ThawSegment extends SingleBitmapSegment {

    private SegmentAnimation mSrcAnimation;
    private int mType;

    public ThawSegment(int duration, int type) {
        mDuration = duration;
        mType = type;
    }

    @Override
    public void drawFrame(GLESCanvas canvas, float segmentRate) {
        if (!mDataPrepared) {
            return;
        }
        if (mBitmapInfo != null && mBitmapInfo.bitmapTexture != null && !mViewportRect.isEmpty()) {
            if(mSrcAnimation==null){
                createAnimation();
            }
            if (segmentRate < 0.2) {
                mSrcAnimation.update(0);
                float ratio = getValue(1, 0, 1 / 0.2f * segmentRate);
                if (mType == 0) {
                    ratio = 0;
                }
                canvas.drawMixed(mBitmapInfo.bitmapTexture, Color.WHITE, ratio, mBitmapInfo.srcShowRect, mViewportRect);
            } else if (segmentRate > 0.8) {
                float ratio = getValue(0, 1, (segmentRate - 0.8f) * 5f);
                canvas.drawMixed(mBitmapInfo.bitmapTexture, Color.WHITE, ratio, mBitmapInfo.srcShowRect, mViewportRect);
            } else {
                float ratio = getValue(0, 1, (segmentRate - 0.2f) * 1 / 0.6f);
                mSrcAnimation.update(ratio);
                canvas.drawTexture(mBitmapInfo.bitmapTexture, mBitmapInfo.srcShowRect, mViewportRect);
            }
        }
    }

    @Override
    protected void onDataPrepared() {
        super.onDataPrepared();
        createAnimation();
    }

    @Override
    public int getRequiredPhotoNum() {
        return 1;
    }

    protected void createAnimation() {
        if (!mBitmapInfo.srcRect.isEmpty() && !mViewportRect.isEmpty()) {
//            mSrcAnimation = new SrcScaleAnimation(mSrcRect, mShowSrcRect, mViewportRect, 1f, 0.5f);
            switch (mType) {
                case 0:
                    mSrcAnimation = new SrcScaleAnimation(mBitmapInfo.srcRect, mBitmapInfo.srcShowRect, mViewportRect, 1.0f, 1.1f);
                    break;
                case 1:
                    mSrcAnimation = new SrcTransAnimation(mBitmapInfo.srcRect, mBitmapInfo.srcShowRect, mViewportRect, -0.4f, 0);
                    break;
                case 2:
                    mSrcAnimation = new SrcTransAnimation(mBitmapInfo.srcRect, mBitmapInfo.srcShowRect, mViewportRect, 0.4f, 0f);
                    break;
            }
        }
    }

    private float getValue(float from, float to, float progress) {
        return from + (to - from) * progress;
    }
}
