package com.hw.photomovie.opengl.animations;


import com.hw.photomovie.util.MLog;

public class IntegerAnim extends Animation {

    private static final String TAG = "IntegerAnim";

    private int mTarget;
    private int mCurrent = 0;
    private int mFrom = 0;
    private boolean mEnabled = false;

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public void startAnimateTo(int target) {

        if (!mEnabled) {
            mTarget = mCurrent = target;
            return;
        }
        MLog.i(TAG, " target:" + target + " mTarget:" + mTarget);
        if (target == mTarget) {
            return;
        }

        mFrom = mCurrent;
        mTarget = target;
        setDuration(300);
        start();
    }

    public int get() {
        return mCurrent;
    }

    public int getTarget() {
        return mTarget;
    }

    @Override
    protected void onCalculate(float progress) {
        mCurrent = Math.round(mFrom + (1 - progress) * (mTarget - mFrom));
        if (progress == 0f) {
            mEnabled = false;
        }
    }
}
