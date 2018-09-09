package com.hw.photomovie.sample.widget;

import com.hw.photomovie.PhotoMovieFactory;
import com.hw.photomovie.moviefilter.CameoMovieFilter;
import com.hw.photomovie.moviefilter.GrayMovieFilter;
import com.hw.photomovie.moviefilter.IMovieFilter;
import com.hw.photomovie.moviefilter.KuwaharaMovieFilter;
import com.hw.photomovie.moviefilter.LutMovieFilter;
import com.hw.photomovie.moviefilter.SnowMovieFilter;

/**
 * Created by huangwei on 2018/9/9.
 */
public class TransferItem {

    public TransferItem(int imgRes, String name, PhotoMovieFactory.PhotoMovieType type) {
        this.imgRes = imgRes;
        this.name = name;
        this.type = type;
    }

    public int imgRes;
    public String name;
    public PhotoMovieFactory.PhotoMovieType type;
}
