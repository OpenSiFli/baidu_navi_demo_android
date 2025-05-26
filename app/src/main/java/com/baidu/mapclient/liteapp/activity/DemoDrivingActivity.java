package com.baidu.mapclient.liteapp.activity;

import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.baidu.mapclient.liteapp.R;
import com.baidu.mapclient.liteapp.routeresult.DemoRouteResultFragment;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;

public class DemoDrivingActivity extends FragmentActivity {

    public boolean isGuideFragment = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driving);
        ViewGroup mapContent = findViewById(R.id.map_container);
        BaiduNaviManagerFactory.getMapManager().attach(mapContent);

        initFragment();
    }

    private void initFragment() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tx = fm.beginTransaction();
        DemoRouteResultFragment fragment = new DemoRouteResultFragment();
        tx.add(R.id.fragment_content, fragment, "RouteResult");
        tx.commit();
    }

    @Override
    public void onBackPressed() {
        if (isGuideFragment) {
            BaiduNaviManagerFactory.getRouteGuideManager().onBackPressed(false);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        BaiduNaviManagerFactory.getMapManager().onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        BaiduNaviManagerFactory.getMapManager().onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
