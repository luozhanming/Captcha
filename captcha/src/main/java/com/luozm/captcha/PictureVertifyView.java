package com.luozm.captcha;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


/**
 * 拼图区域控件
 * Created by luozhanming on 2018/1/17.
 */

class PictureVertifyView extends AppCompatImageView {

    //状态码
    private static final int STATE_DOWN = 1;
    private static final int STATE_MOVE = 2;
    private static final int STATE_LOOSEN = 3;
    private static final int STATE_IDEL = 4;
    private static final int STATE_ACCESS = 5;
    private static final int STATE_UNACCESS = 6;


    private static final int TOLERANCE = 10;         //验证的最大容差


    private int mState = STATE_IDEL;    //当前状态
    private PositionInfo shadowInfo;    //拼图缺块阴影的位置
    private PositionInfo blockInfo;     //拼图缺块的位置
    private Bitmap verfityBlock;        //拼图缺块Bitmap
    private Path blockShape;            //拼图缺块形状
    private Paint bitmapPaint;         //绘制拼图缺块的画笔
    private Paint shadowPaint;         //绘制拼图缺块阴影的画笔
    private long startTouchTime;       //滑动/触动开始时间
    private long looseTime;            //滑动/触动松开时间
    private int blockSize = 50;

    private boolean mTouchEnable = true;   //是否可触动


    private Callback callback;

    private CaptchaStrategy mStrategy;

    private int mMode;                //Captcha验证模式


    interface Callback {
        void onSuccess(long time);

        void onFailed();
    }


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
        setLayerType(View.LAYER_TYPE_SOFTWARE, bitmapPaint);
    }

    private void initDrawElements(){
        if (shadowInfo == null) {
            shadowInfo = mStrategy.getBlockPostionInfo(getWidth(), getHeight(), blockSize);
            if (mMode == Captcha.MODE_BAR) {
                blockInfo = new PositionInfo(0, shadowInfo.top);
            } else {
                blockInfo = mStrategy.getPositionInfoForSwipeBlock(getWidth(), getHeight(), blockSize);
            }
        }
        if (blockShape == null) {
            blockShape = mStrategy.getBlockShape(blockSize);
            blockShape.offset(shadowInfo.left, shadowInfo.top);
        }
        if (verfityBlock == null) {
            verfityBlock = createBlockBitmap();
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        initDrawElements();
        if (mState != STATE_ACCESS) {
            canvas.drawPath(blockShape, shadowPaint);
        }
        if (mState == STATE_MOVE || mState == STATE_IDEL || mState == STATE_DOWN || mState == STATE_UNACCESS) {
            canvas.drawBitmap(verfityBlock, blockInfo.left, blockInfo.top, bitmapPaint);
        }

    }

    /**
     *按下滑动条(滑动条模式)
     * */
    void down(int progress) {
        startTouchTime = System.currentTimeMillis();
        mState = STATE_DOWN;
        blockInfo.left = (int) (progress / 100f * (getWidth() - blockSize));
        invalidate();
    }

    /**
     *触动拼图块(触动模式)
     * */
    void downByTouch(float x, float y) {
        mState = STATE_DOWN;
        blockInfo.left = (int) (x - blockSize / 2f);
        blockInfo.top = (int) (y - blockSize / 2f);
        startTouchTime = System.currentTimeMillis();
        invalidate();
    }

    /**
     * 移动拼图缺块(滑动条模式)
     */
    void move(int progress) {
        mState = STATE_MOVE;
        blockInfo.left = (int) (progress / 100f * (getWidth() - blockSize));
        invalidate();
    }
    /**
     * 触动拼图缺块(触动模式)
     */
    void moveByTouch(float offsetX, float offsetY) {
        mState = STATE_MOVE;
        blockInfo.left += offsetX;
        blockInfo.top += offsetY;
        invalidate();
    }

    /**
     * 松开
     * */
    void loose() {
        mState = STATE_LOOSEN;
        looseTime = System.currentTimeMillis();
        checkAccess();
        invalidate();
    }


    /**
     * 复位
     * */
    void reset() {
        mState = STATE_IDEL;
        verfityBlock = null;
        shadowInfo = null;
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

    void callback(Callback callback) {
        this.callback = callback;
    }


    void setCaptchaStrategy(CaptchaStrategy strategy) {
        this.mStrategy = strategy;
    }

    void setBlockSize(int size) {
        this.blockSize = size;
        this.blockShape = null;
        this.blockInfo = null;
        this.shadowInfo = null;
        this.verfityBlock = null;
        invalidate();
    }

    public void setBitmap(Bitmap bitmap) {
        this.blockShape = null;
        this.blockInfo = null;
        this.shadowInfo = null;
        this.verfityBlock.recycle();
        this.verfityBlock = null;
        setImageBitmap(bitmap);
    }

    void setMode(@Captcha.Mode int mode) {
        this.mMode = mode;
        this.blockShape = null;
        this.blockInfo = null;
        this.shadowInfo = null;
        this.verfityBlock = null;
        invalidate();
    }


    void setTouchEnable(boolean enable) {
        this.mTouchEnable = enable;
    }

    /**
     * 生成拼图缺块的Bitmap
     * */
    private Bitmap createBlockBitmap() {
        Bitmap tempBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(tempBitmap);
        getDrawable().setBounds(0, 0, getWidth(), getHeight());
        canvas.clipPath(blockShape);
        getDrawable().draw(canvas);
        mStrategy.decoreateSwipeBlockBitmap(canvas, blockShape);
        return cropBitmap(tempBitmap);
    }

    /**
     * 保留拼图缺块大小的bitmap
     * */
    private Bitmap cropBitmap(Bitmap bmp) {
        Bitmap result = null;
        result = Bitmap.createBitmap(bmp, shadowInfo.left, shadowInfo.top, blockSize, blockSize);
        bmp.recycle();
        return result;
    }

    /**
     * 检测是否通过
     */
    private void checkAccess() {
        if (Math.abs(blockInfo.left - shadowInfo.left) < TOLERANCE && Math.abs(blockInfo.top - shadowInfo.top) < TOLERANCE) {
            access();
            if (callback != null) {
                long deltaTime = looseTime - startTouchTime;
                callback.onSuccess(deltaTime);
            }
        } else {
            unAccess();
            if (callback != null) {
                callback.onFailed();
            }
        }
    }

    private float tempX, tempY, downX, downY;


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        //触动模式下，点击超出拼图缺块的区域不进行处理
        if (event.getAction() == MotionEvent.ACTION_DOWN && mMode == Captcha.MODE_NONBAR) {
            if (event.getX() < blockInfo.left || event.getX() > blockInfo.left + blockSize || event.getY() < blockInfo.top || event.getY() > blockInfo.top + blockSize) {
                return false;
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mMode == Captcha.MODE_NONBAR && verfityBlock != null && mTouchEnable) {
            float x = event.getX();
            float y = event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX = x;
                    downY = y;
                    downByTouch(x, y);
                    break;
                case MotionEvent.ACTION_UP:
                    loose();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float offsetX = x - tempX;
                    float offsetY = y - tempY;
                    moveByTouch(offsetX, offsetY);
                    break;
            }
            tempX = x;
            tempY = y;
        }
        return true;
    }

}
