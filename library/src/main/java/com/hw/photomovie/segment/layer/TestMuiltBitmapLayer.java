package com.hw.photomovie.segment.layer;

import android.graphics.Rect;
import com.hw.photomovie.opengl.GLESCanvas;
import com.hw.photomovie.segment.BitmapInfo;
import com.hw.photomovie.util.PhotoUtil;
import com.hw.photomovie.util.ScaleType;

import java.util.List;

/**
 * Created by huangwei on 2015/6/3.
 */
public class TestMuiltBitmapLayer extends MovieLayer {

    private Rect mTempRect = new Rect();

    @Override
    public void drawFrame(GLESCanvas canvas, float progress) {
        AvailableRect[] dstAvailableRect = mParentLayer == null ? null : mParentLayer.getChildLayerRects(progress);

        if (mBitmapInfos == null || dstAvailableRect == null || dstAvailableRect.length == 0) {
            return;
        }

        for (int i = 0; i < dstAvailableRect.length; i++) {
            AvailableRect availableRect = dstAvailableRect[i];
            BitmapInfo bitmapInfo = i < mBitmapInfos.size() ? mBitmapInfos.get(i) : null;
            if (bitmapInfo == null || bitmapInfo.bitmapTexture == null || availableRect == null || availableRect.rectF == null) {
                continue;
            }
            //更新bitmap显示区域
            if (bitmapInfo.scaleType == ScaleType.CENTER_CROP) {
                PhotoUtil.getCroppedRect(
                        mTempRect,
                        bitmapInfo.srcRect.width(),
                        bitmapInfo.srcRect.height(),
                        dstAvailableRect[i].rectF.width(),
                        dstAvailableRect[i].rectF.height());
                bitmapInfo.srcShowRect.set(mTempRect);
            }

            canvas.save();
            float cx, cy;
            if (availableRect.rotationPivot == null) {
                cx = availableRect.rectF.centerX();
                cy = availableRect.rectF.centerY();
            } else {
                cx = availableRect.rotationPivot.x;
                cy = availableRect.rotationPivot.y;
            }
            canvas.translate(cx, cy);
            canvas.rotate(-availableRect.rotation, 0f, 0f, -1f);
            canvas.translate(-cx, -cy);
            canvas.drawTexture(bitmapInfo.bitmapTexture, bitmapInfo.srcShowRect, availableRect.rectF);
            canvas.restore();
        }
    }

    @Override
    public void allocPhotos(List<BitmapInfo> bitmapInfos) {
        super.allocPhotos(bitmapInfos);
    }

    @Override
    public AvailableRect[] getChildLayerRects(float progress) {
        return null;
    }

    @Override
    public int getRequiredPhotoNum() {
        return 4;
    }

    @Override
    public void prepare() {

    }

    @Override
    public void release() {

    }
}
