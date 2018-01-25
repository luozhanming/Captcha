# Captcha
Android滑块拼图验证码控件

<img src="https://github.com/luozhanming/Captcha/blob/master/GIF_20180120_215046.gif" width="180" height="320" />

## Feature
1.简单，实用，只需一两句代码即可使用<br>
2.采用策略模式为使用者开放自定义拼图样式策略，对拼图样式(拼图形状、视觉效果)进行定制<br>

## Usage
1.在app的build.gradle添加依赖
```Groovy
compile 'com.luozm.captcha:captcha:1.0.5'
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
     * 定义拼图缺块的形状
     *
     * @param blockSize 单位dp，注意转化为px
     * @return path of the shape
     */
    public abstract Path getBlockShape(int blockSize);

    /**
     * 定义拼图缺块的位置信息
     *
     * @param width  picture width
     * @param height picture height
     * @return position info of the block
     */
    public abstract PictureVertifyView.PositionInfo getBlockPostionInfo(int width, int height);

    /**
     * 获得绘制拼图缺块阴影的Paint
     */
    public abstract Paint getBlockShadowPaint();

    /**
     * 获得绘制拼图滑块的Paint
     */
    public abstract Paint getBlockBitmapPaint();

    /**
     * 装饰滑块图片，在绘制图片后执行，即绘制滑块前景
     *  @param canvas 图片的画布
     *  @shape 拼图形状
     */
    public void decoreateSwipeBlockBitmap(Canvas canvas,Path shape) {

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
        app:progressDrawable="@drawable/progress"
        app:thumbDrawable="@drawable/thumb"
        app:src="@mipmap/cat"/>
```

## 博文地址
```
http://blog.csdn.net/sdfsdfdfa/article/details/79120665
```
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
