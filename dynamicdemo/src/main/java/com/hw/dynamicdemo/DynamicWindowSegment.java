package com.hw.dynamicdemo;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import com.hw.photomovie.PhotoMovieFactory;
import com.hw.photomovie.filter.BetweenLinesFilter;
import com.hw.photomovie.opengl.GLESCanvas;
import com.hw.photomovie.opengl.GLPaint;
import com.hw.photomovie.segment.EndGaussianBlurSegment;
import com.hw.photomovie.segment.MovieSegment;
import com.hw.photomovie.segment.SingleBitmapSegment;
import com.hw.photomovie.segment.WindowSegment;
import com.hw.photomovie.util.AppResources;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangwei on 2015/6/19.
 */
public class DynamicWindowSegment extends SingleBitmapSegment {

    private static final int MODE_POINT_APPEAR = 0;
    private static final int MODE_MOVE_APPEAR = 1;

    private static final float LINE_WIDTH_DP = 5;
    /**
     * 白线出现的动画时间占比
     */
    private float LINE_ANIM_RATE = 0.3f;
    /**
     * 白线分离的动画时间占比
     */
    private static float LINE_SPLIT_RATE = 0.4f;
    /**
     * 白线分离到一半时的停顿时间占比
     */
    private static final float LINE_SPLIT_WAIT_RATE = 0.3f;
    /**
     * 白线起点
     */
    private PointF mStartPoint;
    /**
     * 白线终点
     */
    private PointF mEndPoint;
    /**
     *
     */
    private PointF mTempPoint;

    private PointF mCenterPoint;

    private GLPaint mPaint;
    /**
     * 两条直线分开始时离最初位置的最大距离
     */
    private float mMaxSplitBDis;
    /**
     * 直线出现点到起点或终点的距离较大的一个
     */
    private float mMaxDis;
    //点斜式方程
    private float mK, mB, mB1, mB2;

    private float mLineWidth;

    private BetweenLinesFilter filter = new BetweenLinesFilter();
    /**
     * 是否存在斜率
     */
    private boolean mKExisted;

    private int mAppearMode;

    private float mBMoveDis;

    private MovieSegment mPreMovieSegment;

    /**
     * 坐标x,y范围都是-1~1
     *
     * @param sx 起点坐标
     * @param sy 起点坐标
     * @param ex 终点坐标
     * @param ey 终点坐标
     * @param cx 直线出现点x坐标
     * @param cy 直线出现点y坐标
     *           动画：直线出现点开始，往起点和终点扩散，之后保持斜率，分成两条直线向两边分开，显示出中间的图片，就像拉开窗帘一样
     */
    public DynamicWindowSegment(float sx, float sy, float ex, float ey, float cx, float cy) {
        mAppearMode = MODE_POINT_APPEAR;
        mDuration = 3000;
        mCenterPoint = new PointF(cx, cy);
        mMaxDis = (float) Math.max(Math.sqrt(Math.pow(sx - cx, 2) + Math.pow(sy - cy, 2)), Math.sqrt(Math.pow(ex - cx, 2) + Math.pow(ey - cy, 2)));
        init(sx, sy, ex, ey);
    }

    public DynamicWindowSegment(float sx, float sy, float ex, float ey, float bMoveDis) {
        mAppearMode = MODE_MOVE_APPEAR;
        mDuration = 3000;
        mBMoveDis = bMoveDis;
        init(sx, sy, ex, ey);
    }

    private void init(float sx, float sy, float ex, float ey) {
        mStartPoint = new PointF(sx, sy);
        mEndPoint = new PointF(ex, ey);
        mTempPoint = new PointF();
        mKExisted = sx != ex;
        mPaint = new GLPaint();
        mPaint.setColor(Color.WHITE);
        mLineWidth = (int) (AppResources.getInstance().getAppDensity() * LINE_WIDTH_DP + 0.5f);
        mPaint.setLineWidth(mLineWidth);

        initLineEquation();
        initMaxSplitDis();

        filter.setOpaque(false);
    }

    private void initLineEquation() {
        if (mStartPoint.x == mEndPoint.x) {
            mK = 0;
            mB = mStartPoint.x;
        } else if (mStartPoint.y == mEndPoint.y) {
            mK = 0;
            mB = mStartPoint.y;
        } else {
            mK = (mEndPoint.y - mStartPoint.y) / (mEndPoint.x - mStartPoint.x);
            mB = (mEndPoint.y * mEndPoint.x - mStartPoint.y * mEndPoint.x) / (mStartPoint.x - mEndPoint.x) + mEndPoint.y;
        }
    }

    @Override
    public void onPrepare() {
        super.onPrepare();
        mPreMovieSegment = mPhotoMovie.getSegmentPicker().getPreSegment(this);
        if (mPreMovieSegment != null) {
            mPreMovieSegment.enableRelease(false);
        }
    }

    @Override
    public void onSegmentEnd() {
        super.onSegmentEnd();
        if (mPreMovieSegment != null) {
            mPreMovieSegment.enableRelease(true);
            mPreMovieSegment.release();
        }
    }

    @Override
    public void drawFrame(GLESCanvas canvas, float segmentRate) {
        drawPreForBackground(canvas);
        if (!mDataPrepared) {
            return;
        }
        if (mBitmapInfo != null) {
            mBitmapInfo.makeTextureAvailable(canvas);
        }
        if (segmentRate <= LINE_ANIM_RATE) {
            float rate = segmentRate / LINE_ANIM_RATE;
            drawLineAppear(canvas, rate);
        } else if (segmentRate <= LINE_ANIM_RATE + LINE_SPLIT_RATE + LINE_SPLIT_WAIT_RATE) {
            float rate;
            if (segmentRate < LINE_ANIM_RATE + LINE_SPLIT_RATE * 0.5f) {
                //分离动画前半段
                rate = (segmentRate - LINE_ANIM_RATE) / LINE_SPLIT_RATE;
            } else if (segmentRate <= LINE_ANIM_RATE + LINE_SPLIT_RATE * 0.5f + LINE_SPLIT_WAIT_RATE) {
                //分离动画暂停
                rate = 0.5f;
            } else {
                //分离动画后半段
                rate = (segmentRate - LINE_SPLIT_WAIT_RATE - LINE_ANIM_RATE) / LINE_SPLIT_RATE;
            }
            //直线平行移动的距离
            float mMinEdge = Math.min(mViewportRect.width(), mViewportRect.height());
            //最大移动距离还要加上线的宽度再多一点，不然移到最后线会有一部分留在屏幕上
            float dis = (mMaxSplitBDis + mLineWidth * 4 / mMinEdge) * rate;
            float lineB = mAppearMode == MODE_POINT_APPEAR ? mB : (mB + mBMoveDis * 1);
            mB1 = lineB + dis;
            mB2 = lineB - dis;
            drawBitmap(canvas);
            drawLineSplit(canvas, rate);
        } else {
            drawBitmap(canvas);
        }
    }

    private void drawPreForBackground(GLESCanvas canvas) {
        if (mPreMovieSegment != null) {
            mPreMovieSegment.drawFrame(canvas, 1);
        }
    }

    private void drawBitmap(GLESCanvas canvas) {
        if (mBitmapInfo != null && mBitmapInfo.makeTextureAvailable(canvas)) {
            canvas.unbindArrayBuffer();
            filter.setLines(mK, mB1, mK, mB2, mKExisted);
            filter.drawFrame(0, mBitmapInfo.bitmapTexture.getId(), mBitmapInfo.srcRect, new RectF(mBitmapInfo.srcRect), mViewportRect);
            canvas.rebindArrayBuffer();
        }
    }

    @Override
    public void setViewport(int l, int t, int r, int b) {
        super.setViewport(l, t, r, b);
        filter.init();
        filter.setViewport(l, t, r, b);
    }

    private void drawLineAppear(GLESCanvas canvas, float rate) {
        if (mAppearMode == MODE_MOVE_APPEAR) {
            drawLineMoveAppear(canvas, rate);
        } else if (mAppearMode == MODE_POINT_APPEAR) {
            drawLinePointAppear(canvas, rate);
        }
    }

    private void drawLineMoveAppear(GLESCanvas canvas, float rate) {
        float b = mB + mBMoveDis * rate;
        float x1, y1, x2, y2;

        if (mStartPoint.x == mEndPoint.x) {
            x1 = mStartPoint.x + mBMoveDis * rate;
            x2 = mEndPoint.x + mBMoveDis * rate;
            y1 = mStartPoint.y;
            y2 = mEndPoint.y;
        } else if (mStartPoint.y == mEndPoint.y) {
            x1 = mStartPoint.x;
            x2 = mEndPoint.x;
            y1 = mStartPoint.y + mBMoveDis * rate;
            y2 = mEndPoint.y + mBMoveDis * rate;
        } else {
            x1 = mStartPoint.x;
            y1 = mK * x1 + b;
            x2 = mEndPoint.x;
            y2 = mK * x2 + b;
        }
        canvas.drawLine(
                (1 + (x1 - 1) / 2f) * mViewportRect.width(),
                -(y1 - 1) / 2f * mViewportRect.height(),
                (1 + (x2 - 1) / 2f) * mViewportRect.width(),
                -(y2 - 1) / 2f * mViewportRect.height(),
                mPaint);
    }

    private void drawLinePointAppear(GLESCanvas canvas, float rate) {
        float x1, y1, x2, y2;
        float dis = mMaxDis * rate;
        if (mStartPoint.x == mEndPoint.x) {
            x1 = mCenterPoint.x;
            x2 = mCenterPoint.x;
            y1 = mCenterPoint.y + dis;
            y2 = mCenterPoint.y - dis;
        } else if (mStartPoint.y == mEndPoint.y) {
            x1 = mCenterPoint.x + dis;
            x2 = mCenterPoint.x - dis;
            y1 = mCenterPoint.y;
            y2 = mCenterPoint.y;
        } else {
            float xDis = (float) (dis * Math.cos(Math.atan(mK)));
            x1 = mCenterPoint.x + xDis;
            y1 = mK * x1 + mB;
            x2 = mCenterPoint.x - xDis;
            y2 = mK * x2 + mB;
        }
        canvas.drawLine(
                (1 + (x1 - 1) / 2f) * mViewportRect.width(),
                -(y1 - 1) / 2f * mViewportRect.height(),
                (1 + (x2 - 1) / 2f) * mViewportRect.width(),
                -(y2 - 1) / 2f * mViewportRect.height(),
                mPaint);
    }

    private void drawLineSplit(GLESCanvas canvas, float rate) {
        doDrawSplitedLine(canvas, mB1);
        doDrawSplitedLine(canvas, mB2);
    }

    private void doDrawSplitedLine(GLESCanvas canvas, float b) {
        float x1, x2, y1, y2;
        if (mStartPoint.x == mEndPoint.x) {
            x1 = b;
            x2 = b;
            y1 = mStartPoint.y;
            y2 = mEndPoint.y;
        } else if (mStartPoint.y == mEndPoint.y) {
            x1 = mStartPoint.x;
            x2 = mEndPoint.x;
            y1 = b;
            y2 = b;
        } else {
            x1 = -3;
            x2 = 3;
            y1 = x1 * mK + b;
            y2 = x2 * mK + b;
        }
        canvas.drawLine(
                (1 + (x1 - 1) / 2f) * mViewportRect.width(),
                -(y1 - 1) / 2f * mViewportRect.height(),
                (1 + (x2 - 1) / 2f) * mViewportRect.width(),
                -(y2 - 1) / 2f * mViewportRect.height(),
                mPaint);
    }

    private void initMaxSplitDis() {
        if (mStartPoint.x == mEndPoint.x) {
            float cx;
            if (mAppearMode == MODE_POINT_APPEAR) {
                cx = mStartPoint.x;
            } else {
                cx = mStartPoint.x + mBMoveDis;
            }
            mMaxSplitBDis = Math.max(Math.abs(cx - (-1)), Math.abs(1 - cx));
            return;
        } else if (mStartPoint.y == mEndPoint.y) {
            float cy;
            if (mAppearMode == MODE_POINT_APPEAR) {
                cy = mStartPoint.y;
            } else {
                cy = mStartPoint.y + mBMoveDis;
            }
            mMaxSplitBDis = Math.max(Math.abs(cy - (-1)), Math.abs(1 - cy));
            return;
        }

        float A, B, C;
        if (mAppearMode == MODE_POINT_APPEAR) {
            A = mEndPoint.y - mStartPoint.y;
            B = mStartPoint.x - mEndPoint.x;
            C = mEndPoint.x * mStartPoint.y - mStartPoint.x * mEndPoint.y;
        } else {
            //if(mAppearMode == MODE_MOVE_APPEAR)
            A = mK;
            B = -1;
            C = mB + mBMoveDis;
        }

        float sqrtA2AndB2 = (float) Math.sqrt(A * A + B * B);
        //获得离直线最远的点
        float ltDis = Math.abs(A * -1 + B * 1 + C) / sqrtA2AndB2;
        float rtDis = Math.abs(A * 1 + B * 1 + C) / sqrtA2AndB2;
        float rbDis = Math.abs(A * 1 + B * -1 + C) / sqrtA2AndB2;
        float lbDis = Math.abs(A * -1 + B * -1 + C) / sqrtA2AndB2;

        float max = Math.max(ltDis, rtDis);
        max = Math.max(max, rbDis);
        max = Math.max(max, lbDis);

        float parallelLineB;
        float k = mKExisted ? mK : 1;
        if (max == ltDis) {
            parallelLineB = 1 - k * (-1);
        } else if (max == rtDis) {
            parallelLineB = 1 - k * (1);
        } else if (max == rbDis) {
            parallelLineB = -1 - k * (1);
        } else {
            parallelLineB = -1 - k * (-1);
        }
        mMaxSplitBDis = Math.abs(parallelLineB - (mB + mBMoveDis));
    }

    public MovieSegment removeFirstAnim() {
//        mDuration = (int) (mDuration * (1f-LINE_ANIM_RATE) / 1f);
        LINE_ANIM_RATE = 0;
        return this;
    }

    public static List<MovieSegment<GLESCanvas>> initSegments(){
        List<MovieSegment<GLESCanvas>> segmentList = new ArrayList<MovieSegment<GLESCanvas>>(7);
        segmentList.add(new SingleBitmapSegment(500));
        segmentList.add(new WindowSegment(2.1f, 1f, 2.1f, -1f, -1.1f).removeFirstAnim());
        segmentList.add(new WindowSegment(-1f, 1f, 1f, -1f, 0f, 0f));
        segmentList.add(new WindowSegment(-1f, -2.1f, 1f, -2.1f, 1.1f).removeFirstAnim());
        segmentList.add(new WindowSegment(0f, 1f, 0f, -1f, 0f, 1f));
        segmentList.add(new WindowSegment(-1f, 0f, 1f, 0f, -1f, 0f));
        segmentList.add(new EndGaussianBlurSegment(PhotoMovieFactory.END_GAUSSIANBLUR_DURATION));
        return segmentList;
    }
}