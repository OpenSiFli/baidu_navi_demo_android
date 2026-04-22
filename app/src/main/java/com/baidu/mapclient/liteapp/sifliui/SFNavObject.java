package com.baidu.mapclient.liteapp.sifliui;

import com.baidu.mapapi.http.wrapper.annotation.PUT;

public class SFNavObject {
    private int width;
    private int height;
    private int radius;
    private boolean isVisible;

    public SFNavObject(){

    }

    public  SFNavObject(int width,int height,int radius){
        this.width = width;
        this.height = height;
        this.radius = radius;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }
}
