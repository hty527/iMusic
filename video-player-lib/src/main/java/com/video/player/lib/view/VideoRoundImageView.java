package com.video.player.lib.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.TypedValue;
import com.video.player.lib.R;

/**
 * TinyHung@Outlook.com
 * 2018/9/1
 * 自定义圆角View
 */

public class VideoRoundImageView extends AppCompatImageView {

    public static final int TYPE_CIRCLE = 0;
    public static final int TYPE_ROUND = 1;

    public static final int SCALE_TYPE_CROP = 0;
    public static final int SCALE_TYPE_FIT = 1;

    private static final int BODER_RADIUS_DEFAULT = 10;

    private static final String STATE_INSTANCE = "state_instance";
    private static final String STATE_TYPE = "state_type";
    private static final String STATE_BORDER_RADIUS = "state_border_radius";

    // 左上角是否为圆角
    private boolean corners_top_left = true;

    // 右上角是否为圆角
    private boolean corners_top_right = true;

    // 左下角是否为圆角
    private boolean corners_bottom_left = true;

    // 右下角是否为圆角
    private boolean corners_bottom_right = true;

    // 显示类型，可选项：圆形，圆角矩形
    private int type = TYPE_CIRCLE;

    // 填充类型，可选项：充满，剪裁
    private int scaleType = SCALE_TYPE_CROP;

    // 圆角半径
    private int borderRadius;

    private int width;
    private int radius;

    private Paint bitmapPaint;
    private RectF roundRect;
    private Matrix matrix;
    private BitmapShader bitmapShader;

    public VideoRoundImageView(Context context) {
        this(context, null);
    }

    public VideoRoundImageView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public VideoRoundImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        matrix = new Matrix();
        bitmapPaint = new Paint();
        bitmapPaint.setAntiAlias(true);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.VideoRoundImageView);
        type = array.getInt(R.styleable.VideoRoundImageView_video_type, TYPE_CIRCLE);
        scaleType = array.getInt(R.styleable.VideoRoundImageView_video_scale, SCALE_TYPE_FIT);

        corners_top_left = array.getBoolean(R.styleable.VideoRoundImageView_video_top_left, true);
        corners_top_right = array.getBoolean(R.styleable.VideoRoundImageView_video_top_right, true);
        corners_bottom_left = array.getBoolean(R.styleable.VideoRoundImageView_video_bottom_left, true);
        corners_bottom_right = array.getBoolean(R.styleable.VideoRoundImageView_video_bottom_right, true);
        borderRadius = array.getDimensionPixelSize(R.styleable.VideoRoundImageView_video_borderRadius,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, BODER_RADIUS_DEFAULT,
                        getResources().getDisplayMetrics()));
        array.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (type == TYPE_CIRCLE) {
            width = Math.min(getMeasuredWidth(), getMeasuredHeight());
            radius = width / 2;
            setMeasuredDimension(width, width);
        }
    }

    /**
     * 显示图片
     */
    private void setUpShader() {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }
        Bitmap bitmap = drawableToBitamp(drawable);
        bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        float scale = 1.0f;
        if (type == TYPE_CIRCLE) {
            scale = width * 1.0f / Math.min(bitmap.getWidth(), bitmap.getHeight());
            matrix.setScale(scale, scale);
        } else if (type == TYPE_ROUND) {
            float scaleWidth = getWidth() * 1.0f / bitmap.getWidth();
            float scaleHeight = getHeight() * 1.0f / bitmap.getHeight();
            scale = scaleWidth != scaleHeight ? Math.max(scaleWidth, scaleHeight) : 1f;
            if (scaleType == SCALE_TYPE_CROP) {
                matrix.setScale(scale, scale);
            } else if (scaleType == SCALE_TYPE_FIT) {
                matrix.setScale(scaleWidth, scaleHeight);
            }
        }
        bitmapShader.setLocalMatrix(matrix);
        bitmapPaint.setShader(bitmapShader);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getDrawable() == null) {
            return;
        }
        setUpShader();
        // 圆角模式下剪裁相应的角
        if (type == TYPE_ROUND) {
            canvas.drawRoundRect(roundRect, borderRadius, borderRadius, bitmapPaint);
            if (!corners_top_left) {
                canvas.drawRect(0, 0, borderRadius, borderRadius, bitmapPaint);
            }
            if (!corners_top_right) {
                canvas.drawRect(roundRect.right - borderRadius, 0, roundRect.right, borderRadius, bitmapPaint);
            }
            if (!corners_bottom_left) {
                canvas.drawRect(0, roundRect.bottom - borderRadius, borderRadius, roundRect.bottom, bitmapPaint);
            }
            if (!corners_bottom_right) {
                canvas.drawRect(roundRect.right - borderRadius, roundRect.bottom - borderRadius, roundRect.right, roundRect.bottom, bitmapPaint);
            }
        } else {
            canvas.drawCircle(radius, radius, radius, bitmapPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (type == TYPE_ROUND) {
            roundRect = new RectF(0, 0, w, h);
        }
    }

    private Bitmap drawableToBitamp(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bd = (BitmapDrawable) drawable;
            return bd.getBitmap();
        }
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(STATE_INSTANCE, super.onSaveInstanceState());
        bundle.putInt(STATE_TYPE, type);
        bundle.putInt(STATE_BORDER_RADIUS, borderRadius);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            super.onRestoreInstanceState(((Bundle) state).getParcelable(STATE_INSTANCE));
            this.type = bundle.getInt(STATE_TYPE);
            this.borderRadius = bundle.getInt(STATE_BORDER_RADIUS);
        } else {
            super.onRestoreInstanceState(state);
        }

    }

    /**
     * 设置圆角半径
     *
     * @param borderRadius 半径，单位dp
     */
    public void setBorderRadius(int borderRadius) {
        this.borderRadius = dp2px(borderRadius);
        invalidate();
    }

    /**
     * 设置填充类型
     *
     * @param scaleType 填充类型，可选项：SCALE_TYPE_CROP、SCALE_TYPE_FIT
     */
    public void setScaleType(int scaleType) {
        if (scaleType != SCALE_TYPE_CROP && scaleType != SCALE_TYPE_FIT) {
            return;
        }
        this.scaleType = scaleType;
        invalidate();
    }

    /**
     * 设置显示类型
     *
     * @param type 显示类型，可选项：TYPE_CIRCLE（圆形）、TYPE_ROUND（圆角）
     */
    public void setType(int type) {
        if (this.type != TYPE_ROUND && this.type != TYPE_CIRCLE) {
            return;
        }
        this.type = type;
        invalidate();
    }

    private int dp2px(int dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, getResources().getDisplayMetrics());
    }
}
