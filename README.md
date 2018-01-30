# Captcha
Android滑块拼图验证码控件

<img src="https://github.com/luozhanming/Captcha/blob/master/GIF_20180129_180253.gif" width="180" height="320" />

## Feature
1.简单，实用，只需一两句代码即可使用<br>
2.采用策略模式为使用者开放自定义拼图样式策略，对拼图样式(拼图形状、视觉效果)进行定制<br>
3.自选模式，无滑动条模式（手触移动),有滑动条模式

## Method
|方法名|描述|版本限制
|---|---|---|
|setMode(int mode)| 设置验证模式（默认为MODE_BAR）|1.0.8开始
|setMaxFailedCount(int count)| 设置最大验证失败次数（默认为5次）|1.0.8开始
|setBitmap(Bitmap bitmap)| 设置图片|1.0.8开始
|setBlockSize(int blockSize)| 设置滑块图片大小，单位px（默认50dp）|1.0.8开始
|setCaptchaStrategy(CaptchaStrategy strategy)|设置验证策略 |1.0.5开始
|setSeekBarStyle(int progressDrawable, int thumbDrawable)| 设置滑动条样式 |1.0.5开始


## Attributes属性（captcha布局文件中调用）
|Attributes|forma|describe
|---|---|---|
|mode| enum |mode_bar:带滑动条 mode_nonbar:不带滑动条
|src| reference| 图片
|progressDrawable| reference|滑动条progress样式
|thumbDrawable| reference|滑动条触动点样式
|max_fail_count| integer|最大验证失败次数
|blockSize| dimension|缺块大小


## Usage
1.在app的build.gradle添加依赖
```Groovy
compile 'com.luozm.captcha:captcha:1.0.8'
```
2.将Captcha添加至布局
```xml
<com.luozm.captcha.Captcha
        android:id="@+id/captCha"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:src="@mipmap/cat"/>
```
3.在编写Java代码

```Java
   captcha = (Captcha) findViewById(R.id.captCha);
   captcha.setCaptchaListener(new Captcha.CaptchaListener() {
            @Override
            public void onAccess(long time) {
                Toast.makeText(MainActivity.this,"验证成功",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed() {
                Toast.makeText(MainActivity.this,"验证失败",Toast.LENGTH_SHORT).show();
            }
        });
```
4.(可选)自定义拼图样式<br>
  1.编写策略类,继承CaptchaStrategy类，重写策略方法,具体可参考DefaultCaptchaStrategy类
  ```Java
  public abstract class CaptchaStrategy {

    protected Context mContext;

    public CaptchaStrategy(Context ctx) {
        this.mContext = ctx;
    }

    protected Context getContext() {
        return mContext;
    }

    /**
     * 定义缺块的形状
     *
     * @param blockSize 单位dp，注意转化为px
     * @return path of the shape
     */
    public abstract Path getBlockShape(int blockSize);

    /**
     * 定义缺块的位置信息
     *
     * @param width  picture width
     * @param height picture height
     * @return position info of the block
     */
    public abstract PictureVertifyView.PositionInfo getBlockPostionInfo(int width, int height, int blockSize);

    /**
     * 定义滑块图片的位置信息(只有设置为无滑动条模式有用并建议重写)
     *
     * @param width  picture width
     * @param height picture height
     * @return position info of the block
     */
    public PictureVertifyView.PositionInfo getPositionInfoForSwipeBlock(int width, int height, int blockSize){
        return getBlockPostionInfo(width,height,blockSize);
    }

    /**
     * 获得缺块阴影的Paint
     */
    public abstract Paint getBlockShadowPaint();

    /**
     * 获得滑块图片的Paint
     */
    public abstract Paint getBlockBitmapPaint();

    /**
     * 装饰滑块图片，在绘制图片后执行，即绘制滑块前景
     */
    public void decoreateSwipeBlockBitmap(Canvas canvas, Path shape) {

    }
}

  ```
  2.添加Java代码
```Java
captCha.setCaptchaStrategy(new XXXCaptchaStrategy(context));
```

5.(可选)自定义滑块条
   与Seekbar自定义样式一样
```xml
<com.luozm.captcha.Captcha
        android:id="@+id/captCha"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:src="@mipmap/cat"/>
```

## 博文地址

http://blog.csdn.net/sdfsdfdfa/article/details/79120665

## License
```xml
Captcha library for Android
Copyright (c) 2018 Luozm 

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
