package com.bernieeng.loki;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * Created by ebernie on 9/9/13.
 */
public class WifiStateReceiver extends BroadcastReceiver {

    public static final String DEF_VALUE = "";
//    private static final String LOG_CAT = WifiStateReceiver.class.getName();

    public void onReceive(Context context, Intent intent) {
        DevicePolicyManager mgr = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName cn = new ComponentName(context, AdminReceiver.class);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (mgr.isAdminActive(cn) && prefs.contains(MainActivity.WIFI_NAME) && prefs.contains(MainActivity.PASSWORD)) {
            final String password = prefs.getString(MainActivity.PASSWORD, DEF_VALUE);
            final String safeSsid = prefs.getString(MainActivity.WIFI_NAME, DEF_VALUE);
            if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (networkInfo.isConnected()) {

                    // Wifi is connected
                    final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
                    if (safeSsid.equals(getSSID(connectionInfo))) {
                        mgr.resetPassword(DEF_VALUE, 0);
                        Toast.makeText(context, context.getString(R.string.password_disabled), Toast.LENGTH_SHORT).show();
//                        Log.d(LOG_CAT, "Safe Wifi connected");
                    } else {
                        //foreign SSID, enforce password
                        mgr.resetPassword(password, 0);
//                        Log.d(LOG_CAT, "Unable to match SSID " + getSSID(connectionInfo));
                        Toast.makeText(context, context.getString(R.string.password_enabled) + password, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Wifi is disconnected
                    mgr.resetPassword(password, 0);
                    Toast.makeText(context, context.getString(R.string.password_enabled) + password, Toast.LENGTH_SHORT).show();
//                    Log.d(LOG_CAT, "Wifi disconnected");
                }
            }
        }
    }

    private String getSSID(WifiInfo connectionInfo) {
        String ssid = null;
        if (connectionInfo != null && !TextUtils.isEmpty(connectionInfo.getSSID())) {
            ssid = connectionInfo.getSSID();
            if (ssid.contains("\"")) {
                ssid = ssid.substring(1, ssid.length() - 1);
            }
        }
        return ssid;
    }
}
