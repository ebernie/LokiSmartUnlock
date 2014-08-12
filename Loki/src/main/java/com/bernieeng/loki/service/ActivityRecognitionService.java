package com.bernieeng.loki.service;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.bernieeng.loki.event.LockEvent;
import com.bernieeng.loki.event.UnlockEvent;
import com.bernieeng.loki.model.UnlockType;
import com.bernieeng.loki.Util;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import de.greenrobot.event.EventBus;

public class ActivityRecognitionService extends IntentService {

    private static final String TAG = "Loki ActivityRecognition";

    private boolean mIsBound = false;
    private LokiService mBoundService;

    public ActivityRecognitionService() {
        super("Loki ActivityRecognitionService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        doBindService();
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundService = ((LokiService.LocalBinder) service).getService();

            // Tell the user about this for our demo.
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null;
        }
    };

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        bindService(new Intent(ActivityRecognitionService.this,
                LokiService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (ActivityRecognitionResult.hasResult(intent) && mIsBound && mBoundService != null) {

            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            final int activityType = result.getMostProbableActivity().getType();
            int previousActivity = mBoundService.getPreviousActivity();

            if (activityType != previousActivity) {
                if (DetectedActivity.IN_VEHICLE == activityType) {
                    if (Util.isDriveUnlockEnabled(this)) {
                        EventBus.getDefault().post(new UnlockEvent(UnlockType.ACTIVITY, "Driving"));
                    }

                    mBoundService.setPreviousActivity(activityType);
                } else {
                    if (activityType != DetectedActivity.TILTING && activityType != previousActivity) {
                        mBoundService.setPreviousActivity(activityType);
                        EventBus.getDefault().post(new LockEvent(UnlockType.ACTIVITY, "Driving"));
                    }
                }
            }
        }
    }


}

