package com.monet.connectme.activity;

import android.app.Activity;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import com.monet.connectme.R;
import com.monet.connectme.util.FilesUtil;
import com.monet.connectme.util.WifiApConnect;
import com.monet.connectme.util.WifiConnect;

/**
 * Created by Monet on 2015/6/29.
 *
 * USUAGE
 * --------------------------------------------------------------
 * Setting in /data/data/com.monet.connectme/files/ConnectTo
 * Example 1: <CHOOSE>,SSID,PASSWORD   [split with ","]
 *     where <CHOOSE> can be openWifi | openWifiAp .
 * Means opening Wifi or WifiAp using given SSID and PASSWORD.
 * Example 2: <CHOOSE>
 *     where <CHOOSE> can be closeWifi | closeWifiAp .
 * Means closing Wifi or WifiAp.
 * --------------------------------------------------------------
 */

public class MainActivity extends Activity {
    private WifiConnect wifiConnect;
    private WifiApConnect wifiApConnect;
    private WifiConfiguration mWifiConfiguration;
    private String SSID = "TestConnectMe";
    private String PASSWORD = "";
    private String CHOOSE = "openWifiAp";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        boolean bool;
        // FilesUtil.save(this, "openWifi," + "Test00," + "test123456");
        // 从配置文件读取信息
        String info = FilesUtil.load(this);
        if (!info.equals("")) {
            if (info.trim().equalsIgnoreCase("closeWifi") | info.trim().equalsIgnoreCase("closeWifiAp")) {
                CHOOSE = info.trim();
            } else {
                // 用逗号分隔SSID和PASSWORD,并去掉首尾的空格
                String[] str = info.trim().split(",");
                CHOOSE = str[0].trim();   //选择Wifi还是Wifi热点
                SSID = str[1].trim();    //提取SSID
                PASSWORD = str[2].trim();  //提取PASSWORD
            }
        }
        // 选择不同的操作
        if (CHOOSE.equalsIgnoreCase("openWifiAp")) {
            bool = openWifiAp();
        } else if (CHOOSE.equalsIgnoreCase("openWifi")) {
            bool = openWifi();
        }else if (CHOOSE.equalsIgnoreCase("closeWifi")) {
            bool = closeWifi();
        }else if (CHOOSE.equalsIgnoreCase("closeWifiAp")) {
            bool = closeWifiAp();
        } else {
            bool = false;
        }

        // 退出程序
        if (bool) {
            // 立即退出
            finish();
        } else {
            try {
                //5秒后退出
                Thread.currentThread();
                Thread.sleep(5000);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private boolean openWifi() {
        //wifiAp 与 wifi 不能共存，故先关闭wifiAp
        WifiApConnect.closeWifiAp(this);
        wifiConnect = new WifiConnect(this);
        //打开WIFI
        wifiConnect.openWifi();
        //等待Wifi打开,如果当前状态为没有打开则一直循环
        while (wifiConnect.checkState() != WifiManager.WIFI_STATE_ENABLED) {
            try {
                //为了避免程序一直while循环，每隔100毫秒再检测
                Thread.currentThread();
                Thread.sleep(100);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        //写入所要连接的AP的信息
        mWifiConfiguration = wifiConnect.CreateWifiInfo(SSID, PASSWORD, WifiConnect.WIFICIPHER_WPA);
        wifiConnect.addNetwork(mWifiConfiguration);
        try {
            //等待3s后再返回是否已连接的结果
            Thread.currentThread();
            Thread.sleep(3000);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }

        return WifiConnect.isWiFiActive(this);
    }

    private boolean openWifiAp() {
        wifiApConnect = new WifiApConnect(this);
        return wifiApConnect.openWifiAp(SSID, PASSWORD);
    }

    private boolean closeWifi() {
        wifiConnect = new WifiConnect(this);
        return wifiConnect.closeWifi();
    }

    private boolean closeWifiAp() {
        return WifiApConnect.closeWifiAp(this);
    }
}
