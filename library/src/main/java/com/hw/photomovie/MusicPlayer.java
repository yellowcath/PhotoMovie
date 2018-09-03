package com.hw.photomovie;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.IOException;

/**
 * Created by huangwei on 2015/6/1.
 */
public class MusicPlayer {
    private static final int FADE_DURATION = 1800;

    private MediaPlayer mMediaPlayer;
    private MediaPlayer.OnErrorListener mOnErrorListener;
    private FadeOutRunnable mFadeOutRunnable;

    public MusicPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setLooping(true);
    }

    public void start() {
        if (mFadeOutRunnable != null) {
            mFadeOutRunnable.cancel();
            mFadeOutRunnable = null;
        }
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.setVolume(1f, 1f);
            mMediaPlayer.start();
        }
    }

    public void stop() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            try {
                mMediaPlayer.prepare();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mMediaPlayer.seekTo(0);
        }
    }

    /**
     * 淡出式停止
     *
     * @param handler
     */
    public void fadeStop(Handler handler) {
        mFadeOutRunnable = new FadeOutRunnable(handler, FADE_DURATION);
        handler.postDelayed(mFadeOutRunnable, 1000);
    }

    public void pause() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    public void release() {
        mMediaPlayer.release();
    }
    public void setDataSource(String path) {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            if (mOnErrorListener != null) {
                mOnErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_IO, 0);
            }
        }
    }

    public void setDataSource(FileDescriptor fileDescriptor) {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(fileDescriptor);
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            if (mOnErrorListener != null) {
                mOnErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_IO, 0);
            }
        }
    }

    public void setDataSource(AssetFileDescriptor assetFileDescriptor) {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(
                    assetFileDescriptor.getFileDescriptor(),
                    assetFileDescriptor.getStartOffset(),
                    assetFileDescriptor.getLength());
            assetFileDescriptor.close();
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            if (mOnErrorListener != null) {
                mOnErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_IO, 0);
            }
        }
    }

    public void setDataSource(Context ctx, Uri uri) {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(ctx, uri);
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            if (mOnErrorListener != null) {
                mOnErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_IO, 0);
            }
        }
    }

    public void setErrorListener(MediaPlayer.OnErrorListener onErrorListener) {
        mMediaPlayer.setOnErrorListener(onErrorListener);
    }

    public void setLooping(boolean bool) {
        mMediaPlayer.setLooping(bool);
    }

    public boolean isPlaying(){
        return mMediaPlayer.isPlaying();
    }

    public void seekTo(int i) {
        mMediaPlayer.seekTo(i);
    }

    class FadeOutRunnable implements Runnable {
        private int mDuration;
        private long mStartTime;
        private Handler mHandler;
        private boolean mCancel;

        public FadeOutRunnable(Handler handler, int duration) {
            mDuration = duration;
            mHandler = handler;
        }

        @Override
        public void run() {
            if(mCancel){
                return;
            }
            long curTime = System.currentTimeMillis();
            if (mStartTime == 0) {
                mStartTime = curTime;
            }
            if (curTime - mStartTime > mDuration) {
                synchronized (this) {
                    if(mMediaPlayer.isPlaying()) {
                        mMediaPlayer.stop();
                        try {
                            mMediaPlayer.prepare();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                return;
            }
            float rate = 1 - (curTime - mStartTime) / (float) mDuration;
            mMediaPlayer.setVolume(rate, rate);
            mHandler.postDelayed(this, 50);
        }

        public void cancel() {
            mCancel = true;
            mHandler.removeCallbacks(this);
            synchronized (this) {
                if(mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                    try {
                        mMediaPlayer.prepare();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
