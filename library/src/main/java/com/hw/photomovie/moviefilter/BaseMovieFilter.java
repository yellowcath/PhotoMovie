package com.hw.photomovie.moviefilter;

import android.graphics.Rect;
import android.graphics.RectF;
import android.opengl.GLES20;
import com.hw.photomovie.PhotoMovie;
import com.hw.photomovie.filter.GLHelper;
import com.hw.photomovie.opengl.FboTexture;
import com.hw.photomovie.record.gles.GlUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by huangwei on 2015/6/8.
 */
public class BaseMovieFilter implements IMovieFilter {

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

    protected FloatBuffer mCubeBuffer;
    protected FloatBuffer mTextureCubeBuffer;

    protected int mProgId;
    protected int mAttribPosition;
    protected int mAttribTexCoord;
    protected int mUniformTexture;

    protected boolean mIsInitialized;

    protected RectF mViewportRect = new RectF();

    protected boolean mIsOpaque = false;

    public BaseMovieFilter() {
        this(VERTEX_SHADER, FRAGMENT_SHADER);
    }

    public BaseMovieFilter(String vertexShader, String fragmentShader) {
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
        if (mProgId == 0) {
            throw new RuntimeException("loadProgram fail");
        }
        GLHelper.checkGlError();
        mAttribPosition = GLES20.glGetAttribLocation(mProgId, "position");
        mUniformTexture = GLES20.glGetUniformLocation(mProgId, "inputImageTexture");
        mAttribTexCoord = GLES20.glGetAttribLocation(mProgId,
                "inputTextureCoordinate");
    }

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

        onPreDraw(photoMovie, elapsedTime, inputTexture);

        FloatBuffer cubeBuffer = mCubeBuffer;
        FloatBuffer textureCubeBuffer = mTextureCubeBuffer;

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
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(mAttribPosition);
        GLES20.glDisableVertexAttribArray(mAttribTexCoord);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        GLES20.glDisable(GLES20.GL_BLEND);

    }

    protected void onPreDraw(PhotoMovie photoMovie, int elapsedTime, FboTexture inputTexture) {

    }

    public void setViewport(int l, int t, int r, int b) {
        mViewportRect.set(l, t, r, b);
    }

    public void setOpaque(boolean bool) {
        mIsOpaque = bool;
    }

    private Rect textureRect = new Rect();
    private RectF dstRect = new RectF();

    @Override
    public void doFilter(PhotoMovie photoMovie, int elapsedTime, FboTexture inputTexture, FboTexture outputTexture) {
        textureRect.set(0, 0, inputTexture.getTextureWidth(), inputTexture.getTextureHeight());
        dstRect.set(0, 0, outputTexture.getWidth(), outputTexture.getHeight());
        if (!mIsInitialized) {
            setViewport(0, 0, outputTexture.getWidth(), outputTexture.getHeight());
            init();
        }
        int[] curFb = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, curFb, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, outputTexture.getFrameBuffer());
        GlUtil.checkGlError("glBindFramebuffer");
        drawFrame(photoMovie,
                elapsedTime,
                inputTexture);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, curFb[0]);
        GlUtil.checkGlError("glBindFramebuffer");
    }

    public int getProgram() {
        return mProgId;
    }

    @Override
    public void release() {
        mIsInitialized = false;
        if (GLES20.glIsProgram(mProgId)) {
            GLES20.glDeleteProgram(mProgId);
            mProgId = 0;
        }
    }
}
