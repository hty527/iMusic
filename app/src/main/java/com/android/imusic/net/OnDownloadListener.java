package com.android.imusic.net;

import java.io.File;

/**
 * Created by TinyHung@outlook.com
 * 2019/5/15
 * 文件下载监听
 */

public interface OnDownloadListener {

    /**
     * 下载百分比进度，回调在子线程
     * @param progress 百分比
     * @param totloLength 总长度
     * @param readLength 已下载长度
     */
    void progress(int progress, long totloLength, long readLength);

    /**
     * 下载完成
     * @param file 文件信息
     */
    void onSuccess(File file);

    /**
     * 下载失败
     * @param errorCode 错误码
     * @param message 描述信息
     */
    void onError(int errorCode, String message);
}
