package com.hw.photomovie.record;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import com.hw.photomovie.util.MLog;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

/**
 * Created by huangwei on 2018/10/18.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class AudioRecordThread extends Thread {
    private static final String TAG = "AudioRecordThread";

    private String mAudioPath;
    private MediaMuxer mMediaMuxer;
    private CyclicBarrier mBarrier;
    private Exception mException;
    private long mVideoDurationUs;

    public AudioRecordThread(String audioPath, MediaMuxer mediaMuxer, CyclicBarrier muxerBarrier,long videoDurationMs) {
        super("AudioRecordThread");
        mAudioPath = audioPath;
        mMediaMuxer = mediaMuxer;
        mBarrier = muxerBarrier;
        mVideoDurationUs = videoDurationMs*1000;
    }

    @Override
    public void run() {
        super.run();
        try {
            if (android.os.Build.VERSION.SDK_INT >= 18) {
                recordImpl();
            }
        } catch (Exception e) {
            mException = e;
        }
    }

    private void recordImpl() throws IOException {
        MediaExtractor extractor = new MediaExtractor();
        try {
            extractor.setDataSource(mAudioPath);
            int audioTrackIndex = selectTrack(extractor, true);
            if (audioTrackIndex < 0) {
                throw new RuntimeException("No audio track!");
            }
            MediaFormat format = extractor.getTrackFormat(audioTrackIndex);
            String mimeType = format.getString(MediaFormat.KEY_MIME);
            if (MediaFormat.MIMETYPE_AUDIO_AAC.equals(mimeType)) {
                //AAC格式可以直接写入mp4
                recordAAC(extractor, format, audioTrackIndex);
            } else {
                //其它格式需要先解码成pcm,再编码成aac
                recordOtherAudio(extractor, format, audioTrackIndex);
            }
        }finally {
            extractor.release();
        }
    }

    private void recordAAC(MediaExtractor extractor,MediaFormat format,int audioTrackIndex){
        mMediaMuxer.addTrack(format);
        MLog.i(TAG, "addTrack:" + format);
        try {
            //wait addTrack
            mBarrier.await();
            //wait start
            mBarrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }


        int sampleRate = getAudioSampleRate(format);
        MLog.i(TAG, "sampleRate:" + sampleRate);
        final int AAC_FRAME_TIME_US = 1024 * 1000 * 1000 / sampleRate;

        ByteBuffer buffer = ByteBuffer.allocateDirect(getAudioMaxBufferSize(format));
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        long preLoopSampleTime = 0;
        long preSampleTime = 0;
        extractor.selectTrack(audioTrackIndex);
        while(true){
            long sampleTime = extractor.getSampleTime();
            if(sampleTime<0){
                //已到结尾，检查是否需要继续循环录入
                if(preSampleTime<mVideoDurationUs){
                    extractor.seekTo(0,MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                    preLoopSampleTime = preSampleTime + AAC_FRAME_TIME_US;
                    MLog.i(TAG, "Should loop,preLoopSampleTimeMs:"+preLoopSampleTime/1000);
                    continue;
                }else {
                    break;
                }
            }
            bufferInfo.presentationTimeUs = sampleTime+preLoopSampleTime;
            //检查是否已经足够
            if(bufferInfo.presentationTimeUs > mVideoDurationUs){
                MLog.i(TAG, "Record finished,last frame:"+bufferInfo.presentationTimeUs/1000);
                break;
            }
            bufferInfo.flags = extractor.getSampleFlags();
            buffer.position(0);
            bufferInfo.size = extractor.readSampleData(buffer,0);
            MLog.i(TAG, "writeSampleData,flag" + bufferInfo.flags+" size:"+bufferInfo.size+" timeMs:"+bufferInfo.presentationTimeUs/1000);
            mMediaMuxer.writeSampleData(audioTrackIndex,buffer,bufferInfo);
            preSampleTime = bufferInfo.presentationTimeUs;
            extractor.advance();
        }
        //notify Write Finish
        try {
            mBarrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }
    private void recordOtherAudio(MediaExtractor extractor,MediaFormat format,int audioTrackIndex){

    }

    public Exception getException() {
        return mException;
    }

    private int selectTrack(MediaExtractor extractor, boolean audio) {
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (audio) {
                if (mime.startsWith("audio/")) {
                    return i;
                }
            } else {
                if (mime.startsWith("video/")) {
                    return i;
                }
            }
        }
        return -5;
    }

    private int getAudioMaxBufferSize(MediaFormat format) {
        if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
            return format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
        } else {
            return 100 * 1000;
        }
    }

    private int getAudioSampleRate(MediaFormat format) {
        if (format.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
            return format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
        } else {
            return 14400;
        }
    }
}
