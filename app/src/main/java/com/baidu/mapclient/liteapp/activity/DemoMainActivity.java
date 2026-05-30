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
import android.graphics.PointF;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.mapclient.liteapp.BNDemoFactory;
import com.baidu.mapclient.liteapp.BNDemoUtils;
import com.baidu.mapclient.liteapp.ForegroundService;
import com.baidu.mapclient.liteapp.R;
import com.baidu.mapclient.liteapp.config.SFNaviOption;
import com.baidu.mapclient.liteapp.devicescan.DeviceScanActivity;
import com.baidu.mapclient.liteapp.util.BTBondManager;
import com.baidu.mapclient.liteapp.util.ProgressHUDHelper;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNaviCommonParams;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNLicenseListener;
import com.baidu.navisdk.adapter.IBNOuterSettingManager;
import com.baidu.navisdk.adapter.IBNOuterSettingParams;
import com.baidu.navisdk.adapter.IBNRoutePlanManager;
import com.baidu.navisdk.adapter.struct.BNRoutePlanInfos;
import com.sifli.siflicore.error.SFError;
import com.sifli.siflicore.log.SFLog;
import com.sifli.siflicore.p2p.SFWifiP2PCallback;
import com.sifli.siflicore.p2p.SFWifiP2PManager;
import com.sifli.siflicore.util.StringUtil;
import com.sifli.sifliotasdk.manager.SFTransmissionMode;
import com.sifli.sifliotasdk.modules.sol2.preview.SFPreviewQRHelper;
import com.sifli.sifliotasdk.modules.sol2.preview.SFPreviewQRResult;
import com.sifli.sifliotasdk.modules.sol2.preview.SFPreviewQRWifiResult;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DemoMainActivity extends AppCompatActivity implements SFWifiP2PCallback {
    private final static String TAG = "DemoMainActivity";
    private static final String[] AUTH_BASE_ARR = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            // 室内导航必要权限
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.CAMERA,
            Manifest.permission.CHANGE_WIFI_STATE,
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
//    private CheckBox useSocketCb;
    private RadioButton comunicateBleRb;
    private RadioButton comunicateSppRb;
    private RadioButton comunicateSocketServerRb;
    private RadioButton comunicateSocketClientRb;
    private EditText ipEt;
    private EditText portEt;
    private EditText socketMtuEt;

    private EditText jpegQualityEt;
    private EditText maxFpsEt;
//    private RadioButton size800_480Rb;
//    private RadioButton size480_272Rb;
    private EditText widthEt;
    private EditText heightEt;
    private RadioButton imageModeRb;
    private RadioButton infoModeRb;
    private TextView versionTv;
    private Button logBtn;
    private ImageButton qrScanBtn;
    private String targetMac;

    private BroadcastReceiver mReceiver;
    private int mPageType = BNDemoUtils.NORMAL;
    private BTBondManager bondManager;

    private ActivityResultLauncher<Intent> qrScanlauncher;
    private SFPreviewQRHelper qrHelper = new SFPreviewQRHelper();
    private SFWifiP2PManager p2PManager;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean isAutoStart = false;
    private int qrTransMode = -1;
    private String qrWifiSSID = null;

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
                            BNDemoUtils.gotoNavi(DemoMainActivity.this,targetMac);
                            break;
                        case BNDemoUtils.ANALOG:
                            BNDemoUtils.gotoAnalog(DemoMainActivity.this,targetMac,isAutoStart);
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
        this.bondManager = new BTBondManager(this.getApplicationContext());
        this.p2PManager = new SFWifiP2PManager(this.getApplicationContext());
        this.p2PManager.registerReceiver();
        this.p2PManager.setCallback(this);
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ 必须指定导出标志
            registerReceiver(mReceiver, filter, RECEIVER_NOT_EXPORTED); // 或 RECEIVER_EXPORTED
        } else {
            // 旧版本兼容方式
            registerReceiver(mReceiver, filter);
        }
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
        logBtn = findViewById(R.id.navi_log_btn);
//        useSocketCb = findViewById(R.id.main_use_socket_cb);
        comunicateBleRb = findViewById(R.id.main_comunicate_ble_rb);
        comunicateSppRb = findViewById(R.id.main_comunicate_spp_rb);
        comunicateSocketServerRb = findViewById(R.id.main_comunicate_socket_server_rb);
        comunicateSocketClientRb = findViewById(R.id.main_comunicate_socket_client_rb);
        ipEt = findViewById(R.id.main_server_ip_et);
        portEt = findViewById(R.id.main_server_port_et);
        socketMtuEt = findViewById(R.id.main_socket_mtu_et);

        jpegQualityEt = findViewById(R.id.main_jpeg_quality_et);
        maxFpsEt = findViewById(R.id.main_max_fps_et);
//        size800_480Rb = findViewById(R.id.main_size_800_rb);
//        size480_272Rb = findViewById(R.id.main_size_480_rb);
        widthEt = findViewById(R.id.main_size_width_et);
        heightEt = findViewById(R.id.main_size_height_et);
        imageModeRb = findViewById(R.id.main_mode_image_rb);
        infoModeRb = findViewById(R.id.main_mode_info_rb);
        versionTv = findViewById(R.id.main_version_tv);
        versionTv.setText(getVersionName(this));
        qrScanBtn = findViewById(R.id.main_qr_scan_ib);

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
                    onNaviBtnTouch(false);
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
                    onAnalogBtnTouch(false);
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

        logBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onLogBtnTouch();
            }
        });
        qrScanBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onQrScanBtnTouch();
            }
        });
//        useSocketCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                SFLog.i("DEMO MAIN","use socket =%b",isChecked);
//                SFNaviOption.getInstance().setUseSocket(isChecked);
//            }
//        });
        this.createQRScanLauncher();
    }

    private void onNaviBtnTouch(boolean autoStart){
        this.isAutoStart = autoStart;
        applySetting();
        boolean isBond = validateSppIsBond();
        if(!isBond){
            toast("请先在设置中对设备配对");
            return;
        }
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

    private void onAnalogBtnTouch(boolean autoStart){
        this.isAutoStart = autoStart;
        applySetting();
        boolean isBond = validateSppIsBond();
        if(!isBond){
            toast("请先在设置中对设备配对");
            return;
        }
        if (BaiduNaviManagerFactory.getBaiduNaviManager().isInited()) {
            mPageType = BNDemoUtils.ANALOG;
            Bundle bundle = new Bundle();
            bundle.putInt(BNaviCommonParams.RoutePlanKey.VEHICLE_TYPE,
                    IBNRoutePlanManager.Vehicle.CAR);
            routePlanToNavi(bundle);
        }
    }

    private void createQRScanLauncher() {
        this.qrScanlauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                Intent data = result.getData();
                int resultCode = result.getResultCode();
                if (resultCode == RESULT_OK) {
                    String qrStr = data.getStringExtra(QRScanActivity.QR_RESULT_EXTRA_KEY);
                    onQrResult(qrStr);
                }
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

    private  void onLogBtnTouch(){
        startActivity(new Intent(this, LogsActivity.class));
    }

    private void onQrScanBtnTouch(){
        this.qrScanlauncher.launch(new Intent(DemoMainActivity.this, QRScanActivity.class));
    }

    private void applySetting(){
        SFLog.i("MAIN","applySetting");
        SFNaviOption option = SFNaviOption.getInstance();
        String maxFpsTxt = this.maxFpsEt.getText().toString();
        String jpegQualityTxt = this.jpegQualityEt.getText().toString();
        String ipTxt = this.ipEt.getText().toString();
        String portTxt = this.portEt.getText().toString();
        String widthTxt = this.widthEt.getText().toString();
        String heightText = this.heightEt.getText().toString();
        String socketMtuTxt = this.socketMtuEt.getText().toString();

        int transMode = SFTransmissionMode.TRANSMISSION_MODE_SPP;
        if(this.comunicateBleRb.isChecked())transMode = SFTransmissionMode.TRANSMISSION_MODE_BLE;
        if(this.comunicateSppRb.isChecked())transMode = SFTransmissionMode.TRANSMISSION_MODE_SPP;
        if(this.comunicateSocketServerRb.isChecked())transMode = SFTransmissionMode.TRANSMISSION_MODE_SOCKET_SERVER;
        if(this.comunicateSocketClientRb.isChecked())transMode = SFTransmissionMode.TRANSMISSION_MODE_SOCKET_CLIENT;
//        boolean useSocket = this.useSocketCb.isChecked();
        int mode = this.imageModeRb.isChecked() ? SFNaviOption.NAV_MODE_IMAGE : SFNaviOption.NAV_MODE_INFO;

        int port = 2025;
        int maxFps = 20;
        float jpegQuality = 0.2f;
        int width = 800;
        int height = 480;
        int socketMtu = 16;

        try{
            width = Integer.parseInt(widthTxt);
            height = Integer.parseInt(heightText);
            maxFps = Integer.parseInt(maxFpsTxt);
            jpegQuality = Float.parseFloat(jpegQualityTxt);
            port = Integer.parseInt(portTxt);
            socketMtu = Integer.parseInt(socketMtuTxt);
        }catch (Exception e){
            SFLog.e("MAIN","applySetting error %s",e);
        }

        option.setMaxFPS(maxFps);
        option.setJpegQuality(jpegQuality);
        option.setWidth(width);
        option.setHeight(height);
//        option.setUseSocket(useSocket);
        option.setTransMode(transMode);
        option.setNavMode(mode);
        option.setServerIP(ipTxt);
        option.setServerPort(port);
        option.setSocketMtu(socketMtu);
    }

    private boolean validateSppIsBond(){
        if(SFNaviOption.getInstance().getTransMode() != SFTransmissionMode.TRANSMISSION_MODE_SPP){
            return  true;
        }
        if(StringUtil.isNullOrEmpty(this.targetMac)){
            return false;
        }
        return  this.bondManager.isBond(this.targetMac);
    }

    private void toast(String msg){
        Toast.makeText(DemoMainActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    private void onQrResult(String qrText){
        SFLog.i(TAG,"onQrResult:%s",qrText);
        this.qrWifiSSID = null;
        this.clearMac();
        int transMode = SFTransmissionMode.TRANSMISSION_MODE_SPP;
        if(this.comunicateBleRb.isChecked())transMode = SFTransmissionMode.TRANSMISSION_MODE_BLE;
        if(this.comunicateSppRb.isChecked())transMode = SFTransmissionMode.TRANSMISSION_MODE_SPP;
        if(this.comunicateSocketServerRb.isChecked())transMode = SFTransmissionMode.TRANSMISSION_MODE_SOCKET_SERVER;
        if(this.comunicateSocketClientRb.isChecked())transMode = SFTransmissionMode.TRANSMISSION_MODE_SOCKET_CLIENT;

        int mode = this.imageModeRb.isChecked() ? SFNaviOption.NAV_MODE_IMAGE : SFNaviOption.NAV_MODE_INFO;

        String maxFpsTxt = this.maxFpsEt.getText().toString();
        String jpegQualityTxt = this.jpegQualityEt.getText().toString();
        String ipTxt = this.ipEt.getText().toString();
        String portTxt = this.portEt.getText().toString();
        String widthTxt = this.widthEt.getText().toString();
        String heightText = this.heightEt.getText().toString();
        String mac = null;
        String p2pSSID = null;
        boolean isWifi = false;

        int port = 2025;
        int maxFps = 20;
        float jpegQuality = 0.2f;
        int width = 800;
        int height = 480;

        try{
            width = Integer.parseInt(widthTxt);
            height = Integer.parseInt(heightText);
            maxFps = Integer.parseInt(maxFpsTxt);
            jpegQuality = Float.parseFloat(jpegQualityTxt);
            port = Integer.parseInt(portTxt);
        }catch (Exception e){
            SFLog.e("MAIN","applySetting error %s",e);
        }

        PointF sizepf = new PointF(width,height);

        SFPreviewQRResult qrResult = this.qrHelper.analyzeQRText(qrText);
        if(qrResult == null){
            SFLog.e(TAG,"onQrResult qrResult is null");
            return;
        }
        SFLog.i(TAG,"onQrResult:%s",qrResult);

        SFPreviewQRWifiResult wifiResult = qrResult.getWifiInfo();
        if(wifiResult != null){
            p2pSSID = wifiResult.getSSID();
        }

        Map<String,String> dict = qrResult.getAllCustomInfo();
        if(dict.containsKey(SFPreviewQRResult.KEY_TRANS_TYPE)){
           int transTypeMask = this.qrHelper.makeTransTypeWithText(dict.get(SFPreviewQRResult.KEY_TRANS_TYPE));
           if(this.qrHelper.containTransType(transTypeMask,SFPreviewQRResult.TRANS_TYPE_SPP)){
               transMode = SFTransmissionMode.TRANSMISSION_MODE_SPP;
           }else if(this.qrHelper.containTransType(transTypeMask,SFPreviewQRResult.TRANS_TYPE_BLE)){
               transMode = SFTransmissionMode.TRANSMISSION_MODE_BLE;
           }else if(this.qrHelper.containTransType(transTypeMask,SFPreviewQRResult.TRANS_TYPE_WIFI)){
               transMode = SFTransmissionMode.TRANSMISSION_MODE_SOCKET_CLIENT;
               this.socketMtuEt.setText("16");
               isWifi = true;
           }else if(this.qrHelper.containTransType(transTypeMask,SFPreviewQRResult.TRANS_TYPE_PAN)){
               transMode = SFTransmissionMode.TRANSMISSION_MODE_SOCKET_CLIENT;
               this.socketMtuEt.setText("1");
           }
        }
        this.qrTransMode = transMode;
        if(dict.containsKey(SFPreviewQRResult.KEY_QUALITY)){
            jpegQuality = this.qrHelper.makeQualityWithText(dict.get(SFPreviewQRResult.KEY_QUALITY),jpegQuality);
        }
        if(dict.containsKey(SFPreviewQRResult.KEY_IP)){
            String text = this.qrHelper.makeIPAddressWithText(dict.get(SFPreviewQRResult.KEY_IP));
            if(text != null)ipTxt = text;
        }
        if(dict.containsKey(SFPreviewQRResult.KEY_PORT)){
            port = this.qrHelper.makePortWithText(dict.get(SFPreviewQRResult.KEY_PORT),port);
        }
        if(dict.containsKey(SFPreviewQRResult.KEY_SIZE)){
            sizepf = this.qrHelper.makeSizeWithText(dict.get(SFPreviewQRResult.KEY_SIZE),sizepf);
            width = (int)sizepf.x;
            height = (int)sizepf.y;
        }
        if(dict.containsKey(SFPreviewQRResult.KEY_MAC)){
            mac = this.qrHelper.makeMacAddressWithText(dict.get(SFPreviewQRResult.KEY_MAC));
        }
        if(dict.containsKey(SFPreviewQRResult.KEY_TYPE)){
            int contentMode = this.qrHelper.makeTypeWithText(dict.get(SFPreviewQRResult.KEY_TYPE),SFPreviewQRResult.CONTENT_TYPE_MAP);
            if(contentMode == SFPreviewQRResult.CONTENT_TYPE_MAP){
                mode = SFNaviOption.NAV_MODE_IMAGE;
            }else if(contentMode == SFPreviewQRResult.CONTENT_TYPE_INFO){
                mode = SFNaviOption.NAV_MODE_INFO;
            }
        }

        this.selectComunicateRb(transMode);
        this.selectContentMode(mode);
        this.jpegQualityEt.setText(jpegQuality + "");
        this.ipEt.setText(ipTxt);
        this.portEt.setText(port + "");
        this.widthEt.setText(width + "");
        this.heightEt.setText(height + "");

        if(mac != null){
            boolean isBond = this.bondManager.isBond(mac);
            this.applyMac(mac,transMode == SFTransmissionMode.TRANSMISSION_MODE_SPP);
            //已经配对的情况下直接发起投屏
            if(transMode ==SFTransmissionMode.TRANSMISSION_MODE_SPP){
                if(isBond)onAnalogBtnTouch(true);
            }else if(transMode == SFTransmissionMode.TRANSMISSION_MODE_BLE){
                onAnalogBtnTouch(true);
            }
        }
        this.qrWifiSSID = p2pSSID;
        if(isWifi && !StringUtil.isNullOrEmpty(p2pSSID)){
            ProgressHUDHelper.show(this,"disconnect P2P..");
            SFLog.i(TAG,"disconnect it first...");
            this.p2PManager.disconnect();
//            final  String finalSSID = p2pSSID;
//            this.mainHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    connectP2P(finalSSID);
//                }
//            },1000);
        }

    }

    private  void connectP2P(String ssid){
        ProgressHUDHelper.updateMessage("connect P2P...");
        SFLog.i(TAG,"connectP2P... %s",ssid);
        this.p2PManager.connectToSsid(ssid);
    }

    private void selectComunicateRb(int transMode) {
        if (transMode == SFTransmissionMode.TRANSMISSION_MODE_BLE) {
            this.comunicateBleRb.setChecked(true);
        } else if (transMode == SFTransmissionMode.TRANSMISSION_MODE_SPP) {
            this.comunicateSppRb.setChecked(true);
        } else if (transMode == SFTransmissionMode.TRANSMISSION_MODE_SOCKET_SERVER) {
            this.comunicateSocketServerRb.setChecked(true);
        } else if (transMode == SFTransmissionMode.TRANSMISSION_MODE_SOCKET_CLIENT) {
            this.comunicateSocketClientRb.setChecked(true);
        }
    }

    private void selectContentMode(int mode){
        if(mode == SFNaviOption.NAV_MODE_IMAGE){
            this.imageModeRb.setChecked(true);
        }else if(mode == SFNaviOption.NAV_MODE_INFO){
            this.infoModeRb.setChecked(true);
        }
    }

    private void applyMac(String mac,boolean needBond){
        this.targetMac = mac;
        this.searchDeviceBtn.setText("搜索蓝牙 " + targetMac);
        if(needBond){
            boolean isBond = this.bondManager.isBond(this.targetMac);
            if(!isBond)this.bondManager.createBondByMac(this.targetMac);
        }

    }

    private void clearMac(){
        this.targetMac = null;
        this.searchDeviceBtn.setText("搜索蓝牙");
    }

   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {

       super.onActivityResult(requestCode, resultCode, data);
       if(requestCode == REQUEST_CODE_SEARCH_DEVICE){
           if(resultCode == Activity.RESULT_OK){
               String mac = data.getStringExtra(DeviceScanActivity.EXTRA_BLE_DEVICE);
               applyMac(mac,true);
           }
       }
    }

    // region SFWifiP2PCallback
    @Override
    public void onConnected(InetAddress inetAddress, boolean isGroupOwner, int port) {
        SFLog.i(TAG,"P2P onConnected %s,port %d",inetAddress.getHostAddress(),port);
        ProgressHUDHelper.dismiss();
        if(!isGroupOwner){
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    ipEt.setText(inetAddress.getHostAddress());
                    portEt.setText(port + "");
                    if(qrTransMode == SFTransmissionMode.TRANSMISSION_MODE_SOCKET_CLIENT)onAnalogBtnTouch(true);
                }
            });

        }
        toast("WIFI P2P is Connected");

    }

    @Override
    public void onConnectionFailed(SFError sfError) {
        SFLog.e(TAG,"onConnectionFailed %s",sfError);
        ProgressHUDHelper.dismiss();
        this.toast(sfError.toString());
    }

    @Override
    public void onDisconnectComplete() {
        SFLog.e(TAG,"onDisconnectComplete");
        if(qrWifiSSID != null)connectP2P(qrWifiSSID);

    }
    //endregion
}
