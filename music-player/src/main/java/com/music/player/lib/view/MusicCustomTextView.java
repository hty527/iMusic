package com.music.player.lib.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;

/**
 * TinyHung@Outlook.com
 * 2017/11/9
 * MusicCustomTextView
 */
public class MusicCustomTextView extends android.support.v7.widget.AppCompatTextView {

    private Paint paint1;
    private Paint paint2;
    private int mWidth;
    private LinearGradient gradient;
    private Matrix matrix;
    //渐变的速度
    private int deltaX;

    public MusicCustomTextView(Context context) {
        super(context, null);
    }

    public MusicCustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        paint1 = new Paint();
        paint1.setColor(getResources().getColor(android.R.color.holo_blue_dark));
        paint1.setStyle(Paint.Style.FILL);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if(mWidth == 0){
            mWidth = getMeasuredWidth();
            paint2 = getPaint();
            //颜色渐变器
            gradient = new LinearGradient(0, 0, mWidth, 0, new int[]{Color.GRAY, Color.WHITE, Color.GRAY,}, new float[]{
                    0.3f,0.5f,1.0f
            }, Shader.TileMode.CLAMP);
            paint2.setShader(gradient);
            matrix = new Matrix();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(matrix !=null){
            deltaX += mWidth / 5;
            if(deltaX > 2 * mWidth){
                deltaX = -mWidth;
            }
        }
        //关键代码通过矩阵的平移实现
        matrix.setTranslate(deltaX, 0);
        gradient.setLocalMatrix(matrix);
        postInvalidateDelayed(100);
    }

    public void onDestroy() {
        gradient=null;matrix=null;paint1=null;paint2=null;
    }
}