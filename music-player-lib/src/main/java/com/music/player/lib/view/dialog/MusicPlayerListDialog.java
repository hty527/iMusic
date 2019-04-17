package com.music.player.lib.view.dialog;

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
import android.widget.LinearLayout;
import android.widget.TextView;
import com.music.player.lib.R;
import com.music.player.lib.adapter.MusicPlayerListAdapter;
import com.music.player.lib.adapter.base.OnItemClickListener;
import com.music.player.lib.bean.BaseMediaInfo;
import com.music.player.lib.bean.MusicStatus;
import com.music.player.lib.listener.MusicOnItemClickListener;
import com.music.player.lib.manager.MusicPlayerManager;
import com.music.player.lib.manager.MusicSubjectObservable;
import com.music.player.lib.util.Logger;
import com.music.player.lib.util.MusicUtils;
import com.music.player.lib.view.MusicBackgroungBlurView;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * TinyHung@Outlook.com
 * 2019/3/8
 * Player List
 */

public class MusicPlayerListDialog extends BottomSheetDialog implements Observer {

    private static final String TAG = "MusicPlayerListDialog";
    private MusicBackgroungBlurView mBlurView;
    private MusicPlayerListAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private int currentPosition=0;

    public static MusicPlayerListDialog getInstance(Context context) {
        return new MusicPlayerListDialog(context);
    }

    public MusicPlayerListDialog(@NonNull Context context) {
        this(context, R.style.ButtomAnimationStyle);
    }

    public MusicPlayerListDialog(@NonNull Context context, int theme) {
        super(context, theme);
        setContentView(R.layout.music_dialog_player_list);
        MusicPlayerManager.getInstance().addObservable(this);
        LinearLayout contentLayout = (LinearLayout) findViewById(R.id.content_layout);
        int height=(MusicUtils.getInstance().getScreenHeight(context)/5*3)- MusicUtils.getInstance().dpToPxInt(context,20f);
        contentLayout.getLayoutParams().height=height;
        initLayoutPrams();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MusicPlayerListAdapter(context,null);
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, long itemId) {
                if(null!= mMusicOnItemClickListener &&null!=view.getTag()){
                    if(currentPosition!=position){
                        mAdapter.getData().get(currentPosition).setSelected(false);
                        mAdapter.getData().get(position).setSelected(true);
                        mAdapter.notifyItemChanged(currentPosition,"ITEM_UPDATE");
                        mAdapter.notifyItemChanged(position,"ITEM_UPDATE");
                        currentPosition=position;
                        //选中
                        mMusicOnItemClickListener.onItemClick(view,position,itemId);
                    }
                }
            }
        });
        recyclerView.setAdapter(mAdapter);
        findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicPlayerListDialog.this.dismiss();
            }
        });
        mBlurView = (MusicBackgroungBlurView) findViewById(R.id.view_blur_layout);
        mBlurView.getLayoutParams().height=contentLayout.getLayoutParams().height;
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

    @Override
    public void show() {
        super.show();
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(null!= mAdapter){
                    List<?> currentPlayList = MusicPlayerManager.getInstance().getCurrentPlayList();
                    if(null!=currentPlayList&&currentPlayList.size()>0){
                        List<BaseMediaInfo> musicInfos=new ArrayList<>();
                        musicInfos.addAll((Collection<? extends BaseMediaInfo>) currentPlayList);
                        BaseMediaInfo currentPlayerMusic = MusicPlayerManager.getInstance().getCurrentPlayerMusic();
                        if(null!=currentPlayerMusic){
                            for (int i = 0; i < musicInfos.size(); i++) {
                                if(currentPlayerMusic.getId()==musicInfos.get(i).getId()){
                                    currentPosition=i;
                                    musicInfos.get(i).setSelected(true);
                                }else{
                                    musicInfos.get(i).setSelected(false);
                                }
                            }
                        }
                        Logger.d(TAG,"currentPosition:"+currentPosition);
                        mAdapter.setNewData(musicInfos);
                        if(null!=mLayoutManager){
                            mLayoutManager.scrollToPositionWithOffset(currentPosition, MusicUtils.getInstance().dpToPxInt(getContext(),43f));
                        }
                        ((TextView) findViewById(R.id.view_item_title)).setText("播放列表("+musicInfos.size()+"首)");
                    }
                }
            }
        },100);
    }

    private MusicOnItemClickListener mMusicOnItemClickListener;

    public MusicPlayerListDialog setMusicOnItemClickListener(MusicOnItemClickListener musicOnItemClickListener) {
        mMusicOnItemClickListener = musicOnItemClickListener;
        return MusicPlayerListDialog.this;
    }

    @Override
    public void dismiss() {
        super.dismiss();
        MusicPlayerManager.getInstance().removeObserver(this);
        if(null!= mAdapter){
            mAdapter.onDestroy();
            mAdapter=null;
        }
        if(null!=mBlurView){
            mBlurView.onDestroy();
            mBlurView=null;
        }
        mLayoutManager=null;currentPosition=0;
    }

    /**
     * 关系播放事件
     * @param o
     * @param arg
     */
    @Override
    public void update(Observable o, Object arg) {
        if(null!=mAdapter&&o instanceof MusicSubjectObservable && null!=arg && arg instanceof MusicStatus){
            MusicStatus musicStatus= (MusicStatus) arg;
            if(MusicStatus.PLAYER_STATUS_PREPARED==musicStatus.getPlayerStatus()){
                int currentPlayIndex = MusicUtils.getInstance().getCurrentPlayIndex(mAdapter.getData(), musicStatus.getId());
                Logger.d(TAG,"update:新的播放对象："+musicStatus.getId()+",位置："+currentPlayIndex+",旧的位置："+currentPosition);
                mAdapter.getData().get(currentPosition).setSelected(false);
                mAdapter.getData().get(currentPlayIndex).setSelected(true);
                mAdapter.notifyItemChanged(currentPosition,"ITEM_UPDATE");
                mAdapter.notifyItemChanged(currentPlayIndex,"ITEM_UPDATE");
                currentPosition=currentPlayIndex;
            }
        }
    }
}