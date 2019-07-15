package com.android.imusic.music.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
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

    private static final int REQUEST_INSTALL_STATE = 1001;
    private static final int REQUEST_INSTALL_UNKNOW_SOURCE = 1002;
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
        //下载状态监听器设置
        VersionUpdateManager.getInstance().setDownloadListener(new OnDownloadListener() {
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
                    mTvDownloadTips.setText(getString(R.string.text_version_download_finlsh));
                }
                if(null!=mBtnNext){
                    mBtnNext.setEnabled(true);
                    mBtnNext.setText(getString(R.string.text_version_download_finlsh_instanl));
                }
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    if(getPackageManager().canRequestPackageInstalls()){
                        VersionUpdateManager.getInstance().instanllApk(file);
                    }else{
                        //申请安装外部APK权限
                        ActivityCompat.requestPermissions(VersionUpdateActivity.this, new String[]{Manifest.permission.REQUEST_INSTALL_PACKAGES}, REQUEST_INSTALL_STATE);
                    }
                }else{
                    VersionUpdateManager.getInstance().instanllApk(file);
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
                    mBtnNext.setText(getString(R.string.text_version_download_error));
                }
            }
        });
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
        title.setText(String.format(getString(R.string.text_version_find),versionInfo.getVersion()));
        mTvDownloadProgress.setText(String.format("%sM/%sM","0",versionInfo.getSize()));
        mUpdateContent.setText(versionInfo.getUpdate_log());
        boolean existApk = VersionUpdateManager.getInstance().isEqualNewVersion(versionInfo.getVersion_code());
        if(existApk){
            mBtnCancel.setVisibility(View.GONE);
            mBtnNext.setText(getString(R.string.text_version_instanll));
        }else {
            mBtnNext.setText(getString(R.string.text_version_now));
        }
        mBtnCancel.setText(getString(R.string.text_version_next));
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
        if(!VersionUpdateManager.getInstance().isExistEqualVersionApk(versionInfo.getVersion_code())){
            mBtnCancel.setVisibility(View.GONE);
            mBtnClose.setVisibility(View.GONE);
            mDownloadView.setVisibility(View.VISIBLE);
            mUpdateContent.setVisibility(View.GONE);
            //开始下载后禁止点击外部关闭弹窗
            setFinishOnTouchOutside(false);
            //非强制更新允许用户切换至后台安装
            mBtnNext.setEnabled(false);
            mBtnNext.setText(getString(R.string.text_version_download_loading));
            mTvDownloadTips.setText(getString(R.string.text_version_download_loading_tips));
            VersionUpdateManager.getInstance().downloadAPK(versionInfo.getDown_url());
        }
    }

    /**
     * 加个获取权限的监听
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode==REQUEST_INSTALL_STATE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if(null!=mBtnNext.getTag()){
                    VersionInfo versionInfo= (VersionInfo) mBtnNext.getTag();
                    VersionUpdateManager.getInstance().instanllApk(versionInfo.getDown_url());
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startInstallPermissionSettingActivity();
                }
            }
        }
    }

    /**
     * 注意这个是8.0新API
     * 进入设置界面 打开未知来源安装
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startInstallPermissionSettingActivity() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
        startActivityForResult(intent, REQUEST_INSTALL_UNKNOW_SOURCE);
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
            if(mBtnNext.getText().equals(getString(R.string.text_version_download_loading))){
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