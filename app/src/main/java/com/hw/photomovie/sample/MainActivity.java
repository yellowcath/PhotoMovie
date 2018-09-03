package com.hw.photomovie.sample;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.hw.photomovie.PhotoMovie;
import com.hw.photomovie.PhotoMovieFactory;
import com.hw.photomovie.PhotoMoviePlayer;
import com.hw.photomovie.dynamic.DynamicLoader;
import com.hw.photomovie.model.PhotoInfo;
import com.hw.photomovie.render.GLMovieRenderer;
import com.hw.photomovie.model.PhotoData;
import com.hw.photomovie.model.PhotoSource;
import com.hw.photomovie.model.UilPhotoData;
import com.hw.photomovie.sample.activityAnim.AnimActivity;
import com.hw.photomovie.segment.MovieSegment;
import com.hw.photomovie.timer.IMovieTimer;
import com.hw.photomovie.util.AppResources;
import com.hw.photomovie.util.MLog;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import record.GLMovieRecorder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends Activity implements IMovieTimer.MovieListener, AdapterView.OnItemSelectedListener {

    private PhotoMoviePlayer photoMoviePlayer;
    private Button mButton;
    private GLSurfaceView glSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        final ViewGroup glContainer = (ViewGroup) findViewById(R.id.gl_container);
        glSurfaceView = new GLSurfaceView(this);
        glContainer.addView(glSurfaceView);
//        DynamicLoader.loadSegmentsFromFile(this,"/mnt/sdcard2/pm.jar","com.hw.photomovietest.app.plugin.PluginSegment");
        final PhotoMovie photoMovie = PhotoMovieFactory.generatePhotoMovie(genPhotoSource(), PhotoMovieFactory.PhotoMovieType.THAW);
        final GLMovieRenderer glMovieRenderer = new GLMovieRenderer(glSurfaceView);
        photoMoviePlayer = new PhotoMoviePlayer();
        photoMoviePlayer.setMovieRenderer(glMovieRenderer);
        photoMoviePlayer.setMusic(getResources().openRawResourceFd(R.raw.bg));
        photoMoviePlayer.setDataSource(photoMovie);
        photoMoviePlayer.setMovieListener(this);
        photoMoviePlayer.setOnPreparedListener(new PhotoMoviePlayer.OnPreparedListener() {
            @Override
            public void onPreparing(PhotoMoviePlayer moviePlayer, float progress) {
                MLog.i("onPrepare", "" + progress);
                mButton.setText("prepare progress:" + progress);
            }

            @Override
            public void onPrepared(PhotoMoviePlayer moviePlayer, int prepared, int total) {
                MLog.i("onPrepare", "prepared:" + prepared + " total:" + total);
                mButton.setText("start");
                photoMoviePlayer.seekTo(0);
            }

            @Override
            public void onError(PhotoMoviePlayer moviePlayer) {
                MLog.i("onPrepare", "onPrepare error");
            }
        });

        mButton = (Button) findViewById(R.id.bt);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (photoMoviePlayer.getState()) {
                    case PhotoMoviePlayer.STATE_PREPARED:
                    case PhotoMoviePlayer.STATE_PAUSED:
                    case PhotoMoviePlayer.STATE_PLAYBACK_COMPLETED:
                        photoMoviePlayer.start();
                        break;
                    case PhotoMoviePlayer.STATE_PLAYING:
                        photoMoviePlayer.pause();
                        break;
                }
            }
        });

        Button recordBtn = (Button) findViewById(R.id.record);
        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoMoviePlayer.pause();

                GLMovieRecorder recorder = new GLMovieRecorder();
                final File file = new File(Environment.getExternalStorageDirectory(), "photoMovie.mp4");
                recorder.configOutput(glSurfaceView.getWidth(), glSurfaceView.getHeight(), 2000000, 30, 10, file.getAbsolutePath());
                recorder.setDataSource(glMovieRenderer);
                recorder.startRecord();

                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(Intent.ACTION_VIEW);
                String type = "video/avc";
                intent.setDataAndType(Uri.fromFile(file), type);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onPause() {
        photoMoviePlayer.pause();
        glSurfaceView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        glSurfaceView.onResume();
        super.onResume();
    }

    private void init() {
        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(this));
        AppResources.getInstance().init(getResources());

        MySpinnerAdapter movieAdapter = new MySpinnerAdapter(Arrays.asList(
                new String[]{"Thaw","Scale","ScaleTrans-subtitle","Window","Test"}));
        List<String> movieList = new ArrayList<String>();
        for (int i = 0; i < PhotoMovieFactory.PhotoMovieType.values().length; i++) {
            PhotoMovieFactory.PhotoMovieType photoMovieType = PhotoMovieFactory.PhotoMovieType.values()[i];
            movieList.add(photoMovieType.name());
        }
        movieList.add("activity anim");
        movieList.add("window(DynamicLoad)");
        Spinner movieSpinner = (Spinner) findViewById(R.id.spinner_movie);
        movieSpinner.setAdapter(movieAdapter);
        movieSpinner.setOnItemSelectedListener(this);
    }

    public static PhotoSource genPhotoSource() {
        List<PhotoData> dataList = new ArrayList<PhotoData>();
        {
            PhotoInfo photoInfo = new PhotoInfo();
            photoInfo.description = "啦啦啦啦啦";
            PhotoData photoData1 = new UilPhotoData("drawable://"+R.drawable.p3, PhotoData.STATE_LOCAL);
            photoData1.setPhotoInfo(photoInfo);
            dataList.add(photoData1);
        }
        {
            PhotoInfo photoInfo = new PhotoInfo();
            photoInfo.description = "啦啦";
            PhotoData photoData1 = new UilPhotoData("drawable://"+R.drawable.p2, PhotoData.STATE_LOCAL);
            photoData1.setPhotoInfo(photoInfo);
            dataList.add(photoData1);
        }
        {
            PhotoInfo photoInfo = new PhotoInfo();
            photoInfo.description = "啦啦啦啦啦阿萨德爱上爱上大声大声道阿萨德阿萨德爱上大师大声道啊";
            PhotoData photoData1 = new UilPhotoData("drawable://"+R.drawable.p1, PhotoData.STATE_LOCAL);
            photoData1.setPhotoInfo(photoInfo);
            dataList.add(photoData1);
        }
        dataList.add(new UilPhotoData("drawable://"+R.drawable.p4, PhotoData.STATE_LOCAL));
        dataList.add(new UilPhotoData("drawable://"+R.drawable.p5, PhotoData.STATE_LOCAL));
        PhotoSource source = new PhotoSource(dataList);
        return source;
    }


    @Override
    public void onMovieUpdate(int elapsedTime) {

    }

    @Override
    public void onMovieStarted() {
        mButton.setText("pause");

    }

    @Override
    public void onMoviedPaused() {
        mButton.setText("start");
    }

    @Override
    public void onMovieResumed() {
        mButton.setText("pause");

    }

    @Override
    public void onMovieEnd() {
        mButton.setText("start");
    }

    private File copyRawToFile(AssetFileDescriptor assetFileDescriptor){
        try {
            FileChannel fileChannel = assetFileDescriptor.createInputStream().getChannel();
            File cache = new File(getCacheDir(),"dynamic.jar");
            cache.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(cache);
            fileChannel.transferTo(assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength(), outputStream.getChannel());
            return cache;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void startMovie(int id){
        PhotoMovie photoMovie = null;
        final int typeLen = PhotoMovieFactory.PhotoMovieType.values().length;
        if(id== typeLen){
            startActivity(new Intent(this,AnimActivity.class));
            return;
        } else if(id == typeLen+1){
            AssetFileDescriptor assetFileDescriptor = getResources().openRawResourceFd(R.raw.dynamic_window_segment);
            File jarFile = copyRawToFile(assetFileDescriptor);
            if(jarFile==null){
                Toast.makeText(this,"拷贝jar文件出错",Toast.LENGTH_SHORT).show();
            } else{
                List<MovieSegment>  movieSegments = DynamicLoader.loadSegmentsFromFile(
                        this,
                        jarFile.getAbsolutePath(),
                        "com.hw.dynamicdemo.DynamicWindowSegment");
                photoMovie = new PhotoMovie(genPhotoSource(),movieSegments);
            }
        } else{
            PhotoMovieFactory.PhotoMovieType photoMovieType = PhotoMovieFactory.PhotoMovieType.values()[id];
            photoMovie = PhotoMovieFactory.generatePhotoMovie(genPhotoSource(), photoMovieType);
        }

        photoMoviePlayer.stop();

        photoMoviePlayer.setDataSource(photoMovie);
        photoMoviePlayer.setMusic(getResources().openRawResourceFd(R.raw.bg));
        photoMoviePlayer.setOnPreparedListener(new PhotoMoviePlayer.OnPreparedListener() {
            @Override
            public void onPreparing(PhotoMoviePlayer moviePlayer, float progress) {
                MLog.i("onPrepare", "" + progress);
                mButton.setText("prepare progress:"+progress);
            }

            @Override
            public void onPrepared(PhotoMoviePlayer moviePlayer, int prepared, int total) {
                MLog.i("onPrepare", "prepared:" + prepared + " total:" + total);
                mButton.setText("start");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        photoMoviePlayer.start();
                    }
                });
            }

            @Override
            public void onError(PhotoMoviePlayer moviePlayer) {
                MLog.i("onPrepare", "onPrepare error");
            }
        });
        photoMoviePlayer.prepare();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(parent.getId() == R.id.spinner_movie){
            startMovie(position);
        } else{

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private class MySpinnerAdapter extends BaseAdapter implements SpinnerAdapter {

        private List<String> list;
        private int padding;
        public MySpinnerAdapter(List<String> stringList){
            this.list = stringList;
            padding = (int) (getResources().getDisplayMetrics().density*4);
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {

        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {

        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public String getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv;
            if(convertView==null){
                convertView = new TextView(parent.getContext());
                tv = (TextView) convertView;
                tv.setPadding(padding,padding,padding,padding);
                tv.setGravity(Gravity.CENTER);
            }
            tv = (TextView) convertView;
            tv.setText(getItem(position));
            return tv;
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getView(position,convertView,parent);
        }
    }
}


