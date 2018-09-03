package com.hw.photomovie.util;

import android.content.res.Resources;

/**
 * Created by huangwei on 2015/6/4.
 */
public class AppResources {

    private Resources mResources;

    private AppResources() {

    }

    public void init(Resources resources) {
        mResources = resources;
    }

    public Resources getAppRes() {
        if (mResources == null) {
            throw new RuntimeException("ApplicationResource never inited.");
        }
        return mResources;
    }

    public float getAppDensity() {
        if (mResources == null) {
            throw new RuntimeException("ApplicationResource never inited.");
        }
        return mResources.getDisplayMetrics().density;
    }

    public static AppResources getInstance() {
        return Holder.INSTANCE;
    }

    private static final class Holder {
        private static AppResources INSTANCE = new AppResources();
    }
}
