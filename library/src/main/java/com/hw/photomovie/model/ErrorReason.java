package com.hw.photomovie.model;

import com.nostra13.universalimageloader.core.assist.FailReason;

/**
 * Created by yellowcat on 2015/6/2.
 */
public class ErrorReason extends FailReason {
    public ErrorReason(FailType type, Throwable cause) {
        super(type, cause);
    }
}
