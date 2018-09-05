package com.hw.photomovie.util;

/**
 * Created by huangwei on 2015/6/4.
 */
public enum  ScaleType {
    /**
     * 左右上下裁剪填满输出窗口
     */
    CENTER_CROP,
    /**
     * 不管比例拉伸填满输出窗口
     */
    FIT_XY,
    /**
     * 居中，同时填满输出窗口的宽或者高
     */
    FIT_CENTER
}
