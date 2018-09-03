package com.hw.photomovie.filter;

import android.opengl.GLES20;

/**
 * Created by huangwei on 2015/6/23.
 */
public class OldMovieFilter extends MovieFilter {
    protected static final String FRAGMENT_SHADER = "precision highp float;\n" +
            "            varying vec2 textureCoordinate;\n" +
            "            vec4 coefficient = vec4(0.0,0.5,1.0,0.4); \n" +/* inverted, radius, gradient, brightness. */
            "            vec4 color = vec4(1.0,1.0,1.0,1.0);\n" +
            "            vec2 touchPoint = vec2(0.5,0.5);\n" +
            "            uniform sampler2D inputImageTexture;\n" +
            "            vec2 resolution = vec2(700.0,1000.0);\n" +
            "            uniform float uRandom;\n" +
            "            float dis = 50.0;\n" +
            "           \n" +
            "            float getMask(float radius, vec2 pos, vec2 centre)\n" +
            "            {\n" +
            "                float rDis = dis * uRandom;\n" +
            "                float xDis = rDis/resolution.x;\n" +
            "                float yDis = rDis/resolution.y;\n" +
            "                float l = 0.0 + rDis;\n" +
            "                float r = resolution.x - rDis;\n" +
            "                float b = 0.0 + rDis;\n" +
            "                float t = resolution.y - rDis;\n" +
            "                if(pos.x < r && pos.x >l && pos.y < t && pos.y > b){\n" +
            "                    float dis1 = abs(pos.x - l) / resolution.x;\n" +
            "                   float dis2 = abs(pos.x - r) / resolution.x;\n" +
            "                   float dis3 = abs(pos.y - t) / resolution.y;\n" +
            "                   float dis4 = abs(pos.y - b) / resolution.y;\n" +
            "                   //return 1.0;\n" +
            "                   float minDis = min(dis1,min(dis2,min(dis3,dis4)));\n" +
            "                   return smoothstep(0.0, coefficient.z, pow(minDis, coefficient.w));\n" +
            "                } else{\n" +
            "                   return 0.0;\n" +
            "                }\n" +
            "            }\n" +
            "\n" +
            "            void main()\n" +
            "            {\n" +
            "                vec2 centre = touchPoint;\n" +
            "                vec4 tc = texture2D(inputImageTexture,textureCoordinate);\n" +
            "                float mask = getMask(coefficient.y, gl_FragCoord.xy, centre);\n" +
            "                if (coefficient.x == 0.0)\n" +
            "                    gl_FragColor = vec4(tc*mask*color);\n" +
            "                else\n" +
            "                    gl_FragColor = vec4(tc*(1.0-coefficient.x*mask*color));\n" +
            "           }";

    private int mRandomHandle;

    public OldMovieFilter() {
        super(VERTEX_SHADER, FRAGMENT_SHADER);
    }

    @Override
    public void initShader() {
        super.initShader();
        GLHelper.checkGlError();
        mRandomHandle = GLES20.glGetUniformLocation(mProgId, "uRandom");
        GLHelper.checkGlError();
    }

    @Override
    protected void preDraw(float progress) {
        super.preDraw(progress);
        float sinCount = 5f;
        float p = progress % (1/sinCount) / (1/sinCount);
        GLES20.glUniform1f(mRandomHandle, 1 + (float) Math.sin(p * Math.PI) * .2f);
    }
}
