package com.music.player.lib.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.RelativeLayout;
import com.music.player.lib.bean.MusicStatus;
import com.music.player.lib.constants.MusicConstants;
import com.music.player.lib.listener.MusicAnimatorListener;
import com.music.player.lib.manager.MusicPlayerManager;
import com.music.player.lib.manager.MusicWindowManager;
import com.music.player.lib.util.Logger;
import com.music.player.lib.util.MusicUtils;

/**
 * TinyHung@Outlook.com
 * 2019/3/12
 * Window MiniJukebox
 * 迷你悬浮窗容器，只处理用户交互
 */

public class MusicWindowMiniJukebox extends RelativeLayout {

	private static final String TAG = "MusicWindowMiniJukebox";
	private final Vibrator mVibrator;
    private WindowManager mWindowManager;
	private WindowManager.LayoutParams mParams;
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
    //垃圾桶所在屏幕X,Y轴位置
    private static int mTrashLocationX,mTrashLocationY;
    //是否震动交互过
    private boolean isPlayVibrate=false;
    //悬浮球停靠在屏幕边上的边界大小
    private final int mMaginBorder;

    public MusicWindowMiniJukebox(Context context, WindowManager windowManager) {
		super(context);
		this.mWindowManager = windowManager;
		mViewWidth = MusicUtils.getInstance().dpToPxInt(context,60f);
        mViewHeight = mViewWidth;
		mJukeBoxViewSmall = new MusicJukeBoxViewSmall(context);
        LayoutParams layoutParams = new LayoutParams(mViewWidth, mViewWidth);
        this.addView(mJukeBoxViewSmall,layoutParams);
        mScreenWidth = MusicUtils.getInstance().getScreenWidth(context);
        mScreenHeight = MusicUtils.getInstance().getScreenHeight(context);
        mVibrator = (Vibrator)getContext().getSystemService(getContext().VIBRATOR_SERVICE);
        //停靠边界
        mMaginBorder = MusicUtils.getInstance().dpToPxInt(getContext(), 15f);
    }


    @Override
	public boolean onTouchEvent(MotionEvent event) {
		Logger.d(TAG,"onTouchEvent-->"+event.getAction());
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
			showTrachWindow(event);
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
		    actionTouchUp(event);
			break;
		default:
			break;
		}
		return true;
	}

	/**
	 * 悬浮窗交互
	 * @param event 手势事件
	 */
	private void showTrachWindow(MotionEvent event) {
		//手势垃圾桶,在用户手指上下滑动10个像素触发垃圾桶
		if(Math.abs(xInScreen-xDownInScreen)>=SCROLL_PIXEL||Math.abs(yInScreen-yDownInScreen)>=SCROLL_PIXEL){
            int[] toWindown = MusicWindowManager.getInstance().
                    addMiniJukeBoxTrashToWindown(getContext().getApplicationContext());
            //返回悬浮窗控件的size数组或者悬浮窗对象本身
            if(null!=toWindown){
                mTrashLocationX=toWindown[0];
                mTrashLocationY=toWindown[1];
                MusicWindowManager.getInstance().startTrashWindowAnimation();
            }
            int rawX = (int) event.getRawX();
            int rawY = (int) event.getRawY();
            if(rawX>(mScreenWidth-mTrashLocationX)&&rawY>(mScreenHeight-mTrashLocationY)){
                if(!isPlayVibrate&&null!=mVibrator){
                    isPlayVibrate=true;
                    MusicWindowManager.getInstance().jukeBoxTrashFocusCap(true);
                    mVibrator.vibrate(50);
                    MusicWindowManager.getInstance().startShakeAnimation();
                }
            }else{
                MusicWindowManager.getInstance().jukeBoxTrashFocusCap(false);
                isPlayVibrate=false;
            }
		}
	}

//    /**
//     * 悬浮窗交互
//     * @param event 手势事件
//     */
//    private void showTrachWindow(MotionEvent event) {
//        //手势垃圾桶,在用户手指上下滑动10个像素触发垃圾桶
//        if(Math.abs(xInScreen-xDownInScreen)>=SCROLL_PIXEL||Math.abs(yInScreen-yDownInScreen)>=SCROLL_PIXEL){
//            Object object = MusicWindowManager.getInstance().
//                    addMiniJukeBoxTrashToWindown(getContext().getApplicationContext());
//            //返回悬浮窗控件的size数组或者悬浮窗对象本身
//            if(null!=object){
//                if(object instanceof int[]){
//                    int[] size= (int[]) object;
//                    mTrashLocationX=size[0];
//                    mTrashLocationY=size[1];
//                }else if(object instanceof MusicWindowTrash){
//                    mMusicWindowTrash= (MusicWindowTrash) object;
//                }
//                //仅当悬浮窗本身为空，才显示悬浮窗出来
//                if(null== mMusicWindowTrash){
//                    MusicWindowManager.getInstance().startTrashWindowAnimation();
//                }
//                int rawX = (int) event.getRawX();
//                int rawY = (int) event.getRawY();
//                if(rawX>(mScreenWidth-mTrashLocationX)&&rawY>(mScreenHeight-mTrashLocationY)){
//                    if(null!=mMusicWindowTrash&&null!=mMusicWindowTrash.getRegion()){
//                        if(!isPlayVibrate&&null!=mVibrator){
//                            Region region = mMusicWindowTrash.getRegion();
//                            boolean contains = region.contains(rawX, rawY);
//                            Logger.d(TAG,"X:"+rawX+",Y:"+rawY+",viewX"+(mScreenWidth-mTrashLocationX)+",viewY:"+(mScreenHeight-mTrashLocationY)+",contains:"+contains);
//                            if(!contains){
//                                isPlayVibrate=true;
//                                MusicWindowManager.getInstance().jukeBoxTrashFocusCap(true);
//                                mVibrator.vibrate(50);
//                                MusicWindowManager.getInstance().startShakeAnimation();
//                            }else{
//                                MusicWindowManager.getInstance().jukeBoxTrashFocusCap(false);
//                                isPlayVibrate=false;
//                            }
//                        }
//                    }else{
//                        MusicWindowManager.getInstance().jukeBoxTrashFocusCap(false);
//                        isPlayVibrate=false;
//                    }
//                }else{
//                    MusicWindowManager.getInstance().jukeBoxTrashFocusCap(false);
//                    isPlayVibrate=false;
//                }
//            }
//        }
//    }

    /**
     * 松手事件处理
     * @param event 手势事件
     * @return 是否消费了
     */
    private boolean actionTouchUp(MotionEvent event) {
		MusicWindowManager.getInstance().removeTrashFromWindown(getContext().getApplicationContext());
		//丢进垃圾桶
		if(isPlayVibrate){
			MusicPlayerManager.getInstance().onStop();
			MusicWindowManager.getInstance().removeMiniJukeBoxFromWindow(getContext().getApplicationContext());
			isPlayVibrate=false;
			return true;
		}
        isPlayVibrate=false;
        int[] locations=new int[2];
        getLocationOnScreen(locations);
        //单击事件
        if (isVisible&&Math.abs(xInScreen - xDownInScreen) < SCROLL_PIXEL
                && Math.abs(yInScreen - yDownInScreen) < SCROLL_PIXEL) {
            //前往播放器界面
			String activityName = MusicPlayerManager.getInstance().getPlayerActivityName();
            if(!TextUtils.isEmpty(activityName)){
				Context context = getContext().getApplicationContext();
				Intent startIntent=new Intent();
				startIntent.setClassName(context.getPackageName(),activityName);
				startIntent.putExtra(MusicConstants.KEY_MUSIC_ID, (Long) mJukeBoxViewSmall.getTag());
				startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(startIntent);
			}
            return true;
        }
        //自动靠边吸附悬停
		float eventRawX = event.getRawX();
		//缓慢吸附到屏幕边侧
		scrollToPixel(locations[0],locations[1]-getStatusBarHeight(), (int) eventRawX,350);
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
            int startX = (int) (xInScreen - xInView);
            if(startX<mMaginBorder){
                startX=mMaginBorder;
            }else if(startX>(mScreenWidth-mViewWidth-mMaginBorder)){
                startX=(mScreenWidth-mViewWidth-mMaginBorder);
            }
            mParams.x = startX;
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
        int toPixelX=mMaginBorder;
        if(currentRowX>(mScreenWidth/2)){
            //左边停靠最大X：屏幕宽-自身宽-边距大小
            toPixelX=(mScreenWidth-mViewWidth-mMaginBorder);
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
	 * 唱片机可见
	 * @param audioID 音频ID
	 */
	public void onVisible(long audioID) {
		isVisible=true;
		if(null!=mJukeBoxViewSmall){
			if(audioID>0){
				mJukeBoxViewSmall.setTag(audioID);
			}
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
	}
}