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

import android.content.Context;

import com.bernieeng.loki.Util;
import com.bernieeng.loki.wizardpager.model.AbstractWizardModel;
import com.bernieeng.loki.wizardpager.model.MultipleFixedChoicePage;
import com.bernieeng.loki.wizardpager.model.PageList;
import com.bernieeng.loki.wizardpager.model.PinSetupPage;
import com.kofikodr.loki.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class LokiWizardModel extends AbstractWizardModel {

    public static final String PREF_KEYS = "drive";
    public static final String KEY_PIN_OR_PASS = "Secure Using Password or PIN";
    public static String KEY_BT_UNLOCK;
    public static String KEY_WIFI_UNLOCK;
    public static String KEY_DRIVE_UNLOCK;

    public LokiWizardModel(Context context) {
        super(context);
        KEY_BT_UNLOCK = mContext.getString(R.string.title_bt_unlock);
        KEY_WIFI_UNLOCK = mContext.getString(R.string.title_wifi_unlock);
        KEY_DRIVE_UNLOCK = mContext.getString(R.string.title_vehicle_unlock);
    }

    @Override
    protected PageList onNewRootPageList(Context context) {
        PageList pageList = new PageList();

//        pageList.add(new SingleFixedChoicePage(this, KEY_PIN_OR_PASS).setChoices("PIN", "Password").setRequired(true));
//        pageList.add(new PinSetupPage(this, context.getString(R.string.title_pin_setup)).setRequired(true));
        MultipleFixedChoicePage wifiPage = buildWiFiPage(context);
        if (wifiPage != null) {
            pageList.add(wifiPage);
        }
        final MultipleFixedChoicePage btPage = buildBluetoothPage();
        if (btPage != null) {
            pageList.add(btPage);
        }

        final Set<String> allPossibleSafeActivities = new HashSet<String>(1);
        allPossibleSafeActivities.add(context.getString(R.string.enable_in_vehicle_unlock));
        Util.saveAllPossibleSafeActivitiesList(mContext, allPossibleSafeActivities);
        pageList.add(new MultipleFixedChoicePage(this, context.getString(R.string.title_activity_unlock)).setChoices(new ArrayList<String>(allPossibleSafeActivities)).setRequired(false));
        return pageList;
    }

    private MultipleFixedChoicePage buildBluetoothPage() {
        MultipleFixedChoicePage btPage = new MultipleFixedChoicePage(this, mContext.getString(R.string.title_bt_unlock));
        final ArrayList<String> devices = Util.getBluetoothPairedDevices();
        if (devices != null && !devices.isEmpty()) {
            btPage.setChoices(devices);
            return btPage;
        } else {
            return null;
        }
    }

    private MultipleFixedChoicePage buildWiFiPage(Context context) {
        MultipleFixedChoicePage wifiPage = new MultipleFixedChoicePage(this, context.getString(R.string.title_wifi_unlock));
        final ArrayList<String> wifiNetworkNames = Util.getWifiNetworkNames(context);
        if (wifiNetworkNames != null && !wifiNetworkNames.isEmpty()) {
            wifiPage.setChoices(wifiNetworkNames);
            return wifiPage;
        } else {
            return null;
        }
    }
}
