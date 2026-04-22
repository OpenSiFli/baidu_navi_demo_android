package com.baidu.mapclient.liteapp.sifliui;

import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

public class SFLineInfo extends SFNavObject{
    private final ArrayList<Drawable> mLaneItems = new ArrayList<>();

    public SFLineInfo(int width,int height,int radius){
        super(width,height,radius);
    }
    public List<Drawable> getLineItems(){
        return this.mLaneItems;
    }
    public void clear(){
        this.mLaneItems.clear();
    }

    public void addLine(Drawable drawable){
        this.mLaneItems.add(drawable);
    }
}
