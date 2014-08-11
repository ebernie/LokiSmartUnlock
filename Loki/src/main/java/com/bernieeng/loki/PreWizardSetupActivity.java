package com.bernieeng.loki;

import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import com.bernieeng.loki.wizardpager.SetupWizardActivity;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class PreWizardSetupActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault("fonts/Roboto-Condensed.ttf");
        setContentView(R.layout.activity_pre_wizard_setup);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new CalligraphyContextWrapper(newBase));
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private static final int ACTION_REQUEST_ENABLE = 1982379123;

        @InjectView(R.id.tgl_admin)
        Switch adminSwitch;
        @InjectView(R.id.tgl_bluetooth)
        Switch bt;
        @InjectView(R.id.tgl_wifi)
        Switch wifi;
        @InjectView(R.id.btn_setup)
        Button setup;
        @InjectView(R.id.txt_bt)
        TextView txtBt;
        @InjectView(R.id.txt_wifi)
        TextView txtWifi;
        @InjectView(R.id.txt_admin_rights)
        TextView txtAdmin;

        private BluetoothActivatedReceiver btReceiver;
        private WifiActivatedReceiver wifiReceiver;
        private BluetoothAdapter btAdapter;
        private WifiManager wifiManager;

        public PlaceholderFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            btAdapter = BluetoothAdapter.getDefaultAdapter();
            wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_pre_wizard_setup, container, false);
            ButterKnife.inject(this, rootView);
            return rootView;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            adminSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final ComponentName cn = new ComponentName(getActivity(), AdminReceiver.class);
                    final Intent intent =
                            new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, cn);
                    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                            getString(R.string.admin_explanation));
                    startActivity(intent);
                }
            });

            bt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, ACTION_REQUEST_ENABLE);
                }
            });

            wifi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
                }
            });

            setup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getActivity(), SetupWizardActivity.class));
                }
            });
        }

        @Override
        public void onStart() {
            super.onStart();
            if (btReceiver == null) {
                btReceiver = new BluetoothActivatedReceiver();
            }
            IntentFilter btif = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            getActivity().registerReceiver(btReceiver, btif);

            if (wifiReceiver == null) {
                wifiReceiver = new WifiActivatedReceiver();
            }
            IntentFilter wif = new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
            getActivity().registerReceiver(wifiReceiver, wif);
        }

        @Override
        public void onStop() {
            super.onStop();
            if (btReceiver != null) {
                getActivity().unregisterReceiver(btReceiver);
                btReceiver = null;
            }

            if (wifiReceiver != null) {
                getActivity().unregisterReceiver(wifiReceiver);
                wifiReceiver = null;
            }
        }

        @Override
        public void onResume() {

            super.onResume();

            if (Util.isAdminRightsGranted(getActivity())) {
                onAdminGranted();
            } else {
                onAdminUngranted();
            }

            if (btAdapter == null) {
                // Device does not support Bluetooth
                bt.setEnabled(false);
            } else {
                if (btAdapter.isEnabled()) {
                    onBtEnabled();
                } else {
                    onBtDisabled();
                }
            }

            if (wifiManager.isWifiEnabled()) {
                onWifiEnabled();
            } else {
                onWiFiDisabled();
            }

        }

        private void onWifiEnabled() {
            txtWifi.setText(getString(R.string.wifi_enabled));
            wifi.setClickable(false);
            wifi.setChecked(true);
        }

        private void onWiFiDisabled() {
            txtWifi.setText(getString(R.string.enable_wifi));
            wifi.setClickable(true);
            wifi.setChecked(false);
        }

        private void onAdminUngranted() {
            setup.setEnabled(false);
            setup.setTextColor(getResources().getColor(R.color.text_light));
            setup.setText(R.string.no_admin_rights);
            adminSwitch.setClickable(true);
            adminSwitch.setChecked(false);
            txtAdmin.setText(R.string.grant_loki_admin_rights);
        }

        private void onAdminGranted() {
            setup.setEnabled(true);
            setup.setTextColor(getResources().getColor(android.R.color.white));
            setup.setText(getString(R.string.setup_loki));
            adminSwitch.setClickable(false);
            adminSwitch.setChecked(true);
            txtAdmin.setText(getString(R.string.admin_rights_granted));
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == ACTION_REQUEST_ENABLE) {
                if (resultCode == RESULT_OK) {
                    onBtEnabled();
                }
            }
        }

        private void onBtEnabled() {
            bt.setChecked(true);
            bt.setClickable(false);
            txtBt.setText(getString(R.string.bt_enabled));
        }

        private void onBtDisabled() {
            bt.setChecked(false);
            bt.setClickable(true);
            txtBt.setText(getString(R.string.enable_bt));
        }

        class BluetoothActivatedReceiver extends BroadcastReceiver {

            @Override
            public void onReceive(Context context, Intent intent) {
                if (btAdapter.isEnabled()) {
                    onBtEnabled();
                } else {
                    onBtDisabled();
                }
            }
        }

        class WifiActivatedReceiver extends BroadcastReceiver {

            @Override
            public void onReceive(Context context, Intent intent) {
                if (wifiManager.isWifiEnabled()) {
                    onWifiEnabled();
                } else {
                    onWiFiDisabled();
                }
            }
        }
    }

}
