package com.music.player.lib.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

/**
 * TinyHung@Outlook.com
 * 2017/11/27.
 */

public class MusicMarqueeTextView extends android.support.v7.widget.AppCompatTextView {

    public MusicMarqueeTextView(Context context) {
        super(context);
    }

    public MusicMarqueeTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MusicMarqueeTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}
