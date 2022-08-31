package com.music.player.lib.listener;

import android.view.View;

/**
 * TinyHung@Outlook.com
 * 2019/3/20
 */

public interface MusicWindowClickListener {

    /**
     * 悬浮窗单击事件
     * @param view 悬浮窗
     * @param musicID 当前绑定MediaID
     */
    void onWindownClick(View view, long musicID);

    /**
     * 悬浮窗取消单击事件
     * @param view 悬浮窗
     */
    void onWindownCancel(View view);
}