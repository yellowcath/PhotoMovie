package com.hw.photomovie.segment.layer;

import android.graphics.RectF;
import com.hw.photomovie.opengl.GLESCanvas;
import com.hw.photomovie.segment.BitmapInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangwei on 2015/6/3.
 */
public abstract class MovieLayer {

    protected List<BitmapInfo> mBitmapInfos = new ArrayList<BitmapInfo>();
    protected RectF mViewprotRect = new RectF();

    protected MovieLayer mParentLayer;
    /**
     * 给child指定的显示区域
     */
    protected AvailableRect[] mBaseChildAvailableRect;
    /**
     * 指定下一层的可用区域，左上角0,0,右下角1,1
     * 该数组长度必须是5的整数倍
     * 每五个元素分别代表一个显示区域的x1,y1,x2,y2,rotation
     * 在{@link #setViewprot(int, int, int, int)}时会将次数组转化成实际的{@link #mBaseChildAvailableRect}数组
     */
    protected float[] NEXT_AVAILABLE_RECT;
    private String subtitle;

    public abstract void drawFrame(GLESCanvas canvas, float progress);

    /**
     * 获取该层指定的更上层的输出区域
     *
     * @return
     */
    public AvailableRect[] getChildLayerRects(float progress) {
        return mBaseChildAvailableRect;
    }

    /**
     * 指定该层的parent
     */
    public void setParentLayer(MovieLayer parentLayer) {
        mParentLayer = parentLayer;
    }

    public void setViewprot(int l, int t, int r, int b) {
        mViewprotRect.set(l, t, r, b);
        if (NEXT_AVAILABLE_RECT == null || NEXT_AVAILABLE_RECT.length == 0 || NEXT_AVAILABLE_RECT.length % 5 != 0) {
            //默认指定chilid的显示区域为整个窗口区域
            AvailableRect availableRect = new AvailableRect();
            availableRect.rectF = new RectF(mViewprotRect);
            availableRect.rotation = 0;
            mBaseChildAvailableRect = new AvailableRect[]{availableRect};
            return;
        }
        mBaseChildAvailableRect = new AvailableRect[NEXT_AVAILABLE_RECT.length / 5];
        for (int i = 0; i < NEXT_AVAILABLE_RECT.length; i += 5) {
            float x1 = NEXT_AVAILABLE_RECT[i];
            float y1 = NEXT_AVAILABLE_RECT[i + 1];
            float x2 = NEXT_AVAILABLE_RECT[i + 2];
            float y2 = NEXT_AVAILABLE_RECT[i + 3];
            float rotation = NEXT_AVAILABLE_RECT[i + 4];

            AvailableRect availableRect = new AvailableRect();
            availableRect.rectF = new RectF(
                    l + mViewprotRect.width() * x1,
                    t + mViewprotRect.height() * y1,
                    l + mViewprotRect.width() * x2,
                    t + mViewprotRect.height() * y2);
            availableRect.rotation = rotation;
            mBaseChildAvailableRect[i / 5] = availableRect;
        }
    }

    /**
     * 该layer需要多少张照片
     *
     * @return
     */
    public abstract int getRequiredPhotoNum();

    public void allocPhotos(List<BitmapInfo> bitmapInfos) {
        mBitmapInfos.clear();
        mBitmapInfos.addAll(bitmapInfos);
    }

    public abstract void prepare();

    public abstract void release();
}
