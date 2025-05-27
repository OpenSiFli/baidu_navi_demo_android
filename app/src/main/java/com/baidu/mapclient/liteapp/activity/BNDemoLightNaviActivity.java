package com.baidu.mapclient.liteapp.activity;

import java.io.InputStream;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapclient.liteapp.BNDemoUtils;
import com.baidu.mapclient.liteapp.R;
import com.baidu.mapclient.liteapp.custom.MiniMapViewController;
import com.baidu.mapclient.liteapp.fragment.BNLightNaviFragment;
import com.baidu.mapclient.liteapp.fragment.BNProNaviFragment;
import com.baidu.mapclient.liteapp.fragment.BNRouteResultFragment;
import com.baidu.mapclient.liteapp.fragment.FragmentNavigator;
import com.baidu.mapclient.liteapp.service.MinimapService;
import com.baidu.mapsdkplatform.comapi.util.CoordTrans;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNMiniMapViewManager;
import com.baidu.navisdk.adapter.IBNOuterSettingParams;
import com.baidu.nplatform.comjni.tools.JNITools;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

public class BNDemoLightNaviActivity extends FragmentActivity {

    private final String TAG = "BNDemoLightNaviActivity";
    private FrameLayout mapContainer = null;
    private View mapView = null;
    private FragmentNavigator navigator = null;
    /**
     * 多实例底图
     */
    public MiniMapViewController miniMapViewController = new MiniMapViewController();
    private Button showHideMiniBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"onCreate");
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.onsdk_activity_light_navi);
        initView();
        navigator = new FragmentNavigator(this);
        navigator.jumpTo("BNLightNaviFragment");
        gpsListener();
        fragmentChange();

        Boolean isOpen = BNDemoUtils.getBoolean(this, BNDemoUtils.KEY_GB_ICONSET, false);
        if (isOpen) {
            Bitmap bitmap = BNDemoLightNaviActivity.getbitmap(this, "car.png");
            BaiduNaviManagerFactory.getCommonSettingManager()
                    .setDIYImageToMap(bitmap,
                            IBNOuterSettingParams.DIYImageType.CarLogo);

        } else {
            BaiduNaviManagerFactory.getCommonSettingManager().
                    clearDIYImage(IBNOuterSettingParams.DIYImageType.CarLogo);
        }
        initListener();
    }

    private void initView() {
        mapContainer = findViewById(R.id.map_container);
        showHideMiniBtn = findViewById(R.id.light_nav_hide_minimap_btn);
        // 将底图装载进mapContainer父容器
        BaiduNaviManagerFactory.getMapManager().attach(mapContainer);
    }

    private void initListener() {
        findViewById(R.id.minimap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BNDemoUtils.setBoolean(BNDemoLightNaviActivity.this,
                        BNDemoUtils.KEY_GB_MINI_MAP_TYPE, true);
                initMiniMapView();
            }
        });
        showHideMiniBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                miniMapViewController.showOrHide();
            }
        });
    }

    private View miniMap = null;
    private void initMiniMapView() {
        if (miniMap == null) {
            miniMap = miniMapViewController.onCreate(this);
            if (miniMap != null) {
                mapContainer.addView(miniMap);
            }
            miniMapViewController.onResume();
        } else {
            mapContainer.removeView(miniMap);
            miniMapViewController.stopPreview();
            miniMapViewController.onPause();
            miniMapViewController.onDestroy();
            miniMap = null;
        }
    }

    public static Bitmap getbitmap(Context context, String fileName) {
        InputStream assetFile = null;
        try {
            AssetManager assets = context.getAssets();
            assetFile = assets.open(fileName);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "获取bitmap失败", Toast.LENGTH_SHORT).show();
        }
        return BitmapFactory.decodeStream(assetFile);
    }

    public void gpsListener() {
        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, " 请添加定位权限", Toast.LENGTH_SHORT).show();
            return;
        }
        mlocManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0,
            0,
            new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (SDKInitializer.getCoordType().equals(CoordType.BD09LL)) {
                        LatLng latLng =
                            CoordTrans.wgsToBaidu(new LatLng(location.getLatitude(), location.getLongitude()));
                        BNDemoUtils.setString(
                            BNDemoLightNaviActivity.this, "current_node", latLng.longitude + "," + latLng.latitude);

                    } else {
                        Bundle latLng = JNITools.Wgs84ToGcj02(location.getLongitude(), location.getLatitude());
                        BNDemoUtils.setString(
                            BNDemoLightNaviActivity.this,
                            "current_node",
                            latLng.getDouble("LLx") + "," + latLng.getDouble("LLy"));
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}

                @Override
                public void onProviderEnabled(String provider) {}

                @Override
                public void onProviderDisabled(String provider) {}
            });
    }

    public BNRoutePlanNode getCurrentNode(float d) {
        String end = BNDemoUtils.getString(this, "current_node");
        if (!TextUtils.isEmpty(end)) {
            String[] node = end.split(",");
            return new BNRoutePlanNode.Builder()
                .longitude(Double.parseDouble(node[0]) + d)
                .latitude(Double.parseDouble(node[1]) + d)
                .build();
        }
        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        BaiduNaviManagerFactory.getMapManager().onResume();
        miniMapViewController.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        BaiduNaviManagerFactory.getMapManager().onPause();
        miniMapViewController.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        navigator = null;
        BaiduNaviManagerFactory.getMapManager().detach(mapContainer);
        miniMapViewController.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (navigator != null && navigator.getFragmentStack() != null && navigator.getFragmentStack().size() >= 1) {
            if (navigator.getFragmentStack().peek() instanceof BNProNaviFragment) {
                BaiduNaviManagerFactory.getRouteGuideManager().onBackPressed(false);
            } else if (navigator.getFragmentStack().peek() instanceof BNLightNaviFragment) {
                // goback 会返回上一个fragment
                finish();
            } else {
                goBack();
            }
        } else {
            finish();
        }
    }

    public void jumpTo(String fragmentName) {
        if (navigator != null) {
            navigator.jumpTo(fragmentName);
        }
        fragmentChange();
    }

    public void fragmentChange() {
        if (navigator.getFragmentStack().peek() instanceof BNLightNaviFragment) {
            // 轻导航
            setNaviMode(IBNMiniMapViewManager.NaviMode.SLIGHT);
            miniMapViewController.fullView(false);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else if (navigator.getFragmentStack().peek() instanceof BNRouteResultFragment) {
            // 路线页 车标不会移动
            setNaviMode(IBNMiniMapViewManager.NaviMode.ROUTE);
            miniMapViewController.fullView(true);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else if (navigator.getFragmentStack().peek() instanceof BNProNaviFragment) {
            Intent serviceIntent = new Intent(this, MinimapService.class);
            this.stopService(serviceIntent);
            // 专业导航
            setNaviMode(IBNMiniMapViewManager.NaviMode.NORMAL);
            miniMapViewController.fullView(false);
            miniMapViewController.initNaviListener();
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        } else {
            // 巡航 没有路线展示，车标会移动
            setNaviMode(IBNMiniMapViewManager.NaviMode.CRUISE);
            miniMapViewController.fullView(false);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        }
    }

    public void activityStop() {
        if (navigator.getFragmentStack().peek() instanceof BNProNaviFragment) {
            onPause();
            onStop();
        }
    }

    public void goBack() {
        if (navigator != null) {
            navigator.goBack();
        }
        fragmentChange();
    }

    public void setNaviMode(int i) {
        miniMapViewController.setNaviMode(i);
    }
}
