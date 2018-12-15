package com.hw.photomovie.record;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import com.hw.photomovie.PhotoMovie;
import com.hw.photomovie.render.GLSurfaceMovieRenderer;
import com.hw.photomovie.segment.MovieSegment;
import com.hw.photomovie.util.MLog;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * Created by huangwei on 2015/5/26.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class GLMovieRecorder {
    private static final String TAG = "GLMovieRecorder";

    private Context mContext;
    private GLSurfaceMovieRenderer mGLSurfaceMovieRenderer;
    private boolean mInited;
    private HandlerThread mRecordThread = new HandlerThread("GLMovieRecorder");
    private String mAudioPath;
    private CyclicBarrier mCyclicBarrier;
    private Exception mAudioRecordException;

    public GLMovieRecorder(Context context) {
        mContext = context.getApplicationContext();
        mRecordThread.start();
    }

    public void setDataSource(GLSurfaceMovieRenderer glSurfaceMovieRenderer) {
        mGLSurfaceMovieRenderer = glSurfaceMovieRenderer;
    }

    public void setMusic(String audioPath){
        mAudioPath = audioPath;
    }
    public void configOutput(int width, int height, int bitRate, int frameRate, int iFrameInterval, String outputPath) {
        mWidth = width;
        mHeight = height;
        mBitRate = bitRate;
        mFrameRate = frameRate;
        mIFrameInterval = iFrameInterval;
        mOutputPath = outputPath;
        mInited = true;
    }

    public void startRecord(final OnRecordListener listener) {
        if (!mInited) {
            throw new RuntimeException("please configOutput first.");
        }
        if (mGLSurfaceMovieRenderer == null) {
            throw new RuntimeException("please setDataSource first.");
        }
        final Handler handler = new Handler(mRecordThread.getLooper());
        PhotoMovie photoMovie = mGLSurfaceMovieRenderer.getPhotoMovie();
        final MovieSegment firstSegment = (MovieSegment) photoMovie.getMovieSegments().get(0);
        firstSegment.setOnSegmentPrepareListener(new MovieSegment.OnSegmentPrepareListener() {
            @Override
            public void onSegmentPrepared(boolean success) {
                firstSegment.setOnSegmentPrepareListener(null);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        boolean success = false;
                        try {
                            startRecordImpl(listener);
                            success = true;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (listener != null) {
                            final boolean finalSuccess = success;
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onRecordFinish(finalSuccess);
                                }
                            });
                        }
                    }
                });
            }
        });
        firstSegment.prepare();
    }

    public void startRecordImpl(final OnRecordListener listener) throws IOException {
        prepareEncoder();
        mInputSurface.makeCurrent();

        //prepare要在 mInputSurface.makeCurrent();之后调用。因为切换了eglSurface之后，GLESCanvas之前上传到GPU的program都失效了
        mGLSurfaceMovieRenderer.setRenderToRecorder(true);
        if (mGLSurfaceMovieRenderer.getMovieFilter() != null) {
            mGLSurfaceMovieRenderer.getMovieFilter().release();
        }
        mGLSurfaceMovieRenderer.prepare();
        mGLSurfaceMovieRenderer.setViewport(mWidth, mHeight);

        //开始录制
        PhotoMovie photoMovie = mGLSurfaceMovieRenderer.getPhotoMovie();

        AudioRecordThread audioRecordThread = null;
        if(!TextUtils.isEmpty(mAudioPath)){
            mCyclicBarrier = new CyclicBarrier(2);
            audioRecordThread = new AudioRecordThread(mContext,mAudioPath,mMuxer,mCyclicBarrier,photoMovie.getDuration());
            audioRecordThread.start();
        }else{
            mCyclicBarrier = new CyclicBarrier(1);
        }
        int duration;
        int elapsedTime = 0;
        int frameCount = 0;
        int frameTime = (int) (1000f / mFrameRate);
        int totalDuration = photoMovie.getDuration();
        try {
            while (true) {
                long s = System.currentTimeMillis();
                // Feed any pending encoder output into the muxer.
                drainEncoder(false);
                long s1 = System.currentTimeMillis();
                mGLSurfaceMovieRenderer.drawFrame(elapsedTime);
                long e1 = System.currentTimeMillis();

                mInputSurface.setPresentationTime(computePresentationTimeNsec(frameCount));//这句注释了照样跑，？
                mInputSurface.swapBuffers();
                long e = System.currentTimeMillis();

                MLog.i(TAG, "com.hw.photomovie.record frame " + frameCount);
                MLog.i(TAG, "com.hw.photomovie.record 耗时 " + (e - s) + "ms" + " 绘制耗时:" + (e1 - s1) + "ms");
                frameCount++;
                elapsedTime += frameTime;
                /**
                 * 有的PhotoMovie时长是会变化的{@link com.hw.photomovie.segment.MovieSegment.IS_DURATION_VARIABLE}
                 */
                duration = photoMovie.getDuration();
                if (listener != null) {
                    listener.onRecordProgress(elapsedTime, totalDuration);
                }
                if (elapsedTime > duration) {
                    break;
                }
            }
            drainEncoder(true);
        }catch (Exception e){
            e.printStackTrace();
            MLog.e(TAG, "Encode Error",e);
        }
        finally {
            mGLSurfaceMovieRenderer.releaseInGLThread();
            releaseEncoder();
            mGLSurfaceMovieRenderer.setRenderToRecorder(false);
            if(audioRecordThread!=null){
                try {
                    audioRecordThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            mAudioRecordException = audioRecordThread==null?null:audioRecordThread.getException();
        }
    }

    public Exception getAudioRecordException() {
        return mAudioRecordException;
    }

    private static final boolean VERBOSE = true;           // lots of logging

    // parameters for the encoder
    private static final String MIME_TYPE = "video/avc";    // H.264 Advanced Video Coding

    // size of a frame, in pixels
    private int mWidth = -1;
    private int mHeight = -1;
    // bit rate, in bits per second
    private int mBitRate = -1;

    private int mFrameRate = 30; //帧率
    private int mIFrameInterval = 10; // 10 seconds between I-frames

    // encoder / muxer state
    private MediaCodec mEncoder;
    private CodecInputSurface mInputSurface;
    private MediaMuxer mMuxer;
    private int mTrackIndex;
    private boolean mMuxerStarted;

    // allocate one of these up front so we don't need to do it every time
    private MediaCodec.BufferInfo mBufferInfo;
    //视频输出路径
    private String mOutputPath;

    private int getEven(int n) {
        return n % 2 == 0 ? n : n + 1;
    }

    /**
     * Configures encoder and muxer state, and prepares the input Surface.
     */
    private void prepareEncoder() throws IOException {
        mBufferInfo = new MediaCodec.BufferInfo();
        mEncoder = MediaCodec.createEncoderByType(MIME_TYPE);
        MLog.i(TAG,"encoder name:"+mEncoder.getName());

        if (mEncoder.getName().equals("OMX.MTK.VIDEO.ENCODER.AVC")) {
            if(mWidth>mHeight && mWidth>1920){
                mHeight = (int) (mHeight/(mWidth/1920f));
                mWidth = 1920;
                MLog.e(TAG,"The encoder limited max size,set size to "+mWidth+" X "+ mHeight);
            }else if(mHeight>mWidth && mHeight>1920){
                mWidth = (int) (mWidth/(mHeight/1920f));
                mHeight = 1920;
                MLog.e(TAG,"The encoder limited max size,set size to "+mWidth+" X "+ mHeight);
            }
        }
        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE,getEven(mWidth), getEven(mHeight));
        // Set some properties.  Failing to specify some of these can cause the MediaCodec
        // configure() call to throw an unhelpful exception.
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, mBitRate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, mFrameRate);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, mIFrameInterval);
        if (VERBOSE) {
            Log.d(TAG, "format: " + format);
        }

        // Create a MediaCodec encoder, and configure it with our format.  Get a Surface
        // we can use for input and wrap it with a class that handles the EGL work.
        //
        // If you want to have two EGL contexts -- one for display, one for recording --
        // you will likely want to defer instantiation of CodecInputSurface until after the
        // "display" EGL context is created, then modify the eglCreateContext call to
        // take eglGetCurrentContext() as the share_context argument.
        mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mInputSurface = new CodecInputSurface(mEncoder.createInputSurface());
        mEncoder.start();

        // Output filename.  Ideally this would use Context.getFilesDir() rather than a
        // hard-coded output directory.
        String outputPath = mOutputPath;
        Log.d(TAG, "output file is " + outputPath);


        // Create a MediaMuxer.  We can't add the video track and start() the muxer here,
        // because our MediaFormat doesn't have the Magic Goodies.  These can only be
        // obtained from the encoder after it has started processing data.
        //
        // We're not actually interested in multiplexing audio.  We just want to convert
        // the raw H.264 elementary stream we get from MediaCodec into a .mp4 file.
        try {
            mMuxer = new MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException ioe) {
            throw new RuntimeException("MediaMuxer creation failed", ioe);
        }

        mTrackIndex = -1;
        mMuxerStarted = false;
    }

    /**
     * Releases encoder resources.  May be called after partial / failed initialization.
     */
    private void releaseEncoder() {
        if (VERBOSE) {
            Log.d(TAG, "releasing encoder objects");
        }
        if (mEncoder != null) {
            mEncoder.stop();
            mEncoder.release();
            mEncoder = null;
        }
        if (mInputSurface != null) {
            mInputSurface.release();
            mInputSurface = null;
        }
        if (mMuxer != null) {
            try {
                mCyclicBarrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
            mMuxer.stop();
            mMuxer.release();
            mMuxer = null;
        }
    }

    private void drainEncoder(boolean endOfStream) {
        if (Build.VERSION.SDK_INT < 21) {
            drainEncoderImpl(endOfStream);
        } else {
            drainEncoderApi21(endOfStream);
        }
    }

    /**
     * Extracts all pending data from the encoder.
     * <p/>
     * If endOfStream is not set, this returns when there is no more data to drain.  If it
     * is set, we send EOS to the encoder, and then iterate until we see EOS on the output.
     * Calling this with endOfStream set should be done once, right before stopping the muxer.
     */
    private void drainEncoderImpl(boolean endOfStream) {
        final int TIMEOUT_USEC = 10000;
        if (VERBOSE) {
            Log.d(TAG, "drainEncoder(" + endOfStream + ")");
        }

        if (endOfStream) {
            if (VERBOSE) {
                Log.d(TAG, "sending EOS to encoder");
            }
            mEncoder.signalEndOfInputStream();
        }

        ByteBuffer[] encoderOutputBuffers = mEncoder.getOutputBuffers();
        while (true) {
            int encoderStatus = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // no output available yet
                if (!endOfStream) {
                    break;      // out of while
                } else {
                    if (VERBOSE) {
                        Log.d(TAG, "no output available, spinning to await EOS");
                    }
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // not expected for an encoder
                encoderOutputBuffers = mEncoder.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // should happen before receiving buffers, and should only happen once
                if (mMuxerStarted) {
                    throw new RuntimeException("format changed twice");
                }
                MediaFormat newFormat = mEncoder.getOutputFormat();
                Log.d(TAG, "encoder output format changed: " + newFormat);

                // now that we have the Magic Goodies, start the muxer
                mTrackIndex = mMuxer.addTrack(newFormat);
                try {
                    mCyclicBarrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
                mMuxer.start();
                try {
                    mCyclicBarrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
                mMuxerStarted = true;
            } else if (encoderStatus < 0) {
                Log.w(TAG, "unexpected result from encoder.dequeueOutputBuffer: " +
                        encoderStatus);
                // let's ignore it
            } else {
                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if (encodedData == null) {
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus +
                            " was null");
                }

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    // The codec config data was pulled out and fed to the muxer when we got
                    // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                    if (VERBOSE) {
                        Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
                    }
                    mBufferInfo.size = 0;
                }

                if (mBufferInfo.size != 0) {
                    if (!mMuxerStarted) {
                        throw new RuntimeException("muxer hasn't started");
                    }

                    // adjust the ByteBuffer values to match BufferInfo (not needed?)
                    encodedData.position(mBufferInfo.offset);
                    encodedData.limit(mBufferInfo.offset + mBufferInfo.size);

                    mMuxer.writeSampleData(mTrackIndex, encodedData, mBufferInfo);
                    if (VERBOSE) {
                        Log.d(TAG, "sent " + mBufferInfo.size + " bytes to muxer");
                    }
                }

                mEncoder.releaseOutputBuffer(encoderStatus, false);

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (!endOfStream) {
                        Log.w(TAG, "reached end of stream unexpectedly");
                    } else {
                        if (VERBOSE) {
                            Log.d(TAG, "end of stream reached");
                        }
                    }
                    break;      // out of while
                }
            }
        }
    }


    /**
     * Extracts all pending data from the encoder.
     * <p/>
     * If endOfStream is not set, this returns when there is no more data to drain.  If it
     * is set, we send EOS to the encoder, and then iterate until we see EOS on the output.
     * Calling this with endOfStream set should be done once, right before stopping the muxer.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void drainEncoderApi21(boolean endOfStream) {
        final int TIMEOUT_USEC = 10000;
        if (VERBOSE) {
            Log.d(TAG, "drainEncoder(" + endOfStream + ")");
        }

        if (endOfStream) {
            if (VERBOSE) {
                Log.d(TAG, "sending EOS to encoder");
            }
            mEncoder.signalEndOfInputStream();
        }


        while (true) {
            int encoderIndex = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
            if (encoderIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // no output available yet
                if (!endOfStream) {
                    break;      // out of while
                } else {
                    if (VERBOSE) {
                        Log.d(TAG, "no output available, spinning to await EOS");
                    }
                }
            } else if (encoderIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // not expected for an encoder
            } else if (encoderIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // should happen before receiving buffers, and should only happen once
                if (mMuxerStarted) {
                    throw new RuntimeException("format changed twice");
                }
                MediaFormat newFormat = mEncoder.getOutputFormat();
                Log.d(TAG, "encoder output format changed: " + newFormat);

                // now that we have the Magic Goodies, start the muxer
                mTrackIndex = mMuxer.addTrack(newFormat);
                try {
                    mCyclicBarrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
                mMuxer.start();
                try {
                    mCyclicBarrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
                mMuxerStarted = true;
            } else if (encoderIndex < 0) {
                Log.w(TAG, "unexpected result from encoder.dequeueOutputBuffer: " +
                        encoderIndex);
                // let's ignore it
            } else {
                ByteBuffer outputBuffer = mEncoder.getOutputBuffer(encoderIndex);
                if (outputBuffer == null) {
                    throw new RuntimeException("encoderOutputBuffer " + encoderIndex +
                            " was null");
                }

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    // The codec config data was pulled out and fed to the muxer when we got
                    // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                    if (VERBOSE) {
                        Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
                    }
                    mBufferInfo.size = 0;
                }

                if (mBufferInfo.size != 0) {
                    if (!mMuxerStarted) {
                        throw new RuntimeException("muxer hasn't started");
                    }

                    // adjust the ByteBuffer values to match BufferInfo (not needed?)
                    outputBuffer.position(mBufferInfo.offset);
                    outputBuffer.limit(mBufferInfo.offset + mBufferInfo.size);

                    mMuxer.writeSampleData(mTrackIndex, outputBuffer, mBufferInfo);
                    if (VERBOSE) {
                        Log.d(TAG, "sent " + mBufferInfo.size + " bytes to muxer");
                    }
                }

                mEncoder.releaseOutputBuffer(encoderIndex, false);

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (!endOfStream) {
                        Log.w(TAG, "reached end of stream unexpectedly");
                    } else {
                        if (VERBOSE) {
                            Log.d(TAG, "end of stream reached");
                        }
                    }
                    break;      // out of while
                }
            }
        }
    }

    /**
     * Generates the presentation time for frame N, in nanoseconds.
     */
    private long computePresentationTimeNsec(int frameIndex) {
        final long ONE_BILLION = 1000000000;
        return frameIndex * ONE_BILLION / mFrameRate;
    }


    /**
     * Holds state associated with a Surface used for MediaCodec encoder input.
     * <p/>
     * The constructor takes a Surface obtained from MediaCodec.createInputSurface(), and uses that
     * to create an EGL window surface.  Calls to eglSwapBuffers() cause a frame of data to be sent
     * to the video encoder.
     * <p/>
     * This object owns the Surface -- releasing this will release the Surface too.
     */
    private static class CodecInputSurface {
        private static final int EGL_RECORDABLE_ANDROID = 0x3142;

        private EGLDisplay mEGLDisplay = EGL14.EGL_NO_DISPLAY;
        private EGLContext mEGLContext = EGL14.EGL_NO_CONTEXT;
        private EGLSurface mEGLSurface = EGL14.EGL_NO_SURFACE;

        private Surface mSurface;

        /**
         * Creates a CodecInputSurface from a Surface.
         */
        public CodecInputSurface(Surface surface) {
            if (surface == null) {
                throw new NullPointerException();
            }
            mSurface = surface;

            eglSetup();
        }

        /**
         * Prepares EGL.  We want a GLES 2.0 context and a surface that supports recording.
         */
        private void eglSetup() {
            mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
            if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
                throw new RuntimeException("unable to get EGL14 display");
            }
            int[] version = new int[2];
            if (!EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1)) {
                throw new RuntimeException("unable to initialize EGL14");
            }

            // Configure EGL for recording and OpenGL ES 2.0.
            int[] attribList = {
                    EGL14.EGL_RED_SIZE, 8,
                    EGL14.EGL_GREEN_SIZE, 8,
                    EGL14.EGL_BLUE_SIZE, 8,
                    EGL14.EGL_ALPHA_SIZE, 8,
                    EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                    EGL_RECORDABLE_ANDROID, 1,
                    EGL14.EGL_NONE
            };
            android.opengl.EGLConfig[] configs = new android.opengl.EGLConfig[1];
            int[] numConfigs = new int[1];
            EGL14.eglChooseConfig(mEGLDisplay, attribList, 0, configs, 0, configs.length,
                    numConfigs, 0);
            checkEglError("eglCreateContext RGB888+recordable ES2");

            // Configure context for OpenGL ES 2.0.
            int[] attrib_list = {
                    EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                    EGL14.EGL_NONE
            };
            mEGLContext = EGL14.eglCreateContext(mEGLDisplay, configs[0], EGL14.EGL_NO_CONTEXT,
                    attrib_list, 0);
            checkEglError("eglCreateContext");

            // Create a window surface, and attach it to the Surface we received.
            int[] surfaceAttribs = {
                    EGL14.EGL_NONE
            };
            mEGLSurface = EGL14.eglCreateWindowSurface(mEGLDisplay, configs[0], mSurface,
                    surfaceAttribs, 0);
            checkEglError("eglCreateWindowSurface");
        }

        /**
         * Discards all resources held by this class, notably the EGL context.  Also releases the
         * Surface that was passed to our constructor.
         */
        public void release() {
            if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
                EGL14.eglMakeCurrent(mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE,
                        EGL14.EGL_NO_CONTEXT);
                EGL14.eglDestroySurface(mEGLDisplay, mEGLSurface);
                EGL14.eglDestroyContext(mEGLDisplay, mEGLContext);
                EGL14.eglReleaseThread();
                EGL14.eglTerminate(mEGLDisplay);
            }

            mSurface.release();

            mEGLDisplay = EGL14.EGL_NO_DISPLAY;
            mEGLContext = EGL14.EGL_NO_CONTEXT;
            mEGLSurface = EGL14.EGL_NO_SURFACE;

            mSurface = null;
        }

        /**
         * Makes our EGL context and surface current.
         */
        public void makeCurrent() {
            EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext);
            checkEglError("eglMakeCurrent");
        }

        /**
         * Calls eglSwapBuffers.  Use this to "publish" the current frame.
         */
        public boolean swapBuffers() {
            boolean result = EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurface);
            checkEglError("eglSwapBuffers");
            return result;
        }

        /**
         * Sends the presentation time stamp to EGL.  Time is expressed in nanoseconds.
         */
        public void setPresentationTime(long nsecs) {
            EGLExt.eglPresentationTimeANDROID(mEGLDisplay, mEGLSurface, nsecs);
            checkEglError("eglPresentationTimeANDROID");
        }

        /**
         * Checks for EGL errors.  Throws an exception if one is found.
         */
        private void checkEglError(String msg) {
            int error;
            if ((error = EGL14.eglGetError()) != EGL14.EGL_SUCCESS) {
                throw new RuntimeException(msg + ": EGL error: 0x" + Integer.toHexString(error));
            }
        }
    }

    public interface OnRecordListener {
        void onRecordFinish(boolean success);

        void onRecordProgress(int recordedDuration, int totalDuration);
    }
}
