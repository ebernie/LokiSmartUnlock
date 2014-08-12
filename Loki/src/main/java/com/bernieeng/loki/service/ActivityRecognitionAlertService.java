package com.bernieeng.loki.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.bernieeng.loki.ActivityRecognitionScan;
import com.bernieeng.loki.ui.activity.MainActivity;
import com.bernieeng.loki.R;
import com.google.android.gms.location.DetectedActivity;

public class ActivityRecognitionAlertService extends Service {

    private NotificationManager mNM;
    private final int NOTIFICATION = R.string.unlock_in_vehicle;
    private LocalBinder mBinder = new LocalBinder();
//    private boolean countingDown = false;
//    private CountDownTimer timer;
    private int previousActivity = DetectedActivity.STILL;
    private ActivityRecognitionScan activityRecognitionScan;
//    private static final String DEFAULT_DELAY = "1";

//    public void startLockCountDown() {
//        countingDown = true;
//        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//
//        timer = new CountDownTimer(30000, 3000) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//                // minutes left
//                int minutesLeft = Math.round(millisUntilFinished / 1000);
//                showNotification("Locking in " + minutesLeft + " seconds");
//                Log.d("ActivityRecognitionAlertService", "Loki timer: >>>>>" + millisUntilFinished);
//            }
//            @Override
//            public void onFinish() {
//                countingDown = false;
//                showNotification("Locked because exited vehicle");
//                Log.d("ActivityRecognitionAlertService", "Loki timer: >>>>> LOCKED");
//
//            }
//        };
//        timer.start();
//    }
//
//    public boolean isCountingDown() {
//        return countingDown;
//    }

//    public void setCountingDown(boolean countingDown) {
//        this.countingDown = countingDown;
//    }

    public ActivityRecognitionAlertService() {

    }

    public int getPreviousActivity() {
        return previousActivity;
    }

    public void setPreviousActivity(int previousActivity) {
        this.previousActivity = previousActivity;
    }

//    public CountDownTimer getTimer() {
//        return timer;
//    }

//    public void setTimer(CountDownTimer timer) {
//        this.timer = timer;
//    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class LocalBinder extends Binder {
        ActivityRecognitionAlertService getService() {
            return ActivityRecognitionAlertService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        activityRecognitionScan = new ActivityRecognitionScan(getApplicationContext());
        activityRecognitionScan.startActivityRecognitionScan();
        showNotification(getString(R.string.driving_unlock_enabled));
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (activityRecognitionScan != null) {
            activityRecognitionScan.stopActivityRecognitionScan();
        }
        if (mNM != null) {
            mNM.cancel(NOTIFICATION);
        }
    }

    /**
     * Show a notification while this service is running.
     */
    public void showNotification(String message) {
        Context context = getApplicationContext();
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                new Intent(context, MainActivity.class),
                Intent.FLAG_ACTIVITY_CLEAR_TASK | PendingIntent.FLAG_UPDATE_CURRENT);
        Notification noti = new Notification.Builder(context)
                .setContentTitle(context.getString(R.string.driving_unlock_enabled))
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_action_key)
                .setOngoing(true)
                .setAutoCancel(false)
                .setContentIntent(pendingIntent)
                .setTicker(message)
                .build();

        // Send the notification.
        ((NotificationManager) context.getSystemService(NOTIFICATION_SERVICE)).notify(NOTIFICATION, noti);
    }

}
