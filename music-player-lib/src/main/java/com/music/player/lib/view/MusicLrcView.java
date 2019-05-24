package com.music.player.lib.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.music.player.lib.R;
import com.music.player.lib.bean.MusicLrcRow;
import com.music.player.lib.iinterface.MusicLrcRowParser;
import com.music.player.lib.listener.MusicLrcParserCallBack;
import com.music.player.lib.listener.MusicLrcViewListener;
import com.music.player.lib.model.MusicDefaultLrcParser;
import com.music.player.lib.util.Logger;
import com.music.player.lib.util.MusicUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by TinyHung@outlook.com
 * 2019/5/23
 * Music Lrc
 * 默认歌词解析器为MusicDefaultLrcParser，如果需要实现自己的歌词解析
 * 请继承MusicLrcRowParser复写其中两个重要方法
 */

public class MusicLrcView extends View{

    private static final String TAG = "MusicLrcView";
    private Context mContext;
    //是否是正在加载中、歌词是否拦截触摸事件
    private boolean loadLrcing=false,mEnable=true;
    //歌词解析器
    private MusicLrcRowParser mLrcRowParser;
    //歌词
    private List<MusicLrcRow> mLrcRows;
    //为空、加载中
    private String textNoimal=null,textLoading=null;
    //歌词内存缓存
    private Map<String,List<MusicLrcRow>> cacheLrcRows;
    //歌词文本颜色、歌词高亮颜色、手指拖动时间文字颜色、手指拖动歌词底部线条文字颜色
    private int textColor,textLightColor,textTimeColor,buttomLineColor;
    //文本大小、高亮文字大小、时间字体大小、歌词之间高度
    private float textSize,textLightSize,textTimeSize,textLineHeight;
    //画笔
    private Paint mPaint;
    //持续拖动监听
    private MusicLrcViewListener mLrcViewListener;
    //正常歌词模式
    public final static int DISPLAY_MODE_NORMAL = 0;
    //拖动歌词模式
    public final static int DISPLAY_MODE_SEEK = 1;
    //歌词的当前展示模式
    private int mDisplayMode = DISPLAY_MODE_NORMAL;
    //当前高亮歌词的行数
    private int mHignlightRow = 0;
    //拖动歌词时，在当前高亮歌词下面的一条直线的起始位置
    private int mSeekLinePaddingX = 0;
    //手指拖动位置记录
    private float mLastMotionY,downX,downY;

    {
        cacheLrcRows=new HashMap<>();
        textNoimal="暂时没有找到歌词";
        textLoading="歌词获取中...";
    }

    public MusicLrcView(Context context) {
        this(context,null);
    }

    public MusicLrcView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MusicLrcView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext=context;
        if(null!=attrs){
            TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.MusicLrcView);
            String string = typedArray.getString(R.styleable.MusicLrcView_musicLrcEmptyTips);
            if(!TextUtils.isEmpty(string)){
                textNoimal=string;
            }
            textColor = typedArray.getColor(R.styleable.MusicLrcView_musicLrcTextColor,
                    Color.parseColor("#EAEAEA"));
            textLightColor = typedArray.getColor(R.styleable.MusicLrcView_musicLrcLightTextColor,
                    Color.parseColor("#FFFF00"));
            textSize=typedArray.getDimensionPixelSize(R.styleable.MusicLrcView_musicLrcTextSize,16);
            textTimeSize=typedArray.getDimensionPixelSize(R.styleable.MusicLrcView_musicLrcTimeTextSize,11);
            textLightSize=typedArray.getDimensionPixelSize(R.styleable.MusicLrcView_musicLrcLightTextSize,17);
            textLineHeight=typedArray.getDimensionPixelSize(R.styleable.MusicLrcView_musicLrcLineHeight,22);
            textTimeColor=typedArray.getColor(R.styleable.MusicLrcView_musicLrcTimeTextColor,
                    Color.parseColor("#BABDBC"));
            buttomLineColor =typedArray.getColor(R.styleable.MusicLrcView_musicLrcBottomLineColor,
                    Color.parseColor("#BCBCBC"));
            typedArray.recycle();
        }else{
            textSize= MusicUtils.getInstance().dpToPxInt(getContext(),16f);
            textTimeSize= MusicUtils.getInstance().dpToPxInt(getContext(),11f);
            textLightSize= MusicUtils.getInstance().dpToPxInt(getContext(),17f);
            textLineHeight= MusicUtils.getInstance().dpToPxInt(getContext(),22f);
            textColor=Color.parseColor("#EAEAEA");
            textLightColor=Color.parseColor("#FFFF00");
            textTimeColor=Color.parseColor("#BABDBC");
            buttomLineColor=Color.parseColor("#BCBCBC");
        }
        mPaint=new Paint();
        mPaint.setAntiAlias(true);
    }

    /**
     * 歌词滚动监听器
     * @param lrcViewListener 实现类
     */
    public void setLrcViewListener(MusicLrcViewListener lrcViewListener) {
        this.mLrcViewListener = lrcViewListener;
    }

    /**
     * 设置歌词解析器，支持网络、本地、或其他任意解析器哦
     * @param lrcRowParser 解析器
     */
    public void setLrcRowParser(MusicLrcRowParser lrcRowParser) {
        this.mLrcRowParser = lrcRowParser;
    }

    /**
     * 设置歌词为空时的占位字符串
     * @param textNoimal 占位提示符
     */
    public void setTextNoimal(String textNoimal) {
        this.textNoimal = textNoimal;
    }

    /**
     * 设置加载本地、网络歌词时提示文字
     * @param textLoading 加载提示符
     */
    public void setTextLoading(String textLoading) {
        this.textLoading = textLoading;
    }

    /**
     * 设置常规歌词文字颜色
     * @param textColor 常规歌词文字颜色
     */
    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    /**
     * 设置常规歌词文字大小
     * @param textSize 常规歌词文字大小
     */
    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    /**
     * 设置选中的歌词文字颜色
     * @param textLightColor 高亮歌词文字颜色
     */
    public void setTextLightColor(int textLightColor) {
        this.textLightColor = textLightColor;
    }

    /**
     * 设置高亮歌词文字大小
     * @param textLightSize 高亮歌词文字大小
     */
    public void setTextLightSize(float textLightSize) {
        this.textLightSize = textLightSize;
    }

    /**
     * 设置拖动歌词时的时间文字颜色
     * @param textTimeColor 时间文字颜色
     */
    public void setTextTimeColor(int textTimeColor) {
        this.textTimeColor = textTimeColor;
    }

    /**
     * 设置拖动歌词时的歌词文字大小
     * @param textTimeSize 时间文字大小
     */
    public void setTextTimeSize(float textTimeSize) {
        this.textTimeSize = textTimeSize;
    }

    /**
     * 设置拖动歌词时的歌词下划线颜色
     * @param buttomLineColor 歌词下划线颜色
     */
    public void setButtomLineColor(int buttomLineColor) {
        this.buttomLineColor = buttomLineColor;
    }

    /**
     * 设置歌词之间的行高
     * @param textLineHeight 歌词之间行高
     */
    public void setTextLineHeight(float textLineHeight) {
        this.textLineHeight = textLineHeight;
    }

    /**
     * 更新歌词文件
     * @param lrcString 源歌词文本
     */
    public void setLrcRow(String lrcString){
        setLrcRow(null,lrcString,mLrcRowParser);
    }

    /**
     * 更新歌词文件
     * @param audioID 音频ID，存取内存缓存用，避免重复IO
     * @param lrcString 源歌词文本
     */
    public void setLrcRow(String audioID,String lrcString){
        setLrcRow(audioID,lrcString,mLrcRowParser);
    }

    /**
     * 更新歌词文件
     * @param audioID 音频ID，存取内存缓存用，避免重复IO
     * @param lrcString 源歌词文本
     * @param lrcRowParser 逐行歌词解析器
     */
    public void setLrcRow(final String audioID, String lrcString, MusicLrcRowParser lrcRowParser){
        if(null==lrcRowParser){
            mLrcRowParser=new MusicDefaultLrcParser();
        }
        loadLrcRows(mLrcRowParser,audioID,lrcString,textLoading,false);
    }

    /**
     * 更新网络歌词文件
     * @param lrcRowParser 解析器
     * @param audioID 歌曲ID
     * @param hashKey 酷狗音乐唯一标识
     * @param loadingTips 加载中提示内容
     */
    public void setNetLrcRow(MusicLrcRowParser lrcRowParser, final String audioID, String hashKey,
                             String loadingTips) {
        loadLrcRows(lrcRowParser,audioID,hashKey,loadingTips,true);
    }

    /**
     * 开始加载本地、网络歌词文件
     * @param lrcRowParser 歌词解析器
     * @param audioID 音频ID
     * @param object 如果是加载网络歌词，lrcString为歌曲唯一标识
     * @param textLoading 加载中提示文字
     * @param isNet 是否加载来自网络的? true:网络歌词
     * 注意：没错，你看到的这里的代码加载网络和加载本地的入口个方法都是一模一样的，关键就在解析器，
     * 内部内置了一个默认的解析器，MusicDefaultLrcParser，如果无法满足你的解析需求，
     * 请继承MusicLrcRowParser重写两个重要的方法！！！
     */
    private void loadLrcRows(MusicLrcRowParser lrcRowParser, final String audioID, String object,
                             String textLoading, boolean isNet) {
        //如果已经绘制了不做任何处理
        if(null!=mLrcRows&&mLrcRows.size()>0){
            return;
        }
        this.textLoading=textLoading;
        loadLrcing=true;
        //优先从缓存获取
        if(!TextUtils.isEmpty(audioID)&&null!=cacheLrcRows&&null!=cacheLrcRows.get(audioID)){
            loadLrcing=false;
            List<MusicLrcRow> lrcRowList = cacheLrcRows.get(audioID);
            if(null==mLrcRows){
                mLrcRows=new ArrayList<>();
            }
            mLrcRows.clear();
            mLrcRows.addAll(lrcRowList);
            Logger.d(TAG,"loadLrcRows-->使用内部缓存");
            invalidate();
            return;
        }
        invalidate();
        //从本地、网络加载
        lrcRowParser.formatLrc(object, new MusicLrcParserCallBack() {
            @Override
            public void onLrcRows(List<MusicLrcRow> lrcRows) {
                loadLrcing=false;
                if(!TextUtils.isEmpty(audioID)&&null!=lrcRows){
                    if(null==cacheLrcRows){
                        cacheLrcRows=new HashMap<>();
                    }
                    cacheLrcRows.put(audioID,lrcRows);
                }
                if(null==mLrcRows){
                    mLrcRows=new ArrayList<>();
                }
                mLrcRows.clear();
                if(null!=lrcRows){
                    mLrcRows.addAll(lrcRows);
                }
                invalidate();
            }
        });
    }

    /**
     * 更新歌词文件
     * @param lrcRows 已经解析好的歌词实体数组
     */
    public void setLrcRow(List<MusicLrcRow> lrcRows){
        if(null==mLrcRows){
            mLrcRows=new ArrayList<>();
        }
        mLrcRows.clear();
        if(null!=lrcRows){
            mLrcRows.addAll(lrcRows);
        }
        invalidate();
    }

    /**
     * 歌词控件是否可用
     * @param enable true:正常 false:控件将不可拖动，不会同步更新歌词显示位置
     */
    public void setEnable(boolean enable) {
        this.mEnable=enable;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        //加载中
        if(loadLrcing){
            mPaint.setColor(textColor);
            mPaint.setTextSize(textSize);
            mPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(textLoading,width/2,height/2,mPaint);
            return;
        }
        //歌词为空
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
         * 	第1步：高亮地画出正在播放的那句歌词
         *	第2步：画出正在播放的那句歌词的上面可以展示出来的歌词
         *	第3步：画出正在播放的那句歌词的下面的可以展示出来的歌词
         */
        // 1、 高亮地画出正在要高亮的的那句歌词
        String highlightText = mLrcRows.get(mHignlightRow).getContent();
        int highlightRowY = (int) (height / 2 - textLightSize);
        mPaint.setColor(textLightColor);
        mPaint.setTextSize(textLightSize);
        mPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(highlightText, rowX, highlightRowY, mPaint);
        // 上下拖动歌词的时候 画出拖动要高亮的那句歌词的时间 和 高亮的那句歌词下面的一条直线
        if (mDisplayMode == DISPLAY_MODE_SEEK) {
            // 画出高亮的那句歌词下面的一条直线
            mPaint.setColor(buttomLineColor);
            //该直线的x坐标从0到屏幕宽度  y坐标为高亮歌词和下一行歌词中间的1/3
            float lineOffset = textLineHeight / 3;
            //时间位于控件的右侧
            float leftOffset=width-MusicUtils.getInstance().dpToPxInt(getContext(),55f);
            canvas.drawLine(mSeekLinePaddingX, highlightRowY + lineOffset, width
                    - mSeekLinePaddingX, highlightRowY + lineOffset, mPaint);
            // 画出高亮的那句歌词的时间，绘制在屏幕右侧
            mPaint.setColor(textTimeColor);
            mPaint.setTextSize(textTimeSize);
            mPaint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(mLrcRows.get(mHignlightRow).getTimeText(), leftOffset, highlightRowY , mPaint);
        }
        // 2、画出正在播放的那句歌词的上面可以展示出来的歌词
        mPaint.setColor(textColor);
        mPaint.setTextSize(textSize);
        mPaint.setTextAlign(Paint.Align.CENTER);
        rowNum = mHignlightRow - 1;
        rowY = (int) (highlightRowY - textLineHeight - textSize);
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
     * @param position 要高亮的歌词行数
     * @param isTouch  是否是手指拖动的事件
     */
    private void seekLrc(int position, boolean isTouch) {
        Logger.d(TAG,"seekLrc-->position:"+position+",isTouch:"+isTouch);
        if (mLrcRows == null || position < 0 || position > mLrcRows.size()-1) {
            return;
        }
        MusicLrcRow lrcRow = mLrcRows.get(position);
        //标记高亮行，重绘界面
        mHignlightRow = position;
        invalidate();
        //如果是手指拖动歌词后
        if (mLrcViewListener != null && isTouch) {
            //回调onLrcSeeked方法，将音乐播放器播放的位置移动到高亮歌词的位置
            mLrcViewListener.onLrcSeeked(position, lrcRow);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if(!mEnable){
            return false;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!mEnable){
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionY = event.getY();
                downX = event.getX();
                downY=event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if(null!=mLrcRows&&mLrcRows.size()>0){
                    doSeek(event);
                }
                break;
            case MotionEvent.ACTION_UP:
                mDisplayMode = DISPLAY_MODE_NORMAL;
                //如果用户手指并没有明显的拖动，则抛出单击事件
                if(Math.abs(event.getX()-downX) < 5 && Math.abs(event.getY()-downY) < 5){
                    if(null!=mLrcViewListener){
                        mLrcViewListener.onClick(MusicLrcView.this);
                    }
                }else{
                    if(null!=mLrcRows&&mLrcRows.size()>0){
                        seekLrc(mHignlightRow, true);
                        invalidate();
                    }
                }
                downX=0;downY=0;
                break;
        }
        return true;
    }

    /**
     * 处理单指在屏幕移动时，歌词上下滚动
     */
    private void doSeek(MotionEvent event) {
        //将模式设置为拖动歌词模式
        mDisplayMode = DISPLAY_MODE_SEEK;
        float y = event.getY();//手指当前位置的y坐标
        float offsetY = y - mLastMotionY; //第一次按下的y坐标和目前移动手指位置的y坐标之差
        int rowOffset = (int) Math.abs((int) offsetY / textSize); //歌词要滚动的行数
        //Logger.d(TAG, "doSeek-->hightlightrow : " + mHignlightRow + " offsetY: " + offsetY + " rowOffset:" + rowOffset);
        if (offsetY < 0) {
            //手指向上移动，歌词向下滚动
            mHignlightRow += rowOffset;//设置要高亮的歌词为 当前高亮歌词 向下滚动rowOffset行后的歌词
        } else if (offsetY > 0) {
            //手指向下移动，歌词向上滚动
            mHignlightRow -= rowOffset;//设置要高亮的歌词为 当前高亮歌词 向上滚动rowOffset行后的歌词
        }
        //设置要高亮的歌词为0和mHignlightRow中的较大值，即如果mHignlightRow < 0，mHignlightRow=0
        mHignlightRow = Math.max(0, mHignlightRow);
        //设置要高亮的歌词为0和mHignlightRow中的较小值，即如果mHignlight > RowmLrcRows.size()-1，
        // mHignlightRow=mLrcRows.size()-1
        mHignlightRow = Math.min(mHignlightRow, mLrcRows.size() - 1);
        //如果歌词要滚动的行数大于0，则重画LrcView
        if (rowOffset > 0) {
            mLastMotionY = y;
            invalidate();
        }
    }

    /**
     * 实时跳转至指定位置，更新高亮的行
     * @param currentDurtion 音频的时间位置，单位毫秒
     */
    public void seekTo(long currentDurtion) {
        if(mEnable&&null!=mLrcRows&&mLrcRows.size()>0){
            if (mDisplayMode != DISPLAY_MODE_NORMAL) {
                return;
            }
            for (int i = 0; i < mLrcRows.size(); i++) {
                MusicLrcRow current = mLrcRows.get(i);
                MusicLrcRow next = i + 1 == mLrcRows.size() ? null : mLrcRows.get(i + 1);
                //条件1：满足当前播放的时间>当前随机对象的时间&&当前播放的时间<下个随机对象的时间
                //条件2：满足当前播放的时间>当前随机对象的时间&&已经到达最后一个随机对象了
                if ((currentDurtion >= current.getTime() && next != null && currentDurtion < next.getTime())
                        || (currentDurtion > current.getTime() && next == null)){
                    seekLrc(i, false);
                    return;
                }
            }
        }
    }

    /**
     * 还原，但不清除设置
     */
    public void onReset(){
        if(null!=mLrcRows){
            mLrcRows.clear();
            mLrcRows=null;
        }
        loadLrcing=false;
        invalidate();
    }

    /**
     * 对应生命周期调用
     */
    public void onDestroy(){
        mContext=null;
        if(null!=mLrcRowParser){
            mLrcRowParser.onDestroy();
            mLrcRowParser=null;
        }
        if(null!=mLrcRows){
            mLrcRows.clear();
            mLrcRows=null;
        }
        invalidate();
    }
}