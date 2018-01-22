package com.luozm.captcha;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.AttrRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by luozhanming on 2018/1/17.
 */

public class Captcha extends LinearLayout {

    private PictureVertifyView vertifyView;
    private TextSeekbar seekbar;
    private View accessSuccess, accessFailed;
    private TextView accessText, accessFailedText;

    private int drawableId;
    private int progressDrawableId;
    private int thumbDrawableId;


    //处理滑动条逻辑
    private int oldsign;
    private boolean isResponse;
    private boolean isDown;


    private CaptchaListener mListener;

    public interface CaptchaListener {

        void onAccess(long time);

        void onFailed();

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
        typedArray.recycle();
        init();
    }


    private void init() {
        View parentView = LayoutInflater.from(getContext()).inflate(R.layout.container, this, true);
        vertifyView = (PictureVertifyView) parentView.findViewById(R.id.vertifyView);
        vertifyView.setImageResource(drawableId);
        vertifyView.setAccessListener(new CaptchaListener() {
            @Override
            public void onAccess(long time) {
                if (mListener != null) {
                    mListener.onAccess(time);
                }
                accessSuccess.setVisibility(VISIBLE);
                accessFailed.setVisibility(GONE);
                accessText.setText(String.format(getResources().getString(R.string.vertify_access), time));
            }

            @Override
            public void onFailed() {
                reset();
                accessFailed.setVisibility(VISIBLE);
                accessSuccess.setVisibility(GONE);
                accessFailedText.setText(getResources().getString(R.string.vertify_failed));
                if (mListener != null) {
                    mListener.onFailed();
                }
            }
        });
        seekbar = (TextSeekbar) parentView.findViewById(R.id.seekbar);
        setSeekBarStyle(progressDrawableId, thumbDrawableId);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (isDown) {
                    isDown = false;
                    if (progress > 10) {
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
                    isResponse = false;
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
        accessSuccess = parentView.findViewById(R.id.accessRight);
        accessFailed = parentView.findViewById(R.id.accessFailed);
        accessText = (TextView) parentView.findViewById(R.id.accessText);
        accessFailedText = (TextView) parentView.findViewById(R.id.accessFailedText);
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
     * 设置滑块图片大小，单位dp
     */
    public void setBlockSize(int blockSize) {
        vertifyView.setBlockSize(blockSize);
    }

    /**
     * 复位
     */
    public void reset() {
        vertifyView.reset();
        oldsign = 0;
        seekbar.setProgress(0);
    }


}
