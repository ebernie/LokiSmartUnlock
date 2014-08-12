package com.bernieeng.loki.ui.activity;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bernieeng.loki.R;
import com.bernieeng.loki.Util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class WifiSelectActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_select);
        WifiManager wifiManager = (WifiManager) this
                .getSystemService(Service.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            final AlertDialog dialog = new AlertDialog.Builder(this).setMessage("Oops, you have WiFi. I can't proceed unless you enable them.").setNeutralButton("WiFi Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
                    intent.putExtra("only_access_points", true);
                    intent.putExtra("extra_prefs_show_button_bar", true);
//                    intent.putExtra("wifi_enable_next_on_connect", true);
                    startActivityForResult(intent, 1);
                    startActivity(intent);
                }
            }).create();
            dialog.show();
        } else {
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, new WifiSelectFragment())
                        .commit();
            }
        }
    }

    static class WifiSelectFragment extends ListFragment {

        @InjectView(android.R.id.title)
        TextView title;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_page, container, false);
            ButterKnife.inject(this, rootView);
            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();
            final ArrayList<String> wifiNetworkNames = Util.getWifiNetworkNames(getActivity());
            title.setText(R.string.title_wifi_unlock);
            if (wifiNetworkNames != null) {
                populateListView(wifiNetworkNames);
            }
        }

        private void populateListView(final ArrayList<String> wifiNetworkNames) {
            final ListView listView = getListView();
            setListAdapter(new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_list_item_multiple_choice,
                    android.R.id.text1,
                    wifiNetworkNames));
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

            final Set<String> set = preferences.getStringSet(getString(R.string.title_wifi_unlock), null);
            if (set != null) {
                // Pre-select currently selected items.
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {

                        ArrayList<String> selectedItems = new ArrayList<String>(set.size());
                        selectedItems.addAll(set);
                        if (selectedItems == null || selectedItems.size() == 0) {
                            return;
                        }

                        Set<String> selectedSet = new HashSet<String>(selectedItems);

                        for (int i = 0; i < wifiNetworkNames.size(); i++) {
                            if (selectedSet.contains(wifiNetworkNames.get(i))) {
                                listView.setItemChecked(i, true);
                            }
                        }
                    }
                });
            }
        }
    }

}
