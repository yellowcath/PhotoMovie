package com.hw.photomovie.opengl.animations;


import com.hw.photomovie.opengl.GLESCanvas;

public abstract class CanvasAnim extends Animation {

    public abstract int getCanvasSaveFlags();

    public abstract void apply(GLESCanvas canvas);
}
