package com.hw.photomovie.util;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Created by huangwei on 14-12-31.
 */
public class PhotoUtil {
    /**
     * 根据toWidth和toHieght，返回适用于bitmap的srcRect,只裁剪不压缩
     * 裁剪方式为裁上下或两边
     *
     * @param srcRect
     * @param bitmapWidth
     * @param bitmapHeight
     * @param toWidth
     * @param toHeight
     * @return
     */
    public static Rect getCroppedRect(Rect srcRect, int bitmapWidth, int bitmapHeight, float toWidth, float toHeight) {
        if (srcRect == null) {
            srcRect = new Rect();
        }
        float rate = toWidth / toHeight;
        float bitmapRate = bitmapWidth / (float) bitmapHeight;

        if (Math.abs(rate - bitmapRate) < 0.01) {

            srcRect.left = 0;
            srcRect.top = 0;
            srcRect.right = bitmapWidth;
            srcRect.bottom = bitmapHeight;
        } else if (bitmapRate > rate) {
            //裁两边
            float cutRate = toHeight / (float) bitmapHeight;
            float toCutWidth = cutRate * bitmapWidth - toWidth;
            float toCutWidthReal = toCutWidth / cutRate;

            srcRect.left = (int) (toCutWidthReal / 2);
            srcRect.top = 0;
            srcRect.right = bitmapWidth - (int) (toCutWidthReal / 2);
            srcRect.bottom = bitmapHeight;
        } else {
            //裁上下
            float cutRate = toWidth / (float) bitmapWidth;
            float toCutHeight = cutRate * bitmapHeight - toHeight;
            float toCutHeightReal = toCutHeight / cutRate;

            srcRect.left = 0;
            srcRect.top = (int) (toCutHeightReal / 2);
            srcRect.right = bitmapWidth;
            srcRect.bottom = bitmapHeight - (int) (toCutHeightReal / 2);

        }
        return srcRect;
    }

    /**
     * 计算图片根据高宽居中情况下的目标显示区域
     *
     * @return
     */
    public static RectF getFitCenterRect(RectF dstRect, int bitmapWidth, int bitmapHeight, int outWidth, int outHeight) {
        if (dstRect == null) {
            dstRect = new RectF();
        }
        float rate = outWidth / outHeight;
        float bitmapRate = bitmapWidth / (float) bitmapHeight;

        if (Math.abs(rate - bitmapRate) < 0.01) {
            dstRect.left = 0;
            dstRect.top = 0;
            dstRect.right = outWidth;
            dstRect.bottom = outHeight;
        } else if (bitmapRate > rate) {
            //宽满，上下留白
            int showWidth = outWidth;
            int showHeight = (int) (showWidth / bitmapRate);

            dstRect.left = 0;
            dstRect.right = outWidth;
            dstRect.top = outHeight / 2 - showHeight / 2;
            dstRect.bottom = outHeight / 2 + showHeight / 2;
        } else {
            //高满，左右留白
            int showHeight = outHeight;
            int showWidth = (int) (showHeight * bitmapRate);

            dstRect.left = outWidth / 2 - showWidth / 2;
            dstRect.right = outWidth / 2 + showWidth / 2;
            dstRect.top = 0;
            dstRect.bottom = outHeight;
        }
        return dstRect;
    }


    public static Bitmap createBitmapOrNull(int width, int height, Bitmap.Config config) {
        try {
            return Bitmap.createBitmap(width, height, config);
        } catch (OutOfMemoryError e) {
            return null;
        }
    }
}
