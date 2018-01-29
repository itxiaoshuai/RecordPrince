package com.example.administrator.recordprince.Base;

/**
 * Created by XiaoLuo on 2018/1/29 11:22
 *
 * @项目名 RecordPrince
 * @描述:
 */

public abstract class BaseRecorder {
    protected int mVolume;// 音量

    public abstract int getRealVolume();//抽象方法（获得音量）

    /**
     * @param buffer   buffer
     * @param readSize readSize
     */
    private void calculateRealVolume(Short[] buffer, int readSize) {
        double sum = 0;
        for (int i = 0; i < readSize; i++) {
            // 这里没有做运算的优化，为了更加清晰的展示代码
            sum += buffer[i] * buffer[i];
        }
        if (readSize > 0) {
            double amplitude = sum / readSize;
            mVolume = (int) Math.sqrt(amplitude);
        }
    }
}
