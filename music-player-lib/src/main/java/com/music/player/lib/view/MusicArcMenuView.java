package com.music.player.lib.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.music.player.lib.R;
import com.music.player.lib.util.Logger;
import com.music.player.lib.util.MusicUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2019/4
 * Arc Menu
 * 此自定义扇形菜单支持8个位置抛锚，9个方向展开的圆形弧形能力，支持两种配置方式，xml自定义属性和提供的set方法
 * left\right\top\bottom 这四种展开方向 角度需设置为180°，其他除center外，设置为90°即可
 * center 圆中心方向展开需设置为360°，若未指定360°，内部会自动矫正360°
 * arcGravity + arcMenuExpandOrientation + arcMenuAngle 配合使用
 */

public class MusicArcMenuView extends FrameLayout implements View.OnTouchListener {

    private static final String TAG = "ArcMenuView";
    //扇形菜单集
    private List<ImageView> mViews;
    //菜单对应resID
    private int[] resID;
    private boolean mMenuShowing=false;
    //圆的半径
    private float mRadius;
    //位于ParentView的锚点方位，默认：左上角
    private int mGravity = Gravity.TOP;
    //扇形菜单展开的方向,默认：向右(从上到下)
    private ArcPoint mPoint = ArcPoint.LEFT_TOP;
    //扇形菜单展开的弧度(扇形总角度)，默认90°
    private int mArcAngle =90;
    private ImageView mBtnImage;
    private Drawable mBtnDrawable;
    private int mExpandOpenDurtion;
    private int mExpandCloseDurtion;
    private boolean mCreateMenus;

    public MusicArcMenuView(Context context) {
        this(context,null);
    }

    public MusicArcMenuView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MusicArcMenuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setOnTouchListener(this);
        if(null!=attrs){
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MusicArcMenuView);
            //圆形半径
            mRadius = typedArray.getDimensionPixelSize(R.styleable.MusicArcMenuView_arcMenuRadius, 88);
            //扇形弧度
            mArcAngle = typedArray.getInteger(R.styleable.MusicArcMenuView_arcMenuAngle, 90);
            //菜单展开的方向
            int expandOrientation = typedArray.getInt(R.styleable.MusicArcMenuView_arcDropPosition,0);
            mPoint=getPoint(expandOrientation);
            mGravity = typedArray.getInt(R.styleable.MusicArcMenuView_arcGravity, 0x30);
            mExpandOpenDurtion = typedArray.getInteger(R.styleable.MusicArcMenuView_arcExpandOpenDurtion, 500);
            mExpandCloseDurtion = typedArray.getInteger(R.styleable.MusicArcMenuView_arcExpandCloseDurtion, 500);
            Drawable drawable = typedArray.getDrawable(R.styleable.MusicArcMenuView_arcBtnIcon);
            mBtnDrawable=drawable;
            mCreateMenus = typedArray.getBoolean(R.styleable.MusicArcMenuView_arcAutoCreateDefaultMenus, false);
            typedArray.recycle();
        }
        Logger.d(TAG,"mRadius:"+mRadius+",default:"+MusicUtils.getInstance().dpToPxInt(context,88f)+",gravity:"+mGravity);
        if(mRadius<=0){
            mRadius=MusicUtils.getInstance().dpToPxInt(context,88f);
        }
        if(mCreateMenus){
            createDeftultMenus();
        }
    }

    /**
     * 转换自定义的菜单展开方位
     * @param orientation
     * @return
     */
    private ArcPoint getPoint(int orientation) {
        switch (orientation) {
            case 0:
                return ArcPoint.LEFT_TOP;
            case 1:
                return ArcPoint.LEFT_BOTTOM;
            case 2:
                return ArcPoint.RIGHT_BOTTOM;
            case 3:
                return ArcPoint.RIGHT_TOP;
            case 4:
                return ArcPoint.CENTER;
            case 5:
                return ArcPoint.LEFT;
            case 6:
                return ArcPoint.TOP;
            case 7:
                return ArcPoint.RIGHT;
            case 8:
                return ArcPoint.BOTTOM;
        }
        return ArcPoint.LEFT_TOP;
    }

    /**
     * 菜单展开的方向
     */
    public enum ArcPoint{
        //左上角-->右(从上到下)
        LEFT_TOP,
        //左下角-->(从下到上)
        LEFT_BOTTOM,
        //右上角-->左(从下到上)
        RIGHT_BOTTOM,
        //右上角-->左(从下到上)
        RIGHT_TOP,
        //中心位置
        CENTER,
        //正左-->从左往右
        LEFT,
        //正下-->从上往下
        TOP,
        //正右-->从右往左
        RIGHT,
        //正下-->从下往上
        BOTTOM
    }

    /**
     * 设置菜单展开后的最大弧度(扇形圆形总角度)
     * @param arcAngle 1-360 之间
     */
    public void setArcAngle(int arcAngle){
        this.mArcAngle = arcAngle;
    }

    /**
     * 设置圆的半径
     * @param radius >0
     */
    public void setRadius(float radius){
        this.mRadius=radius;
    }

    /**
     * 设置菜单view在GroupView中所处的锚点方位
     * @param gravity 对应 View.Gravity定义常量
     */
    public void setPointGravity(int gravity){
        Logger.d(TAG,"setPointGravity-->gravity:"+gravity);
        this.mGravity=gravity;
    }

    /**
     * 设置扇形菜单展开的方向
     * @param point 参考ArcPoint定义
     */
    public void setDropPositionPoint(ArcPoint point) {
        this.mPoint=point;
    }

    /**
     * 设置菜单展开动画时长
     * @param expandOpenDurtion
     */
    public void setExpandOpenDurtion(int expandOpenDurtion) {
        mExpandOpenDurtion = expandOpenDurtion;
    }

    /**
     * 设置菜单关闭动画时长
     * @param expandCloseDurtion
     */
    public void setExpandCloseDurtion(int expandCloseDurtion) {
        mExpandCloseDurtion = expandCloseDurtion;
    }

    /**
     * 设置菜单按钮的Image
     * @param resource
     */
    public void setBtnImageResource(int resource){
        setBtnImageDrawable(ContextCompat.getDrawable(getContext(),resource));
    }

    public void setBtnImageDrawable(Drawable drawable){
        this.mBtnDrawable=drawable;
        if(null!=mBtnImage&&null!=drawable){
            mBtnImage.setImageDrawable(drawable);
        }
    }

    /**
     * 生成默认扇形菜单
     */
    public void createDeftultMenus(){
        createDeftultMenus(true);
    }

    /**
     * 生成扇形菜单元素
     * @param createRes
     */
    public void createDeftultMenus(boolean createRes) {
        this.removeAllViews();
        if(!createRes&&(null==resID||resID.length==0)){
            return;
        }
        int menuWidth = MusicUtils.getInstance().dpToPxInt(getContext(),43f);
        int margin = MusicUtils.getInstance().dpToPxInt(getContext(),10f);
        int marginTop = MusicUtils.getInstance().dpToPxInt(getContext(),5f);
        mViews=new ArrayList<>();
        if(createRes){
            resID=new int[]{R.drawable.ic_music_window_next,R.drawable.ic_music_window_last,R.drawable.ic_music_window_play,R.drawable.ic_music_window_home};
        }
        for (int i = 0; i < resID.length; i++) {
            ImageView imageView=new ImageView(getContext());
            imageView.setImageResource(resID[i]);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(menuWidth, menuWidth);
            layoutParams.gravity=mGravity;
            layoutParams.setMargins(marginTop,marginTop,0,0);
            imageView.setLayoutParams(layoutParams);
            imageView.setTag(i);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(null!=mOnItemClickListener){
                        mOnItemClickListener.onItemClick(v, (Integer) v.getTag());
                    }
                }
            });
            mViews.add(imageView);
            MusicArcMenuView.this.addView(imageView);
        }
        mBtnImage = new ImageView(getContext());
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(menuWidth+margin, menuWidth+margin);
        layoutParams.gravity=mGravity;
        if(null!=mBtnDrawable){
            Logger.d(TAG,"属性已设置");
            mBtnImage.setImageDrawable(mBtnDrawable);
        }else{
            mBtnImage.setImageResource(R.drawable.ic_music_default_arc_icon);
        }
        mBtnImage.setLayoutParams(layoutParams);
        mBtnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mMenuShowing){
                    closeArcMenus();
                }else{
                    showArcMenus();
                }
            }
        });
        MusicArcMenuView.this.addView(mBtnImage);
    }

    /**
     * 创建指定定长菜单
     * @param resIDs 菜单指定的res ICON
     */
    public void createMenus(int[] resIDs){
        resID=resIDs;
        if(null!=mViews){
            mViews.clear();
            mViews=null;
        }
        createDeftultMenus(false);
    }


    /**
     * 显示扇形菜单运动轨迹动画
     * 圆点坐标：startX:0 startY:0
     * 半径：mRadius
     * 角度：angle
     * 圆上(边)坐标 X:endX：,Y:endY
     * 计算公式:
     * endX = startX + mRadius * cos ( angle * (3.14 / 180) );
     * endY = startY + mRadius * sin ( angle * (3.14 / 180) );
     */
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private void showArcMenus() {
        mMenuShowing=true;
        if(null!=mViews){
            //圆心向四周扩散,此时0°和360°是重叠状态，此处添加一个占位View至数组，图层在0°位置之上
            if(mPoint.equals(ArcPoint.CENTER)){
                ImageView imageView = new ImageView(getContext());
                imageView.setVisibility(INVISIBLE);
                mViews.add(imageView);
                int avgAngle = 360 / (mViews.size() - 1);
                for (int i = 0; i < mViews.size(); i++) {
                    int angle = avgAngle * i ;
                    float endX= (float) (mRadius*Math.cos(angle*(Math.PI/180)));
                    float endY= (float) (mRadius*Math.sin(angle*(Math.PI/180)));
                    ObjectAnimator objectAnimatorX;
                    ObjectAnimator objectAnimatorY;
                    objectAnimatorX = ObjectAnimator.ofFloat(mViews.get(i), "translationX", 0, endX);
                    objectAnimatorY = ObjectAnimator.ofFloat(mViews.get(i), "translationY", 0, endY);
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.setDuration(mExpandOpenDurtion);
                    //设置同时播放x方向的位移动画和y方向的位移动画
                    animatorSet.play(objectAnimatorX).with(objectAnimatorY);
                    animatorSet.start();
                }
            }else{
                for (int i = 0; i < mViews.size(); i++) {
                    //每个Item的间隔角度
                    int avgAngle = (mArcAngle / (mViews.size() - 1));
                    //根据Item间隔角度计算当前Item真实的起始角度
                    int angle = 0;
                    //根据扩散的方向和每个菜单真实角度计算其运动终点X、Y坐标 endX = startX + mRadius * cos ( angle * (3.14 / 180) )
                    //往 左上角、左下角、正下、圆心 方向
                    if(mPoint.equals(ArcPoint.LEFT_TOP)||mPoint.equals(ArcPoint.CENTER)
                            ||mPoint.equals(ArcPoint.LEFT_BOTTOM)||mPoint.equals(ArcPoint.BOTTOM)){
                        angle = avgAngle * i;
                    //往 右上角 方向
                    }else if(mPoint.equals(ArcPoint.RIGHT_TOP)){
                        angle = avgAngle * i + 270;
                    //往 右下角、正右、正左 方向
                    }else if(mPoint.equals(ArcPoint.RIGHT_BOTTOM)||mPoint.equals(ArcPoint.RIGHT)||mPoint.equals(ArcPoint.LEFT)){
                        angle = avgAngle * i + 90;
                    //往 正下 方向
                    }else if(mPoint.equals(ArcPoint.TOP)){
                        angle = avgAngle * i+180;
                    }
                    float endX = 0,endY = 0;
                    //根据扩散的方向计算运动的终点
                    //往 左上角、圆心、正左 方向
                    if(mPoint.equals(ArcPoint.LEFT_TOP)||mPoint.equals(ArcPoint.CENTER)||mPoint.equals(ArcPoint.RIGHT)){
                        endX= (float) (mRadius*Math.cos(angle*(Math.PI/180)));
                        endY= (float) (mRadius*Math.sin(angle*(Math.PI/180)));
                    //往 左下角、右下角 方向
                    }else if(mPoint.equals(ArcPoint.LEFT_BOTTOM)||mPoint.equals(ArcPoint.RIGHT_BOTTOM)
                            ||mPoint.equals(ArcPoint.TOP)){
                        endX= (float) (mRadius*Math.cos(angle*(Math.PI/180)));
                        endY= (float) (mRadius*-Math.sin(angle*(Math.PI/180)));
                    //往 右上角、正上 方向
                    }else if(mPoint.equals(ArcPoint.RIGHT_TOP)||mPoint.equals(ArcPoint.BOTTOM)){
                        endX= (float) (mRadius*-Math.cos(angle*(Math.PI/180)));
                        endY= (float) (mRadius*-Math.sin(angle*(Math.PI/180)));
                    //往 正右方向
                    }else if(mPoint.equals(ArcPoint.LEFT)){
                        endX= (float) (mRadius*-Math.cos(angle*(Math.PI/180)));
                        endY= (float) (mRadius*Math.sin(angle*(Math.PI/180)));
                    }
                    Logger.d(TAG,"POINT："+mPoint+",INDEX："+i+",ANGLE:"+angle+",endX:"+endX+",endY:"+endY);
                    ObjectAnimator objectAnimatorX,objectAnimatorY;
                    objectAnimatorX = ObjectAnimator.ofFloat(mViews.get(i), "translationX", 0, endX);
                    objectAnimatorY = ObjectAnimator.ofFloat(mViews.get(i), "translationY", 0, endY);
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.setDuration(mExpandOpenDurtion);
                    //设置同时播放x方向的位移动画和y方向的位移动画
                    animatorSet.play(objectAnimatorX).with(objectAnimatorY);
                    animatorSet.start();
                }
            }
        }
    }

    /**
     * 关闭扇形菜单,其属性和角度和打开一致，只是起始位置做了调换
     */
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private void closeArcMenus() {
        mMenuShowing=false;
        if(null!=mViews){
            //先移除临时创建的View
            if(mPoint.equals(ArcPoint.CENTER)){
                for (int i = 0; i < mViews.size(); i++) {
                    int avgAngle = (360 / (mViews.size() - 1));
                    int angle = avgAngle * i;
                    float startX = (float) Math.cos(angle * (Math.PI / 180)) * mRadius;
                    float startY = (float) Math.sin(angle * (Math.PI / 180)) * mRadius;
                    ObjectAnimator objectAnimatorX;
                    ObjectAnimator objectAnimatorY;
                    objectAnimatorX = ObjectAnimator.ofFloat(mViews.get(i), "translationX", startX, 0);
                    objectAnimatorY = ObjectAnimator.ofFloat(mViews.get(i), "translationY", startY, 0);
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.setDuration(mExpandCloseDurtion);
                    animatorSet.play(objectAnimatorX).with(objectAnimatorY);
                    animatorSet.start();
                }
                mViews.remove(mViews.size()-1);
            }else{
                for (int i = 0; i < mViews.size(); i++) {
                    int avgAngle = (mArcAngle / (mViews.size() - 1));
                    int angle = 0;
                    if(mPoint.equals(ArcPoint.LEFT_TOP)||mPoint.equals(ArcPoint.CENTER)||mPoint.equals(ArcPoint.LEFT_BOTTOM)||mPoint.equals(ArcPoint.BOTTOM)){
                        angle = avgAngle * i;
                    }else if(mPoint.equals(ArcPoint.RIGHT_TOP)){
                        angle = avgAngle * i + 270;
                    }else if(mPoint.equals(ArcPoint.RIGHT_BOTTOM)||mPoint.equals(ArcPoint.RIGHT)||mPoint.equals(ArcPoint.LEFT)){
                        angle = avgAngle * i + 90;
                    }else if(mPoint.equals(ArcPoint.TOP)){
                        angle = avgAngle * i+180;
                    }
                    float startX = 0,startY = 0;
                    //计算扇形边界到圆点运动轨迹
                    if(mPoint.equals(ArcPoint.LEFT_TOP)||mPoint.equals(ArcPoint.CENTER)||mPoint.equals(ArcPoint.RIGHT)){
                        startX= (float) (mRadius*Math.cos(angle*(Math.PI/180)));
                        startY= (float) (mRadius*Math.sin(angle*(Math.PI/180)));
                    }else if(mPoint.equals(ArcPoint.LEFT_BOTTOM)||mPoint.equals(ArcPoint.RIGHT_BOTTOM)||mPoint.equals(ArcPoint.TOP)){
                        startX= (float) (mRadius*Math.cos(angle*(Math.PI/180)));
                        startY= (float) (mRadius*-Math.sin(angle*(Math.PI/180)));
                    }else if(mPoint.equals(ArcPoint.RIGHT_TOP)||mPoint.equals(ArcPoint.BOTTOM)){
                        startX= (float) (mRadius*-Math.cos(angle*(Math.PI/180)));
                        startY= (float) (mRadius*-Math.sin(angle*(Math.PI/180)));
                    }else if(mPoint.equals(ArcPoint.LEFT)){
                        startX= (float) (mRadius*-Math.cos(angle*(Math.PI/180)));
                        startY= (float) (mRadius*Math.sin(angle*(Math.PI/180)));
                    }
                    ObjectAnimator objectAnimatorX,objectAnimatorY;
                    Logger.d(TAG,"POINT："+mPoint+",INDEX："+i+",ANGLE:"+angle+",startX:"+startX+",startX:"+startY);
                    objectAnimatorX = ObjectAnimator.ofFloat(mViews.get(i), "translationX", startX, 0);
                    objectAnimatorY = ObjectAnimator.ofFloat(mViews.get(i), "translationY", startY, 0);
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.setDuration(mExpandCloseDurtion);
                    animatorSet.play(objectAnimatorX).with(objectAnimatorY);
                    animatorSet.start();
                }
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_DOWN&&mMenuShowing){
            closeArcMenus();
            return true;
        }
        return false;
    }

    public interface OnArcMenuClickListener{
        void onItemClick(View view,int index);
    }
    private OnArcMenuClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnArcMenuClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    /**
     * 对应生命周期调用
     */
    public void onDestroy(){
        if(null!=mViews){
            mViews.clear();
            mViews=null;
        }
        this.removeAllViews();
        mCreateMenus=false;
        mArcAngle=0;mBtnImage=null;mBtnDrawable=null;mExpandCloseDurtion=0;mExpandOpenDurtion=0;
        resID=null;mOnItemClickListener=null;mRadius=0;mMenuShowing=false;mGravity=0;mPoint=null;
    }
}