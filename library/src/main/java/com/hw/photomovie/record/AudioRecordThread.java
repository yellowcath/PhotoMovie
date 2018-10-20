package com.hw.photomovie.record;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import com.hw.photomovie.util.MLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * Created by huangwei on 2018/10/18.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class AudioRecordThread extends Thread {
    private static final String TAG = "AudioRecordThread";

    private Context mContext;
    private String mAudioPath;
    private MediaMuxer mMediaMuxer;
    private CyclicBarrier mBarrier;
    private volatile Exception mException;
    private long mVideoDurationUs;

    public AudioRecordThread(Context context, String audioPath, MediaMuxer mediaMuxer, CyclicBarrier muxerBarrier, long videoDurationMs) {
        super("AudioRecordThread");
        mContext = context;
        mAudioPath = audioPath;
        mMediaMuxer = mediaMuxer;
        mBarrier = muxerBarrier;
        mVideoDurationUs = videoDurationMs * 1000;
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

    private void recordImpl() throws Exception {
        MediaExtractor extractor = new MediaExtractor();
        try {
            extractor.setDataSource(mAudioPath);
            int audioTrackIndex = selectTrack(extractor, true);
            if (audioTrackIndex < 0) {
                throw new RuntimeException("No audio track!");
            }
            MediaFormat format = extractor.getTrackFormat(audioTrackIndex);
            String mimeType = format.getString(MediaFormat.KEY_MIME);
            extractor.selectTrack(audioTrackIndex);
            if (MediaFormat.MIMETYPE_AUDIO_AAC.equals(mimeType)) {
                //AAC格式可以直接写入mp4
                recordAAC(extractor, format, audioTrackIndex);
            } else {
                //其它格式需要先解码成pcm,再编码成aac
                recordOtherAudio(extractor, format, audioTrackIndex);
            }
        } finally {
            //notify Write Finish
            try {
                mBarrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
            extractor.release();
        }
    }

    //AAC格式，直接写入mp4
    private void recordAAC(MediaExtractor extractor, MediaFormat format, int audioTrackIndex) {
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
        while (true) {
            long sampleTime = extractor.getSampleTime();
            if (sampleTime < 0) {
                //已到结尾，检查是否需要继续循环录入
                if (preSampleTime < mVideoDurationUs) {
                    extractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                    preLoopSampleTime = preSampleTime + AAC_FRAME_TIME_US;
                    MLog.i(TAG, "Should loop,preLoopSampleTimeMs:" + preLoopSampleTime / 1000);
                    continue;
                } else {
                    break;
                }
            }
            bufferInfo.presentationTimeUs = sampleTime + preLoopSampleTime;
            //检查是否已经足够
            if (bufferInfo.presentationTimeUs > mVideoDurationUs) {
                MLog.i(TAG, "Record finished,last frame:" + bufferInfo.presentationTimeUs / 1000);
                break;
            }
            bufferInfo.flags = extractor.getSampleFlags();
            buffer.position(0);
            bufferInfo.size = extractor.readSampleData(buffer, 0);
            MLog.i(TAG, "writeSampleData,flag" + bufferInfo.flags + " size:" + bufferInfo.size + " timeMs:" + bufferInfo.presentationTimeUs / 1000);
            mMediaMuxer.writeSampleData(audioTrackIndex, buffer, bufferInfo);
            preSampleTime = bufferInfo.presentationTimeUs;
            extractor.advance();
        }
    }

    //其它格式，先转换成AAC在写入
    private void recordOtherAudio(MediaExtractor extractor, MediaFormat format, int audioTrackIndex) throws IOException,IllegalArgumentException {
        MediaCodec decoder = null;
        try {
            decoder = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME));
        }catch (IOException|IllegalArgumentException e){
            MLog.e(TAG,"Create audio decoder failed!",e);
            try {
                //wait addTrack
                mBarrier.await();
                mBarrier.await();
            } catch (InterruptedException e1) {
                e.printStackTrace();
            } catch (BrokenBarrierException e2) {
                e.printStackTrace();
            }
            throw e;
        }

        File dir = new File(mContext.getCacheDir(), "AudioRecord");
        dir.mkdirs();
        long time = System.currentTimeMillis();
        File pcmFile = new File(dir, "pcm_"+time+".pcm");
        File wavFile = new File(dir, "wav_"+time+".wav");

        int sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
        int oriChannelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
        int channelConfig = AudioFormat.CHANNEL_IN_MONO;
        if (oriChannelCount == 2) {
            channelConfig = AudioFormat.CHANNEL_IN_STEREO;
        }

        MediaFormat encodeFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, sampleRate, oriChannelCount);
        encodeFormat.setInteger(MediaFormat.KEY_BIT_RATE, getAudioBitrate(format));//比特率
        encodeFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        checkCsd(encodeFormat,
                MediaCodecInfo.CodecProfileLevel.AACObjectLC,
                sampleRate,
                oriChannelCount);

        int muxerAudioTrack = mMediaMuxer.addTrack(encodeFormat);
        try {
            //wait addTrack
            mBarrier.await();
            mBarrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }

        decodeToPCM(decoder,extractor,format,pcmFile.getAbsolutePath(),mVideoDurationUs);
        new PcmToWavUtil(sampleRate, channelConfig, oriChannelCount, AudioFormat.ENCODING_PCM_16BIT).pcmToWav(pcmFile.getAbsolutePath(), wavFile.getAbsolutePath());


        encodeWAVToAAC(wavFile.getPath(), muxerAudioTrack, encodeFormat);
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

    private int getAudioBitrate(MediaFormat format) {
        if (format.containsKey(MediaFormat.KEY_BIT_RATE)) {
            return format.getInteger(MediaFormat.KEY_BIT_RATE);
        } else {
            final int DEFAULT_AAC_BITRATE = 192 * 1000;
            return DEFAULT_AAC_BITRATE;
        }
    }

    /**
     * 需要改变音频速率的情况下，需要先解码->改变速率->编码
     */
    private void decodeToPCM(MediaCodec decoder,MediaExtractor extractor, MediaFormat oriAudioFormat, String outPath, Long endTimeUs) throws IOException {
        int maxBufferSize = getAudioMaxBufferSize(oriAudioFormat);
        ByteBuffer buffer = ByteBuffer.allocateDirect(maxBufferSize);
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

        //调整音频速率需要重解码音频帧
        decoder.configure(oriAudioFormat, null, null, 0);
        decoder.start();

        boolean decodeDone = false;
        boolean decodeInputDone = false;
        final int TIMEOUT_US = 2500;
        File pcmFile = new File(outPath);
        FileChannel writeChannel = new FileOutputStream(pcmFile).getChannel();
        ByteBuffer[] inputBuffers = null;
        ByteBuffer[] outputBuffers = null;

        try {
            while (!decodeDone) {
                if (!decodeInputDone) {
                    boolean eof = false;
                    int decodeInputIndex = decoder.dequeueInputBuffer(TIMEOUT_US);
                    if(Build.VERSION.SDK_INT<21 && decodeInputIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED){
                            outputBuffers = decoder.getOutputBuffers();
                            inputBuffers = decoder.getInputBuffers();
                    } else if (decodeInputIndex >= 0) {
                        long sampleTimeUs = extractor.getSampleTime();
                        if (sampleTimeUs == -1) {
                            eof = true;
                        } else if (endTimeUs != null && sampleTimeUs > endTimeUs) {
                            eof = true;
                        }

                        if (eof) {
                            decodeInputDone = true;
                            decoder.queueInputBuffer(decodeInputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        } else {
                            info.size = extractor.readSampleData(buffer, 0);
                            info.presentationTimeUs = sampleTimeUs;
                            info.flags = extractor.getSampleFlags();
                            ByteBuffer inputBuffer = null;
                            if (android.os.Build.VERSION.SDK_INT >= 21) {
                                inputBuffer = decoder.getInputBuffer(decodeInputIndex);
                            } else {
                                inputBuffer = inputBuffers[decodeInputIndex];
                            }
                            inputBuffer.put(buffer);
                            MLog.i(TAG, "audio decode queueInputBuffer " + info.presentationTimeUs / 1000);
                            decoder.queueInputBuffer(decodeInputIndex, 0, info.size, info.presentationTimeUs, info.flags);
                            extractor.advance();
                        }

                    }
                }

                while (!decodeDone) {
                    int outputBufferIndex = decoder.dequeueOutputBuffer(info, TIMEOUT_US);
                    if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        break;
                    } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        MediaFormat newFormat = decoder.getOutputFormat();
                        MLog.i(TAG, "audio decode newFormat = " + newFormat);
                    } else if (outputBufferIndex < 0) {
                        //ignore
                        MLog.e(TAG, "unexpected result from audio decoder.dequeueOutputBuffer: " + outputBufferIndex);
                    } else {
                        if (info.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                            decodeDone = true;
                        } else {
                            ByteBuffer decodeOutputBuffer = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                decodeOutputBuffer = decoder.getOutputBuffer(outputBufferIndex);
                            } else {
                                decodeOutputBuffer = outputBuffers[outputBufferIndex];
                            }
                            MLog.i(TAG, "audio decode saveFrame " + info.presentationTimeUs / 1000);
                            writeChannel.write(decodeOutputBuffer);
                        }
                        decoder.releaseOutputBuffer(outputBufferIndex, false);
                    }
                }
            }
        } finally {
            writeChannel.close();
            extractor.release();
            decoder.stop();
            decoder.release();
        }
    }

    /**
     * 将WAV音频编码成Aac
     *
     * @param wavPath
     * @param muxerTrackIndex
     * @param aacFormat 待编码成的AAC格式，需包含{@link MediaFormat#KEY_SAMPLE_RATE}
     *                  ,{@link MediaFormat#KEY_CHANNEL_COUNT},{@link MediaFormat#KEY_BIT_RATE},
     *                  {@link MediaFormat#KEY_MAX_INPUT_SIZE}，前两个必须
     * @throws IOException
     */
    private void encodeWAVToAAC(String wavPath, int muxerTrackIndex, MediaFormat aacFormat) throws IOException {
        int sampleRate = aacFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
        int channelCount = aacFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
        int bitrate = getAudioBitrate(aacFormat);
        int maxBufferSize = getAudioMaxBufferSize(aacFormat);

        MediaCodec encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
        MediaFormat encodeFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, sampleRate, channelCount);//参数对应-> mime type、采样率、声道数
        encodeFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);//比特率
        encodeFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        encodeFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, maxBufferSize);
        encoder.configure(encodeFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        encoder.start();

        MediaExtractor wavExtrator = new MediaExtractor();
        wavExtrator.setDataSource(wavPath);
        int audioTrackIndex = selectTrack(wavExtrator, true);
        wavExtrator.selectTrack(audioTrackIndex);

        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        ByteBuffer buffer = ByteBuffer.allocateDirect(maxBufferSize);
        boolean encodeInputDone = false;
        long lastAudioFrameTimeUs = -1;
        final int TIMEOUT_US = 2500;
        final int AAC_FRAME_TIME_US = 1024 * 1000 * 1000 / sampleRate;
        boolean detectTimeError = false;
        ByteBuffer[] inputBuffers = null;
        ByteBuffer[] outputBuffers = null;
        try {
            boolean encodeDone = false;
            while (!encodeDone) {
                int inputBufferIndex = encoder.dequeueInputBuffer(TIMEOUT_US);
                if(Build.VERSION.SDK_INT<21 && inputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED){
                    inputBuffers = encoder.getInputBuffers();
                    outputBuffers = encoder.getOutputBuffers();
                }
                if (!encodeInputDone && inputBufferIndex >= 0) {
                    long sampleTime = wavExtrator.getSampleTime();
                    if (sampleTime < 0) {
                        encodeInputDone = true;
                        encoder.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    } else {
                        int flags = wavExtrator.getSampleFlags();
                        buffer.clear();
                        int size = wavExtrator.readSampleData(buffer, 0);
                        ByteBuffer inputBuffer = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            inputBuffer = encoder.getInputBuffer(inputBufferIndex);
                        }else{
                            inputBuffer = inputBuffers[inputBufferIndex];
                        }
                        inputBuffer.clear();
                        inputBuffer.put(buffer);
                        inputBuffer.position(0);
                        MLog.i(TAG,"audio queuePcmBuffer " + sampleTime / 1000 + " size:" + size);
                        encoder.queueInputBuffer(inputBufferIndex, 0, size, sampleTime, flags);
                        wavExtrator.advance();
                    }
                }

                while (true) {
                    int outputBufferIndex = encoder.dequeueOutputBuffer(info, TIMEOUT_US);
                    if (outputBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        break;
                    } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        MediaFormat newFormat = encoder.getOutputFormat();
                        MLog.i(TAG,"audio decode newFormat = " + newFormat);
                    } else if (outputBufferIndex < 0) {
                        //ignore
                        MLog.e(TAG,"unexpected result from audio decoder.dequeueOutputBuffer: " + outputBufferIndex);
                    } else {
                        if (info.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                            encodeDone = true;
                            break;
                        }
                        ByteBuffer encodeOutputBuffer = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            encodeOutputBuffer = encoder.getOutputBuffer(outputBufferIndex);
                        }else{
                            encodeOutputBuffer = outputBuffers[outputBufferIndex];
                        }
                        MLog.i(TAG,"audio writeSampleData " + info.presentationTimeUs + " size:" + info.size + " flags:" + info.flags);
                        if (!detectTimeError && lastAudioFrameTimeUs != -1 && info.presentationTimeUs < lastAudioFrameTimeUs + AAC_FRAME_TIME_US) {
                            //某些情况下帧时间会出错，目前未找到原因（系统相机录得双声道视频正常，我录的单声道视频不正常）
                            MLog.e(TAG,"audio 时间戳错误，lastAudioFrameTimeUs:" + lastAudioFrameTimeUs + " " +
                                    "info.presentationTimeUs:" + info.presentationTimeUs);
                            detectTimeError = true;
                        }
                        if (detectTimeError) {
                            info.presentationTimeUs = lastAudioFrameTimeUs + AAC_FRAME_TIME_US;
                            MLog.e(TAG,"audio 时间戳错误，使用修正的时间戳:" + info.presentationTimeUs);
                            detectTimeError = false;
                        }
                        if (info.flags != MediaCodec.BUFFER_FLAG_CODEC_CONFIG) {
                            lastAudioFrameTimeUs = info.presentationTimeUs;
                        }
                        mMediaMuxer.writeSampleData(muxerTrackIndex, encodeOutputBuffer, info);

                        encodeOutputBuffer.clear();
                        encoder.releaseOutputBuffer(outputBufferIndex, false);
                    }
                }
            }
        } finally {
            wavExtrator.release();
            encoder.release();
        }
    }

    private final static Map<Integer, Integer> freqIdxMap = new HashMap<Integer, Integer>();

    static {
        freqIdxMap.put(96000, 0);
        freqIdxMap.put(88200, 1);
        freqIdxMap.put(64000, 2);
        freqIdxMap.put(48000, 3);
        freqIdxMap.put(44100, 4);
        freqIdxMap.put(32000, 5);
        freqIdxMap.put(24000, 6);
        freqIdxMap.put(22050, 7);
        freqIdxMap.put(16000, 8);
        freqIdxMap.put(12000, 9);
        freqIdxMap.put(11025, 10);
        freqIdxMap.put(8000, 11);
        freqIdxMap.put(7350, 12);
    }
    private void checkCsd(MediaFormat audioMediaFormat, int profile, int sampleRate, int channel) {
        int freqIdx = freqIdxMap.containsKey(sampleRate) ? freqIdxMap.get(sampleRate) : 4;
//        byte[] bytes = new byte[]{(byte) 0x11, (byte) 0x90};
//        ByteBuffer bb = ByteBuffer.wrap(bytes);
        ByteBuffer csd = ByteBuffer.allocate(2);
        csd.put(0, (byte) (profile << 3 | freqIdx >> 1));
        csd.put(1, (byte) ((freqIdx & 0x01) << 7 | channel << 3));
        audioMediaFormat.setByteBuffer("csd-0", csd);
    }
}
