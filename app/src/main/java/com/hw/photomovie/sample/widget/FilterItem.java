package com.hw.photomovie.sample.widget;

import com.hw.photomovie.filter.GrayFilter;
import com.hw.photomovie.moviefilter.CameoMovieFilter;
import com.hw.photomovie.moviefilter.GrayMovieFilter;
import com.hw.photomovie.moviefilter.IMovieFilter;
import com.hw.photomovie.moviefilter.KuwaharaMovieFilter;
import com.hw.photomovie.moviefilter.LutMovieFilter;
import com.hw.photomovie.moviefilter.SnowMovieFilter;

/**
 * Created by huangwei on 2018/9/9.
 */
public class FilterItem {

    public FilterItem(int imgRes, String name, FilterType type) {
        this.imgRes = imgRes;
        this.name = name;
        this.type = type;
    }

    public int imgRes;
    public String name;
    public FilterType type;

    public IMovieFilter initFilter() {
        switch (type) {
            case GRAY:
                return new GrayMovieFilter();
            case SNOW:
                return new SnowMovieFilter();
            case CAMEO:
                return new CameoMovieFilter();
            case KUWAHARA:
                return new KuwaharaMovieFilter();
            case LUT1:
                return new LutMovieFilter(LutMovieFilter.LutType.A);
            case LUT2:
                return new LutMovieFilter(LutMovieFilter.LutType.B);
            case LUT3:
                return new LutMovieFilter(LutMovieFilter.LutType.C);
            case LUT4:
                return new LutMovieFilter(LutMovieFilter.LutType.D);
            case LUT5:
                return new LutMovieFilter(LutMovieFilter.LutType.E);
            case NONE:
            default:
                return null;
        }
    }
}
