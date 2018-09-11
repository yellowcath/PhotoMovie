package com.hw.photomovie.sample.activityAnim;

import android.app.Activity;
import android.widget.TextView;
import com.hw.photomovie.segment.MovieSegment;

/**
 * Created by huangwei on 2015/7/1.
 */
public class ActivityAnimSegment extends MovieSegment<Activity> {

    private float mTransX, mTransY;
    private float mStepX, mStepY;

    @Override
    protected boolean checkPrepared() {
        return false;
    }

    @Override
    protected void onPrepare() {
        mTransX = mTransY = 0;
        mStepX = getRanStep();
        mStepY = getRanStep();
        if (mOnSegmentPrepareListener != null) {
            mOnSegmentPrepareListener.onSegmentPrepared(true);
        }
        onDataPrepared();
    }

    @Override
    protected void onDataPrepared() {

    }

    @Override
    public void drawFrame(Activity painter, float segmentProgress) {
        TextView textView = (TextView) painter.findViewById(android.R.id.content).findViewWithTag("text");
        textView.setRotation(segmentProgress * 7200);

        int maxX = (int) (mViewportRect.width() / 2);
        int maxY = (int) (mViewportRect.height() / 2);

        if (!(mTransX > -maxX && mTransX < maxX)) {
            mStepX = getRanStep();
            mStepX = mTransX > 0 ? -mStepX : mStepX;
        }
        mTransX += mStepX;

        if (!(mTransY > -maxY && mTransY < maxY)) {
            mStepY = getRanStep();
            mStepY = mTransY > 0 ? -mStepY : mStepY;
        }
        mTransY += mStepY;

        textView.setTranslationX(mTransX);
        textView.setTranslationY(mTransY);
    }

    private float getRanStep() {
        return (float) Math.abs(Math.random() * 40 - 20);
    }

    @Override
    public int getRequiredPhotoNum() {
        return 0;
    }

    @Override
    protected void onRelease() {

    }
}
