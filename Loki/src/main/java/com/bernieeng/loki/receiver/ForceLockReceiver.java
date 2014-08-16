package com.bernieeng.loki.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.bernieeng.loki.event.LockEvent;
import com.bernieeng.loki.model.UnlockType;

import de.greenrobot.event.EventBus;

/**
 * Created by ebernie on 8/16/14.
 */
public class ForceLockReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        EventBus.getDefault().post(new LockEvent(UnlockType.USER_ACTION, null, true));
    }
}
