package com.baidu.mapclient.liteapp.sifliui;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;

public class SFNavInfo extends  SFNavObject{
    private Bitmap turnIcon;
//    private String guideInfoText;
    private String distanceText;
    private String roadNameText;
    private Bitmap cachedScaledIcon; // 缓存缩放后的图标

    public SFNavInfo(int width,int height,int radius){
        super(width,height,radius);
    }

    public Bitmap getTurnIcon() {
        return turnIcon;
    }

    public void setTurnIcon(Bitmap turnIcon) {
        this.turnIcon = turnIcon;
        this.cachedScaledIcon = null; // 清除缓存
    }

    public Bitmap getScaledIcon(int targetSize) {
        if (cachedScaledIcon != null && !cachedScaledIcon.isRecycled()) {
            return cachedScaledIcon;
        }

        if (turnIcon == null || turnIcon.isRecycled()) {
            return null;
        }

        // 计算缩放比例
        int iconWidth = turnIcon.getWidth();
        int iconHeight = turnIcon.getHeight();
        float scale = Math.min((float) targetSize / iconWidth, (float) targetSize / iconHeight);
        int scaledWidth = (int) (iconWidth * scale);
        int scaledHeight = (int) (iconHeight * scale);

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        cachedScaledIcon = Bitmap.createBitmap(turnIcon, 0, 0, iconWidth, iconHeight, matrix, true);

        return cachedScaledIcon;
    }

    public String getDistanceText() {
        return distanceText;
    }

    public void setDistanceText(String distanceText) {
        this.distanceText = distanceText;
    }

    public String getRoadNameText() {
        return roadNameText;
    }

    public void setRoadNameText(String roadNameText) {
        this.roadNameText = roadNameText;
    }
}

