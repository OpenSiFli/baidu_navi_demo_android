/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.mapclient.liteapp;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNLicenseListener;
import com.baidu.navisdk.adapter.IBaiduNaviManager;
import com.baidu.navisdk.adapter.struct.BNTTsInitConfig;
import com.baidu.navisdk.adapter.struct.BNaviInitConfig;

public class ONApplication extends Application {

    public static final String TAG = "BNSDKSimpleDemo";

    @Override
    public void onCreate() {
        super.onCreate();
        SDKInitializer.setAgreePrivacy(this, true);
        SDKInitializer.initialize(this);
        SDKInitializer.setCoordType(CoordType.GCJ02);
        initNavi();
    }

    private void initNavi() {
        if (BaiduNaviManagerFactory.getBaiduNaviManager().isInited()) {
            return;
        }

        BNaviInitConfig config = new BNaviInitConfig.Builder()
                .naviInitListener(new IBaiduNaviManager.INaviInitListener() {
                    @Override
                    public void onAuthResult(int status, String msg) {
                        String result;
                        if (0 == status) {
                            result = "key校验成功!";
                        } else {
                            result = "key校验失败, " + msg;
                        }
                        Log.e(TAG, result);
                    }

                    @Override
                    public void initStart() {
                        Log.e(TAG, "initStart");
                        BaiduNaviManagerFactory.getBaiduNaviManager().enableOutLog(true);
                    }

                    @Override
                    public void initSuccess() {
                        Log.e(TAG, "initSuccess cuid = " + BaiduNaviManagerFactory.getBaiduNaviManager().getCUID());
                        // 初始化tts
                        initTTS();
                        initMiniMapLicense();
                        sendBroadcast(new Intent("com.navi.ready"));
                    }

                    @Override
                    public void initFailed(int errCode) {
                        Log.e(TAG, "initFailed = " + errCode);
                    }
                })
                .build();
        BaiduNaviManagerFactory.getBaiduNaviManager().init(this, config);
    }

    private void initTTS() {
        // 使用内置TTS
        BNTTsInitConfig config = new BNTTsInitConfig.Builder()
                .context(getApplicationContext())
                .appId(BNDemoUtils.getTTSAppID())
                .appKey(BNDemoUtils.getTTSAppKey())
                .secretKey(BNDemoUtils.getTTSsecretKey())
                .authSn(BNDemoUtils.getAuth())
                .build();
        BaiduNaviManagerFactory.getTTSManager().initTTS(config);

        // 使用外置TTS播报，与上面的内置TTS播报接口二选一，不可同时存在
//        BaiduNaviManagerFactory.getTTSManager().initTTS(new IBNTTSManager.IBNOuterTTSPlayerCallback() {
//            @Override
//            public int playTTSText(String speech, int bPreempt, String speechId) {
//                Log.e(TAG, speech);
//                return 0;
//            }
//
//            @Override
//            public int getTTSState() {
//                return 0;
//            }
//        });
    }

    /**
     * 多实例鉴权
     */
    private void initMiniMapLicense() {
        Boolean licenseSwitch = BNDemoUtils.getBoolean(this, BNDemoUtils.KEY_GB_MINI_MAP_LICENSE_SWITCH, true);
        BaiduNaviManagerFactory.getBaiduNaviManager().loadAuth("8pad5G042GGExhPGziRPLHVer14v2NpS" + (licenseSwitch ? "" : "no_permission"), "deviceId", new IBNLicenseListener() {
            @Override
            public void onSuccess(boolean result) {
                Log.e("loadAuth", "b=" + result);
            }

            @Override
            public void onError(int errorCode, String msg) {
                Log.e("loadAuth", "onError" + msg);
            }
        });
    }
}
