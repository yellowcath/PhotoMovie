package com.hw.photomovie.filter;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by huangwei on 2015/6/8.
 */
public class MovieFilter {

    protected static final String VERTEX_SHADER = "" +
            "attribute vec4 position;\n" +
            "attribute vec4 inputTextureCoordinate;\n" +
            " \n" +
            "varying vec2 textureCoordinate;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "    gl_Position = position;\n" +
            "    textureCoordinate = inputTextureCoordinate.xy;\n" +
            "}";
    protected static final String FRAGMENT_SHADER = "" +
            "varying highp vec2 textureCoordinate;\n" +
            " \n" +
            "uniform sampler2D inputImageTexture;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "     gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "}";

    static final float CUBE[] = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,
    };

    static final float TEXTURE_CUBE[] = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
    };

    private String mVertexShader;
    private String mFragmentShader;

    private FloatBuffer mCubeBuffer;
    private FloatBuffer mTextureCubeBuffer;

    protected int mProgId;
    protected int mAttribPosition;
    protected int mAttribTexCoord;
    protected int mUniformTexture;

    protected boolean mIsInitialized;

    protected RectF mViewportRect = new RectF();

    private RectF mTempRect = new RectF();
    private Matrix mTempMatrix = new Matrix();
    private float[] mTempCube = new float[8];

    protected boolean mIsOpaque = false;

    protected float mRangeStart;
    protected float mRangeEnd;

    public MovieFilter() {
        this(VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public MovieFilter(String vertexShader, String fragmentShader) {
        mVertexShader = vertexShader;
        mFragmentShader = fragmentShader;
    }

    public void init() {
        if (mIsInitialized) {
            return;
        }
        loadVertex();
        initShader();
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        mIsInitialized = true;
    }

    public void loadVertex() {
        mCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mCubeBuffer.put(CUBE).position(0);

        mTextureCubeBuffer = ByteBuffer.allocateDirect(TEXTURE_CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mTextureCubeBuffer.put(TEXTURE_CUBE).position(0);
    }

    public void initShader() {
        mProgId = GLHelper.loadProgram(mVertexShader, mFragmentShader);
        GLHelper.checkGlError();
        mAttribPosition = GLES20.glGetAttribLocation(mProgId, "position");
        mUniformTexture = GLES20.glGetUniformLocation(mProgId, "inputImageTexture");
        mAttribTexCoord = GLES20.glGetAttribLocation(mProgId,
                "inputTextureCoordinate");
    }

    public void drawFrame(float progress, int glTextureId, Rect textureRext, RectF srcRect, RectF dstRect) {
        if (!mIsInitialized) {
            return;
        }
        GLHelper.checkGlError();
        if (!GLES20.glIsProgram(mProgId)) {
            initShader();
        }
        GLES20.glUseProgram(mProgId);

        preDraw(progress);

        FloatBuffer cubeBuffer = getCubeBuffer(dstRect);
        FloatBuffer textureCubeBuffer = getTextureCubeBuffer(textureRext, srcRect);

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

        if (glTextureId != GLHelper.NO_TEXTURE) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, glTextureId);
            GLES20.glUniform1i(mUniformTexture, 0);
        }
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(mAttribPosition);
        GLES20.glDisableVertexAttribArray(mAttribTexCoord);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        GLES20.glDisable(GLES20.GL_BLEND);

    }

    protected void preDraw(float progress) {

    }

    /**
     * 将android的坐标转换为opengl的顶点坐标
     *
     * @param dstRect
     * @return
     */
    protected FloatBuffer getCubeBuffer(RectF dstRect) {
        mTempRect.set(-1, -1, 1, 1);

        float scaleX = dstRect.width() / mViewportRect.width();
        float scaleY = dstRect.height() / mViewportRect.height();

        float tranX = (dstRect.left - mViewportRect.left) / mViewportRect.width() * 2;
        float tranY = (dstRect.top - mViewportRect.top) / mViewportRect.height() * 2;

        mTempMatrix.reset();
        mTempMatrix.setScale(scaleX, scaleY, -1, -1);
        mTempMatrix.postTranslate(tranX, tranY);

        mTempMatrix.mapRect(mTempRect);

        mTempRect.set(mTempRect.left, -mTempRect.top, mTempRect.right, -mTempRect.bottom);
//        mTempRect.set(mTempRect.left, -mTempRect.bottom, mTempRect.right, -mTempRect.top);

        mTempCube[0] = mTempRect.left;
        mTempCube[1] = mTempRect.bottom;
        mTempCube[2] = mTempRect.right;
        mTempCube[3] = mTempRect.bottom;
        mTempCube[4] = mTempRect.left;
        mTempCube[5] = mTempRect.top;
        mTempCube[6] = mTempRect.right;
        mTempCube[7] = mTempRect.top;

        mCubeBuffer.put(mTempCube);

        return mCubeBuffer;
    }

    /**
     * 将android的坐标转换为opengl的纹理坐标
     *
     * @param textureRect
     * @param srcRect
     * @return
     */
    protected FloatBuffer getTextureCubeBuffer(Rect textureRect, RectF srcRect) {
        mTempRect.set(0, 0, 1, 1);

        float scaleX = srcRect.width() / textureRect.width();
        float scaleY = srcRect.height() / textureRect.height();

        float tranX = (srcRect.left - textureRect.left) / (float) textureRect.width();
        float tranY = (srcRect.top - textureRect.top) / (float) textureRect.height();

        mTempMatrix.reset();
        mTempMatrix.setScale(scaleX, scaleY, 0, 0);
        mTempMatrix.postTranslate(tranX, tranY);

        mTempMatrix.mapRect(mTempRect);

        mTempRect.set(mTempRect.left, -mTempRect.top, mTempRect.right, -mTempRect.bottom);
        mTempRect.set(mTempRect.left, -mTempRect.bottom, mTempRect.right, -mTempRect.top);

        mTempCube[0] = mTempRect.left;
        mTempCube[1] = mTempRect.top;
        mTempCube[2] = mTempRect.right;
        mTempCube[3] = mTempRect.top;
        mTempCube[4] = mTempRect.left;
        mTempCube[5] = mTempRect.bottom;
        mTempCube[6] = mTempRect.right;
        mTempCube[7] = mTempRect.bottom;

        mTextureCubeBuffer.put(mTempCube);
        return mTextureCubeBuffer;
    }

    public void setViewport(int l, int t, int r, int b) {
        mViewportRect.set(l, t, r, b);
    }

    public void setOpaque(boolean bool) {
        mIsOpaque = bool;
    }

    public void setRange(float start, float end) {
        mRangeStart = start;
        mRangeEnd = end;
    }

    protected float getRate(float progress) {
        return progress;
    }

    public void destroy() {
        mIsInitialized = false;
        GLES20.glDeleteProgram(mProgId);
    }
}
