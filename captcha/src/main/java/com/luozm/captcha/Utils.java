package com.luozm.captcha;

import android.content.Context;

/**
 * Created by luozhanming on 2018/1/17.
 */

public class Utils {

    public static int dp2px(Context ctx, float dip) {
        float density = ctx.getResources().getDisplayMetrics().density;
        return (int) (dip * density);
    }
}
