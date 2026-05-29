package com.baidu.mapclient.liteapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;


import com.baidu.mapclient.liteapp.R;

import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.ZXingView;

public class QRScanActivity extends AppCompatActivity implements QRCodeView.Delegate,View.OnClickListener{
    private ZXingView mZXingView;
    private final static String TAG = "QRScanActivity";
    public final static String QR_RESULT_EXTRA_KEY = "QR_RESULT_EXTRA_KEY";
    private Button closeBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscan);
        mZXingView = findViewById(R.id.zxingview);
        mZXingView.setDelegate(this);
        closeBtn = findViewById(R.id.qrscan_clost_btn);
        closeBtn.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mZXingView.startCamera(); // 打开后置摄像头开始预览，但是并未开始识别
//        mZXingView.startCamera(Camera.CameraInfo.CAMERA_FACING_FRONT); // 打开前置摄像头开始预览，但是并未开始识别

        mZXingView.startSpotAndShowRect(); // 显示扫描框，并开始识别
    }

    @Override
    protected void onStop() {
        mZXingView.stopCamera(); // 关闭摄像头预览，并且隐藏扫描框
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mZXingView.onDestroy(); // 销毁二维码扫描控件
        super.onDestroy();
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        Intent intent = getIntent();
        intent.putExtra(QR_RESULT_EXTRA_KEY, result);
        setResult(RESULT_OK,intent);
        finish();
    }

    @Override
    public void onCameraAmbientBrightnessChanged(boolean isDark) {

    }

    @Override
    public void onScanQRCodeOpenCameraError() {

    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if(viewId == R.id.qrscan_clost_btn){
            finish();
        }
    }
}