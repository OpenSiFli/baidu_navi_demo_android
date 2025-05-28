package com.baidu.mapclient.liteapp.custom;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;

/**
 * @author hecq
 * @email 33912760@qq.com
 * create at 2025/5/27
 * description
 */
public class NavBitmapFactory {
    private final static String TAG = "NavBitmapFactory";
    public static Bitmap mergeBitmap(Bitmap map, Bitmap navInfo, Bitmap topRightInfo,Bitmap enlargeMap,Bitmap lineList) {
        if(map == null)return null;
        if(map.getWidth() == 0 || map.getHeight() == 0){
            return  null;
        }
        // 创建结果Bitmap（与底图同尺寸）
        Bitmap result = Bitmap.createBitmap(
                map.getWidth(),
                map.getHeight(),
                Bitmap.Config.RGB_565
        );

        Canvas canvas = new Canvas(result);

        // 1. 绘制底图
        canvas.drawBitmap(map, 0, 0, null);

        // 2. 顶部居左
        int lineListTop = 0;
        int navInfoTop = 10;
        int marginLeft = 15;
        if (navInfo != null) {

            canvas.drawBitmap(navInfo, marginLeft, navInfoTop, null);
            lineListTop = navInfoTop + navInfo.getHeight() + 5;
        }

        // 3.右上信息

        if (topRightInfo != null) {
            int topRightX = map.getWidth() - topRightInfo.getWidth() -marginLeft;
            Log.i(TAG,"topRight x=" + topRightX + ",mapWdith=" + map.getWidth() + ",topRightWidth=" + topRightInfo.getWidth());
            int topRightY = navInfoTop;
            canvas.drawBitmap(topRightInfo, topRightX, topRightY, null);

        }
        int enlargeTop = lineListTop;
        if(lineList != null){
            canvas.drawBitmap(lineList, marginLeft, lineListTop, null);
            enlargeTop += lineList.getHeight();
        }

        if(enlargeMap != null){
            canvas.drawBitmap(enlargeMap, marginLeft, enlargeTop, null);
        }



        return result;
    }
}
