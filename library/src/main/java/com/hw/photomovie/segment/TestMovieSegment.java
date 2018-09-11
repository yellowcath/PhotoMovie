package com.hw.photomovie.segment;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextPaint;
import com.hw.photomovie.filter.MovieFilter;
import com.hw.photomovie.filter.OldMovieFilter;
import com.hw.photomovie.model.PhotoData;
import com.hw.photomovie.opengl.BitmapTexture;
import com.hw.photomovie.opengl.GLESCanvas;
import com.hw.photomovie.util.Utils;

/**
 * Created by huangwei on 2015/5/25.
 */
public class TestMovieSegment extends GLMovieSegment {

    private BitmapTexture mBitmapTexture;
    private MovieFilter movieFilter;

    public TestMovieSegment(int i) {
        mDuration = 2200;
        TextPaint textPaint = new TextPaint();
        textPaint.setColor(Color.RED);
        textPaint.setTextSize(88);
        movieFilter = new OldMovieFilter();
    }

    private RectF mSrcRect = new RectF();
    private RectF mDstRect = new RectF();

    private int getRanColor() {
        int colors[] = new int[]{Color.BLUE, Color.RED, Color.DKGRAY, Color.GREEN, Color.YELLOW, Color.MAGENTA, Color.WHITE};
        int i = (int) (Math.random() * colors.length);
        return colors[i];
    }

    @Override
    protected boolean checkPrepared() {
        return mBitmapTexture!=null && mBitmapTexture.isLoaded();
    }

    @Override
    public void onPrepare() {
        PhotoData photoData = getPhoto(0);
        if (photoData != null) {
            photoData.prepareData(PhotoData.STATE_BITMAP, new PhotoData.SimpleOnDataLoadListener() {
                @Override
                public void onDataLoaded(PhotoData photoData, Bitmap bitmap) {
                    boolean success = false;
                    onDataPrepared();
                    if (Utils.isBitmapAvailable(bitmap)) {
                        mBitmapTexture = new BitmapTexture(bitmap);
                        mSrcRect.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
                        success = true;
                    }
                    if (mOnSegmentPrepareListener != null) {
                        mOnSegmentPrepareListener.onSegmentPrepared(success);
                    }
                }
            });
        }
    }

    @Override
    protected void onDataPrepared() {

    }

    @Override
    public void drawFrame(GLESCanvas canvas, float segmentRate) {
        mDstRect.set(0, 0, mViewportRect.width(), mViewportRect.height());
//        if (mBitmapTexture != null) {
//            canvas.drawTexture(mBitmapTexture,mSrcRect, mDstRect);
//        }
        if (!mBitmapTexture.isLoaded()) {
            mBitmapTexture.updateContent(canvas);
        }
        Rect rect = new Rect();
        rect.set((int) mSrcRect.left, (int) mSrcRect.top, (int) mSrcRect.right, (int) mSrcRect.bottom);
        canvas.unbindArrayBuffer();
//        movieFilter.setRange(0.2f, 1f);
        movieFilter.drawFrame(segmentRate, mBitmapTexture.getId(), rect, mSrcRect, mDstRect);
        canvas.rebindArrayBuffer();
    }

    @Override
    public int getRequiredPhotoNum() {
        return 1;
    }

    @Override
    public void setViewport(int l, int t, int r, int b) {
        super.setViewport(l, t, r, b);
        movieFilter.setViewport(l, t, r, b);
        movieFilter.init();
    }

    @Override
    public void onRelease() {
        if (mBitmapTexture != null) {
            mBitmapTexture.recycle();
        }
    }
}
