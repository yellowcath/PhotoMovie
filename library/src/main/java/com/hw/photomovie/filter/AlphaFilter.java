package com.hw.photomovie.filter;

import android.graphics.Rect;
import android.graphics.RectF;
import android.opengl.GLES20;

/**
 * Created by huangwei on 2015/6/9.
 */
public class AlphaFilter extends MovieFilter {

    private static final String ALPHA_UNIFORM = "uAlpha";

    protected static final String ALPHA_FRAGMENT_SHADER = "" +
            "precision mediump float;\n" +
            "varying highp vec2 textureCoordinate;\n" +
            " \n" +
            "uniform sampler2D inputImageTexture;\n" +
            " \n" +
            "uniform float " + ALPHA_UNIFORM + ";\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "     gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "     gl_FragColor *=" + ALPHA_UNIFORM + ";\n" +
            "}";

    private int mAlphaLocation;

    public AlphaFilter() {
        super(VERTEX_SHADER, ALPHA_FRAGMENT_SHADER);
    }

    @Override
    public void init() {
        super.init();
        mAlphaLocation = GLES20.glGetUniformLocation(mProgId, ALPHA_UNIFORM);
    }

    @Override
    public void drawFrame(float progress, int glTextureId, Rect textureRext, RectF srcRect, RectF dstRect) {
        GLES20.glUniform1f(mAlphaLocation, progress);
        super.drawFrame(progress, glTextureId, textureRext, srcRect, dstRect);
    }
}
