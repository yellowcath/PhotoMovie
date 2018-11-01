package com.hw.photomovie.sample.activityAnim;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import com.hw.photomovie.render.MovieRenderer;
import com.hw.photomovie.segment.MovieSegment;

import java.util.List;

/**
 * Created by huangwei on 2015/7/1.
 */
public class ActivityMovieRenderer extends MovieRenderer<Activity> {
    @Override
    public void drawFrame(int elapsedTime) {
        drawMovieFrame(elapsedTime);
    }

    @Override
    public MovieRenderer<Activity> setPainter(Activity painter) {
        DisplayMetrics displayMetrics = painter.getResources().getDisplayMetrics();
        setMovieViewport(0,0,displayMetrics.widthPixels,displayMetrics.heightPixels);
        return super.setPainter(painter);
    }

    @Override
    public void release() {

    }

    @Override
    public void release(List<MovieSegment<Activity>> movieSegments) {

    }
}
