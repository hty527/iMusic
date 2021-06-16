package com.android.imusic.music.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextPaint;
import android.util.AttributeSet;
import com.android.imusic.R;

/**
 * Created by TinyHung@outlook.com
 * 2019/11/18
 * 粗效果的TextView
 */

public class BoldMediumTextView extends AppCompatTextView {

    private float mStrokeWidth=0.6f;
    private boolean mTextMarquee;

    public BoldMediumTextView(Context context) {
        this(context,null);
    }

    public BoldMediumTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public BoldMediumTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if(null!=attrs){
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BoldMediumTextView);
            mStrokeWidth = typedArray.getFloat(R.styleable.BoldMediumTextView_boldStorkeWidth,mStrokeWidth);
            mTextMarquee = typedArray.getBoolean(R.styleable.BoldMediumTextView_boldMarquee,false);
            typedArray.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //获取当前控件的画笔
        TextPaint paint = getPaint();
        //设置画笔的描边宽度值
        paint.setStrokeWidth(mStrokeWidth);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        super.onDraw(canvas);
    }

    /**
     * 设置描边宽度
     * @param strokeWidth 从0.0起
     */
    public void setStrokeWidth(float strokeWidth){
        this.mStrokeWidth=strokeWidth;
        invalidate();
    }

    @Override
    public boolean isFocused() {
        return mTextMarquee;
    }
}