package com.android.imusic.base;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialog;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import com.android.imusic.R;
import com.music.player.lib.util.MusicUtils;

/**
 * TinyHung@Outlook.com
 * 2017/3/24 9:12
 * 弹窗的统一父类
 */

public abstract class BaseDialog extends AppCompatDialog {

    protected Context mContext;

    public BaseDialog(@NonNull Context context) {
        super(context, R.style.CenterDialogAnimationStyle);
        mContext=context;
    }

    public BaseDialog(@NonNull Context context, int themeResId) {
        super(context,themeResId);
        mContext=context;
    }

    @Override
    public void setContentView(int layoutResId) {
        super.setContentView(layoutResId);
        initViews();
    }
    public abstract void initViews();

    /**
     * 设置Dialog依附在屏幕中的位置
     */
    protected void initLayoutParams(int gravity) {
        Window window = getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();//得到布局管理者
        WindowManager systemService = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);//得到窗口管理者
        DisplayMetrics displayMetrics=new DisplayMetrics();//创建设备屏幕的管理者
        systemService.getDefaultDisplay().getMetrics(displayMetrics);//得到屏幕的宽高
        int hight= LinearLayout.LayoutParams.WRAP_CONTENT;//取出布局的高度
        attributes.height= hight;
        attributes.width= systemService.getDefaultDisplay().getWidth();
        attributes.gravity= gravity;
    }


    /**
     * 设置Dialog依附在屏幕中的位置
     */
    protected void initLayoutMarginParams(int gravity) {
        Window window = getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();//得到布局管理者
        WindowManager systemService = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);//得到窗口管理者
        DisplayMetrics displayMetrics=new DisplayMetrics();//创建设备屏幕的管理者
        systemService.getDefaultDisplay().getMetrics(displayMetrics);//得到屏幕的宽高
        int hight= LinearLayout.LayoutParams.WRAP_CONTENT;//取出布局的高度
        attributes.height= hight;
        attributes.width= systemService.getDefaultDisplay().getWidth()-120;
        attributes.gravity= gravity;
    }

    protected AppCompatActivity getActivity(){
        return MusicUtils.getInstance().getAppCompActivity(mContext);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        mContext=null;
    }
}
