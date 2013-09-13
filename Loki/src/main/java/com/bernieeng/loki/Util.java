package com.bernieeng.loki;

import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.preference.PreferenceManager;
import android.text.TextUtils;

/**
 * Created by ebernie on 9/11/13.
 */
public class Util {
    public static final String DEF_VALUE = "";
    private static DevicePolicyManager mgr;
    private static KeyguardManager.KeyguardLock keyguardLock;

    public static String getSSID(WifiInfo connectionInfo) {
        String ssid = null;
        if (connectionInfo != null && !TextUtils.isEmpty(connectionInfo.getSSID())) {
            ssid = connectionInfo.getSSID();
            ssid = escapeSSID(ssid);
        }
        return ssid;
    }

    public static String escapeSSID(String ssid) {
        if (ssid.contains("\"")) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        return ssid;
    }

    public static void setPassword(Context context, String password) {
        DevicePolicyManager mgr = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName cn = new ComponentName(context, AdminReceiver.class);
        if (mgr.isAdminActive(cn)) {
            mgr.resetPassword(password, 0);
        }
    }

    public static boolean settingsOkay(Context context) {
        mgr = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName cn = new ComponentName(context, AdminReceiver.class);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return mgr.isAdminActive(cn) && prefs.contains(MainActivity.WIFI_NAME) && prefs.contains(MainActivity.PASSWORD);
    }

    public static void unSetPassword(Context context, boolean disableKeyguard) {
        if (settingsOkay(context)) {
            mgr.resetPassword(DEF_VALUE, 0);
            if (disableKeyguard) {
                if (keyguardLock == null) {
                    KeyguardManager myKeyGuard = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
                    keyguardLock = myKeyGuard.newKeyguardLock(null);
                }
                keyguardLock.disableKeyguard();
            }
        }
    }
}
