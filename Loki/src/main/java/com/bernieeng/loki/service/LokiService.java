package com.bernieeng.loki.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.bernieeng.loki.ActivityRecognitionScan;
import com.bernieeng.loki.receiver.BluetoothStateReceiver;
import com.bernieeng.loki.ui.activity.MainActivity;
import com.bernieeng.loki.R;
import com.bernieeng.loki.model.UnlockType;
import com.bernieeng.loki.Util;
import com.bernieeng.loki.event.LockEvent;
import com.bernieeng.loki.event.UnlockEvent;
import com.google.android.gms.location.DetectedActivity;

import de.greenrobot.event.EventBus;

/**
 * This class handles all locking and unlocking events, monitors BT connectivity & user activity (e.g. driving)
 *
 * Created by ebernie on 8/11/14.
 */
public class LokiService extends Service {

    private BluetoothStateReceiver btReceiver;
    private NotificationManager mNM;
    private LocalBinder mBinder = new LocalBinder();
    private int NOTIFICATION = R.string.app_name;
    private int previousActivity = DetectedActivity.STILL;
    private ActivityRecognitionScan activityRecognitionScan;

    @Override
    public void onCreate() {
        super.onCreate();
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

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {

        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                new Intent(getApplicationContext(), MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final String btName = prefs.getString(MainActivity.BT_NAME, "");

        Notification noti = new Notification.Builder(getApplicationContext())
                .setContentTitle(getString(R.string.loki_enabled))
                .setContentText(btName)
                .setSmallIcon(R.drawable.ic_action_key)
                .setOngoing(true)
                .setAutoCancel(false)
                .setContentIntent(pendingIntent)
                .setTicker(getString(R.string.loki_enabled))
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
        Toast.makeText(this, "Loki: Unlocking", Toast.LENGTH_LONG).show();
        Util.unSetPassword(this, false, event.getType());
    }

    public void onEvent(LockEvent event) {
        Toast.makeText(this, "Loki: Locking & password is " + Util.getPinOrPassword(this), Toast.LENGTH_LONG).show();
        Util.setPassword(this, Util.getPinOrPassword(this), event.getType());
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
    }

    public class LocalBinder extends Binder {
        LokiService getService() {
            return LokiService.this;
        }
    }
}
