package com.baidu.mapclient.liteapp.sifliui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

public class SFLineInfo extends SFNavObject {
    private final ArrayList<Drawable> mLaneItems = new ArrayList<>();
    private Bitmap cachedLineBitmap; // 缓存车道线组合图

    public SFLineInfo(int width,int height,int radius){
        super(width,height,radius);
    }

    public List<Drawable> getLineItems() {
        return this.mLaneItems;
    }

    public void clear() {
        this.mLaneItems.clear();
        clearCache();
    }

    public void addLine(Drawable drawable) {
        this.mLaneItems.add(drawable);
        clearCache(); // 数据变化时清除缓存
    }

    private void clearCache() {
        if (cachedLineBitmap != null && !cachedLineBitmap.isRecycled()) {
            cachedLineBitmap.recycle();
            cachedLineBitmap = null;
        }
    }

    /**
     * 获取车道线组合的Bitmap（线程安全，可在子线程调用）
     * 注意：此方法只读取缓存，不会重新生成Bitmap
     * 重新生成Bitmap必须在主线程通过 regenerateBitmap() 完成
     */
    public Bitmap getCachedBitmap() {
        return cachedLineBitmap;
    }

    /**
     * 重新生成车道线Bitmap（必须在主线程调用）
     */
    public void regenerateBitmap() {
        int width = getWidth();
        int height = getHeight();
        int radius = getRadius();

        if (width <= 0 || height <= 0 || mLaneItems.isEmpty()) {
            return;
        }

        // 创建Bitmap
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // 绘制背景
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.parseColor("#007AFF"));
        RectF bgRect = new RectF(0, 0, width, height);
        canvas.drawRoundRect(bgRect, radius, radius, bgPaint);

        // 绘制车道线图标
        int padding = 5;
        int itemSize = 32;
        int itemSpacing = 5;
        int currentX = padding;

        for (Drawable drawable : mLaneItems) {
            if (drawable == null) continue;
            if (currentX + itemSize > width - padding) break;

            int top = (height - itemSize) / 2;
            drawable.setBounds(currentX, top, currentX + itemSize, top + itemSize);
            drawable.draw(canvas);
            currentX += itemSize + itemSpacing;
        }

        // 更新缓存
        if (cachedLineBitmap != null && !cachedLineBitmap.isRecycled()) {
            cachedLineBitmap.recycle();
        }
        cachedLineBitmap = bitmap;
    }

    public void release() {
        clearCache();
    }
}
