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
 * Setting in /data/data/com.monet.connectme/files/ConnectTo
 * Example: openWifi(Or openWifiAp),SSID,PASSWORD   [split with ","]
 * Means opening Wifi or open WifiAp using given SSID and PASSWORD.
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
        // FilesUtil.save(this, "openWifi," + "Test00," + "test123456");
        String info = FilesUtil.load(this);
        if (!info.equals("")) {
            //用空格或逗号分隔SSID和PASSWORD
            String[] str = info.split(",");
            CHOOSE = str[0];   //选择Wifi还是Wifi热点
            SSID = str[1];    //提取SSID
            PASSWORD = str[2];  //提取PASSWORD
        }
        if (CHOOSE.equalsIgnoreCase("openWifiAp")) {
            openWifiAp();
        } else {
            openWifi();
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
}
