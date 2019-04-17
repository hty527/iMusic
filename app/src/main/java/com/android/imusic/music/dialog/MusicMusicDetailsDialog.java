package com.android.imusic.music.dialog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.imusic.R;
import com.android.imusic.music.adapter.MusicDetailsAdapter;
import com.android.imusic.music.bean.MusicDetails;
import com.android.imusic.music.utils.MediaUtils;
import com.music.player.lib.adapter.base.OnItemClickListener;
import com.music.player.lib.bean.BaseMediaInfo;
import com.music.player.lib.listener.MusicOnItemClickListener;
import com.music.player.lib.listener.MusicPlayerEventListener;
import com.music.player.lib.manager.MusicPlayerManager;
import com.music.player.lib.model.MusicAlarmModel;
import com.music.player.lib.model.MusicPlayModel;
import com.music.player.lib.model.MusicPlayerState;
import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2019/3/23
 * Music Details
 */

public class MusicMusicDetailsDialog extends BottomSheetDialog implements MusicPlayerEventListener {

    private static final String TAG = "MusicPlayerListDialog";
    private MusicDetailsAdapter mAdapter;
    private ImageView mBtnPlayModel;
    //默认的访问场景
    private DialogScene mSceneMode=DialogScene.SCENE_LOCATION;

    public enum DialogScene{
        SCENE_LOCATION,
        SCENE_SEARCH,
        SCENE_ALBUM,
        SCENE_HISTROY,
        SCENE_COLLECT
    }

    /**
     * 普通入口，默认场景为本地音乐入口
     * @param context
     * @param mediaInfo
     * @return
     */
    public static MusicMusicDetailsDialog getInstance(Context context, BaseMediaInfo mediaInfo) {
        return new MusicMusicDetailsDialog(context,mediaInfo,DialogScene.SCENE_LOCATION,null);
    }

    /**
     * 指定场景
     * @param context
     * @param mediaInfo
     * @param sceneMode
     * @return
     */
    public static MusicMusicDetailsDialog getInstance(Context context, BaseMediaInfo mediaInfo,DialogScene sceneMode) {
        return new MusicMusicDetailsDialog(context,mediaInfo,sceneMode,null);
    }

    /**
     * 携带专辑名称的入口
     * @param context
     * @param mediaInfo
     * @param sceneMode
     * @param albumName
     * @return
     */
    public static MusicMusicDetailsDialog getInstance(Context context, BaseMediaInfo mediaInfo,DialogScene sceneMode,String albumName) {
        return new MusicMusicDetailsDialog(context,mediaInfo,sceneMode,albumName);
    }

    public MusicMusicDetailsDialog(@NonNull Context context,BaseMediaInfo mediaInfo,DialogScene sceneMode,String albumName) {
        this(context, R.style.ButtomAnimationStyle,mediaInfo,sceneMode,albumName);
    }

    public MusicMusicDetailsDialog(@NonNull Context context, int theme,BaseMediaInfo mediaInfo,DialogScene sceneMode,String albumName) {
        super(context, theme);
        setContentView(R.layout.music_dialog_details);
        this.mSceneMode=sceneMode;
        ((TextView) findViewById(R.id.view_item_title)).setText("歌曲："+mediaInfo.getVideo_desp());
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        List<MusicDetails> musicDetails= MediaUtils.getInstance().getMusicDetails(mediaInfo,mSceneMode,albumName);
        mAdapter = new MusicDetailsAdapter(context,musicDetails);
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, long itemId) {
                if(null!=view.getTag()){
                    MusicDetails details = (MusicDetails) view.getTag();
                    if(details.getItemID()>0){
                        MusicMusicDetailsDialog.this.dismiss();
                        mMusicOnItemClickListener.onItemClick(view,details.getItemID(),itemId);
                    }
                }
            }
        });
        recyclerView.setAdapter(mAdapter);
        initLayoutPrams();
        mBtnPlayModel = (ImageView) findViewById(R.id.view_btn_play_model);
        if(sceneMode.equals(DialogScene.SCENE_ALBUM)){
            mBtnPlayModel.setVisibility(View.INVISIBLE);
        }
        mBtnPlayModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicPlayerManager.getInstance().changedPlayerPlayFullModel();
        }
        });
        MusicPlayModel playerModel = MusicPlayerManager.getInstance().getPlayerModel();
        mBtnPlayModel.setImageResource(getResToPlayModel(playerModel,false));
    }

    public MusicMusicDetailsDialog setSceneMode(DialogScene sceneMode){
        this.mSceneMode=sceneMode;
        return this;
    }

    /**
     * 获取对应播放模式ICON
     * @param playerModel
     * @param isToast 是否吐司提示
     * @return
     */
    private int getResToPlayModel(MusicPlayModel playerModel,boolean isToast) {
        if(playerModel.equals(MusicPlayModel.MUSIC_MODEL_LOOP)){
            if(isToast){
                Toast.makeText(getContext(),"列表循环",Toast.LENGTH_SHORT).show();
            }
            return R.drawable.ic_music_model_loop;
        }
        if(playerModel.equals(MusicPlayModel.MUSIC_MODEL_SINGLE)){
            if(isToast){
                Toast.makeText(getContext(),"单曲循环",Toast.LENGTH_SHORT).show();
            }
            return R.drawable.ic_music_model_signle;
        }
        if(playerModel.equals(MusicPlayModel.MUSIC_MODEL_RANDOM)){
            if(isToast){
                Toast.makeText(getContext(),"随机播放",Toast.LENGTH_SHORT).show();
            }
            return R.drawable.ic_music_model_random;
        }
        return R.drawable.ic_music_model_signle;
    }

    protected void initLayoutPrams(){
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        WindowManager.LayoutParams attributes = window.getAttributes();
        WindowManager systemService = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics=new DisplayMetrics();
        systemService.getDefaultDisplay().getMetrics(displayMetrics);
        attributes.height= FrameLayout.LayoutParams.WRAP_CONTENT;
        attributes.width= systemService.getDefaultDisplay().getWidth();
        attributes.gravity= Gravity.BOTTOM;
    }

    private MusicOnItemClickListener mMusicOnItemClickListener;

    public MusicMusicDetailsDialog setMusicOnItemClickListener(MusicOnItemClickListener musicOnItemClickListener) {
        mMusicOnItemClickListener = musicOnItemClickListener;
        return MusicMusicDetailsDialog.this;
    }

    @Override
    public void onMusicPlayerState(MusicPlayerState playerState, String message) {}
    @Override
    public void onPrepared(long totalDurtion) {}
    @Override
    public void onBufferingUpdate(int percent) {}
    @Override
    public void onInfo(int event, int extra) {}
    @Override
    public void onPlayMusiconInfo(BaseMediaInfo musicInfo, int position) {}

    @Override
    public void onEchoPlayCurrentIndex(BaseMediaInfo musicInfo,int position) {}

    @Override
    public void onMusicPathInvalid(BaseMediaInfo musicInfo, int position) {}
    @Override
    public void onTaskRuntime(long totalDurtion, long currentDurtion, long alarmResidueDurtion,int bufferProgress) {}

    @Override
    public void onPlayerConfig(MusicPlayModel playModel, MusicAlarmModel alarmModel, boolean isToast) {
        if(null!=playModel&&null!=mBtnPlayModel){
            mBtnPlayModel.setImageResource(getResToPlayModel(playModel,isToast));
        }
    }

    @Override
    public void show() {
        super.show();
        MusicPlayerManager.getInstance().addOnPlayerEventListener(this);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if(null!= mAdapter){
            mAdapter.onDestroy();
            mAdapter=null;
        }
        MusicPlayerManager.getInstance().removePlayerListener(this);
    }
}