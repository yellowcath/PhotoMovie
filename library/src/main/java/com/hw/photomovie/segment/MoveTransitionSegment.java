package com.hw.photomovie.segment;

import android.animation.TimeInterpolator;
import android.view.animation.DecelerateInterpolator;
import com.hw.photomovie.opengl.GLESCanvas;

/**
 * Created by huangwei on 2018/9/4 0004.
 */
public class MoveTransitionSegment extends TransitionSegment<FitCenterSegment, FitCenterSegment> {

    public static int DIRECTION_HORIZON = 0;
    public static int DIRECTION_VERTICAL = 1;

    private float mScaleFrom = 1f;
    private float mScaleTo = 0.7f;
    private int mDirection;
    private TimeInterpolator mInterpolator = new DecelerateInterpolator(1);

    public MoveTransitionSegment(int direction, int duration) {
        mDirection = direction;
        setDuration(duration);
    }

    @Override
    protected void onDataPrepared() {
        mNextSegment.onDataPrepared();
    }

    @Override
    public void drawFrame(GLESCanvas canvas, float segmentProgress) {
        segmentProgress = mInterpolator.getInterpolation(segmentProgress);

        canvas.fillRect(0, 0, mViewportRect.width(), mViewportRect.height(), mPreSegment.getBackgroundColor());

        canvas.save();
        canvas.setAlpha(1 - segmentProgress);
        float scale = mScaleFrom + (mScaleTo - mScaleFrom) * segmentProgress;
        mPreSegment.drawContent(canvas, scale);
        canvas.restore();

        if (mDirection == DIRECTION_VERTICAL) {
            canvas.save();
            canvas.translate(0, (1 - segmentProgress) * mViewportRect.height());
            mNextSegment.drawContent(canvas, 1f);
            canvas.restore();
        } else {
            canvas.save();
            canvas.translate((1 - segmentProgress) * mViewportRect.width(), 0);
            mNextSegment.drawContent(canvas, 1f);
            canvas.restore();
        }
    }

    @Override
    public void onPrepare() {
        super.onPrepare();
    }

    @Override
    public void setViewport(int l, int t, int r, int b) {
        super.setViewport(l, t, r, b);
    }

}
