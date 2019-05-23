package com.music.player.lib.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.RelativeLayout;
import com.music.player.lib.bean.MusicStatus;
import com.music.player.lib.listener.MusicAnimatorListener;
import com.music.player.lib.listener.MusicWindowClickListener;
import com.music.player.lib.manager.MusicPlayerManager;
import com.music.player.lib.manager.MusicWindowManager;
import com.music.player.lib.model.MusicWindowStyle;
import com.music.player.lib.util.MusicUtils;

/**
 * TinyHung@Outlook.com
 * 2019/3/12
 * Window MiniJukebox
 * 迷你悬浮窗播放窗口示例，通过观察者更新UI
 */

public class MusicWindowMiniJukebox extends RelativeLayout {

    private final Vibrator mVibrator;
    private WindowManager mWindowManager;
	private WindowManager.LayoutParams mParams;
	private MusicWindowClickListener mListener;
	private MusicJukeBoxViewSmall mJukeBoxViewSmall;
	//单击事件的有效像素
	public static int SCROLL_PIXEL=10;
	public int mViewWidth,mViewHeight;
	private int mStatusBarHeight, mNavigationHeight;
	//手指在屏幕上的实时X、Y坐标
	private static float xInScreen,yInScreen;
	//手指按下X、Y坐标
	private static float xDownInScreen,yDownInScreen;
	//手指按下此View在屏幕中X、Y坐标
	private static float xInView,yInView;
    //是否允许悬浮窗显示
	private boolean isVisible=true;
    private static int mScreenWidth,mScreenHeight;
    //垃圾桶所在屏幕Y轴位置
    private static int mTrashLocationY;
    //是否震动交互过
    private boolean isPlayVibrate=false;

    public MusicWindowMiniJukebox(Context context, WindowManager windowManager, MusicWindowClickListener listener) {
		super(context);
		this.mWindowManager = windowManager;
		this.mListener=listener;
        //悬浮窗样式
        MusicWindowStyle musicWindowStyle =MusicPlayerManager.getInstance().getWindownStyle();
        if(musicWindowStyle.equals(MusicWindowStyle.DEFAULT)){
            mViewWidth = MusicUtils.getInstance().dpToPxInt(context, 63f);
        }else if(musicWindowStyle.equals(MusicWindowStyle.TRASH)){
            mViewWidth = MusicUtils.getInstance().dpToPxInt(context,60f);
        }
        mViewHeight = mViewWidth;
		mJukeBoxViewSmall = new MusicJukeBoxViewSmall(context);
        LayoutParams layoutParams = new LayoutParams(mViewWidth, mViewWidth);
        this.addView(mJukeBoxViewSmall,layoutParams);
        mScreenWidth = MusicUtils.getInstance().getScreenWidth(context);
        mScreenHeight = MusicUtils.getInstance().getScreenHeight(context);
        mVibrator = (Vibrator)getContext().getSystemService(getContext().VIBRATOR_SERVICE);
    }

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			//XY坐标都需要除去状态栏高度
			xInView = event.getX();
			yInView = event.getY();
			xDownInScreen = event.getRawX();
			yDownInScreen = event.getRawY() - (getStatusBarHeight());
			xInScreen = event.getRawX();
			yInScreen = event.getRawY() - (getStatusBarHeight());
			break;
		case MotionEvent.ACTION_MOVE:
            xInScreen = event.getRawX();
			yInScreen = event.getRawY() - (getStatusBarHeight());
			//手指移动的时候更新小悬浮窗的位置
			updateViewPosition();
			if(MusicPlayerManager.getInstance().isTrashEnable()){
				//手势垃圾桶,在用户手指上下滑动10个像素触发垃圾桶
				if(Math.abs(xInScreen-xDownInScreen)>=SCROLL_PIXEL||Math.abs(yInScreen-yDownInScreen)>=SCROLL_PIXEL){
					int[] trashToWindown = MusicWindowManager.getInstance().
							addMiniJukeBoxTrashToWindown(getContext().getApplicationContext());
					if(null!=trashToWindown){
						MusicWindowManager.getInstance().startTrashWindowAnimation();
						//确定垃圾桶的顶部起点轴在屏幕中的Y轴位置
						this.mTrashLocationY=(mScreenHeight-trashToWindown[1]);
					}
					if(event.getRawY()>=mTrashLocationY){
						if(!isPlayVibrate&&null!=mVibrator){
							MusicWindowManager.getInstance().jukeBoxTrashFocusCap(true);
							isPlayVibrate=true;
							mVibrator.vibrate(50);
							MusicWindowManager.getInstance().startShakeAnimation();
						}
					}else{
						MusicWindowManager.getInstance().jukeBoxTrashFocusCap(false);
						isPlayVibrate=false;
					}
				}
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
		    //松手时手指在屏幕中的位置
            int rawX = (int) event.getRawX();
            int rawY = (int) event.getRawY();
            //手势垃圾桶
		    if(MusicPlayerManager.getInstance().isTrashEnable()){
				MusicWindowManager.getInstance().removeTrashFromWindown(getContext().getApplicationContext());
                //丢进垃圾桶
                if(isPlayVibrate){
                    MusicPlayerManager.getInstance().onStop();
                    MusicWindowManager.getInstance().removeMiniJukeBoxFromWindow(getContext().getApplicationContext());
                    isPlayVibrate=false;
                    return true;
                }
            }
            isPlayVibrate=false;
			int[] locations=new int[2];
			getLocationOnScreen(locations);
            //单击事件
            if (isVisible&&null!=mListener&&Math.abs(xInScreen - xDownInScreen) < SCROLL_PIXEL
					&& Math.abs(yInScreen - yDownInScreen) < SCROLL_PIXEL) {
                //取消事件只在默认样式生效
                if(MusicPlayerManager.getInstance().getWindownStyle().equals(MusicWindowStyle.DEFAULT)){
                    //删除按钮的位置：X：宽度的后1/3段像素内，Y：高度的前1/3段像素内
                    int closeStartX=locations[0]+mViewWidth/3*2;
                    int closeEndX=locations[0]+mViewWidth;
                    int closeStartY=locations[1];
                    int closeEndY=locations[1]+mViewHeight/3*1;
                    //Logger.d(TAG,"closeStartX:"+closeStartX+",closeEndX:"+closeEndX+",closeStartY:"+closeStartY+",closeEndY:"+closeEndY+",rawX:"+rawX+",rawY:"+rawY);
                    if(rawX>closeStartX&&rawX<closeEndX&&rawY>closeStartY&&rawY<closeEndY){
                        mListener.onWindownCancel(MusicWindowMiniJukebox.this);
                        //结束播放并移除自身
                        MusicPlayerManager.getInstance().onStop();
                        MusicWindowManager.getInstance().removeMiniJukeBoxFromWindow(getContext().getApplicationContext());
                        return true;
                    }
                }
                //悬浮窗整体单击事件
                mListener.onWindownClick(MusicWindowMiniJukebox.this,(Long) mJukeBoxViewSmall.getTag());
                return true;
            }
            //自动靠边吸附悬停
            if(MusicPlayerManager.getInstance().isWindownAutoScrollToEdge()){
                float eventRawX = event.getRawX();
				//缓慢吸附到屏幕边侧
				scrollToPixel(locations[0],locations[1]-getStatusBarHeight(), (int) eventRawX,350);
            }
			break;
		default:
			break;
		}
		return true;
	}


    /**
	 * 更新小窗口所在的位置
	 * @param params 小悬浮窗的参数
	 */
	public void setWindowManagerParams(WindowManager.LayoutParams params) {
		mParams = params;
	}

	/**
	 * 更新小悬浮窗在屏幕中的位置。
	 */
	private void updateViewPosition() {
		if(null!=mWindowManager){
			mParams.x = (int) (xInScreen - xInView);
			mParams.y = (int) (yInScreen - yInView);
			mWindowManager.updateViewLayout(this, mParams);
		}
	}

	/**
	 * 设置单击区间有效像素
	 * @param scrollPixel
	 */
	public void setScrollPixel(int scrollPixel) {
		this.SCROLL_PIXEL = scrollPixel;
	}

	/**
     * 自动吸附至屏幕位置
     * @param viewCurrentPixelX 悬浮窗当前所属位置X
     * @param viewCurrentPixelY 悬浮窗当前所属位置Y
     * @param currentRowX 手指松手所在的位置
     * @param scrollDurtion 滚动耗时
     */
    @SuppressLint("ObjectAnimatorBinding")
    private void scrollToPixel(int viewCurrentPixelX, final int viewCurrentPixelY, int currentRowX, int scrollDurtion) {
        int toPixelX=0;
        if(currentRowX>(mScreenWidth/2)){
            toPixelX=mScreenWidth;
        }
        //Logger.d(TAG,"scrollToPixel:pixelX:"+viewCurrentPixelX+",pixelY:"+viewCurrentPixelY+",
		// currentRowX:"+currentRowX+",scrollDurtion:"+scrollDurtion+",toPixelX:"+toPixelX);
        if(null!=mWindowManager){
            if(scrollDurtion<=0){
                mParams.x = toPixelX;
                mParams.y =  viewCurrentPixelY;
                mWindowManager.updateViewLayout(this, mParams);
                return;
            }
            ObjectAnimator objectAnimator = ObjectAnimator.ofInt(this, "number", viewCurrentPixelX, toPixelX);
            objectAnimator.setDuration(scrollDurtion);
            objectAnimator.setInterpolator(new AccelerateInterpolator());
            objectAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int animatedValue = (int) animation.getAnimatedValue();
                    mParams.x = animatedValue;
                    mParams.y =  viewCurrentPixelY;
                    mWindowManager.updateViewLayout(MusicWindowMiniJukebox.this, mParams);
                }
            });
            objectAnimator.start();
        }
    }

    /**
	 * 刷新数据
	 * @param musicStatus
	 */
	public void updateData(MusicStatus musicStatus) {
		if(null!=mJukeBoxViewSmall){
			mJukeBoxViewSmall.updateData(musicStatus);
		}
	}

	public int getViewWidth() {
		return mViewWidth;
	}

	public int getViewHeight() {
		return mViewHeight;
	}

	/**
	 * 获取状态栏高度
	 * @return
	 */
	private int getStatusBarHeight() {
		if (mStatusBarHeight == 0) {
			mStatusBarHeight =MusicUtils.getInstance().getStatusBarHeight(getContext());
		}
		return mStatusBarHeight;
	}

	/**
	 * 获取底部虚拟按键高度
	 * @return
	 */
	private int getNavigationHeight(){
		if (mNavigationHeight == 0) {
			mNavigationHeight =MusicUtils.getInstance().getNavigationHeight(getContext());
		}
		return mNavigationHeight;
	}

	public void onResume() {
		if(null!=mJukeBoxViewSmall){
			mJukeBoxViewSmall.onResume();
		}
	}

	public void onPause() {
		if(null!=mJukeBoxViewSmall){
			mJukeBoxViewSmall.onPause();
		}
	}

	/**
	 * 唱片机可见
	 */
	public void onVisible() {
		isVisible=true;
		if(null!=mJukeBoxViewSmall){
			mJukeBoxViewSmall.onVisible();
		}
		MusicWindowMiniJukebox.this.clearAnimation();
		showWindowAnimation(this);
	}

	/**
	 * 唱片机不可见
	 */
	public void onInvisible() {
		if(null!=mJukeBoxViewSmall){
			mJukeBoxViewSmall.onInvisible();
		}
		isVisible=false;
		MusicWindowMiniJukebox.this.clearAnimation();
		if(MusicWindowMiniJukebox.this.getVisibility()!=GONE){
			hideWindowAnimation(this, new MusicAnimatorListener() {
				@Override
				public void onAnimationStart() {

				}

				@Override
				public void onAnimationEnd() {
					MusicWindowMiniJukebox.this.setVisibility(GONE);
				}
			});
		}
	}

	/**
	 * 悬浮窗可见
	 * @param view
	 */
	public void showWindowAnimation(final View view){
		if(null==view) return;
		if(view.getVisibility()==VISIBLE) return;
		view.clearAnimation();
		AnimatorSet animatorSet=new AnimatorSet();
		ObjectAnimator animator1 = ObjectAnimator.ofFloat(view, "scaleX", 0.0f, 1.0f).setDuration(600);
		ObjectAnimator animator2 = ObjectAnimator.ofFloat(view, "scaleY", 0.0f, 1.0f).setDuration(600);
		ObjectAnimator animator3 = ObjectAnimator.ofFloat(view, "alpha", 1.0f, 1.0f).setDuration(1);
		animatorSet.playTogether(animator1,animator2,animator3);
		animatorSet.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				super.onAnimationStart(animation);
				view.setVisibility(VISIBLE);
			}
		});
		animatorSet.start();
	}

	/**
	 * 悬浮窗不可见
	 * @param view
	 * @param animatorListener
	 */
	public void hideWindowAnimation(final View view, final MusicAnimatorListener animatorListener){
		if(null==view) return;
		if(view.getVisibility()==GONE) return;
		view.clearAnimation();
		ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "alpha", 1.0f, 0.0f);
		objectAnimator.setDuration(160);
		objectAnimator.setInterpolator(new AccelerateInterpolator());
		objectAnimator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				if(null!=animatorListener){
					animatorListener.onAnimationEnd();
				}
			}
		});
		objectAnimator.start();
	}
	public void onDestroy() {
		isVisible=false;
		if(null!=mJukeBoxViewSmall){
			mJukeBoxViewSmall.onDestroy();
		}
		mListener=null;
	}
}