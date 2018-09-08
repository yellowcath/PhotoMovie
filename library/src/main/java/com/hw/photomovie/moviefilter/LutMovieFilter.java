package com.hw.photomovie.moviefilter;

import android.opengl.GLES20;
import android.util.Log;
import com.hw.photomovie.PhotoMovie;
import com.hw.photomovie.opengl.FboTexture;

import static com.hw.photomovie.util.AppResources.loadShaderFromAssets;

/**
 * Created by huangwei on 2018/9/8.
 */
public class LutMovieFilter extends BaseMovieFilter {

    private int mTimeHandler;
    private int mSizeHandler;
    private int mSnowSize = 3;
    private int mSpeed = 5;
    public LutMovieFilter() {
        super(VERTEX_SHADER, loadShaderFromAssets("shader/lut.glsl"));
    }

    @Override
    public void initShader() {
        super.initShader();
        mTimeHandler = GLES20.glGetUniformLocation(getProgram(),"time");
        mSizeHandler = GLES20.glGetUniformLocation(getProgram(),"resolution");
    }

    @Override
    protected void onPreDraw(PhotoMovie photoMovie, int elapsedTime, FboTexture inputTexture) {
        super.onPreDraw(photoMovie, elapsedTime, inputTexture);
        float time = elapsedTime/(float)photoMovie.getDuration();
        Log.e("hwLog","time:"+time);
        GLES20.glUniform1f(mTimeHandler,time*mSpeed);
        GLES20.glUniform2fv(mSizeHandler,1,new float[]{996,1662},0);
    }

    public void setSnowSize(int mSnowSize) {
        this.mSnowSize = mSnowSize;
    }

    public void setSpeed(int mSpeed) {
        this.mSpeed = mSpeed;
    }
}
