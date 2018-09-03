package com.hw.photomovie.segment;

import android.text.TextUtils;
import com.hw.photomovie.model.PhotoData;
import com.hw.photomovie.model.PhotoInfo;
import com.hw.photomovie.opengl.GLESCanvas;
import com.hw.photomovie.segment.layer.MovieLayer;
import com.hw.photomovie.segment.layer.ScaleTransLayer;
import com.hw.photomovie.segment.layer.SubtitleLayer;

import java.util.List;

/**
 * Created by huangwei on 2015/6/10.
 */
public class ScaleTransSegment extends AbsLayerSegment{

    private static final int SCALE_DURATION_SHORT = 2000;
    private static final int SCALE_DURATION_LONG = 3000;
    private static final int TRANS_DURATION_LONG = 700;

    private int mScaleDuration;

    private String mDesStr;

    private ScaleTransLayer mScaleLayer;
    private SubtitleLayer mSubtitleLayer;
    private float mScaleRate;

    public ScaleTransSegment(){
        IS_DURATION_VARIABLE = true;
    }
    @Override
    protected MovieLayer[] initLayers() {
        mScaleLayer = new ScaleTransLayer(1f, 1.05f);
        mSubtitleLayer = new SubtitleLayer();
        return new MovieLayer[]{mScaleLayer, mSubtitleLayer};
    }

    @Override
    public void drawFrame(GLESCanvas canvas, float segmentProgress) {
        super.drawFrame(canvas, segmentProgress);
    }

    @Override
    public void onPrepare() {
        super.onPrepare();
        PhotoInfo photoInfo = null;
        if (mPhotos != null && mPhotos.size() > 0) {
            photoInfo = mPhotos.get(0).getPhotoInfo();
        }
        initSubtitle(photoInfo);
    }

    @Override
    public void allocPhotos(List<PhotoData> photos) {
        super.allocPhotos(photos);
//        PhotoInfo photoInfo = null;
//        if (getAllocatedPhotos() != null && getAllocatedPhotos().size() > 0) {
//            photoInfo = getAllocatedPhotos().get(0).getPhotoInfo();
//        }
//        initSubtitle(photoInfo);
    }

    private void initSubtitle(PhotoInfo photoInfo) {
        mDesStr = photoInfo == null ? null : photoInfo.description;
        //字幕
        if (TextUtils.isEmpty(mDesStr)) {
            mScaleDuration = SCALE_DURATION_SHORT;
        } else {
            int count = mDesStr.length() / 15;
            count += mDesStr.length() % 15 == 0 ? 0 : 1;
            mScaleDuration = count * SCALE_DURATION_LONG;
        }
        mScaleRate = mScaleDuration / (float) (mScaleDuration + TRANS_DURATION_LONG);
        mScaleLayer.setScaleRate(mScaleRate);
        mSubtitleLayer.setDisappearRate(SCALE_DURATION_LONG / (float) (SCALE_DURATION_LONG + TRANS_DURATION_LONG));
        mSubtitleLayer.setSubtitle(mDesStr);
    }

    @Override
    public int getDuration() {
        return mScaleDuration + TRANS_DURATION_LONG;
    }

    @Override
    public int getRequiredPhotoNum() {
        return 1;
    }

    @Override
    public boolean showNextAsBackground() {
        return true;
    }
}
