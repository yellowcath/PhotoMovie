package com.hw.photomovie.segment;

import android.graphics.Bitmap;
import com.hw.photomovie.model.ErrorReason;
import com.hw.photomovie.model.PhotoData;
import com.hw.photomovie.opengl.BitmapTexture;
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

    @Override
    protected boolean checkPrepared() {
        Collection<BitmapInfo> bitmapInfos = mPhotoDataMap.values();
        for (Iterator<BitmapInfo> it = bitmapInfos.iterator(); it.hasNext(); ) {
            BitmapInfo bitmapInfo = it.next();
            if (!bitmapInfo.isTextureAvailable()) {
                return false;
            }
        }
        return true;
    }
}
