package com.music.player.lib.view.blur.impl;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * created by hty
 * 2022/6/19
 * Desc:
 */
public interface BlurImpl {

    boolean prepare(Context context, Bitmap buffer, float radius);

    void release();

    void blur(Bitmap input, Bitmap output);
}