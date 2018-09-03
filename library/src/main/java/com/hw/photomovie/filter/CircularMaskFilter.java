package com.hw.photomovie.filter;

/**
 * Created by huangwei on 2015/6/23.
 */
public class CircularMaskFilter extends MovieFilter {
    protected static final String FRAGMENT_SHADER ="precision highp float;\n" +
            "varying highp vec2 textureCoordinate;\n" +
            "\n" +
            "vec4 coefficient = vec4(0.0,0.5,1.0,1.0); /* inverted, radius, gradient, brightness. */\n" +
            "vec4 color = vec4(1.0,1.0,1.0,1.0);\n" +
            "\n" +
            "vec2 touchPoint = vec2(0.5,0.5);\n" +
            "\n" +
            "uniform sampler2D inputImageTexture;\n" +
            "\n" +
            "float getMask(float radius, vec2 pos, vec2 centre)\n" +
            "{\n" +
            "    float dist = distance(pos, centre);\n" +
            "    \n" +
            "    if (dist < radius) {\n" +
            "        float dd = dist/radius;\n" +
            "        \n" +
            "        return smoothstep(0.0, coefficient.z, 1.0 - pow(dd, coefficient.w));\n" +
            "    }\n" +
            "    \n" +
            "    return 0.0;\n" +
            "}\n" +
            "\n" +
            "void main(void)\n" +
            "{\n" +
            "    vec2 centre = touchPoint;\n" +
            "    \n" +
            "    vec4 tc = texture2D(inputImageTexture,textureCoordinate);\n" +
            "    \n" +
            "    float mask = getMask(coefficient.y, textureCoordinate, centre);\n" +
            "    \n" +
            "    if (coefficient.x == 0.0)\n" +
            "        gl_FragColor = vec4(tc*mask*color);\n" +
            "    else\n" +
            "        gl_FragColor = vec4(tc*(1.0-coefficient.x*mask*color));\n" +
//            "gl_FragColor = tc;"+
            "}";

    public CircularMaskFilter(){
        super(VERTEX_SHADER,FRAGMENT_SHADER);
    }
}
