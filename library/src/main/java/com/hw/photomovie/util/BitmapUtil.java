package com.hw.photomovie.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;

/**
 * Created by huangwei on 2018/10/27.
 */
public class BitmapUtil {
    public static Bitmap generateBitmap(String text,int textSizePx,int textColor){
        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(textSizePx);
        textPaint.setColor(textColor);
        int width = (int) Math.ceil(textPaint.measureText(text));
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        int height = (int) Math.ceil(Math.abs(fontMetrics.bottom) + Math.abs(fontMetrics.top));
        Bitmap bitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawText(text,0,Math.abs(fontMetrics.ascent),textPaint);
        return bitmap;
    }
}
