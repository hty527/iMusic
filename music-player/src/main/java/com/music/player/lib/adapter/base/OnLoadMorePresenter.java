package com.music.player.lib.adapter.base;

/**
 * hty_Yuye@Outlook.com
 * 2019/3/24
 */

public interface OnLoadMorePresenter {
    //加载完成
    void onLoadComplete();
    //结束，一般为空调用
    void onLoadEnd();
    //加载失败
    void onLoadError();
}
