package com.hw.photomovie.segment;

import android.graphics.Bitmap;
import com.hw.photomovie.opengl.BitmapTexture;
import com.hw.photomovie.opengl.GLESCanvas;
import com.hw.photomovie.segment.layer.GaussianBlurLayer;
import com.hw.photomovie.util.AppResources;
import com.hw.photomovie.util.stackblur.StackBlurManager;

/**
 * Created by huangwei on 2015/6/23.
 * 用于做电影结束时的高斯模糊效果
 */
public class EndGaussianBlurSegment extends GLMovieSegment {

    private MovieSegment mPreSegment;
    private BitmapInfo mBluredBitmapInfo;

    public EndGaussianBlurSegment(int duration) {
        super();
        setDuration(duration);
    }

    @Override
    protected boolean checkPrepared() {
        return mBluredBitmapInfo != null && mBluredBitmapInfo.isTextureAvailable();
    }

    @Override
    public void onPrepare() {
        mPreSegment = mPhotoMovie.getSegmentPicker().getPreSegment(this);
        if (mPreSegment != null) {
            mPreSegment.enableRelease(false);
        }
    }

    @Override
    protected void onDataPrepared() {

    }

    @Override
    public void drawFrame(GLESCanvas canvas, float segmentProgress) {
        if (mPreSegment == null) {
            return;
        }
        mPreSegment.drawFrame(canvas, 1);

        if (mBluredBitmapInfo == null) {
            initBluredTexture();
        }

        if (mBluredBitmapInfo.bitmapTexture != null) {
            canvas.drawMixed(mBluredBitmapInfo.bitmapTexture, 0, 1 - segmentProgress, mBluredBitmapInfo.srcShowRect, mViewportRect);
        }
    }

    @Override
    public int getRequiredPhotoNum() {
        return 0;
    }

    @Override
    protected void onRelease() {
        if (mPreSegment != null) {
            mPreSegment.enableRelease(true);
            mPreSegment.release();
            mPreSegment = null;
        }
    }

    @Override
    public void onSegmentEnd() {
        super.onSegmentEnd();
    }

    private void initBluredTexture() {
        BitmapTexture bitmapTexture = null;
        Bitmap bluredBitmap = null;
        try {
            Bitmap bitmap = captureBitmap();
            StackBlurManager manager = new StackBlurManager(bitmap, 0.25f);
            float blurRadius = AppResources.getInstance().getAppDensity() * GaussianBlurLayer.BLUR_RADIUS_DEFAULT;
            bluredBitmap = manager.process((int) (blurRadius * 0.25f));
            bitmapTexture = new BitmapTexture(bluredBitmap);
            bitmapTexture.setOpaque(false);

        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }

        mBluredBitmapInfo = new BitmapInfo();
        mBluredBitmapInfo.bitmapTexture = bitmapTexture;
        if (bluredBitmap != null) {
            mBluredBitmapInfo.srcRect.set(0, 0, bluredBitmap.getWidth(), bluredBitmap.getHeight());
            mBluredBitmapInfo.srcShowRect.set(mBluredBitmapInfo.srcRect);
        }
    }
}
