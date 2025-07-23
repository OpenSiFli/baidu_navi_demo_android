package com.baidu.mapclient.liteapp.tts;

import android.content.Context;
import android.media.MediaMetadataRetriever;

import com.sifli.siflicore.log.SFLog;
import com.sifli.siflicore.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author hecq
 * @email 33912760@qq.com
 * create at 2025/7/21
 * description
 */
public class TTSHolder implements TTSManager.TTSListener,MP3Encoder.EncodeListener{
    private final static String TAG = TTSHolder.class.getSimpleName();
    private TTSManager manager;
    private MP3Encoder mp3Encoder;
    private static TTSHolder instance;
    private byte[] mp3Data;
    /**是否手机播报*/
    private boolean playOnPhone = false;
    private File outDir = null;
    private long lastFetchTime;
    private long lastAudioDuration;
    private int ttsState;
    private SFPlayer player;
    private File currentMp3File;
    private File currentWavFile;

    public void init(Context context){
        this.outDir = new File(getDeviceTTSPath(context));
        this.manager = new TTSManager(context,outDir,this);
        this.mp3Encoder = new MP3Encoder();
        this.player = new SFPlayer();
    }

    public static TTSHolder getInstance(){
        if(instance == null){
            instance = new TTSHolder();
        }
        return instance;
    }

    private void setTtsState(int state){
        this.ttsState = state;
        SFLog.i(TAG,"setTtsState %d",state);
    }

    public byte[] getMp3Data() {
        if(mp3Data != null){
            lastFetchTime = getTimeStamp();
        }
        byte[] data = mp3Data;
        mp3Data = null;
        return data;
    }

//    public void setMp3Data(byte[] mp3Data) {
//        this.mp3Data = mp3Data;
//    }

    public boolean isPlayOnPhone() {
        return playOnPhone;
    }

    public void setPlayOnPhone(boolean playOnPhone) {
        this.playOnPhone = playOnPhone;
    }

    /**
     * 是否空闲
     * */
    public boolean isIdle(){
        return (this.ttsState == TTSHolderState.NONE || this.ttsState == TTSHolderState.TTS_DONE) &&
                !this.isClientPlaying();

    }

    private boolean isClientPlaying(){
        long timeNow = this.getTimeStamp();
        boolean result = timeNow - this.lastFetchTime <= this.lastAudioDuration;
        SFLog.i(TAG,"isClientPlaying %b,timeNow=%d,lastFetch=%d,lastDuration=%d",result,timeNow,lastFetchTime,lastAudioDuration);
        return  result;
    }

    public void handleNaviTTSText(String text){
        SFLog.i(TAG,"handleNaviTTSText playOnPhone=%b",this.playOnPhone);
        setTtsState(TTSHolderState.TTS_SPEAKING);
        this.makeAudioPath();
        if(this.playOnPhone){
            this.manager.speak(text);
        }else{
            this.manager.generateAudio(text,this.currentWavFile);
        }
    }

    // region MP3Encoder.EncodeListener
    @Override
    public void onMp3Success(File mp3File) {
        SFLog.i(TAG,"onMp3Success");
        this.mp3Data = FileUtil.getFileData(mp3File.getPath());
        if(mp3Data != null) SFLog.i(TAG,"onMp3Success file size %d,duration %.1fs",this.mp3Data.length,this.lastAudioDuration /1000.0);
//        this.player.play(mp3File);
        setTtsState(TTSHolderState.TTS_DONE);
    }

    @Override
    public void onMp3Progress(int progress) {

    }

    @Override
    public void onMp3Error(String error) {
        SFLog.i(TAG,"onMp3Error %s",error);
        setTtsState(TTSHolderState.TTS_DONE);
    }
    //endregion

    //region TTSManager.TTSListener
    @Override
    public void onTTSAudioGenerated(File audioFile) {
        SFLog.i(TAG,"onTTSAudioGenerated %s",audioFile);
        setTtsState(TTSHolderState.TTS_MP3_ENCODING);
//        String mp3Path = makeMp3Path();
//        File mp3File = new File(mp3Path);
        try{
            this.lastAudioDuration = this.getAudioDuration(audioFile);
            SFLog.i(TAG,"lastAudioDuration =%d",this.lastAudioDuration);
        }catch (Exception ex){
            ex.printStackTrace();
        }
//        this.player.play(audioFile);
        this.mp3Encoder.encodeToMp3(audioFile,this.currentMp3File,this);
    }

    @Override
    public void onTTSSpeakDone() {
        SFLog.i(TAG,"onTTSSpeakDone");
        setTtsState(TTSHolderState.TTS_DONE);
    }

    @Override
    public void onTTSError(String error) {
        SFLog.i(TAG,"onTTSError %s",error);
    }
    //endregion

    //region Private
    private   String getDeviceTTSPath(Context context){
        String root = context.getExternalFilesDir(null) + "/tts_cache";
        File file = new File(root);
        file.mkdirs();
        return root;
    }

    private void makeAudioPath() {
        this.currentMp3File = null;
        this.currentWavFile = null;
        if (this.outDir != null) {
            // 使用时间戳作为文件名
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String mp3FileName = "TTS_" + timeStamp + ".mp3";
            String wavFileName = "TTS_" + timeStamp + ".wav";
            this.currentMp3File = new File(outDir, mp3FileName);
            this.currentWavFile = new File(outDir, wavFileName);
        }

    }

    private long getTimeStamp() {
        return System.currentTimeMillis() ;
    }

    private long getAudioDuration(File audioFile) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(audioFile.getAbsolutePath());
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            return Long.parseLong(durationStr); // 返回毫秒单位的时长
        } catch (Exception e) {
            e.printStackTrace();
            return -1; // 获取失败返回-1
        } finally {
            retriever.release();
        }
    }

    //endregion
}
