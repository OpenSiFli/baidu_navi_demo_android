package com.baidu.mapclient.liteapp.tts;

import android.media.MediaPlayer;
import java.io.File;
import java.io.IOException;

public class SFPlayer {
    private MediaPlayer mediaPlayer;
    private boolean isPrepared = false;

    public void play(File file) {
        try {
            if (mediaPlayer != null) {
                releasePlayer();
            }

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(file.getAbsolutePath());
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(mp -> {
                isPrepared = true;
                mp.start();
            });

            mediaPlayer.setOnCompletionListener(mp -> releasePlayer());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        if (isPrepared && mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void resume() {
        if (isPrepared && mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            releasePlayer();
        }
    }

    private void releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            isPrepared = false;
        }
    }

    public int getDuration() {
        return isPrepared ? mediaPlayer.getDuration() : 0;
    }

    public int getCurrentPosition() {
        return isPrepared ? mediaPlayer.getCurrentPosition() : 0;
    }
}