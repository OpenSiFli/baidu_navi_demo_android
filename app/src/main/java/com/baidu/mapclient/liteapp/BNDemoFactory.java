package com.baidu.mapclient.liteapp;

import android.content.Context;
import android.text.TextUtils;

import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.struct.BNMotorInfo;
import com.baidu.navisdk.adapter.struct.BNTruckInfo;
import com.baidu.navisdk.adapter.struct.VehicleConstant;

/**
 * Author: v_duanpeifeng
 * Time: 2020-05-22
 * Description:
 */
public class BNDemoFactory {

    public static final int AXLES_NUMBER = 6;
    public static final int AXLES_WEIGHT = 100;
    public static final int LENGTH = 25;
    public static final int WEIGHT = 100;
    public static final int LOAD_WEIGHT = 80;
    public static final String OIL_COST = "40000";
    public static final int HEIGHT = 10;
    public static final float WIDTH = 5f;
    public static final double LATITUDE = 40.041690;
    public static final double LONGITUDE = 116.306333;
    public static final String NAME = "百度大厦";
    public static final double LATITUDE1 = 39.908560;
    public static final double LONGITUDE1 = 116.397609;
    public static final String NAME1 = "北京天安门";
    public static final double DOUBLE = 0.5;
    private BNRoutePlanNode startNode;

    private BNRoutePlanNode endNode;

    private BNDemoFactory() {
    }

    private static class Holder {
        private static BNDemoFactory instance = new BNDemoFactory();
    }

    public static BNDemoFactory getInstance() {
        return Holder.instance;
    }

    public void initCarInfo() {
        // 驾车车牌设置
        // BaiduNaviManagerFactory.getCommonSettingManager().setCarNum("京A88888");

        // 货车信息
        BNTruckInfo truckInfo = new BNTruckInfo.Builder()
                .truckUsage(VehicleConstant.TruckUsage.DANGER)
                .plate("京A88888")
                .axlesNumber(AXLES_NUMBER)
                .axlesWeight(AXLES_WEIGHT)
                .emissionLimit(VehicleConstant.EmissionStandard.S3)
                .length(LENGTH)
                .weight(WEIGHT)
                .loadWeight(LOAD_WEIGHT)
                .oilCost(OIL_COST)
                .plateType(VehicleConstant.PlateType.BLUE)
                .powerType(VehicleConstant.PowerType.OIL)
                .truckType(VehicleConstant.TruckType.HEAVY)
                .height(HEIGHT)
                .width(WIDTH)
                .build();
        // 该接口会做本地持久化，在应用中设置一次即可
        BaiduNaviManagerFactory.getCommonSettingManager().setTruckInfo(truckInfo);

        // 摩托车信息
        BNMotorInfo motorInfo = new BNMotorInfo.Builder()
                .plate("京A88888")
                .plateType(VehicleConstant.PlateType.BLUE)
                .motorType(VehicleConstant.MotorType.OIL)
                .displacement("")
                .build();
        // 该接口会做本地持久化，在应用中设置一次即可
        BaiduNaviManagerFactory.getCommonSettingManager().setMotorInfo(motorInfo);

        // BaiduNaviManagerFactory.getCommonSettingManager().setTestEnvironment(false);
        BaiduNaviManagerFactory.getCommonSettingManager().setNodeClick(true);
    }

    public void initRoutePlanNode() {
        startNode = new BNRoutePlanNode.Builder()
                .latitude(LATITUDE)
                .longitude(LONGITUDE)
                .name(NAME)
                .description(NAME)
                .build();
        endNode = new BNRoutePlanNode.Builder()
                .latitude(LATITUDE1)
                .longitude(LONGITUDE1)
                .name(NAME1)
                .description(NAME1)
                .build();
    }

    public BNRoutePlanNode getStartNode(Context context) {
        String start = BNDemoUtils.getString(context, "start_node");
        if (!TextUtils.isEmpty(start)) {
            String[] node = start.split(",");
            startNode = new BNRoutePlanNode.Builder()
                    .longitude(Double.parseDouble(node[0]))
                    .latitude(Double.parseDouble(node[1]))
                    .build();
        }
        return startNode;
    }

    public BNRoutePlanNode getNewNode(Context context) {
        String start = BNDemoUtils.getString(context, "start_node");
        if (!TextUtils.isEmpty(start)) {
            String[] node = start.split(",");
            startNode = new BNRoutePlanNode.Builder()
                    .longitude(Double.parseDouble(node[0]) + DOUBLE)
                    .latitude(Double.parseDouble(node[1]) + DOUBLE)
                    .build();
        }
        return startNode;
    }

    public void setStartNode(Context context, String value) {
        BNDemoUtils.setString(context, "start_node", value);
    }

    public BNRoutePlanNode getEndNode(Context context) {
        String end = BNDemoUtils.getString(context, "end_node");
        String uid = BNDemoUtils.getString(context, "end_uid");
        if (!TextUtils.isEmpty(end)) {
            String[] node = end.split(",");
            endNode = new BNRoutePlanNode.Builder()
                    .id(!TextUtils.isEmpty(uid) ? uid : "")
                    .longitude(Double.parseDouble(node[0]))
                    .latitude(Double.parseDouble(node[1]))
                    .build();
        }
        return endNode;
    }

    public void setEndNode(Context context, String value) {
        BNDemoUtils.setString(context, "end_node", value);
    }

    public void setEndUid(Context context, String value) {
        BNDemoUtils.setString(context, "end_uid", value);
    }

    public BNRoutePlanNode getCurrentNode(Context context, float d) {
        String end = BNDemoUtils.getString(context, "current_node");
        if (!TextUtils.isEmpty(end)) {
            String[] node = end.split(",");
            return new BNRoutePlanNode.Builder()
                    .longitude(Double.parseDouble(node[0]) + d)
                    .latitude(Double.parseDouble(node[1]) + d)
                    .build();
        }
        return getStartNode(context);
    }

    public void setTestEnvironmentUrl(Context context, String value) {
        BNDemoUtils.setString(context, "TestEnvironmentUrl", value);
    }

    public String getTestEnvironmentUrl(Context context) {
        return BNDemoUtils.getString(context, "TestEnvironmentUrl");
    }
}
