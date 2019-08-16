package com.android.imusic.music.dialog;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.app.AppCompatDialog;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.imusic.R;

/**
 * TinyHung@outlook.com
 * 2017/3/25 15:16
 * 加载进度条
 */

public class MusicLoadingView extends AppCompatDialog {

    private AnimationDrawable mAnimationDrawable;
    private final ImageView mLoadingIcon;
    private final TextView mTextContent;

    public MusicLoadingView(Context context) {
        super(context, R.style.CenterDialogAnimationStyle);
        setContentView(R.layout.music_dialog_progress_layout);
        mLoadingIcon = (ImageView) findViewById(R.id.music_loading_icon);
        mTextContent = (TextView) findViewById(R.id.music_content);
        mAnimationDrawable = (AnimationDrawable) mLoadingIcon.getDrawable();
    }

    @Override
    public void show() {
        super.show();
        if (null != mAnimationDrawable) {
            mLoadingIcon.setVisibility(View.VISIBLE);
            if (null != mAnimationDrawable && !mAnimationDrawable.isRunning())
                mAnimationDrawable.start();
        }
    }

    public void showMessage(String message) {
        super.show();
        if (null != mTextContent) {
            mTextContent.setText(message);
            mLoadingIcon.setVisibility(View.VISIBLE);
            if (null != mAnimationDrawable && !mAnimationDrawable.isRunning())
                mAnimationDrawable.start();
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (null != mAnimationDrawable && mAnimationDrawable.isRunning()) mAnimationDrawable.stop();
        mAnimationDrawable = null;
    }

    public interface OnDialogBackListener {
        boolean isBack();
    }

    private OnDialogBackListener mOnDialogBackListener;

    public void setOnDialogBackListener(OnDialogBackListener onDialogBackListener) {
        mOnDialogBackListener = onDialogBackListener;
    }

    /**
     * 设置Load文字
     *
     * @param message
     */
    public void setMessage(String message) {
        if(null!=mTextContent) mTextContent.setText(message);
    }

    /**
     * 将用户按下返回键时间传递出去
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mOnDialogBackListener != null&&!mOnDialogBackListener.isBack()) {
                return false;
            }
            return super.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }
}