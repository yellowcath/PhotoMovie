package com.hw.photomovie.util;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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

    public static String loadShaderFromAssets(String assetsName){
        AssetManager am = AppResources.getInstance().getAppRes().getAssets();
        StringBuffer stringBuffer = new StringBuffer();
        InputStream inputStream = null;
        try {
            inputStream = am.open(assetsName);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String str = null;
            while ((str = br.readLine()) != null) {
                stringBuffer.append(str);
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(inputStream!=null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return stringBuffer.toString();
    }

    public static Bitmap loadBitmapFromAssets(String assetsName){
        AssetManager am = AppResources.getInstance().getAppRes().getAssets();
        InputStream inputStream = null;
        try {
            inputStream = am.open(assetsName);
            return BitmapFactory.decodeStream(inputStream);
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(inputStream!=null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
