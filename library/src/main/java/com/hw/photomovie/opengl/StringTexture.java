package com.hw.photomovie.opengl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

// StringTexture is a texture shows the content of a specified String.
//
// To create a StringTexture, use the newInstance() method and specify
@SuppressWarnings("checkstyle:membername")
// the String, the font size, and the color.
public class StringTexture extends CanvasTexture {
    private final StringArray mText;
    private final TextPaint mPaint;

    private StringTexture(StringArray text, TextPaint paint, int width, int height) {
        super(width, height);
        mText = text;
        mPaint = paint;
    }

    public static TextPaint getDefaultPaint(float textSize, int color, boolean hasShadow) {
        TextPaint paint = new TextPaint();
        paint.setTextSize(textSize);
        paint.setAntiAlias(true);
        paint.setColor(color);
        if (hasShadow) {
            paint.setShadowLayer(2f, 0f, 0f, Color.BLACK);
        }
        return paint;
    }

    public static TextPaint getPaintNoShadow(float textSize, int color) {
        TextPaint paint = new TextPaint();
        paint.setTextSize(textSize);
        paint.setAntiAlias(true);
        paint.setColor(color);
        return paint;
    }

    public static StringTexture newInstance(
            String text, float textSize, int color, boolean hasShadow) {
        return newInstance(text, getDefaultPaint(textSize, color, hasShadow));
    }

    public static StringTexture newInstance(
            String text, float textSize, int color) {
        return newInstance(text, getDefaultPaint(textSize, color, true));
    }

    public static StringTexture newInstance(
            String text, float textSize, int color,
            float lengthLimit, boolean isBold) {
        TextPaint paint = getDefaultPaint(textSize, color, true);
        if (isBold) {
            paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        }
        if (lengthLimit > 0) {
            text = TextUtils.ellipsize(
                    text, paint, lengthLimit, TextUtils.TruncateAt.END).toString();
        }
        return newInstance(text, paint);
    }

    public static StringTexture newInstance(String text, TextPaint paint) {
        ArrayList<String> textList = new ArrayList<String>();

        int paraStartIndex = 0;
        int paraEndIndex = text.indexOf('\n', paraStartIndex);
        int paraCount = 0;

        int width = 0;
        if (paraEndIndex < 0) {
            width = (int) Math.ceil(paint.measureText(text));
            paraCount = 1;
            textList.add(text);
        } else {
            while (paraEndIndex != -1) {
                paraCount++;
                String para = text.substring(paraStartIndex, paraEndIndex);
                textList.add(para);
                int paraWidth = (int) Math.ceil(paint.measureText(para));
                if (paraWidth > width) {
                    width = paraWidth;
                }
                paraStartIndex = paraEndIndex + 1;
                if (paraStartIndex >= text.length()) {
                    paraEndIndex = -1;
                } else {
                    paraEndIndex = text.indexOf('\n', paraStartIndex);
                }
            }

            // last line
            if (paraStartIndex < text.length() - 1) {
                paraCount++;
                String para = text.substring(paraStartIndex, text.length());
                textList.add(para);
                int paraWidth = (int) Math.ceil(paint.measureText(para));
                if (paraWidth > width) {
                    width = paraWidth;
                }
            }
        }

        FontMetricsInt metrics = paint.getFontMetricsInt();
        int height = (metrics.bottom - metrics.top) * paraCount;

        // The texture size needs to be at least 1x1.
        if (width <= 0) {
            width = 1;
        }
        if (height <= 0) {
            height = 1;
        }
        return new StringTexture(new StringArray(textList), paint, width, height);
    }

    public static StringTexture newInstance(StringArray stringArray) {
        TextPaint paint;
        int width = 0;
        if (!stringArray.hasPaintInfo()) {
            paint = getDefaultPaint(16, Color.BLACK, true);
        } else {
            paint = new TextPaint();
            paint.setAntiAlias(true);
            for (int i = 0; i < stringArray.getCount(); i++) {
                String para = stringArray.getStrings().get(i);
                int paintSize = stringArray.getPaintSizes().get(i);
                int paintColor = stringArray.getPaintColors().get(i);
                paint.setColor(paintColor);
                paint.setTextSize(paintSize);
                int paraWidth = (int) Math.ceil(paint.measureText(para));
                if (paraWidth > width) {
                    width = paraWidth;
                }
            }

            paint.setColor(stringArray.getPaintColors().get(0));
            paint.setTextSize(stringArray.getPaintSizes().get(0));
        }
        FontMetricsInt metrics = paint.getFontMetricsInt();
        int height = (metrics.bottom - metrics.top) * stringArray.getCount();

        // The texture size needs to be at least 1x1.
        if (width <= 0) {
            width = 1;
        }
        if (height <= 0) {
            height = 1;
        }
        return new StringTexture(stringArray, paint, width, height);
    }

    @Override
    protected void onDraw(Canvas canvas, Bitmap backing) {
        FontMetricsInt metrics = mPaint.getFontMetricsInt();
        canvas.translate(0, -metrics.ascent);
        int height = 0;
        if (!mText.hasPaintInfo()) {
            for (String para : mText.getStrings()) {
                canvas.drawText(para, 0, height, mPaint);
                height = height + (metrics.bottom - metrics.top);
            }
        } else {
            for (int i = 0; i < mText.getCount(); i++) {
                String para = mText.getStrings().get(i);
                int paintColor = mText.getPaintColors().get(i);
                int paintSize = mText.getPaintSizes().get(i);
                mPaint.setColor(paintColor);
                mPaint.setTextSize(paintSize);
                canvas.drawText(para, 0, height, mPaint);
                metrics = mPaint.getFontMetricsInt();
                height = height + (metrics.bottom - metrics.top);
            }
        }
    }

    public static class StringArray {
        private List<String> strings;
        private List<Integer> paintSizes;
        private List<Integer> paintColors;

        public StringArray() {
            strings = new ArrayList<String>();
            paintSizes = new ArrayList<Integer>();
            paintColors = new ArrayList<Integer>();
        }

        // use only internal
        StringArray(List<String> strings) {
            this.strings = strings;
            paintSizes = new ArrayList<Integer>();
            paintColors = new ArrayList<Integer>();
        }

        public List<String> getStrings() {
            return strings;
        }

        public List<Integer> getPaintColors() {
            return paintColors;
        }

        public List<Integer> getPaintSizes() {
            return paintSizes;
        }

        public int getCount() {
            return strings.size();
        }

        public boolean hasPaintInfo() {
            return paintColors.size() > 0
                    && paintSizes.size() > 0
                    && paintSizes.size() == paintColors.size()
                    && paintSizes.size() == strings.size();
        }

        public void add(String string, int paintSize, int paintColor) {
            strings.add(string);
            paintSizes.add(paintSize);
            paintColors.add(paintColor);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null && this == null) {
                return true;
            }

            if (o instanceof StringArray && this != null) {
                StringArray that = (StringArray) o;
                return that.getStrings().equals(this.getStrings())
                        && that.getPaintColors().equals(this.getPaintColors())
                        && that.getPaintSizes().equals(this.getPaintSizes());
            }

            return false;
        }
    }
}
