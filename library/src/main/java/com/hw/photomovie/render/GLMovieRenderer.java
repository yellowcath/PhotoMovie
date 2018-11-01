package com.hw.photomovie.render;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import com.hw.photomovie.moviefilter.IMovieFilter;
import com.hw.photomovie.opengl.FboTexture;
import com.hw.photomovie.opengl.GLESCanvas;
import com.hw.photomovie.segment.GLMovieSegment;
import com.hw.photomovie.segment.MovieSegment;
import com.hw.photomovie.segment.WaterMarkSegment;
import com.hw.photomovie.util.BitmapUtil;

import java.util.List;

/**
 * Created by huangwei on 2015/5/26.
 */
public abstract class GLMovieRenderer extends MovieRenderer<GLESCanvas> {

    private FboTexture mFboTexture;
    private FboTexture mFilterTexture;
    protected IMovieFilter mMovieFilter;
    private Object mPrepareLock = new Object();
    private Object mSetFilterLock = new Object();
    protected volatile boolean mPrepared;
    private volatile OnGLPrepareListener mOnGLPrepareListener;
    protected float[] mClearColor = new float[]{0f,0f,0f,1f};
    protected volatile List<MovieSegment<GLESCanvas>> mReleaseMovieSegments;

    public GLMovieRenderer() {
    }

    public GLMovieRenderer(GLMovieRenderer movieRenderer) {
        mMovieFilter = ((GLMovieRenderer) movieRenderer).mMovieFilter;
        if (movieRenderer.mCoverSegment instanceof WaterMarkSegment) {
            mCoverSegment = ((WaterMarkSegment) movieRenderer.mCoverSegment).clone();
        }
    }

    public void setMovieFilter(IMovieFilter movieFilter) {
        synchronized (mSetFilterLock) {
            mMovieFilter = movieFilter;
        }
    }

    @Override
    public void setMovieViewport(int l, int t, int r, int b) {
        super.setMovieViewport(l, t, r, b);
        if (mFboTexture != null && (mFboTexture.getWidth() != r - l || mFboTexture.getHeight() != b - t)) {
            initTexture(r - l, b - t);
        }
    }

    private void initTexture(int w, int h) {
        if(mFboTexture!=null){
            mFboTexture.release();
        }
        if(mFilterTexture!=null){
            mFilterTexture.release();
        }
        mFboTexture = new FboTexture();
        mFboTexture.setSize(w, h);
        mFilterTexture = new FboTexture();
        mFilterTexture.setSize(w, h);
    }

    protected void releaseTextures() {
        if (mFboTexture != null) {
            mFboTexture.release();
            mFboTexture = null;
        }
        if (mFilterTexture != null) {
            mFilterTexture.release();
            mFilterTexture = null;
        }
    }

    @Override
    public void drawMovieFrame(int elapsedTime) {
        synchronized (mPrepareLock) {
            mPrepared = true;
            if (mOnGLPrepareListener != null) {
                final OnGLPrepareListener listener = mOnGLPrepareListener;
                mOnGLPrepareListener = null;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onGLPrepared();
                    }
                });
            }
        }
        if(mReleaseMovieSegments!=null){
            releaseSegments(mReleaseMovieSegments);
            mReleaseMovieSegments = null;
        }
        if (mMovieFilter == null) {
            super.drawMovieFrame(elapsedTime);
            return;
        }

        if (mFboTexture == null || mFilterTexture == null) {
            initTexture(mViewportRect.width(), mViewportRect.height());
        }
        int[] curFb = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, curFb, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFboTexture.getFrameBuffer());
        super.drawMovieFrame(elapsedTime);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, curFb[0]);

        mPainter.unbindArrayBuffer();
        synchronized (mSetFilterLock) {
            if (mMovieFilter != null) {
                mMovieFilter.doFilter(mPhotoMovie, elapsedTime, mFboTexture, mFilterTexture);
            }
        }
        mPainter.rebindArrayBuffer();

        mPainter.drawTexture(mFilterTexture, 0, 0, mViewportRect.width(), mViewportRect.height());
    }

    /**
     * 由子类调用
     */
    protected void releaseGLResources() {
        List<MovieSegment<GLESCanvas>> movieSegments = (mPhotoMovie==null || mPhotoMovie.getMovieSegments()==null)?null:mPhotoMovie.getMovieSegments();
        releaseSegments(movieSegments);
        releaseCoverSegment();
        releaseTextures();
        if (mMovieFilter != null) {
            mMovieFilter.release();
        }
        mPainter.deleteRecycledResources();
        if (mOnReleaseListener != null) {
            mOnReleaseListener.onRelease();
        }
    }

    @Override
    public void release(List<MovieSegment<GLESCanvas>> movieSegments) {
        mReleaseMovieSegments = movieSegments;
    }

    protected void releaseSegments(List<MovieSegment<GLESCanvas>> movieSegments) {
        for(MovieSegment<GLESCanvas> segment:movieSegments){
            segment.enableRelease(true);
            segment.release();
        }
        if(mPainter!=null) {
            mPainter.deleteRecycledResources();
        }
    }

    public void checkGLPrepared(OnGLPrepareListener onGLPrepareListener) {
        synchronized (mPrepareLock) {
            if (mPrepared) {
                onGLPrepareListener.onGLPrepared();
            } else {
                mOnGLPrepareListener = onGLPrepareListener;
            }
        }
    }

    public boolean isPrepared() {
        return mPrepared;
    }

    private void runOnUiThread(Runnable r) {
        new Handler(Looper.getMainLooper()).post(r);
    }

    public IMovieFilter getMovieFilter() {
        return mMovieFilter;
    }

    public void setWaterMark(Bitmap bitmap, RectF dstRect, float alpha) {
        if (bitmap == null || dstRect == null) {
            return;
        }
        if (mCoverSegment == null || !(mCoverSegment instanceof WaterMarkSegment)) {
            mCoverSegment = new WaterMarkSegment();
        }
        ((WaterMarkSegment) mCoverSegment).setWaterMark(bitmap, dstRect, alpha);
        if (mViewportRect != null && mViewportRect.width() > 0) {
            mCoverSegment.setViewport(mViewportRect.left, mViewportRect.top, mViewportRect.right, mViewportRect.bottom);
        }
    }

    public void setWaterMark(String text, int textSize, int textColor, int x, int y) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        if (mCoverSegment == null || !(mCoverSegment instanceof WaterMarkSegment)) {
            mCoverSegment = new WaterMarkSegment();
        }
        Bitmap bitmap = BitmapUtil.generateBitmap(text, textSize, textColor);
        ((WaterMarkSegment) mCoverSegment).setWaterMark(bitmap, new RectF(x, y, x + bitmap.getWidth(), y + bitmap.getHeight()), 1f);
        if (mViewportRect != null && mViewportRect.width() > 0) {
            mCoverSegment.setViewport(mViewportRect.left, mViewportRect.top, mViewportRect.right, mViewportRect.bottom);
        }
    }

    public static interface OnGLPrepareListener {
        void onGLPrepared();
    }
}
