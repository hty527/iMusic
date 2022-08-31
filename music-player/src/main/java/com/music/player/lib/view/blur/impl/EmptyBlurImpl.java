package com.music.player.lib.view.blur.impl;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * created by hty
 * 2022/6/19
 * Desc:
 */
public class EmptyBlurImpl implements BlurImpl {

    @Override
    public boolean prepare(Context context, Bitmap buffer, float radius) {
        return false;
    }

    @Override
    public void release() {

    }

    @Override
    public void blur(Bitmap input, Bitmap output) {

    }
}