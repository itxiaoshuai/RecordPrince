package com.example.administrator.recordprince.Utils;

/**
 * Created by XiaoLuo on 2018/1/29 15:47
 *
 * @项目名 RecordPrince
 * @描述: mp3转化工具类主要是一些native方法的调用
 */

public class LameUtil {
    static {
        System.loadLibrary("mp3lame");//静态代码块，随着类的加载而加载
    }

    /**
     * @param inSamplerate  输入采样频率 Hz
     * @param inChannel     输入声道数
     * @param outSamplerate 输出采样频率 Hz
     * @param outBitrate    编码率
     * @param quality       MP3音频质量
     */
    public native static void init(int inSamplerate, int inChannel,
                                   int outSamplerate, int outBitrate, int quality);

    /**
     * @param bufferLeft  左声道数据
     * @param bufferRight 右声道数据
     * @param samples     每个声道输入数据大小
     * @param mp3buf      用于接收转换后的数据。7200 + (1.25 * buffer_l.length)
     * @return
     */

//    左右声道 ：当前声道选的是单声道，因此两边传入一样的buffer。
//    输入数据大小 ：录音线程读取到buffer中的数据不一定是占满的，所以read方法会返回当前大小size，即前size个数据是有效的音频数据，后面的数据是以前留下的废数据。 这个size同样需要传入到Lame编码器中用于编码。
//    mp3的buffer：官方规定了计算公式：7200 + (1.25 * buffer_l.length)。（可以在lame.h文件中看到）
    public native static int encode(short[] bufferLeft, short[] bufferRight,
                                    int samples, byte[] mp3buf);

    /**
     * Flush LAME buffer.
     * @param mp3buf 刷新要编码的流，避免有遗漏的数据
     * @return
     */
    public native static int flush(byte[] mp3buf);

    /**
     * Close LAME.关闭 lame)
     */
    public native static void close();
}
