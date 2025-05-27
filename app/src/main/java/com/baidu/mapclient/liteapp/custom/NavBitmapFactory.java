package com.baidu.mapclient.liteapp.custom;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * @author hecq
 * @email 33912760@qq.com
 * create at 2025/5/27
 * description
 */
public class NavBitmapFactory {
    public static Bitmap mergeBitmap(Bitmap map, Bitmap navInfo, Bitmap bottomInfo) {
        if(map == null)return null;
        if(map.getWidth() == 0 || map.getHeight() == 0){
            return  null;
        }
        // 创建结果Bitmap（与底图同尺寸）
        Bitmap result = Bitmap.createBitmap(
                map.getWidth(),
                map.getHeight(),
                Bitmap.Config.ARGB_8888
        );

        Canvas canvas = new Canvas(result);

        // 1. 绘制底图
        canvas.drawBitmap(map, 0, 0, null);

        // 2. 顶部导航信息（水平居中）
        if (navInfo != null) {
            int navX = (map.getWidth() - navInfo.getWidth()) / 2;
            canvas.drawBitmap(navInfo, navX, 0, null);
        }

        // 3. 底部里程信息（水平居中）
        if (bottomInfo != null) {
            int bottomX = (map.getWidth() - bottomInfo.getWidth()) / 2;
            int bottomY = map.getHeight() - bottomInfo.getHeight();
            canvas.drawBitmap(bottomInfo, bottomX, bottomY, null);
        }

        return result;
    }
}
