package com.hw.photomovie.segment.layer;

import android.graphics.PointF;
import android.graphics.RectF;
import com.hw.photomovie.opengl.BitmapTexture;
import com.hw.photomovie.opengl.GLESCanvas;

/**
 * Created by huangwei on 2015/6/3.
 */
public class TestBaseLayer extends MovieLayer {

    protected BitmapTexture mBitmapTexture;
    protected RectF mSrcRect = new RectF();

    public TestBaseLayer() {
//        NEXT_AVAILABLE_RECT = new float[]{
//                0.25f, 0.25f, 0.35f, 0.35f, 0f,
//                0.5f, 0.5f, 0.85f, 0.9f, 60f
//        };
        NEXT_AVAILABLE_RECT = new float[]{
                40 / 800f, 110 / 589f, 410 / 800f, 388 / 598f, 0f,
                607 / 800f, 43 / 589f, 741 / 800f, 141 / 598f, 0f,
                623 / 800f, 229 / 589f, 731 / 800f, 376 / 598f, 0f,
                607 / 800f, 468 / 589f, 741 / 800f, 564 / 598f, 0f
        };
    }

    @Override
    public void prepare() {
//        Bitmap bitmap = BitmapFactory.decodeResource(AppResources.getInstance().getAppRes(), R.drawable.testpng);
//        mSrcRect.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
//        mBitmapTexture = new BitmapTexture(bitmap);
//        mBitmapTexture.setOpaque(false);
    }

    @Override
    public void release() {
        if (mBitmapTexture != null) {
            mBitmapTexture.recycle();
        }
    }

    @Override
    public void drawFrame(GLESCanvas canvas, float progress) {
        if (mBitmapTexture != null) {
            canvas.save();
            canvas.translate(mViewprotRect.centerX(), mViewprotRect.centerY());
            canvas.rotate(-0, 0, 0, -1f);
            canvas.translate(-mViewprotRect.centerX(), -mViewprotRect.centerY());
            canvas.drawTexture(mBitmapTexture, mSrcRect, mViewprotRect);
            canvas.restore();
        }
    }

    @Override
    public AvailableRect[] getChildLayerRects(float progress) {
        AvailableRect[] availableRects = super.getChildLayerRects(progress);
        for (int i = 0; availableRects != null && i < availableRects.length; i++) {
            availableRects[i].rotation =  0;
                    //720*2 * progress;
            if (availableRects[i].rotationPivot == null) {
                availableRects[i].rotationPivot = new PointF();
            }
            availableRects[i].rotationPivot.set(mViewprotRect.centerX(), mViewprotRect.centerY());
        }
        return availableRects;
    }

    @Override
    public int getRequiredPhotoNum() {
        return 0;
    }
}
