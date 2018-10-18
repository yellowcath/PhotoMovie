package com.hw.photomovie.moviefilter;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import com.hw.photomovie.PhotoMovie;
import com.hw.photomovie.filter.GLHelper;
import com.hw.photomovie.opengl.FboTexture;
import com.hw.photomovie.util.GLUtil;
import com.hw.photomovie.record.gles.GlUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static com.hw.photomovie.util.AppResources.loadShaderFromAssets;

/**
 * Created by huangwei on 2015/6/8.
 */
public class TwoTextureMovieFilter extends BaseMovieFilter {

    public int mTexture2CoordinateAttribute;
    public int mTexture2Uniform2;
    public int mTexture2Id = GLUtil.NO_TEXTURE;
    private FloatBuffer mTexture2CoordinatesBuffer;

    public TwoTextureMovieFilter() {
        super(loadShaderFromAssets("shader/two_vertex.glsl"), loadShaderFromAssets("shader/two_fragment.glsl"));
    }

    public TwoTextureMovieFilter(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
    }

    public Bitmap mBitmap;

    @Override
    public void loadVertex() {
        super.loadVertex();
        mTexture2CoordinatesBuffer = ByteBuffer.allocateDirect(TEXTURE_CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mTexture2CoordinatesBuffer.put(TEXTURE_CUBE).position(0);
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    @Override
    public void initShader() {
        super.initShader();
        mTexture2CoordinateAttribute = GLES20.glGetAttribLocation(getProgram(), "inputTextureCoordinate2");
        mTexture2Uniform2 = GLES20.glGetUniformLocation(getProgram(), "inputImageTexture2");

    }

    @Override
    public void drawFrame(PhotoMovie photoMovie, int elapsedTime, FboTexture inputTexture) {
        if (!mIsInitialized) {
            return;
        }
        GLHelper.checkGlError();
        if (!GLES20.glIsProgram(mProgId)) {
            initShader();
            GlUtil.checkGlError("initShader");
        }
        GLES20.glUseProgram(mProgId);

        loadBitmap();
        onPreDraw(photoMovie, elapsedTime, inputTexture);

        FloatBuffer cubeBuffer = mCubeBuffer;
        FloatBuffer textureCubeBuffer = mTextureCubeBuffer;
        ;

        if (mIsOpaque) {
            GLES20.glDisable(GLES20.GL_BLEND);
        } else {
            GLES20.glEnable(GLES20.GL_BLEND);
        }

        cubeBuffer.position(0);
        GLES20.glVertexAttribPointer(mAttribPosition, 2, GLES20.GL_FLOAT, false, 0, cubeBuffer);
        GLES20.glEnableVertexAttribArray(mAttribPosition);

        textureCubeBuffer.position(0);
        GLES20.glVertexAttribPointer(mAttribTexCoord, 2, GLES20.GL_FLOAT, false, 0,
                textureCubeBuffer);
        GLES20.glEnableVertexAttribArray(mAttribTexCoord);

        int glTextureId = inputTexture.getId();
        if (glTextureId != GLHelper.NO_TEXTURE) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, glTextureId);
            GLES20.glUniform1i(mUniformTexture, 0);
        }
        //加载第二纹理
        if (mTexture2CoordinateAttribute >= 0) {
            GLES20.glEnableVertexAttribArray(mTexture2CoordinateAttribute);
            mTexture2CoordinatesBuffer.position(0);
            GLES20.glVertexAttribPointer(mTexture2CoordinateAttribute, 2, GLES20.GL_FLOAT, false, 0, mTexture2CoordinatesBuffer);
        }

        if (mTexture2Id >= 0) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexture2Id);
            GLES20.glUniform1i(mTexture2Uniform2, 3);
        }


        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(mAttribPosition);
        GLES20.glDisableVertexAttribArray(mAttribTexCoord);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        GLES20.glDisable(GLES20.GL_BLEND);
    }

    private void loadBitmap() {
        if (mTexture2Id == GLUtil.NO_TEXTURE) {
            if (mTexture2Id != GLUtil.NO_TEXTURE) {
                GLES20.glDeleteTextures(1, new int[]{mTexture2Id}, 0);
            }
            mTexture2Id = GLUtil.loadTexture(mBitmap, GLUtil.NO_TEXTURE, false);
        }
    }

    @Override
    public void release() {
        super.release();
        GLES20.glDeleteTextures(1, new int[]{
                mTexture2Id
        }, 0);
        mTexture2Id = GLUtil.NO_TEXTURE;
    }
}
