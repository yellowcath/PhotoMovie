package com.hw.photomovie.opengl;

/**
 * @Author Jituo.Xuan
 * @Date 11:38:11 AM Mar 20, 2014
 * @Comments:Texture is a rectangular image which can be drawn on GLCanvas.
 */
public interface Texture {
    public int getWidth();

    public int getHeight();

    public void draw(GLESCanvas canvas, int x, int y);

    public void draw(GLESCanvas canvas, int x, int y, int w, int h);

    public boolean isOpaque();
}
