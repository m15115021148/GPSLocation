package com.sitemap.railwaylocation.application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;


import com.baidu.mapapi.SDKInitializer;
import com.sitemap.railwaylocation.model.UserModel;
import com.sitemap.railwaylocation.util.NetworkUtil;

import org.xutils.BuildConfig;
import org.xutils.x;

/**
 * Created by chenmeng on 2016/10/18.
 */
public class MyApplication extends Application{
    /**application对象*/
    private static MyApplication instance;
    public static NetworkUtil netState;//网络状态
    public static UserModel userModel;//登录实体类
//    public static LocationService locationService;
    public static double lat = 0;// 纬度
    public static double lng = 0;// 经度
    public static int gpsTime;//gps time
    public static String mac = "";//mac地址
    public static boolean isupdate = true;// 是否请求更新
    private boolean isDownload;// 是否在下载
    public static String downUrl = "";// 下载地址
    public static String versionName;// 版本号
    public static double length;// apk大小

    public boolean isDownload() {
        return isDownload;
    }

    public void setDownload(boolean isDownload) {
        this.isDownload = isDownload;
    }

    public static int screenWidth = 0;

    public static int screenHeight = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        initXutils();
        getScreenSize();
        netState = new NetworkUtil(getApplicationContext());
        SDKInitializer.initialize(getApplicationContext());
//        JPushInterface.setDebugMode(true);// 设置开启日志,发布时请关闭日志
//        JPushInterface.init(this);
    }

    private static MyApplication instance() {
        if (instance != null) {
            return instance;
        } else {
            return new MyApplication();
        }
    }

    /**
     * 获取屏幕尺寸
     */
    private void getScreenSize() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenHeight = dm.heightPixels;
        screenWidth = dm.widthPixels;
    }

    /**
     * 初始化xutils框架
     */
    private void initXutils() {
        x.Ext.init(this);
        x.Ext.setDebug(BuildConfig.DEBUG); // 是否输出debug日志, 开启debug会影响性能.
    }

    /**
     * 获取手机网络状态对象
     *
     * @return
     */
    public static NetworkUtil getNetObject() {
        if (netState != null) {
            return netState;
        } else {
            return new NetworkUtil(instance().getApplicationContext());
        }
    }

    /**
     * listview没有数据显示 的控件
     * @param context 本类
     * @param T AbsListView
     * @param txt 内容
     */
    public static View setEmptyShowText(Context context, AbsListView T, String txt){
        TextView emptyView = new TextView(context);
        emptyView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        emptyView.setText(txt);
        emptyView.setTextSize(18);
        emptyView.setTextColor(Color.parseColor("#808080"));
        emptyView.setGravity(Gravity.CENTER_HORIZONTAL| Gravity.CENTER_VERTICAL);
        emptyView.setVisibility(View.GONE);
        ((ViewGroup)T.getParent()).addView(emptyView);
        T.setEmptyView(emptyView);
        return emptyView;
    }

    /**
     * 获取手机设备id
     */
    public static String getDeviceID(Activity activity){
        TelephonyManager tm = (TelephonyManager)activity.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

}
