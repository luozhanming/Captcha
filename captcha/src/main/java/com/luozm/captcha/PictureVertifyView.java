package com.luozm.captcha;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;


/**
 * Created by luozhanming on 2018/1/17.
 */

class PictureVertifyView extends AppCompatImageView {

    private static final int STATE_DOWN = 1;
    private static final int STATE_MOVE = 2;
    private static final int STATE_LOOSEN = 3;
    private static final int STATE_IDEL = 4;
    private static final int STATE_ACCESS = 5;
    private static final int STATE_UNACCESS = 6;

    private static final int TOLERANCE = 10;


    private int mState = STATE_IDEL;
    private PositionInfo info;
    private Bitmap verfityBlock;
    private Path blockShape;
    private Paint bitmapPaint;
    private Paint shadowPaint;
    private int currentPosition;
    private long startTouchTime;
    private long looseTime;
    private int blockSize = 50;

    private Captcha.CaptchaListener listener;

    private CaptchaStrategy mStrategy;


    public PictureVertifyView(Context context) {
        this(context, null);
    }

    public PictureVertifyView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public PictureVertifyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mStrategy = new DefaultCaptchaStrategy(context);
        shadowPaint = mStrategy.getBlockShadowPaint();
        bitmapPaint = mStrategy.getBlockBitmapPaint();
        setLayerType(View.LAYER_TYPE_SOFTWARE,bitmapPaint);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (info == null) {
            info = mStrategy.getBlockPostionInfo(getWidth(), getHeight());
        }
        if (blockShape == null) {
            blockShape = mStrategy.getBlockShape(blockSize);
            blockShape.offset(info.left, info.top);
        }
        if (verfityBlock == null) {
            verfityBlock = createBlockBitmap();
        }
        if (mState != STATE_ACCESS) {
            canvas.drawPath(blockShape, shadowPaint);
        }
        if (mState == STATE_MOVE || mState == STATE_IDEL) {
            canvas.drawBitmap(verfityBlock, currentPosition, info.top, bitmapPaint);
        }

    }

    void down(int progress) {
        startTouchTime = System.currentTimeMillis();
        mState = STATE_DOWN;
        currentPosition = (int) (progress / 100f * (getWidth() - Utils.dp2px(getContext(), 50)));
        invalidate();
    }

    void move(int progress) {
        mState = STATE_MOVE;
        currentPosition = (int) (progress / 100f * (getWidth() - Utils.dp2px(getContext(), 50)));
        invalidate();
    }

    void loose() {
        mState = STATE_LOOSEN;
        looseTime = System.currentTimeMillis();
        checkAccess();
        invalidate();
    }


    void reset() {
        mState = STATE_IDEL;
        verfityBlock.recycle();
        verfityBlock = null;
        info = null;
        blockShape = null;
        invalidate();
    }

    void unAccess() {
        mState = STATE_UNACCESS;
        invalidate();
    }

    void access() {
        mState = STATE_ACCESS;
        invalidate();
    }

    void setAccessListener(Captcha.CaptchaListener listener) {
        this.listener = listener;
    }


    void setCaptchaStrategy(CaptchaStrategy strategy) {
        this.mStrategy = strategy;
    }

    void setBlockSize(int size){
        this.blockSize = size;
    }

    private Bitmap createBlockBitmap() {
        Bitmap tempBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(tempBitmap);
        getDrawable().setBounds(0, 0, getWidth(), getHeight());
        canvas.clipPath(blockShape);
        getDrawable().draw(canvas);
        mStrategy.decoreateSwipeBlockBitmap(canvas,blockShape);
        return cropBitmap(tempBitmap);
    }

    private Bitmap cropBitmap(Bitmap bmp) {
        Bitmap result = null;
        int size = Utils.dp2px(getContext(), blockSize);
        result = Bitmap.createBitmap(bmp, info.left, info.top, size, size);
        bmp.recycle();
        return result;
    }

    private void checkAccess() {
        if (Math.abs(currentPosition - info.left) < TOLERANCE) {
            access();
            if (listener != null) {
                long deltaTime = looseTime - startTouchTime;
                listener.onAccess(deltaTime);
            }
        } else {
            unAccess();
            if (listener != null) {
                listener.onFailed();
            }
        }
    }


    public static class PositionInfo {

        int left;
        int top;

        public PositionInfo(int left, int top) {
            this.left = left;
            this.top = top;
        }
    }
}
