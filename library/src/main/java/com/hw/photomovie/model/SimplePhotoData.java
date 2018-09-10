package com.hw.photomovie.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by huangwei on 2018/9/3 0003.
 */
public class SimplePhotoData extends PhotoData {

    private ExecutorService mPool = Executors.newFixedThreadPool(4);
    private Context mContext;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    public SimplePhotoData(Context context, String uri, int state) {
        super(uri, state);
        mContext = context.getApplicationContext();
    }

    @Override
    public void prepareData(int targetState, final OnDataLoadListener onDataLoadListener) {
        mTargetState = targetState;
        switch (mState) {
            case STATE_BITMAP:
                if (targetState == STATE_BITMAP && onDataLoadListener != null) {
                    onDataLoadListener.onDataLoaded(this, getBitmap());
                }else if(targetState == STATE_LOCAL && onDataLoadListener != null){
                    onDataLoadListener.onDownloaded(this);
                }
                break;
            case STATE_LOADING:
                break;
            case STATE_LOCAL:
                if (targetState == STATE_BITMAP) {
                    mPool.submit(new Runnable() {
                        @Override
                        public void run() {
                            mState = STATE_LOADING;
                            mBitmap = loadBitmap(getUri());
                            if (mBitmap != null) {
                                if (mTargetState == STATE_LOCAL) {
                                    mState = STATE_LOCAL;
                                } else if (mTargetState == STATE_BITMAP) {
                                    mState = STATE_BITMAP;
                                }
                                if (onDataLoadListener != null) {
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (mTargetState >= STATE_LOCAL) {
                                                onDataLoadListener.onDownloaded(SimplePhotoData.this);
                                            }
                                            if (mTargetState == STATE_BITMAP) {
                                                onDataLoadListener.onDataLoaded(SimplePhotoData.this, mBitmap);
                                            }
                                        }
                                    });
                                }
                            } else {
                                mState = STATE_ERROR;
                                if (onDataLoadListener != null) {
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            onDataLoadListener.onError(SimplePhotoData.this, null);
                                        }
                                    });
                                }
                            }
                        }
                    });
                } else if (targetState == STATE_LOCAL && onDataLoadListener != null) {
                    onDataLoadListener.onDownloaded(this);
                }
                break;
            case STATE_DOWNLOADING:
                break;
            case STATE_REMOTE:
            case STATE_ERROR:
                mPool.submit(new Runnable() {
                    @Override
                    public void run() {
                        mState = STATE_LOADING;
                        loadBitmap(getUri());
                        mState = STATE_BITMAP;
                    }
                });
                break;
        }
    }

    private Bitmap loadBitmap(String uri) {
        Bitmap bitmap = null;
        if (uri.startsWith("drawable://")) {
            String idStr = uri.substring("drawable://".length());
            int id = Integer.parseInt(idStr);
            bitmap = BitmapFactory.decodeResource(mContext.getResources(), id);
        } else if (uri.startsWith("file://")) {
            String path = uri.substring("file://".length());
            bitmap = BitmapFactory.decodeFile(path);
        } else if(uri.startsWith("http")){
            InputStream is = null;
            try {
                URL url = new URL(uri);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                is = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(is);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }else{
            String path = uri;
            bitmap = BitmapFactory.decodeFile(path);
        }
        return bitmap;
    }
}
