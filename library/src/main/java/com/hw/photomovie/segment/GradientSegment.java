package com.hw.photomovie.segment;

import com.hw.photomovie.opengl.GLESCanvas;

/**
 * Created by huangwei on 2018/9/12 0012.
 */
public class GradientSegment extends FitCenterSegment {
    /**
     * 缩放动画范围
     */
    private float mScaleFrom;
    private float mScaleTo;
    /**
     * 开始做alpha动画的进度
     */
    private float mAlphaStartProgress;
    private float mProgress;

    /**
     * @param duration 片段时长
     * @param alphaDuration 后半段的alpha动画时长
     * @param scaleFrom 缩放范围
     * @param scaleTo 缩放范围
     */
    public GradientSegment(int duration, int alphaDuration,float scaleFrom,float scaleTo) {
        super(duration);
        mScaleFrom = scaleFrom;
        mScaleTo = scaleTo;
        mAlphaStartProgress = (duration-alphaDuration) / (float) duration;
    }

    @Override
    protected void onDataPrepared() {
        super.onDataPrepared();
    }

    @Override
    public void drawFrame(GLESCanvas canvas, float segmentProgress) {
        mProgress = segmentProgress;
        super.drawFrame(canvas, segmentProgress);
    }

    @Override
    protected void drawContent(GLESCanvas canvas, float scale) {
        //FitCenterSegment已提供了缩放功能，这里我们只要提供自己需要的放大倍率即可
        scale = mScaleFrom + (mScaleTo - mScaleFrom) * mProgress;

        if (mProgress < mAlphaStartProgress) {
            //只展示放大动画
            super.drawContent(canvas, scale);
        } else {
            float alpha = 1 - (mProgress - mAlphaStartProgress) / (1 - mAlphaStartProgress);
            //加上alpha效果
            canvas.save();
            canvas.setAlpha(alpha);
            super.drawContent(canvas, scale);
            canvas.restore();
        }
    }

    @Override
    public boolean showNextAsBackground() {
        return true;
    }
}
