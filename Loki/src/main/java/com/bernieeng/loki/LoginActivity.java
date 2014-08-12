package com.bernieeng.loki;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bernieeng.loki.ui.activity.MainActivity;

/**
 * Created by ebernie on 9/11/13.
 */
public class LoginActivity extends Activity {

//    private static final String EXPIRY_TIME = "install_time";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        //check for time bomb
//        if (!prefs.contains(EXPIRY_TIME)) {
//            // 7 days
//            prefs.edit().putLong(EXPIRY_TIME, (System.currentTimeMillis() + (604800))).commit();
//        } else {
//            if (DateUtils.isToday(prefs.getLong(EXPIRY_TIME, 0l))) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                builder.setMessage(getString(R.string.expired_msg)).setIcon(android.R.drawable.ic_dialog_alert).setTitle(getString(R.string.expired)).setPositiveButton(getString(R.string.quit), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        LoginActivity.this.finish();
//
//                    }
//                }).setCancelable(false);
//                builder.show();
//            }
//        }

        if (!prefs.contains(MainActivity.PASSWORD) || !prefs.getBoolean(MainActivity.FORCE_LOGIN, false)) {
            startActivity(new Intent(this, MainActivity.class));
            this.finish();
        } else {
            final Button loginButton = (Button) findViewById(R.id.btnLogin);
            final EditText pinText = (EditText) findViewById(R.id.txtPin);
            pinText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_GO) {
                        loginButton.performClick();
                        return true;
                    }
                    return false;
                }
            });
            pinText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!TextUtils.isEmpty(pinText.getText())) {
                        loginButton.setEnabled(true);
                    } else {
                        loginButton.setEnabled(false);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String pin = pinText.getText().toString();
                    if (prefs.getString(MainActivity.PASSWORD, "").equals(pin)) {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        LoginActivity.this.finish();
                    } else {
                        Toast.makeText(LoginActivity.this, getString(R.string.incorrect_pin), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
