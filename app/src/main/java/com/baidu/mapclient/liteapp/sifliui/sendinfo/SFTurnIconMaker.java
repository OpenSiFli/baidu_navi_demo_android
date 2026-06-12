package com.baidu.mapclient.liteapp.sifliui.sendinfo;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

public class SFTurnIconMaker {

    public static byte[] makeTurnIconEZip(Bitmap turnIcon){
        byte[] pngData = getPngData(turnIcon);
        if(pngData == null)return null;
        return com.sifli.ezip.sifliEzipUtil.pngToEzip(pngData,"rgb565",0,1,2);
    }

    private static byte[] getPngData(Bitmap turnIcon) {
        if (turnIcon == null) {
            return null;
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        // 将Bitmap压缩为PNG格式，质量参数对于PNG无损格式无效，可设置为任意值（如100）
        turnIcon.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
}
