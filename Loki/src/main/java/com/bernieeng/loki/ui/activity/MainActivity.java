package com.bernieeng.loki.ui.activity;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.bernieeng.loki.receiver.BluetoothMonService;
import com.bernieeng.loki.R;
import com.bernieeng.loki.model.UnlockType;
import com.bernieeng.loki.Util;
import com.bernieeng.loki.receiver.AdminReceiver;
import com.bernieeng.loki.service.ActivityRecognitionAlertService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.List;

public class MainActivity extends PreferenceActivity {

    public static final String WIFI_NAME = "wifi_name";
    public static final String PASSWORD = "password";
    public static final String DISABLE_KEYGUARD = "disable_keyguard";
    public static final String FORCE_LOGIN = "force_login";
    public static final String BT_NAME = "bt_name";
    public static final String BT_UNLOCK = "bt_unlock";
    public static final String WIFI_UNLOCK = "wifi_unlock";
    private static final String BT_ADDRESS = "bt_address";
    public static final String ACTIVITY_UNLOCK = "activity_unlock";
    public static final String ACTIVITY_UNLOCK_DELAY_DURATION = "activity_unlock_delay";

    private DevicePolicyManager mgr = null;
    private ComponentName cn = null;
    private SharedPreferences prefs;
    private static int isGooglePlayAvail = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        cn = new ComponentName(this, AdminReceiver.class);
        mgr = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        if (!mgr.isAdminActive(cn) || !prefs.contains(WIFI_NAME) || !prefs.contains(PASSWORD)) {
            Toast.makeText(this, getString(R.string.loki_disabled_warning), Toast.LENGTH_LONG).show();
        }

        isGooglePlayAvail = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mgr.isAdminActive(cn)) {
            Intent intent =
                    new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, cn);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    getString(R.string.admin_explanation));
            startActivity(intent);
        }
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.settings_headers, target);
    }

    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

            final Preference driveUnlockDelayPrefs = findPreference(ACTIVITY_UNLOCK_DELAY_DURATION);
            final Preference driveUnlockPref = findPreference(ACTIVITY_UNLOCK);
            if (isGooglePlayAvail == ConnectionResult.SUCCESS) {
                driveUnlockDelayPrefs.setEnabled(prefs.getBoolean(ACTIVITY_UNLOCK, false));
                driveUnlockPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        Boolean checked = (Boolean) newValue;
                        driveUnlockDelayPrefs.setEnabled(checked);
                        if (checked) {
                            getActivity().startService(new Intent(getActivity(), ActivityRecognitionAlertService.class));
                        } else {
                            getActivity().stopService(new Intent(getActivity(), ActivityRecognitionAlertService.class));
                            Util.setPassword(getActivity(), prefs.getString(PASSWORD, ""), UnlockType.PREF_CHANGE);
                        }
                        return true;
                    }
                });
            } else {
                GooglePlayServicesUtil.getErrorDialog(isGooglePlayAvail, getActivity(), 0).show();
                driveUnlockDelayPrefs.setEnabled(false);
                driveUnlockPref.setEnabled(false);
                driveUnlockDelayPrefs.setSummary(getString(R.string.gplay_error_msg));
            }

            findPreference(PASSWORD).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (!TextUtils.isEmpty((String) newValue)) {
                        Toast.makeText(getActivity(), "PIN set to " + (String) newValue, Toast.LENGTH_SHORT).show();
                    }

                    return true;
                }
            });

            final Preference wifipref = findPreference(WIFI_NAME);
            wifipref.setEnabled(prefs.getBoolean(WIFI_UNLOCK, false));
            wifipref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivityForResult(new Intent(getActivity(), WifiListActivity.class), WifiListActivity.WIFI_PICK_REQUEST);
                    return true;
                }
            });

            findPreference(WIFI_UNLOCK).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Boolean checked = (Boolean) newValue;
                    wifipref.setEnabled(checked);
//                    prefs.edit().putBoolean(WIFI_UNLOCK, checked);
                    if (checked) {
                        wifipref.getOnPreferenceClickListener().onPreferenceClick(wifipref);
                    } else {
                        Util.setPassword(getActivity(), prefs.getString(PASSWORD, ""), UnlockType.PREF_CHANGE);
                    }

                    return true;
                }
            });

            final Preference btpref = findPreference(BT_NAME);
            btpref.setEnabled(prefs.getBoolean(BT_UNLOCK, false));
            btpref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivityForResult(new Intent(getActivity(), BluetoothListActivity.class), BluetoothListActivity.BT_PICK_REQUEST);
                    return true;
                }
            });

            findPreference(BT_UNLOCK).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Boolean checked = (Boolean) newValue;
                    btpref.setEnabled(checked);
//                    prefs.edit().putBoolean(BT_UNLOCK, checked);
                    if (checked) {
                        getActivity().startService(new Intent(getActivity(), BluetoothMonService.class));
                        btpref.getOnPreferenceClickListener().onPreferenceClick(btpref);
                    } else {
                        Util.setPassword(getActivity(), prefs.getString(PASSWORD, ""), UnlockType.PREF_CHANGE);
                        getActivity().stopService(new Intent(getActivity(), BluetoothMonService.class));
                    }

                    return true;
                }
            });
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (RESULT_OK == resultCode) {
                final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                switch (requestCode) {

                    case WifiListActivity.WIFI_PICK_REQUEST:
                        final String ssid = data.getStringExtra(WifiListActivity.WIFI_PICK_RESULT);
                        prefs.edit().putString(WIFI_NAME, ssid).commit();
                        Toast.makeText(getActivity(), "Safe WiFi defined as " + ssid, Toast.LENGTH_SHORT).show();
                        maybeUnlockViaWifi(prefs);
                        break;

                    case BluetoothListActivity.BT_PICK_REQUEST:
                        final String btNameAndAddress = data.getStringExtra(BluetoothListActivity.BT_PICK_RESULT);
                        String btName = btNameAndAddress.substring(0, btNameAndAddress.indexOf("("));
                        String btAddress = btNameAndAddress.substring(btNameAndAddress.indexOf("(") + 1, btNameAndAddress.indexOf(")"));
                        prefs.edit().putString(BT_NAME, btName).commit();
                        prefs.edit().putString((BT_ADDRESS), btAddress).commit();

                        Toast.makeText(getActivity(), "Safe BT defined as " + btName, Toast.LENGTH_SHORT).show();
                        //maybeUnlockUsingBt(prefs, btName);
                }
            }
        }

        private void maybeUnlockViaWifi(SharedPreferences prefs) {
            if (Util.settingsOkay(getActivity())) {
                final WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
                if (wifiManager.isWifiEnabled()) {
                    final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
                    final String safeSsid = prefs.getString(MainActivity.WIFI_NAME, "");
                    final boolean shouldUnlock = prefs.getBoolean(MainActivity.WIFI_UNLOCK, false);
                    final boolean disableKeyguard = prefs.getBoolean(MainActivity.DISABLE_KEYGUARD, false);

                    if (safeSsid.equals(Util.getSSID(connectionInfo)) && shouldUnlock) {
                        Util.unSetPassword(getActivity(), disableKeyguard, UnlockType.WIFI);
                    } else {
                        Util.setPassword(getActivity(), prefs.getString(PASSWORD, ""), UnlockType.WIFI);
                    }
                }
            }
        }
    }
}