package com.baidu.mapclient.liteapp.config;

public class SFNaviOption {

    private boolean useSocket;


    private  static SFNaviOption _instance;
    public static SFNaviOption getInstance(){
        if(_instance == null){
            _instance = new SFNaviOption();
        }
        return _instance;
    }

    public boolean isUseSocket() {
        return useSocket;
    }

    public void setUseSocket(boolean useSocket) {
        this.useSocket = useSocket;
    }
}
