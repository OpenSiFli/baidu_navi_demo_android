package com.baidu.mapclient.liteapp.sifliui.sendinfo;

/**
 * 发送信息模式时，需要发送给车机的信息
 * */
public class SFPreviewNavInfo {
    private String roadName;
    /** 单位 米，距离多少米进入roadName*/
    private int distance;
    /**转向图标名称*/
    private String turnIconName;
    /**速度 公里/小时*/
    private float speed;
    /**距离终点剩余时间 单位 秒*/
    private long remainTime;
    /**距离终点剩余距离 单位 米*/
    private long remainDistance;
    /**剩余红绿灯个数*/
    private int remainLights;
    private boolean turnIconChanged;

    public String getRoadName() {
        return roadName;
    }

    public void setRoadName(String roadName) {
        this.roadName = roadName;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public String getTurnIconName() {
        return turnIconName;
    }

    public void setTurnIconName(String turnIconName) {
        this.turnIconName = turnIconName;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public long getRemainTime() {
        return remainTime;
    }

    public void setRemainTime(long remainTime) {
        this.remainTime = remainTime;
    }

    public long getRemainDistance() {
        return remainDistance;
    }

    public void setRemainDistance(long remainDistance) {
        this.remainDistance = remainDistance;
    }

    public int getRemainLights() {
        return remainLights;
    }

    public void setRemainLights(int remainLights) {
        this.remainLights = remainLights;
    }

    public boolean isTurnIconChanged() {
        return turnIconChanged;
    }

    public void setTurnIconChanged(boolean turnIconChanged) {
        this.turnIconChanged = turnIconChanged;
    }
}
