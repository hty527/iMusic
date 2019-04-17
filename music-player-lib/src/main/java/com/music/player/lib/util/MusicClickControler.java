package com.music.player.lib.util;

/**
 * hty_Yuye@Outlook.com
 * 2019/3/6
 * ClickControler
 */

public class MusicClickControler {

    private int mCounts = 0;            //设定时间内允许触发的次数
    private int mMillisSeconds = 0;           //设定的时间毫秒数
    private int mCurrentCounts = 0;     //当前已经触发的次数
    private long mFirstTriggerTime = 0; //当前时间段内首次触发的时间

    public void init(int nCounts, int millisSeconds) {
        this.mCounts = nCounts;
        this.mMillisSeconds = millisSeconds;
        this.mCurrentCounts = 0;
        this.mFirstTriggerTime = 0;
    }

    public boolean canTrigger() {
        long time = System.currentTimeMillis();
        //重置首次触发时间和已经触发次数
        if (mFirstTriggerTime == 0 || time - mFirstTriggerTime > mMillisSeconds) {
            mFirstTriggerTime = time;
            mCurrentCounts = 0;
        }
        //已经触发了mCounts次，本次不能触发
        if (mCurrentCounts >= mCounts) {
            return false;
        }

        ++mCurrentCounts;
        return true;
    }
}
