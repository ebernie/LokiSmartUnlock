package com.bernieeng.loki;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;

/**
 * Created by ebernie on 9/9/13.
 */
public class WifiStateReceiver extends BroadcastReceiver {

    public static final String DEF_VALUE = "";

    public void onReceive(Context context, Intent intent) {

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final String password = prefs.getString(MainActivity.PASSWORD, DEF_VALUE);
        final boolean disableKeyguard = prefs.getBoolean(MainActivity.DISABLE_KEYGUARD, false);
        final String safeSsid = prefs.getString(MainActivity.WIFI_NAME, DEF_VALUE);
        final boolean shouldUnlock = prefs.getBoolean(MainActivity.WIFI_UNLOCK, false);

        NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            if (networkInfo.isConnectedOrConnecting()) {
                // Wifi is connected
                final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                final WifiInfo connectionInfo = wifiManager.getConnectionInfo();

                if (safeSsid.equals(Util.getSSID(connectionInfo)) && shouldUnlock) {
                    Util.unSetPassword(context, disableKeyguard);
                }
            } else {
                //foreign SSID, enforce password
                Util.setPassword(context, password);
            }
        } else if (!networkInfo.isConnected()) {
            // Wifi is disconnected
            Util.setPassword(context, password);
        }
    }
}