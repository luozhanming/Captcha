package com.example.cdc4512.vertifydemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.luozm.captcha.Captcha;


public class MainActivity extends AppCompatActivity {

    private Captcha captcha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        captcha = (Captcha) findViewById(R.id.captCha);
        captcha.setCaptchaListener(new Captcha.CaptchaListener() {
            @Override
            public void onAccess(long time) {
                Toast.makeText(MainActivity.this,"验证成功",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(int count) {
                Toast.makeText(MainActivity.this,"验证失败,失败次数"+count,Toast.LENGTH_SHORT).show();
                if(count==captcha.getMaxFailedCount()){
                    Toast.makeText(MainActivity.this,"验证超过次数，你的帐号被封锁",Toast.LENGTH_SHORT).show();
                }
            }

        });
    }
}
