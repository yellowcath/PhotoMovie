package com.hw.photomovie.opengl;

// FadeInTexture is a texture which begins with a color, then gradually animates
// into a given texture.
public class FadeInTexture extends FadeTexture implements Texture {
    private final int mColor;

    public FadeInTexture(int color, BasicTexture texture) {
        super(texture);
        mColor = color;
    }

    @Override
    public void draw(GLESCanvas canvas, int x, int y, int w, int h) {
        if (isAnimating()) {
            canvas.drawMixed(mTexture, mColor, getRatio(), x, y, w, h);
        } else {
            mTexture.draw(canvas, x, y, w, h);
        }
    }
}
