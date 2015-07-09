package com.monet.connectme.util;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Monet on 2015/6/29.
 */
public class WifiApConnect {
    private WifiManager mWifiManager = null;
    private Context mContext = null;

    public WifiApConnect(Context context) {
        mContext = context;
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
    }

    public boolean openWifiAp(String ssid, String passwd) {
        //会与Wifi冲突，故先关闭Wifi
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }
        if (!this.isWifiApEnabled()) {
            Method method1 = null;
            try {
                method1 = mWifiManager.getClass().getMethod("setWifiApEnabled",
                        WifiConfiguration.class, Boolean.TYPE);
                WifiConfiguration netConfig = new WifiConfiguration();

                netConfig.SSID = ssid;
                netConfig.preSharedKey = passwd;

                netConfig.allowedAuthAlgorithms
                        .set(WifiConfiguration.AuthAlgorithm.OPEN);
                netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                netConfig.allowedKeyManagement
                        .set(WifiConfiguration.KeyMgmt.WPA_PSK);
                netConfig.allowedPairwiseCiphers
                        .set(WifiConfiguration.PairwiseCipher.CCMP);
                netConfig.allowedPairwiseCiphers
                        .set(WifiConfiguration.PairwiseCipher.TKIP);
                netConfig.allowedGroupCiphers
                        .set(WifiConfiguration.GroupCipher.CCMP);
                netConfig.allowedGroupCiphers
                        .set(WifiConfiguration.GroupCipher.TKIP);

                return (boolean) method1.invoke(mWifiManager, netConfig, true);

            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
            return  false;
        }

    public boolean closeWifiAp() {
        if (this.isWifiApEnabled()) {
            try {
                Method method = mWifiManager.getClass().getMethod("getWifiApConfiguration");
                method.setAccessible(true);
                WifiConfiguration config = (WifiConfiguration) method.invoke(mWifiManager);
                Method method2 = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
                return (boolean) method2.invoke(mWifiManager, config, false);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public boolean isWifiApEnabled() {
        try {
            Method method = mWifiManager.getClass().getMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(mWifiManager);

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}


