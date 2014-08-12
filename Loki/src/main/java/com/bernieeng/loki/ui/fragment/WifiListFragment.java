package com.bernieeng.loki.ui.fragment;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.bernieeng.loki.R;
import com.bernieeng.loki.Util;
import com.bernieeng.loki.ui.activity.WifiListActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ebernie on 9/11/13.
 */
public class WifiListFragment extends ListFragment implements AdapterView.OnItemClickListener {

    private ArrayList<String> configuredNetworkNames = new ArrayList<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WifiManager wifiManager = (WifiManager) getActivity()
                .getSystemService(Service.WIFI_SERVICE);
        final List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();

        if (configuredNetworks != null && !configuredNetworks.isEmpty()) {
            configuredNetworkNames = new ArrayList<String>(configuredNetworks.size());
            for (int i = 0; i < configuredNetworks.size(); i++) {
                WifiConfiguration configuration = configuredNetworks.get(i);
                configuredNetworkNames.add(Util.escapeSSID(configuration.SSID));
            }
        } else {
            showErrorDialog();
        }
    }

    private void showErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.no_wifi_networks_configured)).setTitle(getString(R.string.wifi_setup)).setIcon(android.R.drawable.ic_dialog_alert).setNegativeButton(R.string.quit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getActivity().setResult(getActivity().RESULT_CANCELED);
                getActivity().finish();
            }
        }).setCancelable(false).setNeutralButton("Setup WiFi", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(
                        WifiManager.ACTION_PICK_WIFI_NETWORK));
                getActivity().setResult(getActivity().RESULT_CANCELED);
            }
        });
        builder.show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setListAdapter(new ArrayAdapter<String>(inflater.getContext(), android.R.layout.simple_list_item_1, configuredNetworkNames));
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        getListView().setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent completed = new Intent();
        completed.putExtra(WifiListActivity.WIFI_PICK_RESULT, configuredNetworkNames.get(position)); //TODO set bssid name
        getActivity().setResult(getActivity().RESULT_OK, completed);
        getActivity().finish();
    }

}
