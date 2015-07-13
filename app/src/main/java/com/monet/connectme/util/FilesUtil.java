package com.monet.connectme.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.apache.http.util.EncodingUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by Monet on 2015/6/29.
 */
public class FilesUtil {
    //文件存放在 /data/data/<package name>/files/ 下，需要root权限才能查看
    public static void save(Context context,String data) {
        FileOutputStream out = null;
        BufferedWriter writer = null;
        try {
            // 文件名为ConncetTo
            out = context.openFileOutput("ConnectTo", Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(data);
            Log.e("file", "write configure file succeed.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //文件存放在 /data/data/<package name>/ 下，需要root权限才能查看
    public static String load(Context context) {
        FileInputStream in = null;
        BufferedReader reader = null;
        StringBuilder content = new StringBuilder();
        try {
            //文件名为ConnectTo
            in = context.openFileInput("ConnectTo");
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.e("file", "read configure file succeed.");
        return content.toString();
    }

    //任意位置读文件,此时便不需要root权限了
    public static String readFile() throws IOException {
        String path= Environment.getExternalStorageDirectory().
                getCanonicalPath().toString() + "/ConnectMe";
        File file1 = new File(path);
        if (!file1.exists()) {
            file1.mkdir();
        }
        File file = new File(path + "/config");
        FileInputStream fis = new FileInputStream(file);
        int length = fis.available();
        byte [] buffer = new byte[length];
        fis.read(buffer);
        String res = EncodingUtils.getString(buffer, "UTF-8");
        fis.close();
        return res;
    }

    //任意位置写文件，此时便不需要root权限了
    public static void writeFile(String write_str) throws IOException{
        String path= Environment.getExternalStorageDirectory().
                getCanonicalPath().toString() + "/ConnectMe";
        File file1 = new File(path);
        if (!file1.exists()) {
            file1.mkdir();
        }
        File file = new File(path + "/config");
        if (!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(file);
        byte [] bytes = write_str.getBytes();
        fos.write(bytes);
        fos.close();
    }

}
