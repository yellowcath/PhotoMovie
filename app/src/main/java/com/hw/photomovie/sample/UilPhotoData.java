package com.hw.photomovie.sample;

/**
 * Created by huangwei on 2015/5/27.
 */
public class UilPhotoData{
        //extends PhotoData implements ImageLoadingListener, ImageLoadingProgressListener {
//    private static final String TAG = "UilPhotoData";
//    protected OnDataLoadListener mOnDataLoadListener;
//    protected DisplayImageOptions mOptions = new DisplayImageOptions.Builder().
//            cacheInMemory(true).
//            cacheOnDisk(true).
//            considerExifParams(true).
//            build();
//    protected NoCancelAware mAware;
//
//    public UilPhotoData(String uri, int state) {
//        super(uri, state);
//    }
//
//    @Override
//    public void prepareData(final int targetState, OnDataLoadListener onDataLoadListener) {
//        mOnDataLoadListener = onDataLoadListener;
//        mTargetState = targetState;
//        mAware = mAware == null ? new NoCancelAware() : mAware;
//        switch (mState) {
//            case STATE_BITMAP:
//                if (targetState == STATE_BITMAP && onDataLoadListener != null) {
//                    onDataLoadListener.onDataLoaded(this, getBitmap());
//                }else if (targetState == STATE_LOCAL && onDataLoadListener != null) {
//                    onDataLoadListener.onDownloaded(this);
//                }
//                break;
//            case STATE_LOADING:
//                break;
//            case STATE_LOCAL:
//                if (targetState == STATE_BITMAP) {
//                    ImageLoader.getInstance().displayImage(getUri(), mAware, mOptions, this, this);
//                } else if (targetState == STATE_LOCAL && onDataLoadListener != null) {
//                    onDataLoadListener.onDownloaded(this);
//                }
//                break;
//            case STATE_DOWNLOADING:
//                break;
//            case STATE_REMOTE:
//            case STATE_ERROR:
//                ImageLoader.getInstance().displayImage(getUri(), mAware, mOptions, this, this);
//                break;
//        }
//    }
//
//    @Override
//    public void onLoadingStarted(String imageUri, View view) {
//        if (mState < STATE_LOCAL) {
//            mState = STATE_DOWNLOADING;
//        } else {
//            mState = STATE_LOADING;
//        }
//    }
//
//    @Override
//    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
//        if (mState == STATE_LOADING) {
//            mState = STATE_LOCAL;
//        } else {
//            mState = STATE_ERROR;
//        }
//        if (mOnDataLoadListener != null) {
//            ErrorReason errorReason = null;
//            if (failReason != null) {
//                errorReason = new ErrorReason(failReason.getCause(), failReason.getType().name());
//            }
//            mOnDataLoadListener.onError(this, errorReason);
//        }
//        String reason = failReason != null && failReason.getType() != null ? failReason.getType().name() : "null";
//        MLog.e(TAG, "loading Faild:" + imageUri + " reason:" + reason);
//    }
//
//    @Override
//    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//        if (!Utils.isBitmapAvailable(loadedImage)) {
//            onLoadingFailed(imageUri, view, null);
//            return;
//        }
//
//        if (mTargetState == STATE_LOCAL) {
//            mState = STATE_LOCAL;
//        } else if (mTargetState == STATE_BITMAP) {
//            mState = STATE_BITMAP;
//            mBitmap = loadedImage;
//        }
//
//        if (mOnDataLoadListener != null) {
//            if (mTargetState >= STATE_LOCAL) {
//                mOnDataLoadListener.onDownloaded(this);
//            }
//            if (mTargetState == STATE_BITMAP) {
//                mOnDataLoadListener.onDataLoaded(this, loadedImage);
//            }
//        }
//        MLog.i(TAG, "loading complete:" + imageUri);
//    }
//
//    @Override
//    public void onLoadingCancelled(String imageUri, View view) {
//        MLog.w(TAG, "loading cancelled:" + imageUri);
//        onLoadingFailed(imageUri, view, null);
//    }
//
//    @Override
//    public void onProgressUpdate(String s, View view, int i, int i1) {
//        if (mOnDataLoadListener != null) {
//            mOnDataLoadListener.onDownloadProgressUpdate(this, i, i1);
//        }
//    }
//
//    private class NoCancelAware implements ImageAware {
//        protected final ImageSize imageSize;
//        protected final ViewScaleType scaleType;
//
//        public NoCancelAware() {
//            DisplayMetrics metrics = AppResources.getInstance().getAppRes().getDisplayMetrics();
//            imageSize = new ImageSize(metrics.widthPixels, metrics.heightPixels);
//            scaleType = ViewScaleType.CROP;
//            if (imageSize == null) {
//                throw new IllegalArgumentException("imageSize must not be null");
//            } else if (scaleType == null) {
//                throw new IllegalArgumentException("scaleType must not be null");
//            }
//        }
//
//        public int getWidth() {
//            return this.imageSize.getWidth();
//        }
//
//        public int getHeight() {
//            return this.imageSize.getHeight();
//        }
//
//        public ViewScaleType getScaleType() {
//            return this.scaleType;
//        }
//
//        public View getWrappedView() {
//            return null;
//        }
//
//        public boolean isCollected() {
//            return false;
//        }
//
//        public int getId() {
//            return super.hashCode();
//        }
//
//        public boolean setImageDrawable(Drawable drawable) {
//            return true;
//        }
//
//        public boolean setImageBitmap(Bitmap bitmap) {
//            return true;
//        }
//    }
}
