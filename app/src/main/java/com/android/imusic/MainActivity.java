package com.android.imusic;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.android.imusic.base.BaseActivity;
import com.android.imusic.base.BasePresenter;
import com.android.imusic.music.activity.MusicLockActivity;
import com.android.imusic.music.activity.MusicPlayerActivity;
import com.android.imusic.music.adapter.MusicFragmentPagerAdapter;
import com.android.imusic.music.manager.SqlLiteCacheManager;
import com.android.imusic.music.manager.VersionUpdateManager;
import com.android.imusic.music.ui.fragment.IndexMusicFragment;
import com.android.imusic.music.utils.MediaUtils;
import com.android.imusic.net.OkHttpUtils;
import com.android.imusic.video.activity.VideoPlayerActviity;
import com.android.imusic.video.fragment.IndexVideoFragment;
import com.music.player.lib.bean.BaseAudioInfo;
import com.music.player.lib.constants.MusicConstants;
import com.music.player.lib.listener.MusicInitializeCallBack;
import com.music.player.lib.listener.MusicPlayerInfoListener;
import com.music.player.lib.manager.MusicPlayerManager;
import com.music.player.lib.manager.MusicWindowManager;
import com.music.player.lib.model.MusicPlayerConfig;
import com.music.player.lib.util.Logger;
import com.music.player.lib.util.MusicUtils;
import com.video.player.lib.manager.VideoPlayerManager;
import com.video.player.lib.manager.VideoWindowManager;
import java.util.ArrayList;
import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2019/3/17
 * iMusic
 * Main
 */

public class MainActivity extends BaseActivity {

    private long currentMillis=0;
    private TextView mBtnMusic,mBtnVideo;
    private ViewPager mViewPager;
    private MusicFragmentPagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //视频、音乐播放器初始化
        initConfig();
        mBtnMusic = (TextView) findViewById(R.id.music_btn_music);
        mBtnMusic.setSelected(true);
        mBtnVideo = (TextView) findViewById(R.id.music_btn_video);
        View.OnClickListener onClickListener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                switch (id) {
                    case R.id.music_btn_music:
                        mBtnVideo.setSelected(false);
                        mBtnMusic.setSelected(true);
                        mViewPager.setCurrentItem(0);
                        break;
                    case R.id.music_btn_video:
                        mBtnMusic.setSelected(false);
                        mBtnVideo.setSelected(true);
                        mViewPager.setCurrentItem(1);
                        break;
                }
            }
        };
        mBtnMusic.setOnClickListener(onClickListener);
        mBtnVideo.setOnClickListener(onClickListener);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setOverScrollMode(View.OVER_SCROLL_NEVER);
        List<Fragment> fragments=new ArrayList<>();
        fragments.add(new IndexMusicFragment());
        fragments.add(new IndexVideoFragment());
        mViewPager.setOffscreenPageLimit(1);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels){

            }

            @Override
            public void onPageSelected(int position) {
                if(0==position){
                    mBtnVideo.setSelected(false);
                    mBtnMusic.setSelected(true);
                }else if(1==position){
                    mBtnMusic.setSelected(false);
                    mBtnVideo.setSelected(true);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mPagerAdapter = new MusicFragmentPagerAdapter(getSupportFragmentManager(), fragments);
        mViewPager.setAdapter(mPagerAdapter);
        requstPermissions();

        //当APP被回收或者用户退出了APP，音乐还在后台播放，点击通知栏时会将正在播放的音频ID传到此处
        long audioID = getIntent().getLongExtra(MusicConstants.KEY_MUSIC_ID, 0);
        if(audioID>0){
            startToMusicPlayer(audioID);
        }
    }

    /**
     * 完整的初始化
     */
    private void initConfig() {
        //视频播放器初始化
        VideoPlayerManager.getInstance()
                //循环模式
                .setLoop(true)
                //悬浮窗中打开播放器的绝对路径
                .setPlayerActivityClassName(VideoPlayerActviity.class.getCanonicalName());

        //音乐播放器配置
        MusicPlayerConfig config=MusicPlayerConfig.Build()
                //设置默认的闹钟定时关闭模式，优先取用户设置
                .setDefaultAlarmModel(MusicConstants.MUSIC_ALARM_MODEL_0)
                //设置默认的循环模式，优先取用户设置
                .setDefaultPlayModel(MusicConstants.MUSIC_MODEL_LOOP);

        //音乐播放器初始化
        MusicPlayerManager.getInstance()
                //内部存储初始化
                .init(getApplicationContext())
                //应用播放器配置
                .setMusicPlayerConfig(config)
                //常驻进程开关，默认开启
                .setLockForeground(true)
                //设置点击通知栏跳转的播放器界面,需开启常驻进程开关
                .setPlayerActivityName(MusicPlayerActivity.class.getCanonicalName())
                //设置锁屏界面，如果禁用，不需要设置或者设置为null
                .setLockActivityName(MusicLockActivity.class.getCanonicalName())
                //设置主界面路径，在APP退出后点击通知栏用到
                .setMainctivityName(MainActivity.class.getCanonicalName())
                //监听播放状态
                .setPlayInfoListener(new MusicPlayerInfoListener() {
                    @Override
                    public void onPlayMusiconInfo(BaseAudioInfo musicInfo, int position) {
                        //使用SQL存储本地播放记录
                        SqlLiteCacheManager.getInstance().insertHistroyAudio(musicInfo);
                    }
                })
                //重载方法，初始化音频媒体服务,成功之后如果系统还在播放音乐，则创建一个悬浮窗承载播放器
                .initialize(MainActivity.this, new MusicInitializeCallBack() {

                    @Override
                    public void onFinish() {
                        //如果系统正在播放音乐
                        if(null!=MusicPlayerManager.getInstance().getCurrentPlayerMusic()){
                            MusicPlayerManager.getInstance().createWindowJukebox();
                        }
                    }
                });

    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    @Override
    protected void onRequstPermissionResult(int resultCode) {
        super.onRequstPermissionResult(resultCode);
        if(resultCode==PREMISSION_SUCCESS){
            if(null!=mPagerAdapter){
                if(mPagerAdapter.getItem(0) instanceof IndexMusicFragment){
                    ((IndexMusicFragment) mPagerAdapter.getItem(0)).queryLocationMusic(MainActivity.this);
                }
            }
        }
        //检查并获取通知权限
        boolean premission = MusicUtils.getInstance().hasNiticePremission(getApplicationContext());
        if(!premission){
            new android.support.v7.app.AlertDialog.Builder(MainActivity.this)
                    .setTitle(getString(R.string.text_sys_tips))
                    .setMessage(getString(R.string.text_tips_notice))
                    .setNegativeButton(getString(R.string.music_text_cancel),null)
                    .setPositiveButton(getString(R.string.text_start_open), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                Uri uri = Uri.fromParts("package", getPackageName(), null);
                                intent.setData(uri);
                                startActivity(intent);
                            }catch (RuntimeException e){
                                e.printStackTrace();
                            }
                        }
                    }).setCancelable(false).show();
        }else{
            if(MusicUtils.getInstance().getInt(MusicConstants.SP_FIRST_START,0)==0){
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                        .setTitle(getString(R.string.text_action_tips))
                        .setMessage(getString(R.string.text_action_content))
                        .setNegativeButton(getString(R.string.text_start_now_open), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MediaUtils.getInstance().setLocalImageEnable(true);
                                Toast.makeText(MainActivity.this, getString(R.string.text_start_open_success),
                                        Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setPositiveButton(getString(R.string.text_yse), null).setCancelable(false);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        //检查版本更新
                        VersionUpdateManager.getInstance().checkAppVersion();
                    }
                });
                builder.show();
                MusicUtils.getInstance().putInt(MusicConstants.SP_FIRST_START,1);
            }else{
                //检查版本更新
                VersionUpdateManager.getInstance().checkAppVersion();
            }
        }
    }

    /**
     * 屏幕方向变化监听
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Logger.d(TAG,"onConfigurationChanged-->newConfig:"+newConfig.orientation);
        //转到横屏
        if(2==newConfig.orientation){
            MusicWindowManager.getInstance().onInvisible();
        //转到竖屏
        }else if(1==newConfig.orientation){
            MusicWindowManager.getInstance().onVisible();
        }
    }

    /**
     * 拦截返回和菜单事件
     * @param keyCode
     * @param event
     * @return
     */
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
        //第一遍只是退出全屏、迷你窗口播放器至常规状态
        if(VideoPlayerManager.getInstance().isBackPressed(false)){
            long millis = System.currentTimeMillis();
            if(0 == currentMillis | millis-currentMillis > 2000){
                Toast.makeText(MainActivity.this,getString(R.string.text_back_tips)+getResources().getString(R.string.app_name),Toast.LENGTH_SHORT).show();
                currentMillis=millis;
                return;
            }
            currentMillis=millis;
            //第二遍才是结束播放
            if(VideoPlayerManager.getInstance().isBackPressed(true)){
                super.onBackPressed();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        VideoPlayerManager.getInstance().onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        VideoPlayerManager.getInstance().onPause();
    }

    /**
     * 悬浮窗释放
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        VideoPlayerManager.getInstance().onDestroy();
        VideoWindowManager.getInstance().onDestroy();
        //播放器反初始化。此方法为重载方法，请阅读内部入参说明
        MusicPlayerManager.getInstance().unInitialize(MainActivity.this);
        OkHttpUtils.getInstance().onDestroy();
        if(null!=mPagerAdapter){
            mPagerAdapter.onDestroy();
            mPagerAdapter=null;
        }
    }
}