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

import com.bernieeng.loki.model.UnlockType;
import com.bernieeng.loki.ui.activity.MainActivity;
import com.bernieeng.loki.receiver.AdminReceiver;
import com.bernieeng.loki.wizardpager.model.PinSetupPage;
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

    private static final String PREF_KEY_SAFE_WIFI = "loki.wifi";
    private static final String PREF_KEY_SAFE_BT = "loki.bt";
    private static final String PREF_KEY_DRIVE_UNLOCK = "loki.drive";
    public static final String PREF_KEY_PASS_OR_PIN = "loki.pass";

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

    public static String getPinOrPassword(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_KEY_PASS_OR_PIN, null);
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
        return mgr.isAdminActive(cn);
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

    public static void addSafeWifi(Context context, String name) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> safeWifiNetworkNames = prefs.getStringSet(PREF_KEY_SAFE_WIFI, null);
        if (safeWifiNetworkNames == null) {
            safeWifiNetworkNames = new HashSet<String>();
        }
        safeWifiNetworkNames.add(name);
        prefs.edit().putStringSet(PREF_KEY_SAFE_WIFI, safeWifiNetworkNames).commit();

    }

    public static void addSafeBluetooth(Context context, String name) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> safeBtDeviceNames = prefs.getStringSet(PREF_KEY_SAFE_BT, null);
        if (safeBtDeviceNames == null) {
            safeBtDeviceNames = new HashSet<String>();
        }
        safeBtDeviceNames.add(name);
        prefs.edit().putStringSet(PREF_KEY_SAFE_BT, safeBtDeviceNames).commit();
    }

    public static void removeSafeBluetooth(Context context, String name) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> safeBtDeviceNames = prefs.getStringSet(PREF_KEY_SAFE_BT, null);
        if (safeBtDeviceNames != null) {
            safeBtDeviceNames.remove(name);
            prefs.edit().putStringSet(PREF_KEY_SAFE_BT, safeBtDeviceNames).commit();
        }
    }

    public static void removeSafeWifi(Context context, String name) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> safeWifiNetworkNames = prefs.getStringSet(PREF_KEY_SAFE_WIFI, null);
        if (safeWifiNetworkNames != null) {
            safeWifiNetworkNames.remove(name);
            prefs.edit().putStringSet(PREF_KEY_SAFE_WIFI, safeWifiNetworkNames).commit();
        }
    }

    public static boolean isSafeNetwork(Context context, String networkName) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> safeWifiNetworkNames = prefs.getStringSet(PREF_KEY_SAFE_WIFI, null);
        if (safeWifiNetworkNames != null) {
            return safeWifiNetworkNames.contains(networkName);
        }
        return false;
    }

    public static boolean isSafeBtDevice(Context context, String deviceName) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> safeBtDeviceNames = prefs.getStringSet(PREF_KEY_SAFE_BT, null);
        if (safeBtDeviceNames != null) {
            return safeBtDeviceNames.contains(deviceName);
        }
        return false;
    }

    public static void enableDriveUnlock(Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean(PREF_KEY_DRIVE_UNLOCK, true).commit();
    }

    public static void disableDriveUnlock(Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean(PREF_KEY_DRIVE_UNLOCK, false).commit();
    }

    public static boolean isDriveUnlockEnabled(Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(PREF_KEY_DRIVE_UNLOCK, false);
    }

}
