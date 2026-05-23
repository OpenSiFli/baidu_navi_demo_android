package com.baidu.mapclient.liteapp.config;

public class SFNaviOption {
    public final static int NAV_MODE_IMAGE = 1;
    public final static int NAV_MODE_INFO = 2;

//    private boolean useSocket;
    private int maxFPS;
    private float jpegQuality;
    private int width;
    private int height;
    private int navMode;
    private int transMode;
    private String serverIP;
    private int serverPort;


    private  static SFNaviOption _instance;
    public static SFNaviOption getInstance(){
        if(_instance == null){
            _instance = new SFNaviOption();
        }
        return _instance;
    }

//    public boolean isUseSocket() {
//        return useSocket;
//    }
//
//    public void setUseSocket(boolean useSocket) {
//        this.useSocket = useSocket;
//    }

    public int getMaxFPS() {
        return maxFPS;
    }

    public void setMaxFPS(int maxFPS) {
        this.maxFPS = maxFPS;
    }

    public float getJpegQuality() {
        return jpegQuality;
    }

    public void setJpegQuality(float jpegQuality) {
        this.jpegQuality = jpegQuality;
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

    public int getNavMode() {
        return navMode;
    }

    public void setNavMode(int navMode) {
        this.navMode = navMode;
    }

    public int getTransMode() {
        return transMode;
    }

    public void setTransMode(int transMode) {
        this.transMode = transMode;
    }

    public String getServerIP() {
        return serverIP;
    }

    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }
}
