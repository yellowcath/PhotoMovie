package com.hw.photomovie.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.opengl.GLES10;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.utils.L;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

import javax.microedition.khronos.opengles.GL10;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by huangwei on 14-12-31.
 */
public class PhotoUtil {
    private static final String TAG = PhotoUtil.class.getSimpleName();
    /**
     * decode图片OOM后降质重试的次数
     */
    public static int RETRY_COUNT = 5;

    public static boolean hasDiscCache(String uri) {
        if (TextUtils.isEmpty(uri)) {
            return false;
        }
        File file = ImageLoader.getInstance().getDiskCache().get(uri);
        if (file != null && file.exists()) {
            return true;
        }
        return false;
    }

    public static boolean hasMemoryCache(String uri, int w, int h) {
        if (TextUtils.isEmpty(uri)) {
            return false;
        }
        ImageSize imageSize = new ImageSize(w, h);
        String memoryKey = MemoryCacheUtils.generateKey(uri, imageSize);
        Bitmap bitmap = ImageLoader.getInstance().getMemoryCache().get(memoryKey);
        if (bitmap != null) {
            return true;
        }
        return false;
    }

    public static boolean hasCache(String uri, int w, int h) {
        if (hasMemoryCache(uri, w, h) || hasDiscCache(uri)) {
            return true;
        }
        return false;
    }

    /**
     * 以Center_Crop的方式裁剪图片
     *
     * @param path
     * @param toWidth             裁剪后的宽度
     * @param toHeight            裁剪后的高度
     * @param considerOrientation 是否考虑exif信息里的旋转角度
     * @return
     */
    public static Bitmap getCenterCropBitmap(String path, int toWidth, int toHeight, boolean considerOrientation) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        Rect rect = new Rect();
        getCroppedRect(rect, options.outWidth, options.outHeight, toWidth, toHeight);

        int inSampleSize = (rect.right - rect.left) / toWidth;
        options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;
        Bitmap decodedBitmap = null;
        for (int i = 0; i < RETRY_COUNT; i++) {
            try {
                decodedBitmap = BitmapFactory.decodeFile(path, options);
                break;
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                options = new BitmapFactory.Options();
                inSampleSize *= 2;
                options.inSampleSize = inSampleSize;
                L.i(TAG, "OutOfMemory,set inSampleSize To " + options.inSampleSize);
                continue;
            }
        }
        if (decodedBitmap == null) {
            return null;
        }
        getCroppedRect(rect, decodedBitmap.getWidth(), decodedBitmap.getHeight(), toWidth, toHeight);
        decodedBitmap = safeClipBitmap(decodedBitmap, rect);
        if (considerOrientation) {
            int degree = getPhotoOrientation(path);
            try {
                decodedBitmap = safeRotateBitmap(decodedBitmap, degree);
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            }
        }
        return decodedBitmap;
    }

    public static int getPhotoOrientation(String path) {
        int rotation = 0;
        try {
            ExifInterface exif = new ExifInterface(path);
            int orientationFlag = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientationFlag) {
                case ExifInterface.ORIENTATION_NORMAL:
                    rotation = 0;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotation = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotation = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotation = 270;
                    break;
                default:
                    rotation = 0;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rotation;
    }


    /**
     * 根据toWidth和toHieght，返回适用于bitmap的srcRect,只裁剪不压缩
     * 裁剪方式为裁上下或两边
     *
     * @param srcRect
     * @param bitmapWidth
     * @param bitmapHeight
     * @param toWidth
     * @param toHeight
     * @return
     */
    public static Rect getCroppedRect(Rect srcRect, int bitmapWidth, int bitmapHeight, float toWidth, float toHeight) {
        if (srcRect == null) {
            srcRect = new Rect();
        }
        float rate = toWidth / toHeight;
        float bitmapRate = bitmapWidth / (float) bitmapHeight;

        if (Math.abs(rate - bitmapRate) < 0.01) {

            srcRect.left = 0;
            srcRect.top = 0;
            srcRect.right = bitmapWidth;
            srcRect.bottom = bitmapHeight;
        } else if (bitmapRate > rate) {
            //裁两边
            float cutRate = toHeight / (float) bitmapHeight;
            float toCutWidth = cutRate * bitmapWidth - toWidth;
            float toCutWidthReal = toCutWidth / cutRate;

            srcRect.left = (int) (toCutWidthReal / 2);
            srcRect.top = 0;
            srcRect.right = bitmapWidth - (int) (toCutWidthReal / 2);
            srcRect.bottom = bitmapHeight;
        } else {
            //裁上下
            float cutRate = toWidth / (float) bitmapWidth;
            float toCutHeight = cutRate * bitmapHeight - toHeight;
            float toCutHeightReal = toCutHeight / cutRate;

            srcRect.left = 0;
            srcRect.top = (int) (toCutHeightReal / 2);
            srcRect.right = bitmapWidth;
            srcRect.bottom = bitmapHeight - (int) (toCutHeightReal / 2);

        }
        return srcRect;
    }

    /**
     * 获取用于将指定高宽的bitmap居中显示在指定高宽的空间内所需的Matrix
     *
     * @param matrix
     * @param bitmapWidth
     * @param bitmapHeight
     * @param showWidth
     * @param showHeight
     */
    public static void getCenterCropMatrix(Matrix matrix, int bitmapWidth, int bitmapHeight, int showWidth, int showHeight) {
        if (matrix == null) {
            return;
        }
        float scale;
        float dx = 0, dy = 0;

        if (bitmapWidth * showHeight > showWidth * bitmapHeight) {
            scale = (float) showHeight / (float) bitmapHeight;
            dx = (showWidth - bitmapWidth * scale) * 0.5f;
        } else {
            scale = (float) showWidth / (float) bitmapWidth;
            dy = (showHeight - bitmapHeight * scale) * 0.5f;
        }

        matrix.setScale(scale, scale);
        matrix.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));
    }

    /**
     * 返回适用于读取Res资源的options，其实就是将option里的磁盘缓存关掉
     *
     * @param options
     * @return
     */
    public static DisplayImageOptions getResDisplayImageOptions(DisplayImageOptions options) {
        DisplayImageOptions resOptions;
        if (options == null) {
            resOptions = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(false).build();
        } else {
            resOptions = new DisplayImageOptions.Builder()
                    .cloneFrom(options)
                    .displayer(new SimpleBitmapDisplayer())
                    .cacheOnDisk(false).build();
        }
        return resOptions;
    }

    /**
     * 检查jpeg图片的完整性(头部和尾部各两个字节)
     *
     * @param path
     * @return
     */
    public static boolean checkJpgIntegrity(String path) {
        long s = System.currentTimeMillis();
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        File file = new File(path);
        if (!file.exists()) {
            return false;
        }
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
            long len = randomAccessFile.length();
            if (len < 2) {
                return false;
            }
            byte[] bytes = new byte[2];
            randomAccessFile.read(bytes);
            if (bytes[0] != (byte) 0xFF || bytes[1] != (byte) 0xD8) {
                return false;
            }
            randomAccessFile.seek(len - 2);
            randomAccessFile.read(bytes);
            if (bytes[0] != (byte) 0xFF || bytes[1] != (byte) 0xD9) {
                return false;
            }
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            long e = System.currentTimeMillis();
        }
        return false;
    }

//-----------------------------------SafeBitmap部分------------------------------------------------------------------------

    /**
     * 裁剪图片，OOM时降质裁剪
     *
     * @param bitmap
     * @param rect
     * @return
     */
    public static Bitmap safeClipBitmap(Bitmap bitmap, Rect rect) {
        if (bitmap == null || bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0 || rect == null || rect.width() == 0 || rect.height() == 0) {
            return bitmap;
        }
        Bitmap clipedBitmap = null;
        int sampleSize = 1;
        Matrix m = new Matrix();
        for (int i = 0; i < RETRY_COUNT; i++) {
            try {
                m.setScale(1 / sampleSize, 1 / sampleSize);
                clipedBitmap = Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height(), m, true);
                break;
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                sampleSize *= 2;
                L.i(TAG, "OutOfMemory in safeClipBitmap,set sampleSize to " + sampleSize);
                continue;
            }
        }
        if (clipedBitmap != bitmap) {
            bitmap.recycle();
        }
        return clipedBitmap;
    }

    /**
     * 旋转图片，OOM时降质旋转
     *
     * @param bitmap
     * @param rotation
     * @return
     */
    public static Bitmap safeRotateBitmap(Bitmap bitmap, int rotation) {
        if (bitmap == null || bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0 || rotation == 0) {
            return bitmap;
        }
        int sampleSize = 1;
        Bitmap rotatedBitmap = null;
        Matrix m = new Matrix();
        for (int i = 0; i < RETRY_COUNT; i++) {
            try {
                m.setRotate(rotation);
                m.postScale(1f / sampleSize, 1f / sampleSize);
                rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap
                        .getHeight(), m, true);
                L.i(TAG, "rotate bitmap,degree:" + rotation);
                break;
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                sampleSize *= 2;
                L.i(TAG, "OutOfMemory in safeRotateBitmap,set sampleSize to " + sampleSize);
                continue;
            }
        }
        if (rotatedBitmap != bitmap) {
            bitmap.recycle();
        }
        return rotatedBitmap;

    }

//-----------------------------------杂项部分------------------------------------------------------------------------


    public static Bitmap makeRoundCorner(Bitmap bitmap, int roundCornerRadius) {
        if (bitmap == null) {
            return bitmap;
        }
        Bitmap rtnBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(rtnBitmap);
        RectF rectF = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        paint.setXfermode(null);
        canvas.drawRoundRect(rectF, roundCornerRadius, roundCornerRadius, paint);
//        canvas.drawCircle(bitmap.getWidth()/2,bitmap.getHeight()/2,100,paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return rtnBitmap;
    }

    public static long getMaxFreeMemorySize() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.maxMemory() - (runtime.totalMemory() - runtime.freeMemory());
    }

    public static int calcuSampleSizeByByteSize(int width, int height, Bitmap.Config config, long byteSize) {
        if (byteSize <= 0) {
            return 1;
        }
        int perPixelSize;
        switch (config) {
            case ALPHA_8:
                perPixelSize = 1;
                break;
            case ARGB_4444:
                perPixelSize = 2;
                break;
            case RGB_565:
                perPixelSize = 2;
                break;
            case ARGB_8888:
            default:
                perPixelSize = 4;
                break;

        }
        long fromByteSize = width * height * perPixelSize;

        int rtn = (int) (fromByteSize / (float) byteSize);
        if (rtn % 2 != 0) {
            ++rtn;
        }
        rtn = rtn == 0 ? 1 : rtn;
        return rtn;
    }

    /**
     * 如果key对应的磁盘缓存是坏的，就删除，只支持jpg格式文件。检查函数为{@link #checkJpgIntegrity(String)}
     *
     * @param diskKey
     * @return true:确实删掉了坏的磁盘缓存
     */
    public static boolean deleteBadDiskCache(String diskKey) {
        if (TextUtils.isEmpty(diskKey)) {
            return false;
        }
        File file = ImageLoader.getInstance().getDiskCache().get(diskKey);
        if (file != null) {
            boolean integrity = checkJpgIntegrity(file.getAbsolutePath());
            if (!integrity) {
                file.delete();
                return true;
            }
        }
        return false;
    }

    private static final int DEFAULT_MAX_BITMAP_DIMENSION = 2048;

    /**
     * @return 返回机器最大支持的bitmap尺寸，如获取失败默认返回2048.
     */
    public static int getMaxSupportedBitmapSize() {
        int[] maxTextureSize = new int[1];
        GLES10.glGetIntegerv(GL10.GL_MAX_TEXTURE_SIZE, maxTextureSize, 0);
        return Math.max(maxTextureSize[0], DEFAULT_MAX_BITMAP_DIMENSION);
    }

    public static Bitmap createBitmapOrNull(int width, int height, Bitmap.Config config) {
        try {
            return Bitmap.createBitmap(width, height, config);
        } catch (OutOfMemoryError e) {
            return null;
        }
    }

    /**
     * 生成View的截图
     *
     * @param view
     * @return
     */
    public static Bitmap getViewBitmap(View view) {
        if (view == null) {
            return null;
        }

        int w = view.getWidth();
        int h = view.getHeight();
        Bitmap bitmap = null;
        try {
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
        }
        if (bitmap == null) {
            return null;
        }

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    /**
     * 根据str生成一张bitmap，不换行
     *
     * @param str
     * @param textPaint
     * @return 可能为null
     */
    public static Bitmap genBitmapFromStr(String str, TextPaint textPaint) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }

        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        int h = (int) (Math.abs(fontMetrics.ascent) + Math.abs(fontMetrics.descent));
        int w = (int) textPaint.measureText(str);
        Bitmap bitmap = createBitmapOrNull(w, h, Bitmap.Config.ARGB_4444);
        if (bitmap == null) {
            return null;
        }
        Canvas canvas = new Canvas(bitmap);
        canvas.drawText(str, 0, Math.abs(fontMetrics.ascent), textPaint);
        return bitmap;
    }
}
