package com.example.administrator.recordprince.Utils;

/**
 * Created by XiaoLuo on 2018/1/29 15:47
 *
 * @项目名 RecordPrince
 * @描述:
 */

public class LameUtil {
    static{
        System.loadLibrary("mp3lame");//静态代码块，随着类的加载而加载
    }
}
