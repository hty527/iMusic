package com.video.player.lib.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;
import android.view.View;
import com.video.player.lib.constants.VideoConstants;
import com.video.player.lib.manager.VideoPlayerManager;
import com.video.player.lib.utils.Logger;

/**
 * TinyHung@Outlook.com
 * 2019/4/9
 * Video TextureView 画面渲染
 */

public class VideoTextureView extends TextureView {

    private static final String TAG = "VideoTextureView";
    public int mVideoWidth,mVideoHeight;

    public VideoTextureView(Context context) {
        this(context,null);
    }

    public VideoTextureView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public VideoTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 更新视频的实际宽高，根据缩放模式自适应显示
     * @param videoWidth
     * @param videoHeight
     */
    public void setVideoSize(int videoWidth, int videoHeight) {
        Logger.d(TAG,"setVideoSize-->videoWidth:"+videoWidth+",videoHeight:"+videoHeight);
        if(videoWidth!=mVideoWidth||videoHeight!=mVideoHeight){
            this.mVideoWidth =videoWidth;
            this.mVideoHeight =videoHeight;
            requestLayout();
        }
    }

    /**
     * 设置旋转角度
     * @param rotation
     */
    @Override
    public void setRotation(float rotation) {
        if (rotation != getRotation()) {
            super.setRotation(rotation);
            requestLayout();
        }
    }

    /**
     * 设置画面缩放模式
     * @param displayType 详见VideoConstants常量定义
     */
    public void setVideoDisplayType(int displayType) {
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int videoWidth = mVideoWidth;
        int videoHeight = mVideoHeight;
        //获取控件当前宽高
        int parentHeight = ((View) getParent()).getMeasuredHeight();
        int parentWidth = ((View) getParent()).getMeasuredWidth();
        if (parentWidth != 0 && parentHeight != 0 && videoWidth != 0 && videoHeight != 0) {
            //铺满延申至全屏，可能会有画面变形
            if (VideoConstants.VIDEO_DISPLAY_TYPE_PARENT==
                    VideoPlayerManager.getInstance().getVideoDisplayType()) {
                Logger.d(TAG,"缩放延伸");
                videoHeight = videoWidth * parentHeight / parentWidth;
            }
        }
        // 如果判断成立，则说明显示的TextureView和本身的位置是有90度的旋转的，所以需要交换宽高参数。
//        if (viewRotation == 90 || viewRotation == 270) {
//            int tempMeasureSpec = widthMeasureSpec;
//            widthMeasureSpec = heightMeasureSpec;
//            heightMeasureSpec = tempMeasureSpec;
//        }
        int width = getDefaultSize(videoWidth, widthMeasureSpec);
        int height = getDefaultSize(videoHeight, heightMeasureSpec);
        if (videoWidth > 0 && videoHeight > 0) {
            int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
            int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
            int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
            if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {
                // the size is fixed
                width = widthSpecSize;
                height = heightSpecSize;
                // for compatibility, we adjust size based on aspect ratio
                if (videoWidth * height < width * videoHeight) {
                    width = height * videoWidth / videoHeight;
                } else if (videoWidth * height > width * videoHeight) {
                    height = width * videoHeight / videoWidth;
                }
            } else if (widthSpecMode == MeasureSpec.EXACTLY) {
                // only the width is fixed, adjust the height to match aspect ratio if possible
                width = widthSpecSize;
                height = width * videoHeight / videoWidth;
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    height = heightSpecSize;
                    width = height * videoWidth / videoHeight;
                }
            } else if (heightSpecMode == MeasureSpec.EXACTLY) {
                // only the height is fixed, adjust the width to match aspect ratio if possible
                height = heightSpecSize;
                width = height * videoWidth / videoHeight;
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    width = widthSpecSize;
                    height = width * videoHeight / videoWidth;
                }
            } else {
                // neither the width nor the height are fixed, try to use actual video size
                width = videoWidth;
                height = videoHeight;
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // too tall, decrease both width and height
                    height = heightSpecSize;
                    width = height * videoWidth / videoHeight;
                }
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // too wide, decrease both width and height
                    width = widthSpecSize;
                    height = width * videoHeight / videoWidth;
                }
            }
        } else {
            // no size yet, just adopt the given spec sizes
        }
        if (parentWidth != 0 && parentHeight != 0 && videoWidth != 0 && videoHeight != 0) {
            //原始大小居中显示，不做任何裁剪和缩放
            if (VideoConstants.VIDEO_DISPLAY_TYPE_ORIGINAL==
                    VideoPlayerManager.getInstance().getVideoDisplayType()) {
                Logger.d(TAG,"原始大小");
                height = videoHeight;
                width = videoWidth;
                //缩放至控件宽高，会裁剪超出比例的画面
            } else if (VideoConstants.VIDEO_DISPLAY_TYPE_CUT==
                    VideoPlayerManager.getInstance().getVideoDisplayType()) {
                Logger.d(TAG,"裁剪铺满");
                if (videoHeight / videoWidth > parentHeight / parentWidth) {
                    height = parentWidth / width * height;
                    width = parentWidth;
                } else if (videoHeight / videoWidth < parentHeight / parentWidth) {
                    width = parentHeight / height * width;
                    height = parentHeight;
                }
             //缩放宽度至控件最大宽度，高度按比例缩放
            } else if (VideoConstants.VIDEO_DISPLAY_TYPE_ZOOM==
                    VideoPlayerManager.getInstance().getVideoDisplayType()) {
                Logger.d(TAG,"缩放延伸等比例");
                width=parentWidth;
                height = parentWidth / width * height;
            }
        }
        setMeasuredDimension(width, height);
    }
}