package com.android.imusic.video.view;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * 2019/4/8
 * 配合ViewPager滚动 片段缩放
 */

public class TransformerPageAnimation implements ViewPager.PageTransformer {

    private static final float MAX_SCALE = 1.0f;
    private static final float MIN_SCALE = 0.88f;//0.8f

    @Override
    public void transformPage(View view, float position) {
        if (position < -1){
            view.setScaleX(MIN_SCALE);
            view.setScaleY(MIN_SCALE);
        } else if (position <= 1){//a页滑动至b页 ； a页从 0.0 -1 ；b页从1 ~ 0.0
            float scaleFactor =  MIN_SCALE+(1- Math.abs(position))*(MAX_SCALE-MIN_SCALE);
            view.setScaleX(scaleFactor);
            //每次滑动后进行微小的移动目的是为了防止在三星的某些手机上出现两边的页面为显示的情况
            if(position>0){
                view.setTranslationX(-scaleFactor*2);
            }else if(position<0){
                view.setTranslationX(scaleFactor*2);
            }
            view.setScaleY(scaleFactor);
        } else {
            view.setScaleX(MIN_SCALE);
            view.setScaleY(MIN_SCALE);
        }
    }
}