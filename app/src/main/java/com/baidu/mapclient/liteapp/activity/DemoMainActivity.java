/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.mapclient.liteapp.activity;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.mapclient.liteapp.BNDemoFactory;
import com.baidu.mapclient.liteapp.BNDemoUtils;
import com.baidu.mapclient.liteapp.ForegroundService;
import com.baidu.mapclient.liteapp.R;
import com.baidu.mapclient.liteapp.devicescan.DeviceScanActivity;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNaviCommonParams;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNLicenseListener;
import com.baidu.navisdk.adapter.IBNOuterSettingManager;
import com.baidu.navisdk.adapter.IBNOuterSettingParams;
import com.baidu.navisdk.adapter.IBNRoutePlanManager;
import com.baidu.navisdk.adapter.struct.BNRoutePlanInfos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DemoMainActivity extends Activity {

    private static final String[] AUTH_BASE_ARR = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            // 室内导航必要权限
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
    };

    private static final int AUTH_BASE_REQUEST_CODE = 1;
    private static final int REQUEST_CODE_SEARCH_DEVICE = 2;

    private Button mNaviBtn = null;
    private Button mTruckBtn = null;
    private Button mMotorBtn = null;
    private Button mExternalBtn = null;
    private Button mDrivingBtn = null;
    private Button mCustomBtn = null;
    private Button mCruiserBtn = null;
    private Button mAnalogBtn = null;
    private Button mSelectNodeBtn = null;
    private Button mGotoSettingsBtn = null;
    private Button limitChange = null;
    private Button searchDeviceBtn;
    private String targetMac;

    private BroadcastReceiver mReceiver;
    private int mPageType = BNDemoUtils.NORMAL;

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_START:
                    Toast.makeText(DemoMainActivity.this, "算路开始", Toast.LENGTH_SHORT).show();
                    break;
                case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_SUCCESS:
                    Toast.makeText(DemoMainActivity.this, "算路成功", Toast.LENGTH_SHORT).show();
                    // 躲避限行消息
                    Bundle infoBundle = (Bundle) msg.obj;
                    if (infoBundle != null) {
                        String info = infoBundle
                                .getString(BNaviCommonParams.BNRouteInfoKey.TRAFFIC_LIMIT_INFO);
                        Log.e("OnSdkDemo", "info = " + info);
                    }
                    BNRoutePlanInfos routePlanInfo = BaiduNaviManagerFactory.getRoutePlanManager().getRoutePlanInfo();
                    break;
                case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_FAILED:
                    Toast.makeText(DemoMainActivity.this.getApplicationContext(),
                            "算路失败", Toast.LENGTH_SHORT).show();
                    break;
                case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_TO_NAVI:
                    Toast.makeText(DemoMainActivity.this.getApplicationContext(),
                            "算路成功准备进入导航", Toast.LENGTH_SHORT).show();
                    switch (mPageType) {
                        case BNDemoUtils.NORMAL:
                            BNDemoUtils.gotoNavi(DemoMainActivity.this);
                            break;
                        case BNDemoUtils.ANALOG:
                            BNDemoUtils.gotoAnalog(DemoMainActivity.this,targetMac);
                            break;
                        case BNDemoUtils.EXTGPS:
                            BNDemoUtils.gotoExtGps(DemoMainActivity.this);
                            break;
                        case BNDemoUtils.LIGHT:
                            startActivity(new Intent(DemoMainActivity.this, BNDemoLightNaviActivity.class));
                            break;
                        case BNDemoUtils.CUSTOM:
                            EditText top = DemoMainActivity.this.findViewById(R.id.custom_view_height_top);
                            EditText bottom = DemoMainActivity.this.findViewById(R.id.custom_view_height_bottom);
                            int customViewHeightTop = 0;
                            int customViewHeightBottom = 0;
                            if (top.getText() != null && top.getText().length() > 0) {
                                customViewHeightTop = Integer.parseInt(top.getText().toString());
                            }
                            if (bottom.getText() != null && bottom.getText().length() > 0) {
                                customViewHeightBottom = Integer.parseInt(bottom.getText().toString());
                            }
                            BNDemoUtils.gotoCustomGuide(DemoMainActivity.this
                                    , customViewHeightTop, customViewHeightBottom);
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    // nothing
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 开启前台服务防止应用进入后台gps挂掉
        startService(new Intent(this, ForegroundService.class));

        initView();
        initListener();
        initPermission();

        initBroadCastReceiver();

    }

    private void initBroadCastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.navi.ready");
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                BNDemoFactory.getInstance().initCarInfo();
                BNDemoFactory.getInstance().initRoutePlanNode();
                // 室内导航设置
                BaiduNaviManagerFactory.getBaiduNaviManager().setVdrEnable(true);
                BaiduNaviManagerFactory.getBaiduNaviManager().setGpsNeverClose(true);
                BaiduNaviManagerFactory.getBaiduNaviManager().startLocationMonitor();
            }
        };
        registerReceiver(mReceiver, filter);
    }

    private void initPermission() {
        // 申请权限
        if (Build.VERSION.SDK_INT >= 23) {
            if (!hasBasePhoneAuth()) {
                requestPermissions(AUTH_BASE_ARR, AUTH_BASE_REQUEST_CODE);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, AUTH_BASE_REQUEST_CODE);
            }
        }
    }

    private void initView() {
        mNaviBtn = findViewById(R.id.naviBtn);
        mTruckBtn = findViewById(R.id.truckBtn);
        mMotorBtn = findViewById(R.id.motorBtn);
        mExternalBtn = findViewById(R.id.externalBtn);
        mAnalogBtn = findViewById(R.id.analogBtn);
        mCustomBtn = findViewById(R.id.customBtn);
        mDrivingBtn = findViewById(R.id.drivingBtn);
        mCruiserBtn = findViewById(R.id.cruiserBtn);
        mGotoSettingsBtn = findViewById(R.id.gotoSettingsBtn);
        mSelectNodeBtn = findViewById(R.id.selectNodeBtn);
        mGotoSettingsBtn.setText("导航设置" + getVersionName(this));
        searchDeviceBtn = findViewById(R.id.navi_search_device_btn);
        RecyclerView recyclerView = findViewById(R.id.navi_setting_page_item_recycle);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        List<IBNOuterSettingParams.BNavSettingItem> list = new ArrayList<>();
        for (IBNOuterSettingParams.BNavSettingItem value : IBNOuterSettingParams.BNavSettingItem.values()) {
            list.add(value);
            mSdkFunc.put(value, true);
        }
        recyclerView.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                TextView tv = new TextView(DemoMainActivity.this);
                tv.setBackgroundResource(R.drawable.bnav_setting_btn_bg_selector);
                ViewGroup.MarginLayoutParams layoutParams = new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(20, 20, 20, 20);
                tv.setPadding(10, 10, 10, 10);
                tv.setLayoutParams(layoutParams);
                return new ViewHolder(tv);
            }

            public String getTxt(IBNOuterSettingParams.BNavSettingItem item) {
                switch (item) {
                    case FUNC_NAVI_ANGLE:
                        return "导航视角";
                    case FUNC_DAY_NIGHT:
                        return "日夜模式";
                    case FUNC_NAVI_SCALE:
                        return "智能比例尺";
                    case FUNC_VOICE_SELECTOR:
                        return "播报模式";
                    case FUNC_DIY_SPEAK:
                        return "播报内容";
                    case FUNC_ROUTE_PREFER:
                        return "路线偏好";
                    case FUNC_GROUP_SHORTCUT:
                        return "快捷功能";
                    case FUNC_ROAD_CONDITION_SWITCH:
                        return "实时路况开关";
                    case FUNC_BLUETOOTH_SOUND:
                        return "蓝牙播放";
                    case FUNC_HD_NAVI_SETTING_ITEM:
                        return "高精车道级";
                    default:
                        return "??";
                }
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                boolean isSelected = Boolean.TRUE.equals(mSdkFunc.get(list.get(position)));
                ((TextView) holder.itemView).setText(getTxt(list.get(position)));
                holder.itemView.setTag(position);
                holder.itemView.setSelected(isSelected);
                holder.itemView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.itemView.setSelected(!holder.itemView.isSelected());
                        mSdkFunc.put(list.get((Integer) holder.itemView.getTag()), holder.itemView.isSelected());
                    }
                });
            }

            @Override
            public int getItemCount() {
                return IBNOuterSettingParams.BNavSettingItem.values().length;
            }

            class ViewHolder extends RecyclerView.ViewHolder {
                public ViewHolder(@NonNull View itemView) {
                    super(itemView);
                }
            }
        });
    }

    private void initListener() {
        findViewById(R.id.addpoint).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.addpoint).setSelected(!findViewById(R.id.addpoint).isSelected());
            }
        });

        findViewById(R.id.navi_setting_page).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.navi_setting_page).setSelected(
                        !findViewById(R.id.navi_setting_page).isSelected());
                if (findViewById(R.id.navi_setting_page).isSelected()) {
                    findViewById(R.id.navi_setting_page_item_recycle).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.navi_setting_page_item_recycle).setVisibility(View.GONE);
                }
            }
        });

        if (mNaviBtn != null) {
            mNaviBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (BaiduNaviManagerFactory.getBaiduNaviManager().isInited()) {
                        String url = BNDemoFactory.getInstance().getTestEnvironmentUrl(DemoMainActivity.this);
                        if (url.length() != 0) {
                            BaiduNaviManagerFactory.getCommonSettingManager().setTestEnvironment(true, url);
                        } else {
                            BaiduNaviManagerFactory.getCommonSettingManager().setTestEnvironment(false, url);
                        }
                        mPageType = BNDemoUtils.NORMAL;
                        routePlanToNavi(null);
                    }
                }
            });
        }

        if (mTruckBtn != null) {
            mTruckBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (BaiduNaviManagerFactory.getBaiduNaviManager().isInited()) {
                        mPageType = BNDemoUtils.NORMAL;
                        Bundle bundle = new Bundle();
                        bundle.putInt(BNaviCommonParams.RoutePlanKey.VEHICLE_TYPE,
                                IBNRoutePlanManager.Vehicle.TRUCK);
                        routePlanToNavi(bundle);
                    }
                }
            });
        }

        if (mMotorBtn != null) {
            mMotorBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (BaiduNaviManagerFactory.getBaiduNaviManager().isInited()) {
                        mPageType = BNDemoUtils.NORMAL;
                        Bundle bundle = new Bundle();
                        bundle.putInt(BNaviCommonParams.RoutePlanKey.VEHICLE_TYPE,
                                IBNRoutePlanManager.Vehicle.MOTOR);
                        routePlanToNavi(bundle);
                    }
                }
            });
        }

        if (mExternalBtn != null) {
            mExternalBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (BaiduNaviManagerFactory.getBaiduNaviManager().isInited()) {
                        mPageType = BNDemoUtils.EXTGPS;
                        routePlanToNavi(null);
                    }
                }
            });
        }

        if (mAnalogBtn != null) {
            mAnalogBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (BaiduNaviManagerFactory.getBaiduNaviManager().isInited()) {
                        mPageType = BNDemoUtils.ANALOG;
                        Bundle bundle = new Bundle();
                        bundle.putInt(BNaviCommonParams.RoutePlanKey.VEHICLE_TYPE,
                                IBNRoutePlanManager.Vehicle.CAR);
                        routePlanToNavi(bundle);
                    }
                }
            });
        }

        if (mCustomBtn != null) {
            mCustomBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPageType = BNDemoUtils.CUSTOM;
                    routePlanToNavi(null);
                }
            });
        }

        if (mDrivingBtn != null) {
            mDrivingBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (BaiduNaviManagerFactory.getBaiduNaviManager().isInited()) {
                        BNDemoUtils.gotoDriving(DemoMainActivity.this);
                    }
                }
            });
        }

        if (mCruiserBtn != null) {
            mCruiserBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (BaiduNaviManagerFactory.getBaiduNaviManager().isInited()) {
                        BNDemoUtils.gotoCruiser(DemoMainActivity.this);
                    }
                }
            });
        }

        if (mGotoSettingsBtn != null) {
            mGotoSettingsBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (BaiduNaviManagerFactory.getBaiduNaviManager().isInited()) {
                        BNDemoUtils.gotoSettings(DemoMainActivity.this);
                    }
                }
            });
        }

        if (mSelectNodeBtn != null) {
            mSelectNodeBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (BaiduNaviManagerFactory.getBaiduNaviManager().isInited()) {
                        BNDemoUtils.gotoSelectNode(DemoMainActivity.this);
                    }
                }
            });
        }

        findViewById(R.id.lightNaviBtn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mPageType = BNDemoUtils.LIGHT;
                routePlanToNavi(null);
            }
        });
        limitChange = findViewById(R.id.closeLimit);
        limitChange.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                limitChange.setSelected(!limitChange.isSelected());
                // 货车算路限行
                BaiduNaviManagerFactory.getCommonSettingManager().setTruckLimitSwitch(!limitChange.isSelected());
            }
        });
        searchDeviceBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startSearchDevice();
            }
        });
    }
    public static HashMap<IBNOuterSettingParams.BNavSettingItem, Boolean> mSdkFunc = new HashMap<>();
    private void initNaviViewFunc() {
        IBNOuterSettingManager.IBNProfessionalNaviSetting naviSetting
                = BaiduNaviManagerFactory.getProfessionalNaviSettingManager();
        naviSetting.setSettingPageItemVisible(mSdkFunc);
    }

    private void routePlanToNavi(final Bundle bundle) {
        List<BNRoutePlanNode> list = new ArrayList<>();
        BNRoutePlanNode startNode = BNDemoFactory.getInstance().getCurrentNode(this, 0);
        if (startNode == null) {
            startNode = BNDemoFactory.getInstance().getStartNode(this);
        }
        list.add(startNode);
        if (findViewById(R.id.addpoint).isSelected()) {
            list.add(BNDemoFactory.getInstance().getNewNode(this));
        }
        list.add(BNDemoFactory.getInstance().getEndNode(this));

        // 关闭电子狗
        if (BaiduNaviManagerFactory.getCruiserManager().isCruiserStarted()) {
            BaiduNaviManagerFactory.getCruiserManager().stopCruise();
        }
        BaiduNaviManagerFactory.getRoutePlanManager().routePlan(
                list,
                IBNRoutePlanManager.RoutePlanPreference.ROUTE_PLAN_PREFERENCE_DEFAULT,
                bundle, handler);
    }

    private boolean hasBasePhoneAuth() {
        PackageManager pm = this.getPackageManager();
        for (String auth : AUTH_BASE_ARR) {
            if (pm.checkPermission(auth, this.getPackageName()) != PackageManager
                    .PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AUTH_BASE_REQUEST_CODE) {
            for (int ret : grantResults) {
                if (ret != 0) {
                    Toast.makeText(DemoMainActivity.this.getApplicationContext(),
                            "缺少导航基本的权限!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        limitChange = findViewById(R.id.closeLimit);
        limitChange.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                limitChange.setSelected(!limitChange.isSelected());
                BaiduNaviManagerFactory.getCommonSettingManager().setTruckLimitSwitch(!limitChange.isSelected());
                BaiduNaviManagerFactory.getCommonSettingManager().setTruckWeightLimitSwitch(!limitChange.isSelected());
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        initNaviViewFunc();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        stopService(new Intent(this, ForegroundService.class));
    }

    public static String getVersionName(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName + "(" + pInfo.versionCode + ")";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void startSearchDevice(){
        Intent intent = new Intent(DemoMainActivity.this, DeviceScanActivity.class);

        startActivityForResult(intent,REQUEST_CODE_SEARCH_DEVICE);
    }

   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {

       super.onActivityResult(requestCode, resultCode, data);
       if(requestCode == REQUEST_CODE_SEARCH_DEVICE){
           if(resultCode == Activity.RESULT_OK){
               this.targetMac = data.getStringExtra(DeviceScanActivity.EXTRA_BLE_DEVICE);
               this.searchDeviceBtn.setText("搜索蓝牙 " + targetMac);
           }
       }
    }

}
