package com.baidu.mapclient.liteapp.log;

import com.baidu.mapclient.liteapp.BuildConfig;
import com.elvishew.xlog.XLog;
import com.sifli.siflicore.log.SFLog;
import com.sifli.siflicore.log.SFLogLevel;
import com.sifli.siflicore.log.SFLogManager;

import java.lang.ref.WeakReference;

/**
 * @author hecq
 * @email 33912760@qq.com
 * create at 2025/5/6
 * description
 */
public class SFLogReceiver implements SFLogManager.SFLogManagerCallback {
    private final String TAG = "SFLogReceiver";
    private static SFLogReceiver _instance;
    private WeakReference<SFLogReceiverCallback> weakCallback;
    public SFLogReceiver(){

    }
    public static SFLogReceiver getInstance(){
        if(_instance == null){
            _instance = new SFLogReceiver();
        }
        return _instance;
    }

    public  void setCallback(SFLogReceiverCallback callback){
        this.weakCallback = new WeakReference<>(callback);
    }

    public SFLogReceiverCallback getCallback(){
        if(this.weakCallback == null){
            return null;
        }
        return this.weakCallback.get();
    }

    public  void startReceiveLog(){
        SFLog.i(TAG,"startReceiveLog DEBUG=%s", BuildConfig.DEBUG);
        if (BuildConfig.DEBUG) {
            SFLogManager.getInstance().setLogEnable(true);
        } else {
            SFLogManager.getInstance().setLogEnable(false);
        }

        SFLogManager.getInstance().setCallback(this);
    }

    @Override
    public void onLog(int level, String log) {
        SFLogReceiverCallback callback = this.getCallback();
        if(callback != null){
            callback.onLog(level,log);
        }
//        XLog.i(log);
        if(level == SFLogLevel.INFO){
            XLog.i(log);
        }else if(level == SFLogLevel.DEBUG){
            XLog.d(log);
        }else if(level == SFLogLevel.WARNING){
            XLog.w(log);
        }else if(level == SFLogLevel.ERROR){
            XLog.e(log);
        }else{
            XLog.d(log);
        }
    }
}
