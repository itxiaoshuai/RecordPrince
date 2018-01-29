package com.example.administrator.recordprince;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.iv_read_compare_show_back, R.id.iv_play, R.id.iv_record, R.id.iv_upload})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_read_compare_show_back://返回
                break;
            case R.id.iv_play://播放
                break;
            case R.id.iv_record://录音
                break;
            case R.id.iv_upload://上传
                break;
        }
    }
}
