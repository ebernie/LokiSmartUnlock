package com.bernieeng.loki.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.bernieeng.loki.Util;
import com.bernieeng.loki.event.LockEvent;
import com.bernieeng.loki.event.UnlockEvent;
import com.bernieeng.loki.model.UnlockType;

import de.greenrobot.event.EventBus;

/**
 * Created by ebernie on 9/9/13.
 */
public class WifiStateReceiver extends BroadcastReceiver {


    public void onReceive(Context context, Intent intent) {

        NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            if (networkInfo.isConnectedOrConnecting()) {
                // Wifi is connected
                final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                final WifiInfo connectionInfo = wifiManager.getConnectionInfo();

                if (Util.isSafeNetwork(context, Util.getSSID(connectionInfo))) {
                    EventBus.getDefault().post(new UnlockEvent(UnlockType.WIFI, ""));
                }
            } else {
                //foreign SSID, enforce password
                EventBus.getDefault().post(new LockEvent(UnlockType.WIFI, "", false));
            }
        } else if (!networkInfo.isConnected()) {
            // Wifi is disconnected
            EventBus.getDefault().post(new LockEvent(UnlockType.WIFI, "", false));
        }
    }
}