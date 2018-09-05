package com.hw.photomovie.segment.layer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import com.hw.photomovie.filter.MovieFilter;
import com.hw.photomovie.opengl.BitmapTexture;
import com.hw.photomovie.opengl.GLESCanvas;
import com.hw.photomovie.segment.BitmapInfo;
import com.hw.photomovie.util.AppResources;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangwei on 2015/6/9.
 */
public class MovieTextLayer extends MovieLayer {
    private String mText = "啦啦啦，啦啦啦\n我是卖报的小行家。";
    private TextPaint mTextPaint;
    private List<BitmapInfo> mTextBmList;
    private BitmapTexture mCoverTexture;
    private float mRateY = 0.7f;
    private RectF mDstRect = new RectF();
    private MovieFilter mMovieFilter;

    public MovieTextLayer() {
        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        int textSizeSp = 20;
        float textSize = AppResources.getInstance().getAppRes().getDisplayMetrics().density * textSizeSp + 0.5f;
        mTextPaint.setTextSize(textSize);
        mTextPaint.setColor(Color.RED);
        mMovieFilter = new MovieFilter();
    }

    @Override
    public void drawFrame(GLESCanvas canvas, float progress) {
        if (mTextBmList == null || mTextBmList.size() == 0) {
            return;
        }
        int y = 300;
        for (int i = 0; i < 1; i++) {
            BitmapInfo bitmapInfo = mTextBmList.get(i);
            BitmapTexture bitmapTexture = bitmapInfo.bitmapTexture;
            int w = bitmapInfo.srcRect.width();
            int h = bitmapInfo.srcRect.height();
            float cx = mViewprotRect.centerX();
            float b = mViewprotRect.height() * mRateY;
            mDstRect.set(cx - w / 2f, b - h, cx + w / 2f, b - h * progress);
            bitmapInfo.srcShowRect.set(
                    bitmapInfo.srcShowRect.left,
                    bitmapInfo.srcRect.top + h * progress,
                    bitmapInfo.srcShowRect.right,
                    bitmapInfo.srcRect.bottom);

            mMovieFilter.setRange(0,0);
            if(!bitmapTexture.isLoaded()){
                bitmapTexture.updateContent(canvas);
            }
            mMovieFilter.drawFrame(progress,bitmapTexture.getId(),bitmapInfo.srcRect,bitmapInfo.srcShowRect,mDstRect);
//            canvas.drawTexture(bitmapTexture, bitmapInfo.srcShowRect, mDstRect);
//            canvas.drawTexture(mCoverTexture, 200, y - h / 2, mCoverTexture.getBitmap().getWidth(), mCoverTexture.getBitmap().getHeight());
            y += 200;
        }
    }

    @Override
    public int getRequiredPhotoNum() {
        return 0;
    }

    @Override
    public void prepare() {
        int width = (int) (mViewprotRect.width() * 0.75f);
        StaticLayout staticLayout = new StaticLayout(mText, mTextPaint, width, Layout.Alignment.ALIGN_CENTER, 1, 0, true);
        mTextBmList = new ArrayList<BitmapInfo>(staticLayout.getLineCount());
        for (int i = 0; i < staticLayout.getLineCount(); i++) {
            int s = staticLayout.getLineStart(i);
            int e = staticLayout.getLineEnd(i);
            Bitmap bitmap = strToBitmap(mText.substring(s, e), mTextPaint);
            BitmapTexture bitmapTexture = new BitmapTexture(bitmap);
            bitmapTexture.setOpaque(false);
            BitmapInfo bitmapInfo = new BitmapInfo();
            bitmapInfo.srcRect.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
            bitmapInfo.srcShowRect.set(bitmapInfo.srcRect);
            bitmapInfo.bitmapTexture = bitmapTexture;
            mTextBmList.add(bitmapInfo);
        }
        mMovieFilter.init();
    }

    public static Bitmap strToBitmap(String str, TextPaint textPaint) {
        str = str == null ? "" : str;
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        int h = (int) (Math.abs(fontMetrics.ascent) + Math.abs(fontMetrics.descent));
        int w = (int) Math.ceil(textPaint.measureText(str));
        Bitmap bitmap = null;

        try {
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawText(str, 0, Math.abs(fontMetrics.ascent), textPaint);
        return bitmap;
    }

    @Override
    public void release() {

    }
}
