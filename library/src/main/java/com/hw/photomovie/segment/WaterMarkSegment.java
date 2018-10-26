package com.hw.photomovie.segment;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import com.hw.photomovie.opengl.BitmapTexture;
import com.hw.photomovie.opengl.GLESCanvas;
import com.hw.photomovie.util.ScaleType;

/**
 * Created by huangwei on 2018/10/26.
 */
public class WaterMarkSegment extends SingleBitmapSegment {

    private Bitmap mBitmap;
    private RectF mDstRect;

    public void setWaterMark(Bitmap bitmap, Rect dstRect) {
        mBitmap = bitmap;
        mDstRect = new RectF(dstRect);
        synchronized (this) {
            mBitmapInfo = null;
        }
    }

    @Override
    public void drawFrame(GLESCanvas canvas, float segmentRate) {
        synchronized (this) {
            if (mBitmapInfo == null && mBitmap != null && mDstRect != null) {
                BitmapTexture bitmapTexture = new BitmapTexture(mBitmap);
                bitmapTexture.setOpaque(false);
                mBitmapInfo = new BitmapInfo();
                mBitmapInfo.srcRect = new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
                mBitmapInfo.srcShowRect.set(mBitmapInfo.srcRect);
                mBitmapInfo.scaleType = ScaleType.FIT_XY;
                mBitmapInfo.bitmapTexture = bitmapTexture;
                onDataPrepared();
            }
        }
        if (!mDataPrepared) {
            return;
        }
        if (mBitmapInfo != null && mBitmapInfo.makeTextureAvailable(canvas)) {
            canvas.drawTexture(mBitmapInfo.bitmapTexture, mBitmapInfo.srcShowRect, mDstRect);
        }
    }
}
