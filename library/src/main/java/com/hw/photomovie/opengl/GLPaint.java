package com.hw.photomovie.opengl;

import junit.framework.Assert;

/**
 * @Author Jituo.Xuan
 * @Date 11:40:32 AM Mar 20, 2014
 * @Comments:null
 */
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
        Assert.assertTrue(width >= 0);
        mLineWidth = width;
    }

    public float getLineWidth() {
        return mLineWidth;
    }
}
