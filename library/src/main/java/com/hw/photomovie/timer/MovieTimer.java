package com.hw.photomovie.timer;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.animation.LinearInterpolator;
import com.hw.photomovie.PhotoMovie;

/**
 * Created by yellowcat on 2015/6/12.
 */
public class MovieTimer implements IMovieTimer, ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {

    private final ValueAnimator mAnimator;

    private MovieListener mMovieListener;

    private long mPausedPlayTime;
    private boolean mPaused;

    private PhotoMovie mPhotoMovie;
    private boolean mLoop;

    public MovieTimer(PhotoMovie photoMovie) {
        mPhotoMovie = photoMovie;

        mAnimator = ValueAnimator.ofInt(0, 1);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.addUpdateListener(this);
        mAnimator.addListener(this);
        mAnimator.setDuration(Long.MAX_VALUE);
    }

    public void start() {
        if (!mPaused) {
            mAnimator.start();
        } else {
            mAnimator.start();
        }
    }

    public void pause() {
        if (mPaused) {
            return;
        }
        mPaused = true;
        mPausedPlayTime = mAnimator.getCurrentPlayTime();
        mAnimator.cancel();
    }

    @Override
    public void setMovieListener(IMovieTimer.MovieListener movieListener) {
        this.mMovieListener = movieListener;
    }

    @Override
    public int getCurrentPlayTime() {
        return (int) mPausedPlayTime;
    }

    @Override
    public void setLoop(boolean loop) {
        mLoop = loop;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        if (mPaused || !animation.isRunning()) {
            return;
        }

        long curTime = animation.getCurrentPlayTime();

        if (curTime >= mPhotoMovie.getDuration()) {
            mAnimator.removeUpdateListener(this);
            mAnimator.removeListener(this);
            mAnimator.end();
            if (mMovieListener != null) {
                mMovieListener.onMovieEnd();
            }
            mAnimator.addUpdateListener(this);
            mAnimator.addListener(this);
            if(mLoop){
                mAnimator.start();
            }
        }else{
            if (mMovieListener != null) {
                mMovieListener.onMovieUpdate((int) curTime);
            }
        }
    }

    @Override
    public void onAnimationStart(Animator animation) {
        if (mMovieListener != null) {
            if (mPaused) {
                mMovieListener.onMovieResumed();
            } else {
                mMovieListener.onMovieStarted();
            }
        }
        if (mPaused) {
            mAnimator.setCurrentPlayTime(mPausedPlayTime);
        }
        mPaused = false;
        mPausedPlayTime = 0;
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        if (mMovieListener != null) {
            if (mPaused) {
                mMovieListener.onMoviedPaused();
            } else {
                mMovieListener.onMovieEnd();
            }
        }
    }

    @Override
    public void onAnimationCancel(Animator animation) {
        mPausedPlayTime = mAnimator.getCurrentPlayTime();
    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }
}
