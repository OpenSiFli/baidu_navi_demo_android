/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.mapclient.liteapp.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.fragment.app.FragmentActivity;

import com.baidu.mapclient.liteapp.custom.MiniMapViewController;
import com.baidu.navisdk.adapter.BNaviCommonParams;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNRouteGuideManager;
import com.baidu.navisdk.adapter.IBNaviListener;
import com.baidu.navisdk.adapter.IBNaviViewListener;
import com.baidu.navisdk.adapter.struct.BNGuideConfig;
import com.baidu.navisdk.adapter.struct.BNRoutePlanInfos;

/**
 * 诱导界面
 */
public class DemoGuideActivity extends FragmentActivity {

    private static final String TAG = DemoGuideActivity.class.getName();
    public final static String EXTRA_BLE_DEVICE = "EXTRA_BLE_DEVICE";
    private IBNRouteGuideManager mRouteGuideManager;
    private IBNaviListener.DayNightMode mMode = IBNaviListener.DayNightMode.DAY;
    public MiniMapViewController miniMapViewController = new MiniMapViewController();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"DemoGuideActivity onCreate");
        boolean fullScreen = supportFullScreen();
        Bundle params = new Bundle();

        params.putBoolean(BNaviCommonParams.ProGuideKey.IS_SUPPORT_FULL_SCREEN, fullScreen);
        mRouteGuideManager = BaiduNaviManagerFactory.getRouteGuideManager();
        BNGuideConfig config = new BNGuideConfig.Builder()
                .params(params)
                .build();
        View view = mRouteGuideManager.onCreate(this, config);

        if (view != null) {
            setContentView(view);
        }
        routeGuideEvent();
    }

    // 导航过程事件监听
    private void routeGuideEvent() {
        BaiduNaviManagerFactory.getRouteGuideManager().setNaviListener(new IBNaviListener() {

            @Override
            public void onNaviGuideEnd() {
                DemoGuideActivity.this.finish();
            }

            @Override
            public void onYawingSuccess() {
                super.onYawingSuccess();
                Log.e(TAG, "onYawingSuccess");
                BNRoutePlanInfos routePlanInfo = BaiduNaviManagerFactory.getRoutePlanManager().getRoutePlanInfo();
            }
        });

        BaiduNaviManagerFactory.getRouteGuideManager().setNaviViewListener(
                new IBNaviViewListener() {

                    @Override
                    public void onMainInfoPanCLick() {

                    }

                    @Override
                    public void onNaviTurnClick() {

                    }

                    @Override
                    public void onFullViewButtonClick(boolean b) {

                    }

                    @Override
                    public void onFullViewWindowClick(boolean b) {

                    }

                    @Override
                    public void onNaviBackClick() {

                    }

                    @Override
                    public void onBottomBarClick(Action action) {

                    }

                    @Override
                    public void onNaviSettingClick() {

                    }

                    @Override
                    public void onRefreshBtnClick() {

                    }

                    @Override
                    public void onZoomLevelChange(int i) {

                    }

                    @Override
                    public void onMapClicked(double v, double v1) {

                    }

                    @Override
                    public void onMapMoved() {
                        Log.e(TAG, "onMapMoved");
                    }

                    @Override
                    public void onFloatViewClicked() {
                        try {
                            Intent intent = new Intent();
                            intent.setPackage(getPackageName());
                            intent.setClass(DemoGuideActivity.this,
                                    Class.forName(DemoGuideActivity.class.getName()));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                            startActivity(intent);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mRouteGuideManager.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mRouteGuideManager.onResume();
    }

    protected void onPause() {
        super.onPause();
        mRouteGuideManager.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mRouteGuideManager.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRouteGuideManager.onDestroy(false);
        mRouteGuideManager = null;
    }

    @Override
    public void onBackPressed() {
        mRouteGuideManager.onBackPressed(false, true);
    }

    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mRouteGuideManager.onConfigurationChanged(newConfig);
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {

    }

    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if (!mRouteGuideManager.onKeyDown(keyCode, event)) {
            return super.onKeyDown(keyCode, event);
        }
        return true;

    }

    private boolean supportFullScreen() {
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            int color;
            if (Build.VERSION.SDK_INT >= 23) {
                color = Color.TRANSPARENT;
            } else {
                color = 0x2d000000;
            }
            window.setStatusBarColor(color);

            if (Build.VERSION.SDK_INT >= 23) {
                window.getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                int uiVisibility = window.getDecorView().getSystemUiVisibility();
                if (mMode == IBNaviListener.DayNightMode.DAY) {
                    uiVisibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                }
                window.getDecorView().setSystemUiVisibility(uiVisibility);
            } else {
                window.getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            }

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            return true;
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        mRouteGuideManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mRouteGuideManager.onActivityResult(requestCode, resultCode, data);
    }
}
