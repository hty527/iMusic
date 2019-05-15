package com.android.imusic;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.imusic.base.BaseActivity;
import com.android.imusic.base.BasePresenter;
import com.android.imusic.music.manager.VersionUpdateManager;
import com.android.imusic.music.activity.MusicLockActivity;
import com.android.imusic.music.activity.MusicPlayerActivity;
import com.android.imusic.music.adapter.MusicFragmentPagerAdapter;
import com.android.imusic.music.ui.fragment.IndexMusicFragment;
import com.android.imusic.music.utils.MediaUtils;
import com.android.imusic.net.OkHttpUtils;
import com.android.imusic.video.activity.VideoPlayerActviity;
import com.android.imusic.video.fragment.IndexVideoFragment;
import com.music.player.lib.constants.MusicConstants;
import com.music.player.lib.manager.MusicPlayerManager;
import com.music.player.lib.manager.MusicWindowManager;
import com.music.player.lib.model.MusicPlayerConfig;
import com.music.player.lib.model.MusicWindowStyle;
import com.music.player.lib.util.Logger;
import com.music.player.lib.util.MusicUtils;
import com.video.player.lib.manager.VideoPlayerManager;
import com.video.player.lib.manager.VideoWindowManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Music Player示例
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
        //视频播放器初始化
        VideoPlayerManager.getInstance()
                //悬浮窗中打开播放器的绝对路径，可选的参数,若需要支持从悬浮窗中还原到APP的播放器界面，
                // 则必须设定此参数
                .setVideoPlayerActivityClassName(VideoPlayerActviity.class.getCanonicalName());

        //音乐播放器初始化
        MusicPlayerConfig config=MusicPlayerConfig.Build()
            .setLockForeground(true)//前台服务锁定开关
            .setWindownAutoScrollToEdge(true)//悬浮窗自动吸附开关
            .setTrashEnable(true)//垃圾桶功能开关
            .setScreenOffEnable(true)//锁屏控制器开关
            .setWindownStyle(MusicWindowStyle.TRASH);//悬浮窗播放器样式
        MusicPlayerManager.getInstance().setMusicPlayerConfig(config);
        //设置点击通知栏打开的播放器界面,需开启setLockForeground(true);
        MusicPlayerManager.getInstance().setMusicPlayerActivityClassName(
                MusicPlayerActivity.class.getCanonicalName())
            //设置锁屏界面,需开启setScreenOffEnable(true);
            .setLockActivityName(MusicLockActivity.class.getCanonicalName());
        //初始化音频媒体服务
        MusicPlayerManager.getInstance().initialize(MainActivity.this);

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
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    @Override
    protected void onRequstPermissionResult(int resultCode) {
        super.onRequstPermissionResult(resultCode);
        if(resultCode==PREMISSION_SUCCESS){
            MusicUtils.getInstance().initACache(this);
            if(null!=mPagerAdapter){
                if(mPagerAdapter.getItem(0) instanceof IndexMusicFragment){
                    ((IndexMusicFragment) mPagerAdapter.getItem(0)).queryLocationMusic(MainActivity.this);
                }
            }
        }
        //检查并获取通知权限
        boolean premission = MusicUtils.getInstance().hasNiticePremission(this);
        if(!premission){
            new android.support.v7.app.AlertDialog.Builder(MainActivity.this)
                    .setTitle("系统提示")
                    .setMessage("检测到系统禁用了此APP的通知权限，为更好的体验，请前往手动开启通知栏权限！")
                    .setNegativeButton("取消",null)
                    .setPositiveButton("去开启", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MusicUtils.getInstance().startAppSetting(MainActivity.this);
                        }
                    }).setCancelable(false).show();
        }else{
            if(MusicUtils.getInstance().getInt(MusicConstants.SP_FIRST_START,0)==0){
                new android.support.v7.app.AlertDialog.Builder(MainActivity.this)
                        .setTitle("使用提示")
                        .setMessage("iMusic默认关闭了'本地音乐'列表音乐封面加载功能，如需开启或关闭，请双击" +
                                "标题栏'iMusic'开启或关闭封面加载")
                        .setNegativeButton("现在开启",new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MediaUtils.getInstance().setLocalImageEnable(true);
                                Toast.makeText(MainActivity.this,"本地音乐封面加载已开启",
                                        Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setPositiveButton("知道了", null).setCancelable(false).show();
                MusicUtils.getInstance().putInt(MusicConstants.SP_FIRST_START,1);
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
                Toast.makeText(MainActivity.this,"再按一次退出"+getResources().getString(R.string.app_name),Toast.LENGTH_SHORT).show();
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
        MusicWindowManager.getInstance().onDestroy();
        MusicPlayerManager.getInstance().unInitialize(MainActivity.this);
        OkHttpUtils.onDestroy();
        if(null!=mPagerAdapter){
            mPagerAdapter.onDestroy();
            mPagerAdapter=null;
        }
    }

    public void download(View view) {
        VersionUpdateManager.getInstance().checkAppVersion();
    }
}