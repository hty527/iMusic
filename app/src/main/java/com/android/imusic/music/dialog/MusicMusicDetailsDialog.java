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
import com.music.player.lib.bean.BaseAudioInfo;
import com.music.player.lib.listener.MusicOnItemClickListener;
import com.music.player.lib.listener.MusicPlayerEventListener;
import com.music.player.lib.manager.MusicPlayerManager;
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
     * @param audioInfo
     * @return
     */
    public static MusicMusicDetailsDialog getInstance(Context context, BaseAudioInfo audioInfo) {
        return new MusicMusicDetailsDialog(context,audioInfo,DialogScene.SCENE_LOCATION,null);
    }

    /**
     * 指定场景
     * @param context
     * @param audioInfo
     * @param sceneMode
     * @return
     */
    public static MusicMusicDetailsDialog getInstance(Context context, BaseAudioInfo audioInfo,
                                                      DialogScene sceneMode) {
        return new MusicMusicDetailsDialog(context,audioInfo,sceneMode,null);
    }

    /**
     * 携带专辑名称的入口
     * @param context
     * @param audioInfo
     * @param sceneMode
     * @param albumName
     * @return
     */
    public static MusicMusicDetailsDialog getInstance(Context context, BaseAudioInfo audioInfo,
                                                      DialogScene sceneMode,String albumName) {
        return new MusicMusicDetailsDialog(context,audioInfo,sceneMode,albumName);
    }

    public MusicMusicDetailsDialog(@NonNull Context context,BaseAudioInfo audioInfo,
                                   DialogScene sceneMode,String albumName) {
        this(context, R.style.MusicButtomAnimationStyle,audioInfo,sceneMode,albumName);
    }

    public MusicMusicDetailsDialog(@NonNull Context context, int theme,BaseAudioInfo audioInfo,
                                   DialogScene sceneMode,String albumName) {
        super(context, theme);
        setContentView(R.layout.music_dialog_details);
        this.mSceneMode=sceneMode;
        ((TextView) findViewById(R.id.view_item_title)).setText(getContext().getString(R.string.text_music_title)+audioInfo.getAudioName());
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false));
        List<MusicDetails> musicDetails=
                MediaUtils.getInstance().getMusicDetails(context,audioInfo,mSceneMode,albumName);
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
                MusicPlayerManager.getInstance().changedPlayerPlayModel();
        }
        });
        int playerModel = MusicPlayerManager.getInstance().getPlayerModel();
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
    private int getResToPlayModel(int playerModel,boolean isToast) {
        int playerModelToRes = MediaUtils.getInstance().getPlayerModelToRes(playerModel);
        if(isToast){
            Toast.makeText(getContext(),MediaUtils.getInstance().getPlayerModelToString(getContext(),playerModel),Toast.LENGTH_SHORT).show();
        }
        return playerModelToRes;
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

    public MusicMusicDetailsDialog setMusicOnItemClickListener(
            MusicOnItemClickListener musicOnItemClickListener) {
        mMusicOnItemClickListener = musicOnItemClickListener;
        return MusicMusicDetailsDialog.this;
    }

    @Override
    public void onMusicPlayerState(int playerState, String message) {}
    @Override
    public void onPrepared(long totalDurtion) {}
    @Override
    public void onBufferingUpdate(int percent) {}
    @Override
    public void onInfo(int event, int extra) {}
    @Override
    public void onPlayMusiconInfo(BaseAudioInfo musicInfo, int position) {}

    @Override
    public void onEchoPlayCurrentIndex(BaseAudioInfo musicInfo,int position) {}

    @Override
    public void onMusicPathInvalid(BaseAudioInfo musicInfo, int position) {}
    @Override
    public void onTaskRuntime(long totalDurtion, long currentDurtion,
                              long alarmResidueDurtion,int bufferProgress) {}

    @Override
    public void onPlayerConfig(int playModel, int alarmModel, boolean isToast) {
        if(null!=mBtnPlayModel){
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