package com.baidu.mapclient.liteapp.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.FragmentActivity;

import com.baidu.mapclient.liteapp.LocationController;
import com.baidu.mapclient.liteapp.listener.BNDemoNaviListener;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;

public class DemoExtGpsActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View mapView = BaiduNaviManagerFactory.getRouteGuideManager()
                .onCreate(this, null);
        setContentView(mapView);
        BaiduNaviManagerFactory.getRouteGuideManager().setNaviListener(new BNDemoNaviListener() {
            @Override
            public void onNaviGuideEnd() {
                DemoExtGpsActivity.this.finish();
            }
        });
        initExtGpsData();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        BaiduNaviManagerFactory.getRouteGuideManager().onConfigurationChanged(newConfig);
    }

    private void initExtGpsData() {
        BaiduNaviManagerFactory.getBaiduNaviManager().externalLocation(true);
        LocationController.getInstance().startLocation(getApplication());
    }

    @Override
    public void onBackPressed() {
        BaiduNaviManagerFactory.getRouteGuideManager()
                .onBackPressed(false, true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        BaiduNaviManagerFactory.getRouteGuideManager().onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BaiduNaviManagerFactory.getRouteGuideManager().onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        BaiduNaviManagerFactory.getRouteGuideManager().onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        BaiduNaviManagerFactory.getRouteGuideManager().onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocationController.getInstance().stopLocation();
        BaiduNaviManagerFactory.getBaiduNaviManager().externalLocation(false);
    }
}
