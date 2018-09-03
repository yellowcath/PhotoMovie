package com.hw.photomovie.opengl;

import android.graphics.RectF;

public interface ScreenNail {
    public int getWidth();

    public int getHeight();

    public boolean isReady();

    public void setLoadingTexture(StringTexture loadingTexture);

    public void draw(GLESCanvas canvas, int x, int y, int width, int height);

    // We do not need to draw this ScreenNail in this frame.
    public void noDraw();

    // This ScreenNail will not be used anymore. Release related resources.
    public void recycle();

    // This is only used by TileImageView to back up the tiles not yet loaded.
    public void draw(GLESCanvas canvas, RectF source, RectF dest);
}
