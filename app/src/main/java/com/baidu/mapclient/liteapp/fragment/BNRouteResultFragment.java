/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.mapclient.liteapp.fragment;

import java.util.ArrayList;
import java.util.List;

import com.baidu.mapclient.liteapp.BNDemoFactory;
import com.baidu.mapclient.liteapp.BNDemoUtils;
import com.baidu.mapclient.liteapp.R;
import com.baidu.mapclient.liteapp.activity.BNDemoLightNaviActivity;
import com.baidu.mapclient.liteapp.routeresult.PreferItemsAdapter;
import com.baidu.mapclient.liteapp.routeresult.RouteSortModel;
import com.baidu.navisdk.adapter.BNLightNaviListener;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNaviCommonParams;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNLightNaviManager;
import com.baidu.navisdk.adapter.IBNRoutePlanManager;
import com.baidu.navisdk.adapter.struct.BNRoutePlanInfos;
import com.baidu.navisdk.adapter.struct.BNRoutePlanItem;
import com.baidu.navisdk.adapter.struct.BNaviInfo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class BNRouteResultFragment extends BNBaseFragment {

    private View contentView = null;

    /**
     * isSwitchToNavi 为 true 时返回该页面
     * startBackgroundLightNavi 即可重新开启轻导航
     */
    private boolean isSwitchToNavi = false;

    private LinearLayout mLayoutTab0;
    private LinearLayout mLayoutTab1;
    private LinearLayout mLayoutTab2;

    List<BNRoutePlanItem> mRoutePlanItems;

    private RecyclerView mPreferRecyclerView;
    private PreferItemsAdapter mItemsAdapter;
    private PopupWindow mPopWindow;
    private ArrayList<RouteSortModel> mRouteSortList;
    private int currentPrefer = IBNRoutePlanManager.RoutePlanPreference.ROUTE_PLAN_PREFERENCE_DEFAULT;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.onsdk_fragment_light_navi_result, container, false);
        BaiduNaviManagerFactory.getLightNaviManager().onCreate(getActivity());
        BaiduNaviManagerFactory.getLightNaviSettingManager().setRouteMargin(
                100,
                BNDemoUtils.dip2px(getActivity(), 100),
                100,
                BNDemoUtils.dip2px(getActivity(), 200));
        initView();
        initPreferPopWindow();
        init();
        return contentView;
    }

    private void init() {
        isSwitchToNavi = false;
        /**
         * 显示多路线
         */
        BaiduNaviManagerFactory.getCommonSettingManager().setMultiRouteEnable(true);
        BaiduNaviManagerFactory.getLightNaviManager().setLightNaviListener(
                new BNLightNaviListener() {
                    @Override
                    public void onRoadNameUpdate(String name) {
                    }

                    @Override
                    public void updateGuideInfo(BNaviInfo info) {
                        refreshGuideView(info);
                    }

                    @Override
                    public void onRemainInfoUpdate(int remainDistance, int remainTime) {
                        refreshTopView(remainDistance, remainTime);
                        /**
                         * 获取实时的路线数据
                         */
                        refreshTabView(BaiduNaviManagerFactory.getLightNaviManager().getRemainRouteInfo());
                    }

                    @Override
                    public void onMainRouteChanged() {
                        /**
                         * 路线变更获取3Tab数据
                         */
                        initData();
                        /**
                         * 获取实时的路线数据
                         */
                        refreshTabView(BaiduNaviManagerFactory.getLightNaviManager().getRemainRouteInfo());
                    }
                });
        BaiduNaviManagerFactory.getLightNaviManager().startLightNavi();

        BaiduNaviManagerFactory.getLightNaviManager().setRouteClickedListener(
                new IBNLightNaviManager.IRouteClickedListener() {
                    @Override
                    public void routeClicked(int index) {
                        routeChange(index);
                    }
                });
    }

    public void routeChange(int index) {
        switch (index) {
            case 0:
                mLayoutTab0.setSelected(true);
                mLayoutTab1.setSelected(false);
                mLayoutTab2.setSelected(false);
                break;
            case 1:
                mLayoutTab0.setSelected(false);
                mLayoutTab1.setSelected(true);
                mLayoutTab2.setSelected(false);
                break;
            case 2:
                mLayoutTab0.setSelected(false);
                mLayoutTab1.setSelected(false);
                mLayoutTab2.setSelected(true);
                break;
            default:
                break;
        }
    }

    public void initView() {
        mLayoutTab0 = contentView.findViewById(R.id.route_0);
        mLayoutTab0.setOnClickListener(onClickListener);
        mLayoutTab1 = contentView.findViewById(R.id.route_1);
        mLayoutTab1.setOnClickListener(onClickListener);
        mLayoutTab2 = contentView.findViewById(R.id.route_2);
        mLayoutTab2.setOnClickListener(onClickListener);
        contentView.findViewById(R.id.btn_start_navi).setOnClickListener(onClickListener);
        contentView.findViewById(R.id.btn_prefer).setOnClickListener(onClickListener);
        contentView.findViewById(R.id.btn_fullView).setOnClickListener(onClickListener);
        contentView.findViewById(R.id.btn_road).setOnClickListener(onClickListener);
    }

    /**
     * 偏好弹窗
     */
    private void initPreferPopWindow() {
        View popView =
                LayoutInflater.from(getContext()).inflate(R.layout.dialog_pop_prefer, null, false);
        mPreferRecyclerView = popView.findViewById(R.id.nsdk_route_sort_gv);
        initPreferView();
        mPopWindow = new PopupWindow(popView, LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        mPopWindow.setOutsideTouchable(true);
        mPopWindow.setTouchable(true);
    }

    private void initPreferView() {
        initRouteSortList();
        mPreferRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        mPreferRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        if (mItemsAdapter == null) {
            mItemsAdapter = new PreferItemsAdapter(getContext(), mRouteSortList);
            mItemsAdapter.setClickPreferListener(new PreferItemsAdapter.ClickPreferListener() {
                @Override
                public void onClickPrefer(int clickPrefer) {
                    currentPrefer = clickPrefer;
                    mItemsAdapter.updatePrefer(clickPrefer);
                    mItemsAdapter.notifyDataSetChanged();
                    mPopWindow.dismiss();
                    routePlan();
                }
            });
        }
        mPreferRecyclerView.setAdapter(mItemsAdapter);
    }

    private void routePlan() {
        List<BNRoutePlanNode> list = new ArrayList<>();
        list.add(BNDemoFactory.getInstance().getCurrentNode(getActivity(), 0));
        list.add(BNDemoFactory.getInstance().getEndNode(getActivity()));

        Bundle bundle = new Bundle();
        bundle.putInt(BNaviCommonParams.RoutePlanKey.VEHICLE_TYPE,
                IBNRoutePlanManager.Vehicle.TRUCK);
        BaiduNaviManagerFactory.getRoutePlanManager().routePlan(
                list,
                currentPrefer,
                bundle, handler);
    }

    /**
     * 偏好重算路
     */
    private void reCalcRouteWithPrefer() {
        BaiduNaviManagerFactory.getRouteGuideManager().reCalcRouteWithPrefer(currentPrefer);
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_START:
                    Toast.makeText(getActivity(), "算路开始", Toast.LENGTH_SHORT).show();
                    break;
                case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_SUCCESS:
                    Toast.makeText(getActivity(), "算路成功", Toast.LENGTH_SHORT).show();
                    BaiduNaviManagerFactory.getLightNaviManager().startLightNavi();
                    break;
                case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_FAILED:
                    Toast.makeText(getActivity(),
                            "算路失败", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    // nothing
                    break;
            }
        }
    };

    private void initRouteSortList() {
        mRouteSortList = new ArrayList<>();
        mRouteSortList.add(new RouteSortModel("智能推荐", IBNRoutePlanManager.RoutePlanPreference
                .ROUTE_PLAN_PREFERENCE_DEFAULT));
        mRouteSortList.add(new RouteSortModel("时间优先", IBNRoutePlanManager.RoutePlanPreference
                .ROUTE_PLAN_PREFERENCE_TIME_FIRST));
        mRouteSortList.add(new RouteSortModel("少收费", IBNRoutePlanManager.RoutePlanPreference
                .ROUTE_PLAN_PREFERENCE_NOTOLL));
        mRouteSortList.add(new RouteSortModel("躲避拥堵", IBNRoutePlanManager.RoutePlanPreference
                .ROUTE_PLAN_PREFERENCE_AVOID_TRAFFIC_JAM));
        mRouteSortList.add(new RouteSortModel("不走高速", IBNRoutePlanManager.RoutePlanPreference
                .ROUTE_PLAN_PREFERENCE_NOHIGHWAY));
        mRouteSortList.add(new RouteSortModel("高速优先", IBNRoutePlanManager.RoutePlanPreference
                .ROUTE_PLAN_PREFERENCE_ROAD_FIRST));
    }

    @Override
    public void onStart() {
        super.onStart();
        BaiduNaviManagerFactory.getLightNaviManager().onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        BaiduNaviManagerFactory.getLightNaviManager().onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        BaiduNaviManagerFactory.getLightNaviManager().onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        BaiduNaviManagerFactory.getLightNaviManager().onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BaiduNaviManagerFactory.getLightNaviManager().stopLightNavi(isSwitchToNavi);
        BaiduNaviManagerFactory.getLightNaviManager().onDestroy(isSwitchToNavi);
    }

    @Override
    public void goBack() {

    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.route_0:
                    if (mRoutePlanItems.size() < 1) {
                        return;
                    }
                    mLayoutTab0.setSelected(true);
                    mLayoutTab1.setSelected(false);
                    mLayoutTab2.setSelected(false);
                    BaiduNaviManagerFactory.getRoutePlanManager().selectRoute(0, null);
                    break;
                case R.id.route_1:
                    if (mRoutePlanItems.size() < 2) {
                        return;
                    }
                    mLayoutTab0.setSelected(false);
                    mLayoutTab1.setSelected(true);
                    mLayoutTab2.setSelected(false);
                    BaiduNaviManagerFactory.getRoutePlanManager().selectRoute(1, null);
                    break;
                case R.id.route_2:
                    if (mRoutePlanItems.size() < 3) {
                        return;
                    }
                    mLayoutTab0.setSelected(false);
                    mLayoutTab1.setSelected(false);
                    mLayoutTab2.setSelected(true);
                    BaiduNaviManagerFactory.getRoutePlanManager().selectRoute(2, null);
                    break;
                case R.id.btn_start_navi:
                    if (BaiduNaviManagerFactory.getLightNaviManager().startProfessionalNavi()) {
                        isSwitchToNavi = true;
                        ((BNDemoLightNaviActivity) getActivity()).jumpTo("BNProNaviFragment");
                    } else {
                        Toast.makeText(getActivity(),
                                "路线有误，无法进入专业导航", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.btn_prefer:
                    mPopWindow.showAtLocation(contentView, Gravity.BOTTOM, 0, 0);
                    break;
                case R.id.btn_fullView:
                    BaiduNaviManagerFactory.getLightNaviManager().fullView();
                    break;
                case R.id.btn_road:
                    boolean roadCondition = BaiduNaviManagerFactory.getLightNaviManager().isRoadConditionOpen();
                    BaiduNaviManagerFactory.getLightNaviManager().setRoadCondition(!roadCondition);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 获取3Tab数据，重算路后获取
     * 多路线自动选路或推送新路线后该数据不准
     * 包含路线详细信息 具体请查看 BNRoutePlanInfos
     */
    private void initData() {
        BNRoutePlanInfos routePlaneInfos = BaiduNaviManagerFactory.getRoutePlanManager()
                .getRoutePlanInfo();
        String mtoll = "";
        for (int i = 0; i < routePlaneInfos.getRouteTabInfos().size(); i++) {
            mtoll += routePlaneInfos.getRouteTabInfos().get(i).getToll() + ", ";
        }
        Toast.makeText(getActivity(), "收费路段费用：" + mtoll, Toast.LENGTH_SHORT).show();
        if (routePlaneInfos == null) {
            return;
        }
    }

    private void refreshTabView(List<BNRoutePlanItem> items) {
        mRoutePlanItems = items;
        if (items != null) {
            if (items.size() > 0 && items.get(0) != null) {
                initTabView(mLayoutTab0, items.get(0));
            }

            if (items.size() > 1 && items.get(1) != null) {
                initTabView(mLayoutTab1, items.get(1));
                mLayoutTab1.setVisibility(View.VISIBLE);
            } else {
                mLayoutTab1.setVisibility(View.GONE);
            }

            if (items.size() > 2 && items.get(2) != null) {
                initTabView(mLayoutTab2, items.get(2));
                mLayoutTab2.setVisibility(View.VISIBLE);
            } else {
                mLayoutTab2.setVisibility(View.GONE);
            }
        }
        /**
         * 根据当前路线初始化显示
         */
        routeChange(BaiduNaviManagerFactory.getRoutePlanManager().getSelectRouteId());
    }

    private void initTabView(LinearLayout layoutTab, BNRoutePlanItem bnRoutePlanItem) {
        TextView prefer = layoutTab.findViewById(R.id.prefer);
        prefer.setText(bnRoutePlanItem.getPusLabelName());
        TextView time = layoutTab.findViewById(R.id.time);
        time.setText((int) bnRoutePlanItem.getPassTime() / 60 + "分钟");
        TextView distance = layoutTab.findViewById(R.id.distance);
        distance.setText((int) bnRoutePlanItem.getLength() / 1000 + "公里");
        TextView trafficLight = layoutTab.findViewById(R.id.traffic_light);
        trafficLight.setText(String.valueOf(bnRoutePlanItem.getLights()));
    }

    // 以下可以不关注 与 单路线轻导航页相同（PM非要）
    public void refreshGuideView(BNaviInfo info) {
        ( (TextView) contentView.findViewById(R.id.distance)).setText(info.distance + "米");
        ( (TextView) contentView.findViewById(R.id.route_name)).setText(info.roadName);
        ( (ImageView) contentView.findViewById(R.id.turnIcon)).setImageBitmap(info.turnIcon);
    }

    public void refreshTopView(int remainDistance, int remainTime) {
        if (remainDistance > 0 && remainTime > 0) {
            ( (TextView) contentView.findViewById(R.id.remain_distance)).setText("剩余：" + remainDistance + "米");
            ( (TextView) contentView.findViewById(R.id.remain_time)).setText("剩余：" + remainTime + "秒");
        }
    }

}
