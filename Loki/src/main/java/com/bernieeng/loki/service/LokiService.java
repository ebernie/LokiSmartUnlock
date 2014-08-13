package com.bernieeng.loki.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;

import com.bernieeng.loki.ActivityRecognitionScan;
import com.bernieeng.loki.R;
import com.bernieeng.loki.Util;
import com.bernieeng.loki.event.LockEvent;
import com.bernieeng.loki.event.UnlockEvent;
import com.bernieeng.loki.model.UnlockType;
import com.bernieeng.loki.receiver.BluetoothStateReceiver;
import com.bernieeng.loki.ui.activity.HomeActivity;
import com.google.android.gms.location.DetectedActivity;

import java.util.HashSet;
import java.util.Set;

import de.greenrobot.event.EventBus;

/**
 * This class handles all locking and unlocking events, monitors BT connectivity & user activity (e.g. driving)
 * <p/>
 * Created by ebernie on 8/11/14.
 */
public class LokiService extends Service {

    private BluetoothStateReceiver btReceiver;
    private NotificationManager mNM;
    private LocalBinder mBinder = new LocalBinder();
    private int NOTIFICATION = R.string.app_name;
    private int previousActivity = DetectedActivity.STILL;
    private ActivityRecognitionScan activityRecognitionScan;
    private Set<String> unlocks = new HashSet<String>();
    private static boolean isRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        EventBus.getDefault().register(this);
        btReceiver = new BluetoothStateReceiver();
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        showNotification();
    }

    public int getPreviousActivity() {
        return previousActivity;
    }

    public void setPreviousActivity(int previousActivity) {
        this.previousActivity = previousActivity;
    }

    public boolean isServiceRunning() {
        return isRunning;
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {

        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                new Intent(getApplicationContext(), HomeActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);

        String deviceStatus = unlocks.isEmpty() ? getString(R.string.device_locekd) : getString(R.string.device_unlocked);

        Notification noti = new Notification.Builder(getApplicationContext())
                .setContentTitle(getString(R.string.loki_enabled))
                .setContentText(deviceStatus)
                .setSmallIcon(R.drawable.ic_launcher_white)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_MIN)
                .setAutoCancel(false)
                .setContentIntent(pendingIntent)
                .setTicker(getString(R.string.unlock_event_occured))
                .build();

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

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void onEvent(UnlockEvent event) {
        unlocks.add(event.toString());
//        Toast.makeText(this, "Loki: unlocks size " + unlocks.size(), Toast.LENGTH_LONG).show();
//        Toast.makeText(this, "Loki: Unlocking", Toast.LENGTH_LONG).show();
        Util.unSetPassword(this, false, event.getType());
        showNotification();
    }

    public void onEvent(LockEvent event) {
        unlocks.remove(event.toString());
//        Toast.makeText(this, "Loki: unlocks left " + unlocks.size(), Toast.LENGTH_LONG).show();
        if (unlocks.isEmpty()) {
//            Toast.makeText(this, "Loki: Locking & password is " + Util.getPinOrPassword(this), Toast.LENGTH_LONG).show();
            Util.setPassword(this, Util.getPinOrPassword(this), event.getType());
            showNotification();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        unregisterReceiver(btReceiver);
        if (activityRecognitionScan != null) {
            activityRecognitionScan.stopActivityRecognitionScan();
        }
        if (mNM != null) {
            mNM.cancel(NOTIFICATION);
        }
        Util.setPassword(this, Util.getPinOrPassword(this), UnlockType.KILLED);
    }

    public class LocalBinder extends Binder {
        LokiService getService() {
            return LokiService.this;
        }
    }
}
