package com.hw.photomovie.opengl;

import com.hw.photomovie.util.Utils;

// ColorTexture is a texture which fills the rectangle with the specified color.
public class ColorTexture implements Texture {

    private final int mColor;
    private int mWidth;
    private int mHeight;

    public ColorTexture(int color) {
        mColor = color;
        mWidth = 1;
        mHeight = 1;
    }

    @Override
    public void draw(GLESCanvas canvas, int x, int y) {
        draw(canvas, x, y, mWidth, mHeight);
    }

    @Override
    public void draw(GLESCanvas canvas, int x, int y, int w, int h) {
        canvas.fillRect(x, y, w, h, mColor);
    }

    @Override
    public boolean isOpaque() {
        return Utils.isOpaque(mColor);
    }

    public void setSize(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    @Override
    public int getWidth() {
        return mWidth;
    }

    @Override
    public int getHeight() {
        return mHeight;
    }
}
