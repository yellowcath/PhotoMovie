package com.hw.photomovie.sample;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import com.hw.photomovie.render.GLTextureView;
import com.hw.photomovie.sample.widget.FilterItem;
import com.hw.photomovie.sample.widget.MovieBottomView;
import com.hw.photomovie.sample.widget.MovieFilterView;
import com.hw.photomovie.sample.widget.MovieTransferView;
import com.hw.photomovie.sample.widget.TransferItem;
import com.hw.photomovie.util.AppResources;

import java.util.List;

/**
 * Created by huangwei on 2018/9/9.
 */
public class DemoActivity extends AppCompatActivity implements IDemoView, MovieBottomView.MovieBottomCallback {

    private static final int REQUEST_MUSIC = 234;
    private DemoPresenter mDemoPresenter = new DemoPresenter();
    private GLTextureView mGLTextureView;
    private MovieFilterView mFilterView;
    private MovieTransferView mTransferView;
    private MovieBottomView mBottomView;
    private List<FilterItem> mFilters;
    private List<TransferItem> mTransfers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppResources.getInstance().init(getResources());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        mGLTextureView = findViewById(R.id.gl_texture);
        mBottomView = findViewById(R.id.movie_bottom_layout);
        mDemoPresenter.attachView(this);
        mBottomView.setCallback(this);
    }

    @Override
    public GLTextureView getGLView() {
        return mGLTextureView;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDemoPresenter.detachView();
    }

    @Override
    public void onNextClick() {
      mDemoPresenter.saveVideo();
    }

    @Override
    public void onMusicClick() {
        Intent i = new Intent();
        i.setType("audio/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(i, REQUEST_MUSIC);
    }

    @Override
    public void onTransferClick() {
        if(mTransferView==null){
            ViewStub stub = findViewById(R.id.movie_menu_transfer_stub);
            mTransferView = (MovieTransferView) stub.inflate();
            mTransferView.setVisibility(View.GONE);
            mTransferView.setItemList(mTransfers);
            mTransferView.setTransferCallback(mDemoPresenter);
        }
        mBottomView.setVisibility(View.GONE);
        mTransferView.show();
    }

    @Override
    public void onFilterClick() {
       if(mFilterView==null){
           ViewStub stub = findViewById(R.id.movie_menu_filter_stub);
           mFilterView = (MovieFilterView) stub.inflate();
           mFilterView.setVisibility(View.GONE);
           mFilterView.setItemList(mFilters);
           mFilterView.setFilterCallback(mDemoPresenter);
       }
       mBottomView.setVisibility(View.GONE);
       mFilterView.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == REQUEST_MUSIC){
            Uri uri = data.getData();
            mDemoPresenter.setMusic(uri);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (mFilterView != null && mFilterView.getVisibility() == View.VISIBLE
                    && !checkInArea(mFilterView,ev)){
                mFilterView.hide();
                mBottomView.setVisibility(View.VISIBLE);
                return true;
            }else  if (mTransferView != null && mTransferView.getVisibility() == View.VISIBLE
                    && !checkInArea(mTransferView,ev)){
                mTransferView.hide();
                mBottomView.setVisibility(View.VISIBLE);
                return true;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private boolean checkInArea(View view,MotionEvent event){
        int loc[] = new int[2];
        view.getLocationInWindow(loc);
        return event.getRawY()>loc[1];
    }

    @Override
    public void setFilters(List<FilterItem> filters) {
        mFilters = filters;
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void setTransfers(List<TransferItem> items) {
        mTransfers = items;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDemoPresenter.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDemoPresenter.onResume();
    }
}
