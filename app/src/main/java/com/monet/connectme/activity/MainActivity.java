package com.monet.connectme.activity;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.monet.connectme.R;
import com.monet.connectme.util.FilesUtil;
import com.monet.connectme.util.MobileConnect;
import com.monet.connectme.util.WifiApConnect;
import com.monet.connectme.util.WifiConnect;

import java.util.List;

/**
 * Created by Monet on 2015/6/29.
 * <p/>
 * USUAGE
 * --------------------------------------------------------------
 * Setting in /data/data/com.monet.connectme/files/ConnectTo
 *
 * Example 1: <CHOOSE>,SSID,PASSWORD   [split with ","]
 * where <CHOOSE> can be openWifi | openWifiAp | openNetwork.
 * Means opening Wifi or WifiAp using given SSID and PASSWORD.
 * When choose openNetwork, it will smartly(for better one) connect to Wifi you specified or 3G .
 * Example 2: <CHOOSE>
 * where <CHOOSE> can be closeWifi | closeWifiAp | close3G | closeNetwork | open3G .
 * Means closing Wifi ,WifiAp ,3G, all Networks or open 3G.
 *
 * Totally 8 choices .
 * --------------------------------------------------------------
 */

public class MainActivity extends Activity {
    private WifiConnect mWifiConnect;
    private WifiApConnect mWifiApConnect;
    private WifiConfiguration mWifiConfiguration;
    private static String SSID = "TestConnectMe";
    private static String PASSWORD = "";
    private static String CHOOSE = "openWifiAp";
    private static int signalStrengthValue = -1000;
    private static int rssi = -500;
    MobileSignalListener mMobileSignalListener;
    TelephonyManager mTelephonyManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initSignalListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        boolean doneWell = false;
        // FilesUtil.save(this, "openWifi," + "Test00," + "test123456");
        // 从配置文件读取信息
        String info = FilesUtil.load(this);
        if (!info.equals("")) {
            // 有三个词的情况
            if (info.contains(",")) {
                // 用逗号分隔SSID和PASSWORD,并去掉首尾的空格
                String[] str = info.trim().split(",");
                CHOOSE = str[0].trim();   //选择Wifi还是Wifi热点
                SSID = str[1].trim();    //提取SSID
                PASSWORD = str[2].trim();  //提取PASSWORD
            } else {
                CHOOSE = info.trim();
            }
        } else {
            Log.e("Main", "配置文件为空");
        }

        // 然后选择不同的操作
        if (CHOOSE.equalsIgnoreCase("openWifi")) {
            doneWell = openWifi();
        } else if (CHOOSE.equalsIgnoreCase("openWifiAp")) {
            doneWell = openWifiAp();
        } else if (CHOOSE.equalsIgnoreCase("closeWifi")) {
            doneWell = closeWifi();
        } else if (CHOOSE.equalsIgnoreCase("closeWifiAp")) {
            doneWell = closeWifiAp();
        } else if (CHOOSE.equalsIgnoreCase("open3G")) {
            doneWell = open3G();
        } else if (CHOOSE.equalsIgnoreCase("close3G")) {
            doneWell = close3G();
        } else if (CHOOSE.equalsIgnoreCase("openNetwork")) {
            doneWell = openNetwork();
        } else if (CHOOSE.equalsIgnoreCase("closeNetwork")) {
            doneWell = closeNetwork();
        } else {
            doneWell = false;
        }
        // 主动退出程序
        sayGoodBye(doneWell);
    }

    private boolean openWifi() {
        mWifiConnect = new WifiConnect(this);
        //mWifiConnect.openWifi已经先关闭了WifiAp
        mWifiConnect.openWifi();
        //等待Wifi打开,如果当前状态为没有打开则一直循环
        while (mWifiConnect.checkState() != WifiManager.WIFI_STATE_ENABLED) {
            try {
                //为了避免程序一直while循环，每隔100毫秒再检测
                Thread.currentThread();
                Thread.sleep(100);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        //写入所要连接的AP的信息
        mWifiConfiguration = mWifiConnect.CreateWifiInfo(SSID, PASSWORD, WifiConnect.WIFICIPHER_WPA);
        mWifiConnect.addNetwork(mWifiConfiguration);
        try {
            //等待2s后再返回是否已连接的结果
            Thread.currentThread();
            Thread.sleep(2000);
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        return WifiConnect.isWiFiActive(this);
    }

    private boolean openWifiAp() {
        mWifiApConnect = new WifiApConnect(this);
        // mWifiApConnect.openWifiAp已先关闭Wifi
        return mWifiApConnect.openWifiAp(SSID, PASSWORD);
    }

    private boolean closeWifi() {
        mWifiConnect = new WifiConnect(this);
        return mWifiConnect.closeWifi();
    }

    private boolean closeWifiAp() {
        mWifiApConnect = new WifiApConnect(this);
        return mWifiApConnect.closeWifiAp();
    }

    private boolean open3G() {
        return MobileConnect.open3G(this);
    }

    private boolean close3G() {
        return MobileConnect.close3G(this);
    }

    private boolean openNetwork() {
        /**
         *  Step1: 获取rssi
         */
        mWifiConnect = new WifiConnect(this);
        //获取RSSI需要先打开Wifi，但不需要连接
        mWifiConnect.openWifi();
        //打开Wifi需要花很长时间，所以需要等，这点要注意
        while (mWifiConnect.checkState() != WifiManager.WIFI_STATE_ENABLED) {
            try {
                //为了避免程序一直while循环，每隔100毫秒再检测
                Thread.currentThread();
                Thread.sleep(100);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        List<ScanResult> scanResults = mWifiConnect.getScanResults();
        for (ScanResult scanResult : scanResults) {
            Log.e("Main", "scanSSID: " + scanResult.SSID);
            if (scanResult.SSID.equals(SSID)) {
                rssi = scanResult.level;
                Log.e("Main", scanResult.SSID + "--rssi: " + rssi);
                break;
            }
        }
        //下面这个是得到所连接的AP的RSSI，在此处不太适用
        //rssi = mWifiConnect.getRssi();
        /**
         *  Step2: 获取signalStrengthValue
         */
        int tempSignal = MobileConnect.getSignalStrengthValue(this);  //获取的一直是neighbor Cell的Signal,值比较小
        if (tempSignal > signalStrengthValue) {
            // 很少被执行的
            signalStrengthValue = tempSignal;
        }
        Log.e("Main", "rssi: " + rssi);
        Log.e("Main", "mobileSignal: " + signalStrengthValue);
        Toast.makeText(this, "rssi " + rssi + "  " + signalStrengthValue + " signal", Toast.LENGTH_LONG).show();
        if (rssi < signalStrengthValue) {
            closeWifi();
            while (mWifiConnect.checkState() == WifiManager.WIFI_STATE_ENABLED) {
                try {
                    //为了避免程序一直while循环，每隔100毫秒再检测
                    Thread.currentThread();
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
            return open3G();
        } else {
            close3G();
            return openWifi();
        }
    }

    private boolean closeNetwork() {
        closeWifiAp();
        closeWifi();
        return close3G();
    }

    private void sayGoodBye(Boolean doneWell){
        // 退出程序
        if (doneWell) {
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


    private void initSignalListener() {
        try {
            mMobileSignalListener = new MobileSignalListener();
            mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            mTelephonyManager.listen(mMobileSignalListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public class MobileSignalListener extends PhoneStateListener {
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            if (signalStrength.isGsm()) {
                if (signalStrength.getGsmSignalStrength() != 99)
                    signalStrengthValue = signalStrength.getGsmSignalStrength() * 2 - 113;
                else
                    signalStrengthValue  = signalStrength.getGsmSignalStrength();
            } else {
                signalStrengthValue  = signalStrength.getCdmaDbm();
            }
            Log.e("Main", "Changed" + signalStrengthValue);
        }
    }

    @Override

    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}
