/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.mapclient.liteapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.baidu.mapclient.liteapp.BNDemoUtils;
import com.baidu.mapclient.liteapp.R;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNOuterSettingParams;
import com.baidu.navisdk.adapter.IBNRoutePlanManager;
import com.baidu.navisdk.comapi.setting.SettingParams;

public class DemoNaviSettingActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "DemoNaviSettingActivity";

    /** 导航视角 */
    private static final int GUIDE_VIEW_OPTION_CNT = 2;
    private static final int GUIDE_VIEW_3D_INDEX = 0;
    private static final int GUIDE_VIEW_2D_INDEX = 1;
    private View[] mGuideViewModeViews = new View[GUIDE_VIEW_OPTION_CNT];
    private TextView[] mGuideViewModeTVs = new TextView[GUIDE_VIEW_OPTION_CNT];
    /** 日夜模式 */
    private static final int DAY_NIGHT_MODE_OPTION_CNT = 3;
    private static final int AUTO_MODE_INDEX = 0;
    private static final int DAY_MODE_INDEX = 1;
    private static final int NIGHT_MODE_INDEX = 2;
    private View[] mDayNightModeViews = new View[DAY_NIGHT_MODE_OPTION_CNT];
    private TextView[] mDayNightModeTVs = new TextView[DAY_NIGHT_MODE_OPTION_CNT];
    /** 导航中图面显示 */
    private static final int NAV_DISPLAY_MODE_OPTION_CNT = 2;
    private static final int NAV_DISPLAY_OVERVIEW_INDEX = 0;
    private static final int NAV_DISPLAY_ROAD_COND_BAR_INDEX = 1;
    private View[] mNavDisplayModeViews = new View[NAV_DISPLAY_MODE_OPTION_CNT];
    private TextView[] mNavDisplayModeTVs = new TextView[NAV_DISPLAY_MODE_OPTION_CNT];

    /** 列表设置总数 */
    private static final int LIST_OPTION_CNT = 30;
    /** 智能比例尺 */
    private static final int SCALE_INDEX = 0;
    /** 多路线 */
    private static final int MULTI_ROUTE_INDEX = 1;
    /** 行中路名 */
    private static final int ROAD_NAME_INDEX = 2;
    /** 车道线 */
    private static final int LANE_LINE_INDEX = 3;
    /** 区间测试 */
    private static final int MEASUREMENT_INDEX = 4;
    /** 高速面板 */
    private static final int HIGHWAY_INDEX = 5;
    /** 主辅路桥上桥下 */
    private static final int MAIN_AUXILIARY_OR_BRIDGE = 6;
    /** 导航工具箱 */
    private static final int BOTTOM_BAR_INDEX = 7;
    /** 路况按钮 */
    private static final int ROAD_CONDITION_INDEX = 8;
    /** 导航播报按钮 */
    private static final int VOICE_BUTTON_INDEX = 9;
    /** 路线刷新按钮 */
    private static final int REFRESH_BUTTON_INDEX = 10;
    /** 底图缩放按钮 */
    private static final int ZOOM_BUTTON_INDEX = 11;
    /** 底图缩放按钮 */
    private static final int FLOAT_WINDOW_INDEX = 12;


    private CheckBox[] mCheckBox = new CheckBox[LIST_OPTION_CNT];
    private boolean[] mIsChecked = new boolean[LIST_OPTION_CNT];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initUserConfig();
        initViews();
        initClickListener();
    }

    private void initUserConfig() {
        mIsChecked[SCALE_INDEX] = BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                .isAutoScale(IBNRoutePlanManager.Vehicle.CAR);
        mIsChecked[MULTI_ROUTE_INDEX] = BaiduNaviManagerFactory.getCommonSettingManager()
                .isMultiRouteEnable();
        mIsChecked[ROAD_NAME_INDEX] = BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                .isRoadNameEnable();
        mIsChecked[LANE_LINE_INDEX] = BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                .isLaneLineEnable();
        mIsChecked[MEASUREMENT_INDEX] = BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                .isMeasurementEnable();
        mIsChecked[HIGHWAY_INDEX] = BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                .isHighwayEnable();
        mIsChecked[MAIN_AUXILIARY_OR_BRIDGE] =
                BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                        .isShowMainAuxiliaryOrBridge();
        mIsChecked[BOTTOM_BAR_INDEX] = BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                .isBottomBarOpen();
        mIsChecked[ROAD_CONDITION_INDEX] =
                BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                        .isRoadConditionButtonVisible();
        mIsChecked[VOICE_BUTTON_INDEX] =
                BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                        .isVoiceButtonVisible();
        mIsChecked[REFRESH_BUTTON_INDEX] =
                BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                        .isRefreshButtonVisible();
        mIsChecked[ZOOM_BUTTON_INDEX] =
                BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                        .isZoomButtonVisible();
        mIsChecked[FLOAT_WINDOW_INDEX] = BNDemoUtils.getBoolean(this, "float_window"); // 12

        // 定制icon
        mIsChecked[13] = BNDemoUtils.getBoolean(this, BNDemoUtils.KEY_GB_ICONSET);
        // icon显示
        mIsChecked[14] = BNDemoUtils.getBoolean(this, BNDemoUtils.KEY_GB_ICONSHOW, true);
        // 设置车牌
        mIsChecked[15] = BNDemoUtils.getBoolean(this, BNDemoUtils.KEY_GB_CARNUM);
        carnum = BNDemoUtils.getString(this, BNDemoUtils.KEY_GB_CARNUMTXT);
        // 终点连线 isShowCarLogoToEndRedLine
        mIsChecked[16] = BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                .isShowCarLogoToEndRedLine(IBNRoutePlanManager.Vehicle.CAR);
        // 路口放大图
        mIsChecked[17] = BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                .isShowRoadEnlargeView(IBNRoutePlanManager.Vehicle.CAR);
        // 抵达退出  todo 新加的接口 获取开关状态 其他版本不知道有没有
        mIsChecked[18] = BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                .isAutoQuitWhenArrived();
        // 车标偏移
        mIsChecked[19] = BNDemoUtils.getBoolean(this, BNDemoUtils.KEY_GB_CARICONOFFSET);
        cariconoffsetx = BNDemoUtils.getString(this, BNDemoUtils.KEY_GB_CARICONOFFSET_X);
        cariconoffsety = BNDemoUtils.getString(this, BNDemoUtils.KEY_GB_CARICONOFFSET_Y);
        // 屏幕边距
        mIsChecked[20] = BNDemoUtils.getBoolean(this, BNDemoUtils.KEY_GB_MARGIN);
        // 路线偏好 默认为开启 单次生效
        mIsChecked[21] = BNDemoUtils.getBoolean(this, BNDemoUtils.KEY_GB_ROUTE_SORT, true);
        // 沿途检索 默认为开启 单次生效
        mIsChecked[22] = BNDemoUtils.getBoolean(this, BNDemoUtils.KEY_GB_ROUTE_SEARCH, true);
        // 更多设置 默认开启  单次生效
        mIsChecked[23] = BNDemoUtils.getBoolean(this, BNDemoUtils.KEY_GB_MORE_SETTINGS, true);
        // 进入全览
        mIsChecked[24] = BNDemoUtils.getBoolean(this, BNDemoUtils.KEY_GB_SEEALL);
        // 退出按钮
        mIsChecked[25] = BNDemoUtils.getBoolean(this, BNDemoUtils.KEY_GB_BOTTOM_BAR_TYPE);
        // ugc上报按钮
        mIsChecked[26] = BNDemoUtils.getBoolean(this, BNDemoUtils.KEY_GB_UGC_REPORT_TYPE);
        // 多实例底图
        mIsChecked[27] = BNDemoUtils.getBoolean(this, BNDemoUtils.KEY_GB_MINI_MAP_TYPE);

        mIsChecked[28] = BNDemoUtils.getBoolean(this, BNDemoUtils.KEY_GB_MINI_MAP_LICENSE_SWITCH, true);

        mIsChecked[29] = BNDemoUtils.getBoolean(this, BNDemoUtils.KEY_GB_MINI_MAP_OPEN_BG_DRAW, true);
    }

    String cariconoffsetx;
    String cariconoffsety;
    String carnum;

    EditText carnumEdit;
    EditText offsetx;
    EditText offsety;

    private void initViews() {
        // 导航视角
        mGuideViewModeViews[GUIDE_VIEW_3D_INDEX] = findViewById(R.id.bnav_view_car3d_layout);
        mGuideViewModeViews[GUIDE_VIEW_2D_INDEX] = findViewById(R.id.bnav_view_north2d_layout);
        mGuideViewModeTVs[GUIDE_VIEW_3D_INDEX] = findViewById(R.id.bnav_view_car3d_tv);
        mGuideViewModeTVs[GUIDE_VIEW_2D_INDEX] = findViewById(R.id.bnav_view_north2d_tv);

        // 日夜模式
        mDayNightModeViews[AUTO_MODE_INDEX] = findViewById(R.id.bnav_auto_mode_layout);
        mDayNightModeViews[DAY_MODE_INDEX] = findViewById(R.id.bnav_day_mode_layout);
        mDayNightModeViews[NIGHT_MODE_INDEX] = findViewById(R.id.bnav_night_mode_layout);
        mDayNightModeTVs[AUTO_MODE_INDEX] = findViewById(R.id.bnav_auto_mode_tv);
        mDayNightModeTVs[DAY_MODE_INDEX] = findViewById(R.id.bnav_day_mode_tv);
        mDayNightModeTVs[NIGHT_MODE_INDEX] = findViewById(R.id.bnav_night_mode_tv);

        // 导航中图面显示
        mNavDisplayModeViews[NAV_DISPLAY_OVERVIEW_INDEX] = findViewById(R.id
                .bnav_display_overview_mode_layout);
        mNavDisplayModeTVs[NAV_DISPLAY_OVERVIEW_INDEX] = findViewById(R.id
                .nav_display_overview_mode_tv);

        mNavDisplayModeViews[NAV_DISPLAY_ROAD_COND_BAR_INDEX] = findViewById(R.id
                .bnav_display_road_cond_mode_layout);
        mNavDisplayModeTVs[NAV_DISPLAY_ROAD_COND_BAR_INDEX] = findViewById(R.id
                .nav_display_road_condition_mode_tv);

        mCheckBox[SCALE_INDEX] = findViewById(R.id.nav_scale_cb);
        mCheckBox[MULTI_ROUTE_INDEX] = findViewById(R.id.nav_multi_route_cb);
        mCheckBox[ROAD_NAME_INDEX] = findViewById(R.id.nav_route_name_cb);
        mCheckBox[LANE_LINE_INDEX] = findViewById(R.id.nav_lane_line_cb);
        mCheckBox[MEASUREMENT_INDEX] = findViewById(R.id.nav_measurement_cb);
        mCheckBox[HIGHWAY_INDEX] = findViewById(R.id.nav_highway_cb);
        mCheckBox[MAIN_AUXILIARY_OR_BRIDGE] = findViewById(R.id.nav_bridge_cb);
        mCheckBox[BOTTOM_BAR_INDEX] = findViewById(R.id.nav_bottom_bar_cb);
        mCheckBox[ROAD_CONDITION_INDEX] = findViewById(R.id.nav_road_condition_cb);
        mCheckBox[VOICE_BUTTON_INDEX] = findViewById(R.id.nav_voice_cb);
        mCheckBox[REFRESH_BUTTON_INDEX] = findViewById(R.id.nav_refresh_cb);
        mCheckBox[ZOOM_BUTTON_INDEX] = findViewById(R.id.nav_zoom_cb);
        mCheckBox[FLOAT_WINDOW_INDEX] = findViewById(R.id.float_window_cb);

        mCheckBox[13] = findViewById(R.id.nav_iconset_cb);
        mCheckBox[14] = findViewById(R.id.nav_iconvisibility_cb);
        mCheckBox[15] = findViewById(R.id.nav_carnum_cb);
        mCheckBox[16] = findViewById(R.id.nav_redline_cb);
        mCheckBox[17] = findViewById(R.id.nav_RoadEnlargeView_cb);
        mCheckBox[18] = findViewById(R.id.nav_AutoQuit_cb);
        mCheckBox[19] = findViewById(R.id.nav_CarIconOffset_cb);
        mCheckBox[20] = findViewById(R.id.nav_margin_cb);
        mCheckBox[21] = findViewById(R.id.nav_RouteSort_cb);
        mCheckBox[22] = findViewById(R.id.nav_RouteSearch_cb);
        mCheckBox[23] = findViewById(R.id.nav_moreset_cb);
        mCheckBox[24] = findViewById(R.id.nav_seeallwhenbegin_cb);
        mCheckBox[25] = findViewById(R.id.nav_bottom_bar_type_cb);
        mCheckBox[26] = findViewById(R.id.nav_ugc_report_cb);
        mCheckBox[27] = findViewById(R.id.nav_minimap_cb);
        mCheckBox[28] = findViewById(R.id.nav_minimap_license_cb);
        mCheckBox[29] = findViewById(R.id.nav_minimap_open_bg_draw_cb);
        carnumEdit = findViewById(R.id.carnum_edit);
        offsetx = findViewById(R.id.offset_x);
        offsety = findViewById(R.id.offset_y);

        carnumEdit.setText(carnum);
        offsetx.setText(cariconoffsetx);
        offsety.setText(cariconoffsety);



        updateDayNightModeView(BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                .getDayNightMode(IBNRoutePlanManager.Vehicle.CAR));
        updateGuideViewModeView(BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                .getGuideViewMode(IBNRoutePlanManager.Vehicle.CAR));
        updateNavDisplayViewModeView(BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                .getFullViewMode(IBNRoutePlanManager.Vehicle.CAR));

        for (int i = 0; i < LIST_OPTION_CNT; i++) {
            mCheckBox[i].setChecked(mIsChecked[i]);
            mCheckBox[i].setOnClickListener(this);
        }
    }

    private void initClickListener() {
        findViewById(R.id.bnav_view_north2d_layout).setOnClickListener(this);
        findViewById(R.id.bnav_view_car3d_layout).setOnClickListener(this);

        findViewById(R.id.bnav_auto_mode_layout).setOnClickListener(this);
        findViewById(R.id.bnav_day_mode_layout).setOnClickListener(this);
        findViewById(R.id.bnav_night_mode_layout).setOnClickListener(this);

        findViewById(R.id.nav_scale_cb).setOnClickListener(this);
        findViewById(R.id.nav_multi_route_cb).setOnClickListener(this);
        findViewById(R.id.nav_route_name_cb).setOnClickListener(this);
        findViewById(R.id.nav_lane_line_cb).setOnClickListener(this);
        findViewById(R.id.nav_measurement_cb).setOnClickListener(this);
        findViewById(R.id.nav_highway_cb).setOnClickListener(this);
        findViewById(R.id.nav_bridge_cb).setOnClickListener(this);
        findViewById(R.id.nav_bottom_bar_cb).setOnClickListener(this);
        findViewById(R.id.nav_road_condition_cb).setOnClickListener(this);
        findViewById(R.id.nav_voice_cb).setOnClickListener(this);
        findViewById(R.id.nav_refresh_cb).setOnClickListener(this);
        findViewById(R.id.nav_zoom_cb).setOnClickListener(this);

        findViewById(R.id.nav_minimap_cb).setOnClickListener(this);





        findViewById(R.id.bnav_display_overview_mode_layout).setOnClickListener(this);
        findViewById(R.id.bnav_display_road_cond_mode_layout).setOnClickListener(this);
    }

    private void updateDayNightModeView(int mode) {
        mDayNightModeViews[AUTO_MODE_INDEX].setSelected(mode == SettingParams.Action
                .DAY_NIGHT_MODE_AUTO);
        mDayNightModeTVs[AUTO_MODE_INDEX].setSelected(mode == SettingParams.Action
                .DAY_NIGHT_MODE_AUTO);

        mDayNightModeViews[DAY_MODE_INDEX].setSelected(mode == SettingParams.Action
                .DAY_NIGHT_MODE_DAY);
        mDayNightModeTVs[DAY_MODE_INDEX].setSelected(mode == SettingParams.Action
                .DAY_NIGHT_MODE_DAY);

        mDayNightModeViews[NIGHT_MODE_INDEX].setSelected(mode == SettingParams.Action
                .DAY_NIGHT_MODE_NIGHT);
        mDayNightModeTVs[NIGHT_MODE_INDEX].setSelected(mode == SettingParams.Action
                .DAY_NIGHT_MODE_NIGHT);
    }

    private void updateGuideViewModeView(int mode) {
        mGuideViewModeViews[GUIDE_VIEW_3D_INDEX].setSelected(mode == SettingParams.MapMode.CAR_3D);
        mGuideViewModeTVs[GUIDE_VIEW_3D_INDEX].setSelected(mode == SettingParams.MapMode.CAR_3D);

        mGuideViewModeViews[GUIDE_VIEW_2D_INDEX]
                .setSelected(mode == SettingParams.MapMode.NORTH_2D);
        mGuideViewModeTVs[GUIDE_VIEW_2D_INDEX].setSelected(mode == SettingParams.MapMode.NORTH_2D);
    }

    private void updateNavDisplayViewModeView(int mode) {
        mNavDisplayModeViews[NAV_DISPLAY_OVERVIEW_INDEX]
                .setSelected(mode == IBNOuterSettingParams.PreViewMode.MapMini);
        mNavDisplayModeTVs[NAV_DISPLAY_OVERVIEW_INDEX]
                .setSelected(mode == IBNOuterSettingParams.PreViewMode.MapMini);

        mNavDisplayModeViews[NAV_DISPLAY_ROAD_COND_BAR_INDEX]
                .setSelected(mode == IBNOuterSettingParams.PreViewMode.RoadBar);
        mNavDisplayModeTVs[NAV_DISPLAY_ROAD_COND_BAR_INDEX]
                .setSelected(mode == IBNOuterSettingParams.PreViewMode.RoadBar);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View v) {
        if (v == null) {
            return;
        }
        boolean isOpen = false;
        switch (v.getId()) {
            case R.id.bnav_view_car3d_layout:
                updateGuideViewModeView(SettingParams.MapMode.CAR_3D);
                BaiduNaviManagerFactory.getProfessionalNaviSettingManager().setGuideViewMode(
                        SettingParams.MapMode.CAR_3D, IBNRoutePlanManager.Vehicle.CAR);
                break;
            case R.id.bnav_view_north2d_layout:
                updateGuideViewModeView(SettingParams.MapMode.NORTH_2D);
                BaiduNaviManagerFactory.getProfessionalNaviSettingManager().setGuideViewMode(
                        SettingParams.MapMode.NORTH_2D, IBNRoutePlanManager.Vehicle.CAR);
                break;
            case R.id.bnav_auto_mode_layout:
                updateDayNightModeView(SettingParams.Action.DAY_NIGHT_MODE_AUTO);
                BaiduNaviManagerFactory.getProfessionalNaviSettingManager().setDayNightMode(
                        SettingParams.Action.DAY_NIGHT_MODE_AUTO, IBNRoutePlanManager.Vehicle.CAR);
                BaiduNaviManagerFactory.getProfessionalNaviSettingManager().setDayNightMode(
                        SettingParams.Action.DAY_NIGHT_MODE_AUTO, IBNRoutePlanManager.Vehicle.TRUCK);
                BaiduNaviManagerFactory.getProfessionalNaviSettingManager().setDayNightMode(
                        SettingParams.Action.DAY_NIGHT_MODE_AUTO, IBNRoutePlanManager.Vehicle.MOTOR);
                break;
            case R.id.bnav_day_mode_layout:
                updateDayNightModeView(SettingParams.Action.DAY_NIGHT_MODE_DAY);
                BaiduNaviManagerFactory.getProfessionalNaviSettingManager().setDayNightMode(
                        SettingParams.Action.DAY_NIGHT_MODE_DAY, IBNRoutePlanManager.Vehicle.CAR);
                BaiduNaviManagerFactory.getProfessionalNaviSettingManager().setDayNightMode(
                        SettingParams.Action.DAY_NIGHT_MODE_DAY, IBNRoutePlanManager.Vehicle.TRUCK);
                BaiduNaviManagerFactory.getProfessionalNaviSettingManager().setDayNightMode(
                        SettingParams.Action.DAY_NIGHT_MODE_DAY, IBNRoutePlanManager.Vehicle.MOTOR);
                break;
            case R.id.bnav_night_mode_layout:
                updateDayNightModeView(SettingParams.Action.DAY_NIGHT_MODE_NIGHT);
                BaiduNaviManagerFactory.getProfessionalNaviSettingManager().setDayNightMode(
                        SettingParams.Action.DAY_NIGHT_MODE_NIGHT, IBNRoutePlanManager.Vehicle.CAR);
                BaiduNaviManagerFactory.getProfessionalNaviSettingManager().setDayNightMode(
                        SettingParams.Action.DAY_NIGHT_MODE_NIGHT, IBNRoutePlanManager.Vehicle.TRUCK);
                BaiduNaviManagerFactory.getProfessionalNaviSettingManager().setDayNightMode(
                        SettingParams.Action.DAY_NIGHT_MODE_NIGHT, IBNRoutePlanManager.Vehicle.MOTOR);
                break;
            case R.id.bnav_display_overview_mode_layout:
                updateNavDisplayViewModeView(IBNOuterSettingParams.PreViewMode.MapMini);
                BaiduNaviManagerFactory.getProfessionalNaviSettingManager().setFullViewMode(
                        IBNOuterSettingParams.PreViewMode.MapMini, IBNRoutePlanManager.Vehicle.CAR);
                break;
            case R.id.bnav_display_road_cond_mode_layout:
                updateNavDisplayViewModeView(IBNOuterSettingParams.PreViewMode.RoadBar);
                BaiduNaviManagerFactory.getProfessionalNaviSettingManager().setFullViewMode(
                        IBNOuterSettingParams.PreViewMode.RoadBar, IBNRoutePlanManager.Vehicle.CAR);
                break;
            case R.id.nav_scale_cb: // 地图智能缩放
                isOpen = mCheckBox[SCALE_INDEX].isChecked();
                BaiduNaviManagerFactory.getProfessionalNaviSettingManager().setAutoScale(
                        isOpen, IBNRoutePlanManager.Vehicle.CAR);
                break;
            case R.id.nav_multi_route_cb: // 多路线推荐
                isOpen = mCheckBox[MULTI_ROUTE_INDEX].isChecked();
                BaiduNaviManagerFactory.getCommonSettingManager().setMultiRouteEnable(isOpen);
                break;
            case R.id.nav_route_name_cb: // 当前道路名
                isOpen = mCheckBox[ROAD_NAME_INDEX].isChecked();
                BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                        .setRoadNameEnable(isOpen);
                break;
            case R.id.nav_lane_line_cb: // 车道线
                isOpen = mCheckBox[LANE_LINE_INDEX].isChecked();
                BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                        .setLaneLineEnable(isOpen);
                break;
            case R.id.nav_measurement_cb: // 区间测速
                isOpen = mCheckBox[MEASUREMENT_INDEX].isChecked();
                BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                        .setMeasurementEnable(isOpen);
                break;
            case R.id.nav_highway_cb: // 高速面板
                isOpen = mCheckBox[HIGHWAY_INDEX].isChecked();
                BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                        .setHighwayEnable(isOpen);
                break;
            case R.id.nav_bridge_cb: // 主辅路 桥上桥下 按钮
                BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                        .setShowMainAuxiliaryOrBridge(isOpen);
                break;
            case R.id.nav_bottom_bar_cb: // 工具箱更多
                isOpen = mCheckBox[BOTTOM_BAR_INDEX].isChecked();
                BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                        .enableBottomBarOpen(isOpen);
                break;
            case R.id.nav_road_condition_cb: // 路况按钮
                isOpen = mCheckBox[ROAD_CONDITION_INDEX].isChecked();
                BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                        .setRoadConditionButtonVisible(isOpen);
                break;
            case R.id.nav_voice_cb: // 导航播报按钮
                isOpen = mCheckBox[VOICE_BUTTON_INDEX].isChecked();
                BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                        .setVoiceButtonVisible(isOpen);
                break;
            case R.id.nav_refresh_cb: // 路线刷新按钮
                isOpen = mCheckBox[REFRESH_BUTTON_INDEX].isChecked();
                BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                        .setRefreshButtonVisible(isOpen);
                break;
            case R.id.nav_zoom_cb: // 地图缩放按钮
                isOpen = mCheckBox[ZOOM_BUTTON_INDEX].isChecked();
                BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                        .setZoomButtonVisible(isOpen);
                break;
            case R.id.float_window_cb: // 诱导悬浮窗
                isOpen = mCheckBox[FLOAT_WINDOW_INDEX].isChecked();
                BNDemoUtils.setBoolean(DemoNaviSettingActivity.this,
                        "float_window", isOpen);
                if (isOpen && !Settings.canDrawOverlays(this)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, 1234);
                }
                break;
            case R.id.nav_iconset_cb: // 定制icon 修改了车标
                isOpen = mCheckBox[13].isChecked();
                BNDemoUtils.setBoolean(DemoNaviSettingActivity.this, BNDemoUtils.KEY_GB_ICONSET, isOpen);
                if (isOpen) {
                    Bitmap bitmap = BNDemoLightNaviActivity.getbitmap(this, "car.png");
                    BaiduNaviManagerFactory.getCommonSettingManager()
                            .setDIYImageToMap(bitmap,
                                    IBNOuterSettingParams.DIYImageType.CarLogo);

                } else {
                    BaiduNaviManagerFactory.getCommonSettingManager().
                            clearDIYImage(IBNOuterSettingParams.DIYImageType.CarLogo);
                }

                break;
            case R.id.nav_iconvisibility_cb: // icon显示
                isOpen = mCheckBox[14].isChecked();
                BNDemoUtils.setBoolean(DemoNaviSettingActivity.this, BNDemoUtils.KEY_GB_ICONSHOW, isOpen);
                BaiduNaviManagerFactory.getCommonSettingManager()
                        .setDIYImageStatus(isOpen, IBNOuterSettingParams.DIYImageType.CarLogo);
                break;
            case R.id.nav_carnum_cb: // 车牌号
                isOpen = mCheckBox[15].isChecked();
                BNDemoUtils.setBoolean(DemoNaviSettingActivity.this, BNDemoUtils.KEY_GB_CARNUM, isOpen);
                if (carnumEdit.getText() != null && isOpen) {
                    BaiduNaviManagerFactory.getCommonSettingManager()
                            .setCarNum(carnumEdit.getText().toString());
                    BNDemoUtils.setString(DemoNaviSettingActivity.this, BNDemoUtils.KEY_GB_CARNUMTXT,
                            carnumEdit.getText().toString());
                } else {
                    BaiduNaviManagerFactory.getCommonSettingManager()
                            .setCarNum("");
                    mCheckBox[15].setChecked(false);
                }
                break;
            case R.id.nav_redline_cb: // 终点连线
                isOpen = mCheckBox[16].isChecked();
                BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                        .setShowCarLogoToEndRedLine(isOpen, IBNRoutePlanManager.Vehicle.CAR);
                break;
            case R.id.nav_RoadEnlargeView_cb: // 路口放大图
                isOpen = mCheckBox[17].isChecked();
                BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                        .setShowRoadEnlargeView(isOpen, IBNRoutePlanManager.Vehicle.CAR);
                break;
            case R.id.nav_AutoQuit_cb: // 自动退出
                isOpen = mCheckBox[18].isChecked();
                BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                        .setIsAutoQuitWhenArrived(isOpen);
                break;
            case R.id.nav_CarIconOffset_cb: // 车标偏移
                isOpen = mCheckBox[19].isChecked();
                BNDemoUtils.setBoolean(DemoNaviSettingActivity.this, BNDemoUtils.KEY_GB_CARICONOFFSET, isOpen);
                if (isOpen) {
                    BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                            .setCarIconOffsetForNavi(Integer.valueOf(offsetx.getText().toString()),
                                    Integer.valueOf(offsety.getText().toString()));
                    BNDemoUtils.setString(DemoNaviSettingActivity.this,
                            BNDemoUtils.KEY_GB_CARICONOFFSET_X, offsetx.getText().toString());
                    BNDemoUtils.setString(DemoNaviSettingActivity.this,
                            BNDemoUtils.KEY_GB_CARICONOFFSET_Y, offsety.getText().toString());
                } else {
                    BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                            .setCarIconOffsetForNavi(0, 0);
                }
                break;
            case R.id.nav_margin_cb: // 屏幕边距
                isOpen = mCheckBox[20].isChecked();
                BNDemoUtils.setBoolean(DemoNaviSettingActivity.this, BNDemoUtils.KEY_GB_MARGIN, isOpen);
                if (isOpen) {
                    BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                            .setFullViewMarginSize(40, 40, 40, 40);
                } else {
                    BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                            .setCarIconOffsetForNavi(0, 0);
                }
                break;
            case R.id.nav_RouteSort_cb: // 路线偏好 进入前 单次生效
                isOpen = mCheckBox[21].isChecked();
                BNDemoUtils.setBoolean(DemoNaviSettingActivity.this, BNDemoUtils.KEY_GB_ROUTE_SORT, isOpen);
                BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                        .enableRouteSort(isOpen);
                break;
            case R.id.nav_RouteSearch_cb: // 沿途检索 进入前 单次生效
                isOpen = mCheckBox[22].isChecked();
                BNDemoUtils.setBoolean(DemoNaviSettingActivity.this, BNDemoUtils.KEY_GB_ROUTE_SEARCH, isOpen);
                BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                        .enableRouteSearch(isOpen);
                break;
            case R.id.nav_moreset_cb: // 更多设置 进入前 单次生效
                isOpen = mCheckBox[23].isChecked();
                BNDemoUtils.setBoolean(DemoNaviSettingActivity.this, BNDemoUtils.KEY_GB_MORE_SETTINGS, isOpen);
                BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                        .enableMoreSettings(isOpen);
                break;
            case R.id.nav_seeallwhenbegin_cb: // 全览
                isOpen = mCheckBox[24].isChecked();
                BNDemoUtils.setBoolean(DemoNaviSettingActivity.this, BNDemoUtils.KEY_GB_SEEALL, isOpen);
                BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                        .setStartByFullView(isOpen);
                break;
            case R.id.nav_bottom_bar_type_cb:
                isOpen = mCheckBox[25].isChecked();
                BNDemoUtils.setBoolean(DemoNaviSettingActivity.this, BNDemoUtils.KEY_GB_BOTTOM_BAR_TYPE, isOpen);
                BaiduNaviManagerFactory.getProfessionalNaviSettingManager().setBottomBarType(isOpen ? 1 : 0);
                break;
            case R.id.nav_ugc_report_cb:
                isOpen = mCheckBox[26].isChecked();
                BNDemoUtils.setBoolean(DemoNaviSettingActivity.this,
                        BNDemoUtils.KEY_GB_UGC_REPORT_TYPE, isOpen);
                BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                        .setUgcButtonEnable(isOpen);
                break;
            case R.id.nav_minimap_cb:
                isOpen = mCheckBox[27].isChecked();
                BNDemoUtils.setBoolean(DemoNaviSettingActivity.this,
                        BNDemoUtils.KEY_GB_MINI_MAP_TYPE, isOpen);
                break;
            case R.id.nav_minimap_license_cb:
                isOpen = mCheckBox[28].isChecked();
                BNDemoUtils.setBoolean(DemoNaviSettingActivity.this,
                        BNDemoUtils.KEY_GB_MINI_MAP_LICENSE_SWITCH, isOpen);
                break;
            case R.id.nav_minimap_open_bg_draw_cb:
                isOpen = mCheckBox[29].isChecked();
                BNDemoUtils.setBoolean(DemoNaviSettingActivity.this,
                        BNDemoUtils.KEY_GB_MINI_MAP_OPEN_BG_DRAW, isOpen);
                break;
            default:
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1234) {
            if (Settings.canDrawOverlays(this)) {
                BNDemoUtils.setBoolean(DemoNaviSettingActivity.this,
                        "float_window",  true);
            } else {
                BNDemoUtils.setBoolean(DemoNaviSettingActivity.this,
                        "float_window", false);
            }
        }
    }
}
