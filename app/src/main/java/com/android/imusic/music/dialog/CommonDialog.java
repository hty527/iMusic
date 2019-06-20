package com.android.imusic.music.dialog;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import com.android.imusic.R;
import com.android.imusic.base.BaseDialog;
import com.android.imusic.music.utils.MediaUtils;

/**
 * TinyHung@Outlook.com
 * 2017/3/24 9:12
 * 通用对话框
 */

public class CommonDialog extends BaseDialog {

    public static CommonDialog getInstance(Activity activity) {
        return new CommonDialog(activity);
    }

    public CommonDialog(@NonNull Activity context) {
        super(context, R.style.CenterDialogAnimationStyle);
        MediaUtils.getInstance().setDialogWidth(this);
    }

    /**
     * 设置弹窗内容
     * @param view
     * @return
     */
    public CommonDialog setContent(View view) {
        setContentView(view);
        MediaUtils.getInstance().setDialogWidth(this);
        return this;
    }

    /**
     * 设置弹窗内容
     * @param view
     * @param params
     * @return
     */
    public CommonDialog setContent(View view, ViewGroup.LayoutParams params) {
        setContentView(view, params);
        return this;
    }

    /**
     * 是否允许触摸边界关闭此弹窗
     * @param isCanceledOnTouchOutside
     * @return
     */
    public CommonDialog setDialogCanceledOnTouchOutside(boolean isCanceledOnTouchOutside){
        this.setCanceledOnTouchOutside(isCanceledOnTouchOutside);
        return this;
    }

    /**
     * 是否允许按下返回键关闭弹窗
     * @param isCancelable
     * @return
     */
    public CommonDialog setDialogCancelable(boolean isCancelable){
        this.setCancelable(isCancelable);
        return this;
    }

    @Override
    public void initViews() {

    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

}
