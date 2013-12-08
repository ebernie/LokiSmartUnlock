package com.bernieeng.loki;

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

/**
 * Created by ebernie on 9/13/13.
 */
public class BluetoothMonService extends Service {

    public static final String STOP = "STOP";
    private BluetoothStateReceiver btReceiver;
    private NotificationManager mNM;
    private LocalBinder mBinder = new LocalBinder();

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = R.string.app_name;

    @Override
    public void onCreate() {
        super.onCreate();
        btReceiver = new BluetoothStateReceiver();
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        showNotification();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IntentFilter connected = new IntentFilter();
        connected.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        connected.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        connected.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        registerReceiver(btReceiver, connected);

        return START_STICKY;
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {

        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                new Intent(getApplicationContext(), MainActivity.class),
                Intent.FLAG_ACTIVITY_NEW_TASK);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final String btName = prefs.getString(MainActivity.BT_NAME, "");

        Notification noti = new Notification.Builder(getApplicationContext())
                .setContentTitle(getString(R.string.bt_unlock))
                .setContentText(btName)
                .setSmallIcon(R.drawable.ic_action_key)
                .setOngoing(true)
                .setAutoCancel(false)
                .setContentIntent(pendingIntent)
                .setTicker(getString(R.string.bt_unlock_enabled))
                .build();

        // Send the notification.
        mNM.notify(NOTIFICATION, noti);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(btReceiver);
        if (mNM != null) {
            mNM.cancel(NOTIFICATION);
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        BluetoothMonService getService() {
            return BluetoothMonService.this;
        }
    }
}
