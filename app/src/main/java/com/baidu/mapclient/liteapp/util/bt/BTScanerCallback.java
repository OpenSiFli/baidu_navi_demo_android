package com.baidu.mapclient.liteapp.util.bt;

import android.bluetooth.BluetoothDevice;

/**
 * @author hecq
 * @email 33912760@qq.com
 * create at 2023/9/4
 * description
 */
public interface BTScanerCallback {
    /**
     * 发现目标设备
     * */
    void onTargetDeviceFound(BluetoothDevice device);
    /**
     * 超时
     * */
    void onTimeout();
}
