package com.bernieeng.loki;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by ebernie on 9/11/13.
 */
public class WifiListActivity extends Activity {

    public static final int WIFI_PICK_REQUEST = 1001;
    public static String WIFI_PICK_RESULT = "com.bernieeng.loki";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_list);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
