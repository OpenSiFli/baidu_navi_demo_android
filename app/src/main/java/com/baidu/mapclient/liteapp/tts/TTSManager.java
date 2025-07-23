package com.baidu.mapclient.liteapp.tts;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.sifli.siflicore.log.SFLog;

import java.io.File;
import java.util.Locale;
import java.util.UUID;

/**
 * @author hecq
 * @email 33912760@qq.com
 * create at 2025/7/21
 * description
 */
public class TTSManager implements TextToSpeech.OnInitListener {
    private final static String TAG = TTSManager.class.getSimpleName();
    private TextToSpeech textToSpeech;
    private Context context;
    private TTSListener listener;
    private File outputDir;

    public interface TTSListener {
        void onTTSAudioGenerated(File audioFile);
        void onTTSSpeakDone();
        void onTTSError(String error);
    }

    public TTSManager(Context context, File outputDir, TTSListener listener) {
        this.context = context;
        this.outputDir = outputDir;
        this.listener = listener;
        this.textToSpeech = new TextToSpeech(context, this);
    }

    @Override
    public void onInit(int status) {
        SFLog.i(TAG,"onInit status = %d",status);
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.CHINA);
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                SFLog.i(TAG,"Language not supported");
                notifyError("Language not supported");
            }else{
                SFLog.i(TAG,"TTS initialization success");
            }
        } else {

            notifyError("TTS initialization failed");
        }
    }

    public void generateAudio(String text,File outputFile) {
        SFLog.i(TAG,"generateAudio %s",text);
        String utteranceId = UUID.randomUUID().toString();
//        File outputFile = new File(outputDir, utteranceId + ".wav");

        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override public void onStart(String utteranceId) {}
            @Override public void onDone(String utteranceId) {
                notifySuccess(outputFile);
            }
            @Override public void onError(String utteranceId) {
                notifyError("TTS synthesis failed");
            }
        });
        textToSpeech.setSpeechRate(0.8f);
        int result = textToSpeech.synthesizeToFile(text, null, outputFile, utteranceId);
        if (result != TextToSpeech.SUCCESS) {
            notifyError("Failed to generate audio");
        }
    }

    public void speak(String text) {
        SFLog.i(TAG,"speak %s",text);
        String utteranceId = UUID.randomUUID().toString();
        Bundle params = new Bundle();
        // 修改为使用音乐媒体流（通过扬声器播放）
        params.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM,
                AudioManager.STREAM_MUSIC);

        // 确保音频焦点和扬声器模式
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_NORMAL);
        audioManager.setSpeakerphoneOn(true);

        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override public void onStart(String utteranceId) {}
            @Override public void onDone(String utteranceId) {
                notifySpeakDone();
            }
            @Override public void onError(String utteranceId) {
                notifyError("TTS synthesis failed");
            }
        });
        textToSpeech.setSpeechRate(0.8f);
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId);
    }

    private void notifySuccess(File file) {
        if (listener != null) listener.onTTSAudioGenerated(file);
    }

    private void notifyError(String error) {
        if (listener != null) listener.onTTSError(error);
    }
    private void notifySpeakDone(){
        if(listener != null)listener.onTTSSpeakDone();
    }

    public void release() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}