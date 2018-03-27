package com.luozm.captcha;

import android.animation.Animator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.AttrRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Created by luozhanming on 2018/1/17.
 */

public class Captcha extends LinearLayout {

    private PictureVertifyView vertifyView;
    private TextSeekbar seekbar;
    private View accessSuccess, accessFailed;
    private TextView accessText, accessFailedText;
    private ImageView refreshView;

    private int drawableId;
    private int progressDrawableId;
    private int thumbDrawableId;
    private int mMode;
    private int maxFailedCount;
    private int failCount;
    private int blockSize;

    //处理滑动条逻辑
    private boolean isResponse;
    private boolean isDown;

    private CaptchaListener mListener;
    /**
     * 带滑动条验证模式
     */
    public static final int MODE_BAR = 1;
    /**
     * 不带滑动条验证，手触模式
     */
    public static final int MODE_NONBAR = 2;

    @IntDef(value = {MODE_BAR, MODE_NONBAR})
    public @interface Mode {
    }


    public interface CaptchaListener {

        /**
         * Called when captcha access.
         *
         * @param time cost of access time
         * @return text to show,show default when return null
         */
        String onAccess(long time);

        /**
         * Called when captcha failed.
         *
         * @param failCount fail count
         * @return text to show,show default when return null
         */
        String onFailed(int failCount);

        /**
         * Called when captcha failed
         *
         * @return text to show,show default when return null
         */
        String onMaxFailed();

    }


    public Captcha(@NonNull Context context) {
        super(context);
    }

    public Captcha(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Captcha(@NonNull final Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Captcha);
        drawableId = typedArray.getResourceId(R.styleable.Captcha_src, R.drawable.cat);
        progressDrawableId = typedArray.getResourceId(R.styleable.Captcha_progressDrawable, R.drawable.po_seekbar);
        thumbDrawableId = typedArray.getResourceId(R.styleable.Captcha_thumbDrawable, R.drawable.thumb);
        mMode = typedArray.getInteger(R.styleable.Captcha_mode, MODE_BAR);
        maxFailedCount = typedArray.getInteger(R.styleable.Captcha_max_fail_count, 3);
        blockSize = typedArray.getDimensionPixelSize(R.styleable.Captcha_blockSize, Utils.dp2px(getContext(), 50));
        typedArray.recycle();
        init();
    }


    private void init() {
        View parentView = LayoutInflater.from(getContext()).inflate(R.layout.container, this, true);
        vertifyView = (PictureVertifyView) parentView.findViewById(R.id.vertifyView);
        seekbar = (TextSeekbar) parentView.findViewById(R.id.seekbar);
        accessSuccess = parentView.findViewById(R.id.accessRight);
        accessFailed = parentView.findViewById(R.id.accessFailed);
        accessText = (TextView) parentView.findViewById(R.id.accessText);
        accessFailedText = (TextView) parentView.findViewById(R.id.accessFailedText);
        refreshView = (ImageView) parentView.findViewById(R.id.refresh);
        setMode(mMode);
        vertifyView.setImageResource(drawableId);
        setBlockSize(blockSize);
        vertifyView.callback(new PictureVertifyView.Callback() {
            @Override
            public void onSuccess(long time) {
                if (mListener != null) {
                    String s = mListener.onAccess(time);
                    if (s != null) {
                        accessText.setText(s);
                    } else {//默认文案
                        accessText.setText(String.format(getResources().getString(R.string.vertify_access), time));
                    }
                }
                accessSuccess.setVisibility(VISIBLE);
                accessFailed.setVisibility(GONE);
            }

            @Override
            public void onFailed() {
                seekbar.setEnabled(false);
                vertifyView.setTouchEnable(false);
                failCount = failCount > maxFailedCount ? maxFailedCount : failCount + 1;
                accessFailed.setVisibility(VISIBLE);
                accessSuccess.setVisibility(GONE);
                if (mListener != null) {
                    if (failCount == maxFailedCount) {
                        String s = mListener.onMaxFailed();
                        if (s != null) {
                            accessFailedText.setText(s);
                        } else {//默认文案
                            accessFailedText.setText(String.format(getResources().getString(R.string.vertify_failed), maxFailedCount - failCount));
                        }
                    } else {
                        String s = mListener.onFailed(failCount);
                        if (s != null) {
                            accessFailedText.setText(s);
                        } else {//默认文案
                            accessFailedText.setText(String.format(getResources().getString(R.string.vertify_failed), maxFailedCount - failCount));
                        }
                    }
                }
            }

        });
        setSeekBarStyle(progressDrawableId, thumbDrawableId);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (isDown) {  //手指按下
                    isDown = false;
                    if (progress > 10) { //按下位置不正确
                        isResponse = false;
                    } else {
                        isResponse = true;
                        accessFailed.setVisibility(GONE);
                        vertifyView.down(0);
                    }
                }
                if (isResponse) {
                    vertifyView.move(progress);
                } else {
                    seekBar.setProgress(0);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isDown = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (isResponse) {
                    vertifyView.loose();
                }
            }
        });
        refreshView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startRefresh(v);
            }
        });
    }

    private void startRefresh(View v) {
        v.animate().rotationBy(360).setDuration(500)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        reset(false);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
    }


    public void setCaptchaListener(CaptchaListener listener) {
        this.mListener = listener;
    }

    public void setCaptchaStrategy(CaptchaStrategy strategy) {
        if (strategy != null) {
            vertifyView.setCaptchaStrategy(strategy);
        }
    }

    public void setSeekBarStyle(@DrawableRes int progressDrawable, @DrawableRes int thumbDrawable) {
        seekbar.setProgressDrawable(getResources().getDrawable(progressDrawable));
        seekbar.setThumb(getResources().getDrawable(thumbDrawable));
        seekbar.setThumbOffset(0);
    }

    /**
     * 设置滑块图片大小，单位px
     */
    public void setBlockSize(int blockSize) {
        vertifyView.setBlockSize(blockSize);
    }

    /**
     * 设置滑块验证模式
     */
    public void setMode(@Mode int mode) {
        this.mMode = mode;
        vertifyView.setMode(mode);
        if (mMode == MODE_NONBAR) {
            seekbar.setVisibility(GONE);
            vertifyView.setTouchEnable(true);
        } else {
            seekbar.setVisibility(VISIBLE);
            seekbar.setEnabled(true);
        }
        hideText();
    }

    public int getMode() {
        return this.mMode;
    }

    public void setMaxFailedCount(int count) {
        this.maxFailedCount = count;
    }

    public int getMaxFailedCount() {
        return this.maxFailedCount;
    }


    public void setBitmap(int drawableId) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), drawableId);
        setBitmap(bitmap);
    }

    public void setBitmap(Bitmap bitmap) {
        vertifyView.setImageBitmap(bitmap);
        reset(false);
    }


    /**
     * 复位
     */
    public void reset(boolean clearFailed) {
        hideText();
        vertifyView.reset();
        if (clearFailed) {
            failCount = 0;
        }
        if (mMode == MODE_BAR){
            seekbar.setEnabled(true);
            seekbar.setProgress(0);
        }else{
            vertifyView.setTouchEnable(true);
        }
    }

    public void hideText() {
        accessFailed.setVisibility(GONE);
        accessSuccess.setVisibility(GONE);
    }


}
