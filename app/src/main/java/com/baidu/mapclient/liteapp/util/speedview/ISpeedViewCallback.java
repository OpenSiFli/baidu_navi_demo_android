package com.baidu.mapclient.liteapp.util.speedview;

/**
 * @author hecq
 * @email 33912760@qq.com
 * create at 2024/1/25
 * description
 */
public interface ISpeedViewCallback {
    /**
     * 传输速度更新 kb/s
     * */
    void onSpeed(float currentSpeed,float averageSpeed);
}
