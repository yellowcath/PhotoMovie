package com.hw.photomovie.render;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import com.hw.photomovie.opengl.GLES20Canvas;
import com.hw.photomovie.opengl.GLESCanvas;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by huangwei on 2015/5/26.
 */
public class GLMovieRenderer extends FboMovieRenderer implements GLSurfaceView.Renderer {

    private GLSurfaceView mGLSurfaceView;

    private boolean mSurfaceCreated;
    /**
     * 录制时不再渲染到GLSurfaceView上
     */
    private boolean mRenderToRecorder = false;

    /**
     * 无GLSurfaceView的构造函数用于{@link record.GLMovieRecorder},会在外部设置GLES输出的Surface
     */
    public GLMovieRenderer() {
        super();
    }

    public GLMovieRenderer(GLSurfaceView glSurfaceView) {
        super();
        mGLSurfaceView = glSurfaceView;
        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setRenderer(this);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

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
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        drawMovieFrame(mElapsedTime);
    }

    public void prepare() {
        GLES20.glClearColor(0, 0, 0, 1);
        GLESCanvas canvas = new GLES20Canvas();
        setPainter(canvas);
    }

    public void setViewport(int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        mPainter.setSize(width, height);
        setMovieViewport(0, 0, width, height);
    }

    @Override
    public void drawFrame(int elapsedTime) {
        mElapsedTime = elapsedTime;
        if (mSurfaceCreated && !mRenderToRecorder) {
            mGLSurfaceView.requestRender();
        } else {
            onDrawFrame(null);
        }
    }

    public void setRenderToRecorder(boolean renderToRecorder){
        mRenderToRecorder = renderToRecorder;
    }
}
