package com.hw.photomovie.render

import android.opengl.GLES20
import com.hw.photomovie.opengl.GLES20Canvas
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by huangwei on 2018/9/6 0006.
 */
class GLTextureMovieRender() : GLMovieRenderer() {

    lateinit var mGLTextureView: GLTextureView

    constructor(glTextrureView: GLTextureView) : this() {
        mGLTextureView = glTextrureView
        mGLTextureView.setEGLContextClientVersion(2)
        mGLTextureView.setRenderer(object:GLTextureView.Renderer{

            override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
                prepare()
                mSurfaceCreated = true
            }

            override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
                setViewport(width, height)
            }

            override fun onSurfaceDestroyed() {

            }
            override fun onDrawFrame(gl: GL10?):Boolean {
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
                drawMovieFrame(mElapsedTime)
                return true
            }
        })
        mGLTextureView.renderMode = GLTextureView.RENDERMODE_WHEN_DIRTY
    }


    override fun prepare() {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        val canvas = GLES20Canvas()
        setPainter(canvas)
    }

    override fun drawFrame(elapsedTime: Int) {
        mElapsedTime = elapsedTime
        if (mSurfaceCreated && !mRenderToRecorder) {
            mGLTextureView.requestRender()
        } else {
            onDrawFrame(null)
        }
    }

}