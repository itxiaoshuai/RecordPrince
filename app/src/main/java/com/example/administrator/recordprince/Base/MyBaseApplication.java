package com.example.administrator.recordprince.Base;

import android.app.Application;
import android.content.Context;

/**
 * Created by XiaoLuo on 2017/12/13 19:44
 * 描述:
 */

public class MyBaseApplication extends Application {

    //上下文
    public static Context mContext;

    /**
     * 得到上下文对象
     *
     * @return
     */
    public static Context getContext() {
        return mContext;
    }

    public void onCreate() {
        super.onCreate();
        //初始化上下文
        mContext = getApplicationContext();
    }

}
