/**
 * StackBlur v1.0 for Android
 *
 * @Author: Enrique López Mañas <eenriquelopez@gmail.com>
 * http://www.lopez-manas.com
 * <p/>
 * Author of the original algorithm: Mario Klingemann <mario.quasimondo.com>
 * <p/>
 * This is a compromise between Gaussian Blur and Box blur
 * It creates much better looking blurs than Box Blur, but is
 * 7x faster than my Gaussian Blur implementation.
 * <p/>
 * I called it Stack Blur because this describes best how this
 * filter works internally: it creates a kind of moving stack
 * of colors whilst scanning through the image. Thereby it
 * just has to add one new block of color to the right side
 * of the stack and remove the leftmost color. The remaining
 * colors on the topmost layer of the stack are either added on
 * or reduced by one, depending on if they are on the right or
 * on the left side of the stack.
 * @copyright: Enrique López Mañas
 * @license: Apache License 2.0
 */


package com.hw.photomovie.util.stackblur;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import java.io.FileOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StackBlurManager {
    static final int EXECUTOR_THREADS = Runtime.getRuntime().availableProcessors();
    static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(EXECUTOR_THREADS);

    /**
     * Original image
     */
    private final Bitmap _image;

    /**
     * Most recent result of blurring
     */
    private Bitmap _result;

    /**
     * Method of blurring
     */
    private final BlurProcess _blurProcess;

    /**
     * Constructor method (basic initialization and construction of the pixel array)
     *
     * @param image The image that will be analyed
     */
    public StackBlurManager(Bitmap image) {
        _image = image;
        _blurProcess = new JavaBlurProcess();
    }

    public StackBlurManager(Bitmap image,float preScale) {
        Bitmap compressedBitmap = null;
        try {
            Matrix matrix = new Matrix();
            matrix.setScale(preScale,preScale);
            compressedBitmap = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
            compressedBitmap = image;
        }
        _image = compressedBitmap;
        _blurProcess = new JavaBlurProcess();
    }

    /**
     * Process the image on the given radius. Radius must be at least 1
     *
     * @param radius
     */
    public Bitmap process(int radius) {
        _result = _blurProcess.blur(_image, radius);
        return _result;
    }

    /**
     * Returns the blurred image as a bitmap
     *
     * @return blurred image
     */
    public Bitmap returnBlurredImage() {
        return _result;
    }

    /**
     * Save the image into the file system
     *
     * @param path The path where to save the image
     */
    public void saveIntoFile(String path) {
        try {
            FileOutputStream out = new FileOutputStream(path);
            _result.compress(Bitmap.CompressFormat.PNG, 90, out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the original image as a bitmap
     *
     * @return the original bitmap image
     */
    public Bitmap getImage() {
        return this._image;
    }

    /**
     * Process the image using a native library
     */
}
