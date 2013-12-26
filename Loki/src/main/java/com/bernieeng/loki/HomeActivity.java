package com.bernieeng.loki;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
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
        getActionBar().setTitle(R.string.app_name);
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

        public UnlockListFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_home, container, false);
            ButterKnife.inject(this, rootView);
            return rootView;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            List<Unlock> unlocks = new ArrayList<Unlock>(10);
            unlocks.add(new Unlock(UnlockType.WIFI, "WiFi 1"));
            unlocks.add(new Unlock(UnlockType.WIFI, "WiFi 2"));
            unlocks.add(new Unlock(UnlockType.WIFI, "WiFi 3"));
            unlocks.add(new Unlock(UnlockType.BLUETOOTH, "BT 1"));
            unlocks.add(new Unlock(UnlockType.BLUETOOTH, "BT 2"));
            unlocks.add(new Unlock(UnlockType.ACTIVITY, "In-Vehicle"));
            setListAdapter(new UnlockListAdapter(unlocks, getActivity()));
            getListView().setDividerHeight(0);
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
                return items.size();
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
