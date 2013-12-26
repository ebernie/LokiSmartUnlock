package com.bernieeng.loki;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bernieeng.loki.model.Unlock;
import com.bernieeng.loki.wizardpager.LokiWizardModel;
import com.bernieeng.loki.wizardpager.SetupWizardActivity;
import com.google.common.collect.HashMultimap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class HomeActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault("fonts/Roboto-Regular.ttf");
        setContentView(R.layout.activity_home);

        if (!PreferenceManager.getDefaultSharedPreferences(this).contains(LokiWizardModel.PREF_KEYS)) {
            startActivity(new Intent(this, SetupWizardActivity.class));
            this.finish();
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new UnlockListFragment())
                    .commit();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new CalligraphyContextWrapper(newBase));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(getApplicationContext(), SetupWizardActivity.class));
            return true;
        }

        if (id == R.id.action_add) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class UnlockListFragment extends ListFragment {

        private SharedPreferences preferences = null;
        private List<Unlock> unlockList;

        public UnlockListFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_home, container, false);
            ButterKnife.inject(this, rootView);
            preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            return rootView;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            final Set<String> keys = preferences.getStringSet(LokiWizardModel.PREF_KEYS, null);
            unlockList = new ArrayList<Unlock>();
            if (keys != null) {
                for (String key : keys) {
                    try {
                        String unlockPref = preferences.getString(key, null);
                        if (!TextUtils.isEmpty(unlockPref)) {
                            addUnlockToList(unlockList, key, unlockPref);
                        }
                    } catch (ClassCastException ex) {
                        Set<String> unlocks = preferences.getStringSet(key, null);
                        for (String unlockName : unlocks) {
                            addUnlockToList(unlockList, key, unlockName);
                        }
                    }
                }
            }

            setListAdapter(new UnlockListAdapter(unlockList, getActivity()));
            getListView().setDividerHeight(0);
        }

        private void addUnlockToList(List<Unlock> target, String prefKey, String unlockName) {
            if (prefKey.contains(getString(R.string.title_bt_unlock))) {
                target.add(new Unlock(UnlockType.BLUETOOTH, unlockName, prefKey));
            } else if (prefKey.contains(getString(R.string.title_wifi_unlock))) {
                target.add(new Unlock(UnlockType.WIFI, unlockName, prefKey));
            } else if (prefKey.contains(getString(R.string.title_vehicle_unlock))) {
                target.add(new Unlock(UnlockType.ACTIVITY, unlockName, prefKey));
            }
        }

        class UnlockListAdapter extends BaseAdapter {

            private final Set<Integer> HEADER_POSITIONS = new HashSet<Integer>();
            private List items = new ArrayList();
            private final int VIEW_TYPE_HEADER = 1;
            private final int VIEW_TYPE_ITEM = 2;
            private final int VIEW_TYPE_BOOLEAN_LOCK = 3;

            private final View.OnClickListener removeClickListener = new View.OnClickListener () {

                @Override
                public void onClick(View v) {
                    int position = getListView().getPositionForView(v);
                    Toast.makeText(context, "Selected position: " + position, Toast.LENGTH_SHORT).show();
                    unlockList.remove(getListAdapter().getItem(position));
                    setListAdapter(new UnlockListAdapter(unlockList, context));
                    final Unlock unlock = (Unlock) items.get(position);
                    try {
                        //test if this is a StringSet or just a String
                        preferences.getString(unlock.getKey(), null);
                        preferences.edit().remove(unlock.getKey()).commit();

                    } catch (ClassCastException e) {
                        //Oops it's a StringSet
                        final Set<String> stringSet = preferences.getStringSet(unlock.getKey(), null);
                        if (stringSet != null) {
                            stringSet.remove(unlock.getName());
                            preferences.edit().putStringSet(unlock.getKey(), stringSet);
                        }
                    }
                }
            };


            private final Context context;

            public UnlockListAdapter(List<Unlock> unlocks, Context context) {
                this.context = context;
                /*
                    wifi -> wifi1, wifi2, wifi3
                    bt -> bt1, bt2, bt3
                 */
                final HashMultimap<UnlockType, Unlock> unlockTypeToUnlockMap = HashMultimap.create();
                for (int i = 0; i < unlocks.size(); i++) {
                    Unlock unlock = unlocks.get(i);
                    final UnlockType key = unlock.getType();
                    unlockTypeToUnlockMap.get(key).add(unlock);

                }

                // wifi, bt //
                final Iterator<UnlockType> iterator = unlockTypeToUnlockMap.keySet().iterator();
                while (iterator.hasNext()) {
                    // for each type, we get all unlocks
                    final UnlockType key = iterator.next();
                    final int size = unlockTypeToUnlockMap.get(key).size();
                    /*
                    items: wifi,wifi1,wifi2,wifi3,bt,bt1,bt2,bt3
                     */
                    this.items.add(key);
                    this.items.addAll(Arrays.asList(unlockTypeToUnlockMap.get(key).toArray()));
                    HEADER_POSITIONS.add(items.indexOf(key));
                }
            }

            @Override
            public int getCount() {
                return items == null ? 0 : items.size();
            }

            @Override
            public Object getItem(int position) {
                return items == null ? null : items.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                final int viewType = getItemViewType(position);
                switch (viewType) {
                    case VIEW_TYPE_HEADER:
                        final UnlockType unlockType = (UnlockType) items.get(position);
                        if (convertView == null) {
                            convertView = LayoutInflater.from(context).inflate(R.layout.list_unlock_header, null);
                            HeaderViewHolder headerViewHolder = new HeaderViewHolder(convertView);
                            convertView.setTag(headerViewHolder);
                        }
                        HeaderViewHolder headerViewHolder = (HeaderViewHolder) convertView.getTag();
                        headerViewHolder.header.setText(unlockType.name());
                        return convertView;
                    case VIEW_TYPE_ITEM:
                        final Unlock unlock = (Unlock) items.get(position);
                        if (convertView == null) {
                            convertView = LayoutInflater.from(context).inflate(R.layout.list_unlock_item, null);
                            UnlockViewHolder unlockViewHolder = new UnlockViewHolder(convertView);
                            convertView.setTag(unlockViewHolder);
                        }
                        UnlockViewHolder unlockViewHolder = (UnlockViewHolder) convertView.getTag();
                        unlockViewHolder.unlockName.setText(unlock.getName());
                        unlockViewHolder.delete.setOnClickListener(removeClickListener);
                        if (UnlockType.WIFI.equals(unlock.getType())) {
                            unlockViewHolder.unlockSymbol.setImageResource(R.drawable.ic_action_wifi);
                        } else if (UnlockType.BLUETOOTH.equals(unlock.getType())) {
                            unlockViewHolder.unlockSymbol.setImageResource(R.drawable.ic_action_bluetooth);
                        } else {
                            unlockViewHolder.unlockSymbol.setImageResource(R.drawable.ic_action_car);
                        }
                        if (HEADER_POSITIONS.contains(position + 1) || position == items.size() - 1) {
                            unlockViewHolder.divider.setVisibility(View.INVISIBLE);
                        }
                        return convertView;
                    case VIEW_TYPE_BOOLEAN_LOCK:
                        return convertView;
                }

                return null;
            }

            @Override
            public int getItemViewType(int position) {
                return HEADER_POSITIONS.contains(position) ? VIEW_TYPE_HEADER : VIEW_TYPE_ITEM;
            }

            @Override
            public int getViewTypeCount() {
                return items.size() > 0 ? items.size() : 1;
            }

            class HeaderViewHolder {
                @InjectView(R.id.txt_unlockHeader)
                TextView header;

                public HeaderViewHolder(View v) {
                    ButterKnife.inject(this, v);
                }
            }

            class UnlockViewHolder {
                @InjectView(R.id.txt_unlockName)
                TextView unlockName;
                @InjectView(R.id.img_unlockSymbol)
                ImageView unlockSymbol;
                @InjectView(R.id.view_divider)
                View divider;
                @InjectView(R.id.btn_delete)
                ImageButton delete;

                public UnlockViewHolder(View v) {
                    ButterKnife.inject(this, v);
                }
            }
        }
    }

}
