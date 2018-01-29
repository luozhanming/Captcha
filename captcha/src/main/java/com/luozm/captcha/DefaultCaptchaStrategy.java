package com.luozm.captcha;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;

import java.util.Random;

/**
 * Created by luozhanming on 2018/1/19.
 */

public class DefaultCaptchaStrategy extends CaptchaStrategy {

    public DefaultCaptchaStrategy(Context ctx) {
        super(ctx);
    }

    @Override
    public Path getBlockShape(int blockSize) {
        int gap = (int) (blockSize/5f);
        Path path = new Path();
        path.moveTo(0, gap);
        path.rLineTo(blockSize/2.5f, 0);
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

    @Override
    public PictureVertifyView.PositionInfo getBlockPostionInfo(int width, int height,int blockSize) {
        Random random = new Random();
        int left = random.nextInt(width - blockSize);
        //Avoid robot frequently and quickly click the start point to access the captcha.
        if (left < blockSize) {
            left = blockSize;
        }
        int top = random.nextInt(height - blockSize);
        if (top < 0) {
            top = 0;
        }
        return new PictureVertifyView.PositionInfo(left, top);
    }

    @Override
    public PictureVertifyView.PositionInfo getPositionInfoForSwipeBlock(int width, int height, int blockSize) {
        Random random = new Random();
        int left = random.nextInt(width - blockSize);
        int top = random.nextInt(height - blockSize);
        if (top < 0) {
            top = 0;
        }
        return new PictureVertifyView.PositionInfo(left, top);
    }

    @Override
    public Paint getBlockShadowPaint() {
        Paint shadowPaint = new Paint();
        shadowPaint.setColor(Color.parseColor("#000000"));
        shadowPaint.setAlpha(165);
        return shadowPaint;
    }

    @Override
    public Paint getBlockBitmapPaint() {
        Paint paint = new Paint();
        return paint;
    }


    @Override
    public void decoreateSwipeBlockBitmap(Canvas canvas, Path shape) {
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#FFFFFF"));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);
        paint.setPathEffect(new DashPathEffect(new float[]{20,20},10));
        Path path = new Path(shape);
        canvas.drawPath(path,paint);
    }
}
