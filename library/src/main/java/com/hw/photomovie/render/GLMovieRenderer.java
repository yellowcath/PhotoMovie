package com.hw.photomovie.render;

import android.content.Context;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.Looper;
import com.hw.photomovie.moviefilter.IMovieFilter;
import com.hw.photomovie.opengl.FboTexture;
import com.hw.photomovie.opengl.GLESCanvas;

/**
 * Created by huangwei on 2015/5/26.
 */
public abstract class GLMovieRenderer extends MovieRenderer<GLESCanvas> {
    public static Context sContext;

    private FboTexture mFboTexture;
    private FboTexture mFilterTexture;
    protected IMovieFilter mMovieFilter;
    private Object mPrepareLock = new Object();
    private Object mSetFilterLock = new Object();
    protected volatile boolean mPrepared;
    private volatile OnGLPrepareListener mOnGLPrepareListener;

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
        mFboTexture = new FboTexture();
        mFboTexture.setSize(w, h);
        mFilterTexture = new FboTexture();
        mFilterTexture.setSize(w, h);
    }

    public void releaseTextures() {
        if(mFboTexture!=null) {
            mFboTexture.release();
            mFboTexture = null;
        }
        if(mFilterTexture!=null) {
            mFilterTexture.release();
            mFilterTexture = null;
        }
    }

    @Override
    public void drawMovieFrame(int elapsedTime) {
        synchronized (mPrepareLock){
            mPrepared = true;
            if(mOnGLPrepareListener!=null){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mOnGLPrepareListener.onGLPrepared();
                        mOnGLPrepareListener = null;
                    }
                });
            }
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
            if(mMovieFilter!=null) {
                mMovieFilter.doFilter(mPhotoMovie, elapsedTime, mFboTexture, mFilterTexture);
            }
        }
        mPainter.rebindArrayBuffer();

        mPainter.drawTexture(mFilterTexture, 0, 0, mViewportRect.width(), mViewportRect.height());
    }

    public void release(){
        releaseTextures();
    }

    public void checkGLPrepared(OnGLPrepareListener onGLPrepareListener){
        synchronized (mPrepareLock){
            if(mPrepared){
                onGLPrepareListener.onGLPrepared();
            }else{
                mOnGLPrepareListener = onGLPrepareListener;
            }
        }
    }

    public boolean isPrepared() {
        return mPrepared;
    }

    private void runOnUiThread(Runnable r){
        new Handler(Looper.getMainLooper()).post(r);
    }

    public static interface OnGLPrepareListener{
        void onGLPrepared();
    }
}
