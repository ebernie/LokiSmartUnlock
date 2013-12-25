/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bernieeng.loki.wizardpager;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import com.bernieeng.loki.Util;
import com.bernieeng.loki.wizardpager.model.AbstractWizardModel;
import com.bernieeng.loki.wizardpager.model.MultipleFixedChoicePage;
import com.bernieeng.loki.wizardpager.model.PageList;
import com.bernieeng.loki.wizardpager.model.PinSetupPage;
import com.bernieeng.loki.wizardpager.model.SingleFixedChoicePage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class LokiWizardModel extends AbstractWizardModel {

    public LokiWizardModel(Context context) {
        super(context);
    }

    @Override
    protected PageList onNewRootPageList(Context context) {

        PageList pageList = new PageList();
        pageList.add(new PinSetupPage(this, "PIN setup").setRequired(true));

        MultipleFixedChoicePage wifiPage = buildWiFiPage(context);
        pageList.add(wifiPage);

        final MultipleFixedChoicePage btPage = buildBluetoothPage();
        if (btPage != null) {
            pageList.add(btPage);
        }

        pageList.add(new SingleFixedChoicePage(this, "In-vehicle unlock").setChoices("Enable in-vehicle unlock", "Disable in-vehicle unlock").setRequired(true));

        return pageList;
    }

    private MultipleFixedChoicePage buildBluetoothPage(){
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //BT is not supported on this device
        if (bluetoothAdapter == null) {
            return null;
        }
        MultipleFixedChoicePage btPage = new MultipleFixedChoicePage(this, "Choose safe device");
        Set<BluetoothDevice> pairedDevices
                = bluetoothAdapter.getBondedDevices();
        if (pairedDevices != null && !pairedDevices.isEmpty()) {
            ArrayList<String> pairedBluetoothDevices = new ArrayList<String>(pairedDevices.size());
            final Iterator<BluetoothDevice> iterator = pairedDevices.iterator();
            while (iterator.hasNext()) {
                final BluetoothDevice device = iterator.next();
                StringBuilder sb = new StringBuilder(device.getName());
                sb.append("(").append(device.getAddress()).append(")");
                pairedBluetoothDevices.add(sb.toString());
            }
            btPage.setChoices(pairedBluetoothDevices);
        }
        return btPage;
    }

    private MultipleFixedChoicePage buildWiFiPage(Context context) {
        MultipleFixedChoicePage wifiPage = new MultipleFixedChoicePage(this, "Choose safe network");
        WifiManager wifiManager = (WifiManager) context
                .getSystemService(Service.WIFI_SERVICE);
        final List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
        if (configuredNetworks != null && !configuredNetworks.isEmpty()) {
            ArrayList<String> configuredNetworkNames = new ArrayList<String>(configuredNetworks.size());
            for (int i = 0; i < configuredNetworks.size(); i++) {
                WifiConfiguration configuration = configuredNetworks.get(i);
                configuredNetworkNames.add(Util.escapeSSID(configuration.SSID));
            }
            wifiPage.setChoices(configuredNetworkNames);
        }
        return wifiPage;
    }
}
