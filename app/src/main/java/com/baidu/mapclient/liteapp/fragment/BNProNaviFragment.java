/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.mapclient.liteapp.fragment;

import com.baidu.mapclient.liteapp.R;
import com.baidu.mapclient.liteapp.activity.BNDemoLightNaviActivity;
import com.baidu.mapclient.liteapp.listener.BNDemoNaviListener;
import com.baidu.mapclient.liteapp.listener.BNDemoNaviViewListener;
import com.baidu.navisdk.adapter.BNaviCommonParams;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNaviListener;
import com.baidu.navisdk.adapter.struct.BNGuideConfig;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class BNProNaviFragment extends BNBaseFragment {

    private static final String TAG = "BNProNaviFragment";

    private View contentView = null;

    private IBNaviListener.DayNightMode mMode = IBNaviListener.DayNightMode.DAY;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.onsdk_fragment_pro_light_navi, container, false);
        initView();
        initListener();
        return contentView;
    }

    private void initListener() {
        BaiduNaviManagerFactory.getRouteGuideManager().setNaviListener(new BNDemoNaviListener() {
            @Override
            public void onStartYawing(String flag) {

            }

            @Override
            public void onNaviGuideEnd() {
                if (getActivity() != null) {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    ((BNDemoLightNaviActivity) getActivity()).goBack();
                }
            }
        });

        BaiduNaviManagerFactory.getRouteGuideManager()
                .setNaviViewListener(new BNDemoNaviViewListener());
    }

    private void initView() {
        FrameLayout contentContainer = contentView.findViewById(R.id.content_container);
        Bundle bundle = new Bundle();
        bundle.putBoolean(BNaviCommonParams.ProGuideKey.ADD_MAP, false);
        bundle.putBoolean(BNaviCommonParams.ProGuideKey.IS_SUPPORT_FULL_SCREEN, true);
        BNGuideConfig config = new BNGuideConfig.Builder()
                .params(bundle)
                .build();
        View view = BaiduNaviManagerFactory.getRouteGuideManager().onCreate(getActivity(), config);
        if (view != null) {
            if (view.getParent() != null) {
                ViewGroup parent = (ViewGroup) view.getParent();
                parent.removeView(view);
            }
            contentContainer.addView(view);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        BaiduNaviManagerFactory.getRouteGuideManager().onConfigurationChanged(newConfig);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        BaiduNaviManagerFactory.getRouteGuideManager().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStart() {
        super.onStart();
        BaiduNaviManagerFactory.getRouteGuideManager().onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        BaiduNaviManagerFactory.getRouteGuideManager().onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        BaiduNaviManagerFactory.getRouteGuideManager().onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        BaiduNaviManagerFactory.getRouteGuideManager().onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BaiduNaviManagerFactory.getRouteGuideManager().onDestroy(true);
    }

    @Override
    public void goBack() {
        ((BNDemoLightNaviActivity) getActivity()).jumpTo("BNLightNaviFragment");
    }
}
