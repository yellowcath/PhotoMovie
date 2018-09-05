package com.hw.photomovie.segment.layer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextPaint;
import android.text.TextUtils;
import com.hw.photomovie.opengl.BitmapTexture;
import com.hw.photomovie.opengl.GLESCanvas;
import com.hw.photomovie.segment.BitmapInfo;
import com.hw.photomovie.util.AppResources;
import com.hw.photomovie.util.PhotoUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangwei on 2015/6/10.
 */
public class SubtitleLayer extends MovieLayer {

    private static final int MAX_LEN = 15;
    private static final int MARGIN_BOTTOM_DP = 20;
    private static final float STROKE_WIDTH_DP = 1.5f;


    private String mText;
    private int mMarginBottom;
    private List<BitmapInfo> mBitmapInfos = new ArrayList<BitmapInfo>();

    private RectF mDstRect = new RectF();
    /**
     * 进度超过这个点就开始消失
     */
    private float mDisappearRate;

    public SubtitleLayer() {
    }

    @Override
    public void drawFrame(GLESCanvas canvas, float progress) {
        if (mBitmapInfos == null || mBitmapInfos.size() == 0) {
            return;
        }
        float rate = 1f / mBitmapInfos.size();
        int index = (int) (progress / rate);
        index = index >= mBitmapInfos.size() ? (mBitmapInfos.size() - 1) : index;
        float subProgress = progress % rate / rate;
        BitmapInfo bitmapInfo = mBitmapInfos.get(index);

        float cx = mViewprotRect.centerX();
        float w = bitmapInfo.srcShowRect.width();
        float b = mViewprotRect.bottom - mMarginBottom;
        mDstRect.set(cx - w / 2f, b - bitmapInfo.srcShowRect.height(), cx + w / 2f, b);

        if (subProgress < mDisappearRate) { //渐现
            subProgress = (mDisappearRate -subProgress)/mDisappearRate;
            canvas.drawMixed(bitmapInfo.bitmapTexture, 0, subProgress, bitmapInfo.srcShowRect, mDstRect);
        } else { //消失
            subProgress = (subProgress - mDisappearRate) / (1 - mDisappearRate);
            canvas.drawMixed(bitmapInfo.bitmapTexture, 0, subProgress, bitmapInfo.srcShowRect, mDstRect);
        }
    }

    public void setDisappearRate(float rate) {
        mDisappearRate = rate;
    }

    @Override
    public int getRequiredPhotoNum() {
        return 0;
    }

    @Override
    public void prepare() {

    }

    private Bitmap genBitmapFromStr(String str, TextPaint textPaint, float density) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }

        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        int h = (int) (Math.abs(fontMetrics.ascent) + Math.abs(fontMetrics.descent));
        int w = (int) textPaint.measureText(str);
        Bitmap bitmap = PhotoUtil.createBitmapOrNull(w, h, Bitmap.Config.ARGB_4444);
        if (bitmap == null) {
            return null;
        }
        Canvas canvas = new Canvas(bitmap);
        // 描外层
        textPaint.setColor(0xFF343434);
        textPaint.setStrokeWidth(density * STROKE_WIDTH_DP + 0.5f);  // 描边宽度
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE); //描边种类
        textPaint.setFakeBoldText(true); // 外层text采用粗体
//        textPaint.setShadowLayer(1, 0, 0, 0); //字体的阴影效果，可以忽略
        canvas.drawText(str, 0, Math.abs(fontMetrics.ascent), textPaint);

        // 描内层，恢复原先的画笔
        textPaint.setColor(0xFFFFFFFF);
        textPaint.setStrokeWidth(0);
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setFakeBoldText(false);
//        textPaint.setShadowLayer(0, 0, 0, 0);
        canvas.drawText(str, 0, Math.abs(fontMetrics.ascent), textPaint);

        return bitmap;
    }

    @Override
    public void setViewprot(int l, int t, int r, int b) {
        super.setViewprot(l, t, r, b);
    }

    @Override
    public void release() {
        if (mBitmapInfos != null) {
            for (BitmapInfo bitmapInfo : mBitmapInfos) {
                bitmapInfo.bitmapTexture.recycle();
            }
            mBitmapInfos.clear();
        }
    }

    public void setSubtitle(String subtitle) {
        mText = subtitle;
        genSubtitles();
    }

    private void genSubtitles() {
        if (TextUtils.isEmpty(mText)) {
            return;
        }

        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        float density = AppResources.getInstance().getAppRes().getDisplayMetrics().density;
        int textSizeSp = 18;
        int textSize = (int) (density * textSizeSp + 0.5f);
        textPaint.setTextSize(textSize);

        List<String> strList = new ArrayList<String>();
        StringBuilder str = new StringBuilder(mText);
        while (str.length() > 0) {
            if (str.length() > MAX_LEN) {
                strList.add(str.substring(0, MAX_LEN));
                str.delete(0, MAX_LEN);
            } else {
                strList.add(str.toString());
                str.delete(0, str.length());
            }
        }

        for (String subtitle : strList) {
            Bitmap bitmap = genBitmapFromStr(subtitle, textPaint, density);
            if (bitmap != null) {
                BitmapInfo bitmapInfo = new BitmapInfo();
                bitmapInfo.bitmapTexture = new BitmapTexture(bitmap);
                bitmapInfo.bitmapTexture.setOpaque(false);
                bitmapInfo.srcRect.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
                bitmapInfo.srcShowRect.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
                mBitmapInfos.add(bitmapInfo);
            }
        }

        mMarginBottom = (int) (density * MARGIN_BOTTOM_DP + 0.5f);
    }
}
