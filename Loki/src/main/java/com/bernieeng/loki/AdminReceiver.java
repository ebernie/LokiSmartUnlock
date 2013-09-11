package com.bernieeng.loki;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by ebernie on 9/9/13.
 */
public class AdminReceiver extends DeviceAdminReceiver {

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        return context.getString(R.string.admin_disabled_message);
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        // lock the device with set PIN if admin rights is revoked
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final String password = prefs.getString(MainActivity.PASSWORD, "1234");
        DevicePolicyManager mgr = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        mgr.resetPassword(password, 0);
        mgr.lockNow();
    }
}
