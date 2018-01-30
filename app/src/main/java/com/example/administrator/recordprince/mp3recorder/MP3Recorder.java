package com.example.administrator.recordprince.mp3recorder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;

import com.example.administrator.recordprince.Base.BaseRecorder;
import com.example.administrator.recordprince.Utils.LameUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * Created by XiaoLuo on 2018/1/29 11:20
 *
 * @项目名 RecordPrince
 * @描述:录音
 */

public class MP3Recorder extends BaseRecorder {
    /*****************************录音默认设置************* start *******************/
    private static final int DEFAULT_AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;//设定录音来源为主麦克风。
    /**
     * 以下三项为默认配置参数。Google Android文档明确表明只有以下3个参数是可以在所有设备上保证支持的。
     */
    private static final int DEFAULT_SAMPLING_RATE = 44100;//(音频采样率)模拟器仅支持从麦克风输入8kHz采样率
    private static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;//录音的声道，单声道
    /**
     * 下面是对此的封装
     * private static final int DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
     */

    private static final PCMFormat DEFAULT_AUDIO_FORMAT = PCMFormat.PCM_16BIT;//(期望录音的比特数)

    /*****************************录音默认设置************* end *******************/


    //======================Lame(LAME是目前最好的MP3编码引擎) Default Settings=====================
    //2 ：near-best quality, not too slow
    //5 ：good quality, fast
    //7 ：ok quality, really fast
    private static final int DEFAULT_LAME_MP3_QUALITY = 7;//(quality ： MP3音频质量。0~9。 其中0是最好，非常慢，9是最差。)

    private static final int DEFAULT_LAME_IN_CHANNEL = 1;  //与DEFAULT_CHANNEL_CONFIG相关，因为是mono单声，所以是1(输入声道数--->单声道)

    private static final int DEFAULT_LAME_MP3_BIT_RATE = 32;//Encoded bit rate. MP3 file will be encoded with bit rate 32kbps(编码率)

    //==================================================================

    private File mRecordFile;
    private AudioRecord mAudioRecord;
    private DataEncodeThread mEncodeThread;
    private Handler errorHandler;
    /**
     * 自定义 每160帧作为一个周期，通知一下需要进行编码
     */
    private static final int FRAME_COUNT = 160;
    public static final int ERROR_TYPE = 22;
    private boolean mIsRecording = false;//录音状态，是否正在录音
    private int mMinBufferSize;//缓冲大小
    private short[] mPCMBuffer;
    private boolean mSendError;
    private boolean mPause;//是否暂停
    private ArrayList<Short> dataList;
    //最大数量
    private int mMaxSize;
    //波形速度
    private int mWaveSpeed = 300;
    private static final int MAX_VOLUME = 2000;

    /**
     * Default constructor. Setup recorder with default sampling rate 1 channel,
     * 16 bits pcm
     *
     * @param recordFile target file
     */
    public MP3Recorder(File recordFile) {
        mRecordFile = recordFile;
    }


    public void start() throws FileNotFoundException {
        if (mIsRecording) {//如果正在录音，return
            return;
        }
        mIsRecording = true; // 提早，防止init或startRecording被多次调用
        initAudioRecorder();
        try {
            mAudioRecord.startRecording();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        new Thread() {
            boolean isError = false;

            @Override
            public void run() {
                super.run();
                //设置线程权限
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                while (mIsRecording) {
                    int readSize = mAudioRecord.read(mPCMBuffer, 0, mMinBufferSize);

                    if (readSize == AudioRecord.ERROR_INVALID_OPERATION || readSize == AudioRecord.ERROR_BAD_VALUE) {//如果读到错误数值
                        if (errorHandler != null && !mSendError) {
                            mSendError = true;
                            errorHandler.sendEmptyMessage(ERROR_TYPE);
                            mIsRecording = false;
                            isError = true;
                        }
                    } else {
                        if (readSize > 0) {
                            if (mPause) {
                                continue;
                            }
                            mEncodeThread.addTask(mPCMBuffer, readSize);
                            calculateRealVolume(mPCMBuffer, readSize);
                            //                            sendData(mPCMBuffer, readSize);
                        } else {
                            if (errorHandler != null && !mSendError) {
                                mSendError = true;
                                errorHandler.sendEmptyMessage(ERROR_TYPE);
                                mIsRecording = false;
                                isError = true;
                            }
                        }
                    }

                    try {
                        // 释放并完成录音
                        mAudioRecord.stop();
                        mAudioRecord.release();
                        mAudioRecord = null;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    // stop the encoding thread and try to wait
                    // until the thread finishes its job
                    if (isError) {
                        mEncodeThread.sendErrorMessage();
                    } else {
                        mEncodeThread.sendStopMessage();
                    }
                }


            }
        }.start();
    }

    private void initAudioRecorder() throws FileNotFoundException {
        mMinBufferSize = AudioRecord.getMinBufferSize(DEFAULT_SAMPLING_RATE,
                DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT.getAudioFormat());//采集数据需要的缓冲区的大小
        int bytesPerFrame = DEFAULT_AUDIO_FORMAT.getBytesPerFrame();
        int frameSize = mMinBufferSize / bytesPerFrame;
           /* Get number of samples. Calculate the buffer size
         * (round up to the factor of given frame size)
		 * 使能被整除，方便下面的周期性通知
		 * */
        if (frameSize % FRAME_COUNT != 0) {
            frameSize += (FRAME_COUNT - frameSize % FRAME_COUNT);
            mMinBufferSize = frameSize * bytesPerFrame;
        }
    /* Setup audio recorder */
        //int audioSource(音频源：指的是从哪里采集音频。这里我们当然是从麦克风采集音频，所以此参数的值为MIC),
        // int sampleRateInHz(采样率：音频的采样频率，每秒钟能够采样的次数，采样率越高，音质越高。给出的实例是44100、22050、11025但不限于这几个参数。例如要采集低质量的音频就可以使用4000、8000等低采样率。),
        // int channelConfig,(声道设置：android支持双声道立体声和单声道。MONO单声道，STEREO立体声)
        // int audioFormat, (编码制式和采样大小：采集来的数据当然使用PCM编码(脉冲代码调制编码，即PCM编码。PCM通过抽样、量化、编码三个步骤将连续变化的模拟信号转换为数字编码。) android支持的采样大小16bit或者8bit。当然采样大小越大，那么信息量越多，音质也越高，现在主流的采样大小都是16bit，在低质量的语音传输的时候8bit足够了。)
        // int bufferSizeInBytes (采集数据需要的缓冲区的大小，如果不知道最小需要的大小可以在getMinBufferSize()查看)
        mAudioRecord = new AudioRecord(DEFAULT_AUDIO_SOURCE, DEFAULT_SAMPLING_RATE,
                DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT.getAudioFormat(), mMinBufferSize);

        mPCMBuffer = new short[mMinBufferSize];//编码缓冲大小

        //初始化MP3编码器
        LameUtil.init(DEFAULT_SAMPLING_RATE, DEFAULT_LAME_IN_CHANNEL, DEFAULT_SAMPLING_RATE, DEFAULT_LAME_MP3_BIT_RATE, DEFAULT_LAME_MP3_QUALITY);
        //创建主线去编码数据
        mEncodeThread = new DataEncodeThread(mRecordFile, mMinBufferSize);
        mEncodeThread.start();
        mAudioRecord.setRecordPositionUpdateListener(mEncodeThread, mEncodeThread.getHandler());
        mAudioRecord.setPositionNotificationPeriod(FRAME_COUNT);
    }

    //重写base中的抽象方法，返回calculateRealVolume计算出的音量
    @Override
    public int getRealVolume() {
        return mVolume;
    }

    /**
     * 获取相对音量。 超过最大值时取最大值。
     *
     * @return 音量
     */
    public int getVolume() {
        if (mVolume >= MAX_VOLUME) {
            return MAX_VOLUME;
        }
        return mVolume;
    }

    /**
     * 根据资料假定的最大值。 实测时有时超过此值。
     *
     * @return 最大音量值。
     */
    public int getMaxVolume() {
        return MAX_VOLUME;
    }


    private void sendData(short[] shorts, int readSize) {
        if (dataList != null) {
            int length = readSize / mWaveSpeed;
            short resultMax = 0, resultMin = 0;
            for (short i = 0, k = 0; i < length; i++, k += mWaveSpeed) {
                for (short j = k, max = 0, min = 1000; j < k + mWaveSpeed; j++) {
                    if (shorts[j] > max) {
                        max = shorts[j];
                        resultMax = max;
                    } else if (shorts[j] < min) {
                        min = shorts[j];
                        resultMin = min;
                    }
                }
                if (dataList.size() > mMaxSize) {
                    dataList.remove(0);
                }
                dataList.add(resultMax);
            }
        }
    }


    /**
     * 设置数据的获取显示，设置最大的获取数，一般都是控件大小/线的间隔offset
     *
     * @param dataList 数据
     * @param maxSize  最大个数
     */
    public void setDataList(ArrayList<Short> dataList, int maxSize) {
        this.dataList = dataList;
        this.mMaxSize = maxSize;
    }

    /**
     * 是否暂停
     */
    public boolean isPause() {
        return mPause;
    }


    public void setPause(boolean pause) {
        this.mPause = pause;
    }

    public void stop() {
        mPause = false;
        mIsRecording = false;
    }

    public boolean isRecording() {
        return mIsRecording;
    }

    /**
     * pcm数据的速度，默认300
     * 数据越大，速度越慢
     */
    public void setWaveSpeed(int waveSpeed) {
        if (mWaveSpeed <= 0) {
            return;
        }
        this.mWaveSpeed = waveSpeed;
    }

    public int getWaveSpeed() {
        return mWaveSpeed;
    }
    /**
     * 设置错误回调
     *
     * @param errorHandler 错误通知
     */
    public void setErrorHandler(Handler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public static void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else {
                String[] filePaths = file.list();
                for (String path : filePaths) {
                    deleteFile(filePath + File.separator + path);
                }
                file.delete();
            }
        }
    }
}
