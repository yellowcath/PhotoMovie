package com.hw.photomovie.render;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import com.hw.photomovie.opengl.GLES20Canvas;
import com.hw.photomovie.opengl.GLESCanvas;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by huangwei on 2015/5/26.
 */
public class GLSurfaceMovieRenderer extends GLMovieRenderer implements GLSurfaceView.Renderer {

    private GLSurfaceView mGLSurfaceView;

    protected volatile boolean mSurfaceCreated;
    /**
     * 录制时不再渲染到GLSurfaceView上
     */
    protected boolean mRenderToRecorder = false;

    /**
     * 无GLSurfaceView的构造函数用于{@link com.hw.photomovie.record.GLMovieRecorder},会在外部设置GLES输出的Surface
     */
    public GLSurfaceMovieRenderer() {
        super();
    }

    /**
     * 无GLSurfaceView的构造函数用于{@link com.hw.photomovie.record.GLMovieRecorder},会在外部设置GLES输出的Surface
     */
    public GLSurfaceMovieRenderer(GLSurfaceMovieRenderer movieRenderer) {
        super(movieRenderer);
    }

    protected AtomicBoolean mNeedRelease = new AtomicBoolean(false);

    public GLSurfaceMovieRenderer(GLSurfaceView glSurfaceView) {
        super();
        mGLSurfaceView = glSurfaceView;
        mGLSurfaceView.setEGLContextClientVersion(2);
        mGLSurfaceView.setRenderer(this);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mSurfaceCreated = true;
        mNeedRelease.set(false);
        //资源起其实已经销毁了，这里只是告知其保存的纹理已经不可用，需要重建
        if (mMovieFilter != null) {
            mMovieFilter.release();
        }
        if(mCoverSegment!=null){
            mCoverSegment.release();
        }
        releaseTextures();
        prepare();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        setViewport(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if(mNeedRelease.get()){
            mNeedRelease.set(false);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            drawMovieFrame(mElapsedTime);
            releaseGLResources();
            return;
        }
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        drawMovieFrame(mElapsedTime);
    }

    public void prepare() {
        GLES20.glClearColor(0, 0, 0, 1);
        GLESCanvas canvas = new GLES20Canvas();
        setPainter(canvas);
        if(mCurrentSegment!=null){
            mCurrentSegment.prepare();
        }
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

    @Override
    public void release() {
        if(mGLSurfaceView!=null) {
            mNeedRelease.set(true);
            if (mSurfaceCreated) {
                mGLSurfaceView.requestRender();
            }
        }
    }

    public void releaseInGLThread(){
        releaseGLResources();
    }

    public boolean isSurfaceCreated() {
        return mSurfaceCreated;
    }

    public void setRenderToRecorder(boolean renderToRecorder){
        mRenderToRecorder = renderToRecorder;
    }

}
