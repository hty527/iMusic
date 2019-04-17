package com.android.imusic.music.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.imusic.R;

/**
 * TinyHung@Outlook.com
 * 2018/3/18
 * 通用的加载中，数据为空、加载失败、刷新重试 控件
 */

public class MusicEmptyView extends RelativeLayout implements View.OnClickListener {

    private ImageView mImageView;
    private TextView mTextView;
    private View mContentView;

    public MusicEmptyView(Context context) {
        super(context);
        init(context);
    }

    public MusicEmptyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        View.inflate(context, R.layout.music_view_list_empty, this);
        mImageView = findViewById(R.id.iv_view_icon);
        mTextView = findViewById(R.id.tv_view_content);
        this.setOnClickListener(this);
        this.setClickable(false);
        showEmptyView();
    }

    /**
     * 加载中状态
     */
    public void showLoadingView(){
        this.setVisibility(View.VISIBLE);
        this.setClickable(false);
        if(null!=mImageView) mImageView.setImageResource(0);
        if(null!=mTextView) mTextView.setText("");
    }

    /**
     * 数据为空状态
     * @param content  要显示的文本
     * @param srcResID icon
     */
    public void showEmptyView(String content, int srcResID) {
        showEmptyState(content, null, srcResID);
    }

    public void showEmptyView(int content, int srcResID) {
        showEmptyState(getContext().getResources().getString(content), null, srcResID);
    }

    public void showEmptyView(boolean flag) {
        showEmptyState("暂无数据", null, R.drawable.ic_music_empty_default_img);
    }

    public void showEmptyView() {
        showEmptyState("暂无数据", null, R.drawable.ic_music_empty_default_img);
    }

    public void showEmptyState(String content, String desc, int srcResID) {
        this.setClickable(false);
        if (null != mTextView) mTextView.setText(content);
        if (null != mImageView) {
            if (0 != srcResID) {
                mImageView.setImageResource(srcResID);
            } else {
                mImageView.setImageResource(R.drawable.ic_music_empty_default_img);
            }
        }
    }

    /**
     * 重置所有状态
     */
    public void reset(){
        if(null!=mImageView) mImageView.setImageResource(0);
        if(null!=mTextView) mTextView.setText("");
    }

    /**
     * 加载失败状态
     * @param content  要显示的文本
     * @param srcResID icon
     */
    public void showErrorView(String content, int srcResID) {
        showErrorState(content, srcResID);
    }

    public void showErrorView(int content, int srcResID) {
        showErrorState(getContext().getResources().getString(content), srcResID);
    }

    public void showErrorView(String content) {
        showErrorState(content, R.drawable.ic_music_empty_error);
    }

    public void showErrorView() {
        showErrorState("加载失败，轻触重试", R.drawable.ic_music_empty_error);
    }

    public void showErrorState(String content, int srcResID) {
        if (null != mTextView) mTextView.setText(content);
        if (null != mImageView) {
            if (0 != srcResID) {
                mImageView.setImageResource(srcResID);
            } else {
                mImageView.setImageResource(R.drawable.ic_music_empty_error);
            }
        }
        this.setClickable(true);
    }

    /**
     * 分离界面应用
     * @param contentView
     * @param msg
     */
    public void showLoading(View contentView, String msg) {
        this.mContentView=contentView;
        if(null!=mContentView) contentView.setVisibility(GONE);
        showLoadingView();
    }

    public void hide() {
        reset();
        if(null!=mContentView) mContentView.setVisibility(VISIBLE);
    }

    public void showNoData() {
        if(null!=mContentView) mContentView.setVisibility(GONE);
        showEmptyView();
    }

    public void showNoNet(OnRefreshListener onRefreshListener) {
        this.mOnRefreshListener=onRefreshListener;
        showErrorView();
    }

    @Override
    public void onClick(View v) {
        if (null != mOnRefreshListener) {
            mOnRefreshListener.onRefresh();
        }
    }

    public interface OnFuctionListener{
        void onSubmit();
    }

    private OnFuctionListener mOnFuctionListener;

    public void setOnFuctionListener(OnFuctionListener onFuctionListener) {
        mOnFuctionListener = onFuctionListener;
    }

    public interface OnRefreshListener {
        void onRefresh();
    }

    private OnRefreshListener mOnRefreshListener;

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        mOnRefreshListener = onRefreshListener;
    }

    public void onDestroy() {
        reset();
        mTextView = null;mImageView = null;mOnRefreshListener = null;
    }
}