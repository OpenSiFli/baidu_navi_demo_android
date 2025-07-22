package com.baidu.mapclient.liteapp.tts;

import com.naman14.androidlame.AndroidLame;
import com.naman14.androidlame.LameBuilder;
import com.naman14.androidlame.WaveReader;
import com.sifli.siflicore.log.SFLog;

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
    private static final int MP3_BUFFER_SIZE = 8192;

    private static final int TARGET_SAMPLE_RATE = 16000;
    private static final int TARGET_BITRATE = 16;
    private static final int RIFF_HEADER_SIZE = 12; // "RIFF" + size + "WAVE"

    public interface EncodeListener {
        void onMp3Success(File mp3File);
        void onMp3Progress(int progress);
        void onMp3Error(String error);
    }

    // 保留原有import和接口定义...
    public void encodeToMp3(File inputFile, File outputFile, EncodeListener listener) {
        new Thread(() -> {
            AndroidLame androidLame = null;
            try {
                WaveReader waveReader = new WaveReader(inputFile);
                waveReader.openWave();
                int dataOffset = getDataOffset(inputFile);
                long dataSize = waveReader.getDataSize();

                androidLame = new LameBuilder()
                        .setInSampleRate(waveReader.getSampleRate())
                        .setOutSampleRate(waveReader.getSampleRate())
                        .setOutChannels(1)
                        .setMode(LameBuilder.Mode.MONO)
                        .setVbrMode(LameBuilder.VbrMode.VBR_ABR)
                        .setAbrMeanBitrate(TARGET_BITRATE)
                        .setVbrQuality(4)
                        .setQuality(7) // 对应iOS的quality=7
                        .build();

                try (InputStream inputStream = new BufferedInputStream(new FileInputStream(inputFile));
                     OutputStream outputStream = new FileOutputStream(outputFile)) {

                    inputStream.skip(dataOffset);
                    short[] pcmBuffer = new short[BUFFER_SIZE];
                    byte[] mp3Buffer = new byte[MP3_BUFFER_SIZE];
                    long remainingBytes = dataSize;

                    int bytesRead;
                    while (remainingBytes > 0 && (bytesRead = readShorts(inputStream, pcmBuffer)) > 0) {
                        int samplesToEncode = Math.min(bytesRead, (int)(remainingBytes / 2));
                        int encodedSize = androidLame.encode(pcmBuffer, pcmBuffer, samplesToEncode, mp3Buffer);
                        if (encodedSize > 0) outputStream.write(mp3Buffer, 0, encodedSize);
                        remainingBytes -= samplesToEncode * 2;
                        // 进度更新逻辑...
                    }

                    int flushedSize = androidLame.flush(mp3Buffer);
                    if (flushedSize > 0) outputStream.write(mp3Buffer, 0, flushedSize);
                    listener.onMp3Success(outputFile);
                }
            } catch (Exception e) {
                listener.onMp3Error(e.getMessage());
            } finally {
                if (androidLame != null) androidLame.close();
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

    public static int getDataOffset(File waveFile) throws IOException {
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(waveFile))) {
            // 跳过RIFF头
            inputStream.skip(RIFF_HEADER_SIZE);

            // 读取fmt块头
            byte[] fmtChunk = new byte[8];
            inputStream.read(fmtChunk);

            // 验证fmt标记
            if (!new String(fmtChunk, 0, 4).equals("fmt ")) {
                throw new IOException("Invalid WAV fmt chunk");
            }

            // 计算fmt块大小
            int fmtSize = (fmtChunk[4] & 0xFF) |
                    (fmtChunk[5] << 8) |
                    (fmtChunk[6] << 16) |
                    (fmtChunk[7] << 24);

            // 计算总偏移量：RIFF头 + fmt块头 + fmt块内容 + data块头
            return RIFF_HEADER_SIZE + 8 + fmtSize + 8;
        }
    }
}
