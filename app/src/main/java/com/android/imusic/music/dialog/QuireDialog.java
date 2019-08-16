package com.android.imusic.music.dialog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.imusic.R;
import com.android.imusic.base.BaseDialog;
import com.android.imusic.music.utils.MediaUtils;

/**
 * TinyHung@Outlook.com
 * 2017/9/15.
 * 询问对话框
 */

public class QuireDialog extends BaseDialog {

    //是否允许内部关闭弹窗
    private boolean btnClickDismiss = true;

    public static QuireDialog getInstance(Context context) {
        return new QuireDialog(context);
    }

    public QuireDialog(@NonNull Context context) {
        super(context, R.style.CenterDialogAnimationStyle);
        setContentView(R.layout.music_dialog_quire_layout);
        MediaUtils.getInstance().setDialogWidth(this);
    }

    @Override
    public void initViews() {
        View.OnClickListener onClickListener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_submit:
                        if (btnClickDismiss) {
                            QuireDialog.this.dismiss();
                        }
                        if(null!=mOnQueraConsentListener) mOnQueraConsentListener.onConsent(QuireDialog.this);
                        break;
                    case R.id.btn_cancel:
                        if (btnClickDismiss) {
                            QuireDialog.this.dismiss();
                        }
                        if(null!=mOnQueraConsentListener) mOnQueraConsentListener.onRefuse(QuireDialog.this);
                        break;
                }
            }
        };
        findViewById(R.id.btn_submit).setOnClickListener(onClickListener);
        findViewById(R.id.btn_cancel).setOnClickListener(onClickListener);
    }


    @Override
    public void dismiss() {
        super.dismiss();
        if(null!=mOnQueraConsentListener) mOnQueraConsentListener.onDissmiss();
    }

    /**
     * 设置标题
     * @param title
     * @return
     */
    public QuireDialog setTitleText(String title) {
        TextView tvTitle = (TextView) findViewById(R.id.tv_title);
        if(null!=tvTitle) tvTitle.setText(title);
        return this;
    }

    /**
     * 设置确定按钮文字内容
     * @param submitTitle
     * @return
     */
    public QuireDialog setSubmitTitleText(String submitTitle) {
        TextView tv_submit = (TextView) findViewById(R.id.btn_submit);
        if(null!=tv_submit) tv_submit.setText(submitTitle);
        return this;
    }

    /**
     * 设置取消文字按钮
     * @param cancelTitleText
     * @return
     */
    public  QuireDialog setCancelTitleText(String cancelTitleText) {
        TextView tv_cancel = (TextView) findViewById(R.id.btn_cancel);
        if(null!=tv_cancel) tv_cancel.setText(cancelTitleText);
        return this;
    }
    /**
     * 设置提示内容
     * @param content
     * @return
     */
    public  QuireDialog setContentText(String content) {
        TextView tv_content = (TextView) findViewById(R.id.tv_content);
        if(null!=tv_content) tv_content.setText(Html.fromHtml(content));
        return this;
    }

    /**
     * 设置标题文字颜色
     * @param color
     * @return
     */
    public  QuireDialog setTitleTextColor(int color) {
        TextView tvTitle = (TextView) findViewById(R.id.tv_title);
        if(null!=tvTitle) tvTitle.setTextColor(color);
        return this;
    }


    /**
     * 设置确定按钮文字颜色
     * @param color
     * @return
     */
    public QuireDialog setSubmitTitleTextColor(int color) {
        TextView tv_submit = (TextView) findViewById(R.id.btn_submit);
        if(null!=tv_submit) tv_submit.setTextColor(color);
        return this;
    }

    /**
     * 设置取消文字颜色
     * @param color
     * @return
     */
    public  QuireDialog setCancelTitleTextColor(int color) {
        TextView tv_cancel = (TextView) findViewById(R.id.btn_cancel);
        if(null!=tv_cancel) tv_cancel.setTextColor(color);
        return this;
    }

    /**
     * 设置提示内容文字颜色
     * @param color
     * @return
     */
    public  QuireDialog setContentTextColor(int color) {
        TextView tv_content = (TextView) findViewById(R.id.tv_content);
        if(null!=tv_content) tv_content.setTextColor(color);
        return this;
    }

    public QuireDialog setTopImageRes(int resID){
        ImageView imageView = (ImageView) findViewById(R.id.ic_top);
        if(null!=imageView){
            imageView.setImageResource(resID);
        }
        return this;
    }

    /**
     * 点击确认、取消按钮时是否自动关闭弹窗
     * @param dismiss 是否自动关闭
     * @return
     */
    public QuireDialog setBtnClickDismiss(boolean dismiss){
        this.btnClickDismiss = dismiss;
        return this;
    }

    /**
     * 是否允许按下返回键关闭弹窗
     * @param isCancelable
     * @return
     */
    public QuireDialog setDialogCancelable(boolean isCancelable){
        this.setCancelable(isCancelable);
        return this;
    }

    /**
     * 是否允许触摸边界关闭此弹窗
     * @param isCanceledOnTouchOutside
     * @return
     */
    public QuireDialog setDialogCanceledOnTouchOutside(boolean isCanceledOnTouchOutside){
        this.setCanceledOnTouchOutside(isCanceledOnTouchOutside);
        return this;
    }

    public abstract static class OnQueraConsentListener{
        public void onConsent(QuireDialog dialog){}
        public void onRefuse(QuireDialog dialog){}
        public void onDissmiss(){}
    }
    private OnQueraConsentListener mOnQueraConsentListener;

    public QuireDialog setOnQueraConsentListener(OnQueraConsentListener onQueraConsentListener) {
        mOnQueraConsentListener = onQueraConsentListener;
        return this;
    }
}