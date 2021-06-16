package com.android.imusic.music.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.android.imusic.R;

/**
 * Created by TinyHung@outlook.com
 * 2019/5/10
 * 自行指定背景圆角、颜色的BUTTON
 */

public class ShapeTextView extends android.support.v7.widget.AppCompatTextView implements View.OnTouchListener {

    private boolean mTextMarquee;
    private float mStrokeWidth=0.6f;
    //圆角、边框
    private int mRadius,mStroke;
    //背景颜色
    private int mBackGroundColor= Color.parseColor("#00000000")
            //背景按下颜色
            ,mBackGroundSelectedColor= Color.parseColor("#00000000")
            //边框颜色
            ,mStrokeColor= Color.parseColor("#00000000");

    public ShapeTextView(@NonNull Context context) {
        this(context,null);
    }

    public ShapeTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ShapeTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setOnTouchListener(this);
        if(null!=attrs){
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShapeTextView);
            mRadius = typedArray.getDimensionPixelSize(R.styleable.ShapeTextView_shapeRadius, 0);
            mStroke = typedArray.getDimensionPixelSize(R.styleable.ShapeTextView_shapeStrokeWidth, 0);
            mStrokeColor = typedArray.getColor(R.styleable.ShapeTextView_shapeStrokeColor,
                    ContextCompat.getColor(getContext(), android.R.color.transparent));
            mBackGroundColor = typedArray.getColor(R.styleable.ShapeTextView_shapeBackgroundColor,
                    ContextCompat.getColor(getContext(), R.color.colorAccent));
            mBackGroundSelectedColor = typedArray.getColor(R.styleable.ShapeTextView_shapeBackgroundSelectorColor,
                    ContextCompat.getColor(getContext(), R.color.colorPrimaryDark));
            mStrokeWidth = typedArray.getFloat(R.styleable.ShapeTextView_shapeStorkeWidth,mStrokeWidth);
            mTextMarquee = typedArray.getBoolean(R.styleable.ShapeTextView_shapeMarquee,false);
            typedArray.recycle();
        }
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(mRadius);
        gradientDrawable.setStroke(mStroke,mStrokeColor);
        gradientDrawable.setColor(mBackGroundColor);
        this.setBackground(gradientDrawable);
        setClickable(true);
    }


    public void setRadius(int radius) {
        mRadius = radius;
        GradientDrawable gradientDrawable = (GradientDrawable) getBackground();
        if(null!=gradientDrawable){
            gradientDrawable.setCornerRadius(mRadius);
        }
    }

    public void setBackGroundColor(int color){
        this.mBackGroundColor=color;
        GradientDrawable gradientDrawable = (GradientDrawable) getBackground();
        if(null!=gradientDrawable){
            gradientDrawable.setColor(mBackGroundColor);
        }
    }

    public void setBackGroundSelectedColor(int color){
        this.mBackGroundSelectedColor=color;
    }

    public void setStroke(int stroke) {
        mStroke = stroke;
        GradientDrawable gradientDrawable = (GradientDrawable) getBackground();
        if(null!=gradientDrawable){
            gradientDrawable.setStroke(mStroke,mStrokeColor);
        }
    }

    public void setStrokeColor(int strokeColor) {
        mStrokeColor = strokeColor;
        GradientDrawable gradientDrawable = (GradientDrawable) getBackground();
        if(null!=gradientDrawable){
            gradientDrawable.setStroke(mStroke,mStrokeColor);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            //用户手指按下，使用按下Color
            case MotionEvent.ACTION_DOWN:
                GradientDrawable gradientDrawable = (GradientDrawable) getBackground();
                if(null!=gradientDrawable){
                    gradientDrawable.setColor(mBackGroundSelectedColor);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            //用户松手，使用默认背景Color
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                GradientDrawable background = (GradientDrawable) getBackground();
                if(null!=background){
                    background.setColor(mBackGroundColor);
                }
                break;
        }
        return super.onTouchEvent(event);
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