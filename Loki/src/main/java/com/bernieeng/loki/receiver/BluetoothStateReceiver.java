package com.bernieeng.loki.receiver;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.bernieeng.loki.Util;
import com.bernieeng.loki.event.LockEvent;
import com.bernieeng.loki.event.UnlockEvent;
import com.bernieeng.loki.model.UnlockType;

import de.greenrobot.event.EventBus;

/**
 * Created by ebernie on 9/13/13.
 */
public class BluetoothStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        String action = intent.getAction();
        if (BluetoothDevice.ACTION_ACL_CONNECTED.equalsIgnoreCase(action) &&
                Util.isSafeBtDevice(context, device.getName())) {
            EventBus.getDefault().post(new UnlockEvent(UnlockType.BLUETOOTH, device.getName()));
        }
        if ((BluetoothDevice.ACTION_ACL_DISCONNECTED.equalsIgnoreCase(action) ||
                BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equalsIgnoreCase(action)) &&
                Util.isSafeBtDevice(context, device.getName())) {
            EventBus.getDefault().post(new LockEvent(UnlockType.BLUETOOTH, device.getName()));
        }
    }
}