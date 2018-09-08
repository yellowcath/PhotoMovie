package com.hw.photomovie.moviefilter;

import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Created by huangwei on 2018/9/8.
 */
public class CameoMovieFilter extends BaseMovieFilter {

    protected static final String FRAGMENT_SHADER = 
            "precision mediump float;\n" +
            "varying vec2 textureCoordinate;\n" +
            "uniform sampler2D inputImageTexture;\n" +
            "const highp vec3 W = vec3(0.2125, 0.7154, 0.0721);\n" +
            "const vec2 TexSize = vec2(100.0, 100.0);\n" +
            "const vec4 bkColor = vec4(0.5, 0.5, 0.5, 1.0);\n" +
            "\n" +
            "void main()\n" +
            "{\n" +
            "    vec2 tex = textureCoordinate;\n" +
            "    vec2 upLeftUV = vec2(tex.x-1.0/TexSize.x, tex.y-1.0/TexSize.y);\n" +
            "    vec4 curColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "    vec4 upLeftColor = texture2D(inputImageTexture, upLeftUV);\n" +
            "    vec4 delColor = curColor - upLeftColor;\n" +
            "    float luminance = dot(delColor.rgb, W);\n" +
            "    gl_FragColor = vec4(vec3(luminance), 0.0) + bkColor;\n" +
            "}"; 

    public CameoMovieFilter(){
        super(VERTEX_SHADER,FRAGMENT_SHADER);
    }

    @Override
    public void initShader() {
        super.initShader();
    }

    @Override
    protected void preDraw(float progress, Rect textureRect, RectF srcRect, RectF dstRect) {
        super.preDraw(progress, textureRect, srcRect, dstRect);
    }
}
