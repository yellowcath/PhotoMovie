package com.hw.photomovie.segment;

import com.hw.photomovie.opengl.GLESCanvas;
import com.hw.photomovie.segment.layer.MovieLayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangwei on 2015/6/3.
 */
public abstract class AbsLayerSegment extends MulitBitmapSegment {

    protected MovieLayer[] mLayers;

    public AbsLayerSegment() {
        super();
        mLayers = initLayers();
    }

    public AbsLayerSegment(int duration) {
        super(duration);
        mLayers = initLayers();
    }

    protected abstract MovieLayer[] initLayers();

    @Override
    public void drawFrame(GLESCanvas canvas, float segmentProgress) {
        if (mLayers == null || mLayers.length == 0) {
            return;
        }

        for (int i = 0; i < mLayers.length; i++) {
            mLayers[i].drawFrame(canvas, segmentProgress);
        }
    }

    @Override
    public void setViewport(int l, int t, int r, int b) {
        super.setViewport(l, t, r, b);
        for (int i = 0; mLayers != null && i < mLayers.length; i++) {
            mLayers[i].setViewprot(l, t, r, b);
        }
    }

    @Override
    public int getRequiredPhotoNum() {
        int num = 0;
        for (int i = 0; mLayers != null && i < mLayers.length; i++) {
            num += mLayers[i].getRequiredPhotoNum();
        }
        return num;
    }

    @Override
    protected void onDataPrepared() {
        allocPhotoToLayers();
        for(int i=0;mLayers!=null && i<mLayers.length;i++){
            mLayers[i].prepare();
        }
    }

    protected void allocPhotoToLayers() {
        if (mPhotos.size() == 0 || mLayers == null || mLayers.length == 0) {
            return;
        }
        int index = 0;
        List<BitmapInfo> photoDatas = new ArrayList<BitmapInfo>();
        for (MovieLayer layer : mLayers) {
            photoDatas.clear();
            int required = layer.getRequiredPhotoNum();
            while (required > 0) {
                if (index >= mPhotos.size()) {
                    index = 0;
                }
                photoDatas.add(mPhotoDataMap.get(mPhotos.get(index)));
                --required;
                ++index;
            }
            layer.allocPhotos(photoDatas);
        }
    }

    @Override
    public void onRelease() {
        super.onRelease();
        for(int i=0;mLayers!=null && i<mLayers.length;i++){
            mLayers[i].release();
        }
    }

    @Override
    protected boolean checkPrepared() {
        return false;
    }
}
