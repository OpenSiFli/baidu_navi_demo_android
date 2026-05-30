/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.mapclient.liteapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.ViewConfiguration;

import com.baidu.mapclient.liteapp.activity.BNDemoCustomGuideActivity;
import com.baidu.mapclient.liteapp.activity.DemoAnalogActivity;
import com.baidu.mapclient.liteapp.activity.DemoCruiserActivity;
import com.baidu.mapclient.liteapp.activity.DemoDrivingActivity;
import com.baidu.mapclient.liteapp.activity.DemoExtGpsActivity;
import com.baidu.mapclient.liteapp.activity.DemoGuideActivity;
import com.baidu.mapclient.liteapp.activity.DemoNaviSettingActivity;
import com.baidu.mapclient.liteapp.activity.DemoSelectNodeActivity;

public class BNDemoUtils {

    private static final String APP_FOLDER_NAME = "BaiduNavi";

    public static final int NORMAL = 1 << 1;
    public static final int ANALOG = 1 << 2;
    public static final int EXTGPS = 1 << 3;
    public static final int CUSTOM = 1 << 4;

    public static final int LIGHT = 1 << 5;

    /** 用于sp储存 定制icon 开关状态 */
    public static final String KEY_GB_ICONSET = "gb_iconset";
    /** icon显示 开关状态 */
    public static final String KEY_GB_ICONSHOW = "gb_iconshow";
    /** 设置车牌 开关状态 */
    public static final String KEY_GB_CARNUM = "gb_carnum";
    /** 设置的车牌 */
    public static final String KEY_GB_CARNUMTXT = "gb_carnumtxt";
    /** 车标偏移 开关状态 */
    public static final String KEY_GB_CARICONOFFSET = "gb_cariconoffset";
    /** 车标偏移x坐标 */
    public static final String KEY_GB_CARICONOFFSET_X = "gb_cariconoffset_x";
    /** 车标偏移y坐标 */
    public static final String KEY_GB_CARICONOFFSET_Y = "gb_cariconoffset_y";
    /** 屏幕边距 开关状态 */
    public static final String KEY_GB_MARGIN = "gb_margin";
    /** 路线偏好 开关状态 */
    public static final String KEY_GB_ROUTE_SORT = "gb_routeSort";
    /** 沿途检索 开关状态 */
    public static final String KEY_GB_ROUTE_SEARCH = "gb_routeSearch";
    /** 更多设置 开关状态 */
    public static final String KEY_GB_MORE_SETTINGS = "gb_moreSettings";
    /** 用于sp储存 定制icon 开关状态 */
    public static final String KEY_GB_SEEALL = "gb_seeall";
    public static final String KEY_GB_BOTTOM_BAR_TYPE = "gb_bottom_bar_type";
    public static final String KEY_GB_UGC_REPORT_TYPE = "gb_ugc_report_type";

    public static final String KEY_GB_MINI_MAP_TYPE = "gb_mini_map_type";

    public static final String KEY_GB_MINI_MAP_LICENSE_RESULT = "gb_mini_map_license_result";
    public static final String KEY_GB_MINI_MAP_LICENSE_SWITCH = "gb_mini_map_license_switch";
    public static final String KEY_GB_MINI_MAP_OPEN_BG_DRAW = "gb_mini_map_open_bg_draw";


    public static void gotoNavi(Activity activity,String targetMac) {
//        Intent it = new Intent(activity, DemoGuideActivity.class);
//        if(targetMac != null) it.putExtra(DemoGuideActivity.EXTRA_BLE_DEVICE, targetMac);
//        activity.startActivity(it);
        Intent intent = new Intent(activity, DemoAnalogActivity.class);
        if(targetMac != null) intent.putExtra(DemoAnalogActivity.EXTRA_BLE_DEVICE, targetMac);
        intent.putExtra(DemoAnalogActivity.EXTRA_IS_REAL_NAV,true);
        activity.startActivity(intent);
    }

    public static void gotoExtGps(Activity activity) {
        Intent it = new Intent(activity, DemoExtGpsActivity.class);
        activity.startActivity(it);
    }

    public static void gotoAnalog(Activity activity,String targetMac,boolean isAutoStart) {
        Intent intent = new Intent(activity, DemoAnalogActivity.class);
        if(targetMac != null) intent.putExtra(DemoAnalogActivity.EXTRA_BLE_DEVICE, targetMac);
        intent.putExtra(DemoAnalogActivity.EXTRA_IS_REAL_NAV,false);
        intent.putExtra(DemoAnalogActivity.EXTRA_AUTO_START,isAutoStart);
        activity.startActivity(intent);
    }

    public static void gotoCustomGuide(Activity activity, int topHeight, int bottomHeight) {
        Intent it = new Intent(activity, BNDemoCustomGuideActivity.class);
        Bundle bundle = new Bundle();
        if (topHeight != 0) {
            bundle.putInt("custom_view_top_height"
                    , topHeight);
        }
        if (bottomHeight != 0) {
            bundle.putInt("custom_view_bottom_height"
                    , bottomHeight);
        }
        it.putExtras(bundle);
        activity.startActivity(it);
    }

    public static void gotoSettings(Activity activity) {
        Intent it = new Intent(activity, DemoNaviSettingActivity.class);
        activity.startActivity(it);
    }

    public static void gotoDriving(Activity activity) {
        Intent it = new Intent(activity, DemoDrivingActivity.class);
        activity.startActivity(it);
    }

    public static void gotoCruiser(Activity activity) {
        Intent it = new Intent(activity, DemoCruiserActivity.class);
        activity.startActivity(it);
    }

    public static void gotoSelectNode(Activity activity) {
        Intent it = new Intent(activity, DemoSelectNodeActivity.class);
        activity.startActivity(it);
    }

    public static String getTTSAppID() {
        return "11213224";
    }

    public static String getTTSAppKey() {
        return "gT2XSUgoMFysCzwLCUtrIItTUdclThsf";
    }

    public static String getTTSsecretKey() {
        return "MEokc3O8y95Lh9fOLX7lrxY1jD9OkWFf";
    }

    public static String getAuth() {
        return "8092f102-684cde5d-01-0050-006d-0091-01";
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static boolean checkDeviceHasNavigationBar(Activity context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Display display = context.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            Point realSize = new Point();
            display.getSize(size);
            display.getRealSize(realSize);
            return realSize.y != size.y;
        } else {
            boolean hasMenuKey = ViewConfiguration.get(context)
                    .hasPermanentMenuKey();
            boolean hasBackKey = KeyCharacterMap
                    .deviceHasKey(KeyEvent.KEYCODE_BACK);
            return hasMenuKey || hasBackKey;
        }
    }

    public static int getNavigationBarHeight(Activity context) {
        int result = 0;
        if (checkDeviceHasNavigationBar(context)) {
            Resources res = context.getResources();
            int resourceId = res.getIdentifier("navigation_bar_height",
                    "dimen", "android");
            if (resourceId > 0) {
                result = res.getDimensionPixelSize(resourceId);
            }
        }
        return result;
    }

    public static void setString(Context context, String key, String value) {
        SharedPreferences sp =
                context.getSharedPreferences(APP_FOLDER_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getString(Context context, String key) {
        SharedPreferences sp =
                context.getSharedPreferences(APP_FOLDER_NAME, Context.MODE_PRIVATE);
        return sp.getString(key, "");
    }

    public static void setBoolean(Context context, String key, boolean value) {
        SharedPreferences sp =
                context.getSharedPreferences(APP_FOLDER_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static Boolean getBoolean(Context context, String key) {
        SharedPreferences sp =
                context.getSharedPreferences(APP_FOLDER_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(key, false);
    }

    public static Boolean getBoolean(Context context, String key, boolean defaultreturn) {
        SharedPreferences sp =
                context.getSharedPreferences(APP_FOLDER_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(key, defaultreturn);
    }

}
