package com.baidu.mapclient.liteapp.tts;

import com.naman14.androidlame.AndroidLame;
import com.naman14.androidlame.LameBuilder;
import com.naman14.androidlame.WaveReader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MP3Encoder {
    private static final String TAG = "MP3Encoder";
    private static final int BUFFER_SIZE = 8192;
    private static final int MP3_BUFFER_SIZE = 7200;

    private static final int TARGET_SAMPLE_RATE = 16000;
    private static final int TARGET_BITRATE = 24;

    public interface EncodeListener {
        void onMp3Success(File mp3File);
        void onMp3Progress(int progress);
        void onMp3Error(String error);
    }

    public void encodeToMp3(File inputFile, File outputFile, EncodeListener listener) {
        new Thread(() -> {
            AndroidLame androidLame = null;
            try {
                // 1. 读取WAV文件头信息
                WaveReader waveReader = new WaveReader(inputFile);
                waveReader.openWave();

                // 2. 初始化LAME编码器
                androidLame = new LameBuilder()
                        .setInSampleRate(waveReader.getSampleRate())
                        .setOutSampleRate(waveReader.getSampleRate())
                        .setOutChannels(1)
                        .setMode(LameBuilder.Mode.MONO)
                        .setVbrMode(LameBuilder.VbrMode.VBR_ABR)
                        .setVbrQuality(TARGET_BITRATE)
                        .setQuality(7) // 对应iOS的quality=7
                        .build();

                // 3. 准备文件流
                long totalBytes = inputFile.length();
                long processedBytes = 0;

                try (InputStream inputStream = new BufferedInputStream(new FileInputStream(inputFile));
                     OutputStream outputStream = new FileOutputStream(outputFile)) {

                    // 4. 跳过WAV文件头(44字节)
                    byte[] header = new byte[44];
                    inputStream.read(header);
                    processedBytes += 44;

                    // 5. 准备缓冲区
                    short[] pcmBuffer = new short[BUFFER_SIZE];
                    byte[] mp3Buffer = new byte[MP3_BUFFER_SIZE];

                    // 6. 编码循环
                    int bytesRead;
                    while ((bytesRead = readShorts(inputStream, pcmBuffer)) > 0) {
                        processedBytes += bytesRead * 2;

                        // 编码PCM数据
                        int encodedSize = androidLame.encode(
                                pcmBuffer, pcmBuffer,
                                bytesRead / 2, mp3Buffer
                        );

                        if (encodedSize > 0) {
                            outputStream.write(mp3Buffer, 0, encodedSize);
                        }

                        // 更新进度(0-100)
                        int progress = (int) ((processedBytes * 100) / totalBytes);
                        if (listener != null) {
                            listener.onMp3Progress(Math.min(progress, 100));
                        }
                    }

                    // 7. 刷新编码缓冲区
                    int flushedSize = androidLame.flush(mp3Buffer);
                    if (flushedSize > 0) {
                        outputStream.write(mp3Buffer, 0, flushedSize);
                    }

                    // 8. 完成编码
                    if (listener != null) {
                        listener.onMp3Success(outputFile);
                    }
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.onMp3Error("编码失败: " + e.getMessage());
                }
            } finally {
                if (androidLame != null) {
                    androidLame.close();
                }
            }
        }).start();
    }

    private int readShorts(InputStream input, short[] buffer) throws IOException {
        byte[] byteBuffer = new byte[buffer.length * 2];
        int bytesRead = input.read(byteBuffer);

        if (bytesRead <= 0) return bytesRead;

        // 将字节转换为short (小端序)
        for (int i = 0; i < bytesRead / 2; i++) {
            buffer[i] = (short) ((byteBuffer[i * 2] & 0xFF) |
                    (byteBuffer[i * 2 + 1] << 8));
        }
        return bytesRead / 2;
    }
}
