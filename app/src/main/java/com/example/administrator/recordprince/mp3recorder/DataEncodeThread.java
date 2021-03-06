package com.example.administrator.recordprince.mp3recorder;

import android.media.AudioRecord;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.example.administrator.recordprince.Utils.LameUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by XiaoLuo on 2018/1/29 16:53
 *
 * @项目名 RecordPrince
 * @描述:
 */

public class DataEncodeThread extends HandlerThread implements AudioRecord.OnRecordPositionUpdateListener {
    private FileOutputStream mFileOutputStream;//文件输出流
    private String path;
    private byte[] mMp3Buffer;
    private StopHandler mHandler;
    private static final int PROCESS_STOP = 1;
    private static final int PROCESS_ERROR = 2;


    private static class StopHandler extends Handler {
        private DataEncodeThread mDataEncodeThread;

        public StopHandler(Looper looper, DataEncodeThread dataEncodeThread) {
            super(looper);
            mDataEncodeThread = dataEncodeThread;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == PROCESS_STOP) {
                //处理缓冲区中的数据
                while (mDataEncodeThread.processData() > 0) ;//while(true); 死循环，不断检索缓冲区是否有数据
                // Cancel any event left in the queue
                removeCallbacksAndMessages(null);//清除当前mHandler消息队列
                mDataEncodeThread.flushAndRelease();
                getLooper().quit();
            } else if (msg.what == PROCESS_ERROR) {
                //处理缓冲区中的数据
                while (mDataEncodeThread.processData() > 0) ;
                // Cancel any event left in the queue
                removeCallbacksAndMessages(null);
                mDataEncodeThread.flushAndRelease();
                getLooper().quit();
                MP3Recorder.deleteFile(mDataEncodeThread.path);//删除文件和文件夹
            }
        }
    }


    public DataEncodeThread(File file, int bufferSize) throws FileNotFoundException {
        super("DataEncodeThread");
        this.mFileOutputStream = new FileOutputStream(file);
        path = file.getAbsolutePath();
        mMp3Buffer = new byte[(int) (7200 + (bufferSize * 2 * 1.25))];//这个大小是默认写法
    }

    /**
     * 重写thread的start方法
     */
    @Override
    public synchronized void start() {
        super.start();
        mHandler = new StopHandler(getLooper(), this);// 子线程创建hndler要准备looper
    }

    private void check() {
        if (mHandler == null) {
            throw new IllegalStateException();
        }
    }

    public void sendErrorMessage() {
        check();
        mHandler.sendEmptyMessage(PROCESS_ERROR);
    }

    public void sendStopMessage() {
        check();
        mHandler.sendEmptyMessage(PROCESS_STOP);
    }

    public Handler getHandler() {
        check();
        return mHandler;
    }

    /**
     * 当AudioRecord达到setNotificationMarkerPosition（int）设置的通知标记
     * 。
     *
     * @param recorder
     */
    @Override
    public void onMarkerReached(AudioRecord recorder) {

    }

    /**
     * 由setPositionNotificationPeriod（int）设置的记录头的进度定期更新时，将调用回调的接口定义
     *
     * @param recorder
     */
    @Override
    public void onPeriodicNotification(AudioRecord recorder) {
        processData();
    }

    /**
     * 从缓冲区中读取并处理数据，使用lame编码MP3
     *
     * @return 从缓冲区中读取的数据的长度
     * 缓冲区中没有数据时返回0
     */
    private int processData() {
        if (mTasks.size() > 0) {
            Task task = mTasks.remove(0);
            short[] buffer = task.getData();
            int readSize = task.getReadSize();
            int encodedSize = LameUtil.encode(buffer, buffer, readSize, mMp3Buffer);
            if (encodedSize > 0) {
                try {
                    mFileOutputStream.write(mMp3Buffer, 0, encodedSize);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return readSize;
        }
        return 0;
    }

    /**
     * 将MP3结尾信息写入buffer中
     */
    private void flushAndRelease() {
        int flush = LameUtil.flush(mMp3Buffer);
        if (flush > 0) {//如果流中还有数据
            try {
                mFileOutputStream.write(mMp3Buffer, 0, flush);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (mFileOutputStream != null) {
                    try {
                        mFileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                LameUtil.close();
            }
        }
    }

    private List<Task> mTasks = Collections.synchronizedList(new ArrayList<Task>());//ArrayList本身不是线程安全的，所以通过集合Collections.synchronizedList将其转换为一个线程安全的类

    public void addTask(short[] rawData, int readSize) {
        mTasks.add(new Task(rawData, readSize));
    }

    private class Task {
        private short[] rawData;
        private int readSize;

        public Task(short[] rawData, int readSize) {
            this.rawData = rawData.clone();
            this.readSize = readSize;
        }

        public short[] getData() {
            return rawData;
        }

        public int getReadSize() {
            return readSize;
        }
    }
}
