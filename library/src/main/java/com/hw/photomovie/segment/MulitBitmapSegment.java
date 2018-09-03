package com.hw.photomovie.segment;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import com.hw.photomovie.model.ErrorReason;
import com.hw.photomovie.model.PhotoData;
import com.hw.photomovie.opengl.BitmapTexture;
import com.hw.photomovie.opengl.GLESCanvas;
import com.hw.photomovie.util.PhotoUtil;
import com.hw.photomovie.util.ScaleType;
import com.hw.photomovie.util.Utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by huangwei on 2015/6/3.
 */
public abstract class MulitBitmapSegment extends GLMovieSegment implements PhotoData.OnDataLoadListener {

    protected Map<PhotoData, BitmapInfo> mPhotoDataMap = new HashMap<PhotoData, BitmapInfo>();
    private int mLoaded;

    public MulitBitmapSegment() {
    }

    public MulitBitmapSegment(int duration) {
        mDuration = duration;
    }

    @Override
    public void allocPhotos(List<PhotoData> photos) {
        super.allocPhotos(photos);
        mLoaded = 0;
    }

    @Override
    public void onPrepare() {
        mPhotoDataMap.clear();
        for (PhotoData photoData : mPhotos) {
            mPhotoDataMap.put(photoData, new BitmapInfo());
        }
        mLoaded = 0;
        for (PhotoData photoData : mPhotos) {
            photoData.prepareData(PhotoData.STATE_BITMAP, this);
        }
    }

    @Override
    public void onRelease() {
        Collection<BitmapInfo> bitmapInfos = mPhotoDataMap.values();
        for (Iterator<BitmapInfo> it = bitmapInfos.iterator(); it.hasNext(); ) {
            BitmapInfo bitmapInfo = it.next();
            if (bitmapInfo.bitmapTexture != null) {
                bitmapInfo.bitmapTexture.recycle();
                bitmapInfo.bitmapTexture = null;
            }
        }
    }

    @Override
    public void onDataLoaded(PhotoData photoData, Bitmap bitmap) {
        mLoaded++;
        BitmapInfo bitmapInfo = mPhotoDataMap.get(photoData);
        if (bitmapInfo == null) {
            return;
        }
        if (Utils.isBitmapAvailable(bitmap)) {
            bitmapInfo.bitmapTexture = new BitmapTexture(bitmap);
            bitmapInfo.srcRect.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
            bitmapInfo.applyScaleType(mViewportRect);
        }
        checkAllLoaded();
    }

    @Override
    public void setViewport(int l, int t, int r, int b) {
        super.setViewport(l, t, r, b);
        for (BitmapInfo bitmapInfo : mPhotoDataMap.values()) {
            bitmapInfo.applyScaleType(mViewportRect);
        }
    }

    private void checkAllLoaded() {
        if (mLoaded != mPhotoDataMap.size()) {
            return;
        }
        //加载完成，检查是否有失败的
        boolean success = true;
        Collection<BitmapInfo> bitmapInfos = mPhotoDataMap.values();
        for (Iterator<BitmapInfo> it = bitmapInfos.iterator(); it.hasNext(); ) {
            BitmapInfo bitmapInfo = it.next();
            if (bitmapInfo.bitmapTexture == null) {
                success = false;
                break;
            }
        }

        if (mOnSegmentPrepareListener != null) {
            mOnSegmentPrepareListener.onSegmentPrepared(success);
        }
        if (success) {
            onDataPrepared();
        }
    }

    @Override
    public void onDownloaded(PhotoData photoData) {

    }

    @Override
    public void onDownloadProgressUpdate(PhotoData photoData, int current, int total) {

    }

    @Override
    public void onError(PhotoData photoData, ErrorReason errorReason) {
        mLoaded++;
        checkAllLoaded();
    }

    public static class BitmapInfo {
        public BitmapTexture bitmapTexture;
        public Rect srcRect = new Rect();
        public RectF srcShowRect = new RectF();
        public ScaleType scaleType = ScaleType.CENTER_CROP;

        public void applyScaleType(RectF dstRect) {
            if (dstRect == null || dstRect.width() <= 0 || dstRect.height() <= 0) {
                srcShowRect.set(srcRect);
            }

            if (scaleType == ScaleType.CENTER_CROP) {
                srcShowRect.set(PhotoUtil.getCroppedRect(null,
                        srcRect.width(),
                        srcRect.height(),
                        dstRect.width(),
                        dstRect.height()));
            } else {
                srcShowRect.set(srcRect);
            }
        }

        public boolean isTextureAvailable() {
            return bitmapTexture != null && bitmapTexture.isLoaded();
        }

        /**
         * 如果材质不可用，尝试重新加载一次
         * @param canvas
         * @return
         */
        public boolean makeTextureAvailable(GLESCanvas canvas){
            if(bitmapTexture==null ){
                return false;
            }
            if(bitmapTexture.isLoaded()){
                return true;
            }
            bitmapTexture.updateContent(canvas);
            return bitmapTexture.isLoaded();
        }
    }
}
