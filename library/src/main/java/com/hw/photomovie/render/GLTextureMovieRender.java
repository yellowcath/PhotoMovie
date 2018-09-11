package com.hw.photomovie.render;

import android.opengl.GLES20;
import com.hw.photomovie.util.MLog;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by huangwei on 2018/9/6 0006.
 */
public class GLTextureMovieRender extends GLSurfaceMovieRenderer {

    private static final String TAG = "GLTextureMovieRender";
    protected GLTextureView mGLTextureView;

    public GLTextureMovieRender(GLTextureView glTextureView) {
        mGLTextureView = glTextureView;
        mGLTextureView.setEGLContextClientVersion(2);
        mGLTextureView.setRenderer(new GLTextureView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                mSurfaceCreated = true;
                prepare();
            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                setViewport(width, height);
            }

            @Override
            public boolean onDrawFrame(GL10 gl) {
                if(mNeedRelease.get()){
                    mNeedRelease.set(false);
                    releaseGLResources();
                    return false;
                }
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                drawMovieFrame(mElapsedTime);
                return true;
            }

            @Override
            public void onSurfaceDestroyed() {
                mSurfaceCreated = false;
                release();
            }
        });
        mGLTextureView.setRenderMode(GLTextureView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public void drawFrame(int elapsedTime) {
        mElapsedTime = elapsedTime;
        if(mRenderToRecorder){
            onDrawFrame(null);
            return;
        }
        if(mSurfaceCreated){
            mGLTextureView.requestRender();
        }else{
            MLog.e(TAG,"Surface not created!");
        }
    }

    @Override
    public void release() {
        mNeedRelease.set(true);
        if(mSurfaceCreated){
            mGLTextureView.requestRender();
        }
    }
}
