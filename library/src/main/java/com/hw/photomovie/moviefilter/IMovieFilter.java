package com.hw.photomovie.moviefilter;

import com.hw.photomovie.PhotoMovie;
import com.hw.photomovie.opengl.FboTexture;

/**
 * Created by huangwei on 2018/9/5 0005.
 */
public interface IMovieFilter {
    void doFilter(PhotoMovie photoMovie,int elapsedTime, FboTexture inputTexture, FboTexture outputTexture);
    void release();
}
