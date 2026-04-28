package com.baidu.mapclient.liteapp.sifliui.sendinfo;

import com.sifli.siflicore.util.ByteUtil;

/**
 * 地图导航信息模式时需要发送的数据内容包
 * 包含导航信息，以及转向图的ezip数据，将来可能还会加入放大图，车道图
 * */
public class SFPreviewNavContent {
    private byte[] navInfo;
    private byte[] turnIconEZip;

    public SFPreviewNavContent(byte[] navInfo,byte[] turnIconEZip){
        this.navInfo = navInfo;
        this.turnIconEZip = turnIconEZip;
    }

    public byte[] getNavInfo(){
        return this.navInfo;
    }

    public byte[] getTurnIconEZip(){
        return this.turnIconEZip;
    }

    public byte[] mashal(){
        if(this.navInfo == null || this.turnIconEZip == null){
            return null;
        }
        int navInfoLen = this.navInfo.length;
        byte[] navInfoLenData = ByteUtil.intTo4Bytes(navInfoLen);
        int turnIconEzipLen = this.turnIconEZip.length;
        byte[] turnIconEzipLenData = ByteUtil.intTo4Bytes(turnIconEzipLen);
        byte[] result = ByteUtil.mergeArrays(navInfoLenData,navInfo);
        result = ByteUtil.mergeArrays(result,turnIconEzipLenData);
        result = ByteUtil.mergeArrays(result,turnIconEZip);
        return result;
    }
}

