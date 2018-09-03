package com.hw.photomovie.model;

import android.graphics.Bitmap;

/**
 * Created by huangwei on 2015/5/25.
 */
public abstract class PhotoData {

    public static final int STATE_ERROR = -1;
    public static final int STATE_REMOTE = 0;
    public static final int STATE_DOWNLOADING = 1;
    public static final int STATE_LOCAL = 2;
    public static final int STATE_LOADING = 3;
    public static final int STATE_BITMAP = 4;

    protected String mUri;
    protected String mLocalUri;
    protected volatile Bitmap mBitmap;

    protected int mState;
    protected int mTargetState;

    private PhotoInfo mPhotoInfo;

    public PhotoData(String uri, int state) {
        this(uri, state, null);
    }

    public PhotoData(String uri, int state, PhotoInfo photoInfo) {
        mUri = uri;
        mState = state;
        mPhotoInfo = photoInfo;
    }

    /**
     * 可能返回null,只有状态为{@link #STATE_BITMAP}才返回bitmap
     *
     * @return
     */
    public Bitmap getBitmap() {
        return mBitmap;
    }

    /**
     * 可能返回null,只有状态为{@link #STATE_BITMAP}才返回bitmap
     *
     * @return
     */
    public Bitmap getBitmapAndRelease() {
        Bitmap bitmap = mBitmap;
        mBitmap = null;
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            mBitmap = bitmap;
        }
    }

    public String getUri() {
        return mUri;
    }

    public int getState() {
        return mState;
    }

    public int getTargetState() {
        return mTargetState;
    }

    public PhotoInfo getPhotoInfo() {
        return mPhotoInfo;
    }

    public void setPhotoInfo(PhotoInfo photoInfo) {
        mPhotoInfo = photoInfo;
    }

    /**
     * @param targetState        must be {@link #STATE_LOCAL} OR {@link #STATE_BITMAP}
     * @param onDataLoadListener
     */
    public abstract void prepareData(int targetState, OnDataLoadListener onDataLoadListener);

    public boolean isLocal() {
        return mState == STATE_LOCAL || mState == STATE_LOADING || mState == STATE_BITMAP;
    }

    public static class SimpleOnDataLoadListener implements OnDataLoadListener {

        @Override
        public void onDataLoaded(PhotoData photoData, Bitmap bitmap) {

        }

        @Override
        public void onDownloaded(PhotoData photoData) {

        }

        @Override
        public void onDownloadProgressUpdate(PhotoData photoData, int current, int total) {

        }

        @Override
        public void onError(PhotoData photoData, ErrorReason errorReason) {

        }
    }

    public interface OnDataLoadListener {
        void onDataLoaded(PhotoData photoData, Bitmap bitmap);

        void onDownloaded(PhotoData photoData);

        void onDownloadProgressUpdate(PhotoData photoData, int current, int total);

        void onError(PhotoData photoData, ErrorReason errorReason);
    }
}
