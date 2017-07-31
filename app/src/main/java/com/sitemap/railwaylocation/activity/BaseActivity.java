package com.sitemap.railwaylocation.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

import org.xutils.x;

/**
 * activity的基类
 * Created by chenM on 2017/4/28.
 */
public abstract class BaseActivity extends FragmentActivity {
    protected static final String TAG_ESC_ACTIVITY = "com.broader.esc";//内容描述 退出activity时 发送的广播信号
    private MyBroaderEsc receiver;//广播

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置无标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {//设置为竖屏
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        // 注册广播
        receiver = new MyBroaderEsc();
        registerReceiver(receiver, new IntentFilter(TAG_ESC_ACTIVITY));
        // 反射注解机制初始化
        x.view().inject(this);
        initData();
    }

    protected abstract void initData();//初始化数据

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    /**
     * @发送广播 退出activity
     *
     */
    class MyBroaderEsc extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                finish();
            }
        }
    }

}
