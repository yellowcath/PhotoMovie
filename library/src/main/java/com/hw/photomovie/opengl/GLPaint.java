package com.hw.photomovie.opengl;

public class GLPaint {
    private float mLineWidth = 1f;
    private int mColor = 0;

    public void setColor(int color) {
        mColor = color;
    }

    public int getColor() {
        return mColor;
    }

    public void setLineWidth(float width) {
        mLineWidth = width;
    }

    public float getLineWidth() {
        return mLineWidth;
    }
}
