package com.baidu.mapclient.liteapp.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.Image;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.baidu.mapclient.liteapp.R;
import com.baidu.mapclient.liteapp.service.MyMediaProjectionService;
import com.baidu.mapclient.liteapp.util.speedview.SpeedView;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.sifli.siflicore.error.SFError;
import com.sifli.siflicore.log.SFLog;
import com.sifli.siflicore.shell.SFBleShellStatus;
import com.sifli.siflicore.util.StringUtil;
import com.sifli.sifliimagelib.helper.SifliImageHelper;
import com.sifli.sifliotasdk.manager.ISFPreviewVideoManagerCallback;
import com.sifli.sifliotasdk.manager.SFPreviewBaseManager;
import com.sifli.sifliotasdk.manager.SFPreviewVideoConfiguration;
import com.sifli.sifliotasdk.manager.SFPreviewVideoManager;
import com.sifli.sifliotasdk.manager.SFTransmissionMode;

public class AirPlayActivity extends AppCompatActivity
        implements View.OnClickListener,
        MyMediaProjectionService.MyMedioOnImageAvailableListener,
        ISFPreviewVideoManagerCallback {
    public final static String EXTRA_BLE_DEVICE = "EXTRA_BLE_DEVICE";
    public final static String EXTRA_TRANS_MODE = "EXTRA_TRANS_MODE";
    public final static String EXTRA_IP = "EXTRA_IP";
    public final static String EXTRA_PORT = "EXTRA_PORT";
    public final static String EXTRA_MAX_FPS = "EXTRA_MAX_FPS";
    public final static String EXTRA_JPEG_QUALITY = "EXTRA_JPEG_QUALITY";
    public final static String EXTRA_SIZE_WIDTH = "EXTRA_SIZE_WIDTH";
    public final static String EXTRA_SIZE_HEIGHT = "EXTRA_SIZE_HEIGHT";
    public final static String EXTRA_SOCKET_MTU = "EXTRA_SOCKET_MTU";

    private static final String TAG = "AirPlayActivity";
    private static final int REQUEST_CODE_SCREEN_CAPTURE = 100;

    private Button airplayBtn;
    private Button stopBtn;
    private TextView speedTv;
    private MediaProjectionManager projectionManager;

    // 服务相关
    private MyMediaProjectionService mediaService;
    private boolean isBound = false;
    // 临时保存 MediaProjection（若获取时服务尚未绑定）
    private MediaProjection pendingProjection;
    // 屏幕尺寸（从系统获取）
    private int screenWidth;
    private int screenHeight;
    private int screenDensity;

    private SFPreviewVideoManager manager;
    private SifliImageHelper imageHelper;

    private SpeedView speedView;
    private float fps;

    private String targetMac = "FF:FF:79:DA:77:C8";//525
    private String ip = "192.168.49.1";
    private int port = 2025;
    private int targetWidth = 200;
    private int targetHeight = 200;
    private int maxFps = 10;
    private float jpegQuality = 0.2f;
    private int socketMtu = 16;
    private int transMode = SFTransmissionMode.TRANSMISSION_MODE_BLE;

    private KProgressHUD hud;
    private boolean isPreview;
    private boolean isStopMirror = false;

    // 服务连接回调
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyMediaProjectionService.LocalBinder binder = (MyMediaProjectionService.LocalBinder) service;
            mediaService = binder.getService();
            isBound = true;
            SFLog.d(TAG, "Service 绑定成功");

            // 设置图像监听（当前 Activity 实现了回调接口）
            mediaService.setOnImageAvailableListener(AirPlayActivity.this);

            // 如果之前已经获取到 MediaProjection，立即传递给 Service
            if (pendingProjection != null) {
                mediaService.initProjection(pendingProjection, screenWidth, screenHeight, screenDensity);
                pendingProjection = null;
                SFLog.d(TAG, "已传递 MediaProjection 给 Service");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mediaService = null;
            isBound = false;
            SFLog.d(TAG, "Service 断开连接");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_air_play);

        projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        // 获取屏幕尺寸
        getScreenDimensions();

        initView();
        bindEvent();
        this.init();

        this.speedView = new SpeedView();
        this.imageHelper = new SifliImageHelper();
        this.manager = SFPreviewVideoManager.getInstance();
        this.manager.setCallback(this);
        this.manager.init(this.getApplication(), this.transMode, this.imageHelper);
    }

    private void getScreenDimensions() {
        android.view.WindowManager wm = (android.view.WindowManager) getSystemService(WINDOW_SERVICE);
        if (wm != null) {
            android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(metrics);
            screenWidth = metrics.widthPixels;
            screenHeight = metrics.heightPixels;
            screenDensity = metrics.densityDpi;
        } else {
            // 兜底值
            screenWidth = 1080;
            screenHeight = 1920;
            screenDensity = 320;
        }
    }

    private void initView() {
        airplayBtn = findViewById(R.id.navi_airplay_start_btn);
        stopBtn = findViewById(R.id.navi_airplay_stop_btn);
        speedTv = findViewById(R.id.navi_airplay_speed_tv);
    }

    private void bindEvent() {
        airplayBtn.setOnClickListener(this);
        stopBtn.setOnClickListener(this);
    }

    private void init(){
        String mac = getIntent().getStringExtra(EXTRA_BLE_DEVICE);
        this.transMode = getIntent().getIntExtra(EXTRA_TRANS_MODE, SFTransmissionMode.TRANSMISSION_MODE_BLE);
        if(mac != null){
            targetMac = mac;
        }
        this.ip = getIntent().getStringExtra(EXTRA_IP);
        this.port = getIntent().getIntExtra(EXTRA_PORT,2025);
        this.maxFps = getIntent().getIntExtra(EXTRA_MAX_FPS,20);
        this.jpegQuality = getIntent().getFloatExtra(EXTRA_MAX_FPS,0.2f);
        this.targetWidth = getIntent().getIntExtra(EXTRA_SIZE_WIDTH,400);
        this.targetHeight = getIntent().getIntExtra(EXTRA_SIZE_HEIGHT,400);
        this.socketMtu = getIntent().getIntExtra(EXTRA_SOCKET_MTU,16);


    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.navi_airplay_start_btn) {
            onAirPlayBtnTouch();
        }else if(viewId == R.id.navi_airplay_stop_btn){
            onStopAirplayBtnTouch();
        }
    }

    private void onAirPlayBtnTouch() {
        SFLog.i(TAG,"onAirPlayBtnTouch");
        // 1. 如果尚未绑定服务，则启动并绑定
        if (!isBound) {
            Intent intent = new Intent(this, MyMediaProjectionService.class);
            // 必须先 startService 才能成为前台服务
            startService(intent);
            // 绑定服务
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }

        // 2. 请求录屏权限（系统弹窗）
        Intent captureIntent = projectionManager.createScreenCaptureIntent();
        startActivityForResult(captureIntent, REQUEST_CODE_SCREEN_CAPTURE);
        this.isStopMirror = false;
    }

    private void onStopAirplayBtnTouch(){
        SFLog.i(TAG,"onStopAirplayBtnTouch");
        this.isStopMirror = true;
        this.manager.stop();
        this.stopScreenMirroring();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SCREEN_CAPTURE) {
            if (resultCode == RESULT_OK) {
                MediaProjection projection = projectionManager.getMediaProjection(resultCode, data);
                if (isBound && mediaService != null) {
                    // 服务已绑定，直接初始化
                    mediaService.initProjection(projection, screenWidth, screenHeight, screenDensity);
                    this.startPreview();
                    Toast.makeText(this, "录屏已开始", Toast.LENGTH_SHORT).show();
                } else {
                    // 尚未绑定（极少情况），暂存
                    pendingProjection = projection;
                    Toast.makeText(this, "录屏授权成功，等待服务绑定", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "用户拒绝录屏权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ------------------- 图像回调（后续集成 SDK） -------------------
    @Override
    public void onImageAvailable(Image image) {
        // 注意：此回调运行在 Service 的后台线程
        // 在这里可以集成 SifliotSDK，将 Image 转码或直接传递
        // 务必在操作完成后关闭 Image，否则内存泄漏
        SFLog.d(TAG, "收到图像帧: " + image.getWidth() + "x" + image.getHeight());
        if(!isPreview){
            image.close();
            return;
        }
        if(isStopMirror){
            image.close();
            return;
        }
        this.manager.previewVideoSample(image);

        // 【示例】直接释放（后续替换为 SDK 处理）
        // 如果 SDK 是异步处理，需要拷贝数据后再 close
        image.close();
    }

    // ------------------- 生命周期管理 -------------------
    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.isStopMirror = true;
        // 解绑服务（但一般投屏希望后台继续，因此这里仅解绑，不停止服务）
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
            SFLog.d(TAG, "已解绑服务，服务继续在后台运行");
        }
        this.manager.stop();
        // 如果希望 Activity 销毁时也停止录屏，可以取消注释以下代码：
        // if (mediaService != null) {
        //     mediaService.stopProjection();
        // }
    }

    // 可选：用户主动停止投屏的方法（可以加一个停止按钮调用）
    public void stopScreenMirroring() {
        if (mediaService != null) {
            mediaService.stopProjection();
        }
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
    }

    private void showProgressHUD(String statusText){
        hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel(statusText)
                .setCancellable(false) // 是否可点击外部取消
                .setAnimationSpeed(2) // 动画速度
                .setDimAmount(0.5f) // 背景变暗程度
                .show();
    }

    private void dismissProgressHUD(){
        if (hud != null && hud.isShowing()) {
            hud.dismiss();
            hud = null;
        }
    }

    private void updateProgressHUDText(String newText) {
        if (hud != null && hud.isShowing()) {
            hud.setLabel(newText);
        }
    }

    private void startPreview(){
        SFLog.i(TAG,"startPreview");
        this.showProgressHUD("正在连接...");
        SFPreviewVideoConfiguration config = new SFPreviewVideoConfiguration();
        config.setPreviewType(SFPreviewVideoConfiguration.PREVIEW_TYPE_VIDEO);
        config.setWatchScreenWidth(targetWidth);
        config.setWatchScreenHeight(targetHeight);
        config.setJpegQuality(jpegQuality);
        config.setMirroredHorizontally(false);
        config.setMaxFps(maxFps);
        config.setRotation(90);
        if (this.transMode == SFTransmissionMode.TRANSMISSION_MODE_SOCKET_CLIENT) {
            if (StringUtil.isNullOrEmpty(ip)) {
                Toast.makeText(this, "ip 参数异常", Toast.LENGTH_SHORT).show();
                return;
            }
            this.manager.setSocketMtu(this.socketMtu);
            this.manager.startPreviewVideo(config, ip, port,null);
            SFLog.i(TAG,"start Preview..." + ip);
        } else if (this.transMode == SFTransmissionMode.TRANSMISSION_MODE_BLE || this.transMode == SFTransmissionMode.TRANSMISSION_MODE_SPP) {
            this.manager.startPreviewVideo(config, targetMac);
            SFLog.i(TAG,"start Preview..." + targetMac);
        } else if (this.transMode == SFTransmissionMode.TRANSMISSION_MODE_SOCKET_SERVER) {
            this.manager.setSocketMtu(this.socketMtu);
            this.manager.startPreviewVideoAsSocketServer(config);
            SFLog.i(TAG,"start Preview as socket server...");
        }
    }

    private void toast(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }

    private void setIsPreview(boolean isPreview){
        this.isPreview = isPreview;

    }

    //region ISFPreviewVideoManagerCallback
    @Override
    public void completeWithError(SFPreviewBaseManager manager, SFError error) {
        SFLog.i(TAG, "completeWithError =" + error);
        this.dismissProgressHUD();
        this.manager.stop();
        if(error != null){
            this.toast(error.toString());
        }

    }

    @Override
    public void updateManagerState(SFPreviewBaseManager manager, int managerStatus) {
        SFLog.i(TAG,"updateManagerState %d",managerStatus);
        boolean b = managerStatus == SFBleShellStatus.MODULE_WORKING;
        setIsPreview(b);
        if(this.isPreview){

        }
        if(managerStatus == SFBleShellStatus.NONE || managerStatus == SFBleShellStatus.MODULE_WORKING){
            this.dismissProgressHUD();
        }
    }

    @Override
    public void onFps(SFPreviewBaseManager manager, float fps) {
        this.fps = fps;
    }

    @Override
    public void onImageMake(byte[] jpgData) {

    }

    @Override
    public void onSendImageCount(long imageCount, long sendBytes) {
        String fpsText = String.format("fps:%.1f ",this.fps);
        String speedTxt = fpsText + this.speedView.getCurrentSpeedText();
        this.speedView.viewSpeedByCompleteBytes(sendBytes);
        this.speedTv.setText(speedTxt);
    }

    @Override
    public void onPreviewModeChange(int previewMode) {
        SFLog.i(TAG,"onPreviewModeChange %d",previewMode);
    }
    //endregion
}