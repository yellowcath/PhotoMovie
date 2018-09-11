package com.hw.photomovie.sample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import com.hw.photomovie.moviefilter.BaseMovieFilter;
import com.hw.photomovie.moviefilter.LutMovieFilter;
import com.hw.photomovie.opengl.BitmapTexture;
import com.hw.photomovie.opengl.FboTexture;
import com.hw.photomovie.opengl.GLES20Canvas;
import com.hw.photomovie.render.GLTextureView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by huangwei on 2018/9/9.
 */
public class TestFilterRenderer implements GLTextureView.Renderer {

    private Context mContext;
    BaseMovieFilter mMovieFilter = new LutMovieFilter(LutMovieFilter.LutType.E);
    private Bitmap mBitmap;
    private int mTextureId;
    FboTexture mInputTexture = new FboTexture();
    FboTexture mOutputTexture = new FboTexture();
    private GLES20Canvas mCanvas;
    private BitmapTexture mBitmapTexture;
    private GLTextureView mTextureView;

    public TestFilterRenderer(Context context, GLTextureView glTextureView){
        mContext  =context;
        mBitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.filter_default);
        mTextureView = glTextureView;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mCanvas = new GLES20Canvas();
        mMovieFilter.init();
        mInputTexture = new FboTexture();
        mOutputTexture = new FboTexture();
        mBitmapTexture = new BitmapTexture(mBitmap);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mMovieFilter.setViewport(0,0,width,height);
        mCanvas.setSize(width,height);
        mBitmapTexture.setSize(mBitmap.getWidth(),mBitmap.getHeight());
        mInputTexture = new FboTexture();
        mOutputTexture = new FboTexture();
        mInputTexture.setSize(width,height);
        mOutputTexture.setSize(width,height);
    }

    private int n = 0;
    @Override
    public boolean onDrawFrame(GL10 gl) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,mInputTexture.getFrameBuffer());
        mCanvas.drawTexture(mBitmapTexture,0,0,mBitmapTexture.getWidth(),mBitmapTexture.getHeight());
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER,0);

        mCanvas.unbindArrayBuffer();
        mMovieFilter.doFilter(null,0,mInputTexture,mOutputTexture);
        mCanvas.rebindArrayBuffer();
        mCanvas.drawTexture(mOutputTexture,0,0,mOutputTexture.getWidth(),mOutputTexture.getHeight());


        if(n++>5) {
            File file = new File("/sdcard/l5.jpg");
            if (!file.exists()) {
                GLES20.glFinish();
                Bitmap bmp = mTextureView.getBitmap();
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(file);//注意app的sdcard读写权限问题
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                bmp.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                try {
                    fos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    @Override
    public void onSurfaceDestroyed() {

    }
}
