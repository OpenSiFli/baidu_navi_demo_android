package com.baidu.mapclient.liteapp.util;

import android.content.Context;

import java.io.File;

/**
 * @author hecq
 * @email 33912760@qq.com
 * create at 2023/9/14
 * description
 */
public class FolderHelper {
    public static String getDeviceLogPath(Context context){
        String root = context.getExternalFilesDir(null) + "/DeviceLogs";
        File file = new File(root);
        file.mkdirs();
        return root;
    }

    public static String getDeviceAssetPath(Context context){
        String root = context.getExternalFilesDir(null) + "/DeviceAssets";
        File file = new File(root);
        file.mkdirs();
        return root;
    }

    public static String getDeviceMetricsPath(Context context){
        String root = context.getExternalFilesDir(null) + "/DeviceMetrics";
        File file = new File(root);
        file.mkdirs();
        return root;
    }

    public static String getDeviceHCIPath(Context context){
        String root = context.getExternalFilesDir(null) + "/DeviceHCI";
        File file = new File(root);
        file.mkdirs();
        return root;
    }

    public static String getDeviceAudioDumpPath(Context context){
        String root = context.getExternalFilesDir(null) + "/DeviceAudioDump";
        File file = new File(root);
        file.mkdirs();
        return root;
    }
    public static String getDeviceWatchfacePath(Context context){
        String root = context.getExternalFilesDir(null) + "/Watchface";
        File file = new File(root);
        file.mkdirs();
        return root;
    }
    public static String getDeviceOTAPath(Context context){
        String root = context.getExternalFilesDir(null) + "/Ota";
        File file = new File(root);
        file.mkdirs();
        return root;
    }

    public static String getDeviceMutilWatchfacePath(Context context){
        String root = context.getExternalFilesDir(null) + "/MutilWatchface";
        File file = new File(root);
        file.mkdirs();
        return root;
    }

    public static String getPreviewNavPath(Context context){
        String root = context.getExternalFilesDir(null) + "/preview_nav";
        File file = new File(root);
        file.mkdirs();
        return root;
    }

    public static String getPreviewNavConvertPath(Context context){
        String root = context.getExternalFilesDir(null) + "/preview_nav_convert";
        File file = new File(root);
        file.mkdirs();
        return root;
    }

    public static String getEZipPngDir(Context context){
        String root = context.getExternalFilesDir(null) + "/ezip_png";
        File file = new File(root);
        file.mkdirs();
        return root;
    }

    public static String getEZipBinDir(Context context){
        String root = context.getExternalFilesDir(null) + "/ezip_bin";
        File file = new File(root);
        file.mkdirs();
        return root;
    }

    public static String getTempPath(Context context){
        String root = context.getExternalFilesDir(null) + "/temp";
        File file = new File(root);
        file.mkdirs();
        return root;
    }
}
