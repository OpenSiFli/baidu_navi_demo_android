package com.baidu.mapclient.liteapp.sifliui;

public class SFTopRightInfo extends SFNavObject{
    private String remainInfo;
    private String arriveInfo;

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
}
