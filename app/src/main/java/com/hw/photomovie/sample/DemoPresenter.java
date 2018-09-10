package com.hw.photomovie.sample;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;
import com.hw.photomovie.PhotoMovie;
import com.hw.photomovie.PhotoMovieFactory;
import com.hw.photomovie.PhotoMoviePlayer;
import com.hw.photomovie.model.PhotoData;
import com.hw.photomovie.model.PhotoInfo;
import com.hw.photomovie.model.PhotoSource;
import com.hw.photomovie.model.SimplePhotoData;
import com.hw.photomovie.render.GLSurfaceMovieRenderer;
import com.hw.photomovie.render.GLTextureMovieRender;
import com.hw.photomovie.render.GLTextureView;
import com.hw.photomovie.sample.widget.FilterItem;
import com.hw.photomovie.sample.widget.FilterType;
import com.hw.photomovie.sample.widget.MovieFilterView;
import com.hw.photomovie.sample.widget.MovieTransferView;
import com.hw.photomovie.sample.widget.TransferItem;
import com.hw.photomovie.timer.IMovieTimer;
import com.hw.photomovie.util.MLog;
import com.hw.videoprocessor.VideoProcessor;
import record.GLMovieRecorder;

import java.io.File;
import java.io.IOException;
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
        items.add(new TransferItem(R.drawable.ic_movie_transfer, "Thaw", PhotoMovieFactory.PhotoMovieType.THAW));
        items.add(new TransferItem(R.drawable.ic_movie_transfer, "Tranlation", PhotoMovieFactory.PhotoMovieType.SCALE_TRANS));
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

        mPhotoMovie = PhotoMovieFactory.generatePhotoMovie(genPhotoSource(mDemoView.getActivity()), PhotoMovieFactory.PhotoMovieType.HORIZONTAL_TRANS);
        mMovieRenderer = new GLTextureMovieRender(glTextureView);
        mPhotoMoviePlayer = new PhotoMoviePlayer(mDemoView.getActivity().getApplicationContext());
        mPhotoMoviePlayer.setMovieRenderer(mMovieRenderer);
        mPhotoMoviePlayer.setDataSource(mPhotoMovie);
        mPhotoMoviePlayer.setMovieListener(this);
        mPhotoMoviePlayer.setLoop(true);
        mPhotoMoviePlayer.setOnPreparedListener(new PhotoMoviePlayer.OnPreparedListener() {
            @Override
            public void onPreparing(PhotoMoviePlayer moviePlayer, float progress) {
            }

            @Override
            public void onPrepared(PhotoMoviePlayer moviePlayer, int prepared, int total) {
                mPhotoMoviePlayer.start();
            }

            @Override
            public void onError(PhotoMoviePlayer moviePlayer) {
                MLog.i("onPrepare", "onPrepare error");
            }
        });
        mPhotoMoviePlayer.prepare();
    }

    public static PhotoSource genPhotoSource(Context context) {
        List<PhotoData> dataList = new ArrayList<PhotoData>();
        {
            PhotoInfo photoInfo = new PhotoInfo();
            photoInfo.description = "字幕字幕";
            PhotoData photoData1 = new SimplePhotoData(context, "drawable://" + R.drawable.p1, PhotoData.STATE_LOCAL);
            photoData1.setPhotoInfo(photoInfo);
            dataList.add(photoData1);
        }
        {
            PhotoInfo photoInfo = new PhotoInfo();
            photoInfo.description = "字幕字幕";
            PhotoData photoData1 = new SimplePhotoData(context, "drawable://" + R.drawable.p2, PhotoData.STATE_LOCAL);
            photoData1.setPhotoInfo(photoInfo);
            dataList.add(photoData1);
        }
        {
            PhotoInfo photoInfo = new PhotoInfo();
            photoInfo.description = "字幕字幕字幕字幕字幕字幕字幕字幕";
            PhotoData photoData1 = new SimplePhotoData(context, "drawable://" + R.drawable.p3, PhotoData.STATE_LOCAL);
            photoData1.setPhotoInfo(photoInfo);
            dataList.add(photoData1);
        }
        dataList.add(new SimplePhotoData(context, "drawable://" + R.drawable.p4, PhotoData.STATE_LOCAL));
        dataList.add(new SimplePhotoData(context, "drawable://" + R.drawable.p5, PhotoData.STATE_LOCAL));
        PhotoSource source = new PhotoSource(dataList);
        return source;
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
        mPhotoMoviePlayer.stop();
        mPhotoMovie = PhotoMovieFactory.generatePhotoMovie(mPhotoMovie.getPhotoSource(), item.type);
        mPhotoMoviePlayer.setDataSource(mPhotoMovie);
        mPhotoMoviePlayer.setMusic(mDemoView.getActivity(), mMusicUri);
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
        final AlertDialog dialog = new ProgressDialog.Builder(mDemoView.getActivity())
                .setMessage("saving video...").create();
        dialog.show();
        GLMovieRecorder recorder = new GLMovieRecorder();
        final File file = initVideoFile();
        GLTextureView glTextureView = mDemoView.getGLView();
        int bitrate = glTextureView.getWidth() * glTextureView.getHeight() > 1000 * 1500 ? 8000000 : 4000000;
        recorder.configOutput(glTextureView.getWidth(), glTextureView.getHeight(), bitrate, 30, 1, file.getAbsolutePath());
        recorder.setDataSource(mMovieRenderer);
        recorder.startRecord(new GLMovieRecorder.onRecordListener() {
            @Override
            public void onRecordFinish(boolean success) {
                File outputFile = file;
                if (mMusicUri != null) {
                    if(Build.VERSION.SDK_INT<Build.VERSION_CODES.LOLLIPOP){
                        Toast.makeText(mDemoView.getActivity().getApplicationContext(), "Mix audio needs api21!", Toast.LENGTH_LONG).show();
                    }else {
                        //合成音乐
                        File mixFile = initVideoFile();
                        String audioPath = UriUtil.getPath(mDemoView.getActivity(), mMusicUri);
                        try {
                            VideoProcessor.mixAudioTrack(mDemoView.getActivity(), file.getAbsolutePath(), audioPath, mixFile.getAbsolutePath(), null, null, 0,
                                    100, 1f, 1f);
                            file.delete();
                            outputFile = mixFile;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                dialog.dismiss();
                if (success) {
                    Intent intent = new Intent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setAction(Intent.ACTION_VIEW);
                    String type = "video/avc";
                    intent.setDataAndType(Uri.fromFile(outputFile), type);
                    mDemoView.getActivity().startActivity(intent);
                } else {
                    Toast.makeText(mDemoView.getActivity().getApplicationContext(), "record error!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private File initVideoFile() {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), String.format("photo_movie_%s.mp4",
                new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(System.currentTimeMillis())));
    }

    private long getVideoDurationUs(String videoPath){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoPath);
        long duration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        return duration*1000;
    }
}
