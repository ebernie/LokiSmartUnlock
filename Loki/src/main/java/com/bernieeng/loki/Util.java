package com.bernieeng.loki;

import android.app.KeyguardManager;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by ebernie on 9/11/13.
 */
public class Util {
    public static final String DEF_VALUE = "";
    private static DevicePolicyManager mgr;
    private static KeyguardManager.KeyguardLock keyguardLock;
    private static Set<UnlockType> unlocksEngaged = new HashSet<UnlockType>();

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

    public static void setPassword(Context context, String password, UnlockType key) {
        DevicePolicyManager mgr = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName cn = new ComponentName(context, AdminReceiver.class);
        if (mgr.isAdminActive(cn)) {

            // 1. if there are any kinds of locks engaged, we assume that the user would not want to lock his phone
            // e.g. i'm driving but disengages BT while in the car, phone should not be locked
            // 2. if all unlocks have been removed, then any event that triggers a set password must lock the phone
            if ((unlocksEngaged.size() == 1 && unlocksEngaged.contains(key)) || unlocksEngaged.isEmpty() || UnlockType.PREF_CHANGE == key) {
                // locks phone
                mgr.resetPassword(password, 0);
            }
            // we just remove the 'key' from list of unlocks
            unlocksEngaged.remove(key);
        }
    }

    public static boolean settingsOkay(Context context) {
        mgr = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName cn = new ComponentName(context, AdminReceiver.class);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return mgr.isAdminActive(cn) && prefs.contains(MainActivity.WIFI_NAME) && prefs.contains(MainActivity.PASSWORD);
    }

    public static void unSetPassword(Context context, boolean disableKeyguard, UnlockType key) {
        if (settingsOkay(context)) {
            unlocksEngaged.add(key);
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

    /**
     * When supplied with the integer representation of the activity returns the activity as friendly string
     */
    public static String getDetectedActivityFriendlyName(int detected_activity_type) {
        switch (detected_activity_type) {
            case DetectedActivity.IN_VEHICLE:
                return "in vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "on bike";
            case DetectedActivity.ON_FOOT:
                return "on foot";
            case DetectedActivity.TILTING:
                return "tilting";
            case DetectedActivity.STILL:
                return "still";
            default:
                return "unknown";
        }
    }

    public static ArrayList<String> getWifiNetworkNames(Context context) {
        WifiManager wifiManager = (WifiManager) context
                .getSystemService(Service.WIFI_SERVICE);
        final List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
        if (configuredNetworks == null) {
            return null;
        }
        ArrayList<String> configuredNetworkNames = new ArrayList<String>(configuredNetworks.size());
        if (configuredNetworks != null && !configuredNetworks.isEmpty()) {
            for (int i = 0; i < configuredNetworks.size(); i++) {
                WifiConfiguration configuration = configuredNetworks.get(i);
                configuredNetworkNames.add(Util.escapeSSID(configuration.SSID));
            }
        }
        return configuredNetworkNames;
    }

    public static ArrayList<String> getBluetoothPairedDevices() {
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //BT is not supported on this device
        if (bluetoothAdapter == null) {
            return null;
        }
        Set<BluetoothDevice> pairedDevices
                = bluetoothAdapter.getBondedDevices();
        ArrayList<String> pairedBluetoothDevices = new ArrayList<String>(pairedDevices.size());

        if (pairedDevices != null && !pairedDevices.isEmpty()) {
            final Iterator<BluetoothDevice> iterator = pairedDevices.iterator();
            while (iterator.hasNext()) {
                final BluetoothDevice device = iterator.next();
                pairedBluetoothDevices.add(device.getName());
            }
        }
        return pairedBluetoothDevices;
    }

    public static boolean isAdminRightsGranted(Context context) {
        final DevicePolicyManager dm = (DevicePolicyManager) context.getSystemService("device_policy");
        final ComponentName cn = new ComponentName(context, AdminReceiver.class);
        return dm.isAdminActive(cn);
    }
}
