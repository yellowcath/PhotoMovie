package com.hw.photomovie.segment.layer;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.view.animation.AccelerateDecelerateInterpolator;
import com.hw.photomovie.opengl.GLESCanvas;
import com.hw.photomovie.segment.BitmapInfo;
import com.hw.photomovie.segment.animation.DstAnimation;
import com.hw.photomovie.segment.animation.DstTransAnimation;
import com.hw.photomovie.segment.animation.SrcAnimation;
import com.hw.photomovie.segment.animation.SrcLeftRightAnimation;
import com.hw.photomovie.segment.animation.SrcScaleAnimation;

/**
 * Created by huangwei on 2015/6/10.
 */
public class ScaleTransLayer extends MovieLayer {

    private SrcAnimation mSrcAnimation;
    private DstAnimation mTransAnimation;
    private float mFrom, mTo;
    private float mScaleRate = 0.5f;
    private BitmapInfo mBitmapInfo;
    /**
     * 图片的宽高比超过这个数，就不再展示缩放动画，而是左右移动的动画
     */
    private static final float TRANS_RATE = 1.2f;

    public ScaleTransLayer(float from, float to) {
        mFrom = from;
        mTo = to;
    }

    public void setScaleRate(float scaleRate) {
        mScaleRate = scaleRate;
    }

    @Override
    public void drawFrame(GLESCanvas canvas, float progress) {
        if (mSrcAnimation == null || mBitmapInfo == null || mBitmapInfo.bitmapTexture == null) {
            return;
        }
        if (progress < mScaleRate) {
            mSrcAnimation.update(progress * mScaleRate);
            canvas.drawTexture(mBitmapInfo.bitmapTexture, mBitmapInfo.srcShowRect, mViewprotRect);
        } else {
            progress = (progress - mScaleRate) / (1 - mScaleRate);
            canvas.drawTexture(mBitmapInfo.bitmapTexture, mBitmapInfo.srcShowRect, mTransAnimation.update(progress));
        }
    }

    @Override
    public int getRequiredPhotoNum() {
        return 1;
    }

    @Override
    public void prepare() {
        mBitmapInfo = (mBitmapInfos != null && mBitmapInfos.size() > 0) ? mBitmapInfos.get(0) : null;
        if (mBitmapInfo != null) {
            Bitmap bitmap = mBitmapInfo.bitmapTexture.getBitmap();
            float whRate = bitmap.getWidth() / (float) bitmap.getHeight();
            if (whRate > TRANS_RATE) {
                mSrcAnimation = new SrcLeftRightAnimation(mBitmapInfo.srcRect, mBitmapInfo.srcShowRect, mViewprotRect);
            } else {
                mSrcAnimation = new SrcScaleAnimation(mBitmapInfo.srcRect, mBitmapInfo.srcShowRect, mViewprotRect, mFrom, mTo);
            }
            mSrcAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        }
    }

    @Override
    public void release() {

    }

    @Override
    public void setViewprot(int l, int t, int r, int b) {
        super.setViewprot(l, t, r, b);
        if (mTransAnimation == null) {
            mTransAnimation = getRandomTransAnimation(mViewprotRect);
            mTransAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        } else {
            mTransAnimation.updateDstRect(mViewprotRect);
        }
        if (mSrcAnimation != null) {
            mSrcAnimation.updateDstRect(mViewprotRect);
        }
    }

    private DstTransAnimation getRandomTransAnimation(RectF dstRect) {
        int i = (int) (Math.random() * 4);
        switch (i) {
            case 0:
                return new DstTransAnimation(dstRect, 1, 0);
            case 1:
                return new DstTransAnimation(dstRect, -1, 0);
            case 2:
                return new DstTransAnimation(dstRect, 0, 1);
            case 3:
                return new DstTransAnimation(dstRect, 0, -1);
            default:
                return null;

        }
    }
}
