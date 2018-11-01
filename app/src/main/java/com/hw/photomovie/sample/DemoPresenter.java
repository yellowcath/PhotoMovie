package com.hw.photomovie.sample;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.Toast;
import com.hw.photomovie.PhotoMovie;
import com.hw.photomovie.PhotoMovieFactory;
import com.hw.photomovie.PhotoMoviePlayer;
import com.hw.photomovie.model.PhotoData;
import com.hw.photomovie.model.PhotoSource;
import com.hw.photomovie.model.SimplePhotoData;
import com.hw.photomovie.record.GLMovieRecorder;
import com.hw.photomovie.render.GLSurfaceMovieRenderer;
import com.hw.photomovie.render.GLTextureMovieRender;
import com.hw.photomovie.render.GLTextureView;
import com.hw.photomovie.render.MovieRenderer;
import com.hw.photomovie.sample.widget.FilterItem;
import com.hw.photomovie.sample.widget.FilterType;
import com.hw.photomovie.sample.widget.MovieFilterView;
import com.hw.photomovie.sample.widget.MovieTransferView;
import com.hw.photomovie.sample.widget.TransferItem;
import com.hw.photomovie.timer.IMovieTimer;
import com.hw.photomovie.util.MLog;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by huangwei on 2018/9/9.
 */
public class DemoPresenter implements MovieFilterView.FilterCallback, IMovieTimer.MovieListener, MovieTransferView.TransferCallback {
    private IDemoView mDemoView;

    private PhotoMovie mPhotoMovie;
    private PhotoMoviePlayer mPhotoMoviePlayer;
    private GLSurfaceMovieRenderer mMovieRenderer;
    private Uri mMusicUri;
    private PhotoMovieFactory.PhotoMovieType mMovieType = PhotoMovieFactory.PhotoMovieType.HORIZONTAL_TRANS;

    public void attachView(IDemoView demoView) {
        mDemoView = demoView;
        initFilters();
        initTransfers();
        initMoviePlayer();
    }

    private void initTransfers() {
        List<TransferItem> items = new LinkedList<TransferItem>();
        items.add(new TransferItem(R.drawable.ic_movie_transfer, "LeftRight", PhotoMovieFactory.PhotoMovieType.HORIZONTAL_TRANS));
        items.add(new TransferItem(R.drawable.ic_movie_transfer, "UpDown", PhotoMovieFactory.PhotoMovieType.VERTICAL_TRANS));
        items.add(new TransferItem(R.drawable.ic_movie_transfer, "Window", PhotoMovieFactory.PhotoMovieType.WINDOW));
        items.add(new TransferItem(R.drawable.ic_movie_transfer, "Gradient", PhotoMovieFactory.PhotoMovieType.GRADIENT));
        items.add(new TransferItem(R.drawable.ic_movie_transfer, "Tranlation", PhotoMovieFactory.PhotoMovieType.SCALE_TRANS));
        items.add(new TransferItem(R.drawable.ic_movie_transfer, "Thaw", PhotoMovieFactory.PhotoMovieType.THAW));
        items.add(new TransferItem(R.drawable.ic_movie_transfer, "Scale", PhotoMovieFactory.PhotoMovieType.SCALE));
        mDemoView.setTransfers(items);
    }

    private void initFilters() {
        List<FilterItem> items = new LinkedList<FilterItem>();
        items.add(new FilterItem(R.drawable.filter_default, "None", FilterType.NONE));
        items.add(new FilterItem(R.drawable.gray, "BlackWhite", FilterType.GRAY));
        items.add(new FilterItem(R.drawable.kuwahara, "Watercolour", FilterType.KUWAHARA));
        items.add(new FilterItem(R.drawable.snow, "Snow", FilterType.SNOW));
        items.add(new FilterItem(R.drawable.l1, "Lut_1", FilterType.LUT1));
        items.add(new FilterItem(R.drawable.cameo, "Cameo", FilterType.CAMEO));
        items.add(new FilterItem(R.drawable.l2, "Lut_2", FilterType.LUT2));
        items.add(new FilterItem(R.drawable.l3, "Lut_3", FilterType.LUT3));
        items.add(new FilterItem(R.drawable.l4, "Lut_4", FilterType.LUT4));
        items.add(new FilterItem(R.drawable.l5, "Lut_5", FilterType.LUT5));
        mDemoView.setFilters(items);
    }

    private void initMoviePlayer() {
        final GLTextureView glTextureView = mDemoView.getGLView();


        mMovieRenderer = new GLTextureMovieRender(glTextureView);
        addWaterMark();
        mPhotoMoviePlayer = new PhotoMoviePlayer(mDemoView.getActivity().getApplicationContext());
        mPhotoMoviePlayer.setMovieRenderer(mMovieRenderer);
        mPhotoMoviePlayer.setMovieListener(this);
        mPhotoMoviePlayer.setLoop(true);
        mPhotoMoviePlayer.setOnPreparedListener(new PhotoMoviePlayer.OnPreparedListener() {
            @Override
            public void onPreparing(PhotoMoviePlayer moviePlayer, float progress) {
            }

            @Override
            public void onPrepared(PhotoMoviePlayer moviePlayer, int prepared, int total) {
                mDemoView.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPhotoMoviePlayer.start();
                    }
                });

            }

            @Override
            public void onError(PhotoMoviePlayer moviePlayer) {
                MLog.i("onPrepare", "onPrepare error");
            }
        });
    }

    private void addWaterMark(){
//        Bitmap waterMark = BitmapFactory.decodeResource(mDemoView.getActivity().getResources(),R.drawable.watermark);
//        DisplayMetrics displayMetrics = mDemoView.getActivity().getResources().getDisplayMetrics();
//        mMovieRenderer.setWaterMark(waterMark,new RectF(displayMetrics.widthPixels-waterMark.getWidth(),0,displayMetrics.widthPixels,waterMark.getHeight()),0.5f);
//
//        mMovieRenderer.setWaterMark("Watermark",40, Color.argb(100,255,0,0),100,100);
    }

    private void startPlay(PhotoSource photoSource) {
        mPhotoMovie = PhotoMovieFactory.generatePhotoMovie(photoSource, mMovieType);
        mPhotoMoviePlayer.setDataSource(mPhotoMovie);
        mPhotoMoviePlayer.prepare();
    }

    public void detachView() {
        mDemoView = null;
    }

    @Override
    public void onFilterSelect(FilterItem item) {
        mMovieRenderer.setMovieFilter(item.initFilter());
    }

    @Override
    public void onMovieUpdate(int elapsedTime) {

    }

    @Override
    public void onMovieStarted() {

    }

    @Override
    public void onMoviedPaused() {

    }

    @Override
    public void onMovieResumed() {

    }

    @Override
    public void onMovieEnd() {

    }

    @Override
    public void onTransferSelect(TransferItem item) {
        mMovieType = item.type;
        mPhotoMoviePlayer.stop();
        mPhotoMovie = PhotoMovieFactory.generatePhotoMovie(mPhotoMovie.getPhotoSource(), mMovieType);
        mPhotoMoviePlayer.setDataSource(mPhotoMovie);
        if (mMusicUri != null) {
            mPhotoMoviePlayer.setMusic(mDemoView.getActivity(), mMusicUri);
        }
        mPhotoMoviePlayer.setOnPreparedListener(new PhotoMoviePlayer.OnPreparedListener() {
            @Override
            public void onPreparing(PhotoMoviePlayer moviePlayer, float progress) {
            }

            @Override
            public void onPrepared(PhotoMoviePlayer moviePlayer, int prepared, int total) {
                mDemoView.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPhotoMoviePlayer.start();
                    }
                });
            }

            @Override
            public void onError(PhotoMoviePlayer moviePlayer) {
                MLog.i("onPrepare", "onPrepare error");
            }
        });
        mPhotoMoviePlayer.prepare();
    }

    public void setMusic(Uri uri) {
        mMusicUri = uri;
        mPhotoMoviePlayer.setMusic(mDemoView.getActivity(), uri);
    }

    public void saveVideo() {
        mPhotoMoviePlayer.pause();
        final ProgressDialog dialog = new ProgressDialog(mDemoView.getActivity());
        dialog.setMessage("saving video...");
        dialog.setMax(100);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setCancelable(false);
        dialog.show();
        final long startRecodTime = System.currentTimeMillis();
        final GLMovieRecorder recorder = new GLMovieRecorder(mDemoView.getActivity());
        final File file = initVideoFile();
        GLTextureView glTextureView = mDemoView.getGLView();
        int bitrate = glTextureView.getWidth() * glTextureView.getHeight() > 1000 * 1500 ? 8000000 : 4000000;
        recorder.configOutput(glTextureView.getWidth(), glTextureView.getHeight(), bitrate, 30, 1, file.getAbsolutePath());
        //生成一个全新的MovieRender，不然与现有的GL环境不一致，相互干扰容易出问题
        PhotoMovie newPhotoMovie = PhotoMovieFactory.generatePhotoMovie(mPhotoMovie.getPhotoSource(), mMovieType);
        GLSurfaceMovieRenderer newMovieRenderer = new GLSurfaceMovieRenderer(mMovieRenderer);
        newMovieRenderer.setPhotoMovie(newPhotoMovie);
        String audioPath = null;
        if(mMusicUri!=null) {
            audioPath = UriUtil.getPath(mDemoView.getActivity(), mMusicUri);
        }
        if (!TextUtils.isEmpty(audioPath)) {
            if (Build.VERSION.SDK_INT < 18) {
                Toast.makeText(mDemoView.getActivity().getApplicationContext(), "Mix audio needs api18!", Toast.LENGTH_LONG).show();
            } else {
                recorder.setMusic(audioPath);
            }
        }
        recorder.setDataSource(newMovieRenderer);
        recorder.startRecord(new GLMovieRecorder.OnRecordListener() {
            @Override
            public void onRecordFinish(boolean success) {
                File outputFile = file;
                long recordEndTime = System.currentTimeMillis();
                MLog.i("Record", "record:" + (recordEndTime - startRecodTime));
                dialog.dismiss();
                if (success) {
                    Toast.makeText(mDemoView.getActivity().getApplicationContext(), "Video save to path:" + outputFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    
                    mDemoView.getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));

                    
                    Intent intent = new Intent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setAction(Intent.ACTION_VIEW);
                    String type = "video/*";
                    intent.setDataAndType(Uri.fromFile(outputFile), type);
                    mDemoView.getActivity().startActivity(intent);
                } else {
                    Toast.makeText(mDemoView.getActivity().getApplicationContext(), "com.hw.photomovie.record error!", Toast.LENGTH_LONG).show();
                }
                if(recorder.getAudioRecordException()!=null){
                    Toast.makeText(mDemoView.getActivity().getApplicationContext(), "record audio failed:"+recorder.getAudioRecordException().toString(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onRecordProgress(int recordedDuration, int totalDuration) {
                dialog.setProgress((int) (recordedDuration / (float) totalDuration * 100));
            }
        });
    }

    private File initVideoFile() {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!dir.exists()) {
            dir = mDemoView.getActivity().getCacheDir();
        }
        return new File(dir, String.format("photo_movie_%s.mp4",
                new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(System.currentTimeMillis())));
    }

    public void onPause() {
        mPhotoMoviePlayer.pause();
    }

    public void onResume() {
        mPhotoMoviePlayer.start();
    }

    public void onPhotoPick(ArrayList<String> photos) {
        List<PhotoData> photoDataList = new ArrayList<PhotoData>(photos.size());
        for (String path : photos) {
            PhotoData photoData = new SimplePhotoData(mDemoView.getActivity(), path, PhotoData.STATE_LOCAL);
            photoDataList.add(photoData);
        }
        PhotoSource photoSource = new PhotoSource(photoDataList);
        if (mPhotoMoviePlayer == null) {
            startPlay(photoSource);
        } else {
            mPhotoMoviePlayer.stop();
            mPhotoMovie = PhotoMovieFactory.generatePhotoMovie(photoSource, PhotoMovieFactory.PhotoMovieType.HORIZONTAL_TRANS);
            mPhotoMoviePlayer.setDataSource(mPhotoMovie);
            if (mMusicUri != null) {
                mPhotoMoviePlayer.setMusic(mDemoView.getActivity(), mMusicUri);
            }
            mPhotoMoviePlayer.setOnPreparedListener(new PhotoMoviePlayer.OnPreparedListener() {
                @Override
                public void onPreparing(PhotoMoviePlayer moviePlayer, float progress) {
                }

                @Override
                public void onPrepared(PhotoMoviePlayer moviePlayer, int prepared, int total) {
                    mDemoView.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mPhotoMoviePlayer.start();
                        }
                    });
                }

                @Override
                public void onError(PhotoMoviePlayer moviePlayer) {
                    MLog.i("onPrepare", "onPrepare error");
                }
            });
            mPhotoMoviePlayer.prepare();
        }
    }
}
