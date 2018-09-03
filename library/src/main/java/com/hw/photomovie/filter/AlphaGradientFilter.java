package com.hw.photomovie.filter;

import android.graphics.Rect;
import android.graphics.RectF;
import android.opengl.GLES20;

/**
 * Created by huangwei on 2015/6/9.
 * range[0~1],以top为0，bottom为1，从start到end，alpha值渐变[0~1]
 */
public class AlphaGradientFilter extends MovieFilter {

    protected static final String ALPHA_FRAGMENT_SHADER = "" +
            "precision mediump float;\n" +
            "varying highp vec2 textureCoordinate;\n" +
            "uniform sampler2D inputImageTexture;\n" +
            "uniform float uStart;\n" +
            "uniform float uEnd;\n" +
            "float uAlpha;\n" +
            "float y;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "    gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n" +
            "    y = textureCoordinate.y;" +
            "    if(uStart<uEnd){\n" +
            "        if(y <= uStart){\n" +
            "            uAlpha = 0.0;\n" +
            "        } else if(y >=uEnd){\n" +
            "            uAlpha = 1.0;\n" +
            "        } else{\n" +
            "            uAlpha = (y - uStart) / (uEnd - uStart);\n" +
            "        }\n" +
            "    } else if(uStart == uEnd){\n" +
            "        uAlpha = 1.0;\n" +
            "    } else{\n" +
            "        if(y >= uStart){\n" +
            "            uAlpha = 0.0;\n" +
            "        } else if(y <=uEnd){\n" +
            "            uAlpha = 1.0;\n" +
            "        } else{\n" +
            "            uAlpha = (y - uStart) / (uEnd - uStart);\n" +
            "        } \n" +
            "    }" +
            "     gl_FragColor *=uAlpha;\n" +
            "}";


    private int mStartLocation;
    private int mEndLocation;

    public AlphaGradientFilter() {
        super(VERTEX_SHADER, ALPHA_FRAGMENT_SHADER);
    }

    @Override
    public void init() {
        super.init();
        mStartLocation = GLES20.glGetUniformLocation(mProgId, "uStart");
        mEndLocation = GLES20.glGetUniformLocation(mProgId, "uEnd");
    }

    @Override
    public void drawFrame(float progress, int glTextureId, Rect textureRext, RectF srcRect, RectF dstRect) {
        float baseY = (srcRect.top - textureRext.top) / textureRext.height();
        float scaleY = srcRect.height() / textureRext.height();
        mRangeStart = baseY + mRangeStart * scaleY;
        mRangeEnd = baseY + mRangeEnd * scaleY;
        GLES20.glUniform1f(mStartLocation, mRangeStart);
        GLES20.glUniform1f(mEndLocation, mRangeEnd);
        super.drawFrame(progress, glTextureId, textureRext, srcRect, dstRect);
    }

    @Override
    public void setRange(float start, float end) {
        super.setRange(start, end);
    }
}

