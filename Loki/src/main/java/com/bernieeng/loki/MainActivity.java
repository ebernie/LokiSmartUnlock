package com.bernieeng.loki;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends PreferenceActivity {

    public static final String WIFI_NAME = "wifi_name";
    public static final String PASSWORD = "password";
    public static final String DISABLE_KEYGUARD = "disable_keyguard";
    private DevicePolicyManager mgr = null;
    private ComponentName cn = null;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        cn = new ComponentName(this, AdminReceiver.class);
        mgr = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        if (mgr.isAdminActive(cn) && prefs.contains(WIFI_NAME) && prefs.contains(PASSWORD)) {
            mgr.resetPassword(prefs.getString(PASSWORD, ""), 0);
        } else {
            Toast.makeText(this, getString(R.string.loki_disabled_warning), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mgr.isAdminActive(cn)) {
            Intent intent=
                    new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, cn);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    getString(R.string.device_admin_explanation));
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
                    Toast.makeText(getActivity(), "PIN set to " + (String) newValue, Toast.LENGTH_LONG).show();
                    return true;
                }
            });

            findPreference(WIFI_NAME).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Toast.makeText(getActivity(), "Safe WiFi defined as " + (String) newValue, Toast.LENGTH_LONG).show();
                    return true;
                }
            });
        }
    }

}
