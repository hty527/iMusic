package com.music.player.lib.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.animation.Interpolator;
import android.widget.Scroller;
import java.lang.reflect.Field;

/**
 * TinyHung@Outlook.com
 * 2019/3/8
 * ViewPager Scroller
 */

public class MusicViewPagerScroller extends Scroller {

    private int mScrollDuration = 500; // 滑动速度
    private boolean isScroller=true;//是否允许滚动

    public MusicViewPagerScroller(Context context) {
        this(context,null);
    }

    public MusicViewPagerScroller(Context context, Interpolator interpolator) {
        super(context, interpolator);
    }

    /**
     * 设置速度速度
     * @param duration
     */
    public void setScrollDuration(int duration) {
        this.mScrollDuration = duration;
    }

    /**
     * 设置是否允许滚动
     * @param scroller
     */
    public void setScroller(boolean scroller) {
        isScroller = scroller;
    }

    public boolean isScroller() {
        return isScroller;
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        if(isScroller){
            super.startScroll(startX, startY, dx, dy, mScrollDuration);
        }else{
            super.startScroll(startX, startY, dx, dy, 0);
        }
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy) {
        if(isScroller){
            super.startScroll(startX, startY, dx, dy, mScrollDuration);
        }else{
            super.startScroll(startX, startY, dx, dy, 0);
        }
    }

    public void initViewPagerScroll(ViewPager viewPager) {
        try {
            Field mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            mScroller.set(viewPager, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
