package com.hw.photomovie.render;

import android.content.Context;
import android.opengl.GLES20;
import com.hw.photomovie.moviefilter.IMovieFilter;
import com.hw.photomovie.opengl.FboTexture;
import com.hw.photomovie.opengl.GLESCanvas;

/**
 * Created by huangwei on 2015/5/26.
 */
public abstract class FboMovieRenderer extends MovieRenderer<GLESCanvas> {
    public static Context sContext;

    private FboTexture mFboTexture;
    private FboTexture mFilterTexture;
    private IMovieFilter mMovieFilter;

    public void setMovieFilter(IMovieFilter movieFilter) {
        mMovieFilter = movieFilter;
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
        mFboTexture = null;
        mFilterTexture = null;
    }

    @Override
    public void drawMovieFrame(int elapsedTime) {
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
        mMovieFilter.doFilter(mFboTexture, mFilterTexture);
        mPainter.rebindArrayBuffer();

        mPainter.drawTexture(mFilterTexture, 0, 0, mViewportRect.width(), mViewportRect.height());
    }
}
