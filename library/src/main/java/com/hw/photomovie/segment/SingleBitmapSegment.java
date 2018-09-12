package com.hw.photomovie.segment;

import android.graphics.Bitmap;
import com.hw.photomovie.model.ErrorReason;
import com.hw.photomovie.model.PhotoData;
import com.hw.photomovie.opengl.BitmapTexture;
import com.hw.photomovie.opengl.GLESCanvas;
import com.hw.photomovie.util.MLog;
import com.hw.photomovie.util.ScaleType;
import com.hw.photomovie.util.Utils;

/**
 * Created by huangwei on 2015/5/25.
 * 适用于单张bitmap的电影片段
 */
public class SingleBitmapSegment extends GLMovieSegment {

    protected static final String TAG = "SingleBitmapSegment";
    protected volatile BitmapInfo mBitmapInfo;
    protected ScaleType mDefaultScaleType = ScaleType.CENTER_CROP;

    public SingleBitmapSegment() {
    }

    public SingleBitmapSegment(int duration) {
        mDuration = duration;
    }

    @Override
    public void onPrepare() {
        PhotoData photoData = getPhoto(0);
        if (photoData != null) {
            photoData.prepareData(PhotoData.STATE_BITMAP, new PhotoData.SimpleOnDataLoadListener() {
                @Override
                public void onDataLoaded(PhotoData photoData, Bitmap bitmap) {
                    if (Utils.isBitmapAvailable(bitmap)) {
                        BitmapTexture bitmapTexture = new BitmapTexture(bitmap);
                        mBitmapInfo = new BitmapInfo();
                        mBitmapInfo.scaleType = mDefaultScaleType;
                        mBitmapInfo.bitmapTexture = bitmapTexture;
                        mBitmapInfo.srcRect.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
                        mBitmapInfo.srcShowRect.set(mBitmapInfo.srcRect);
                        onDataPrepared();
                    }
                    if (mOnSegmentPrepareListener != null) {
                        mOnSegmentPrepareListener.onSegmentPrepared(true);
                    }
                }

                @Override
                public void onError(PhotoData photoData, ErrorReason errorReason) {
                    if (mOnSegmentPrepareListener != null) {
                        mOnSegmentPrepareListener.onSegmentPrepared(false);
                    }
                }
            });
        } else {
            MLog.e(TAG, "available photoData is null,segment:" + this);
        }
    }

    @Override
    protected void onDataPrepared() {
        mDataPrepared = true;
        if (mBitmapInfo != null) {
            mBitmapInfo.applyScaleType(mViewportRect);
        }
    }

    @Override
    public void drawFrame(GLESCanvas canvas, float segmentRate) {
        if (!mDataPrepared) {
            return;
        }
        if (mBitmapInfo != null && mBitmapInfo.makeTextureAvailable(canvas)) {
            canvas.drawTexture(mBitmapInfo.bitmapTexture, mBitmapInfo.srcShowRect, mViewportRect);
        }
    }

    @Override
    public int getRequiredPhotoNum() {
        return 1;
    }

    @Override
    public void onRelease() {
        if (mBitmapInfo != null && mBitmapInfo.bitmapTexture != null) {
            mBitmapInfo.bitmapTexture.recycle();
        }
        mBitmapInfo = null;
    }

    @Override
    public void setViewport(int l, int t, int r, int b) {
        super.setViewport(l, t, r, b);
        if (mBitmapInfo != null) {
            mBitmapInfo.applyScaleType(mViewportRect);
        }
    }

    @Override
    protected boolean checkPrepared() {
        return mBitmapInfo != null && mBitmapInfo.isTextureAvailable();
    }

    public BitmapInfo getBitmapInfo() {
        return mBitmapInfo;
    }
}
