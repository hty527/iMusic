package com.android.imusic.music.base;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.android.imusic.music.bean.MediaInfo;
import com.android.imusic.music.bean.MusicDetails;
import com.android.imusic.music.dialog.MusicLoadingView;
import com.android.imusic.music.net.MusicNetUtils;
import com.music.player.lib.bean.BaseMediaInfo;
import com.music.player.lib.bean.MusicStatus;
import com.music.player.lib.constants.MusicConstants;
import com.music.player.lib.manager.MusicPlayerManager;
import com.music.player.lib.manager.MusicWindowManager;
import com.music.player.lib.model.MusicPlayerState;
import com.music.player.lib.util.Logger;
import com.music.player.lib.util.MusicUtils;
import java.io.Serializable;
import java.util.List;

/**
 * hty_Yuye@Outlook.com
 * 2019/3/22
 */

public class MusicBaseActivity<P extends MusicNetUtils> extends AppCompatActivity{

    protected static final String TAG = "MusicBaseActivity";
    protected P mPresenter;
    //权限处理
    private final static int READ_EXTERNAL_STORAGE_CODE = 100;//SD卡
    private final static int WRITE_EXTERNAL_STORAGE_CODE = 101;//SD卡
    protected final static int SETTING_REQUST = 123;
    protected static final int PREMISSION_CANCEL=0;//权限被取消申请
    protected static final int PREMISSION_SUCCESS=1;//权限申请成功
    protected MusicLoadingView mLoadingView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

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
                new PermissionModel(Manifest.permission.READ_EXTERNAL_STORAGE, "读取本地音频文件需要'读取本地存储'权限", READ_EXTERNAL_STORAGE_CODE),
                new PermissionModel(Manifest.permission.WRITE_EXTERNAL_STORAGE, "播放记录存储需要'写入数据到存储'权限", WRITE_EXTERNAL_STORAGE_CODE)
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
                            new android.support.v7.app.AlertDialog.Builder(MusicBaseActivity.this)
                                    .setTitle("获取本地音乐失败")
                                    .setMessage(findPermissionExplain(permissions[0]))
                                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            onRequstPermissionResult(PREMISSION_CANCEL);
                                        }
                                    })
                                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            requstPermissions();
                                        }
                                    }).setCancelable(false).show();
                        } else {
                            //用户勾选了不再询问，手动开启
                            new android.support.v7.app.AlertDialog.Builder(MusicBaseActivity.this)
                                    .setTitle("获取本地音乐失败")
                                    .setMessage("读取本地存储权限被拒绝，请点击‘去设置’手动开启读取本地存储权限")
                                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            onRequstPermissionResult(PREMISSION_CANCEL);
                                        }
                                    })
                                    .setPositiveButton("去设置", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            startActivityForResult(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.fromParts("package", getPackageName(), null)), SETTING_REQUST);
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
                if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, model.permission)) {
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
        Logger.d(TAG,"onResume:");
        if(isWindowEnable&&requstCode>0&&requstCode== MusicConstants.REQUST_WINDOWN_PERMISSION&&
                MusicWindowManager.getInstance().checkAlertWindowsPermission(MusicBaseActivity.this)){
            requstCode=0;
            createMiniJukeBoxToWindown();
        }
    }

    /**
     * 即将退出播放器
     */
    protected void createMiniJukeboxWindow() {
        if(!MusicWindowManager.getInstance().checkAlertWindowsPermission(MusicBaseActivity.this)){
            new android.support.v7.app.AlertDialog.Builder(MusicBaseActivity.this)
                    .setTitle("播放提示")
                    .setMessage("前往开启悬浮窗，更好的体验播放功能")
                    .setNegativeButton("暂不开启", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MusicPlayerManager.getInstance().onStop();
                            finish();
                        }
                    })
                    .setPositiveButton("去开启", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.setData(Uri.parse( "package:"+MusicUtils.getInstance().getPackageName(MusicBaseActivity.this)));
                                MusicBaseActivity.this.startActivityForResult(intent,MusicConstants.REQUST_WINDOWN_PERMISSION);
                            } else {
                                Toast.makeText(MusicBaseActivity.this,"请在设置中手动开启",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent();
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                                intent.setData(Uri.fromParts("package", getPackageName(), null));
                                startActivityForResult(intent,MusicConstants.REQUST_WINDOWN_PERMISSION);
                            }
                        }
                    }).setCancelable(false).show();
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
                BaseMediaInfo musicInfo = MusicPlayerManager.getInstance().getCurrentPlayerMusic();
                MusicWindowManager.getInstance().createMiniJukeBoxToWindown(MusicBaseActivity.this.getApplicationContext(), MusicUtils.getInstance().dpToPxInt(MusicBaseActivity.this,80f)
                        ,MusicUtils.getInstance().dpToPxInt(MusicBaseActivity.this,170f));
                MusicStatus musicStatus=new MusicStatus();
                musicStatus.setId(musicInfo.getId());
                String frontPath=MusicUtils.getInstance().getMusicFrontPath(musicInfo);
                musicStatus.setCover(frontPath);
                musicStatus.setTitle(musicInfo.getVideo_desp());
                MusicPlayerState playerState = MusicPlayerManager.getInstance().getPlayerState();
                boolean playing = playerState.equals(MusicPlayerState.MUSIC_PLAYER_PLAYING) || playerState.equals(MusicPlayerState.MUSIC_PLAYER_PREPARE) || playerState.equals(MusicPlayerState.MUSIC_PLAYER_BUFFER);
                musicStatus.setPlayerStatus(playing?MusicStatus.PLAYER_STATUS_START:MusicStatus.PLAYER_STATUS_PAUSE);
                MusicWindowManager.getInstance().updateWindowStatus(musicStatus);
                MusicWindowManager.getInstance().onVisible();
            }
        }
    }

    /**
     * 显示加载中弹窗
     * @param message
     */
    public void showProgressDialog(String message){
        if(!MusicBaseActivity.this.isFinishing()){
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
            if(!MusicBaseActivity.this.isFinishing()){
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
     * @param itemId
     * @param mediaInfo
     */
    protected void onMusicMenuClick(int position,int itemId, BaseMediaInfo mediaInfo) {
        if(itemId== MusicDetails.ITEM_ID_NEXT_PLAY){
            MusicPlayerManager.getInstance().playNextMusic();
        }else if(itemId== MusicDetails.ITEM_ID_SHARE){
            try {
                if(!TextUtils.isEmpty(mediaInfo.getFile_path())){
                    if(mediaInfo.getFile_path().startsWith("http:")||mediaInfo.getFile_path().startsWith("https:")){
                        Intent sendIntent = new Intent();
                        //sendIntent.setPackage("com.tencent.mm")
                        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "iMusic分享");
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, "我正在使用"+getResources().getString(R.string.app_name)+
                                "听:《"+mediaInfo.getVideo_desp()+"》，快来听吧~猛戳-->"+mediaInfo.getFile_path());
                        sendIntent.setType("text/plain");
                        startActivity(Intent.createChooser(sendIntent, "iMusic分享"));
                    }else{
                        Intent sendIntent = new Intent();
                        //sendIntent.setPackage("com.tencent.mm")
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, "来自iMusic的音乐分享:《"+mediaInfo.getVideo_desp()+"》-"+mediaInfo.getNickname());
                        sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(mediaInfo.getFile_path()));
                        sendIntent.setType("audio/*");
                        startActivity(Intent.createChooser(sendIntent, "iMusic分享"));
                    }
                }else{
                    Toast.makeText(MusicBaseActivity.this,"此歌曲已被下架",Toast.LENGTH_SHORT).show();
                }
            }catch (RuntimeException e){
                e.printStackTrace();
                Toast.makeText(MusicBaseActivity.this,"分享失败："+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }else if(itemId==MusicDetails.ITEM_ID_COLLECT){
            boolean toCollect = MusicUtils.getInstance().putMusicToCollect(mediaInfo);
            if(toCollect){
                Toast.makeText(MusicBaseActivity.this,"已添加至收藏列表",Toast.LENGTH_SHORT).show();
                MusicPlayerManager.getInstance().observerUpdata(new MusicStatus());
            }
        }
    }

    /**
     * 打开播放器
     * @param musicID
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
     * @param musicID
     * @param mediaInfos
     */
    protected void startToMusicPlayer(long musicID,List<MediaInfo> mediaInfos){
        Intent intent=new Intent(getApplicationContext(), MusicPlayerActivity.class);
        intent.putExtra(MusicConstants.KEY_MUSIC_LIST, (Serializable) mediaInfos);
        intent.putExtra(MusicConstants.KEY_MUSIC_ID, musicID);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(intent);
    }

    /**
     * 打开播放器
     * @param musicID
     * @param mediaInfos
     */
    protected void startMusicPlayer(long musicID,List<BaseMediaInfo> mediaInfos){
        Intent intent=new Intent(getApplicationContext(), MusicPlayerActivity.class);
        intent.putExtra(MusicConstants.KEY_MUSIC_LIST, (Serializable) mediaInfos);
        intent.putExtra(MusicConstants.KEY_MUSIC_ID, musicID);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null!=mPresenter){
            mPresenter.onDestroy();
            mPresenter=null;
        }
    }
}