package com.hw.photomovie.render;

import android.opengl.GLES20;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by huangwei on 2018/9/6 0006.
 */
public class GLTextureMovieRender extends GLMovieRenderer {

    protected GLTextureView mGLTextureView;

    public GLTextureMovieRender(GLTextureView glTextureView) {
        mGLTextureView = glTextureView;
        mGLTextureView.setEGLContextClientVersion(2);
        mGLTextureView.setRenderer(new GLTextureView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                prepare();
                mSurfaceCreated = true;
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                setViewport(width, height);
            }

            @Override
            public boolean onDrawFrame(GL10 gl) {
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                drawMovieFrame(mElapsedTime);
                return true;
            }

            @Override
            public void onSurfaceDestroyed() {

            }
        });
        mGLTextureView.setRenderMode(GLTextureView.RENDERMODE_WHEN_DIRTY);
    }
}
