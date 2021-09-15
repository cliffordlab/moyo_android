package com.cliffordlab.amoss.gui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by ChristopherWainwrightAaron on 1/31/17.
 */

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean("EXIT", false)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAndRemoveTask();
                System.exit(0);
                return;
            } else {
                finishAffinity();
                System.exit(0);
                return;
            }
        }
        Intent intent = new Intent(this, LoginActivity.class);
//        if (settingsUtil.isAmossLoggedIn()) {
//            intent = new Intent(this, UTSWBrowserLogin.class);
//        } else {
//            intent = new Intent(this, LoginActivity.class);
//        }
        startActivity(intent);
        finish();
    }
}
