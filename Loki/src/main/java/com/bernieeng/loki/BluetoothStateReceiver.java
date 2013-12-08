package com.bernieeng.loki;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by ebernie on 9/13/13.
 */
public class BluetoothStateReceiver extends BroadcastReceiver {

    public static final String DEF_VALUE = "";
    private String TAG = "BluetoothStateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final String password = prefs.getString(MainActivity.PASSWORD, DEF_VALUE);
        final boolean disableKeyguard = prefs.getBoolean(MainActivity.DISABLE_KEYGUARD, false);
        final String btName = prefs.getString(MainActivity.BT_NAME, DEF_VALUE);
        final boolean shouldUnlock = prefs.getBoolean(MainActivity.BT_UNLOCK, false);

        final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        String action = intent.getAction();
        if (BluetoothDevice.ACTION_ACL_CONNECTED.equalsIgnoreCase(action) && device.getName().equals(btName) && shouldUnlock) {
            Util.unSetPassword(context, disableKeyguard, UnlockType.BLUETOOTH);
            Log.d(TAG, "Unlocking due to BT");
        }
        if ((BluetoothDevice.ACTION_ACL_DISCONNECTED.equalsIgnoreCase(action) || BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equalsIgnoreCase(action)) && device.getName().equals(btName)) {
            Util.setPassword(context, password, UnlockType.BLUETOOTH);
            Log.d(TAG, "Locking due to BT");
        }
    }
}