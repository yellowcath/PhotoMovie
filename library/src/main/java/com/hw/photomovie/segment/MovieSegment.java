package com.hw.photomovie.segment;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.opengl.GLES20;
import com.hw.photomovie.PhotoMovie;
import com.hw.photomovie.model.PhotoData;
import com.hw.photomovie.segment.strategy.ReallocStrategy;
import com.hw.photomovie.segment.strategy.RetryStrategy;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangwei on 2015/5/25.
 */
public abstract class MovieSegment<T> {

    protected boolean IS_DURATION_VARIABLE = false;

    protected PhotoMovie mPhotoMovie;

    private List<PhotoData> mAllocatedPhotos = new ArrayList<PhotoData>();

    protected List<PhotoData> mPhotos = new ArrayList<PhotoData>();

    protected int mDuration;

    protected RectF mViewportRect = new RectF();

    protected OnSegmentPrepareListener mOnSegmentPrepareListener;

    protected boolean mDataPrepared;

    /**
     * 是否真正执行{@link #release()}函数的内容
     */
    private boolean mEnableRelease = true;
    /**
     * 分配好的PhotoData不可用时的策略
     */
    protected RetryStrategy mRetryStrategy = new ReallocStrategy();

    public void setPhotoMovie(PhotoMovie photoMovie) {
        mPhotoMovie = photoMovie;
    }

    public int getDuration() {
        return mDuration;
    }

    public MovieSegment setDuration(int duration) {
        mDuration = duration;
        return this;
    }

    public final void prepare() {
        if (checkPrepared()) {
            if (mOnSegmentPrepareListener != null) {
                mOnSegmentPrepareListener.onSegmentPrepared(true);
            }
            return;
        }
        checkPhotoData();
        onPrepare();
        if (IS_DURATION_VARIABLE) {
            mPhotoMovie.calcuDuration();
        }
    }

    /**
     * if true {@link #onPrepare()} will be skiped to avoid repeated prepare.
     * @return
     */
    protected abstract boolean checkPrepared();

    public boolean isVariableDuration() {
        return IS_DURATION_VARIABLE;
    }

    /**
     * 确保数据可用
     */
    protected void checkPhotoData() {
        if (mAllocatedPhotos == null || mAllocatedPhotos.size() == 0) {
            return;
        }
        boolean allLocal = true;
        for (PhotoData photoData : mAllocatedPhotos) {
            if (photoData.getState() < PhotoData.STATE_LOCAL) {
                allLocal = false;
                break;
            }
        }
        mPhotos.clear();
        if (allLocal) {
            mPhotos.addAll(mAllocatedPhotos);
        } else {
            mPhotos.addAll(mRetryStrategy.getAvailableData(mPhotoMovie, this));
        }
    }

    /**
     * 开始播放某一片段会调用上一个片段的{@link #prepare()}
     */
    protected abstract void onPrepare();

    /**
     * 建议在{@link #onPrepare()}完成后调用，顺便将{@link #mDataPrepared}置为true
     */
    protected abstract void onDataPrepared();

    public abstract void drawFrame(T painter, float segmentProgress);

    public void setViewport(int l, int t, int r, int b) {
        mViewportRect.set(l, t, r, b);
    }

    public List<PhotoData> getAllocatedPhotos() {
        return mAllocatedPhotos;
    }

    public void allocPhotos(List<PhotoData> photos) {
        this.mAllocatedPhotos.clear();
        mAllocatedPhotos.addAll(photos);
    }

    public PhotoData getPhoto(int i) {
        return i >= 0 && i < mPhotos.size() ? mPhotos.get(i) : null;
    }

    /**
     * 是否展示下一个片段作为当前片段的背景
     *
     * @return
     */
    public boolean showNextAsBackground() {
        return false;
    }

    /**
     * 该片段需要多少张照片
     *
     * @return
     */
    public abstract int getRequiredPhotoNum();

    public final void release() {
        if (!mEnableRelease) {
            return;
        }
        onRelease();
    }

    /**
     * @param enableRelease 为false后当{@link #release()}函数被调用时不会真正执行释放资源操作，
     *                      需要由该函数的调用者自行管理释放资源
     *                      用于下一个片段需要用到当前片段的情况
     */
    public final void enableRelease(boolean enableRelease) {
        mEnableRelease = enableRelease;
    }

    protected abstract void onRelease();

    /**
     * 该片段播放完毕时调用
     */
    public void onSegmentEnd() {
    }

    public void setOnSegmentPrepareListener(OnSegmentPrepareListener l) {
        mOnSegmentPrepareListener = l;
    }

    public interface OnSegmentPrepareListener {
        void onSegmentPrepared(boolean success);
    }

    public Bitmap captureBitmap() throws OutOfMemoryError {
        int width = (int) mViewportRect.width();
        int height = (int) mViewportRect.height();

        final IntBuffer pixelBuffer = IntBuffer.allocate(width * height);
        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixelBuffer);
        int[] pixelArray = pixelBuffer.array();
        final int[] pixelMirroredArray = new int[width * height];

        // Convert upside down mirror-reversed image to right-side up normal image.
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                pixelMirroredArray[(height - i - 1) * width + j] = pixelArray[i * width + j];
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(IntBuffer.wrap(pixelMirroredArray));
        return bitmap;
    }
}
