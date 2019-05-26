package com.music.player.lib.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import com.music.player.lib.R;
import com.music.player.lib.util.Logger;

/**
 * hty_Yuye@Outlook.com
 * 2019/5/25
 * 一个支持圆形,饼形进度条的自定义View
 * 如果是饼状:根据自身需求设置circlePrgBorder为0dp
 */

public class CircleProgressBar extends View {

    private static final String TAG = "CircleProgressBar";
    private Context mContext;
    private Paint mTextPaint,mProgressPaint,mCirclePaint;
    private int mWidth,mHeight;
    //文字、圆心、边框（背景、扇形）颜色
    private int mTextColor, mCircleColor, mProgressColor;
    //完成后的提示文字，如果为空则为 mMaxProgress%
    private String mSuccessText;
    //文字大小,实时进度
    private int mTextSize,mCurrentProgress;
    //是否是镂空的圆形进度条,是否显示文字进度
    private boolean mSolid =false,mShowText=true;
    //起始角度
    private int mStartAngle =270;
    //最大进度,最小进度,边框进度大小、边框大小
    private int mMaxProgress =100,mMiniProgress=0,mBorderWidth;



    public CircleProgressBar(Context context) {
        this(context,null);
    }

    public CircleProgressBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CircleProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext=context;
        if(null!=attrs){
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressBar);
            mTextSize = typedArray.getDimensionPixelSize(R.styleable.CircleProgressBar_circlePrgTextSize,15);
            mTextColor = typedArray.getColor(R.styleable.CircleProgressBar_circlePrgTextColor,Color.parseColor("#FFFFFF"));
            mCircleColor = typedArray.getColor(R.styleable.CircleProgressBar_circlePrgCircleColor,Color.parseColor("#8000FF"));
            mProgressColor = typedArray.getColor(R.styleable.CircleProgressBar_circlePrgProgressColor,Color.parseColor("#FF0000"));
            mStartAngle = typedArray.getInt(R.styleable.CircleProgressBar_circlePrgStartAngle,270);
            mMaxProgress = typedArray.getInt(R.styleable.CircleProgressBar_circlePrgMaxProgress,100);
            mMiniProgress = typedArray.getInt(R.styleable.CircleProgressBar_circlePrgMiniProgress,0);
            mSuccessText =typedArray.getString(R.styleable.CircleProgressBar_circlePrgSuccessText);
            mSolid=typedArray.getBoolean(R.styleable.CircleProgressBar_circlePrgIsSolid,false);
            mShowText=typedArray.getBoolean(R.styleable.CircleProgressBar_circlePrgIsShowText,true);
            mBorderWidth=typedArray.getDimensionPixelSize(R.styleable.CircleProgressBar_circlePrgBorder,3);
            typedArray.recycle();
        }else{
            mTextColor=Color.parseColor("#FFFFFF");
            mProgressColor=Color.parseColor("#FF0000");
            mCircleColor=Color.parseColor("#8000FF");
            mTextSize=dpToPxInt(getContext(),15f);
            mBorderWidth=dpToPxInt(getContext(),3f);
        }
        int toPxInt = dpToPxInt(getContext(), 15f);
        if(mTextSize<toPxInt){
            mTextSize=toPxInt;
        }
        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mProgressPaint = new Paint();
        mProgressPaint.setColor(mProgressColor);
        mProgressPaint.setTextAlign(Paint.Align.CENTER);
        mProgressPaint.setAntiAlias(true);
        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setTextAlign(Paint.Align.CENTER);
        mCirclePaint.setColor(mCircleColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(0==mWidth){
            mWidth=getWidth();
            mHeight=getHeight();
        }
        if(mSolid){
            //镂空的,将扇形绘制到底层
            canvas.drawArc(new RectF(0,0,mWidth,mHeight),
                    mStartAngle,getAngleToProgress(mCurrentProgress),true,mProgressPaint);
            //再画圆心
            canvas.drawCircle(mWidth/2,mHeight/2,
                    mWidth/2-mBorderWidth,mCirclePaint);
        }else{
            //饼状的,将圆心绘制到底层
            canvas.drawCircle(mWidth/2,mHeight/2,mWidth/2,mCirclePaint);
            canvas.drawArc(new RectF(mBorderWidth,mBorderWidth,mWidth-mBorderWidth,
                    mHeight-mBorderWidth), mStartAngle,getAngleToProgress(mCurrentProgress),
                    true,mProgressPaint);
        }
        if(mShowText){
            //绘制文字
            if(mCurrentProgress >=mMaxProgress){
                if(!TextUtils.isEmpty(mSuccessText)){
                    canvas.drawText(mSuccessText,mWidth/2,mHeight/2,mTextPaint);
                }else{
                    canvas.drawText(getTextToProgress(mCurrentProgress),mWidth/2,mHeight/2,mTextPaint);
                }
            }else{
                canvas.drawText(getTextToProgress(mCurrentProgress),mWidth/2,mHeight/2,mTextPaint);
            }
        }
    }

    /**
     * 设置文字大小
     * @param textSize dp size
     */
    public void setTextSize(int textSize) {
        mTextSize = textSize;
        postInvalidate();
    }

    /**
     * 设置文字颜色
     * @param textColor 色值
     */
    public void setTextColor(int textColor) {
        mTextColor = textColor;
        postInvalidate();
    }

    /**
     * 设置圆心颜色
     * @param circleColor 色值
     */
    public void setCircleColor(int circleColor) {
        mCircleColor = circleColor;
        postInvalidate();
    }

    /**
     * 设置进度条\扇形\背景颜色
     * @param progressColor 色值
     */
    public void setProgressColor(int progressColor) {
        mProgressColor = progressColor;
        postInvalidate();
    }

    /**
     * 设置完成提示文字
     * @param text 文本描述
     */
    public void setSuccessText(String text) {
        mSuccessText = text;
    }

    /**
     * 是否是镂空的进度条
     * @param solid true:镂空
     */
    public void setSolid(boolean solid) {
        mSolid = solid;
        postInvalidate();
    }

    /**
     * 设置起始角度
     * @param startAngle 起始角度
     */
    public void setStartAngle(int startAngle) {
        mStartAngle = startAngle;
        postInvalidate();
    }

    /**
     * 设置边框宽度
     * @param borderWidth dp size
     */
    public void setBorderWidth(int borderWidth) {
        mBorderWidth = borderWidth;
        postInvalidate();
    }

    /**
     * 是否显示文字进度
     * @param showText true:显示文字进度
     */
    public void setShowText(boolean showText) {
        mShowText = showText;
        postInvalidate();
    }

    /**
     * 设置最大进度阈值
     * @param maxProgress 0-Integer.MAX_VALUE
     */
    public void setMaxProgress(int maxProgress) {
        mMaxProgress = maxProgress;
    }

    /**
     * 设置最小进度阈值
     * @param miniProgress Integer.MIN_VALUE-0
     */
    public void setMiniProgress(int miniProgress) {
        mMiniProgress = miniProgress;
    }

    /**
     * 将实时进度转换为弧度，圆的弧度为360°，进度条是100，所以比例为1:3.6f
     * @param currentProgress
     * @return 弧度
     */
    private float getAngleToProgress(int currentProgress) {
        int progress = (int) ((float) currentProgress / mMaxProgress * 100);
        return progress*3.6f;
    }

    /**
     * 将实时进度转换为
     * @param currentProgress
     * @return
     */
    private String getTextToProgress(int currentProgress) {
        int progress = (int) ((float) currentProgress / mMaxProgress * 100);
        Logger.d(TAG,"progress:"+progress);
        return progress+"%";
    }

    /**
     * 设置进度,支持子线程刷新
     * @param progress 0-100之间的随机值
     */
    public void setProgress(int progress){
        if(progress< mMiniProgress){
            progress= mMiniProgress;
        }else if(progress> mMaxProgress){
            progress= mMaxProgress;
        }
        this.mCurrentProgress =progress;
        postInvalidate();
    }

    /**
     * 将dp转换成px
     * @param dp dp单位数值
     * @return px数值
     */
    private float dpToPx(Context context,float dp) {
        return dp * context.getApplicationContext().getResources().getDisplayMetrics().density;
    }

    private int dpToPxInt(Context context,float dp) {
        return (int) (dpToPx(context,dp) + 0.5f);
    }

    public void onDestroy(){
        mContext=null;mWidth=0;mHeight=0;mTextSize=0;mTextColor=0;
        mCircleColor=0;mProgressColor=0;
        mSuccessText =null;mCurrentProgress=0;
        mSolid=false;mStartAngle=0;mMaxProgress=0;mMiniProgress=0;
        mBorderWidth=0;mTextPaint=null;mProgressPaint=null;mCirclePaint=null;mShowText=false;
    }
}