package com.hw.photomovie;

import android.graphics.Bitmap;
import android.graphics.Rect;
import com.hw.photomovie.model.PhotoData;
import com.hw.photomovie.model.PhotoSource;
import com.hw.photomovie.render.MovieRenderer;
import com.hw.photomovie.segment.MovieSegment;
import com.hw.photomovie.util.MLog;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by huangwei on 2015/5/25.
 */
public class PhotoMovie<T> {
    private static final String TAG = "PhotoMovie";
    private PhotoSource mPhotoSource;
    private List<MovieSegment<T>> mMovieSegments = new LinkedList<MovieSegment<T>>();
    private SegmentPicker<T> mSegmentPicker;
    private PhotoAllocator mPhotoAllocator;

    private int mMovieDuration;

    private MovieRenderer mMovieRenderer;

    public PhotoMovie(PhotoSource photoSource, List<MovieSegment<T>> movieSegments) {
        mPhotoSource = photoSource;
        mMovieSegments.addAll(movieSegments);
        mPhotoAllocator = new PhotoAllocator();
        reAllocPhoto();
        //计算duration
        calcuDuration();

        mSegmentPicker = new SegmentPicker<T>(this);
    }

    /**
     * 重新将{@link PhotoData}分配给{@link MovieSegment}
     */
    protected void reAllocPhoto() {
        mPhotoAllocator.allocatePhoto();
    }

    public void setMovieRenderer(MovieRenderer movieRenderer) {
        mMovieRenderer = movieRenderer;
    }

    public List<MovieSegment<T>> getMovieSegments() {
        return mMovieSegments;
    }

    public int getDuration() {
        return mMovieDuration;
    }

    public int calcuDuration() {
        int duration = 0;
        for (MovieSegment<T> segment : mMovieSegments) {
            segment.setPhotoMovie(this);
            duration += segment.getDuration();
        }
        mMovieDuration = duration;
        return mMovieDuration;
    }

    public PhotoSource getPhotoSource() {
        return mPhotoSource;
    }

    public void updateProgress(int elapsedTime) {
        if (mMovieRenderer != null) {
            mMovieRenderer.drawFrame(elapsedTime);
        }
    }

    public SegmentPicker getSegmentPicker() {
        return mSegmentPicker;
    }

    protected MovieRenderer getMovieRender() {
        return mMovieRenderer;
    }

    public static class SegmentPicker<T> {

        private MovieSegment<T> mNextSegment;
        //        private MovieSegment mPreSegment;
        private MovieSegment<T> mCurSegment;

        private List<MovieSegment<T>> mMovieSegments;
        private PhotoMovie<T> mPhotoMovie;

        public SegmentPicker(PhotoMovie<T> photoMovie) {
            mMovieSegments = photoMovie.getMovieSegments();
            mPhotoMovie = photoMovie;
        }

        /**
         * 选取当前Segment，并且调用下一个的{@link MovieSegment#onPrepare()}函数
         *
         * @param elapsedTime
         * @return
         */
        public MovieSegment pickCurrentSegment(int elapsedTime) {
            if (mMovieSegments == null || mMovieSegments.size() == 0) {
                return null;
            }
            if (elapsedTime == 0) {
                mCurSegment = null;
                mNextSegment = null;
            }
            MovieSegment<T> segment = getCurrentSegment(elapsedTime);

            if (segment != mCurSegment) {
                if (mCurSegment != null) {
                    mCurSegment.onSegmentEnd();
                    mCurSegment.release();
                }
                mCurSegment = segment;
                MLog.i(TAG, "pick segment " + ":" + segment.toString());
            }
            //通知下一个片段准备
            /**第一个片段会prepare两次，第一次是在{@link PhotoMoviePlayer#prepareFirstSegment(int, int)}
             * ,第二次是在这里，但是这第二次是有必要的，因为第一次准备时可能surface还未创建，此时一些依赖surface状态的
             * 初始化可能之后就无效了*/
            MovieSegment<T> nextSegment = getNextSegment(elapsedTime);
            if (nextSegment != mNextSegment) {
                MLog.i(TAG, "onPrepare next segment " + ":" + nextSegment.toString());
                nextSegment.prepare();
                mNextSegment = nextSegment;
            }
            return segment;
        }

        public MovieSegment<T> getCurrentSegment(int elapsedTime) {
            int duration = mPhotoMovie.getDuration();
            if (duration <= 0) {
                throw new RuntimeException("Segment duration must >0!");
            }
            int size = mMovieSegments.size();
            if (elapsedTime >= duration) {
                return mMovieSegments.get(size - 1);
            }

            int totalDuration = 0;
            for (MovieSegment<T> segment : mMovieSegments) {
                int segmentDuration = segment.getDuration();
                if (elapsedTime >= totalDuration && elapsedTime < totalDuration + segmentDuration) {
                    return segment;
                }
                totalDuration += segmentDuration;
            }
            MLog.e(TAG, "getCurrentSegment 出错,elapsedTime:" + elapsedTime + " 返回第一个片段");
            return mMovieSegments.get(0);
        }

        public MovieSegment<T> getNextSegment(int elapsedTime) {
            int duration = mPhotoMovie.getDuration();
            int size = mMovieSegments.size();
            if (elapsedTime >= duration) {
                return mMovieSegments.get(0);
            }
            int totalDuration = 0;
            for (int i = 0; i < size; i++) {
                MovieSegment<T> segment = mMovieSegments.get(i);
                int segmentDuration = segment.getDuration();
                if (elapsedTime >= totalDuration && elapsedTime < totalDuration + segmentDuration) {
                    if (i < size - 1) {
                        return mMovieSegments.get(i + 1);
                    } else {
                        return mMovieSegments.get(0);
                    }
                }
                totalDuration += segmentDuration;
            }
            MLog.e(TAG, "getNextSegment 出错,elapsedTime:" + elapsedTime + " 返回第一个片段");
            return mMovieSegments.get(0);
        }

        public MovieSegment<T> getLastSegment() {
            return mMovieSegments.get(mMovieSegments.size() - 1);
        }

        public float getSegmentProgress(MovieSegment<T> movieSegment, int elapsedTime) {
            float rtn = 0;
            int duration = 0;
            for (MovieSegment<T> segment : mMovieSegments) {
                if (segment == movieSegment) {
                    rtn = (elapsedTime - duration) / (float) segment.getDuration();
                    rtn = rtn > 1 ? 1 : rtn;
                    break;
                } else {
                    duration += segment.getDuration();
                }
            }
            rtn = rtn >= 0 && rtn <= 1 ? rtn : 0;
            return rtn;
        }

        public MovieSegment<T> getPreSegment(MovieSegment<T> movieSegment) {
            int i = mMovieSegments.indexOf(movieSegment);
            MovieSegment<T> preSegment = null;
            if (i > 0) {
                preSegment = mMovieSegments.get(i - 1);
            } else if (i == 0) {
                preSegment = mMovieSegments.get(mMovieSegments.size() - 1);
            }
            return preSegment != null && preSegment != movieSegment ? preSegment : null;
        }
    }

    protected class PhotoAllocator {
        public void allocatePhoto() {
            if (mPhotoSource == null || mPhotoSource.size() == 0 || mMovieSegments.size() == 0) {
                return;
            }
            int index = 0;
            for (MovieSegment<T> segment : mMovieSegments) {
                int required = segment.getRequiredPhotoNum();
                List<PhotoData> photoDatas = new LinkedList<PhotoData>();
                while (required > 0) {
                    if (index >= mPhotoSource.size()) {
                        index = 0;
                    }
                    photoDatas.add(mPhotoSource.get(index));
                    --required;
                    ++index;
                }
                segment.allocPhotos(photoDatas);
            }
        }
    }
}
