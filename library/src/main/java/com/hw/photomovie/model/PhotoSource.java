package com.hw.photomovie.model;

import android.graphics.Bitmap;
import android.util.SparseArray;
import com.hw.photomovie.util.MLog;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by huangwei on 2015/5/25.
 */
public class PhotoSource implements PhotoData.OnDataLoadListener {
    private static final String TAG = "PhotoSource";
    /**
     * 每个PhotoData加载失败后重试的次数
     */
    protected static final int REPEAT_COUNT = 2;

    private List<PhotoData> mPhotoDataList = new Vector<PhotoData>();
    /**
     * 保存下载进度
     */
    private SparseArray<Float> mProgressArray = new SparseArray<Float>();
    /**
     * 保存下载失败的情况
     */
    private Map<PhotoData, Integer> mErrorMap = new ConcurrentHashMap<PhotoData, Integer>();

    private OnSourcePrepareListener mOnSourcePrepareListener;
    private AtomicInteger mDiskPrepared = new AtomicInteger(0);

    private int mRequiredDiskPrepareNum;

    public PhotoSource(List<PhotoData> photoDataList) {
        if(photoDataList!=null) {
            mPhotoDataList.addAll(photoDataList);
        }
        mDiskPrepared.set(0);
    }

    public int size() {
        return mPhotoDataList.size();
    }

    public PhotoData get(int i) {
        return i >= 0 && i < mPhotoDataList.size() ? mPhotoDataList.get(i) : null;
    }

    /**
     * @return 加载失败的数据
     */
    private List<PhotoData> getErrorList() {
        List<PhotoData> errorList = new ArrayList<PhotoData>(mErrorMap.keySet());
        for (int i = errorList.size() - 1; i < errorList.size() && i >= 0; i--) {
            Integer errorCount = mErrorMap.get(errorList.get(i));
            if (errorCount == null || errorCount < REPEAT_COUNT) {
                errorList.remove(i);
            }
        }
        return errorList;
    }

    public void prepare(int requiredDiskPrepareNum) {
        mRequiredDiskPrepareNum = requiredDiskPrepareNum;
        mProgressArray.clear();
        if (size() == 0) {
            if (mOnSourcePrepareListener != null) {
                mOnSourcePrepareListener.onPreparing(this, 1f);
                mOnSourcePrepareListener.onPrepared(this, 0, null);
            }
            return;
        }

        //如果之前有加载失败的，重置状态
        mPhotoDataList.addAll(mErrorMap.keySet());
        mErrorMap.clear();

        mDiskPrepared.set(0);

        for (int i = 0; i < size() && i < requiredDiskPrepareNum; i++) {
            PhotoData photoData = get(i);
            photoData.prepareData(PhotoData.STATE_LOCAL, this);
        }
    }

    public void prepare() {
        prepare(size());
    }

    public void setOnSourcePreparedListener(OnSourcePrepareListener onSourcePreparedListener) {
        this.mOnSourcePrepareListener = onSourcePreparedListener;
    }

    private synchronized void notifyPrepareProgress() {
        //计算下载进度
        float discPreparedProgress = 0;
        for (int i = 0; i < size() && i < mRequiredDiskPrepareNum; i++) {
            PhotoData data = get(i);
            discPreparedProgress += mProgressArray.get(data.hashCode(), 0f) * 1 / (float) mRequiredDiskPrepareNum;
        }

        //总进度
        float totalProgress = discPreparedProgress;
        if (mOnSourcePrepareListener != null) {
            mOnSourcePrepareListener.onPreparing(this, totalProgress);
            if (mDiskPrepared.get() >= mRequiredDiskPrepareNum) {
                mOnSourcePrepareListener.onPrepared(this, mDiskPrepared.get(), getErrorList());
                //继续下载剩余部分
                for (int i = mRequiredDiskPrepareNum; i < size(); i++) {
                    PhotoData photoData = get(i);
                    photoData.prepareData(PhotoData.STATE_LOCAL, null);
                }
            }
        }
        MLog.i(TAG, "onDownloadProgressUpdate:" + totalProgress);
    }

    @Override
    public void onDataLoaded(PhotoData photoData, Bitmap bitmap) {
        notifyPrepareProgress();
    }

    @Override
    public void onDownloaded(PhotoData photoData) {
        mDiskPrepared.addAndGet(1);
        mProgressArray.put(photoData.hashCode(), 1f);
        notifyPrepareProgress();
    }

    public List<PhotoData> getSourceData() {
        return new LinkedList<PhotoData>(mPhotoDataList);
    }

    @Override
    public void onDownloadProgressUpdate(PhotoData photoData, int current, int total) {
        //因为这里不好拿到待加载数据的总大小（byte），所以视为每一个文件都一样大，以此来计算下载总进度
        if (photoData == null) {
            return;
        }
        mProgressArray.put(photoData.hashCode(), current / (float) total);

        notifyPrepareProgress();
    }

    @Override
    public void onError(PhotoData photoData, ErrorReason errorReason) {
        Integer errorCount = mErrorMap.containsKey(photoData) ? (mErrorMap.get(photoData) + 1) : 1;
        mErrorMap.put(photoData, errorCount);

        if (errorCount >= REPEAT_COUNT) {
            //超过重试次数，放弃该任务
            MLog.e(TAG, photoData + " prepare error:" + errorCount + " 放弃加载。");
            mPhotoDataList.remove(photoData);
            notifyPrepareProgress();
        } else {
            //重试
            photoData.prepareData(photoData.getTargetState(), this);
            MLog.e(TAG, photoData + " prepare error:" + errorCount);
        }
    }

    public interface OnSourcePrepareListener {
        void onPreparing(PhotoSource photoSource, float progress);

        /**
         * @param photoSource
         * @param downloaded      已经下载好的数量
         * @param prepareFailList 加载失败，重试{@link #REPEAT_COUNT}次后仍然失败的PhotoData被从{@link #mPhotoDataList}
         *                        移到该列表
         */
        void onPrepared(PhotoSource photoSource, int downloaded, List<PhotoData> prepareFailList);

        /**
         * 不代表整个任务失败，只是{@link PhotoData}加载失败
         *
         * @param photoSource
         * @param photoData
         * @param errorReason
         */
        void onError(PhotoSource photoSource, PhotoData photoData, ErrorReason errorReason);
    }
}
