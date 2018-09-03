package com.hw.photomovie.opengl;

// ExtTexture is a texture whose content comes from a external texture.
// Before drawing, setSize() should be called.
public class ExtTexture extends BasicTexture {

    private int mTarget;

    public ExtTexture(GLESCanvas canvas, int target) {
        GLId glId = canvas.getGLId();
        mId = glId.generateTexture();
        mTarget = target;
    }

    private void uploadToCanvas(GLESCanvas canvas) {
        canvas.setTextureParameters(this);
        setAssociatedCanvas(canvas);
        mState = STATE_LOADED;
    }

    @Override
    protected boolean onBind(GLESCanvas canvas) {
        if (!isLoaded()) {
            uploadToCanvas(canvas);
        }

        return true;
    }

    @Override
    public int getTarget() {
        return mTarget;
    }

    @Override
    public boolean isOpaque() {
        return true;
    }

    @Override
    public void yield() {
        // we cannot free the texture because we have no backup.
    }
}
