package com.music.player.lib.listener;

import android.view.View;
import com.music.player.lib.bean.MusicLrcRow;

/**
 * Created by TinyHung@outlook.com
 * 2019/5/23
 * 歌词控件、事件监听器
 */

public interface MusicLrcViewListener {

    /**
     * 歌词被拖动了
     * @param position 位置
     * @param lrcRow 歌词实体类
     */
    void onLrcSeeked(int position, MusicLrcRow lrcRow);

    /**
     * 单击了歌词
     * @param view 歌词控件
     */
    void onClick(View view);
}