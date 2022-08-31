package com.video.player.lib.view;

import android.graphics.Outline;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.ViewOutlineProvider;

/**
 * TinyHung@Outlook.com
 * 2019/4/11
 * ViewRound Radius
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class VideoTextrueProvider extends ViewOutlineProvider {

    private float mRadius;

    public VideoTextrueProvider(float radius){
        this.mRadius = radius;
    }

    @Override
    public void getOutline(View view, Outline outline) {
        outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), mRadius);
        view.setClipToOutline(true);
    }
}