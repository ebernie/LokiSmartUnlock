package com.bernieeng.loki.ui.activity;

import android.app.Activity;
import android.os.Bundle;

import com.bernieeng.loki.R;

/**
 * Created by ebernie on 9/13/13.
 */
public class BluetoothListActivity extends Activity {

    public static final int BT_PICK_REQUEST = 1002;
    public static String BT_PICK_RESULT = "com.bernieeng.loki.bt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_list);
    }
}
