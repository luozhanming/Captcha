package com.luozm.captcha;

/**
 * Created by cdc4512 on 2018/2/5.
 */

public class PositionInfo {

    int left;     //拼图缺块离整张图片左边距离
    int top;      //拼图缺块离整张图片上方距离

    public PositionInfo(int left, int top) {
        this.left = left;
        this.top = top;
    }
}
