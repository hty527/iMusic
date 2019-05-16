package com.android.imusic.music.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.android.imusic.R;
import com.android.imusic.base.BaseActivity;
import com.android.imusic.base.BasePresenter;
import com.android.imusic.music.bean.VersionInfo;
import com.android.imusic.music.manager.VersionUpdateManager;
import com.android.imusic.music.utils.FileUtils;
import com.android.imusic.music.view.ShapeTextView;
import com.android.imusic.net.OnDownloadListener;
import com.music.player.lib.util.Logger;
import com.music.player.lib.util.MusicUtils;
import java.io.File;

/**
 * Created by TinyHung@outlook.com
 * 2019/5/15
 * 版本更新，支持强制更新
 */

public class VersionUpdateActivity extends BaseActivity{

    private ShapeTextView mBtnNext,mBtnCancel;
    private ImageView mBtnClose;
    private ProgressBar mProgressBar;
    private View mDownloadView;
    private TextView mUpdateContent;
    private TextView mTvDownloadTips;
    private TextView mTvDownloadProgress;

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //默认点击外部禁止关闭
        setFinishOnTouchOutside(false);
        setContentView(R.layout.activity_version_update);
        initLayoutParams();
        initViews();
        getIntentParams(getIntent());
    }

    private void initViews() {
        mBtnCancel = (ShapeTextView) findViewById(R.id.btn_cancel);
        mBtnNext = (ShapeTextView) findViewById(R.id.btn_next);
        mBtnClose = (ImageView) findViewById(R.id.btn_close);
        mTvDownloadTips = (TextView) findViewById(R.id.tv_download_tips);
        mTvDownloadProgress = (TextView) findViewById(R.id.tv_download_progress);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_download_progress);
        mDownloadView = findViewById(R.id.ll_download_view);

        View.OnClickListener onClickListener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_cancel:
                    case R.id.btn_close:
                        finish();
                        break;
                    case R.id.btn_next:
                        if(null!=v.getTag()){
                            VersionInfo versionInfo= (VersionInfo) v.getTag();
                            startDownloadApk(versionInfo);
                        }
                        break;
                }
            }
        };
        mBtnCancel.setOnClickListener(onClickListener);
        mBtnClose.setOnClickListener(onClickListener);
        mBtnNext.setOnClickListener(onClickListener);
    }

    /**
     * 设置Dialog显示在屏幕中央
     */
    protected int initLayoutParams() {
        Window window = getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();//得到布局管理者
        WindowManager systemService = (WindowManager)getSystemService(android.content.Context.WINDOW_SERVICE);//得到窗口管理者
        DisplayMetrics displayMetrics=new DisplayMetrics();//创建设备屏幕的管理者
        systemService.getDefaultDisplay().getMetrics(displayMetrics);//得到屏幕的宽高
        int hight= LinearLayout.LayoutParams.WRAP_CONTENT;//取出布局的高度
        attributes.height= hight;
        //兼容低分辨率机型
        int screenDensity = MusicUtils.getInstance().getScreenDensity(VersionUpdateActivity.this);
        attributes.width= (systemService.getDefaultDisplay().getWidth()-(screenDensity>300?190:90));
        attributes.gravity= Gravity.CENTER;
        return attributes.width;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        getIntentParams(intent);
    }

    /**
     * 获取视频入参
     * @param intent
     */
    private void getIntentParams(Intent intent) {
        if(null==intent) return;
        VersionInfo versionInfo = intent.getParcelableExtra("versionInfo");
        if(null==versionInfo){
            Toast.makeText(VersionUpdateActivity.this,"参数错误，更新失败",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if(null==mBtnNext){
            Toast.makeText(VersionUpdateActivity.this,"内部错误，更新失败",Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        //必须在下载前设置
        VersionUpdateManager.getInstance().setOutPutFileName(FileUtils.getInstance().getFileName(versionInfo.getDown_url()));
        TextView title = (TextView) findViewById(R.id.tv_update_title);
        mUpdateContent = (TextView) findViewById(R.id.tv_update_content);
        mUpdateContent.setMovementMethod(ScrollingMovementMethod.getInstance());
        title.setText(String.format("发现新版本：%s",versionInfo.getVersion()));
        mTvDownloadProgress.setText(String.format("%sM/%sM","0",versionInfo.getSize()));
        mUpdateContent.setText(versionInfo.getUpdate_log());
        boolean existApk = VersionUpdateManager.getInstance().isEqualNewVersion(versionInfo.getVersion_code());
        if(existApk){
            mBtnCancel.setVisibility(View.GONE);
            mBtnNext.setText("已下载，点击安装");
        }else {
            mBtnNext.setText("立即更新");
        }
        mBtnCancel.setText("下次更新");
        mBtnNext.setTag(versionInfo);
        //是否强制更新
        if(versionInfo.getCompel_update()>0){
            mBtnCancel.setVisibility(View.GONE);
            mBtnClose.setVisibility(View.GONE);
            setFinishOnTouchOutside(false);
        }else{
            setFinishOnTouchOutside(true);
        }
    }

    /**
     * 开始下载
     * @param versionInfo 版本信息
     */
    private void startDownloadApk(VersionInfo versionInfo) {
        boolean existApk = VersionUpdateManager.getInstance().isEqualNewVersion(versionInfo.getVersion_code());
        Logger.d(TAG,"startDownloadApk-->existApk:"+existApk);
        if(existApk){
            mTvDownloadTips.setText("已下载");
            mBtnNext.setText("已下载，点击安装");
            VersionUpdateManager.getInstance().instanllApk(versionInfo.getDown_url());
            return;
        }
        mBtnCancel.setVisibility(View.GONE);
        mBtnClose.setVisibility(View.GONE);
        mDownloadView.setVisibility(View.VISIBLE);
        mUpdateContent.setVisibility(View.GONE);
        //开始下载后禁止点击外部关闭弹窗
        setFinishOnTouchOutside(false);
        mBtnNext.setEnabled(false);
        mBtnNext.setText("下载中，请稍后...");
        mTvDownloadTips.setText("下载中");
        VersionUpdateManager.getInstance().downloadAPK(versionInfo.getDown_url(), new OnDownloadListener() {
            @Override
            public void progress(int progress, final long totloLength, final long readLength) {
                Logger.d(TAG,"progress-->progress:"+progress+",totloLength:"+totloLength+",readLength:"+readLength);
                if(null!=mProgressBar){
                    mProgressBar.setProgress(progress);
                }
                if(null!=mTvDownloadProgress){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTvDownloadProgress.setText(FileUtils.getInstance().formatSizeToString(readLength)
                                    +"/"+FileUtils.getInstance().formatSizeToString(totloLength));
                        }
                    });
                }
            }

            @Override
            public void onSuccess(File file) {
                Logger.d(TAG,"onSuccess-->file:"+file.getAbsolutePath());
                if(null!=mTvDownloadTips){
                    mTvDownloadTips.setText("已下载");
                }
                if(null!=mBtnNext){
                    mBtnNext.setEnabled(true);
                    mBtnNext.setText("下载完成，点击安装");
                }
            }

            @Override
            public void onError(int errorCode, String message) {
                Logger.d(TAG,"onError-->errorCode:"+errorCode+",message:"+message);
                if(null!=mTvDownloadTips){
                    mTvDownloadTips.setText(message);
                }
                if(null!=mBtnNext){
                    mBtnNext.setEnabled(true);
                    mBtnNext.setText("下载失败，点击重试");
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        //拦截强制更新
        if(null!=mBtnNext&&null!=mBtnNext.getTag()){
            VersionInfo versionInfo= (VersionInfo) mBtnNext.getTag();
            if(versionInfo.getCompel_update()>0){
                return;
            }
            //下载中不允许关闭对话框
            if(mBtnNext.getText().equals("下载中，请稍后...")){
                return;
            }
            super.onBackPressed();
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VersionUpdateManager.getInstance().onDestroy();
    }
}