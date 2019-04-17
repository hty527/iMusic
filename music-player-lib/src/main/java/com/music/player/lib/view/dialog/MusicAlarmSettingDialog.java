package com.music.player.lib.view.dialog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.GridLayoutManager;
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
import com.music.player.lib.adapter.MusicAlarmAdapter;
import com.music.player.lib.adapter.base.OnItemClickListener;
import com.music.player.lib.bean.MusicAlarmSetting;
import com.music.player.lib.manager.MusicPlayerManager;
import com.music.player.lib.model.MusicAlarmModel;
import com.music.player.lib.model.MusicItemSpaceDecoration;
import com.music.player.lib.util.Logger;
import com.music.player.lib.util.MusicUtils;
import com.music.player.lib.view.MusicBackgroungBlurView;
import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2019/3/8
 * Alarm Setting
 */

public class MusicAlarmSettingDialog extends BottomSheetDialog {

    private static final String TAG = "MusicAlarmSettingDialog";
    private MusicAlarmAdapter mAdapter;
    private MusicBackgroungBlurView mBlurView;

    public static MusicAlarmSettingDialog getInstance(Context context) {
        return new MusicAlarmSettingDialog(context);
    }

    public MusicAlarmSettingDialog(@NonNull Context context) {
        this(context, R.style.ButtomAnimationStyle);
    }

    public MusicAlarmSettingDialog(@NonNull Context context, int theme) {
        super(context, theme);
        setContentView(R.layout.music_dialog_alarm_setting);
        initLayoutPrams();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new MusicItemSpaceDecoration(MusicUtils.getInstance().dpToPxInt(context,10f)));
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),4,GridLayoutManager.VERTICAL,false));
        List<MusicAlarmSetting> alarmSettings = MusicUtils.getInstance().createAlarmSettings();
        mAdapter = new MusicAlarmAdapter(context,alarmSettings);
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, long itemId) {
                if(null!=mOnAlarmModelListener&&null!=view.getTag()){
                    MusicAlarmSetting alarmSetting = (MusicAlarmSetting) view.getTag();
                    MusicAlarmSettingDialog.this.dismiss();
                    mOnAlarmModelListener.onAlarmModel(alarmSetting.getAlarmModel());
                }
            }
        });
        recyclerView.setAdapter(mAdapter);
        View.OnClickListener onClickListener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.btn_close) {
                    MusicAlarmSettingDialog.this.dismiss();

                } else if (v.getId() == R.id.view_btn_current_close) {
                    if (null != mOnAlarmModelListener) {
                        MusicAlarmSettingDialog.this.dismiss();
                        mOnAlarmModelListener.onAlarmModel(MusicAlarmModel.MUSIC_ALARM_MODEL_CURRENT);
                    }

                } else if(v.getId()==R.id.view_btn_cancel){
                    if (null != mOnAlarmModelListener) {
                        MusicAlarmSettingDialog.this.dismiss();
                        mOnAlarmModelListener.onAlarmModel(MusicAlarmModel.MUSIC_ALARM_MODEL_0);
                    }
                }
            }
        };
        findViewById(R.id.view_btn_current_close).setOnClickListener(onClickListener);
        findViewById(R.id.btn_close).setOnClickListener(onClickListener);
        TextView cancelBtn = findViewById(R.id.view_btn_cancel);
        cancelBtn.setOnClickListener(onClickListener);
        if (MusicPlayerManager.getInstance().getPlayerAlarmModel().equals(MusicAlarmModel.MUSIC_ALARM_MODEL_0)) {
            cancelBtn.setVisibility(View.GONE);
        } else {
            cancelBtn.setVisibility(View.VISIBLE);
        }
        int width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        LinearLayout contentLayout = (LinearLayout) findViewById(R.id.content_layout);
        contentLayout.measure(width,width);
        Logger.d(TAG,"HEIGHT:"+contentLayout.getMeasuredHeight());
        mBlurView = (MusicBackgroungBlurView) findViewById(R.id.view_blur_layout);
        mBlurView.getLayoutParams().height=contentLayout.getMeasuredHeight();
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

    private OnAlarmModelListener mOnAlarmModelListener;

    public MusicAlarmSettingDialog setOnAlarmModelListener(OnAlarmModelListener onAlarmModelListener) {
        mOnAlarmModelListener = onAlarmModelListener;
        return MusicAlarmSettingDialog.this;
    }
    public interface OnAlarmModelListener{
        void onAlarmModel(MusicAlarmModel alarmModel);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if(null!= mAdapter){
            mAdapter.onDestroy();
            mAdapter=null;
        }
        if(null!=mBlurView){
            mBlurView.onDestroy();
            mBlurView=null;
        }
    }
}