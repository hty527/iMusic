package com.android.imusic.base;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import com.android.imusic.R;
import com.android.imusic.music.activity.MusicPlayerActivity;
import com.android.imusic.music.bean.AudioInfo;
import com.android.imusic.music.bean.MusicDetails;
import com.android.imusic.music.bean.MusicParams;
import com.android.imusic.music.dialog.MusicLoadingView;
import com.android.imusic.music.dialog.QuireDialog;
import com.google.gson.Gson;
import com.music.player.lib.bean.BaseAudioInfo;
import com.music.player.lib.bean.MusicStatus;
import com.music.player.lib.constants.MusicConstants;
import com.music.player.lib.manager.MusicPlayerManager;
import com.music.player.lib.manager.MusicWindowManager;
import com.music.player.lib.util.MusicUtils;
import java.util.List;

/**
 * hty_Yuye@Outlook.com
 * 2019/3/22
 */

public abstract class BaseActivity<P extends BasePresenter> extends AppCompatActivity
        implements BaseContract.BaseView {

    protected static final String TAG = "BaseActivity";
    protected P mPresenter;
    //权限处理
    private final static int READ_EXTERNAL_STORAGE_CODE = 100;//SD卡
    private final static int WRITE_EXTERNAL_STORAGE_CODE = 101;//SD卡
    protected final static int SETTING_REQUST = 123;
    protected static final int PREMISSION_CANCEL=0;//权限被取消申请
    protected static final int PREMISSION_SUCCESS=1;//权限申请成功
    protected MusicLoadingView mLoadingView;
    private boolean isTransparent=true;//is full transparent

    public void setTransparent(boolean transparent) {
        isTransparent = transparent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isTransparent&&Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS |
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
        //初始化Presenter
        mPresenter=createPresenter();
        if(null!=mPresenter){
            mPresenter.attachView(this);
        }
    }

    /**
     * 交由子类实现自己指定的Presenter,可以为空
     * @return 子类持有的继承自BasePresenter的Presenter
     */
    protected abstract P createPresenter();


    @Override
    public void showLoading() {}

    @Override
    public void showError(int code, String errorMsg) {}

    //============================================权限处理===========================================
    /**
     * 向用户申请的权限列表
     */
    protected static PermissionModel[] models;

    /**
     * 运行时权限
     */
    protected void requstPermissions() {
        if(Build.VERSION.SDK_INT < 23){
            onRequstPermissionResult(PREMISSION_SUCCESS);
            return;
        }
        if(null==models) models=new PermissionModel[]{
                new PermissionModel(Manifest.permission.READ_EXTERNAL_STORAGE,
                        getString(R.string.text_per_storage_read), READ_EXTERNAL_STORAGE_CODE),
                new PermissionModel(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        getString(R.string.text_per_storage_write), WRITE_EXTERNAL_STORAGE_CODE)
        };
        try {
            for (PermissionModel model : models) {
                if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, model.permission)) {
                    ActivityCompat.requestPermissions(this, new String[]{model.permission}, model.requestCode);
                    return;
                }
            }
            // 到这里就表示所有需要的权限已经通过申
            onRequstPermissionResult(PREMISSION_SUCCESS);
        } catch (Throwable e) {
        }
    }

    /**
     * 运行时权限
     */
    protected void requstPermissions(PermissionModel[] permissionModels) {
        if(Build.VERSION.SDK_INT < 23){
            onRequstPermissionResult(PREMISSION_SUCCESS);
            return;
        }
        models=permissionModels;
        try {
            for (PermissionModel model : models) {
                if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, model.permission)) {
                    ActivityCompat.requestPermissions(this, new String[]{model.permission}, model.requestCode);
                    return;
                }
            }
            // 到这里就表示所有需要的权限已经通过申
            onRequstPermissionResult(PREMISSION_SUCCESS);
        } catch (Throwable e) {
        }
    }

    /**
     * 请求权限成功
     * @param resultCode 1：已授予 0：未授予
     */
    protected void onRequstPermissionResult(int resultCode) {}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (READ_EXTERNAL_STORAGE_CODE == requestCode) {
            if (isAllRequestedPermissionGranted()) {
                onRequstPermissionResult(PREMISSION_SUCCESS);
            } else {
                requstPermissions();
            }
        }
        requstCode=requestCode;
    }

    /**
     * 申请结果回调
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_CODE:
            case WRITE_EXTERNAL_STORAGE_CODE:
                if(null!=grantResults&&grantResults.length>0){
                    if (PackageManager.PERMISSION_GRANTED != grantResults[0]) {
                        //用户拒绝过其中一个权限
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                            new android.support.v7.app.AlertDialog.Builder(BaseActivity.this)
                                    .setTitle(getString(R.string.text_per_read_song_error))
                                    .setMessage(findPermissionExplain(permissions[0]))
                                    .setNegativeButton(getString(R.string.text_cancel), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            onRequstPermissionResult(PREMISSION_CANCEL);
                                        }
                                    })
                                    .setPositiveButton(getString(R.string.text_submit), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            requstPermissions();
                                        }
                                    }).setCancelable(false).show();
                        } else {
                            //用户勾选了不再询问，手动开启
                            new android.support.v7.app.AlertDialog.Builder(BaseActivity.this)
                                    .setTitle(getString(R.string.text_per_read_song_error))
                                    .setMessage(getString(R.string.text_per_read_song_pre_error))
                                    .setNegativeButton(getString(R.string.text_cancel), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            onRequstPermissionResult(PREMISSION_CANCEL);
                                        }
                                    })
                                    .setPositiveButton(getString(R.string.text_start_setting), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            startActivityForResult(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                                    .setData(Uri.fromParts("package", getPackageName(), null)), SETTING_REQUST);
                                        }
                                    }).setCancelable(false).show();
                        }
                        return;
                    }
                    // 到这里就表示用户允许了本次请求，继续检查是否还有待申请的权限没有申请
                    if (isAllRequestedPermissionGranted()) {
                        onRequstPermissionResult(PREMISSION_SUCCESS);
                    } else {
                        requstPermissions();
                    }
                }
                break;
        }
    }

    protected String findPermissionExplain(String permission) {
        if (null!=models) {
            for (PermissionModel model : models) {
                if (model != null && model.permission != null && model.permission.equals(permission)) {
                    return model.explain;
                }
            }
        }
        return null;
    }

    protected boolean isAllRequestedPermissionGranted() {
        if(null!=models){
            for (PermissionModel model : models) {
                if (PackageManager.PERMISSION_GRANTED !=
                        ContextCompat.checkSelfPermission(this, model.permission)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static class PermissionModel {
        public String permission;
        public String explain;
        public int requestCode;

        public PermissionModel(String permission, String explain, int requestCode) {
            this.permission = permission;
            this.explain = explain;
            this.requestCode = requestCode;
        }
    }

    //============================================悬浮窗=============================================

    private int requstCode=0;
    //悬浮窗是否开启
    private boolean isWindowEnable=false;

    /**
     * 子类调用开启悬浮窗逻辑
     * @param windowEnable
     */
    public void setWindowEnable(boolean windowEnable) {
        isWindowEnable = windowEnable;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isWindowEnable&&requstCode>0&&requstCode== MusicConstants.REQUST_WINDOWN_PERMISSION&&
                MusicWindowManager.getInstance().checkAlertWindowsPermission(BaseActivity.this)){
            requstCode=0;
            createMiniJukeBoxToWindown();
        }
    }

    /**
     * 即将退出播放器
     */
    protected void createMiniJukeboxWindow() {
        if(!MusicWindowManager.getInstance().checkAlertWindowsPermission(BaseActivity.this)){
            QuireDialog.getInstance(BaseActivity.this)
                    .setTitleText(getString(R.string.text_music_play_tips))
                    .setContentText(getString(R.string.text_music_play_window_tips))
                    .setSubmitTitleText(getString(R.string.text_start_open))
                    .setCancelTitleText(getString(R.string.text_music_play_no_open))
                    .setTopImageRes(R.drawable.ic_setting_tips1)
                    .setBtnClickDismiss(false)
                    .setDialogCancelable(false)
                    .setOnQueraConsentListener(new QuireDialog.OnQueraConsentListener() {
                        @Override
                        public void onConsent(QuireDialog dialog) {
                            dialog.dismiss();
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.setData(Uri.parse( "package:"+MusicUtils.getInstance()
                                        .getPackageName(BaseActivity.this)));
                                BaseActivity.this.startActivityForResult(intent,MusicConstants.REQUST_WINDOWN_PERMISSION);
                            } else {
                                Toast.makeText(BaseActivity.this,getString(R.string.text_music_active_open),Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent();
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                                intent.setData(Uri.fromParts("package", getPackageName(), null));
                                startActivityForResult(intent,MusicConstants.REQUST_WINDOWN_PERMISSION);
                            }
                        }

                        @Override
                        public void onRefuse(QuireDialog dialog) {
                            dialog.dismiss();
                            MusicPlayerManager.getInstance().onStop();
                            finish();
                        }
                    }).show();
            return;
        }
        createMiniJukeBoxToWindown();
    }

    /**
     * 创建一个全局的迷你唱片至窗口
     */
    private void createMiniJukeBoxToWindown() {
        if(!MusicWindowManager.getInstance().isWindowShowing()){
            if(null!= MusicPlayerManager.getInstance().getCurrentPlayerMusic()){
                BaseAudioInfo musicInfo = MusicPlayerManager.getInstance().getCurrentPlayerMusic();
                MusicWindowManager.getInstance().createMiniJukeBoxToWindown(getApplicationContext());
                MusicStatus musicStatus=new MusicStatus();
                musicStatus.setId(musicInfo.getAudioId());
                String frontPath=MusicUtils.getInstance().getMusicFrontPath(musicInfo);
                musicStatus.setCover(frontPath);
                musicStatus.setTitle(musicInfo.getAudioName());
                int playerState = MusicPlayerManager.getInstance().getPlayerState();
                boolean playing = playerState==MusicConstants.MUSIC_PLAYER_PLAYING
                        || playerState==MusicConstants.MUSIC_PLAYER_PREPARE
                        || playerState==MusicConstants.MUSIC_PLAYER_BUFFER;
                musicStatus.setPlayerStatus(playing?MusicStatus.PLAYER_STATUS_START:MusicStatus.PLAYER_STATUS_PAUSE);
                MusicWindowManager.getInstance().updateWindowStatus(musicStatus);
                MusicWindowManager.getInstance().onVisible();
            }
        }
    }

    /**
     * 显示加载中弹窗
     * @param message 提示MSG
     */
    public void showProgressDialog(String message){
        if(!BaseActivity.this.isFinishing()){
            if(null==mLoadingView){
                mLoadingView = new MusicLoadingView(this);
            }
            mLoadingView.setMessage(message);
            mLoadingView.show();
        }
    }

    /**
     * 关闭进度框
     */
    public void closeProgressDialog(){
        try {
            if(!BaseActivity.this.isFinishing()){
                if(null!=mLoadingView){
                    mLoadingView.dismiss();
                }
                mLoadingView=null;
            }
        }catch (Exception e){
        }
    }

    /**
     * 音乐列表菜单处理
     * @param itemId ITEM 类型
     * @param audioInfo 音频对象
     */
    protected void onMusicMenuClick(int position,int itemId, BaseAudioInfo audioInfo) {
        if(itemId== MusicDetails.ITEM_ID_NEXT_PLAY){
            MusicPlayerManager.getInstance().playNextMusic();
        }else if(itemId== MusicDetails.ITEM_ID_SHARE){
            try {
                if(!TextUtils.isEmpty(audioInfo.getAudioPath())){
                    if(audioInfo.getAudioPath().startsWith("http:")||audioInfo.getAudioPath().startsWith("https:")){
                        Intent sendIntent = new Intent();
                        //sendIntent.setPackage("com.tencent.mm")
                        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "iMusic分享");
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, "我正在使用"+getResources().getString(R.string.app_name)+
                                "听:《"+audioInfo.getAudioName()+"》，快来听吧~猛戳-->"+audioInfo.getAudioPath());
                        sendIntent.setType("text/plain");
                        startActivity(Intent.createChooser(sendIntent, "iMusic分享"));
                    }else{
                        Intent sendIntent = new Intent();
                        //sendIntent.setPackage("com.tencent.mm")
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, "来自iMusic的音乐分享:《"
                                +audioInfo.getAudioName()+"》-"+audioInfo.getNickname());
                        sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(audioInfo.getAudioPath()));
                        sendIntent.setType("audio/*");
                        startActivity(Intent.createChooser(sendIntent, "iMusic分享"));
                    }
                }else{
                    Toast.makeText(BaseActivity.this,"此歌曲已被下架",Toast.LENGTH_SHORT).show();
                }
            }catch (RuntimeException e){
                e.printStackTrace();
                Toast.makeText(BaseActivity.this,"分享失败："+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }else if(itemId==MusicDetails.ITEM_ID_COLLECT){
            boolean toCollect = MusicPlayerManager.getInstance().collectMusic(audioInfo);
            if(toCollect){
                Toast.makeText(BaseActivity.this,"已添加至收藏列表",Toast.LENGTH_SHORT).show();
                MusicPlayerManager.getInstance().observerUpdata(new MusicStatus());
            }
        }
    }

    /**
     * 打开播放器
     * @param musicID 正在播放的音频对象
     */
    protected void startToMusicPlayer(long musicID) {
        Intent intent=new Intent(getApplicationContext(), MusicPlayerActivity.class);
        intent.putExtra(MusicConstants.KEY_MUSIC_ID, musicID);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //overridePendingTransition( R.anim.music_bottom_menu_enter,0);
        getApplicationContext().startActivity(intent);
    }

    /**
     * 打开播放器
     * @param musicID 要播放的音频对象
     * @param audioInfos 要播放的音频队列对象
     */
    protected void startMusicPlayer(long musicID,List<AudioInfo> audioInfos){
        Intent intent=new Intent(getApplicationContext(), MusicPlayerActivity.class);
        MusicParams params=new MusicParams();
        params.setAudioInfos(audioInfos);
        String json = new Gson().toJson(params);
        intent.putExtra(MusicConstants.KEY_MUSIC_LIST,json);
        intent.putExtra(MusicConstants.KEY_MUSIC_ID, musicID);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(intent);
    }

    /**
     * 屏幕方向变化监听
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //转到横屏
        if(2==newConfig.orientation){
            MusicWindowManager.getInstance().onInvisible();
            //转到竖屏
        }else if(1==newConfig.orientation){
            MusicWindowManager.getInstance().onVisible();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null!=mPresenter){
            mPresenter.detachView();
            mPresenter=null;
        }
    }
}