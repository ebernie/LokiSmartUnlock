package com.bernieeng.loki.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.bernieeng.loki.Util;
import com.bernieeng.loki.model.UnlockType;
import com.bernieeng.loki.service.LokiService;

/**
 * Created by ebernie on 8/12/14.
 */
public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //we lock the device to be safe
        Util.setPassword(context);
        context.startService(new Intent(context, LokiService.class));
    }
}
