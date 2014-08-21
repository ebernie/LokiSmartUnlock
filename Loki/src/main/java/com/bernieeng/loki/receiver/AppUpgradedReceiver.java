package com.bernieeng.loki.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.bernieeng.loki.Util;
import com.bernieeng.loki.service.LokiService;

/**
 * Created by ebernie on 8/14/14.
 */
public class AppUpgradedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
//        Util.wizardRunCheck(context);
        context.startService(new Intent(context, LokiService.class));
    }
}
