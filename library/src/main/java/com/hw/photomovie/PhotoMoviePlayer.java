package com.hw.photomovie;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Handler;
import com.hw.photomovie.model.ErrorReason;
import com.hw.photomovie.model.PhotoData;
import com.hw.photomovie.model.PhotoSource;
import com.hw.photomovie.music.IMusicPlayer;
import com.hw.photomovie.music.MusicPlayer;
import com.hw.photomovie.render.GLMovieRenderer;
import com.hw.photomovie.render.GLSurfaceMovieRenderer;
import com.hw.photomovie.render.MovieRenderer;
import com.hw.photomovie.segment.MovieSegment;
import com.hw.photomovie.timer.IMovieTimer;
import com.hw.photomovie.timer.MovieTimer;
import com.hw.photomovie.util.AppResources;
import com.hw.photomovie.util.MLog;

import java.io.FileDescriptor;
import java.util.List;

/**
 * Created by huangwei on 2015/5/22.
 */
public class PhotoMoviePlayer implements MovieTimer.MovieListener {

    private static final String TAG = "PhotoMoviePlayer";
    /**
     * 加载第一个片段到可播放状态在总准备进度里占的比例
     */
    protected static final float FIRST_SEGMENT_PREPARE_RATE = 0.05f;
    // all possible internal states
    public static final int STATE_ERROR = -1;
    public static final int STATE_IDLE = 0;
    public static final int STATE_PREPARING = 1;
    public static final int STATE_PREPARED = 2;
    public static final int STATE_PLAYING = 3;
    public static final int STATE_PAUSED = 4;
    public static final int STATE_PLAYBACK_COMPLETED = 5;

    private int mCurrentState = STATE_IDLE;

    private PhotoMovie mPhotoMovie;
    private MovieRenderer mMovieRenderer;

    private IMovieTimer mMovieTimer;
    private IMovieTimer.MovieListener mMovieListener;

    private IMusicPlayer mMusicPlayer;

    private OnPreparedListener mOnPreparedListener;
    private boolean mLoop;

    public PhotoMoviePlayer(Context context) {
        mMusicPlayer = new MusicPlayer();
        AppResources.getInstance().init(context.getResources());
    }

    public void setMusicPlayer(IMusicPlayer musicPlayer) {
        mMusicPlayer = musicPlayer;
    }

    public void setDataSource(PhotoMovie photoMovie) {
        if(mPhotoMovie!=null && mMovieRenderer!=null){
            mMovieRenderer.release(mPhotoMovie.getMovieSegments());
        }
        setStateValue(STATE_IDLE);
        mPhotoMovie = photoMovie;
        mMovieTimer = new MovieTimer(mPhotoMovie);
        mMovieTimer.setMovieListener(this);
        if (mMovieRenderer != null && mPhotoMovie != null) {
            mPhotoMovie.setMovieRenderer(mMovieRenderer);
            mMovieRenderer.setPhotoMovie(mPhotoMovie);
        }
        setLoop(mLoop);
    }

    public void setMovieRenderer(MovieRenderer movieRenderer) {
        mMovieRenderer = movieRenderer;
        if (mMovieRenderer != null && mPhotoMovie != null) {
            mPhotoMovie.setMovieRenderer(mMovieRenderer);
            mMovieRenderer.setPhotoMovie(mPhotoMovie);
        }
    }

    public void setMusic(String path) {
        mMusicPlayer.setDataSource(path);
    }

    public void setMusic(Context context, Uri uri) {
        mMusicPlayer.setDataSource(context, uri);
    }

    public void setMusic(FileDescriptor fileDescriptor) {
        mMusicPlayer.setDataSource(fileDescriptor);
    }

    public void setMusic(AssetFileDescriptor fileDescriptor) {
        mMusicPlayer.setDataSource(fileDescriptor);
    }

    public IMusicPlayer getMusicPlayer() {
        return mMusicPlayer;
    }

    public void prepare() {
        if (mPhotoMovie == null || mPhotoMovie.getPhotoSource() == null) {
            throw new NullPointerException("PhotoSource is null!");
        }
        prepare(mPhotoMovie.getPhotoSource().size());
    }

    /**
     * @param discPrepareNum 只预先下载好PhotoSource里一部分的资源，其它的边播边下
     */
    public void prepare(int discPrepareNum) {
        if (mPhotoMovie == null || mPhotoMovie.getPhotoSource() == null) {
            throw new NullPointerException("PhotoSource is null!");
        }
        setStateValue(STATE_PREPARING);
        mPhotoMovie.getPhotoSource().setOnSourcePreparedListener(new PhotoSource.OnSourcePrepareListener() {

            @Override
            public void onPreparing(PhotoSource photoSource, float progress) {
                if (mOnPreparedListener != null) {
                    mOnPreparedListener.onPreparing(PhotoMoviePlayer.this, progress * (1 - FIRST_SEGMENT_PREPARE_RATE));
                }
            }

            @Override
            public void onPrepared(PhotoSource photoSource, int downloaded, List<PhotoData> prepareFailList) {
                if (prepareFailList == null || prepareFailList.size() == 0) {
                    //没有任务加载失败
                    prepareFirstSegment(downloaded, photoSource.size());
                } else if (photoSource.size() > 0) {
                    //部分PhotoData加载失败，重新分配
                    mPhotoMovie.reAllocPhoto();
                    prepareFirstSegment(downloaded, photoSource.size() + prepareFailList.size());
                } else {
                    //全部任务加载失败
                    if (mOnPreparedListener != null) {
                        mOnPreparedListener.onError(PhotoMoviePlayer.this);
                    }
                    setStateValue(STATE_ERROR);
                    MLog.e(TAG, "数据加载失败");
                }
            }

            @Override
            public void onError(PhotoSource photoSource, PhotoData photoData, ErrorReason errorReason) {
            }
        });
        mPhotoMovie.getPhotoSource().prepare(discPrepareNum);
    }

    public void setStateValue(int state) {
        mCurrentState = state;
        if (mMovieRenderer != null) {
            switch (mCurrentState) {
                case STATE_ERROR:
                case STATE_IDLE:
                    mMovieRenderer.enableDraw(false);
                    break;
                case STATE_PREPARED:
                    mMovieRenderer.enableDraw(true);
                    break;
                case STATE_PREPARING:
                    mMovieRenderer.enableDraw(false);
                    break;
            }
        }
    }

    private void prepareFirstSegment(final int prepared, final int total) {
        List<MovieSegment> segmentList = mPhotoMovie.getMovieSegments();
        if (segmentList == null || segmentList.size() < 1) {
            setStateValue(STATE_PREPARED);
            if (mOnPreparedListener != null) {
                onPrepared(prepared, total);
            }
            return;
        }
        final MovieSegment firstSegment = segmentList.get(0);
        firstSegment.setOnSegmentPrepareListener(new MovieSegment.OnSegmentPrepareListener() {
            @Override
            public void onSegmentPrepared(boolean success) {
                firstSegment.setOnSegmentPrepareListener(null);
                setStateValue(STATE_PREPARED);
                if (mOnPreparedListener != null) {
                    mOnPreparedListener.onPreparing(PhotoMoviePlayer.this, 1f);
                    onPrepared(prepared, total);
                }
            }
        });
        firstSegment.prepare();
    }

    public void seekTo(int movieTime) {
        onMovieUpdate(movieTime);
    }

    public int getCurrentPlayTime() {
        return mMovieTimer.getCurrentPlayTime();
    }

    public void pause() {
        if (mMovieTimer != null) {
            mMovieTimer.pause();
        }
    }

    public void start() {
        if (!isPrepared()) {
            MLog.e(TAG, "start error!not prepared!");
            return;
        }
        //重新开始的话，重新计算电影持续时间
        if (mCurrentState != STATE_PAUSED) {
            mPhotoMovie.calcuDuration();
        }
        mMovieTimer.start();
    }

    public void stop() {
        if (mCurrentState < STATE_PREPARED) {
            return;
        }
        pause();
        seekTo(0);
    }

    public boolean isPlaying() {
        return mCurrentState == STATE_PLAYING;
    }

    private boolean isInPlaybackState() {
        return (mPhotoMovie != null &&
                mCurrentState != STATE_ERROR &&
                mCurrentState != STATE_IDLE &&
                mCurrentState != STATE_PREPARING);
    }

    public void setMovieListener(MovieTimer.MovieListener movieListener) {
        mMovieListener = movieListener;
    }

    @Override
    public void onMovieUpdate(int elapsedTime) {
        if (mMovieListener != null) {
            mMovieListener.onMovieUpdate(elapsedTime);
        }
        if(mPhotoMovie!=null) {
            mPhotoMovie.updateProgress(elapsedTime);
        }
    }

    @Override
    public void onMovieStarted() {
        MLog.i(TAG, "onMovieStarted");
        if (mMovieListener != null) {
            mMovieListener.onMovieStarted();
        }
        mMusicPlayer.start();
        setStateValue(STATE_PLAYING);
    }

    @Override
    public void onMoviedPaused() {
        MLog.i(TAG, "onMoviedPaused");
        if (mMovieListener != null) {
            mMovieListener.onMoviedPaused();
        }
        mMusicPlayer.pause();
        setStateValue(STATE_PAUSED);
    }

    @Override
    public void onMovieResumed() {
        MLog.i(TAG, "onMovieResumed");
        if (mMovieListener != null) {
            mMovieListener.onMovieResumed();
        }
        mMusicPlayer.start();
        setStateValue(STATE_PLAYING);
    }

    @Override
    public void onMovieEnd() {
        MLog.i(TAG, "onMovieEnd");
        if (mMovieListener != null) {
            mMovieListener.onMovieEnd();
        }
        mMusicPlayer.stop();
//        mMusicPlayer.fadeStop(new Handler());
        setStateValue(STATE_PLAYBACK_COMPLETED);
        if (mLoop) {
            releaseAndRestart();
        } else {
            mMovieRenderer.release();
        }
    }

    private void releaseAndRestart() {
        if (mMovieRenderer instanceof GLSurfaceMovieRenderer && !((GLSurfaceMovieRenderer) mMovieRenderer).isSurfaceCreated()) {
            restartImpl();
            return;
        }
        final Handler handler = new Handler();
        //在只有一个片段的情况下，有可能先准备好下一轮的资源，然后立刻被这一轮的release释放掉,因此等释放完再启动下一轮播放
        mMovieRenderer.setOnReleaseListener(new MovieRenderer.OnReleaseListener() {
            @Override
            public void onRelease() {
                mMovieRenderer.setOnReleaseListener(null);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        restartImpl();
                    }
                });
            }
        });
        mMovieRenderer.release();
    }

    private void restartImpl() {
        List<MovieSegment> segmentList = mPhotoMovie.getMovieSegments();
        if (segmentList == null || segmentList.size() == 0) {
            return;
        }
        setStateValue(STATE_PREPARING);
        final MovieSegment firstSegment = segmentList.get(0);
        firstSegment.setOnSegmentPrepareListener(new MovieSegment.OnSegmentPrepareListener() {
            @Override
            public void onSegmentPrepared(boolean success) {
                firstSegment.setOnSegmentPrepareListener(null);
                setStateValue(STATE_PREPARED);
                start();
            }
        });
        firstSegment.prepare();
    }

    public void destroy() {
        pause();
        setMovieListener(null);
        setOnPreparedListener(null);
        mMovieTimer.setMovieListener(null);
        mMovieTimer = null;
    }

    public void setOnPreparedListener(OnPreparedListener onPreparedListener) {
        this.mOnPreparedListener = onPreparedListener;
    }

    private void onPrepared(final int prepared, final int total) {
        if (mMovieRenderer instanceof GLMovieRenderer) {
            ((GLMovieRenderer) mMovieRenderer).checkGLPrepared(new GLMovieRenderer.OnGLPrepareListener() {
                @Override
                public void onGLPrepared() {
                    mOnPreparedListener.onPrepared(PhotoMoviePlayer.this, prepared, total);
                }
            });
        } else {
            mOnPreparedListener.onPrepared(PhotoMoviePlayer.this, prepared, total);
        }

    }

    public int getState() {
        return mCurrentState;
    }

    public void setLoop(boolean loop) {
        mLoop = loop;
    }

    public boolean isPrepared() {
        return mCurrentState == STATE_PREPARED || mCurrentState == STATE_PAUSED || mCurrentState == STATE_PLAYBACK_COMPLETED;
    }

    public interface OnPreparedListener {
        void onPreparing(PhotoMoviePlayer moviePlayer, float progress);

        void onPrepared(PhotoMoviePlayer moviePlayer, int prepared, int total);

        void onError(PhotoMoviePlayer moviePlayer);
    }
}
