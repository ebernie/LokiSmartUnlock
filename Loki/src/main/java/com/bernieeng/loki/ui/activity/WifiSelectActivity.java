package com.bernieeng.loki.ui.activity;

import android.app.ActionBar;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

    private static ArrayList<String> selectedItems = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_select);
        getActionBar().setIcon(R.drawable.ic_launcher_white);
        WifiManager wifiManager = (WifiManager) this
                .getSystemService(Service.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            final AlertDialog dialog = new AlertDialog.Builder(this).setMessage("Oops, you have WiFi. I can't proceed unless you enable them.").setNeutralButton("WiFi Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
                    intent.putExtra("only_access_points", true);
                    intent.putExtra("extra_prefs_show_button_bar", true);
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

        ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                final Set<String> set = new HashSet<String>();
                set.addAll(selectedItems);
                preferences.edit().putStringSet(getString(R.string.title_wifi_unlock), set).commit();
                finish();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    public static class WifiSelectFragment extends ListFragment {

        @InjectView(android.R.id.title)
        TextView title;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_page, container, false);
            ButterKnife.inject(this, rootView);
            setHasOptionsMenu(true);
            return rootView;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (getListView().isItemChecked(position)) {
                        selectedItems.add((String) parent.getItemAtPosition(position));
                    } else {
                        selectedItems.remove(parent.getItemAtPosition(position));
                    }
                }
            });
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            menu.clear();
            inflater.inflate(R.menu.wifi_select, menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_save_wifi:
                    saveEntries();
                    return true;
            }
            return super.onOptionsItemSelected(item);
        }

        private void saveEntries() {
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            final Set<String> set = new HashSet<String>();
            set.addAll(selectedItems);
            preferences.edit().putStringSet(getString(R.string.title_wifi_unlock), set).commit();
            getActivity().finish();
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
                        selectedItems = new ArrayList<String>(set.size());
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
