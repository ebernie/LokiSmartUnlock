package com.bernieeng.loki;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by ebernie on 9/13/13.
 */
public class BluetoothListFragment extends ListFragment implements AdapterView.OnItemClickListener {

    private static final int REQUEST_ENABLE_BT = 13371;
    private ArrayList<String> pairedBluetoothDevices = new ArrayList<String>();
    private BluetoothAdapter bluetoothAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            showPairedDevices();
        }

    }

    private void showPairedDevices() {
        Set<BluetoothDevice> pairedDevices
                = bluetoothAdapter.getBondedDevices();
        if (pairedDevices != null && !pairedDevices.isEmpty()) {
            pairedBluetoothDevices = new ArrayList<String>(pairedDevices.size());
            final Iterator<BluetoothDevice> iterator = pairedDevices.iterator();
            while (iterator.hasNext()) {
                final BluetoothDevice device = iterator.next();
                StringBuilder sb = new StringBuilder(device.getName());
                sb.append("(").append(device.getAddress()).append(")");
                pairedBluetoothDevices.add(sb.toString());
            }
            setListAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, pairedBluetoothDevices));
        } else {
            showErrorDialog();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (getActivity().RESULT_OK == resultCode && requestCode == REQUEST_ENABLE_BT) {
            if (bluetoothAdapter.isEnabled()) {
                showPairedDevices();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setListAdapter(new ArrayAdapter<String>(inflater.getContext(), android.R.layout.simple_list_item_1, pairedBluetoothDevices));
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        getListView().setOnItemClickListener(this);
    }

    private void showErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("No Bluetooth devices paired. Run this setup again once you've paired a device").setTitle(getString(R.string.wifi_setup)).setIcon(android.R.drawable.ic_dialog_alert).setNegativeButton(R.string.quit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getActivity().setResult(getActivity().RESULT_CANCELED);
                getActivity().finish();
            }
        }).setCancelable(false).setNeutralButton(getString(R.string.pair), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(enableBtIntent);
                getActivity().setResult(getActivity().RESULT_CANCELED);

            }
        });
        builder.show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent completed = new Intent();
        completed.putExtra(BluetoothListActivity.BT_PICK_RESULT, pairedBluetoothDevices.get(position));
        getActivity().setResult(getActivity().RESULT_OK, completed);
        getActivity().finish();
    }
}
