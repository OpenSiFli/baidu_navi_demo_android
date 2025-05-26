package com.baidu.mapclient.liteapp.listener;

import android.graphics.Bitmap;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.view.View;

import com.baidu.navisdk.adapter.IBNaviListener;
import com.baidu.navisdk.adapter.struct.BNHighwayInfo;
import com.baidu.navisdk.adapter.struct.BNRoadCondition;
import com.baidu.navisdk.adapter.struct.BNaviInfo;
import com.baidu.navisdk.adapter.struct.BNaviLocation;
import com.baidu.navisdk.adapter.struct.GuidePanelMessage;
import com.baidu.navisdk.ui.routeguide.model.RGLineItem;

import java.util.List;

/**
 * Author: v_duanpeifeng
 * Time: 2020-07-23
 * Description:
 */
public abstract class BNDemoNaviListener extends IBNaviListener {

    @Override
    public void onRoadNameUpdate(String name) {

    }

    @Override
    public void onRemainInfoUpdate(int remainDistance, int remainTime) {

    }

    @Override
    public void onGuideInfoUpdate(BNaviInfo naviInfo, GuidePanelMessage guidePanelMessage) {

    }

    @Override
    public void onHighWayInfoUpdate(Action action, BNHighwayInfo info) {

    }

    @Override
    public void onFastExitWayInfoUpdate(Action action, String name, int dist, String id) {

    }

    @Override
    public void onEnlargeMapUpdate(Action action, View enlargeMap, String remainDistance,
                                   int progress, String roadName, Bitmap turnIcon, SpannableStringBuilder stringBuilder) {

    }

    @Override
    public void onDayNightChanged(DayNightMode style) {

    }

    @Override
    public void onRoadConditionInfoUpdate(double progress, List<BNRoadCondition> items) {

    }

    @Override
    public void onMainSideBridgeUpdate(int type) {

    }

    @Override
    public void onSpeedUpdate(int speed, int overSpeed) {

    }

    @Override
    public void onOverSpeed(int i, int i1) {

    }

    @Override
    public void onArriveDestination() {

    }

    @Override
    public void onArrivedWayPoint(int index) {

    }

    @Override
    public void onLocationChange(BNaviLocation naviLocation) {

    }

    @Override
    public void onMapStateChange(MapStateMode mapStateMode) {

    }

    @Override
    public void onStartYawing(String flag) {

    }

    @Override
    public void onYawingSuccess() {

    }

    @Override
    public void onNotificationShow(String msg) {

    }

    @Override
    public void onHeavyTraffic() {

    }

    @Override
    public void onNaviGuideEnd() {

    }

    @Override
    public void onPreferChanged(int i) {

    }

    @Override
    public void onViaListRemainInfoUpdate(int[] remainDists, int[] remainTimes) {

    }

    @Override
    public void onYawingArriveViaPoint(int index) {

    }
}
