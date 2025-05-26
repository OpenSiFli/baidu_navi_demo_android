package com.baidu.mapclient.liteapp.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.baidu.mapclient.liteapp.listener.BNDemoNaviListener;
import com.baidu.navisdk.adapter.BNaviCommonParams;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNRouteGuideManager;
import com.baidu.navisdk.adapter.struct.BNGuideConfig;

/**
 * 导航可自定义区域
 */
public class BNDemoCustomGuideActivity extends FragmentActivity {

    private IBNRouteGuideManager mRouteGuideManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        Bundle params = new Bundle();
        params.putBoolean(BNaviCommonParams.ProGuideKey.IS_SUPPORT_FULL_SCREEN, true);

        Bundle bundle = getIntent().getExtras();
        int customViewHeightTop = 100;
        int customViewHeightBottom = 100;
        if (bundle != null) {
            customViewHeightTop = bundle.getInt("custom_view_top_height", 100);
            customViewHeightBottom = bundle.getInt("custom_view_bottom_height", 100);
        }
        int finalCustomViewHeightTop = customViewHeightTop;
        int finalCustomViewHeightBottom = customViewHeightBottom;
        BNGuideConfig config = new BNGuideConfig.Builder()
                // 导航工具栏上方view
                .addAboveBottomView(new IBNRouteGuideManager.NaviAddViewCallback() {

                    @Override
                    public int getViewHeight() {
                        return finalCustomViewHeightTop;
                    }

                    @Override
                    public View getAddedView() {
                        TextView textView = new TextView(BNDemoCustomGuideActivity.this);
                        textView.setBackgroundColor(Color.parseColor("#ffff00"));
                        textView.setText("导航工具栏上方自定义空间");
                        textView.setGravity(Gravity.CENTER);
                        textView.setTextColor(Color.parseColor("#000000"));
                        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, 100);
                        textView.setLayoutParams(lp);
                        return textView;
                    }
                })
                // 导航工具栏view，该区域只有在进导航前调用下面的接口才会生效
                // BaiduNaviManagerFactory.getProfessionalNaviSettingManager().useOldSetting(true);
                .addBottomBarView(new IBNRouteGuideManager.NaviAddViewCallback() {

                    @Override
                    public View getAddedView() {
                        TextView textView = new TextView(BNDemoCustomGuideActivity.this);
                        textView.setBackgroundColor(Color.parseColor("#ff00ff"));
                        textView.setText("导航工具栏自定义空间");
                        textView.setGravity(Gravity.CENTER);
                        textView.setTextColor(Color.parseColor("#000000"));
                        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        textView.setLayoutParams(lp);
                        return textView;
                    }
                })
                // 导航工具栏下方view
                .addBelowBottomView(new IBNRouteGuideManager.NaviAddViewCallback() {

                    @Override
                    public int getViewHeight() {
                        return finalCustomViewHeightBottom;
                    }

                    @Override
                    public View getAddedView() {
                        TextView textView = new TextView(BNDemoCustomGuideActivity.this);
                        textView.setBackgroundColor(Color.parseColor("#00ffff"));
                        textView.setText("导航工具栏下方自定义空间");
                        textView.setGravity(Gravity.CENTER);
                        textView.setTextColor(Color.parseColor("#000000"));
                        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, 100);
                        textView.setLayoutParams(lp);
                        return textView;
                    }
                })
                .params(params)
                .build();

        mRouteGuideManager = BaiduNaviManagerFactory.getRouteGuideManager();
        View view = mRouteGuideManager.onCreate(this, config);
        if (view != null) {
            setContentView(view);
        }
        routeGuideEvent();
    }

    // 导航事件监听
    private void routeGuideEvent() {
        BaiduNaviManagerFactory.getRouteGuideManager().setNaviListener(new BNDemoNaviListener() {
            @Override
            public void onNaviGuideEnd() {
                BNDemoCustomGuideActivity.this.finish();
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
        mRouteGuideManager.onBackPressed(false);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mRouteGuideManager.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mRouteGuideManager.onActivityResult(requestCode, resultCode, data);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mRouteGuideManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}