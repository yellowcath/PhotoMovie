package com.hw.photomovie.segment;

import java.util.List;

/**
 * 转场片段，本身不需求照片，只持有上一个以及下一个片段，实现转场动画
 */
public abstract class TransitionSegment<PRE extends MovieSegment, NEXT extends MovieSegment> extends MulitBitmapSegment {

    protected PRE mPreSegment;
    protected NEXT mNextSegment;

    @Override
    public void onPrepare() {
        List<MovieSegment> movieSegments = mPhotoMovie.getMovieSegments();
        int index = movieSegments.indexOf(this);
        if (index <= 0 || index == movieSegments.size() - 1) {
            throw new TransitionSegmentException("TransitionSegment must be in the middle of two other Segments");
        }
        mPreSegment = (PRE) movieSegments.get(index - 1);
        mNextSegment = (NEXT) movieSegments.get(index + 1);
        if (mPreSegment instanceof TransitionSegment || mNextSegment instanceof TransitionSegment) {
            throw new TransitionSegmentException("TransitionSegment must be in the middle of two other Segments");
        }
        mNextSegment.setOnSegmentPrepareListener(new OnSegmentPrepareListener() {
            @Override
            public void onSegmentPrepared(boolean success) {
                onDataPrepared();
                mNextSegment.setOnSegmentPrepareListener(null);
            }
        });
        mNextSegment.prepare();
        mPreSegment.enableRelease(false);
    }

    @Override
    public int getRequiredPhotoNum() {
        return 0;
    }

    private static class TransitionSegmentException extends RuntimeException {
        public TransitionSegmentException(String message) {
            super(message);
        }
    }

    @Override
    public void onRelease() {
        super.onRelease();
        if(mPreSegment!=null) {
            mPreSegment.enableRelease(true);
            mPreSegment.release();
        }
    }

    @Override
    protected boolean checkPrepared() {
        return false;
    }
}
