connectme
==================
# USUAGE
------------------
Update: 2015-07-13


 **Settings** in `/mnt/sdcard/ConnectMe/config`

> Example 1: <`CHOOSE`>,SSID,PASSWORD

 * Split with ","
 * where <`CHOOSE`> can be openWifi | openWifiAp | openNetwork.
 * Means opening Wifi or WifiAp using given SSID and PASSWORD.
 * When choose openNetwork, it will smartly(for better one) connect to Wifi you specified or 3G .


> Example 2: <`CHOOSE`>

 * where <`CHOOSE`> can be closeWifi | closeWifiAp | close3G | closeNetwork | open3G .
 * Means closing Wifi ,WifiAp ,3G, all Networks or open 3G.


**Totally 8 choices .**


> Initiate App

Install:
```
adb [-s <Device Serial Num>] push connectme-android-1.0.0.apk data/local/tmp/com.monet.connectme
adb [-s <Device Serial Num>] shell pm install -r "/data/local/tmp/com.monet.connectme"
```

Launch:
```
adb [-s <Device Serial Num>] shell am start -n "com.monet.connectme/com.monet.connectme.activity.MainActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER
```
