package com.bernieeng.loki.ui.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bernieeng.loki.Util;
import com.bernieeng.loki.model.Unlock;
import com.bernieeng.loki.model.UnlockType;
import com.bernieeng.loki.service.LokiService;
import com.bernieeng.loki.ui.BackgroundContainer;
import com.bernieeng.loki.wizardpager.LokiWizardModel;
import com.cocosw.undobar.UndoBarController;
import com.google.common.collect.HashMultimap;
import com.kofikodr.loki.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class HomeActivity extends FragmentActivity {

    private static BluetoothAdapter btAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault("fonts/Roboto-Regular.ttf");
        setContentView(R.layout.activity_home);
        getActionBar().setIcon(R.drawable.ic_launcher_white);
        if (!PreferenceManager.getDefaultSharedPreferences(this).contains(LokiWizardModel.PREF_KEYS)) {
            startActivity(new Intent(this, PreWizardSetupActivity.class));
            this.finish();
        } else {
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, new UnlockListFragment())
                        .commit();
            }

            if (!Util.isMyServiceRunning(this, LokiService.class)) {
                startService(new Intent(this, LokiService.class));
            }
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

        if (id == R.id.action_run_wizard) {
            startActivity(new Intent(getApplicationContext(), PreWizardSetupActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class UnlockListFragment extends ListFragment implements UndoBarController.UndoListener {

        private static final int MOVE_DURATION = 150;
        private static final int SWIPE_DURATION = 250;
        private static final int ACTION_REQUEST_ENABLE = 1337;


        /**
         * A placeholder fragment containing a simple view.
         */
        @InjectView(R.id.listViewBackground)
        BackgroundContainer mBackgroundContainer;

        private SharedPreferences preferences = null;

        boolean mSwiping = false;
        boolean mItemPressed = false;
        HashMap<Long, Integer> mItemIdTopMap = new HashMap<Long, Integer>();

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
        public void onResume() {
            super.onResume();
            final Set<String> keys = preferences.getStringSet(LokiWizardModel.PREF_KEYS, null);
            if (keys != null && !keys.contains(getString(R.string.title_activity_unlock))) {
                keys.add(getString(R.string.title_activity_unlock));
                keys.remove(getString(R.string.title_vehicle_unlock));
                preferences.edit().putStringSet(LokiWizardModel.PREF_KEYS, keys).commit();
            }
            List<Unlock> unlockList = new ArrayList<Unlock>();
            if (keys != null) {
                for (String key : keys) {
                    Log.d("Loki Debug", "Keys in wizard model: " + key);
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

            setListAdapter(new UnlockListAdapter(unlockList, getActivity(), mTouchListener, R.layout.list_unlock_item));
            getListView().setDividerHeight(0);
            getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    UnlockListAdapter adapter = (UnlockListAdapter) adapterView.getAdapter();
                    if (adapter.isFooter(position)) {
                        final UnlockType unlockType = UnlockType.valueOf((String) adapter.getItem(position));
                        switch (unlockType) {
                            case WIFI:
                                startActivity(new Intent(getActivity(), WifiSelectActivity.class));
                                break;
                            case BLUETOOTH:
                                btAdapter = BluetoothAdapter.getDefaultAdapter();
                                if (btAdapter != null) {
                                    if (btAdapter.isEnabled()) {
                                        startActivity(new Intent(getActivity(), BluetoothSelectActivity.class));
                                    } else {
                                        btReceiver = new BluetoothActivatedReceiver();
                                        IntentFilter btif = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                                        getActivity().registerReceiver(btReceiver, btif);
                                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                        startActivityForResult(enableBtIntent, ACTION_REQUEST_ENABLE);
                                    }
                                }
                                break;
                            case ACTIVITY:
                                startActivity(new Intent(getActivity(), ActActivity.class));
                                break;
                            default:
                                //do nothing
                        }
                    }
                }
            });
        }

        private BluetoothActivatedReceiver btReceiver;

        class BluetoothActivatedReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (btAdapter.isEnabled()) {
                    startActivity(new Intent(getActivity(), BluetoothSelectActivity.class));
                }
            }
        }

        @Override
        public void onStop() {
            super.onStop();
            if (btReceiver != null) {
                try {
                    getActivity().unregisterReceiver(btReceiver);
                } catch (IllegalArgumentException ex) {
                    // ignore when receiver not registered
                }
            }
        }

        private void addUnlockToList(List<Unlock> target, String prefKey, String unlockName) {
            Log.d("Loki Debug", "Home screen loading: " + unlockName);
            if (prefKey.contains(getString(R.string.title_bt_unlock))) {
                Util.addSafeBluetooth(getActivity(), unlockName);
                target.add(new Unlock(UnlockType.BLUETOOTH, unlockName, prefKey));
            } else if (prefKey.contains(getString(R.string.title_wifi_unlock))) {
                target.add(new Unlock(UnlockType.WIFI, unlockName, prefKey));
                Util.addSafeWifi(getActivity(), unlockName);
            } else if (prefKey.contains(getString(R.string.title_activity_unlock)) || prefKey.contains(getString(R.string.title_vehicle_unlock)) || prefKey.contains("Enable in-vehicle unlock")) {
//                Toast.makeText(getActivity(), "Pref Key is " + prefKey, Toast.LENGTH_LONG).show();
                Log.d("Loki Debug", "Home screen saving: " + unlockName);
                target.add(new Unlock(UnlockType.ACTIVITY, unlockName, prefKey));
//                Util.enableDriveUnlock(getActivity());
                Util.addSafeActivity(getActivity(), unlockName);
            }
        }

        @Override
        public void onUndo(Parcelable token) {
            if (token != null && deletedUnlock != null) {
                final int position = ((Bundle) token).getInt("index");
                final SharedPreferences.Editor edit = preferences.edit();
                //save deleted unlock
//                if (UnlockType.BLUETOOTH.equals(deletedUnlock.getType()) || UnlockType.WIFI.equals(deletedUnlock.getType()) || UnlockType.) {
                String key = deletedUnlock.getKey();
                Set<String> data = preferences.getStringSet(key, null);
                String name = deletedUnlock.getName();
                data.add(name);
                edit.remove(key).commit();
                edit.putStringSet(key, data).commit();

                if (key.contains(getString(R.string.title_bt_unlock))) {
                    Util.addSafeBluetooth(getActivity(), name);
                } else if (key.contains(getString(R.string.title_wifi_unlock))) {
                    Util.addSafeWifi(getActivity(), name);
                } else if (key.contains(getString(R.string.title_activity_unlock)) || key.contains(getString(R.string.title_vehicle_unlock)) || key.contains("Enable in-vehicle unlock")) {
//                        Util.enableDriveUnlock(getActivity());
                    Log.d("Loki Debug", "Home screen saving (undo): " + name);
                    Util.addSafeActivity(getActivity(), name);
                }

//                } else if (UnlockType.ACTIVITY.equals(deletedUnlock.getType())) {

//                    preferences.edit().putString(deletedUnlock.getKey(), deletedUnlock.getName()).commit();
//                }
                // put it back into the list
                ((UnlockListAdapter) getListAdapter()).insert(deletedUnlock, position);
            }
        }

        class UnlockListAdapter extends ArrayAdapter {

            private final Set<Integer> HEADER_POSITIONS = new HashSet<Integer>();
            private final Set<Integer> FOOTER_POSITIONS = new HashSet<Integer>();
            private List items = new ArrayList();
            private final int VIEW_TYPE_HEADER = 0;
            private final int VIEW_TYPE_ITEM = 1;
            private final int VIEW_TYPE_ADD = 2;
            HashMap<Object, Integer> mIdMap = new HashMap<Object, Integer>();
            View.OnTouchListener mTouchListener;

            private final View.OnClickListener removeClickListener = new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int position = getListView().getPositionForView(v);
                    removeFromPreference(position);
                    //this will actually remove the item from the adapter too
                    animateRemoval(getListView(), v);
                }
            };

            private final Context context;

            public boolean isFooter(int position) {
                return FOOTER_POSITIONS.contains(position);
            }

            public UnlockListAdapter(List<Unlock> unlocks, Context context, View.OnTouchListener listener, int resId) {
                super(context, resId, unlocks);
                this.context = context;
                this.mTouchListener = listener;

                /*
                    wifi -> wifi1, wifi2, wifi3
                    bt -> bt1, bt2, bt3
                 */
                final HashMultimap<UnlockType, Unlock> unlockTypeToUnlockMap = HashMultimap.create();
                String dummy = "dummy";
                Unlock dummy1 = new Unlock(UnlockType.ACTIVITY, dummy, dummy);
                Unlock dummy2 = new Unlock(UnlockType.WIFI, dummy, dummy);
                Unlock dummy3 = new Unlock(UnlockType.BLUETOOTH, dummy, dummy);
                unlockTypeToUnlockMap.put(UnlockType.ACTIVITY, dummy1);
                unlockTypeToUnlockMap.put(UnlockType.WIFI, dummy2);
                unlockTypeToUnlockMap.put(UnlockType.BLUETOOTH, dummy3);
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
                    /*
                    items: wifi_header,wifi1,wifi2,wifi3,bt_header,bt1,bt2,bt3
                     */
                    this.items.add(key);
                    final Set<Unlock> set = unlockTypeToUnlockMap.get(key);
                    ArrayList<Unlock> tmp = new ArrayList<Unlock>(set.size());
                    tmp.addAll(set);
                    tmp.remove(dummy1);
                    tmp.remove(dummy2);
                    tmp.remove(dummy3);
                    Collections.sort(tmp, new Comparator<Unlock>() {
                        @Override
                        public int compare(Unlock lhs, Unlock rhs) {
                            if (lhs != null && rhs != null) {
                                return lhs.getName().toLowerCase().compareTo(rhs.getName().toLowerCase());
                            }
                            return 0;
                        }
                    });
                    this.items.addAll(tmp);
                    HEADER_POSITIONS.add(items.indexOf(key));
                    this.items.add(key.getValue());
                    FOOTER_POSITIONS.add(items.indexOf(key.getValue()));
                }

                for (int i = 0; i < this.items.size(); ++i) {
                    mIdMap.put(this.items.get(i), i);
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
            public boolean hasStableIds() {
                return true;
            }

            @Override
            public long getItemId(int position) {
                Object item = getItem(position);
                return mIdMap.get(item);
            }

            @Override
            public void insert(Object object, int index) {
                super.insert(object, 0);
                //insert
                items.add(index, object);
                //recalculate header & footer positions
                recalculateHeaderFooter();
                notifyDataSetChanged();
            }

            @Override
            public void remove(Object object) {
                super.remove(object);
                //remove
                items.remove(object);
                //recalculate header & footer positions
                recalculateHeaderFooter();
                notifyDataSetChanged();
            }

            private void recalculateHeaderFooter() {
                HEADER_POSITIONS.clear();
                FOOTER_POSITIONS.clear();
                for (int i = 0; i < items.size(); i++) {
                    Object obj = items.get(i);
                    if (obj instanceof UnlockType) {
                        HEADER_POSITIONS.add(i);
                    } else if (obj instanceof String) {
                        FOOTER_POSITIONS.add(i);
                    }
                }
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
                            convertView.setOnTouchListener(mTouchListener);
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
                        return convertView;
                    case VIEW_TYPE_ADD:
                        if (convertView == null) {
                            convertView = LayoutInflater.from(context).inflate(R.layout.list_unlock_footer, null);
                        }
                        return convertView;
                }

                return null;
            }

            @Override
            public boolean isEnabled(int position) {
                if (HEADER_POSITIONS.contains(position)) {
                    return false;
                }
                return true;
            }

            @Override
            public int getItemViewType(int position) {
                if (HEADER_POSITIONS.contains(position)) {
                    return VIEW_TYPE_HEADER;
                } else if (FOOTER_POSITIONS.contains(position)) {
                    return VIEW_TYPE_ADD;
                } else {
                    return VIEW_TYPE_ITEM;
                }
            }

            @Override
            public int getViewTypeCount() {
                return 3;
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
                @InjectView(R.id.btn_delete)
                ImageButton delete;

                public UnlockViewHolder(View v) {
                    ButterKnife.inject(this, v);
                }
            }
        }

        private Unlock deletedUnlock;

        private void removeFromPreference(int position) {
            final Bundle b = new Bundle();
            b.putInt("index", position);
            deletedUnlock = (Unlock) getListAdapter().getItem(position);
            UndoBarController.show(getActivity(), deletedUnlock.getName()
                    + " deleted", this, b);
            final SharedPreferences.Editor edit = preferences.edit();
            try {
                //test if this is a StringSet or just a String
                final Set<String> stringSet = preferences.getStringSet(deletedUnlock.getKey(), null);
                if (stringSet != null) {
                    stringSet.remove(deletedUnlock.getName());
                    edit.remove(deletedUnlock.getKey()).commit();
                    edit.putStringSet(deletedUnlock.getKey(), stringSet).commit();

                    if (deletedUnlock.getKey().contains(getString(R.string.title_bt_unlock))) {
                        Util.removeSafeBluetooth(getActivity(), deletedUnlock.getName());
                    } else if (deletedUnlock.getKey().contains(getString(R.string.title_wifi_unlock))) {
                        Util.removeSafeWifi(getActivity(), deletedUnlock.getName());
                    } else if (deletedUnlock.getKey().contains(getString(R.string.title_activity_unlock)) || deletedUnlock.getKey().contains(getString(R.string.title_vehicle_unlock))) {
//                        Util.disableDriveUnlock(getActivity());
                        Util.removeSafeActivity(getActivity(), deletedUnlock.getName());
                        Log.d("Loki Debug: loading into home - ", deletedUnlock.toString());
                    }

                }
            } catch (ClassCastException e) {
                //oops it's a string
                preferences.getString(deletedUnlock.getKey(), null);
                edit.remove(deletedUnlock.getKey()).commit();
//                Toast.makeText(getActivity(), "Removing (handle this!) " + deletedUnlock.getKey(), Toast.LENGTH_LONG).show();
            }
        }

        /**
         * Handle touch events to fade/move dragged items as they are swiped out
         */
        private View.OnTouchListener mTouchListener = new View.OnTouchListener() {

            float mDownX;
            private int mSwipeSlop = -1;
            private VelocityTracker mVelocityTracker = null;
            private int mMinFlingVelocity;
            private int mMaxFlingVelocity;

            @Override
            public boolean onTouch(final View v, MotionEvent event) {

                final ListView listView = getListView();

                if (mSwipeSlop < 0) {
                    final ViewConfiguration vc = ViewConfiguration.get(getActivity());
                    mSwipeSlop = vc.
                            getScaledTouchSlop();
                    mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
                    mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        if (mVelocityTracker == null) {
                            mVelocityTracker = VelocityTracker.obtain();
                        } else {
                            mVelocityTracker.clear();
                        }
                        mVelocityTracker.addMovement(event);
                        if (mItemPressed) {
                            // Multi-item swipes not handled
                            return false;
                        }
                        mItemPressed = true;
                        mDownX = event.getX();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        v.setAlpha(1);
                        v.setTranslationX(0);
                        mItemPressed = false;
                        break;
                    case MotionEvent.ACTION_MOVE: {
                        mVelocityTracker.addMovement(event);
                        mVelocityTracker.computeCurrentVelocity(1000);

                        float x = event.getX() + v.getTranslationX();
                        float deltaX = x - mDownX;
                        float deltaXAbs = Math.abs(deltaX);
                        if (!mSwiping) {
                            if (deltaXAbs > mSwipeSlop) {
                                mSwiping = true;
                                listView.requestDisallowInterceptTouchEvent(true);
                                mBackgroundContainer.showBackground(v.getTop(), v.getHeight());
                            }
                        }
                        if (mSwiping) {
                            v.setTranslationX((x - mDownX));
                            v.setAlpha(1 - deltaXAbs / v.getWidth());
                        }
                    }
                    break;
                    case MotionEvent.ACTION_UP: {
                        // User let go - figure out whether to animate the view out, or back into place
                        if (mSwiping) {
                            mVelocityTracker.addMovement(event);
                            mVelocityTracker.computeCurrentVelocity(1000);
                            float velocityX = Math.abs(mVelocityTracker.getXVelocity());
                            float velocityY = Math.abs(mVelocityTracker.getYVelocity());

                            float x = event.getX() + v.getTranslationX();
                            float deltaX = x - mDownX;
                            float deltaXAbs = Math.abs(deltaX);
                            float fractionCovered;
                            float endX;
                            float endAlpha;
                            final boolean remove;
                            if (deltaXAbs > v.getWidth() / 4) {
                                // Greater than a quarter of the width - animate it out
                                fractionCovered = deltaXAbs / v.getWidth();
                                endX = deltaX < 0 ? -v.getWidth() : v.getWidth();
                                endAlpha = 0;
                                remove = true;
                            } else if (mMinFlingVelocity <= velocityX && velocityX <= mMaxFlingVelocity
                                    && velocityY < velocityX) {
                                remove = true;
                                endX = deltaX < 0 ? -v.getWidth() : v.getWidth();
                                endAlpha = 0;
                                fractionCovered = deltaXAbs / v.getWidth();
                            } else {
                                // Not far enough - animate it back
                                fractionCovered = 1 - (deltaXAbs / v.getWidth());
                                endX = 0;
                                endAlpha = 1;
                                remove = false;
                            }
                            // Animate position and alpha of swiped item
                            // NOTE: This is a simplified version of swipe behavior, for the
                            // purposes of this demo about animation. A real version should use
                            // velocity (via the VelocityTracker class) to send the item off or
                            // back at an appropriate speed.
                            long duration = (int) ((1 - fractionCovered) * SWIPE_DURATION);
                            listView.setEnabled(false);
                            v.animate().setDuration(duration).
                                    alpha(endAlpha).translationX(endX).
                                    withEndAction(new Runnable() {
                                        @Override
                                        public void run() {
                                            // Restore animated values
                                            v.setAlpha(1);
                                            v.setTranslationX(0);
                                            if (remove) {
                                                removeFromPreference(listView.getPositionForView(v));
                                                animateRemoval(listView, v);
                                            } else {
                                                mBackgroundContainer.hideBackground();
                                                mSwiping = false;
                                                listView.setEnabled(true);
                                            }
                                        }
                                    });
                        }
                    }
                    mItemPressed = false;
                    break;
                    default:
                        return false;
                }
                return true;
            }
        };

        /**
         * This method animates all other views in the ListView container (not including ignoreView)
         * into their final positions. It is called after ignoreView has been removed from the
         * adapter, but before layout has been run. The approach here is to figure out where
         * everything is now, then allow layout to run, then figure out where everything is after
         * layout, and then to run animations between all of those start/end positions.
         */
        private void animateRemoval(final ListView listview, View viewToRemove) {

            final ListView mListView = getListView();
            final UnlockListAdapter mAdapter = (UnlockListAdapter) getListAdapter();

            int firstVisiblePosition = listview.getFirstVisiblePosition();
            for (int i = 0; i < listview.getChildCount(); ++i) {
                View child = listview.getChildAt(i);
                if (child != viewToRemove) {
                    int position = firstVisiblePosition + i;
                    long itemId = mAdapter.getItemId(position);
                    mItemIdTopMap.put(itemId, child.getTop());
                }
            }
            // Delete the item from the adapter
            int position = mListView.getPositionForView(viewToRemove);
            mAdapter.remove(mAdapter.getItem(position));

            final ViewTreeObserver observer = listview.getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                public boolean onPreDraw() {
                    observer.removeOnPreDrawListener(this);
                    boolean firstAnimation = true;
                    int firstVisiblePosition = listview.getFirstVisiblePosition();
                    for (int i = 0; i < listview.getChildCount(); ++i) {
                        final View child = listview.getChildAt(i);
                        int position = firstVisiblePosition + i;
                        long itemId = mAdapter.getItemId(position);
                        Integer startTop = mItemIdTopMap.get(itemId);
                        int top = child.getTop();
                        if (startTop != null) {
                            if (startTop != top) {
                                int delta = startTop - top;
                                child.setTranslationY(delta);
                                child.animate().setDuration(MOVE_DURATION).translationY(0);
                                if (firstAnimation) {
                                    child.animate().withEndAction(new Runnable() {
                                        public void run() {
                                            mBackgroundContainer.hideBackground();
                                            mSwiping = false;
                                            mListView.setEnabled(true);
                                        }
                                    });
                                    firstAnimation = false;
                                }
                            }
                        } else {
                            // Animate new views along with the others. The catch is that they did not
                            // exist in the start state, so we must calculate their starting position
                            // based on neighboring views.
                            int childHeight = child.getHeight() + listview.getDividerHeight();
                            startTop = top + (i > 0 ? childHeight : -childHeight);
                            int delta = startTop - top;
                            child.setTranslationY(delta);
                            child.animate().setDuration(MOVE_DURATION).translationY(0);
                            if (firstAnimation) {
                                child.animate().withEndAction(new Runnable() {
                                    public void run() {
                                        mBackgroundContainer.hideBackground();
                                        mSwiping = false;
                                        mListView.setEnabled(true);
                                    }
                                });
                                firstAnimation = false;
                            }
                        }
                    }
                    mItemIdTopMap.clear();
                    return true;
                }
            });
        }
    }
}
