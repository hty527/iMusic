package com.music.player.lib.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.music.player.lib.R;
import com.music.player.lib.bean.MusicLrcRow;
import com.music.player.lib.exinterface.MusicLrcRowFormat;
import com.music.player.lib.exinterface.MusicLrcRowParser;
import com.music.player.lib.listener.MusicLrcParserCallBack;
import com.music.player.lib.listener.MusicLrcViewListener;
import com.music.player.lib.model.MusicDefaultLrcParser;
import com.music.player.lib.model.MusicDefsultLrcFormat;
import com.music.player.lib.util.MusicUtils;
import java.util.List;

/**
 * Created by TinyHung@outlook.com
 * 2019/5/23
 * Music Lrc
 */

public class MusicLrcView extends View{

    private static final String TAG = "MusicLrcView";
    //歌词解析器
    private MusicLrcRowFormat mLrcRowFormat;
    //逐行歌词解析器
    private MusicLrcRowParser mLrcRowParser;
    //歌词
    List<MusicLrcRow> mLrcRows;
    private String textNoimal="没有歌词";
    //歌词文本颜色
    private int textColor= Color.parseColor("#EEEEEE");
    //歌词高亮颜色
    private int textLightColor= Color.parseColor("#8000FF");
    //文本大小
    private float textSize;
    //文本最大大小
    private float textMaxSize;
    //拖动、高亮文字大小
    private float textLightSize;
    //歌词之间高度
    private float textLineHeight;
    //手指拖动时时间文字大小
    private float textTimeSize;
    //手指拖动时间文字颜色
    private int textTimeColor=Color.parseColor("#8000FF");
    //手指拖动歌词底部线条文字颜色
    private int buttomLineColorSize=Color.parseColor("#8000FF");
    //画笔
    private Paint mPaint;
    //持续拖动监听
    private MusicLrcViewListener mLrcViewListener;
    //正常歌词模式
    public final static int DISPLAY_MODE_NORMAL = 0;
    //拖动歌词模式
    public final static int DISPLAY_MODE_SEEK = 1;
    //缩放歌词模式
    public final static int DISPLAY_MODE_SCALE = 2;
    //歌词的当前展示模式
    private int mDisplayMode = DISPLAY_MODE_NORMAL;
    //最小移动的距离，当拖动歌词时如果小于该距离不做处理
    private int mMinSeekFiredOffset = 10;
    //当前高亮歌词的行数
    private int mHignlightRow = 0;
    //拖动歌词时，在当前高亮歌词下面的一条直线的起始位置
    private int mSeekLinePaddingX = 0;


    public MusicLrcView(Context context) {
        this(context,null);
    }

    public MusicLrcView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MusicLrcView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if(null!=attrs){
            TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.MusicLrcView);
            String string = typedArray.getString(R.styleable.MusicLrcView_musicLrcEmptyTips);
            if(!TextUtils.isEmpty(string)){
                textNoimal=string;
            }
            textColor = typedArray.getColor(R.styleable.MusicLrcView_musicLrcTextColor, Color.parseColor("#EEEEEE"));
            textLightColor = typedArray.getColor(R.styleable.MusicLrcView_musicLrcLightTextColor, Color.parseColor("#8000FF"));
            textSize=typedArray.getDimensionPixelSize(R.styleable.MusicLrcView_musicLrcTextSize,16);
            textMaxSize=typedArray.getDimensionPixelSize(R.styleable.MusicLrcView_musicLrcTextMaxSize,30);
            textLightSize=typedArray.getDimensionPixelSize(R.styleable.MusicLrcView_musicLrcLightTextSize,17);
            textLineHeight=typedArray.getDimensionPixelSize(R.styleable.MusicLrcView_musicLrcLineHeight,10);
            textTimeSize=typedArray.getDimensionPixelSize(R.styleable.MusicLrcView_musicLrcTimeTextSize,15);
            textTimeColor=typedArray.getColor(R.styleable.MusicLrcView_musicLrcTimeTextColor,Color.parseColor("#8000FF"));
            buttomLineColorSize=typedArray.getColor(R.styleable.MusicLrcView_musicLrcBottomLineColor,Color.parseColor("#8000FF"));
            typedArray.recycle();
        }else{
            textSize= MusicUtils.getInstance().dpToPxInt(getContext(),16f);
            textLightSize= MusicUtils.getInstance().dpToPxInt(getContext(),17f);
            textLineHeight= MusicUtils.getInstance().dpToPxInt(getContext(),10f);
        }
        mPaint=new Paint();
    }

    /**
     * 监听歌词滚动
     * @param lrcViewListener 监听器
     */
    public void setLrcViewListener(MusicLrcViewListener lrcViewListener) {
        mLrcViewListener = lrcViewListener;
    }

    /**
     * 设置歌词解析构造器
     * @param lrcRowFormat 歌词解析器
     */
    public void setLrcFormatConstructor(MusicLrcRowFormat lrcRowFormat){
        this.mLrcRowFormat=lrcRowFormat;
    }

    /**
     * 设置歌词逐行解析构造器
     * @param lrcRowParser 逐行歌词解析器
     */
    public void setLrcFormatConstructor(MusicLrcRowParser lrcRowParser){
        this.mLrcRowParser=lrcRowParser;
    }

    /**
     * 更新歌词文件
     * @param lrcString 源歌词文本
     */
    public void updateLrc(String lrcString){
        updateLrc(lrcString,mLrcRowFormat,mLrcRowParser);
    }

    /**
     * 更新歌词文件
     * @param lrcString 源歌词文本
     * @param lrcRowFormat 歌词解析器
     */
    public void updateLrc(String lrcString,MusicLrcRowFormat lrcRowFormat){
        updateLrc(lrcString,lrcRowFormat,mLrcRowParser);
    }

    /**
     * 更新歌词文件
     * @param lrcString 源歌词文本
     * @param lrcRowParser 逐行歌词解析器
     */
    public void updateLrc(String lrcString,MusicLrcRowParser lrcRowParser){
        updateLrc(lrcString,mLrcRowFormat,lrcRowParser);
    }

    /**
     * 更新歌词文件
     * @param lrcString 源歌词文本
     * @param lrcRowFormat 歌词解析器
     * @param lrcRowParser 逐行歌词解析器
     */
    public void updateLrc(String lrcString,MusicLrcRowFormat lrcRowFormat,MusicLrcRowParser lrcRowParser){
        if(null==lrcRowFormat){
            mLrcRowFormat=new MusicDefsultLrcFormat();
        }
        if(null==lrcRowParser){
            mLrcRowParser=new MusicDefaultLrcParser();
        }
        mLrcRowFormat.formatLrcFromString(lrcString, mLrcRowParser, new MusicLrcParserCallBack() {
            @Override
            public void onLrcRows(List<MusicLrcRow> lrcRows) {
                MusicLrcView.this.mLrcRows=lrcRows;
                invalidate();
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        if(null==mLrcRows||mLrcRows.size()==0){
            mPaint.setColor(textColor);
            mPaint.setTextSize(textSize);
            mPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(textNoimal,width/2,height/2,mPaint);
            return;
        }
        int rowY = 0;
        final int rowX = width / 2;
        int rowNum = 0;
        /**
         * 分以下三步来绘制歌词：
         *
         * 	第1步：高亮地画出正在播放的那句歌词
         *	第2步：画出正在播放的那句歌词的上面可以展示出来的歌词
         *	第3步：画出正在播放的那句歌词的下面的可以展示出来的歌词
         */
        // 1、 高亮地画出正在要高亮的的那句歌词
        String highlightText = mLrcRows.get(mHignlightRow).getContent();
        int highlightRowY = (int) (height / 2 - textSize);
        mPaint.setColor(textLightColor);
        mPaint.setTextSize(textSize);
        mPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(highlightText, rowX, highlightRowY, mPaint);

        // 上下拖动歌词的时候 画出拖动要高亮的那句歌词的时间 和 高亮的那句歌词下面的一条直线
        if (mDisplayMode == DISPLAY_MODE_SEEK) {
            // 画出高亮的那句歌词下面的一条直线
            mPaint.setColor(buttomLineColorSize);
            //该直线的x坐标从0到屏幕宽度  y坐标为高亮歌词和下一行歌词中间
            canvas.drawLine(mSeekLinePaddingX, highlightRowY + textLineHeight, width - mSeekLinePaddingX, highlightRowY + textLineHeight, mPaint);

            // 画出高亮的那句歌词的时间
            mPaint.setColor(textTimeColor);
            mPaint.setTextSize(textLineHeight);
            mPaint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(mLrcRows.get(mHignlightRow).getTimeText(), 0, highlightRowY, mPaint);
        }

        // 2、画出正在播放的那句歌词的上面可以展示出来的歌词
        mPaint.setColor(textColor);
        mPaint.setTextSize(textSize);
        mPaint.setTextAlign(Paint.Align.CENTER);
        rowNum = mHignlightRow - 1;
        rowY = (int) (highlightRowY - textLineHeight - textSize);
        //只画出正在播放的那句歌词的上一句歌词
//        if (rowY > -mLrcFontSize && rowNum >= 0) {
//            String text = mLrcRows.get(rowNum).content;
//            canvas.drawText(text, rowX, rowY, mPaint);
//        }

        //画出正在播放的那句歌词的上面所有的歌词
        while( rowY > -textSize && rowNum >= 0){
            String text = mLrcRows.get(rowNum).getContent();
            canvas.drawText(text, rowX, rowY, mPaint);
            rowY -=  (textLineHeight + textSize);
            rowNum --;
        }

        // 3、画出正在播放的那句歌词的下面的可以展示出来的歌词
        rowNum = mHignlightRow + 1;
        rowY = (int) (highlightRowY + textLineHeight + textSize);

        //只画出正在播放的那句歌词的下一句歌词
//        if (rowY < height && rowNum < mLrcRows.size()) {
//            String text2 = mLrcRows.get(rowNum).content;
//            canvas.drawText(text2, rowX, rowY, mPaint);
//        }

        //画出正在播放的那句歌词的所有下面的可以展示出来的歌词
        while( rowY < height && rowNum < mLrcRows.size()){
            String text = mLrcRows.get(rowNum).getContent();
            canvas.drawText(text, rowX, rowY, mPaint);
            rowY += (textLineHeight + textSize);
            rowNum ++;
        }
    }

    /**
     * 设置要高亮的歌词为第几行歌词
     *
     * @param position 要高亮的歌词行数
     * @param cb       是否是手指拖动后要高亮的歌词
     */
    public void seekLrc(int position, boolean cb) {
        if (mLrcRows == null || position < 0 || position > mLrcRows.size()) {
            return;
        }
        MusicLrcRow lrcRow = mLrcRows.get(position);
        mHignlightRow = position;
        invalidate();
        //如果是手指拖动歌词后
        if (mLrcViewListener != null && cb) {
            //回调onLrcSeeked方法，将音乐播放器播放的位置移动到高亮歌词的位置
            mLrcViewListener.onLrcSeeked(position, lrcRow);
        }
    }

    private float mLastMotionY;
    /**
     * 第一个手指的坐标
     **/
    private PointF mPointerOneLastMotion = new PointF();
    /**
     * 第二个手指的坐标
     **/
    private PointF mPointerTwoLastMotion = new PointF();
    /**
     * 是否是第一次移动，当一个手指按下后开始移动的时候，设置为true,
     * 当第二个手指按下的时候，即两个手指同时移动的时候，设置为false
     */
    private boolean mIsFirstMove = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mLrcRows == null || mLrcRows.size() == 0) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction()) {
            //手指按下
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "down,mLastMotionY:" + mLastMotionY);
                mLastMotionY = event.getY();
                mIsFirstMove = true;
                invalidate();
                break;
            //手指移动
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 2) {
                    Log.d(TAG, "two move");
                    doScale(event);
                    return true;
                }
                Log.d(TAG, "one move");
                // single pointer mode ,seek
                //如果是双指同时按下，进行歌词大小缩放，抬起其中一个手指，另外一个手指不离开屏幕地移动的话，不做任何处理
                if (mDisplayMode == DISPLAY_MODE_SCALE) {
                    //if scaling but pointer become not two ,do nothing.
                    return true;
                }
                //如果一个手指按下，在屏幕上移动的话，拖动歌词上下
                doSeek(event);
                break;
            case MotionEvent.ACTION_CANCEL:
                //手指抬起
            case MotionEvent.ACTION_UP:
                if (mDisplayMode == DISPLAY_MODE_SEEK) {
                    //高亮手指抬起时的歌词并播放从该句歌词开始播放
                    seekLrc(mHignlightRow, true);
                }
                mDisplayMode = DISPLAY_MODE_NORMAL;
                invalidate();
                break;
        }
        return true;
    }

    /**
     * 处理双指在屏幕移动时的，歌词大小缩放
     */
    private void doScale(MotionEvent event) {
        //如果歌词的模式为：拖动歌词模式
        if (mDisplayMode == DISPLAY_MODE_SEEK) {
            //如果是单指按下，在进行歌词上下滚动，然后按下另外一个手指，则把歌词模式从 拖动歌词模式 变为 缩放歌词模式
            mDisplayMode = DISPLAY_MODE_SCALE;
            Log.d(TAG, "change mode from DISPLAY_MODE_SEEK to DISPLAY_MODE_SCALE");
            return;
        }
        // two pointer mode , scale font
        if (mIsFirstMove) {
            mDisplayMode = DISPLAY_MODE_SCALE;
            invalidate();
            mIsFirstMove = false;
            //两个手指的x坐标和y坐标
            setTwoPointerLocation(event);
        }
        //获取歌词大小要缩放的比例
        int scaleSize = getScale(event);
        Log.d(TAG, "scaleSize:" + scaleSize);
        //如果缩放大小不等于0，进行缩放，重绘LrcView
        if (scaleSize != 0) {
            setNewFontSize(scaleSize);
            invalidate();
        }
        setTwoPointerLocation(event);
    }

    /**
     * 处理单指在屏幕移动时，歌词上下滚动
     */
    private void doSeek(MotionEvent event) {
        float y = event.getY();//手指当前位置的y坐标
        float offsetY = y - mLastMotionY; //第一次按下的y坐标和目前移动手指位置的y坐标之差
        //如果移动距离小于10，不做任何处理
        if (Math.abs(offsetY) < mMinSeekFiredOffset) {
            return;
        }
        //将模式设置为拖动歌词模式
        mDisplayMode = DISPLAY_MODE_SEEK;
        int rowOffset = (int) Math.abs((int) offsetY / textSize); //歌词要滚动的行数

        Log.d(TAG, "move to new hightlightrow : " + mHignlightRow + " offsetY: " + offsetY + " rowOffset:" + rowOffset);

        if (offsetY < 0) {
            //手指向上移动，歌词向下滚动
            mHignlightRow += rowOffset;//设置要高亮的歌词为 当前高亮歌词 向下滚动rowOffset行后的歌词
        } else if (offsetY > 0) {
            //手指向下移动，歌词向上滚动
            mHignlightRow -= rowOffset;//设置要高亮的歌词为 当前高亮歌词 向上滚动rowOffset行后的歌词
        }
        //设置要高亮的歌词为0和mHignlightRow中的较大值，即如果mHignlightRow < 0，mHignlightRow=0
        mHignlightRow = Math.max(0, mHignlightRow);
        //设置要高亮的歌词为0和mHignlightRow中的较小值，即如果mHignlight > RowmLrcRows.size()-1，mHignlightRow=mLrcRows.size()-1
        mHignlightRow = Math.min(mHignlightRow, mLrcRows.size() - 1);
        //如果歌词要滚动的行数大于0，则重画LrcView
        if (rowOffset > 0) {
            mLastMotionY = y;
            invalidate();
        }
    }

    /**
     * 设置当前两个手指的x坐标和y坐标
     */
    private void setTwoPointerLocation(MotionEvent event) {
        mPointerOneLastMotion.x = event.getX(0);
        mPointerOneLastMotion.y = event.getY(0);
        mPointerTwoLastMotion.x = event.getX(1);
        mPointerTwoLastMotion.y = event.getY(1);
    }

    /**
     * 设置缩放后的字体大小
     */
    private void setNewFontSize(int scaleSize) {
        //设置歌词缩放后的的最新字体大小
        textSize += scaleSize;
        textSize = Math.max(textSize, textSize);
        textSize = Math.min(textSize, textMaxSize);
        //设置歌词的最新字体大小
        textLineHeight += scaleSize;
        textLineHeight = Math.max(textLineHeight, textTimeSize);
        textLineHeight = Math.min(textLineHeight, textTimeSize);
    }

    /**
     * 获取歌词大小要缩放的比例
     */
    private int getScale(MotionEvent event) {
        Log.d(TAG, "scaleSize getScale");
        float x0 = event.getX(0);
        float y0 = event.getY(0);
        float x1 = event.getX(1);
        float y1 = event.getY(1);

        float maxOffset = 0; // max offset between x or y axis,used to decide scale size

        boolean zoomin = false;
        //第一次双指之间的x坐标的差距
        float oldXOffset = Math.abs(mPointerOneLastMotion.x - mPointerTwoLastMotion.x);
        //第二次双指之间的x坐标的差距
        float newXoffset = Math.abs(x1 - x0);

        //第一次双指之间的y坐标的差距
        float oldYOffset = Math.abs(mPointerOneLastMotion.y - mPointerTwoLastMotion.y);
        //第二次双指之间的y坐标的差距
        float newYoffset = Math.abs(y1 - y0);

        //双指移动之后，判断双指之间移动的最大差距
        maxOffset = Math.max(Math.abs(newXoffset - oldXOffset), Math.abs(newYoffset - oldYOffset));
        //如果x坐标移动的多一些
        if (maxOffset == Math.abs(newXoffset - oldXOffset)) {
            //如果第二次双指之间的x坐标的差距大于第一次双指之间的x坐标的差距则是放大，反之则缩小
            zoomin = newXoffset > oldXOffset ? true : false;
        }
        //如果y坐标移动的多一些
        else {
            //如果第二次双指之间的y坐标的差距大于第一次双指之间的y坐标的差距则是放大，反之则缩小
            zoomin = newYoffset > oldYOffset ? true : false;
        }
        Log.d(TAG, "scaleSize maxOffset:" + maxOffset);
        if (zoomin) {
            return (int) (maxOffset / 10);//放大双指之间移动的最大差距的1/10
        } else {
            return -(int) (maxOffset / 10);//缩小双指之间移动的最大差距的1/10
        }
    }

    /**
     * 设置歌词行集合
     * @param lrcRows
     */
    public void setLrc(List<MusicLrcRow> lrcRows) {
        mLrcRows = lrcRows;
        invalidate();
    }

    /**
     * 播放的时候调用该方法滚动歌词，高亮正在播放的那句歌词
     * @param time
     */
    public void seekLrcToTime(long time) {
        if (mLrcRows == null || mLrcRows.size() == 0) {
            return;
        }
        if (mDisplayMode != DISPLAY_MODE_NORMAL) {
            return;
        }
        Log.d(TAG, "seekLrcToTime:" + time);

        for (int i = 0; i < mLrcRows.size(); i++) {
            MusicLrcRow current = mLrcRows.get(i);
            MusicLrcRow next = i + 1 == mLrcRows.size() ? null : mLrcRows.get(i + 1);
            /**
             *  正在播放的时间大于current行的歌词的时间而小于next行歌词的时间， 设置要高亮的行为current行
             *  正在播放的时间大于current行的歌词，而current行为最后一句歌词时，设置要高亮的行为current行
             */
            if ((time >= current.getTime() && next != null && time < next.getTime())
                    || (time > current.getTime() && next == null)){
                seekLrc(i, false);
                return;
            }
        }
    }
}