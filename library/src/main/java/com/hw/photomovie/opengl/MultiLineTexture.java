package com.hw.photomovie.opengl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;


// MultiLineTexture is a texture shows the content of a specified String.
//
// To create a MultiLineTexture, use the newInstance() method and specify
// the String, the font size, and the color.
class MultiLineTexture extends CanvasTexture {
    private final Layout mLayout;

    private MultiLineTexture(Layout layout) {
        super(layout.getWidth(), layout.getHeight());
        mLayout = layout;
    }

    public static MultiLineTexture newInstance(
            String text, int maxWidth, float textSize, int color,
            Layout.Alignment alignment) {
        TextPaint paint = StringTexture.getDefaultPaint(textSize, color, true);
        Layout layout = new StaticLayout(text, 0, text.length(), paint,
                maxWidth, alignment, 1, 0, true, null, 0);

        return new MultiLineTexture(layout);
    }

    @Override
    protected void onDraw(Canvas canvas, Bitmap backing) {
        mLayout.draw(canvas);
    }
}
