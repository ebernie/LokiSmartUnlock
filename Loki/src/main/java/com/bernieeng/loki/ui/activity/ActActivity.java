package com.bernieeng.loki.ui.activity;

import android.app.ActionBar;
import android.content.SharedPreferences;
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

import com.bernieeng.loki.Util;
import com.kofikodr.loki.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by ebernie on 8/19/14.
 */
public class ActActivity extends FragmentActivity {

    private static ArrayList<String> selectedItems = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //re-using layout from wifi select activity
        setContentView(R.layout.activity_wifi_select);
        ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("");
        actionBar.setIcon(R.drawable.ic_launcher_white);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ActSelectFragment())
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                final Set<String> set = new HashSet<String>();
                set.addAll(selectedItems);
                preferences.edit().putStringSet(getString(R.string.title_activity_unlock), set).commit();
                finish();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    public static class ActSelectFragment extends ListFragment {

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
            preferences.edit().putStringSet(getString(R.string.title_activity_unlock), set).commit();
            getActivity().finish();
        }

        @Override
        public void onResume() {
            super.onResume();
            final ArrayList<String> actNames = Util.getAllPossibleUnlockActivities(getActivity());
            title.setText(R.string.title_activity_unlock);
            if (actNames != null) {
                populateListView(actNames);
            }
        }

        private void populateListView(final ArrayList<String> activityNames) {
            final ListView listView = getListView();
            setListAdapter(new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_list_item_multiple_choice,
                    android.R.id.text1,
                    activityNames));
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

            final Set<String> set = Util.getUnlockActivities(getActivity());

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
                        for (int i = 0; i < activityNames.size(); i++) {
                            if (selectedSet.contains(activityNames.get(i))) {
                                listView.setItemChecked(i, true);
                            }
                        }
                    }
                });
            }
        }

    }
}
