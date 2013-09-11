package com.bernieeng.loki;

import android.app.KeyguardManager;
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

import java.util.List;

public class MainActivity extends PreferenceActivity {

    public static final String WIFI_NAME = "wifi_name";
    public static final String PASSWORD = "password";
    public static final String DISABLE_KEYGUARD = "disable_keyguard";
    public static final String FORCE_LOGIN = "force_login";
    private DevicePolicyManager mgr = null;
    private ComponentName cn = null;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        cn = new ComponentName(this, AdminReceiver.class);
        mgr = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        if (!mgr.isAdminActive(cn) || !prefs.contains(WIFI_NAME) || !prefs.contains(PASSWORD)) {
            Toast.makeText(this, getString(R.string.loki_disabled_warning), Toast.LENGTH_LONG).show();
        }
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
            findPreference(PASSWORD).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (TextUtils.isEmpty((String) newValue)) {
                    } else {
                        Toast.makeText(getActivity(), "PIN set to " + (String) newValue, Toast.LENGTH_SHORT).show();
                    }

                    return true;
                }
            });

            findPreference(WIFI_NAME).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivityForResult(new Intent(getActivity(), WifiListActivity.class), WifiListActivity.WIFI_PICK_REQUEST);
                    return true;
                }
            });
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (RESULT_OK == resultCode) {
                switch (requestCode) {
                    case WifiListActivity.WIFI_PICK_REQUEST:
                        final String ssid = data.getStringExtra(WifiListActivity.WIFI_PICK_RESULT);
                        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                        prefs.edit().putString(WIFI_NAME, ssid).commit();
                        Toast.makeText(getActivity(), "Safe WiFi defined as " + ssid, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        final String defValue = "";
        //retrieve user settings
        final String password = prefs.getString(MainActivity.PASSWORD, defValue);
        final String safeSsid = prefs.getString(MainActivity.WIFI_NAME, defValue);
        final boolean disableKeyguard = prefs.getBoolean(MainActivity.DISABLE_KEYGUARD, false);
        if (mgr.isAdminActive(cn) && prefs.contains(WIFI_NAME) && prefs.contains(PASSWORD)) {
            //check if we're connected to safe wifi, and do our stuff
            final WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (safeSsid.equals(Util.getSSID(connectionInfo))) {
                mgr.resetPassword(defValue, 0);
                if (disableKeyguard) {
                    KeyguardManager myKeyGuard = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
                    myKeyGuard.newKeyguardLock(null).disableKeyguard();
                }
            } else {
                //foreign SSID, enforce password
                mgr.resetPassword(password, 0);
            }
        }

    }
}
