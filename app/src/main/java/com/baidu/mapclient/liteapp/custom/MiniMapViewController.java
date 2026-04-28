package com.baidu.mapclient.liteapp.custom;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.provider.Settings;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.mapclient.liteapp.BNDemoUtils;
import com.baidu.mapclient.liteapp.R;
import com.baidu.mapclient.liteapp.activity.BNDemoLightNaviActivity;
import com.baidu.mapclient.liteapp.config.SFNaviOption;
import com.baidu.mapclient.liteapp.listener.BNDemoNaviListener;
import com.baidu.mapclient.liteapp.service.MinimapService;
import com.baidu.mapclient.liteapp.sifliui.NavBitmapFactory;
import com.baidu.mapclient.liteapp.sifliui.SFEnlargeMapInfo;
import com.baidu.mapclient.liteapp.sifliui.SFLineInfo;
import com.baidu.mapclient.liteapp.sifliui.SFNavInfo;
import com.baidu.mapclient.liteapp.sifliui.SFTopRightInfo;
import com.baidu.mapclient.liteapp.tts.TTSHolder;
import com.baidu.mapclient.liteapp.util.speedview.SpeedView;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNMiniMapViewManager;
import com.baidu.navisdk.adapter.IBNOuterSettingParams;
import com.baidu.navisdk.adapter.struct.BNavLineItem;
import com.baidu.navisdk.adapter.struct.BNaviInfo;
import com.baidu.navisdk.adapter.struct.GuidePanelMessage;
import com.sifli.siflicore.error.SFError;
import com.sifli.siflicore.error.SFErrorCode;
import com.sifli.siflicore.image.ISifliImageHelper;
import com.sifli.siflicore.log.SFLog;
import com.sifli.siflicore.shell.SFBleShellStatus;
import com.sifli.sifliimagelib.helper.SifliImageHelper;
import com.sifli.sifliotasdk.manager.ISFPreviewVideoManagerCallback;
import com.sifli.sifliotasdk.manager.SFPreviewVideoConfiguration;
import com.sifli.sifliotasdk.manager.SFPreviewVideoManager;
import com.sifli.sifliotasdk.manager.SFTransmissionMode;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MiniMapViewController implements IBNMiniMapViewManager, ISFPreviewVideoManagerCallback {

    private  final  String TAG = "MiniMapViewController";
    private  static int miniWidth = 800;
    private  static int miniHeight = 480;
    /**
     * 多实例底图
     */
    private IBNMiniMapViewManager miniMapViewManager;
    private View minimapView;
    private RelativeLayout mRootView;
    private Context mContext;
    private boolean fullViewState = false;
    private boolean touchable = false;
    private BNDemoNaviListener naviListener;
    private EditText enlargeViewSize;

    private boolean showElement = false;
    private boolean isPause = false;
    private Button bgButton;
    private Button snapBtn;

    private int speed;
   private SFPreviewVideoManager videoManager;
   private Handler mainHandler = new Handler(Looper.getMainLooper());
   private boolean isPreview = false;
   private TextView fpsTv;
   private LinearLayout topGuideInfoLl;
   private RelativeLayout bottomGuideInfoRl;
   private RelativeLayout enlargeLayout;
    // handler
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private String targetMac;
    private int transMode;
    private SpeedView speedView;
    private float fps;
    //上次截取的快照，保鲜期80毫秒
    private Bitmap lastSnapMap;
    private long lastSnapTimestamp;
    private final int KEEP_SNAP_TIME = 80;

    private final SFNavInfo navInfo;
    private final SFTopRightInfo topRightInfo;
    private final SFLineInfo lineInfo;
    private final SFEnlargeMapInfo enlargeMapInfo;

    public MiniMapViewController() {
        SFNaviOption op = SFNaviOption.getInstance();
        miniWidth = op.getWidth();
        miniHeight = op.getHeight();
        SFLog.i(TAG,"init with width=%d,height=%d,quality=%.1f,max fps=%d",op.getWidth(),op.getHeight(),op.getJpegQuality(),op.getMaxFPS());
        this.navInfo = new SFNavInfo((int)(miniWidth * 0.4),100,10);
        this.topRightInfo = new SFTopRightInfo((int)(miniWidth * 0.4),100,10);
        this.lineInfo = new SFLineInfo((int)(miniWidth * 0.4),60,10);
        this.enlargeMapInfo = new SFEnlargeMapInfo();
    }

    @Override
    public View onCreate(Context context) {
        Log.i(TAG,"onCreate");
        if (!BNDemoUtils.getBoolean(context, BNDemoUtils.KEY_GB_MINI_MAP_TYPE)) {
            return null;
        }
        miniMapViewManager = BaiduNaviManagerFactory.getMiniMap();


        mContext = context;
        mRootView = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.minimap_layout, null, false);
        mRootView.setBackgroundColor(Color.parseColor("#3385ff"));
        minimapView = miniMapViewManager.onCreate(context);
        if (minimapView != null) {
            minimapView.setLayoutParams(new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
            mRootView.addView(minimapView, 0);
            miniMapViewManager.setMapDpiScale(0.5f);
            miniMapViewManager.openBackgroundDrawNavi(BNDemoUtils.getBoolean(mContext, BNDemoUtils.KEY_GB_MINI_MAP_OPEN_BG_DRAW, true));
        } else {
            return null;
        }
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(miniWidth, miniHeight);
        mRootView.setLayoutParams(layoutParams);
        init(mRootView, context);
        viewHide(false);
        initPreviewNav();
        mRootView.animate().translationXBy(0).translationYBy(500).setDuration(0);
        enlarge();
        enlarge();
        return mRootView;
    }

    private void initPreviewNav(){
        this.speedView = new SpeedView();
        this.startBackgroundThread();
        this.videoManager = SFPreviewVideoManager.getInstance();
        this.videoManager.setCallback(this);
        Activity a = (Activity)mContext;
        this.transMode = SFTransmissionMode.TRANSMISSION_MODE_SPP;
        if(SFNaviOption.getInstance().isUseSocket()){
            this.transMode = SFTransmissionMode.TRANSMISSION_MODE_SOCKET;
        }
        SFLog.i(TAG,"initPreviewNav Trans Mode = %d",this.transMode);
        ISifliImageHelper imageHelper = new SifliImageHelper();
        this.videoManager.init(a.getApplication(), this.transMode,imageHelper);
    }

    private void init(ViewGroup rootView, Context context) {
        enlargeViewSize = rootView.findViewById(R.id.enlarge_map_size);
        bgButton = rootView.findViewById(R.id.btn_open_bg);
        snapBtn = rootView.findViewById(R.id.btn_save_image);
        fpsTv = rootView.findViewById(R.id.bnav_fps_tv);
        topGuideInfoLl = rootView.findViewById(R.id.guide_info);
        bottomGuideInfoRl = rootView.findViewById(R.id.relative_bottom_layout);
        enlargeLayout = rootView.findViewById(R.id.enlarge_map_layout);
        rootView.findViewById(R.id.btn_open_bg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"onClick btn_open_bg");
//                if (isPause) {
//                    onResume();
//                    bgButton.setText("模拟调用onPause");
//                } else {
//                    onPause();
//                    bgButton.setText("模拟调用onResume");
//                }
//                isPause = !isPause;

                isPreview = !isPreview;

                if (isPreview) {
//                    startPreview();
                    onStartPreviewBtnTouch();
                    bgButton.setText("停止预览");
                } else {
                   stopPreview();
                    bgButton.setText("开始预览");
                }
//                saveBitmapToFile(getMapViewBitmap(), System.currentTimeMillis() + ".png");
            }
        });
        snapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSnap();
            }
        });

        rootView.findViewById(R.id.btn_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startServiceMinimap();
            }
        });
        rootView.findViewById(R.id.btn_fullView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fullView(!fullViewState);
            }
        });
        rootView.findViewById(R.id.btn_touchable).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                touchAble(!touchable);
            }
        });
        rootView.findViewById(R.id.btn_move).setOnTouchListener(new View.OnTouchListener() {
            private float lastX;
            private float lastY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getX();
                float y = event.getY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = x;
                        lastY = y;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        float deltaX = x - lastX;
                        float deltaY = y - lastY;
                        if (Math.abs(deltaX) > 5 || Math.abs(deltaY) > 5) {
                            rootView.animate().translationXBy(deltaX).translationYBy(deltaY).setDuration(0);

                            lastX = x;
                            lastY = y;
                        }
                        break;
                    default:
                        break;
                }

                return true;
            }
        });

        rootView.findViewById(R.id.btn_enlarge).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                int width ;
//                int height ;
//                String size = enlargeViewSize.getText().toString();
//                if (!TextUtils.isEmpty(size)) {
//                    String[] sizes = size.split(",");
//                    if (sizes != null && sizes.length == 2) {
//                        width = Integer.parseInt(sizes[0]);
//                        height = Integer.parseInt(sizes[1]);
//                    }
//                }
               enlarge();
            }
        });

        rootView.findViewById(R.id.btn_hide).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (showElement){
                    ((Button) rootView.findViewById(R.id.btn_hide)).setText("显示元素");
                } else{
                    ((Button) rootView.findViewById(R.id.btn_hide)).setText("隐藏元素");
                }
                ArrayList<MapElementTypeEnum> arrCloseType = new ArrayList<>();
                arrCloseType.add(MapElementTypeEnum.Undefined);
                miniMapViewManager.setMapElementShow(showElement);
                showElement = !showElement;

            }
        });

        RecyclerView naviModeLayout = rootView.findViewById(R.id.navi_mode_type_layout);
        final boolean[] visibility = {false};
        rootView.findViewById(R.id.btn_navimode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visibility[0] = !visibility[0];
                if (visibility[0]) {
                    naviModeLayout.setVisibility(View.VISIBLE);
                } else {
                    naviModeLayout.setVisibility(View.GONE);
                }
            }
        });
        final boolean[] btnVisibility = {true};
        View btnLayout = rootView.findViewById(R.id.btn_layout);
        View btnLayoutLeft = rootView.findViewById(R.id.btn_layout_left);
        rootView.findViewById(R.id.btn_hide_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnVisibility[0] = !btnVisibility[0];
                if (btnVisibility[0]) {
                    btnLayout.setVisibility(View.VISIBLE);
                    btnLayoutLeft.setVisibility(View.VISIBLE);
                } else {
                    btnLayout.setVisibility(View.GONE);
                    btnLayoutLeft.setVisibility(View.GONE);
                }
            }
        });
        initNaviMode(naviModeLayout, context);
        initNaviListener();
        initLanelineView(new ArrayList<>());
        initSetting();

    }

    private void initSetting() {
        Boolean isOpen = BNDemoUtils.getBoolean(mContext, BNDemoUtils.KEY_GB_ICONSET, false);
        if (isOpen){
            Bitmap bitmap = BNDemoLightNaviActivity.getbitmap(mContext, "car.png");
            miniMapViewManager.setDIYImageToMap(bitmap, MapDIYImageTypeEnum.CarLogo);
        }
    }

    private int selectPosotion = -1;
    RecyclerView.Adapter adapter;

    /**
     * 底图导航模式设置
     * @param recyclerView
     * @param context
     */
    private void initNaviMode(RecyclerView recyclerView, Context context) {
        recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
        recyclerView.setAdapter(adapter = new RecyclerView.Adapter() {

            private List<String> naviMode = new ArrayList<String>(){
                {
                    add("普通导航"); //  NaviMode.NORMAL
                    add("轻导航"); // NaviMode.SLIGHT
                    add("电子狗"); // NaviMode.CRUISE
                    add("导航结束页"); // NaviMode.FINISH
                    add("驾车路线页"); // NaviMode.ROUTE
                }
            };

            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                TextView tv = new TextView(context);
                tv.setBackgroundResource(R.drawable.bnav_setting_btn_bg_selector);
                ViewGroup.MarginLayoutParams layoutParams = new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(20, 20, 20, 20);
                tv.setPadding(10, 10, 10, 10);
                tv.setLayoutParams(layoutParams);
                return new ViewHolder(tv);
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                boolean isSelected = (selectPosotion == position);
                ((TextView) holder.itemView).setText(naviMode.get(position));
                holder.itemView.setTag(position);
                holder.itemView.setSelected(isSelected);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.itemView.setSelected(true);
                        setNaviMode((int) holder.itemView.getTag() + 1);
                    }
                });
            }

            @Override
            public int getItemCount() {
                return 5;
            }

            class ViewHolder extends RecyclerView.ViewHolder {
                public ViewHolder(@NonNull View itemView) {
                    super(itemView);
                }
            }
        });
    }

    private void enlarge(){
        ViewGroup.LayoutParams layoutParams = mRootView.getLayoutParams();
        if (layoutParams.width == miniWidth) {
            layoutParams.width = miniWidth;
            layoutParams.height = miniHeight;
            offset(0, -250);
            ((TextView) mRootView.findViewById(R.id.btn_enlarge)).setText("缩小");
        } else {
            layoutParams.width = miniWidth;
            layoutParams.height = miniHeight;
            ((TextView) mRootView.findViewById(R.id.btn_enlarge)).setText("放大");
            offset(0, -250);
        }
        mRootView.setLayoutParams(layoutParams);
        mRootView.requestLayout();
        viewHide(false);


    }

    public void viewHide(boolean isEnlarge) {
        int visibility = isEnlarge ? View.VISIBLE : View.GONE;
        mRootView.findViewById(R.id.btn_fullView).setVisibility(visibility);
        mRootView.findViewById(R.id.btn_touchable).setVisibility(View.GONE);
        mRootView.findViewById(R.id.navi_mode).setVisibility(visibility);
        mRootView.findViewById(R.id.relative_top_layout).setVisibility(View.VISIBLE);
        mRootView.findViewById(R.id.relative_bottom_layout).setVisibility(View.VISIBLE);
        mRootView.findViewById(R.id.guide_info).setVisibility(View.VISIBLE);
        mRootView.findViewById(R.id.btn_hide_btn).setVisibility(View.GONE);
        mRootView.findViewById(R.id.enlarge_map_size).setVisibility(View.GONE);
        mRootView.findViewById(R.id.btn_hide).setVisibility(visibility);
    }

    public void showOrHide(){
        if(mRootView == null)return;
            int visibility = mRootView.getVisibility();
            if(visibility == View.VISIBLE){
             visibility = View.INVISIBLE;
            }else{
                visibility = View.VISIBLE;
            }
            mRootView.setVisibility(visibility);
    }



    @Override
    public void onResume() {
        if (miniMapViewManager != null) {
            miniMapViewManager.onResume();
        }
    }

    @Override
    public void onPause() {
        if (miniMapViewManager != null) {
            miniMapViewManager.onPause();
        }
    }

    public void startServiceMinimap() {
        if (isOverlayPermission(mContext)){
            Intent serviceIntent = new Intent(mContext, MinimapService.class);
            mContext.startService(serviceIntent);
        } else {
            jumpToPermission();
        }

    }

    // 判断是否有悬浮窗权限
    public boolean isOverlayPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        } else {
            try {
                Class clazz = Settings.class;
                Method method = clazz.getDeclaredMethod("canDrawOverlays", Context.class);
                return (Boolean) method.invoke(null, context);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    // 跳转系统设置-悬浮窗页面
    public void jumpToPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + mContext.getPackageName()));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    @Override
    public void onDestroy() {
        SFLog.i(TAG,"onDestroy mini");
        this.isPreview = false;
        if (miniMapViewManager != null) {
            miniMapViewManager.onDestroy();
        }
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        removeNaviListener();

        this.stopBackgroundThread();
        if(this.videoManager != null){
            this.videoManager.stop();
        }
    }

    /**
     *
     * @param i {@link IBNMiniMapViewManager.NaviMode}
     */
    @Override
    public void setNaviMode(int i) {
        selectPosotion = i - 1;
        // 底图状态
        if (miniMapViewManager != null) {
            miniMapViewManager.setNaviMode(i);
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void setNightMode(boolean nightMode) {
        if (miniMapViewManager != null) {
            miniMapViewManager.setNightMode(nightMode);
        }
    }

    @Override
    public void fullView(boolean fullView) {
        // 全览
        if (miniMapViewManager != null) {
            fullViewState = fullView;
            miniMapViewManager.fullView(fullViewState);
        }
    }

    @Override
    public void touchAble(boolean isTouchable) {
        // 底图是否允许操作
        if (miniMapViewManager != null) {
            touchable = isTouchable;
            miniMapViewManager.touchAble(isTouchable);
        }
    }

    @Override
    public void offset(long xOffSet, long yOffSet) {
        // 车标偏移
        if (miniMapViewManager != null) {
            miniMapViewManager.offset(xOffSet, yOffSet);
        }
    }

    @Override
    public void openBackgroundDrawNavi(boolean b) {
        if (miniMapViewManager != null) {
            miniMapViewManager.openBackgroundDrawNavi(b);
        }
    }

    @Override
    public Bitmap getMapViewBitmap() {
        if (miniMapViewManager != null) {
            return miniMapViewManager.getMapViewBitmap();
        }
        return null;
    }

    @Override
    public void snapshotScope(SnapshotReadyCallback snapshotReadyCallback, boolean b) {
        if(miniMapViewManager != null){
            miniMapViewManager.snapshotScope(snapshotReadyCallback,b);
        }
    }

    @Override
    public void setFullViewMarginSize(int left, int top, int right, int bottom) {
        if (miniMapViewManager != null) {
            miniMapViewManager.setFullViewMarginSize(0, 0, 0, 0);
        }
    }

    @Override
    public void setRouteClickedListener(IRouteClickedListener iRouteClickedListener) {
        if (miniMapViewManager != null) {
            miniMapViewManager.setRouteClickedListener(iRouteClickedListener);
        }
    }

    @Override
    public boolean setRotateMode(int i) {
        if(miniMapViewManager != null){
            return miniMapViewManager.setRotateMode(i);
        }
        return  false;
    }

    @Override
    public void setLogoVisibleAndPosition(boolean b, int i, int i1, int i2, int i3, int i4) {
        if(miniMapViewManager != null){
            miniMapViewManager.setLogoVisibleAndPosition(b,i,i1,i2,i3,i4);
        }
    }

    @Override
    public boolean setFixedLevelEnable(boolean b) {
        if(miniMapViewManager != null){
            return miniMapViewManager.setFixedLevelEnable(b);
        }
        return false;
    }

    @Override
    public boolean setFixedLevel(float v) {
        if (miniMapViewManager != null) {
            return miniMapViewManager.setFixedLevel(v);
        }
        return false;
    }

    @Override
    public void setCustomStyleEnable(boolean b, String s) {
        if (miniMapViewManager != null) {
            miniMapViewManager.setCustomStyleEnable(b, s);
        }
    }

    @Override
    public void setShowCarLogoToEndRedLine(boolean b) {
        if(miniMapViewManager != null){
            miniMapViewManager.setShowCarLogoToEndRedLine(b);
        }
    }

    @Override
    public void setIconElementShow(boolean b) {
        if(miniMapViewManager != null){
            miniMapViewManager.setIconElementShow(b);
        }
    }

    @Override
    public void hideIconElement(ArrayList<IBNOuterSettingParams.CarIconElementTypeEnum> arrayList) {
        if(miniMapViewManager != null){
            miniMapViewManager.hideIconElement(arrayList);
        }
    }

    @Override
    public void setTrafficEnabled(boolean b) {
        if(miniMapViewManager != null){
            miniMapViewManager.setTrafficEnabled(b);
        }
    }

    @Override
    public void setNaviRouteHalfWidth(float v, float v1, float v2, float v3) {
        if(miniMapViewManager != null){
            miniMapViewManager.setNaviRouteHalfWidth(v,v1,v2,v3);
        }
    }

    @Override
    public boolean setNaviRouteDIYImageToMap(Bitmap bitmap, IBNOuterSettingParams.RouteDIYImageTypeEnum routeDIYImageTypeEnum) {
        if(miniMapViewManager != null){
            return miniMapViewManager.setNaviRouteDIYImageToMap(bitmap,routeDIYImageTypeEnum);
        }
        return false;
    }

    private RecyclerView lanelineList;
    private List<BNavLineItem> mLaneItems = new ArrayList<>();
    private RecyclerView.Adapter laneLineAdapter;

    /**
     * 车道线
     * @param laneItems
     */
    public void initLanelineView(List<BNavLineItem> laneItems) {
        mLaneItems = laneItems;
        lanelineList = mRootView.findViewById(R.id.lane_line_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        lanelineList.setLayoutManager(linearLayoutManager);
        lanelineList.setAdapter(laneLineAdapter = new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                ImageView imageView = new ImageView(mContext);
                imageView.setLayoutParams(new LinearLayout.LayoutParams(70, LinearLayout.LayoutParams.MATCH_PARENT));
                return new ImageViewHolder(imageView);
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                ((ImageView) holder.itemView).setImageDrawable(mLaneItems.get(position).getDrawable());
            }

            @Override
            public int getItemCount() {
                return mLaneItems.size();
            }

            class ImageViewHolder extends RecyclerView.ViewHolder {
                public ImageViewHolder(ImageView itemView) {
                    super(itemView);
                }
            }
        });
    }

    public void initNaviListener() {
        // 已支持同时设置多个监听
        BaiduNaviManagerFactory.getRouteGuideManager()
                .setNaviListener(naviListener = new BNDemoNaviListener() {

                    @Override
                    public void onSpeedUpdate(int speed, int overSpeed) {
                        Log.e("onSpeedUpdate", "speed:" + speed);
                        mRootView.post(new Runnable() {
                            @Override
                            public void run() {
                                ((TextView) mRootView.findViewById(R.id.speed_txt)).setText(speed + "km/h");
                            }
                        });
                    }

                    @Override
                    public void onLaneInfoUpdate(Action action, List<BNavLineItem> laneItems) {
                        Log.e("onLaneInfoUpdate", action.toString() + (laneItems != null ? laneItems.size() : "null"));
                        lanelineList.post(new Runnable() {
                            @Override
                            public void run() {
                                mLaneItems = laneItems;

                                if (action == Action.HIDE) {
                                    lanelineList.setVisibility(View.GONE);
                                    lineInfo.setVisible(false);
                                } else {
                                    lanelineList.setVisibility(View.VISIBLE);
                                    laneLineAdapter.notifyDataSetChanged();
                                    lineInfo.setVisible(true);
                                    lineInfo.clear();
                                    for (BNavLineItem item:mLaneItems) {
                                        lineInfo.addLine(item.getDrawable());
                                    }
                                    lineInfo.regenerateBitmap();
                                }
                            }
                        });
                    }

                    @Override
                    public void onEnlargeMapUpdate(Action action, View enlargeMap, String remainDistance, int progress,
                                                   String roadName, Bitmap turnIcon, SpannableStringBuilder stringBuilder) {
                        Log.e("onEnlargeMapUpdate", (action != null ? action.toString() : "??" ) + "|" + (stringBuilder != null ? stringBuilder.toString() : ""));
                        mRootView.post(new Runnable() {
                            @Override
                            public void run() {
                                if (action != Action.HIDE) {
                                    showEnlargeMap(enlargeMap, stringBuilder);
                                } else {
                                    Toast.makeText(mContext, "隐藏放大图", Toast.LENGTH_SHORT).show();
                                    ((RelativeLayout) mRootView.findViewById(
                                            R.id.enlarge_map_layout)).removeAllViews();
                                    enlargeView = null;
                                    enlargeMapInfo.setVisible(false);
                                }
                            }
                        });
                    }

                    @Override
                    public void onRemainInfoUpdate(int remainDistance, int remainTime) {
                        mRemainDistance = remainDistance;
                        mRemainTime = remainTime;
                        remainInfoShow();
                    }

                    @Override
                    public void onViaListRemainInfoUpdate(int[] remainDists, int[] remainTimes) {
                        mRemainDists = remainDists;
                        mRemainTimes = remainTimes;
                        remainInfoShow();
                    }

                    @Override
                    public void onRemainLightsUpdate(int remainLights, int viaRemainLights) {
                        mRemainLights = remainLights;
                        mViaRemainLights = viaRemainLights;
                        remainInfoShow();
                    }

                    @Override
                    public void onGuideInfoUpdate(BNaviInfo naviInfo, GuidePanelMessage guidePanelMessage) {
                        mRootView.post(new Runnable() {
                            @Override
                            public void run() {
                                if (naviInfo != null) {
//                                    ((ImageView) mRootView.findViewById(R.id.guideinfo_icon)).setImageDrawable(guidePanelMessage.getIcon());
//                                    ((TextView) mRootView.findViewById(R.id.guideinfo_txt)).setText(guidePanelMessage.getStringBuilder());
                                    BitmapDrawable d  = new BitmapDrawable(naviInfo.getTurnIcon());
                                    String distanceText = getDistanceText(naviInfo.getDistance());
                                    ((ImageView) mRootView.findViewById(R.id.guideinfo_icon)).setImageDrawable(d);
                                    ((TextView) mRootView.findViewById(R.id.guideinfo_txt)).setText(distanceText + "进入\n" + naviInfo.getRoadName());
                                    navInfo.setTurnIcon(naviInfo.getTurnIcon());
                                    navInfo.setDistanceText(distanceText + "进入");
                                    navInfo.setRoadNameText(naviInfo.getRoadName());
                                    navInfo.setVisible(true);
                                    SFLog.i(TAG,naviInfo.toString());

                                } else {
                                    Toast.makeText(mContext, "诱导信息为空！！", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });

        setRouteClickedListener(new IRouteClickedListener() {
            @Override
            public void routeClicked(int i) {
                Log.e("routeClicked", "index:" + i);
            }
        });
    }

    private View etaView;

    private int mRemainDistance;
    private int mRemainTime;

    int[] mRemainDists;
    int[] mRemainTimes;

    int mRemainLights;
    int mViaRemainLights;

    Timer mTimer;
    TimerTask mTask;
    int showType;

    public void remainInfoShow() {
        if (etaView == null) {
            etaView = LayoutInflater.from(mContext).inflate(R.layout.onsdk_remain_info_layout, null, false);
            RelativeLayout.LayoutParams layoutParams =
                    new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            etaView.setElevation(0);
            ((RelativeLayout) mRootView.findViewById(
                    R.id.relative_bottom_layout)).addView(etaView, layoutParams);
        }

        if (mTimer == null) {
            mTimer = new Timer();
            mTask = new TimerTask() {
                @Override
                public void run() {
                    showType++;
                }
            };
            mTimer.schedule(mTask, 5000, 5000);
        }
        updateRemainInfo(showType);
    }

    public void updateRemainInfo(int type) {
        if (etaView == null) {
            return;
        }
        etaView.post(new Runnable() {
            @Override
            public void run() {
                int time;
                if (type == 1 && mRemainDists != null && mRemainDists.length > 0) {
                    String remainInfo = "途:" + getRemainDist(mRemainDists[0]) +
                            getFormatTime(mRemainTimes[0]) + mViaRemainLights + "红绿灯";
                    ((TextView) etaView.findViewById(R.id.remain_info)).setText(remainInfo);
                    time = mRemainTimes[0];
                    topRightInfo.setRemainInfo(remainInfo);
                } else {
                    String remainInfo = "终:" + getRemainDist(mRemainDistance) + getFormatTime(mRemainTime) + mRemainLights + "红绿灯";
                    ((TextView) etaView.findViewById(R.id.remain_info)).setText(remainInfo);
                    time = mRemainTime;
                    topRightInfo.setRemainInfo(remainInfo);
                }
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.SECOND, time);
                Date newTime = calendar.getTime();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                String arriveInfo = "预计" + sdf.format(newTime) + "到达";
                ((TextView) etaView.findViewById(R.id.arrive_info)).setText(arriveInfo);
                topRightInfo.setArriveInfo(arriveInfo);
                topRightInfo.setVisible(true);
            }
        });
    }

    public String getRemainDist(int dist) {
        return dist > 1000 ? String.format("%.1f", dist / 1000.0d) + "公里 " : dist + "米";
    }

    public static String getFormatTime(long time) {
        if (time < 60) {
            return "小于1分钟";
        }

        long hour1 = time / 3600;
        long minute1 = time % 3600 / 60;
        long second1 = time % 60;
        if (second1 >= 30) {
            minute1 += 1;
        }

        if (minute1 > 59) {
            hour1 += 1;
            minute1 = 0;
        }

        String hourContent = hour1 < 1 ? "" : (hour1 + "小时");
        String minuteContent;
        if (minute1 < 1) {
            minuteContent = "";
        } else {
            minuteContent = minute1 + (hour1 < 1 ? "分钟" : "分");
        }

        return hourContent + minuteContent;
    }


    private View enlargeView;
    /**
     * 放大图展示
     */
    public void showEnlargeMap(View enlargeMap, SpannableStringBuilder stringBuilder) {
        if (enlargeView == null) {
            enlargeView = LayoutInflater.from(mContext).inflate(R.layout.onsdk_enlarge_map_layout, null, false);
            if (enlargeMap.getParent() != null) {
                ((ViewGroup) enlargeMap.getParent()).removeView(enlargeMap);
            }

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(300, 200);
            layoutParams.addRule(RelativeLayout.BELOW, R.id.lane_line_list);
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            ((RelativeLayout) enlargeView.findViewById(R.id.enlarge_map)).addView(enlargeMap, layoutParams);

            RelativeLayout.LayoutParams layoutParams1 =
                    new RelativeLayout.LayoutParams(300, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams1.addRule(RelativeLayout.BELOW, R.id.lane_line_list);
            layoutParams1.addRule(RelativeLayout.CENTER_HORIZONTAL);
            ((RelativeLayout) mRootView.findViewById(
                    R.id.enlarge_map_layout)).addView(enlargeView, layoutParams1);
        }
        if (stringBuilder == null) {
            return;
        }

        // 重新调整stringBuilder中转向或随后等icon大小
        ImageSpan[] imageSpans = stringBuilder.getSpans(0, stringBuilder.length(), ImageSpan.class);
        for (ImageSpan span : imageSpans) {
            Drawable originalDrawable = span.getDrawable();
            originalDrawable.setBounds(0, 0, 50, 70);
            ImageSpan newImageSpan = new ImageSpan(originalDrawable, span.getVerticalAlignment());
            stringBuilder.setSpan(newImageSpan, stringBuilder.getSpanStart(span),
                    stringBuilder.getSpanEnd(span), stringBuilder.getSpanFlags(span));
            stringBuilder.removeSpan(span);
        }

        // 重新调整stringBuilder中文本字体
        TypefaceSpan[] typefaceSpans = stringBuilder.getSpans(0, stringBuilder.length(), TypefaceSpan.class);
        for (TypefaceSpan span : typefaceSpans) {
            float sizeRatio = 0.9f;
            RelativeSizeSpan sizeSpan = new RelativeSizeSpan(sizeRatio);

            stringBuilder.setSpan(sizeSpan, stringBuilder.getSpanStart(span),
                    stringBuilder.getSpanEnd(span), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            // 移除原始的 TypefaceSpan
            stringBuilder.removeSpan(span);
        }



        ((TextView) enlargeView.findViewById(R.id.enlarge_message)).setText(stringBuilder);
        Bitmap enlargeMapBitmap = makeViewBitmap(enlargeView);
        enlargeMapInfo.setEnlargeMap(enlargeMapBitmap);
        enlargeMapInfo.setVisible(true);
    }

    private void removeNaviListener() {
        BaiduNaviManagerFactory.getRouteGuideManager().removeNaviListener(naviListener);
    }


//    @Override
//    public void setMapElementShow(boolean show, ArrayList<MapElementTypeEnum> arrCloseType) {
//        if (miniMapViewManager != null) {
//            miniMapViewManager.setMapElementShow(show, arrCloseType);
//        }
//    }

    @Override
    public void setMapDpiScale(float scale) {
        if (miniMapViewManager != null) {
            miniMapViewManager.setMapDpiScale(scale);
        }
    }


    @Override
    public boolean setDIYImageToMap(Bitmap pngBitmap, MapDIYImageTypeEnum imageType) {
        if (miniMapViewManager != null) {
            miniMapViewManager.setDIYImageToMap(pngBitmap, imageType);
        }
        return false;
    }

    @Override
    public void setMapElementShow(boolean b) {
        if(miniMapViewManager != null){
            miniMapViewManager.setMapElementShow(b);
        }
    }

    @Override
    public void hideMapElement(ArrayList<MapElementTypeEnum> arrayList) {
        if(miniMapViewManager != null){
            miniMapViewManager.hideMapElement(arrayList);
        }
    }

    @Override
    public boolean getAuthResult() {
        if (miniMapViewManager != null) {
            miniMapViewManager.getAuthResult();
        }
        return false;
    }


    public void saveBitmapToFile(Bitmap bitmap, String filename) {
        FileOutputStream fos = null;
        try {
            File file = new File(mContext.getExternalCacheDir(), filename);
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            Log.i(TAG,"saveBitmapToFile " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void saveSnap(){
        Bitmap map = this.getMapViewBitmap();
        Bitmap navInfo = makeViewBitmap(this.topGuideInfoLl);
        Bitmap bottomInfo = makeViewBitmap(this.bottomGuideInfoRl);
        Bitmap lineListMap = null;
        if(lanelineList.getVisibility() == View.VISIBLE){
            lineListMap = makeViewBitmap(this.lanelineList);
        }
        Bitmap enlargeMap = null;
        if(enlargeView != null){
            enlargeMap = makeViewBitmap(enlargeLayout);
        }
        Bitmap bitmap = NavBitmapFactory.mergeBitmap(map,navInfo,bottomInfo,enlargeMap,lineListMap);
        if(bitmap != null)saveBitmapToFile(bitmap, System.currentTimeMillis() + ".png");
    }

    public static Bitmap makeViewBitmap(View view) {
        // 创建与View相同尺寸的Bitmap
        if(view.getWidth() == 0 || view.getHeight()== 0){
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(
                view.getWidth(),
                view.getHeight(),
                Bitmap.Config.ARGB_8888
        );

        // 创建Canvas并绘制View内容

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

//    private void  cycleImage(){
//        if(!isPreview)return;
//        if(this.videoManager == null)return;
//        boolean canSendImage = this.videoManager.canSendBitmap();
//        if(!canSendImage){
//            //sdk 还没发完，不需要制作图片
//            this.mBackgroundHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    cycleImage();
//                }
//            },50);
//            return;
//        }
//        this.miniMapViewManager.snapshotScope(new SnapshotReadyCallback() {
//            @Override
//            public void onSnapshotReady(Bitmap bitmap) {
//                onSnapReady(bitmap);
//            }
//        },true);
//
//    }

    private void makeSnapMap(){
        SFLog.i(TAG,"makeSnapMap");
        this.miniMapViewManager.snapshotScope(new SnapshotReadyCallback() {
            @Override
            public void onSnapshotReady(Bitmap bitmap) {
                lastSnapMap = bitmap;
                lastSnapTimestamp = System.currentTimeMillis();
                SFLog.i(TAG,"makeSnapMap ready size %d * %d",bitmap.getWidth(),bitmap.getHeight());
            }
        },true);

    }
    private void  sendSingleImage(){
        if(!isPreview)return;
        if(this.videoManager == null)return;
        boolean canSendImage = this.videoManager.canSendBitmap();
        if(!canSendImage){
            //sdk 还没发完，不需要制作图片
            SFLog.e(TAG,"sendSingleImage when videoManager is busy");
            return;
        }
        byte[] mp3Data = TTSHolder.getInstance().getMp3Data();
        if(mp3Data != null){
            SFLog.i(TAG,"sendSingleImage preview audio data %d",mp3Data.length);
            long ts = System.currentTimeMillis();
            this.videoManager.previewAudio(mp3Data,ts);
        }else{

            long now = System.currentTimeMillis();
            if(this.lastSnapMap == null || now - lastSnapTimestamp > KEEP_SNAP_TIME){
                SFLog.i(TAG,"need make snap map...");
                this.mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        makeSnapMap();
                    }
                });
//                SFLog.i(TAG,"sendSingleImage request snapshotScope");
//                this.miniMapViewManager.snapshotScope(new SnapshotReadyCallback() {
//                    @Override
//                    public void onSnapshotReady(Bitmap bitmap) {
//                        onSnapReady(bitmap);
//                    }
//                },true);
            }else{
                SFLog.i(TAG,"sendSingleImage use lastSnapMap");

            }

            if(this.lastSnapMap != null){
                onSnapReady(this.lastSnapMap);
            }else{
                SFLog.e(TAG,"snap map is null");
            }

        }
    }

    private  void onSnapReady(Bitmap map){
        SFLog.i(TAG,"onSnapReady size %d * %d",map.getWidth(),map.getHeight());
//        Bitmap navInfo = makeViewBitmap(this.topGuideInfoLl);
//        Bitmap bottomInfo = makeViewBitmap(this.bottomGuideInfoRl);
//        Bitmap lineListMap = null;
//        if(lanelineList.getVisibility() == View.VISIBLE){
//            lineListMap = makeViewBitmap(this.lanelineList);
//        }
//        Bitmap enlargeMap = null;
//        if(enlargeView != null){
//            enlargeMap = makeViewBitmap(enlargeView);
//        }
//        Bitmap bitmap = NavBitmapFactory.mergeBitmap(map,navInfo,bottomInfo,enlargeMap,lineListMap);
        Bitmap bitmap = NavBitmapFactory.makeBitmap(map,navInfo,topRightInfo,enlargeMapInfo,lineInfo);
        SFLog.i(TAG,"previewVideoSample...");
        long ts = System.currentTimeMillis();
        if(bitmap != null)this.videoManager.previewVideoSample(bitmap,ts);
//        this.mBackgroundHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                cycleImage();
//            }
//        },50);
    }

    private void onStartPreviewBtnTouch(){
        SFLog.i(TAG,"onStartPreviewBtnTouch trans mode=%d",this.transMode);
        this.speedView.clear();
        if(this.transMode == SFTransmissionMode.TRANSMISSION_MODE_SOCKET){
            this.videoManager.startTcpListen(2025);
            this.videoManager.setSocketMtu(16);
        }else{
            this.startPreview();
        }
    }
    private void startPreview(){
        if(this.targetMac == null || this.targetMac.isEmpty()){
            toast("target mac is null or empty");
            return;
        }
        NavBitmapFactory.clearCache();
        int width = SFNaviOption.getInstance().getWidth();
        int height = SFNaviOption.getInstance().getHeight();
        int rotation = 0;
        float quality = SFNaviOption.getInstance().getJpegQuality();
        int maxFps = SFNaviOption.getInstance().getMaxFPS();
        SFPreviewVideoConfiguration config = new SFPreviewVideoConfiguration();
        config.setPreviewType(SFPreviewVideoConfiguration.PREVIEW_TYPE_IMAGE);
        config.setAspectSizeForNaviMap(false);
        config.setWatchScreenWidth(width);
        config.setWatchScreenHeight(height);
        config.setJpegQuality(quality);
        config.setMirroredHorizontally(false);
        config.setRotation(rotation);
        config.setMaxFps(maxFps);
//        this.cycleImage();
        this.makeSnapMap();
        this.videoManager.startPreviewVideo(config,targetMac);
//        this.videoManager.startPreviewVideo(config,"BB:00:00:AB:00:19");
    }

    public   void stopPreview(){
        this.isPreview = false;
        this.videoManager.stop();
    }

    private void startBackgroundThread() {
        if (mBackgroundThread == null || mBackgroundHandler == null) {
            Log.v(TAG, "startBackgroundThread");
            mBackgroundThread = new HandlerThread("Sol2ModuleBackground") {
                @Override
                public void run() {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_DISPLAY); // 设置为显示优先级
                    super.run();
                }
            };

            mBackgroundThread.start();
            mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
        }
    }

    private void stopBackgroundThread() {
        Log.v(TAG, "stopBackgroundThread");
        try {
            if(mBackgroundHandler != null){
                mBackgroundHandler.removeCallbacksAndMessages(null);
                mBackgroundHandler = null;
            }
            if(mBackgroundThread != null){
                mBackgroundThread.quitSafely();
                mBackgroundThread = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            SFLog.e(TAG,e.toString());
        }
    }

    @Override
    public void completeWithError(SFPreviewVideoManager sfPreviewVideoManager, SFError sfError) {
        this.isPreview = false;
        this.bgButton.setText("开始预览");
        String msg = String.format("completeWithError:%s",sfError);
        toast(msg);
        if(sfError != null && sfError.getCode() != SFErrorCode.SF_MANUAL_STOP){
            SFLog.i(TAG,"completeWithError,stop videoManager");
            this.videoManager.stop();
        }
    }

    @Override
    public void updateManagerState(SFPreviewVideoManager sfPreviewVideoManager, int status) {
        this.bgButton.setEnabled(status != SFBleShellStatus.SEARCH_AND_CONNECTING);
        if(this.transMode == SFTransmissionMode.TRANSMISSION_MODE_SOCKET){
            if(!this.videoManager.isBusy() && status == SFBleShellStatus.MODULE_WORKING){
                this.startPreview();
            }
        }
    }

    @Override
    public void onFps(SFPreviewVideoManager sfPreviewVideoManager, float v) {
        this.fps = v;

    }

    @Override
    public void onImageMake(byte[] bytes) {

    }

    @Override
    public void onMakeNextFrame(float process) {
        SFLog.i(TAG,"onMakeNextFrame %.1f",process);
        this.sendSingleImage();
    }

    @Override
    public void onReadyToSendImage() {
        SFLog.i(TAG,"onReadyToSendImage");
        this.sendSingleImage();
    }
    @Override
    public void onSendImageCount(long imageCount, long sendBytes) {
        SFLog.i(TAG,"onSendImageCount %d,sendBytes %d",imageCount,sendBytes);
        this.speedView.viewSpeedByCompleteBytes(sendBytes);
        String fpsText = String.format("%.1f",this.fps);
        this.fpsTv.setText(fpsText + "/" + this.speedView.getCurrentSpeedText());
//        this.speedView.viewSpeedByCompleteBytes(sendBytes);
//        this.speedTv.setText(this.speedView.getCurrentSpeedText());
    }


    private void toast(String msg){
        Toast.makeText(mContext,msg,Toast.LENGTH_SHORT).show();
    }
    private String getDistanceText(int distance){
        if(distance <= 20){
            return "现在";
        }else if(distance < 1000){
            return distance + "米";
        }else {
            float fdistance = distance;
            return  String.format("%.1f公里",fdistance/1000);
        }
    }

    public String getTargetMac() {
        return targetMac;
    }

    public void setTargetMac(String targetMac) {
        this.targetMac = targetMac;
    }
}
