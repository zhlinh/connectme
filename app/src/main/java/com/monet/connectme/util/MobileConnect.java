package com.monet.connectme.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by Monet on 2015/7/7.
 * 移动网络开关
 */
public class MobileConnect {
    public static final int NETWORK_TYPE_WIFI       = 0;
    public static final int NETWORK_TYPE_3G         = 1;
    public static final int NETWORK_TYPE_2G         = 2;
    public static final int NETWORK_TYPE_WAP        = 3;
    public static final int NETWORK_TYPE_UNKNOWN    = 4;
    public static final int NETWORK_TYPE_DISCONNECT = 5;
    public static final int NETWORK_TYPE_BAD_WIFI = 6;
    public static final int NETWORK_TYPE_BAD_MOBILE = 7;

    // TODO 暂时未用到，可用于检查网络连接
    public static int getNetworkType(Context context) {
        int type = NETWORK_TYPE_DISCONNECT;
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return type;
        } else {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo == null || !networkInfo.isAvailable() || !networkInfo.isConnected()) {
                //Log.e("MobileConnect", type);
                return type;
            } else {
                String typeName = networkInfo.getTypeName();
                if (!checkConnectWithPing()) {
                    if ("WIFI".equalsIgnoreCase(typeName)) {
                        return NETWORK_TYPE_BAD_WIFI;
                    } else {
                        return NETWORK_TYPE_BAD_MOBILE;
                    }
                } else if ("WIFI".equalsIgnoreCase(typeName)) {
                    type = NETWORK_TYPE_WIFI;
                } else if ("MOBILE".equalsIgnoreCase(typeName)) {
                    String proxyHost = android.net.Proxy.getDefaultHost();
                    type = TextUtils.isEmpty(proxyHost) ? (isFastMobileNetwork(context) ? NETWORK_TYPE_3G : NETWORK_TYPE_2G)
                            : NETWORK_TYPE_WAP;
                } else {
                    type = NETWORK_TYPE_UNKNOWN;
                }
            }
        }
        //Log.e("MobileConnect", type);
        return type;
    }

    // 检查是否可以与外网通信
    private static boolean checkConnectWithPing() {
        try {
            String ip = "www.baidu.com";
            Process p = Runtime.getRuntime().exec("ping -c 1 -w 5 " + ip);// ping网址1次 超时为10秒
            // ping的状态
            int status = p.waitFor();
            if (status != 0) {
                return false;
            }else {
                return true;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }

    private static boolean isFastMobileNetwork(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager == null) {
            return false;
        }
        switch (telephonyManager.getNetworkType()) {
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return false;
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return false;
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return false;
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return true;
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return true;
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return false;
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return true;
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return true;
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return true;
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return true;
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return true;
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return true;
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return true;
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return false;
            case TelephonyManager.NETWORK_TYPE_LTE:
                return true;
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                return false;
            default:
                return false;
        }
    }

    public static int getSignalStrengthValue(Context context) {
        int signalStrengthValue = -1000;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        // for example value of first element
        List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();
        if (cellInfos != null) {
            if (TelephonyManager.PHONE_TYPE_NONE == telephonyManager.getPhoneType()) {
                // 没有移动网络，比如平板，或未手机插入sim卡
                Log.e("Main", "PHONE_TYPE_NONE: " + signalStrengthValue);
                return signalStrengthValue;
            } else if (TelephonyManager.PHONE_TYPE_CDMA == telephonyManager.getPhoneType()) {
                // CDMA制式
                CellInfoCdma cellInfoCdma = (CellInfoCdma) cellInfos.get(0);
                CellSignalStrengthCdma cellSignalStrengthCdma = cellInfoCdma.getCellSignalStrength();
                signalStrengthValue = cellSignalStrengthCdma.getDbm();
                Log.e("Main", "PHONE_TYPE_CDMA: " + signalStrengthValue);
            } else {
                // GSM 制式或sip（VOIP的，不常用）
                CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfos.get(0);
                CellSignalStrengthGsm cellSignalStrengthGsm = cellInfoGsm.getCellSignalStrength();
                signalStrengthValue = cellSignalStrengthGsm.getDbm();
                Log.e("Main", "PHONE_TYPE_GSM(or VOIP): " + signalStrengthValue);
            }
        } else {
            List<NeighboringCellInfo> neighboringCellInfos = telephonyManager.getNeighboringCellInfo();
            if (neighboringCellInfos != null) {
                NeighboringCellInfo neighboringCellInfo = neighboringCellInfos.get(0);
                int asu = neighboringCellInfo.getRssi();
                signalStrengthValue = -113 + 2 * asu;
                Log.e("Main", "neithborCellInfos: " + signalStrengthValue);
            }
        }
        return signalStrengthValue;
    }

    public static boolean open3G(Context context) {
        return setMobileDataEnabled(context, true);
    }

    public static boolean close3G(Context context) {
        return setMobileDataEnabled(context, false);
    }

    /**
     * 系统没有直接提供开放的方法，只在ConnectivityManager类中有一个不可见的setMobileDataEnabled方法
     * 它是调用IConnectivityManager类中的setMobileDataEnabled(boolean)方法
     * 由于方法不可见，只能采用反射来调用：
     */

    public static boolean setMobileDataEnabled(Context context, boolean enabled) {
        try {
            final ConnectivityManager conman = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            final Class conmanClass = Class
                    .forName(conman.getClass().getName());
            final Field iConnectivityManagerField = conmanClass
                    .getDeclaredField("mService");
            iConnectivityManagerField.setAccessible(true);
            final Object iConnectivityManager = iConnectivityManagerField
                    .get(conman);
            final Class iConnectivityManagerClass = Class
                    .forName(iConnectivityManager.getClass().getName());
            final Method setMobileDataEnabledMethod = iConnectivityManagerClass
                    .getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
            setMobileDataEnabledMethod.setAccessible(true);
            return (boolean) setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
