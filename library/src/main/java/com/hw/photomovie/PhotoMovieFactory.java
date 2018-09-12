package com.hw.photomovie;

import com.hw.photomovie.model.PhotoSource;
import com.hw.photomovie.opengl.GLESCanvas;
import com.hw.photomovie.segment.EndGaussianBlurSegment;
import com.hw.photomovie.segment.FitCenterScaleSegment;
import com.hw.photomovie.segment.FitCenterSegment;
import com.hw.photomovie.segment.GradientTransferSegment;
import com.hw.photomovie.segment.LayerSegment;
import com.hw.photomovie.segment.MoveTransitionSegment;
import com.hw.photomovie.segment.MovieSegment;
import com.hw.photomovie.segment.ScaleSegment;
import com.hw.photomovie.segment.ScaleTransSegment;
import com.hw.photomovie.segment.SingleBitmapSegment;
import com.hw.photomovie.segment.TestMovieSegment;
import com.hw.photomovie.segment.ThawSegment;
import com.hw.photomovie.segment.WindowSegment;
import com.hw.photomovie.segment.layer.GaussianBlurLayer;
import com.hw.photomovie.segment.layer.MovieLayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangwei on 2015/5/29.
 */
public class PhotoMovieFactory {
    public enum PhotoMovieType {
        THAW,  //融雪
        SCALE, //缩放
        SCALE_TRANS, //缩放 OR 平移
        WINDOW, //窗扉
        HORIZONTAL_TRANS,//横向平移
        VERTICAL_TRANS,//纵向平移
        GRADIENT,//渐变
        TEST
    }

    public static final int END_GAUSSIANBLUR_DURATION = 1500;

    public static PhotoMovie generatePhotoMovie(PhotoSource photoSource, PhotoMovieType type) {
        switch (type) {
            case THAW:
                return generateThawPhotoMovie(photoSource);
            case SCALE:
                return generateScalePhotoMovie(photoSource);
            case SCALE_TRANS:
                return generateScaleTransPhotoMovie(photoSource);
            case WINDOW:
                return generateWindowPhotoMovie(photoSource);
            case HORIZONTAL_TRANS:
                return generateHorizontalTransPhotoMovie(photoSource);
            case VERTICAL_TRANS:
                return generateVerticalTransPhotoMovie(photoSource);
            case GRADIENT:
                return genGradientPhotoMovie(photoSource);
            case TEST:
                return generateTestPhotoMovie(photoSource);
            default:
                return null;
        }
    }

    private static PhotoMovie generateHorizontalTransPhotoMovie(PhotoSource photoSource) {
        List<MovieSegment> segmentList = new ArrayList<MovieSegment>();
        for (int i = 0; i < photoSource.size(); i++) {
            segmentList.add(new FitCenterSegment(1000).setBackgroundColor(0xFF323232));
            segmentList.add(new MoveTransitionSegment(MoveTransitionSegment.DIRECTION_HORIZON, 800));
        }
        segmentList.remove(segmentList.size() - 1);
        PhotoMovie photoMovie = new PhotoMovie(photoSource, segmentList);
        return photoMovie;
    }

    private static PhotoMovie generateVerticalTransPhotoMovie(PhotoSource photoSource) {
        List<MovieSegment> segmentList = new ArrayList<MovieSegment>();
        for (int i = 0; i < photoSource.size(); i++) {
            segmentList.add(new FitCenterSegment(1000).setBackgroundColor(0xFF323232));
            segmentList.add(new MoveTransitionSegment(MoveTransitionSegment.DIRECTION_VERTICAL, 800));
        }
        segmentList.remove(segmentList.size() - 1);
        PhotoMovie photoMovie = new PhotoMovie(photoSource, segmentList);
        return photoMovie;
    }

    private static PhotoMovie generateTestPhotoMovie(PhotoSource photoSource) {
        List<MovieSegment> segmentList = new ArrayList<MovieSegment>();
        segmentList.add(new TestMovieSegment(5555));
        PhotoMovie photoMovie = new PhotoMovie(photoSource, segmentList);
        return photoMovie;
    }

    private static PhotoMovie generateWindowPhotoMovie(PhotoSource photoSource) {
        List<MovieSegment<GLESCanvas>> segmentList = new ArrayList<MovieSegment<GLESCanvas>>(7);
        segmentList.add(new SingleBitmapSegment(500));
        segmentList.add(new WindowSegment(2.1f, 1f, 2.1f, -1f, -1.1f).removeFirstAnim());
        segmentList.add(new WindowSegment(-1f, 1f, 1f, -1f, 0f, 0f));
        segmentList.add(new WindowSegment(-1f, -2.1f, 1f, -2.1f, 1.1f).removeFirstAnim());
        segmentList.add(new WindowSegment(0f, 1f, 0f, -1f, 0f, 1f));
        segmentList.add(new WindowSegment(-1f, 0f, 1f, 0f, -1f, 0f));
        segmentList.add(new EndGaussianBlurSegment(END_GAUSSIANBLUR_DURATION));
        PhotoMovie<GLESCanvas> photoMovie = new PhotoMovie<GLESCanvas>(photoSource, segmentList);
        return photoMovie;
    }

    private static PhotoMovie generateScalePhotoMovie(PhotoSource photoSource) {
        List<MovieSegment> segmentList = new ArrayList<MovieSegment>(photoSource.size() + 1);
        for (int i = 0; i < photoSource.size(); i++) {
            segmentList.add(new ScaleSegment(1800, 10, 1));
        }
        segmentList.add(new EndGaussianBlurSegment(END_GAUSSIANBLUR_DURATION));
        PhotoMovie photoMovie = new PhotoMovie(photoSource, segmentList);
        return photoMovie;
    }

    private static PhotoMovie generateScaleTransPhotoMovie(PhotoSource photoSource) {
        List<MovieSegment> segmentList = new ArrayList<MovieSegment>(photoSource.size() + 1);
        for (int i = 0; i < photoSource.size() - 1; i++) {
            segmentList.add(new ScaleTransSegment());
        }
        segmentList.add(new LayerSegment(new MovieLayer[]{new GaussianBlurLayer()}, 2000));
        PhotoMovie photoMovie = new PhotoMovie(photoSource, segmentList);
        return photoMovie;
    }

    private static PhotoMovie generateThawPhotoMovie(PhotoSource photoSource) {
        List<MovieSegment> segmentList = new ArrayList<>();
        int thawType = 0;
        int duration = 1800;
        for (int i = 0; i < photoSource.size() - 1; i++) {
            segmentList.add(new ThawSegment(duration, thawType++));
            if (thawType == 3) {
                thawType = 0;
            }
        }
        segmentList.add(new ScaleSegment(1800, 1, 1.1f));
        segmentList.add(new EndGaussianBlurSegment(END_GAUSSIANBLUR_DURATION));
        PhotoMovie photoMovie = new PhotoMovie(photoSource, segmentList);
        return photoMovie;
    }

    private static PhotoMovie genGradientPhotoMovie(PhotoSource photoSource) {
        List<MovieSegment> segmentList = new ArrayList<>(photoSource.size());
        for (int i = 0; i < photoSource.size(); i++) {
            if (i == 0) {
                segmentList.add(new FitCenterScaleSegment(1600, 1f, 1.1f));
            } else {
                segmentList.add(new FitCenterScaleSegment(1600, 1.05f, 1.1f));
            }
            if (i < photoSource.size() - 1) {
                segmentList.add(new GradientTransferSegment(800, 1.1f, 1.15f, 1.0f, 1.05f));
            }
        }
        return new PhotoMovie(photoSource, segmentList);
    }
}
