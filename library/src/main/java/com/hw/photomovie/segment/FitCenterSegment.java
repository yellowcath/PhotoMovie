package com.hw.photomovie.segment;

import android.graphics.Matrix;
import android.graphics.RectF;
import com.hw.photomovie.opengl.GLESCanvas;
import com.hw.photomovie.util.PhotoUtil;
import com.hw.photomovie.util.ScaleType;

/**
 * Created by huangwei on 2018/9/4 0004.
 */
public class FitCenterSegment extends SingleBitmapSegment {

    private Matrix mScaleMatrix = new Matrix();
    protected RectF mDstRect = new RectF();
    protected RectF mScaleRect = new RectF();
    private int mBackgroundColor;

    public FitCenterSegment(int duration) {
        super(duration);
        mDefaultScaleType = ScaleType.FIT_CENTER;
    }

    @Override
    public void drawFrame(GLESCanvas canvas, float segmentProgress) {
        if (!mDataPrepared) {
            return;
        }
        drawBackground(canvas);
        drawContent(canvas, 1f);
    }

    protected void drawBackground(GLESCanvas canvas) {
        if (mBackgroundColor != 0) {
            canvas.fillRect(0, 0, mViewportRect.width(), mViewportRect.height(), mBackgroundColor);
        }
    }

    protected void drawContent(GLESCanvas canvas, float scale) {
        if (mBitmapInfo != null && mBitmapInfo.makeTextureAvailable(canvas)) {
            if (scale != 1f) {
                mScaleMatrix.setScale(scale, scale, mDstRect.centerX(), mDstRect.centerY());
                mScaleMatrix.mapRect(mScaleRect, mDstRect);
                canvas.drawTexture(mBitmapInfo.bitmapTexture, mBitmapInfo.srcShowRect, mScaleRect);
            } else {
                canvas.drawTexture(mBitmapInfo.bitmapTexture, mBitmapInfo.srcShowRect, mDstRect);
            }
        }
    }

    @Override
    public void setViewport(int l, int t, int r, int b) {
        super.setViewport(l, t, r, b);
        calDstRect();
    }

    @Override
    protected void onDataPrepared() {
        super.onDataPrepared();
        calDstRect();
    }

    private void calDstRect() {
        if (mBitmapInfo == null || mViewportRect.width() == 0 || mViewportRect.height() == 0) {
            return;
        }
        PhotoUtil.getFitCenterRect(mDstRect, (int) mBitmapInfo.srcShowRect.width(), (int) mBitmapInfo.srcShowRect.height(), (int) mViewportRect.width(), (int) mViewportRect.height());
    }

    public FitCenterSegment setBackgroundColor(int backgroundColor) {
        mBackgroundColor = backgroundColor;
        return this;
    }

    @Override
    public void onRelease() {
        super.onRelease();
    }

    public RectF getDstRect() {
        return mDstRect;
    }

    int getBackgroundColor() {
        return mBackgroundColor;
    }
}
