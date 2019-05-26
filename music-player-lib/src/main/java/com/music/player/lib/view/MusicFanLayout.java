package com.music.player.lib.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.View;
import com.music.player.lib.R;

/**
 * hty_Yuye@Outlook.com
 * 2019/5/25
 * 一个碰撞的扇形
 */

public class MusicFanLayout extends View{

    public static final String TAG="MusicPlayerTrashLayout";
    private final int mColor;
    private final Region mCircleRegion;
    private Paint mPaint;

    public MusicFanLayout(Context context) {
        this(context,null);
    }

    public MusicFanLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MusicFanLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if(null!=attrs){
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MusicFanLayout);
            mColor = typedArray.getColor(R.styleable.MusicFanLayout_trashColor,
                    Color.parseColor("#FF0000"));
            typedArray.recycle();
        }else{
            mColor=Color.parseColor("#FF0000");
        }
        mPaint = new Paint();
        mPaint.setColor(mColor);
        mPaint.setAntiAlias(true);
        mCircleRegion = new Region();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //方式1
        //RectF rectF=new RectF(0,0,getWidth(),getHeight());
        //canvas.drawArc(rectF,180,90,true,mPaint);
        //方式2，直接绘制圆形，只不过半径放大一倍，这里的问题还未解决，*Region
        Path path = new Path();
        path.addCircle(getWidth(), getHeight(), getWidth(), Path.Direction.CCW);
        canvas.drawPath(path, mPaint);
        //将Path设置给Region
        Region globalRegion = new Region(-getWidth(), -getHeight(), getWidth(), getHeight());
        mCircleRegion.setPath(path, globalRegion);
    }
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                break;
//            case MotionEvent.ACTION_MOVE:
//                int rawX = (int) event.getRawX();
//                int rawY = (int) event.getRawY();
//                boolean contains = mCircleRegion.contains(rawX, rawY);
//                Logger.d(TAG,"rawX:"+rawX+",rawY:"+rawY+",contains:"+contains);
//                break;
//        }
//        return true;
//    }

    /**
     * 返回包含轨迹的Region
     * @return Region
     */
    public Region getRegion(){
        return mCircleRegion;
    }
}