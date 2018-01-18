package com.luozm.captcha;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.AttrRes;
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
    private View accessSuccess,accessFailed;
    private TextView accessText,accessFailedText;

    private int drawableId;


    public interface CaptchaListener {
        void onAccess(long time);

        void onFailed();
    }

    private CaptchaListener mListener;


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
                accessText.setText(String.format(getResources().getString(R.string.vertify_access),time));
            }

            @Override
            public void onFailed() {
                reset();
                if (mListener != null) {
                    mListener.onFailed();
                }
//                accessSuccess.setVisibility(GONE);
//                accessFailed.setVisibility(VISIBLE);
//                accessFailedText.setText(getResources().getString(R.string.vertify_failed));
            }
        });
        seekbar = (TextSeekbar) parentView.findViewById(R.id.seekbar);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                vertifyView.move(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                vertifyView.down(seekBar.getProgress());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                vertifyView.loose();
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

    public void reset(){
        vertifyView.reset();
        accessFailed.setVisibility(GONE);
        accessSuccess.setVisibility(GONE);
    }


}
