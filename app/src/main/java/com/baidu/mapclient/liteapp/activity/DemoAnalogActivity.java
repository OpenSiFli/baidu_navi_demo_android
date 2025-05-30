package com.baidu.mapclient.liteapp.activity;


import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapclient.liteapp.BNDemoUtils;
import com.baidu.mapclient.liteapp.R;
import com.baidu.mapclient.liteapp.custom.MiniMapViewController;
import com.baidu.mapclient.liteapp.listener.BNDemoNaviListener;
import com.baidu.navisdk.adapter.BNaviCommonParams;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNRouteGuideManager;
import com.baidu.navisdk.adapter.IBNaviListener;
import com.baidu.navisdk.adapter.struct.BNGuideConfig;
import com.sifli.siflicore.error.SFError;
import com.sifli.sifliotasdk.manager.ISFPreviewVideoManagerCallback;
import com.sifli.sifliotasdk.manager.SFPreviewVideoConfiguration;
import com.sifli.sifliotasdk.manager.SFPreviewVideoManager;
import com.sifli.sifliotasdk.manager.SFTransmissionMode;

/**
 * Author: v_duanpeifeng
 * Time: 2020-03-30
 * Description:
 */
public class DemoAnalogActivity extends FragmentActivity implements ISFPreviewVideoManagerCallback {

    private static final String TAG = DemoAnalogActivity.class.getName();

    private IBNRouteGuideManager mRouteGuideManager;

    private IBNaviListener.DayNightMode mMode = IBNaviListener.DayNightMode.DAY;
    private SFPreviewVideoManager videoManager;
    private boolean isPreview;
    private Button previewBtn;
    private TextView fpsTv;
    private EditText macEt;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    private FrameLayout mapContainer = null;
    public MiniMapViewController miniMapViewController = new MiniMapViewController();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analog);

        // 模拟导航ui自定义,隐藏导航自带的退出、速度、开始/暂停按钮。
        BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                .setAnalogQuitButtonVisible(false);
        BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                .setAnalogSpeedButtonVisible(false);
        BaiduNaviManagerFactory.getProfessionalNaviSettingManager()
                .setAnalogSwitchButtonVisible(false);

        Bundle params = new Bundle();
        params.putBoolean(BNaviCommonParams.ProGuideKey.IS_REALNAVI, false);
        params.putBoolean(BNaviCommonParams.ProGuideKey.IS_SUPPORT_FULL_SCREEN,
                supportFullScreen());
        mRouteGuideManager = BaiduNaviManagerFactory.getRouteGuideManager();
        BNGuideConfig config = new BNGuideConfig.Builder()
                .params(params).build();
        View view = mRouteGuideManager.onCreate(this, config);
        FrameLayout layout = findViewById(R.id.mapView);
        if (view != null && view.getParent() != null) {
            ((ViewGroup) view.getParent()).removeAllViews();
        }
        layout.addView(view);
        mapContainer = layout;
        initListener();
        iniPreviewNav();
    }

    private void initListener() {
        BaiduNaviManagerFactory.getRouteGuideManager().setNaviListener(new BNDemoNaviListener() {
            @Override
            public void onNaviGuideEnd() {
                DemoAnalogActivity.this.finish();
            }
        });
    }

    private void iniPreviewNav(){

        this.previewBtn = findViewById(R.id.analog_start_preview_btn);
        this.fpsTv = findViewById(R.id.analog_fps_tv);
        this.macEt = findViewById(R.id.analog_mac_et);
//        BaiduNaviManagerFactory.getMapManager().getMapView().getMap();
        this.videoManager = SFPreviewVideoManager.getInstance();
        this.videoManager.setCallback(this);
        this.videoManager.init(this.getApplication(), SFTransmissionMode.TRANSMISSION_MODE_SPP);
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
        String targetMac = this.macEt.getText().toString();
        miniMapViewController.setTargetMac(targetMac);
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

    @RequiresApi(api = Build.VERSION_CODES.M)
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
//        if(videoManager != null){
//            videoManager.stop();
//        }
        if(miniMapViewController != null){
            miniMapViewController.onDestroy();
        }
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

    public void onClick(View view) {
        if (R.id.resume == view.getId()) {
            mRouteGuideManager.resumeRouteGuide();
        } else if (R.id.pause == view.getId()) {
            mRouteGuideManager.pauseRouteGuide();
        } else if (R.id.quit == view.getId()) {
            finish();
        }else if(R.id.analog_start_preview_btn == view.getId()){
            isPreview = !isPreview;

            if (isPreview) {
                startPreview();
                previewBtn.setText("停止预览");
            } else {
                stopPreview();
                previewBtn.setText("开始预览");
            }
        }else if(R.id.analog_open_mini_btn == view.getId()){
            BNDemoUtils.setBoolean(DemoAnalogActivity.this,
                    BNDemoUtils.KEY_GB_MINI_MAP_TYPE, true);
            initMiniMapView();
        }else if (R.id.analog_start_hide_mini_btn == view.getId()){
            this.miniMapViewController.showOrHide();
        }
    }

    private void  cycleImage(){
        if(!isPreview)return;

       BaiduNaviManagerFactory.getMapManager().getMapView().getMap().snapshot(new BaiduMap.SnapshotReadyCallback() {
           @Override
           public void onSnapshotReady(Bitmap bitmap) {
               videoManager.previewVideoSample(bitmap);
               mainHandler.postDelayed(new Runnable() {
                   @Override
                   public void run() {
                       cycleImage();
                   }
               },100);
           }
       });

    }

    private void startPreview(){
        int width = 800;
        int height = 480;
        int rotation = 90;
        float quality = 0.5f;
        SFPreviewVideoConfiguration config = new SFPreviewVideoConfiguration();
        config.setWatchScreenWidth(width);
        config.setWatchScreenHeight(height);
        config.setJpegQuality(quality);
        config.setMirroredHorizontally(false);
        config.setRotation(rotation);
        this.cycleImage();
        this.videoManager.startPreviewVideo(config,"11:22:33:44:88:8E");
    }

    private  void stopPreview(){
        this.videoManager.stop();
    }

    @Override
    public void completeWithError(SFPreviewVideoManager sfPreviewVideoManager, SFError sfError) {
        this.isPreview = false;
        this.previewBtn.setText("开始预览");
        String msg = String.format("completeWithError:%s",sfError);
        toast(msg);
    }

    @Override
    public void updateManagerState(SFPreviewVideoManager sfPreviewVideoManager, int i) {

    }

    @Override
    public void onFps(SFPreviewVideoManager sfPreviewVideoManager, float v) {
        this.fpsTv.setText("" + v);
    }

    @Override
    public void onImageMake(byte[] bytes) {

    }

    @Override
    public void onHandShake() {

    }

    @Override
    public void onFrameSent() {

    }

    private void toast(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }
}
