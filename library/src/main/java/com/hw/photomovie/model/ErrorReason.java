package com.hw.photomovie.model;


/**
 * Created by yellowcat on 2015/6/2.
 */
public class ErrorReason {

    private Throwable mThrowable;
    private String mExtra;

    public ErrorReason(Throwable cause,String extra) {
        mThrowable = cause;
        mExtra = extra;
    }

    public Throwable getThrowable() {
        return mThrowable;
    }

    public String getExtra() {
        return mExtra;
    }
}
