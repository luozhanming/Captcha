package com.luozm.captcha;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import java.util.Random;


/**
 * Created by cdc4512 on 2018/1/17.
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
    private VertifyInfo info;
    private Bitmap verfityBlock;
    private Path blockPath;
    private Paint bitmapPaint;
    private Paint shadowPaint;
    private int currentPosition;
    private long startTouchTime;
    private long looseTime;

    private Captcha.CaptchaListener listener;


    public PictureVertifyView(Context context) {
        this(context, null);
    }

    public PictureVertifyView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PictureVertifyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        shadowPaint = new Paint();
        shadowPaint.setColor(Color.parseColor("#000000"));
        shadowPaint.setAlpha(164);
        bitmapPaint = new Paint();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (info == null) {
            info = generateVerfificationData();
        }
        if (blockPath == null) {
            blockPath = generateBlockPath();
        }
        if (verfityBlock == null) {
            verfityBlock = createBlockBitmap();
        }
        if (mState != STATE_ACCESS) {
            canvas.drawPath(blockPath, shadowPaint);
        }
        if (mState == STATE_DOWN || mState == STATE_MOVE || mState == STATE_UNACCESS) {
            canvas.drawBitmap(verfityBlock, currentPosition, info.top, bitmapPaint);
        }

    }

    public void down(int progress) {
        startTouchTime = System.currentTimeMillis();
        mState = STATE_DOWN;
        currentPosition = (int) (progress / 100f * (getWidth() - Utils.dp2px(getContext(), 50)));
        invalidate();
    }

    public void move(int progress) {
        mState = STATE_MOVE;
        currentPosition = (int) (progress / 100f * (getWidth() - Utils.dp2px(getContext(), 50)));
        invalidate();
    }

    public void loose() {
        mState = STATE_LOOSEN;
        looseTime = System.currentTimeMillis();
        checkAccess();
        invalidate();
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

    public void reset() {
        mState = STATE_IDEL;
        verfityBlock.recycle();
        verfityBlock = null;
        info = null;
        blockPath = null;
        invalidate();
    }

    public void unAccess() {
        mState = STATE_UNACCESS;
        invalidate();
    }

    public void access() {
        mState = STATE_ACCESS;
        invalidate();
    }

    public void setAccessListener(Captcha.CaptchaListener listener) {
        this.listener = listener;
    }

    private Bitmap createBlockBitmap() {
        Bitmap tempBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(tempBitmap);
        int gap = Utils.dp2px(getContext(), 10);
        Path path = new Path();
        path.moveTo(0, gap);
        path.rLineTo(Utils.dp2px(getContext(), 20), 0);
        path.rLineTo(0, -gap);
        path.rLineTo(gap, 0);
        path.rLineTo(0, gap);
        path.rLineTo(2 * gap, 0);
        path.rLineTo(0, 4 * gap);
        path.rLineTo(-5 * gap, 0);
        path.rLineTo(0, -1.5f * gap);
        path.rLineTo(gap, 0);
        path.rLineTo(0, -gap);
        path.rLineTo(-gap, 0);
        path.close();
        getDrawable().setBounds(0, 0, getWidth(), getHeight());
        canvas.clipPath(blockPath);
        getDrawable().draw(canvas);
        return cropBitmap(tempBitmap);

    }

    private Bitmap cropBitmap(Bitmap bmp) {
        Bitmap result = null;
        int size = Utils.dp2px(getContext(), 50);
        result = Bitmap.createBitmap(bmp, info.left, info.top, size, size);
        bmp.recycle();
        return result;
    }


    //generate block path
    private Path generateBlockPath() {
        int gap = Utils.dp2px(getContext(), 10);
        Path path = new Path();
        path.moveTo(info.left, info.top + gap);
        path.rLineTo(Utils.dp2px(getContext(), 20), 0);
        path.rLineTo(0, -gap);
        path.rLineTo(gap, 0);
        path.rLineTo(0, gap);
        path.rLineTo(2 * gap, 0);
        path.rLineTo(0, 4 * gap);
        path.rLineTo(-5 * gap, 0);
        path.rLineTo(0, -1.5f * gap);
        path.rLineTo(gap, 0);
        path.rLineTo(0, -gap);
        path.rLineTo(-gap, 0);
        path.close();
        return path;
    }

    //generate block postion
    private VertifyInfo generateVerfificationData() {
        int width = getWidth();
        int height = getHeight();
        Random random = new Random();
        int edge = Utils.dp2px(getContext(), 50);
        int left = random.nextInt(width - edge);
        if (left < 0) {
            left = 0;
        }
        int top = random.nextInt(height - edge);
        if (top < 0) {
            top = 0;
        }
        return new VertifyInfo(left, top);
    }


    public class VertifyInfo {

        int left;
        int top;

        public VertifyInfo(int left, int top) {
            this.left = left;
            this.top = top;
        }
    }
}
