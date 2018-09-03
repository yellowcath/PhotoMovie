package com.hw.photomovie.segment.animation;

import android.animation.TimeInterpolator;
import android.view.animation.LinearInterpolator;

/**
 * Created by huangwei on 2015/5/29.
 */
public abstract class SegmentAnimation {
    protected TimeInterpolator mInterpolator = new LinearInterpolator();

    public abstract Object update(float progress);

    public void setInterpolator(TimeInterpolator interpolator) {
        mInterpolator = interpolator;
    }
}
