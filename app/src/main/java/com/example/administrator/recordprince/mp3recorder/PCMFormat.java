package com.example.administrator.recordprince.mp3recorder;

import android.media.AudioFormat;

/**
 * Created by XiaoLuo on 2018/1/29 13:43
 *
 * @项目名 RecordPrince
 * @描述:比特率
 */

public enum PCMFormat {
    PCM_8BIT(1, AudioFormat.ENCODING_PCM_8BIT),
    PCM_16BIT(2, AudioFormat.ENCODING_PCM_16BIT);
    private int bytesPerFrame;
    private int audioFormat;

    PCMFormat(int bytesPerFrame, int audioFormat) {
        this.bytesPerFrame = bytesPerFrame;
        this.audioFormat = audioFormat;
    }

    public int getBytesPerFrame() {
        return bytesPerFrame;
    }

    public int getAudioFormat() {
        return audioFormat;
    }
}
