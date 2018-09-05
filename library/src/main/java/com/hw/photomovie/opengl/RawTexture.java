package com.hw.photomovie.opengl;

import com.hw.photomovie.util.MLog;

import javax.microedition.khronos.opengles.GL11;

public class RawTexture extends BasicTexture {
    private static final String TAG = "RawTexture";

    private final boolean mOpaque;
    private boolean mIsFlipped;

    public RawTexture(int width, int height, boolean opaque) {
        mOpaque = opaque;
        setSize(width, height);
    }

    @Override
    public boolean isOpaque() {
        return mOpaque;
    }

    @Override
    public boolean isFlippedVertically() {
        return mIsFlipped;
    }

    public void setIsFlippedVertically(boolean isFlipped) {
        mIsFlipped = isFlipped;
    }

    public void prepare(GLESCanvas canvas) {
        GLId glId = canvas.getGLId();
        mId = glId.generateTexture();
        canvas.initializeTextureSize(this, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE);
        canvas.setTextureParameters(this);
        mState = STATE_LOADED;
        setAssociatedCanvas(canvas);
    }

    @Override
    protected boolean onBind(GLESCanvas canvas) {
        if (isLoaded()) {
            return true;
        }
        MLog.w(TAG, "lost the content due to context change");
        return false;
    }

    @Override
    public void yield() {
        // we cannot free the texture because we have no backup.
    }

    @Override
    protected int getTarget() {
        return GL11.GL_TEXTURE_2D;
    }
}
