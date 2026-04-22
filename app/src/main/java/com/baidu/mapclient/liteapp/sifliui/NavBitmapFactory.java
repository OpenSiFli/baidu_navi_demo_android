package com.baidu.mapclient.liteapp.sifliui;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
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

    public static Bitmap makeBitmap(Bitmap map, SFNavInfo navInfo, SFTopRightInfo topRightInfo,
                                    SFEnlargeMapInfo enlargeMapInfo, SFLineInfo lineInfo) {
        if (map == null || map.getWidth() == 0 || map.getHeight() == 0) {
            return null;
        }

        // 创建与底图同尺寸的结果Bitmap (RGB_565 节省内存)
        Bitmap result = Bitmap.createBitmap(map.getWidth(), map.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(result);

        // 1. 绘制底图
        canvas.drawBitmap(map, 0, 0, null);

        int marginLeft = 10;
        int marginTop = 10;
        int spacing = 5;
        int currentTop = marginTop;

        // 2. 绘制左上角导航信息
        if (navInfo != null && navInfo.isVisible()) {
            drawNavInfo(canvas, navInfo, marginLeft, currentTop);
            currentTop += navInfo.getHeight() + spacing;
        }

        // 3. 绘制右上角信息
        if (topRightInfo != null && topRightInfo.isVisible()) {
            drawTopRightInfo(canvas, topRightInfo, map.getWidth(), marginTop);
        }

        // 4. 绘制车道线信息 (位于导航信息下方)
        if (lineInfo != null && lineInfo.isVisible() && !lineInfo.getLineItems().isEmpty()) {
            drawLineInfo(canvas, lineInfo, marginLeft, currentTop);
            currentTop += lineInfo.getHeight() + spacing;
        }

        // 5. 绘制放大图 (位于车道线下方 或 导航信息下方)
        if (enlargeMapInfo != null && enlargeMapInfo.isVisible() && enlargeMapInfo.getEnlargeMap() != null) {
            drawEnlargeMap(canvas, enlargeMapInfo, marginLeft, currentTop);
        }

        return result;
    }

    private static void drawNavInfo(Canvas canvas, SFNavInfo navInfo, int left, int top) {
        int width = navInfo.getWidth();
        int height = navInfo.getHeight();
        int radius = navInfo.getRadius();

        // 绘制圆角矩形背景 (#212121)
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.parseColor("#212121"));
        RectF bgRect = new RectF(left, top, left + width, top + height);
        canvas.drawRoundRect(bgRect, radius, radius, bgPaint);

        int padding = 5;
        int iconTargetSize = 40;
        int textStartX = left + padding + iconTargetSize + padding;

        // 绘制缩放后的图标
        Bitmap scaledIcon = navInfo.getScaledIcon(iconTargetSize);
        if (scaledIcon != null && !scaledIcon.isRecycled()) {
            int iconLeft = left + padding;
            int iconTop = top + (height - scaledIcon.getHeight()) / 2;
            canvas.drawBitmap(scaledIcon, iconLeft, iconTop, null);
        }

        // 绘制文本（两排）
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(convertSpToPx(8));

        // 计算每行文本的高度
        float textHeight = textPaint.getTextSize();
        float lineSpacing = 2; // 两行之间的间距
        float totalTextHeight = textHeight * 2 + lineSpacing;

        // 计算文本起始Y坐标（垂直居中）
        float textStartY = top + (height - totalTextHeight) / 2 + textHeight;

        // 第一排：distanceText
        String distanceText = navInfo.getDistanceText();
        if (distanceText != null && !distanceText.isEmpty()) {
            canvas.drawText(distanceText, textStartX, textStartY, textPaint);
        }

        // 第二排：roadNameText
        String roadNameText = navInfo.getRoadNameText();
        if (roadNameText != null && !roadNameText.isEmpty()) {
            canvas.drawText(roadNameText, textStartX, textStartY + textHeight + lineSpacing, textPaint);
        }
    }

    private static void drawTopRightInfo(Canvas canvas, SFTopRightInfo topRightInfo, int mapWidth, int top) {
        int width = topRightInfo.getWidth();
        int height = topRightInfo.getHeight();
        int radius = topRightInfo.getRadius();

        int right = mapWidth - 10; // 右边距10
        int left = right - width;

        // 绘制圆角矩形背景
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.parseColor("#212121"));
        RectF bgRect = new RectF(left, top, left + width, top + height);
        canvas.drawRoundRect(bgRect, radius, radius, bgPaint);

        int padding = 5;
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(convertSpToPx(8));

        String remainInfo = topRightInfo.getRemainInfo();
        String arriveInfo = topRightInfo.getArriveInfo();

        int textLeft = left + padding;
        int textTop = top + padding + (int) textPaint.getTextSize();

        if (remainInfo != null && !remainInfo.isEmpty()) {
            canvas.drawText(remainInfo, textLeft, textTop, textPaint);
            textTop += textPaint.getTextSize() + 2; // 两行间距
        }
        if (arriveInfo != null && !arriveInfo.isEmpty()) {
            canvas.drawText(arriveInfo, textLeft, textTop, textPaint);
        }
    }

    private static void drawLineInfo(Canvas canvas, SFLineInfo lineInfo, int left, int top) {
//        int width = lineInfo.getWidth();
//        int height = lineInfo.getHeight();
//        int radius = lineInfo.getRadius();

        // 绘制背景 (iOS UIColor(0,0.48,1,1) -> #007AFF)
//        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        bgPaint.setColor(Color.parseColor("#007AFF"));
//        RectF bgRect = new RectF(left, top, left + width, top + height);
//        canvas.drawRoundRect(bgRect, radius, radius, bgPaint);

        int padding = 5;
//        int itemSize = 32;
//        int itemSpacing = 5;
        int currentX = left + padding;

        Bitmap lineBitmap = lineInfo.getCachedBitmap();
        if(lineBitmap != null){
            canvas.drawBitmap(lineBitmap,currentX,top,null);
        }


//        for (Drawable drawable : lineInfo.getLineItems()) {
//            if (drawable == null) continue;
//            // 限制绘制区域不超出背景
//            if (currentX + itemSize > left + width - padding) break;
//
//            drawable.setBounds(currentX, top + (height - itemSize) / 2,
//                    currentX + itemSize, top + (height + itemSize) / 2);
//            drawable.draw(canvas);
//            currentX += itemSize + itemSpacing;
//        }
    }

    private static void drawEnlargeMap(Canvas canvas, SFEnlargeMapInfo enlargeMapInfo, int left, int top) {
        Bitmap enlargeMap = enlargeMapInfo.getEnlargeMap();
        if (enlargeMap == null || enlargeMap.isRecycled()) return;

        // 按原始尺寸绘制，左边距10
        canvas.drawBitmap(enlargeMap, left, top, null);
    }

    // 辅助方法：将sp转换为px (假设context可用，或使用默认密度)
    private static int convertSpToPx(int sp) {
        float density = Resources.getSystem().getDisplayMetrics().scaledDensity;
        return (int) (sp * density + 0.5f);
    }
}
