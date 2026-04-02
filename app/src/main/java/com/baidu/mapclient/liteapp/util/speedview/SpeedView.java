package com.baidu.mapclient.liteapp.util.speedview;
import java.util.LinkedList;
import java.util.Queue;
/**
 * @author hecq
 * @email 33912760@qq.com
 * create at 2024/1/25 20250614 优化为时间窗口方式
 * description
 */
public class SpeedView {
    private static final int CURRENT_WINDOW_MS = 3000; // 3秒窗口
    private static final int AVERAGE_WINDOW_MS = 60000; // 60秒窗口

    private final Queue<SpeedPoint> currentWindow = new LinkedList<>();
    private final Queue<SpeedPoint> averageWindow = new LinkedList<>();

    private double currentSpeed;
    private double averageSpeed;

    public void viewSpeedByCompleteBytes(long completeBytes) {
        long now = timeNow();
        SpeedPoint newPoint = new SpeedPoint(now, completeBytes);

        // 更新3秒窗口(瞬时速度)
        updateWindow(currentWindow, newPoint, CURRENT_WINDOW_MS);
        currentSpeed = calculateSpeed(currentWindow);

        // 更新60秒窗口(平均速度)
        updateWindow(averageWindow, newPoint, AVERAGE_WINDOW_MS);
        averageSpeed = calculateSpeed(averageWindow);
    }

    private void updateWindow(Queue<SpeedPoint> window, SpeedPoint newPoint, long windowSize) {
        window.offer(newPoint);

        // 移除超出时间窗口的点
        while (!window.isEmpty() &&
                (newPoint.getTimestamp() - window.peek().getTimestamp()) > windowSize) {
            window.poll();
        }
    }

    private double calculateSpeed(Queue<SpeedPoint> window) {
        if (window.size() < 2) return 0;

        SpeedPoint first = window.peek();
        SpeedPoint last = null;
        for (SpeedPoint point : window) {
            last = point;
        }

        if (first == null || last == null || first == last) return 0;

        long timeDiff = last.getTimestamp() - first.getTimestamp();
        long bytesDiff = last.getCompleteBytes() - first.getCompleteBytes();

        return getKbPerSec(bytesDiff, timeDiff);
    }

    public void clear() {
        currentWindow.clear();
        averageWindow.clear();
        currentSpeed = 0;
        averageSpeed = 0;
    }

    public String getSpeedText() {
        return String.format("瞬时 %.1f KB/s 平均 %.1f KB/s", currentSpeed, averageSpeed);
    }

    public String getCurrentSpeedText() {
        return String.format("%.1f KB/s", currentSpeed);
    }

    public String getSpeedText(long currentBytes, long totalBytes) {
        double currentM = (double) currentBytes / (1024 * 1024);
        double totalM = (double) totalBytes / (1024 * 1024);
        return String.format("瞬时 %.1f KB/s 平均 %.1f KB/s %.1f/%.1fMB",
                currentSpeed, averageSpeed, currentM, totalM);
    }

    private double getKbPerSec(long bytes, long ms) {
        if (ms == 0) return 0;
        return ((double) bytes / 1024) / ((double) ms / 1000);
    }

    private long timeNow() {
        return System.currentTimeMillis();
    }

    public double getCurrentSpeed() {
        return currentSpeed;
    }

    public double getAverageSpeed() {
        return averageSpeed;
    }
}
