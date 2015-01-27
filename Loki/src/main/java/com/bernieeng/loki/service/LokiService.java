package com.bernieeng.loki.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import com.bernieeng.loki.ActivityRecognitionScan;
import com.bernieeng.loki.Util;
import com.bernieeng.loki.event.LockEvent;
import com.bernieeng.loki.event.UnlockEvent;
import com.bernieeng.loki.model.UnlockType;
import com.bernieeng.loki.receiver.BluetoothStateReceiver;
import com.bernieeng.loki.receiver.ForceLockReceiver;
import com.bernieeng.loki.ui.activity.HomeActivity;
import com.bernieeng.loki.ui.activity.PreWizardSetupActivity;
import com.google.android.gms.location.DetectedActivity;
import com.kofikodr.loki.R;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.greenrobot.event.EventBus;

/**
 * This class handles all locking and unlocking events, monitors BT connectivity & user activity (e.g. driving)
 * <p/>
 * Created by ebernie on 8/11/14.
 */
public class LokiService extends Service {

    public static final String LOKI_ACTION_FORCELOCK = "loki.action.forcelock";
    public static final int LOKI_ACTION_FORCELOCK_REQ_CODE = 1981;
    private BluetoothStateReceiver btReceiver;
    private ForceLockReceiver lockReceiver;
    private NotificationManager mNM;
    private LocalBinder mBinder = new LocalBinder();
    private int NOTIFICATION = R.string.app_name;
    private int previousActivity = DetectedActivity.STILL;
    private ActivityRecognitionScan activityRecognitionScan;
    private static Set<String> unlocks = Collections.synchronizedSet(new HashSet<String>());

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        btReceiver = new BluetoothStateReceiver();
        lockReceiver = new ForceLockReceiver();
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        showNotification();

        //check if safe wifi is connected & unlock appropriately
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mWifi.isConnected()) {
            final WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            String ssid = Util.getSSID(connectionInfo);
            if (Util.isSafeNetwork(this, ssid)) {
                onEvent(new UnlockEvent(UnlockType.WIFI, ""));
            }
        }

        //toggle BT since can't get a list of BT connected devices
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    bluetoothAdapter.enable();
                }
            }, 5000);
        }
    }

    public int getPreviousActivity() {
        return previousActivity;
    }

    public void setPreviousActivity(int previousActivity) {
        this.previousActivity = previousActivity;
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {

        PendingIntent pendingIntent = null;
//        if (Util.isWizardRunNeeded(this)) {
//            pendingIntent = PendingIntent.getActivity(
//                    getApplicationContext(),
//                    0,
//                    new Intent(getApplicationContext(), PreWizardSetupActivity.class),
//                    PendingIntent.FLAG_UPDATE_CURRENT);
//        } else {
            pendingIntent = PendingIntent.getActivity(
                    getApplicationContext(),
                    0,
                    new Intent(getApplicationContext(), HomeActivity.class),
                    PendingIntent.FLAG_UPDATE_CURRENT);
//        }

        Notification noti;
        String deviceStatus;
        if (unlocks.isEmpty()) {
            deviceStatus = getString(R.string.device_locekd);
//            deviceStatus = Util.isWizardRunNeeded(this) ? getString(R.string.rerun_setup_wizard) : getString(R.string.device_locekd);
            noti = new Notification.Builder(getApplicationContext())
                    .setContentTitle(deviceStatus)
                    .setContentText(getString(R.string.unlock_factors) + " " + unlocks.size())
                    .setSmallIcon(R.drawable.ic_stat_loki)
                    .setOngoing(true)
                    .setPriority(Notification.PRIORITY_MIN)
                    .setAutoCancel(false)
                    .setContentIntent(pendingIntent)
                    .setTicker(getString(R.string.unlock_event_occured))
                    .build();
        } else {
            deviceStatus = getString(R.string.device_unlocked);
//            deviceStatus = Util.isWizardRunNeeded(this) ? getString(R.string.rerun_setup_wizard) : getString(R.string.device_unlocked);
            Intent lockIntent = new Intent(this, ForceLockReceiver.class);
            lockIntent.setAction(LOKI_ACTION_FORCELOCK);
            PendingIntent pi = PendingIntent.getBroadcast(this, LOKI_ACTION_FORCELOCK_REQ_CODE, lockIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//            StringBuilder sb = new StringBuilder();
//            Iterator<String> iterator = unlocks.iterator();
//            while (iterator.hasNext()) {
//                sb.append(iterator.next());
//                sb.append("\n");
//            }
//            String subtext = sb.toString().trim();
            noti = new Notification.Builder(getApplicationContext())
                    .setContentTitle(deviceStatus)
                    .setContentText(getString(R.string.unlock_factors) + " " + unlocks.size())
                    .setSmallIcon(R.drawable.ic_stat_loki)
                    .setOngoing(true)
                    .setPriority(Notification.PRIORITY_MIN)
                    .setAutoCancel(false)
                    .addAction(R.drawable.ic_action_lock_closed, getString(R.string.lock_device), pi)
                    .setContentIntent(pendingIntent)
                    .setTicker(getString(R.string.unlock_event_occured))
                    .build();
        }

        // Send the notification.
        mNM.notify(NOTIFICATION, noti);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IntentFilter connected = new IntentFilter();
        connected.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        connected.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        connected.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        registerReceiver(btReceiver, connected);
        activityRecognitionScan = new ActivityRecognitionScan(getApplicationContext());
        activityRecognitionScan.startActivityRecognitionScan();

        IntentFilter lockFilter = new IntentFilter();
        lockFilter.addAction(LOKI_ACTION_FORCELOCK);
        registerReceiver(lockReceiver, lockFilter);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void onEvent(UnlockEvent event) {
        unlocks.add(event.toString());
//        Toast.makeText(this, "Adding unlock factor: " + event.toString(), Toast.LENGTH_LONG).show();
        Util.unSetPassword(this, false, event.getType());
        showNotification();
    }

    public void onEvent(LockEvent event) {

        if (event.isForceLock()) {
            unlocks.clear();
        } else {
            unlocks.remove(event.toString());
        }

        if (unlocks.isEmpty()) {
            Util.setPassword(this);
        }
        showNotification();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        unregisterReceiver(btReceiver);
        unregisterReceiver(lockReceiver);
        if (activityRecognitionScan != null) {
            activityRecognitionScan.stopActivityRecognitionScan();
        }
        if (mNM != null) {
            mNM.cancel(NOTIFICATION);
        }
        Util.setPassword(this);
    }

    public class LocalBinder extends Binder {
        public LokiService getService() {
            return LokiService.this;
        }
    }

}
