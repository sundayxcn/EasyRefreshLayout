package com.sunday.views.assist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sunday.views.HeaderView;

import sunday.com.easyrefreshlayout.R;

/**
 * Created by zhongfei.sun on 2018/1/19.
 */

public class ClassicsHeaderView implements HeaderView {

    private Context mContext;
    private View mParent;

    private TextView mTextView;
    private ImageView mFinishView;

    public ClassicsHeaderView(Context context){
        mContext = context;
    }

    @Override
    public void begin() {
        reset();
    }

    @Override
    public void progress(float progress) {
        if(progress >= 1f){
            mTextView.setText("松开刷新");
        }else{
            mTextView.setText("下拉刷新");
        }
    }

    @Override
    public void loading() {
        mTextView.setText("刷新中");
    }

    @Override
    public void reset() {
        mFinishView.setVisibility(View.INVISIBLE);
        mTextView.setText("下拉刷新");
    }

    @Override
    public View getView() {
        if(mParent == null){
            mParent = LayoutInflater.from(mContext).inflate(R.layout.layout_head_classics,null,false);
            mTextView = (TextView) mParent.findViewById(R.id.text);
            mFinishView = (ImageView) mParent.findViewById(R.id.finish);
        }
        return mParent;
    }

    @Override
    public void showPause(boolean success) {
        mFinishView.setVisibility(View.VISIBLE);
        if(success) {
            mTextView.setText("刷新成功");
        }else{
            mTextView.setText("刷新失败");
        }
    }

    @Override
    public boolean isPauseTime() {
        return mFinishView.getVisibility() == View.VISIBLE;
    }

    @Override
    public long getPauseMillTime() {
        return 0;
    }
}
