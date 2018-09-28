package com.hw.photomovie.segment;

import android.opengl.GLES20;
import com.hw.photomovie.opengl.GLESCanvas;

/**
 * Created by huangwei on 2018/9/12 0012.
 */
public class GradientTransferSegment extends TransitionSegment<FitCenterScaleSegment, FitCenterScaleSegment> {
    /**
     * 缩放动画范围
     */
    private float mPreScaleFrom;
    private float mPreScaleTo;
    private float mNextScaleFrom;
    private float mNextScaleTo;

    public GradientTransferSegment(int duration,
                                   float preScaleFrom, float preScaleTo,
                                   float nextScaleFrom, float nextScaleTo) {
        mPreScaleFrom = preScaleFrom;
        mPreScaleTo = preScaleTo;
        mNextScaleFrom = nextScaleFrom;
        mNextScaleTo = nextScaleTo;
        setDuration(duration);
    }

    @Override
    protected void onDataPrepared() {

    }

    @Override
    public void drawFrame(GLESCanvas canvas, float segmentProgress) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        //下一个片段开始放大
        float nextScale = mNextScaleFrom + (mNextScaleTo - mNextScaleFrom) * segmentProgress;
        mNextSegment.drawContent(canvas, nextScale);

        //上一个片段继续放大同时变透明
        float preScale = mPreScaleFrom + (mPreScaleTo - mPreScaleFrom) * segmentProgress;
        float alpha = 1 - segmentProgress;
        mPreSegment.drawBackground(canvas);
        canvas.save();
        canvas.setAlpha(alpha);
        mPreSegment.drawContent(canvas, preScale);
        canvas.restore();
    }
}
