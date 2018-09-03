package com.hw.photomovie.filter;

import android.opengl.GLES20;

/**
 * Created by huangwei on 2015/6/19.
 * 只展示所给的两条之间中间部分图片内容
 */
public class BetweenLinesFilter extends MovieFilter {
    protected static final String VERTEX_SHADER = "" +
            "attribute vec4 position;\n" +
            "attribute vec4 inputTextureCoordinate;\n" +
            " \n" +
            "varying vec2 textureCoordinate;\n" +
            "varying vec2 vPosition;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "    gl_Position = position;\n" +
            "    vPosition = position.xy;\n" +
            "    textureCoordinate = inputTextureCoordinate.xy;\n" +
            "}";
    protected static final String FRAGMENT_SHADER =
            "precision mediump float;" +
                    "varying highp vec2 textureCoordinate;\n" +
                    " \n" +
                    "uniform sampler2D inputImageTexture;\n" +
                    "varying mediump vec2 vPosition;\n" +
                    "uniform float k1,k2,b1,b2;\n" +
                    "uniform int kExistedInt;\n" +
                    "bool isBetweenLines(float x,float y){\n" +
                    "bool kExisted = kExistedInt == 0?false:true;"+
                    "   float cx = x;\n" +
                    "   float cy = y;\n" +
                    "   float disX1,disX2;\n" +
                    "   if(b1==b2){\n" +
                    "      return false;\n" +
                    "   }" +
                    "   if(kExisted){\n" +
                    "if(k1==k2 && k1==0.0){\n" +
                    "   disX1 = b1 - cy;\n" +
                    "   disX2 = b2 - cy;\n" +
                    "}\n" +
                    "else{\n" +
                    "      float ck = -1.0 / k1;\n" +
                    "      float cb = cy - ck * cx;\n" +
                    "      //求两个交点\n" +
                    "      float x1 = (cb - b1)/(k1-ck);\n" +
                    "      float y1 = k1 * x1+b1;\n" +
                    "      float x2 = (cb - b2)/(k2-ck);\n" +
                    "      float y2 = k2*x2 + b2;\n" +
                    "      //两个交点是否在中点的同侧\n" +
                    "     disX1 = x1 - cx;\n" +
                    "     disX2 = x2 - cx;\n" +
                    "}\n" +
                    "   }else{\n" +
                    "     //不存在斜率，即方程为 x = 0,1,2...的样式\n" +
                    "     disX1 = b1 - cx;\n" +
                    "     disX2 = b2 - cx;\n" +
                    "   }\n" +
                    " if((disX1>=0.0 && disX2>=0.0)\n" +
                    "    || (disX1 <=0.0 && disX2 <=0.0)){\n" +
                    "     return false;\n" +
                    " } else{\n" +
                    "   return true;\n" +
                    " }\n" +
                    "}\n" +
                    "void main()\n" +
                    "{\n" +
                    "     mediump vec4 color = texture2D(inputImageTexture, textureCoordinate);\n" +
                    "    if(!isBetweenLines(vPosition.x,vPosition.y)){\n" +
                    "       color.argb = vec4(0.0,0.0,0.0,0.0);" +
                    "    }" +
                    "     gl_FragColor = color;\n" +
                    "}";

    private float mK1, mK2, mB1, mB2;
    private boolean mKExisted;
    private int mK1Handle, mK2Handle, mB1Handle, mB2Handle, mKExistedHandler;


    public BetweenLinesFilter() {
        super(VERTEX_SHADER, FRAGMENT_SHADER);
    }

    @Override
    public void initShader() {
        super.initShader();
        mK1Handle = GLES20.glGetUniformLocation(mProgId, "k1");
        mK2Handle = GLES20.glGetUniformLocation(mProgId, "k2");
        mB1Handle = GLES20.glGetUniformLocation(mProgId, "b1");
        mB2Handle = GLES20.glGetUniformLocation(mProgId, "b2");
        mKExistedHandler = GLES20.glGetUniformLocation(mProgId, "kExistedInt");
    }

    public void setLines(float k1, float b1, float k2, float b2, boolean kExisted) {
        mK1 = k1;
        mB1 = b1;
        mK2 = k2;
        mB2 = b2;
        mKExisted = kExisted;
    }

    @Override
    protected void preDraw(float progress) {
        super.preDraw(progress);
        GLES20.glUniform1f(mK1Handle, mK1);
        GLES20.glUniform1f(mB1Handle, mB1);
        GLES20.glUniform1f(mK2Handle, mK2);
        GLES20.glUniform1f(mB2Handle, mB2);
        GLES20.glUniform1i(mKExistedHandler, mKExisted ? 1 : 0);
    }
}
