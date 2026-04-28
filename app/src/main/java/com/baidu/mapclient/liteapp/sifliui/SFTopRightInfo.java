package com.baidu.mapclient.liteapp.sifliui;

public class SFTopRightInfo extends SFNavObject{
    private String remainInfo;
    private String arriveInfo;

    //以下输出到导航信息
    private long remainTime;
    private long remainDistance;
    private int remainLights;

    public SFTopRightInfo(int width,int height,int radius){
        super(width,height,radius);
    }

    public String getRemainInfo() {
        return remainInfo;
    }

    public void setRemainInfo(String remainInfo) {
        this.remainInfo = remainInfo;
    }

    public String getArriveInfo() {
        return arriveInfo;
    }

    public void setArriveInfo(String arriveInfo) {
        this.arriveInfo = arriveInfo;
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
}
