package com.music.player.lib.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.music.player.lib.R;

/**
 * TinyHung@Outlook.com
 * 2018/5/29
 * 通用的标题栏
 */

public class MusicCommentTitleView extends LinearLayout implements View.OnClickListener {

    private static final String TAG = "MusicCommentTitleView";
    private TextView mTitleView,mSubTitle;
    private long[] clickCount = new long[2];

    public MusicCommentTitleView(Context context) {
        super(context);
        init(context,null);
    }

    public MusicCommentTitleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        View.inflate(context, R.layout.music_comment_title_layout,this);
        ImageView btnback = (ImageView) findViewById(R.id.view_btn_back);
        ImageView btnMenu = (ImageView) findViewById(R.id.view_btn_menu);
        mTitleView = (TextView) findViewById(R.id.view_title);
        mSubTitle = (TextView) findViewById(R.id.view_sub_title);
        if(null!=attrs){
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MusicCommentTitleView);
            Drawable backDrawable = typedArray.getDrawable(R.styleable.MusicCommentTitleView_musicCommentBackRes);
            Drawable menuDrawable = typedArray.getDrawable(R.styleable.MusicCommentTitleView_musicCommentMenuRes);
            if(null!=backDrawable){
                btnback.setImageDrawable(backDrawable);
            }
            if(null!=menuDrawable){
                btnMenu.setImageDrawable(menuDrawable);
            }
            //标题
            String titleText = typedArray.getString(R.styleable.MusicCommentTitleView_musicCommentTitle);
            int titleColor = typedArray.getColor(R.styleable.MusicCommentTitleView_musicCommentTitleColor,Color.parseColor("#FFFFFF"));
            float titleSize = typedArray.getDimensionPixelSize(R.styleable.MusicCommentTitleView_musicCommentTitleSize, 18);
            //副标题
            String subTitleText = typedArray.getString(R.styleable.MusicCommentTitleView_musicCommentSubTitle);
            int subTitleColor = typedArray.getColor(R.styleable.MusicCommentTitleView_musicCommentSubTitleColor,Color.parseColor("#EFEFEF"));
            float subTitleSize = typedArray.getDimensionPixelSize(R.styleable.MusicCommentTitleView_musicCommentSubTitleSize, 14);
            //标题
            mTitleView.setText(titleText);
            mTitleView.setTextColor(titleColor);
            mTitleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,titleSize);
            if(!TextUtils.isEmpty(subTitleText)){
                btnMenu.setVisibility(GONE);
                //副标题
                mSubTitle.setText(subTitleText);
                mSubTitle.setTextColor(subTitleColor);
                mSubTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP,subTitleSize);
            }
            typedArray.recycle();
        }
        btnback.setOnClickListener(this);
        mTitleView.setOnClickListener(this);
        mSubTitle.setOnClickListener(this);
        btnMenu.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id==R.id.view_btn_back){
            if(null!=mOnTitleClickListener) mOnTitleClickListener.onBack(v);
        }else if(id==R.id.view_sub_title){
            if(null!=mOnTitleClickListener) mOnTitleClickListener.onSubTitleClick(v);
        }else if(id==R.id.view_btn_menu){
            if(null!=mOnTitleClickListener) mOnTitleClickListener.onMenuClick(v);
        }else if(id==R.id.view_title){
            if(null!=clickCount&&null!=mOnTitleClickListener){
                System.arraycopy(clickCount,1,clickCount,0,clickCount.length - 1);
                clickCount[clickCount.length - 1] = SystemClock.uptimeMillis();
                if (clickCount[0] >= (clickCount[clickCount.length - 1] - 1000)) {
                    if(null!=mOnTitleClickListener) mOnTitleClickListener.onTitleClick(v,true);
                    return;
                }
                if(null!=mOnTitleClickListener) mOnTitleClickListener.onTitleClick(v,false);
            }
        }
    }

    public void setTitle(String title){
        if(null!=mTitleView){
            mTitleView.setText(title);
        }
    }

    public void setSubTitle(String subTitle) {
        if(null!=mSubTitle){
            mSubTitle.setVisibility(VISIBLE);
            findViewById(R.id.view_btn_menu).setVisibility(GONE);
            mSubTitle.setText(subTitle);
        }
    }

    public abstract static class OnTitleClickListener{
        public void onBack(View view){}
        public void onTitleClick(View view,boolean doubleClick){}
        public void onSubTitleClick(View v){}
        public void onMenuClick(View v){}
    }

    private OnTitleClickListener mOnTitleClickListener;

    public void setOnTitleClickListener(OnTitleClickListener onTitleClickListener) {
        mOnTitleClickListener = onTitleClickListener;
    }
}