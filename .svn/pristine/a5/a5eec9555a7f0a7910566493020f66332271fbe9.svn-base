package com.sitemap.railwaylocation.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;

/**
 * Created by chenMeng on 2017/6/7.
 */

public class GpsUtil {
    private static LocationManager manager;// 定位管理器

    /**
     * 判断GPS是否可用
     *
     * @return
     */
    public static boolean isGPSEnable(Context context) {
        manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }


    /**
     * 自动打开gps
     */
    public static void openGPSSettings(final Context context) {
        LocationManager alm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (alm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("提示");
            builder.setMessage("是否开启GPS？");
            builder.setCancelable(false);
            builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        context.startActivity(intent);
                    } catch (ActivityNotFoundException ex) {
                        intent.setAction(Settings.ACTION_SETTINGS);
                        try {
                            context.startActivity(intent);
                        } catch (Exception e) {
                        }
                    }
                }
            });
            builder.setNegativeButton("否", null);
            builder.show();
        }
    }
}
