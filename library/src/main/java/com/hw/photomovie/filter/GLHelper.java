package com.hw.photomovie.filter;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;
import com.hw.photomovie.util.MLog;

/**
 * Created by huangwei on 2015/6/8.
 */
public class GLHelper {

    private static final String TAG = "GLHelper";
    public static final int NO_TEXTURE = -1;

    public static int loadProgram(String vSource, String fSource) {
        int iVShader;
        int iFShader;
        int iProgram;
        int[] link = new int[1];
        iVShader = loadShader(vSource, GLES20.GL_VERTEX_SHADER);
        if (iVShader == 0) {
            Log.d(TAG, "Load Vertex Shader Failed");
            return 0;
        }
        iFShader = loadShader(fSource, GLES20.GL_FRAGMENT_SHADER);
        if (iFShader == 0) {
            Log.d(TAG, ":Load Fragment Shader Failed");
            return 0;
        }
        iProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(iProgram, iVShader);
        checkGlError("");
        GLES20.glAttachShader(iProgram, iFShader);
        checkGlError("");
        GLES20.glLinkProgram(iProgram);
        checkGlError("");
        GLES20.glGetProgramiv(iProgram, GLES20.GL_LINK_STATUS, link, 0);
        if (link[0] <= 0) {
            Log.d(TAG, "Linking Failed");
            return 0;
        }
        GLES20.glDeleteShader(iVShader);
        checkGlError("");

        GLES20.glDeleteShader(iFShader);
        checkGlError("");

        return iProgram;
    }

    public static int loadShader(String shaderStr, int type) {
        int[] compiled = new int[1];
        int iShader = GLES20.glCreateShader(type);
        checkGlError("");
        GLES20.glShaderSource(iShader, shaderStr);
        checkGlError("");
        GLES20.glCompileShader(iShader);
        checkGlError("");
        GLES20.glGetShaderiv(iShader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        checkGlError("");
        if (compiled[0] == 0) {
            throw new RuntimeException("Load Shader Failed Compilation\n" + GLES20.glGetShaderInfoLog(iShader));
        }
        return iShader;
    }

    public static int loadTexture(final Bitmap bitmap, final int usedTextureId) {
        int[] textures = new int[1];
        if (usedTextureId == NO_TEXTURE) {
            GLES20.glGenTextures(1, textures, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        } else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, usedTextureId);
            GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, bitmap);
            textures[0] = usedTextureId;
        }
        return textures[0];
    }

    public static void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            if(MLog.DEBUG) {
                Log.e(TAG, ": glError " + error);
                throw new RuntimeException(": glError " + error);
            } else{
                Log.e(TAG, ": glError " + error,new Throwable());
            }
        }
    }

    public static void checkGlError() {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            if(MLog.DEBUG) {
                Log.e(TAG, ": glError " + error);
                throw new RuntimeException(": glError " + error);
            } else{
                Log.e(TAG, ": glError " + error,new Throwable());
            }
        }
    }
}
