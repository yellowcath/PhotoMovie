package com.hw.photomovie.segment.strategy;

import com.hw.photomovie.PhotoMovie;
import com.hw.photomovie.model.PhotoData;
import com.hw.photomovie.segment.MovieSegment;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2015/6/12.
 */
public class ReallocStrategy implements RetryStrategy {
    @Override
    public List<PhotoData> getAvailableData(PhotoMovie photoMovie, MovieSegment movieSegment) {
        if (movieSegment == null) {
            return null;
        }
        if (photoMovie == null || photoMovie.getPhotoSource() == null) {
            return movieSegment.getAllocatedPhotos();
        }

        int required = movieSegment.getRequiredPhotoNum();
        List<PhotoData> segmentDatas = new LinkedList<PhotoData>(movieSegment.getAllocatedPhotos());
        for (int i = segmentDatas.size() - 1; i >= 0; i--) {
            PhotoData photoData = segmentDatas.get(i);
            //未加载好
            if (photoData.getState() < PhotoData.STATE_LOCAL) {
                segmentDatas.remove(i);
            }
        }
        //还需要多少
        int need = required - segmentDatas.size();
        List<PhotoData> source = photoMovie.getPhotoSource().getSourceData();
        List<PhotoData> availableList = new ArrayList<PhotoData>();
        for (PhotoData photoData : source) {
            //跳过没准备好或者已有的
            if (photoData.getState() < PhotoData.STATE_LOCAL || segmentDatas.contains(photoData)) {
                continue;
            }
            if (need <= 0) {
                return segmentDatas;
            }
            availableList.add(photoData);
        }
        //从可用的里面随机选
        while(need > 0 && availableList.size()>0){
            int ran = (int) (Math.random() * availableList.size());
            segmentDatas.add(availableList.get(ran));
            need--;
        }

        //如果还不够，就从自己身上重复
        int size = segmentDatas.size();
        for(int i=0;i<size;i++){
            if(need <=0){
                break;
            }
            PhotoData photoData = segmentDatas.get(i);
            segmentDatas.add(photoData);
            need--;
        }
        //还不够就没法了
        return segmentDatas;
    }
}
