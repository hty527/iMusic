package com.video.player.lib.view;

import android.content.Context;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import com.video.player.lib.utils.VideoUtils;

/**
 * TinyHung@Outlook.com
 * 2019/4/12
 * Window VideoPlayer
 * 允许悬浮窗上至状态栏、下至虚拟导航栏都能拖拽显示
 */

public class VideoWindowPlayerGroup extends FrameLayout{

	private static final String TAG = "VideoWindowPlayerGroup";
    private WindowManager mWindowManager;
	private WindowManager.LayoutParams mParams;
	private int mStatusBarHeight;
	//手指在屏幕上的实时X、Y坐标
	private static float xInScreen,yInScreen;
	//手指按下此View在屏幕中X、Y坐标
	private static float xInView,yInView;

	public VideoWindowPlayerGroup(Context context, WindowManager windowManager) {
		super(context);
		this.mWindowManager = windowManager;
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//			setOutlineProvider(new VideoTextrueProvider(VideoUtils.getInstance().dpToPxInt(context,5f)));
//		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				//XY坐标都需要除去状态栏高度
				xInView = event.getX();
				yInView = event.getY();
				break;
			case MotionEvent.ACTION_MOVE:
				xInScreen = event.getRawX();
				yInScreen = event.getRawY()-getStatusBarHeight();
				//手指移动的时候更新小悬浮窗的位置
				updateViewPosition();
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
	 * 获取状态栏高度
	 * @return
	 */
	private int getStatusBarHeight() {
		if (mStatusBarHeight == 0) {
			mStatusBarHeight = VideoUtils.getInstance().getStatusBarHeight(getContext());
		}
		return mStatusBarHeight;
	}

	public void onDestroy() {
		this.removeAllViews();
		mParams=null;
	}
}