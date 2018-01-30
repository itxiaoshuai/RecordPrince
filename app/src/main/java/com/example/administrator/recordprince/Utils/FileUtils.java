package com.example.administrator.recordprince.Utils;

import android.os.Environment;

import java.io.File;

public class FileUtils {
    public static String recAudioPath(String file) {
        return new File(Environment.getExternalStorageDirectory(), file).getAbsolutePath();
    }
    public static File recAudioDir(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }
    private static final String SD_PATH = Environment.getExternalStorageDirectory().getPath();
    private static final String DATA_PATH = Environment.getDataDirectory().getPath();
    private static final String SD_STATE = Environment.getExternalStorageState();
    public static final String NAME = "audioWave";

    public static String getAppPath() {
        StringBuilder sb = new StringBuilder();
        if (SD_STATE.equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            sb.append(SD_PATH);
        } else {
            sb.append(DATA_PATH);
        }
        sb.append(File.separator);
        sb.append(NAME);
        sb.append(File.separator);
        return sb.toString();
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