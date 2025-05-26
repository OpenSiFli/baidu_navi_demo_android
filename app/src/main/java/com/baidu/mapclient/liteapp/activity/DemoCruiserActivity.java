package com.baidu.mapclient.liteapp.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.baidu.mapclient.liteapp.R;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNCruiserManager;

public class DemoCruiserActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cruiser);

        findViewById(R.id.open_cruiser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BaiduNaviManagerFactory.getCruiserManager().isCruiserStarted()) {
                    BaiduNaviManagerFactory.getCruiserManager().startCruiser(DemoCruiserActivity.this,
                            new IBNCruiserManager.ICruiserListener() {
                                @Override
                                public void onCruiserStart() {
                                    Toast.makeText(DemoCruiserActivity.this, "电子狗开启",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        findViewById(R.id.close_cruiser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BaiduNaviManagerFactory.getCruiserManager().isCruiserStarted()) {
                    BaiduNaviManagerFactory.getCruiserManager().stopCruise();
                }
            }
        });
    }
}
