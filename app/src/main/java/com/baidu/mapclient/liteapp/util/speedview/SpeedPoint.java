package com.baidu.mapclient.liteapp.util.speedview;

/**
 * @author hecq
 * @email 33912760@qq.com
 * create at 2024/1/25
 * description 速度统计的记录点
 */
class SpeedPoint {
    private final long timestamp;
    private final long completeBytes;

    public SpeedPoint(long timestamp, long completeBytes) {
        this.timestamp = timestamp;
        this.completeBytes = completeBytes;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getCompleteBytes() {
        return completeBytes;
    }
}
