package com.music.player.lib.view;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RSRuntimeException;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import com.music.player.lib.R;

/**
 * TinyHung@Outlook.com
 * 2018/1/18.
 * MusicBackgroungBlurView
 * buildToolsVersion >= 24.0.2
 */

public class MusicBackgroungBlurView extends View {

	private float mDownsampleFactor; // 采样因子 3-10为最佳
	private int mOverlayColor; // 背景颜色，默认0xAA3A1B59
	private float mBlurRadius; // 模糊半径 (0-25之间)
    private final float mFramRadius;//圆角半径
    //圆角
    private final Path mRoundPath;
    private final RectF mRectF;
	private boolean mDirty;
	//背景
	private Bitmap mBitmapToBlur, mBlurredBitmap;
	private Canvas mBlurringCanvas;
	private RenderScript mRenderScript;
	private ScriptIntrinsicBlur mBlurScript;
	private Allocation mBlurInput, mBlurOutput;
	private boolean mIsRendering;
	private final Rect mRectSrc = new Rect(), mRectDst = new Rect();
	private static int RENDERING_COUNT;
    private final boolean mTopLeft;
    private final boolean mTopRight;
    private final boolean mBottomRight;
    private final boolean mBottomLeft;

    public MusicBackgroungBlurView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MusicBackgroungBlurView);
		mBlurRadius = typedArray.getDimension(R.styleable.MusicBackgroungBlurView_blurBlurRadius,TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, context.getResources().getDisplayMetrics()));
		mDownsampleFactor = typedArray.getFloat(R.styleable.MusicBackgroungBlurView_blurDownsampleFactor, 5);
		mOverlayColor = typedArray.getColor(R.styleable.MusicBackgroungBlurView_blurOverlayColor, 0xAA3A1B59);
        mFramRadius = typedArray.getDimension(R.styleable.MusicBackgroungBlurView_blurFramRadius, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, context.getResources().getDisplayMetrics()));
        mTopLeft = typedArray.getBoolean(R.styleable.MusicBackgroungBlurView_blurTopLeft,false);
        mTopRight = typedArray.getBoolean(R.styleable.MusicBackgroungBlurView_blurTopRight,false);
        mBottomRight = typedArray.getBoolean(R.styleable.MusicBackgroungBlurView_blurBottomRight,false);
        mBottomLeft = typedArray.getBoolean(R.styleable.MusicBackgroungBlurView_blurBottomLeft,false);
		typedArray.recycle();
        //绘制圆角准备
        setWillNotDraw(false);
        mRoundPath = new Path();
        mRectF = new RectF();
	}

	public void setBlurRadius(float radius) {
		if (mBlurRadius != radius) {
			mBlurRadius = radius;
			mDirty = true;
			invalidate();
		}
	}

    /**
     * 设置一个圆角
     * @param radius
     */
	public void setRadius(float radius){
        float topLeftRadius = getRadiusToDirection(mTopLeft,radius);
        float topRightRadius = getRadiusToDirection(mTopRight,radius);
        float bottomLeftRadius = getRadiusToDirection(mBottomLeft,radius);
        float bottomRightRadius = getRadiusToDirection(mBottomRight,radius);
//        mRoundPath.addRoundRect(mRectF, radius, radius, Path.Direction.CW);//绘制四个方向圆角
        //绘制指定方向圆角
        mRoundPath.addRoundRect(mRectF, new float[]{topLeftRadius,topLeftRadius,topRightRadius,topRightRadius,bottomLeftRadius,bottomLeftRadius,bottomRightRadius,bottomRightRadius}, Path.Direction.CW);
    }

    private float getRadiusToDirection(boolean direction,float framRadius) {
        return direction?framRadius:0;
    }

    public void setDownsampleFactor(float factor) {
		if (factor <= 0) {
			throw new IllegalArgumentException("Downsample factor must be greater than 0.");
		}
		if (mDownsampleFactor != factor) {
			mDownsampleFactor = factor;
			mDirty = true; // may also change blur radius
			releaseBitmap();
			invalidate();
		}
	}

	public void setOverlayColor(int color) {
		if (mOverlayColor != color) {
			mOverlayColor = color;
			invalidate();
		}
	}

	private void releaseBitmap() {
		if (mBlurInput != null) {
			mBlurInput.destroy();
			mBlurInput = null;
		}
		if (mBlurOutput != null) {
			mBlurOutput.destroy();
			mBlurOutput = null;
		}
		if (mBitmapToBlur != null) {
			mBitmapToBlur.recycle();
			mBitmapToBlur = null;
		}
		if (mBlurredBitmap != null) {
			mBlurredBitmap.recycle();
			mBlurredBitmap = null;
		}
	}

	private void releaseScript() {
		if (mRenderScript != null) {
			mRenderScript.destroy();
			mRenderScript = null;
		}
		if (mBlurScript != null) {
			mBlurScript.destroy();
			mBlurScript = null;
		}
	}

	protected void release() {
		releaseBitmap();
		releaseScript();
	}

	protected boolean prepare() {
		if (mBlurRadius == 0) {
			release();
			return false;
		}
		float downsampleFactor = mDownsampleFactor;
		if (mDirty || mRenderScript == null) {
			if (mRenderScript == null) {
				try {
					mRenderScript = RenderScript.create(getContext());
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
						mBlurScript = ScriptIntrinsicBlur.create(mRenderScript, Element.U8_4(mRenderScript));
					}
				} catch (RSRuntimeException e) {
					return false;
				}
			}

			mDirty = false;
			float radius = mBlurRadius / downsampleFactor;
			if (radius > 25) {
				downsampleFactor = downsampleFactor * radius / 25;
				radius = 25;
			}
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
				mBlurScript.setRadius(radius);
			}
		}

		final int width = getWidth();
		final int height = getHeight();

		int scaledWidth = Math.max(1, (int) (width / downsampleFactor));
		int scaledHeight = Math.max(1, (int) (height / downsampleFactor));

		if (mBlurringCanvas == null || mBlurredBitmap == null
				|| mBlurredBitmap.getWidth() != scaledWidth
				|| mBlurredBitmap.getHeight() != scaledHeight) {
			releaseBitmap();

			mBitmapToBlur = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);
			if (mBitmapToBlur == null) {
				return false;
			}

			mBlurredBitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);
			if (mBlurredBitmap == null) {
				return false;
			}

			mBlurringCanvas = new Canvas(mBitmapToBlur);
			mBlurInput = Allocation.createFromBitmap(mRenderScript, mBitmapToBlur,
					Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
			mBlurOutput = Allocation.createTyped(mRenderScript, mBlurInput.getType());
		}
		return true;
	}

	protected void blur() {
		mBlurInput.copyFrom(mBitmapToBlur);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			mBlurScript.forEach(mBlurOutput);
			mBlurScript.setInput(mBlurInput);
		}
		mBlurOutput.copyTo(mBlurredBitmap);
	}

	private final ViewTreeObserver.OnPreDrawListener preDrawListener = new ViewTreeObserver.OnPreDrawListener() {
		@Override
		public boolean onPreDraw() {
			final int[] locations = new int[2];
			if (isShown() && prepare()) {
				Activity a = null;
				Context ctx = getContext();
				while (true) {
					if (ctx instanceof Activity) {
						a = (Activity) ctx;
						break;
					} else if (ctx instanceof ContextWrapper) {
						ctx = ((ContextWrapper) ctx).getBaseContext();
					} else {
						break;
					}
				}
				if (a == null) {
					// Not in a activity
					return true;
				}

				View decor = a.getWindow().getDecorView();
				decor.getLocationOnScreen(locations);
				int x = -locations[0];
				int y = -locations[1];

				getLocationOnScreen(locations);
				x += locations[0];
				y += locations[1];

				if (decor.getBackground() instanceof ColorDrawable) {
					mBitmapToBlur.eraseColor(((ColorDrawable) decor.getBackground()).getColor());
				} else {
					mBitmapToBlur.eraseColor(Color.TRANSPARENT);
				}

				int rc = mBlurringCanvas.save();
				mIsRendering = true;
				RENDERING_COUNT++;
				try {
					mBlurringCanvas.scale(1.f * mBlurredBitmap.getWidth() / getWidth(), 1.f * mBlurredBitmap.getHeight() / getHeight());
					mBlurringCanvas.translate(-x, -y);
					decor.draw(mBlurringCanvas);
				} catch (StopException e) {
				} finally {
					mIsRendering = false;
					RENDERING_COUNT--;
					mBlurringCanvas.restoreToCount(rc);
				}

				blur();
			}

			return true;
		}
	};

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		getViewTreeObserver().addOnPreDrawListener(preDrawListener);
	}

	@Override
	protected void onDetachedFromWindow() {
		getViewTreeObserver().removeOnPreDrawListener(preDrawListener);
		release();
		super.onDetachedFromWindow();
	}

	@Override
	public void draw(Canvas canvas) {
		if (mIsRendering) {
			// Quit here, don't draw views above me
			throw STOP_EXCEPTION;
		} else if (RENDERING_COUNT > 0) {
			// Doesn't support blurview overlap on another blurview
		} else {
			super.draw(canvas);
		}
	}

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mRectF.set(0f, 0f, getMeasuredWidth(), getMeasuredHeight());
        setRadius(mFramRadius);
    }

    @Override
	protected void onDraw(Canvas canvas) {
        if (mFramRadius > 0f) {
            canvas.clipPath(mRoundPath);
        }
		super.onDraw(canvas);
		drawBlurredBitmap(canvas, mBlurredBitmap, mOverlayColor);
	}

	/**
	 * Custom draw the blurred bitmap and color to define your own shape
	 *
	 * @param canvas
	 * @param blurredBitmap
	 * @param overlayColor
	 */
	protected void drawBlurredBitmap(Canvas canvas, Bitmap blurredBitmap, int overlayColor) {
		if (blurredBitmap != null) {
			mRectSrc.right = blurredBitmap.getWidth();
			mRectSrc.bottom = blurredBitmap.getHeight();
			mRectDst.right = getWidth();
			mRectDst.bottom = getHeight();
			canvas.drawBitmap(blurredBitmap, mRectSrc, mRectDst, null);
		}
		canvas.drawColor(overlayColor);
	}

	private static class StopException extends RuntimeException {
	}

	private static StopException STOP_EXCEPTION = new StopException();

	public void onDestroy(){
		release();
	}
}
