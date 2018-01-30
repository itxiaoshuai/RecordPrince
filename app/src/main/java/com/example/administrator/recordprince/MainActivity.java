package com.example.administrator.recordprince;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.administrator.recordprince.Utils.ToastUtils;
import com.example.administrator.recordprince.mp3recorder.MP3Recorder;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.iv_read_compare_show_back)
    ImageView mIvReadCompareShowBack;
    @BindView(R.id.tv_re_recording)
    TextView mTvReRecording;
    @BindView(R.id.wv_show)
    WebView mWvShow;
    @BindView(R.id.tv_record_time)
    TextView mTvRecordTime;
    @BindView(R.id.iv_play)
    ImageView mIvPlay;
    @BindView(R.id.iv_record)
    ImageView mIvRecord;
    @BindView(R.id.iv_upload)
    ImageView mIvUpload;

    private String filePath;//文件路径
    private boolean isRecordable = true;//默认是可以录音的
    private MP3Recorder mRecorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.iv_read_compare_show_back, R.id.iv_play, R.id.iv_record, R.id.iv_upload})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_read_compare_show_back:
                ToastUtils.showSingleLongToast("返回");
                break;
            case R.id.iv_play:
                ToastUtils.showSingleLongToast("播放");

//                filePath = FileUtils.getAppPath();
//                File file = new File(filePath);
//                if (!file.exists()) {
//                    if (!file.mkdirs()) {
//                        Toast.makeText(MainActivity.this, "创建文件失败", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//
//                    filePath = FileUtils.getAppPath() + UUID.randomUUID().toString() + ".mp3";
//                    MP3Recorder mp3Recorder = new MP3Recorder(new File(filePath));
//                    try {
//                        mp3Recorder.start();
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    }
//                }
                break;
            case R.id.iv_record:
                ToastUtils.showSingleLongToast("录音");
                if (isRecordable) {//如果当前是可以录音的状态（UI状态）
                    if (mRecorder != null && mRecorder.isPause()) {
                    }
                }
                break;
            case R.id.iv_upload:
                ToastUtils.showSingleLongToast("上传录音");
                break;
        }
    }
}
